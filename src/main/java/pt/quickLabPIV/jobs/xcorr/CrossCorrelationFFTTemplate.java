// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.exception.QueryFailedException;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.optimizers.GlobalGeometry;
import pt.quickLabPIV.jobs.optimizers.WorkItemsGeometryOptimizer;

public abstract class CrossCorrelationFFTTemplate extends Job<List<Tile>, XCorrelationResults> {
    private PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
    private final GeometryCache cache = new GeometryCache();
	
	private Range range;
	private LocalGeometry localGeometry;
    private GlobalGeometry globalGeometry;
	
	private ComputationDevice computeDevice;
	private boolean explicit = false;
	private boolean alwaysDispose = false;
	private int[] computeDeviceGeometry;
	private int computeMaxDimensions;
	private boolean normalized = false;
	protected List<Matrix> inputMatricesF;
	protected List<Matrix> inputMatricesG;
	
	private int[] inputGeometry;  //Geometry of the input sub-Matrix (I,J)
	private int[] outputGeometry; //Geometry of the output sub-Matrix (I,J)

	private short numberOfDimensionsUsed;

	public enum EmulationModeEnum {
		None,
		GPU,
		CPU
	}
	
	private EmulationModeEnum emulationMode = EmulationModeEnum.None;
	
	public CrossCorrelationFFTTemplate setEmulationMode(EmulationModeEnum mode) {
		emulationMode = mode;
		return this;
	}
	
	protected boolean isEmulateGPU() {
		return EmulationModeEnum.GPU == emulationMode;
	}
	
	protected boolean isEmulateCPU() {
		return EmulationModeEnum.CPU == emulationMode;
	}
	
	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationFFTTemplate(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		if (matricesF.size() < 1 || matricesG.size() < 1) {
			return;
		}

		Matrix refMatrix = matricesF.get(0);
		int dimI = refMatrix.getHeight();
		int dimJ = refMatrix.getWidth();
				
		for (Matrix matrix : matricesF) {
			if (dimI != matrix.getHeight()) {
				throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along I)");
			}
			
			if (dimJ != matrix.getWidth()) {
				throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along J)");
			}
		}
		
		for (Matrix matrix : matricesG) {
			if (dimI != matrix.getHeight()) {
				throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along I)");
			}
			
			if (dimJ != matrix.getWidth()) {
				throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along J)");
			}
		}

		if (dimI != dimJ) {
			throw new RuntimeException("This FFT standard job can only handle square matrices.");
		}
		
		if (matricesF.size() != matricesG.size()) {
			throw new RuntimeException("The number of matrices in F must be matched with the number of matrices G");
		}
	
		computeDevice = device;
		this.normalized = normalized;
		
		inputMatricesF = matricesF;
		inputMatricesG = matricesG;
		
		inputGeometry = new int[2];
		inputGeometry[0] = dimI;
		inputGeometry[1] = dimJ;
		
		outputGeometry = new int[2];
		outputGeometry[0] = dimI * 2;
		outputGeometry[1] = dimJ * 2;
	}

	public CrossCorrelationFFTTemplate(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		computeDevice = device;
		this.normalized = normalized;
		this.computeDeviceGeometry = computeDeviceGeometry;
		this.computeMaxDimensions = computeDeviceGeometry != null ? computeDeviceGeometry.length : 0;
		
		inputMatricesF = null;
		inputMatricesG = null;
	}
	
	public CrossCorrelationFFTTemplate(final boolean normalized, final ComputationDevice device) {
		computeDevice = device;
		this.normalized = normalized;
		
		inputMatricesF = null;
		inputMatricesG = null;
	}
	
	protected boolean isAlwaysDispose() {
		return alwaysDispose;
	}
	
	protected boolean isExplicit() {
		return explicit;
	}
	
	protected boolean isNormalized() {
		return normalized;
	}
	
	protected ComputationDevice getComputeDevice() {
		return computeDevice;
	}

	protected LocalGeometry getLocalGeometry() {
		return localGeometry;
	}

	protected void setLocalGeometry(LocalGeometry _localGeometry) {
	    localGeometry = _localGeometry;
	}
	
	protected GlobalGeometry getGlobalGeometry() {
		return globalGeometry;
	}
	
	protected void setGlobalGeometry(GlobalGeometry _globalGeometry) {
	    globalGeometry = _globalGeometry;
	}
	
	protected int getNumberOfDimensionsUsed() {
		return numberOfDimensionsUsed;
	}
	
	protected int[] getInputGeometry() {
		return inputGeometry;
	}
	
	protected int[] getOutputGeometry() {
		return outputGeometry;
	}
	
	protected void setOutputGeometry(int _outputGeometry[]) {
	    outputGeometry = _outputGeometry;
	}
		
	protected abstract Logger getLogger();
	
	protected LocalGeometry perfectShuffleFFTOrder(LocalGeometry geom) {
		int inputSize = outputGeometry[1];
		
		int[] inputOrder = new int[inputSize];
		int[] shuffleOrder = new int[inputSize];
		
		for (int i = 0; i < inputSize; i++) {
			inputOrder[i] = i;
			shuffleOrder[i] = i;
		}
		
		//Each outer loop performs an FFT ordering at the respective depth-level: moving the even indices to the left (even side)
		//ordered by increasing index order of even indices and moving the odd indices to the right (odd side) ordered by increseaing order
		//of odd indices.
		//Example:
		//        Level 1                              Level  2                                       Level 3
		//[ a0 a1 a2 a3 a4 a5 a6 a7 ] -> [ [ a0 a2 a4 a6 ] [ a1 a3 a5 a7 ] ] -> [ [ [ a0 a4 ] [ a2 a6 ] ] [ [ a1 a5 ] [ a3 a7 ] ] ]
		//Computational Complexity: Not easy to assess (N=8 -> 6, N=16 -> 24, N=32 -> 80)
		for (int outerSplitFactor=1, outerDepthSize = inputSize; outerDepthSize > 2; outerDepthSize >>>= 1, outerSplitFactor <<= 1) {
			for (int outerSplitIndex = 0; outerSplitIndex < outerSplitFactor; outerSplitIndex++) {
				int outerOffset =  outerSplitIndex * outerDepthSize;
				//These inner loops start by interchanging the odd elements of the even-side (left side) with event elements
				//of the odd-side. As this is not exactly the desired ordering for the FFT at this depth level, additional similar shuffles 
				//must be performed to bring align the indices into order.
				//Example:
				//[ a0 a1 a2 a3 a4 a5 a6 a7 ] -> [ [a0 a4 a2 a6] [a1 a5 a3 a7] -> [ [a0 a2] [a4 a6] [a1 a3] [a5 a7] ] 
				//[ [a0 a2 a4 a6] [a1 a3 a5 a7] ] (finally in FFT order for the second depth level)
				for (int splitFactor=1, depthSize = outerDepthSize; depthSize > 2; depthSize >>>= 1, splitFactor <<= 1) {
					for (int splitIndex = 0; splitIndex < splitFactor; splitIndex++) {
						int innerOffset = splitIndex * depthSize;
						for (int i = 0; i < depthSize/2; i+=2) {
							int src = outerOffset + innerOffset + i + 1;
							int dst = outerOffset + innerOffset + depthSize/2 + i;						
							
							int temp = shuffleOrder[src];
							shuffleOrder[src] = shuffleOrder[dst];
							shuffleOrder[dst] = temp;
						}
					}
				}
			}
		}
		
		/*System.out.print("Shuffle order before: ");
		for (int i = 0; i < inputSize; i++) {
			System.out.print(shuffleOrder[i] + ", ");
		}
		System.out.println("");*/
		
		for (int j = 0; j < inputSize; j++) {
			if (shuffleOrder[j] < j) {
				inputOrder[j] = 0;
				shuffleOrder[j] = 0;
			}
		}

		/*System.out.print("Shuffle order after: ");
		for (int i = 0; i < inputSize; i++) {
			System.out.print(shuffleOrder[i] + ", ");
		}
		System.out.println("");*/
		
		geom.inputOrder = inputOrder;
		geom.shuffleOrder = shuffleOrder;
		
		return geom;
	}
	
	protected float[] initEulerTable(int N) {
		float[] w = new float[N];
		
		for (int n = 0; n < N; n++) {
			//This should be the Euler table of exp(-2j*Pi*n/N) however due to the NOTES A) above, it suffices to have sin(2*Pi*n/N)
			w[n] = (float)FastMath.sin(2.0f*FastMath.PI*(float)n/(float)N);
		}
		
		return w;
	}
	
	protected LocalGeometry refineLocalGeometry(LocalGeometry geom) {
		return geom;
	}
	
    protected int getLocalMemoryBytesPerEntry() {
        return Float.BYTES * 4;
    }   

    protected abstract int getCompiledKernelPreferredWorkItemMultipleImpl() throws QueryFailedException;    
    private int getCompiledKernelPreferredWorkItemMultiple() {
        try {
            return getCompiledKernelPreferredWorkItemMultipleImpl();
        } catch (QueryFailedException e) {
            throw new JobAnalyzeException("Failed to query PreferredWorkItemMultiple for device: " + 
                    computeDevice.getDeviceName() + ", with id" + computeDevice.getDeviceId());
        }
    }
    
    protected abstract int getCompiledKernelMaxGroupThreadsImpl() throws QueryFailedException;
    private int getCompiledKernelMaxGroupThreads() {
        try {
            return getCompiledKernelMaxGroupThreadsImpl();
        } catch (QueryFailedException e) {
            throw new JobAnalyzeException("Failed to query MaxGroupThreads for device: " + 
                    computeDevice.getDeviceName() + ", with id" + computeDevice.getDeviceId());
        }
    }
	
	protected void computeLocalGeometry(Logger logger, Device device) {
		int preferredWorkItemMultiple;
		int maxWorkItems;
		int linesPerWorkGroup;
		int columnsPerWorkGroup;		

        final int kernelPreferredWorkItemMultiple = getCompiledKernelPreferredWorkItemMultiple();
        final int kernelMaxWorkGroup = getCompiledKernelMaxGroupThreads();

		linesPerWorkGroup = 0;
		int maxLinesPerWorkGroup = 0;
		if (Device.TYPE.GPU.equals(device.getType()) || isEmulateGPU()) {
			preferredWorkItemMultiple = computeDevice.getMinGroupThreads();
			maxWorkItems = computeDevice.getMaxThreadsPerThreadGroup();

			//TODO In order for this to take effect it is needed to know in advance how many workitems are needed in total to process the whole matrices 
	        /*int multiplier = computeDevice.getMaxComputeUnits() * computeDevice.getThreadsPerComputeUnit() / computeDevice.getGreatestThreadGroupCommonDivisor();
	        DeviceRuntimeConfiguration runtimeConfig = singleton.getPIVRunParameters().getDeviceConfiguration(computeDevice.getDeviceId());
	        int cpuThreads = runtimeConfig.getNrOfCpuThreadsForDevice();
	        int leftOverFactor = 1;
	        float usageFactor = (float)cpuThreads * leftOverFactor / (float)multiplier;
	        while (usageFactor < 0.75f ) {
	            leftOverFactor *= 2;
	            usageFactor = (float)cpuThreads * leftOverFactor / (float)multiplier;
	        }*/
			
			if ( Device.TYPE.GPU.equals(device.getType()) ) {
    			if (kernelPreferredWorkItemMultiple != preferredWorkItemMultiple) {
    			    logger.warn("Changed preferredWorkItemMultiple from: " + preferredWorkItemMultiple +
    			      " to: " + kernelPreferredWorkItemMultiple + " per device driver recommendation: " + device.getDeviceId());
    			    preferredWorkItemMultiple = kernelPreferredWorkItemMultiple; 
    			}
    			if (kernelMaxWorkGroup != maxWorkItems) {
                    logger.warn("Changed maxWorkGroup from: " + maxWorkItems +
                            " to: " + kernelMaxWorkGroup + " per device driver recommendation: " + device.getDeviceId());
                    maxWorkItems = kernelMaxWorkGroup;     			    
    			}
			}
			
			int localSharedMemorySize = (int)computeDevice.getLocalMemoryBytes();
			maxLinesPerWorkGroup = localSharedMemorySize / (outputGeometry[1] * getLocalMemoryBytesPerEntry());
			linesPerWorkGroup = maxLinesPerWorkGroup;
			if (maxLinesPerWorkGroup > outputGeometry[0]) {
				linesPerWorkGroup = outputGeometry[0];
			}
			
			//Although the GPU implementation does not require this, it is more efficient even if we don't use all
			//local memory, but make full use of the all the local work group.
			if (outputGeometry[0]/linesPerWorkGroup*linesPerWorkGroup != outputGeometry[0]) {
				linesPerWorkGroup -= outputGeometry[0] - outputGeometry[0]/linesPerWorkGroup*linesPerWorkGroup;
			}
			
			//linesPerWorkGroup--;
		} else {
			preferredWorkItemMultiple = 128;
			maxWorkItems = 256;
			linesPerWorkGroup = outputGeometry[0];
		}
			
		columnsPerWorkGroup = 2;
		int minLeftOverWorkItems = Integer.MAX_VALUE;
		int workItemSize = 0;
		//If kernel allows workItems that are not a power of two... 
		//for (int workItems = preferredWorkItemMultiple; workItems <= maxWorkItems + 2*preferredWorkItemMultiple; workItems+=preferredWorkItemMultiple) {
		//Otherwise
		int bits = (int)FastMath.ceil(FastMath.log(2, linesPerWorkGroup));
		int adjustedLinesPerWorkGroup = (int)FastMath.pow(2, bits);
		for (int workItems = preferredWorkItemMultiple; workItems <= maxWorkItems; workItems*=2) {
			int newColumnsPerWorkGroup = workItems / adjustedLinesPerWorkGroup;
			if (newColumnsPerWorkGroup > 0) {
				if (newColumnsPerWorkGroup > outputGeometry[1] / 2) {
					newColumnsPerWorkGroup = outputGeometry[1] / 2;
				}
				int leftOverWorkItems = workItems - (newColumnsPerWorkGroup * adjustedLinesPerWorkGroup);
				if (minLeftOverWorkItems > leftOverWorkItems || minLeftOverWorkItems == leftOverWorkItems && workItems <= maxWorkItems) {
					minLeftOverWorkItems = leftOverWorkItems;
					columnsPerWorkGroup = newColumnsPerWorkGroup;
					workItemSize = workItems;
				}
			}
		}

		//For kernels that require an exact power of two -- note that 192 is not a power of two - last divisor is 3 
		/*if (outputGeometry[1]/columnsPerWorkGroup*columnsPerWorkGroup != outputGeometry[1]) {
			columnsPerWorkGroup -= outputGeometry[1] - outputGeometry[1]/columnsPerWorkGroup*columnsPerWorkGroup;
		}*/
		
		if (columnsPerWorkGroup < 2) {
			//TODO Fall-back to FFT Std kernel?
			columnsPerWorkGroup = 2;
			do {
				minLeftOverWorkItems = workItemSize - (columnsPerWorkGroup * linesPerWorkGroup);
				if (minLeftOverWorkItems < 0 && workItemSize < maxWorkItems) {
					workItemSize *= 2;
				} else if (workItemSize >= maxWorkItems) {
				    break;
				}
			} while (minLeftOverWorkItems < 0);
		} else {
		
		}
		
		logger.info("Lines per work-group: {}, Columns per work-group: {}.", linesPerWorkGroup, columnsPerWorkGroup);
		if (minLeftOverWorkItems > preferredWorkItemMultiple / 10) {
			logger.trace("Discarding {} work-items.", minLeftOverWorkItems);
		}
		
		if (columnsPerWorkGroup * linesPerWorkGroup > computeDevice.getMaxThreadsPerThreadGroup()) {
			throw new JobAnalyzeException("Computed work group exceed max. allowed by the computation device");
		}
        
        int exactNumberOfGroups = (int)(computeDevice.getMaxComputeUnits() * computeDevice.getThreadsPerComputeUnit()) / (linesPerWorkGroup*columnsPerWorkGroup);
        if (exactNumberOfGroups == 0) {
            exactNumberOfGroups = 1;
        }
        final float log2Val = (float)FastMath.log(2, exactNumberOfGroups);
        final int log2IntVal = (int)FastMath.ceil(log2Val);
        int minNumberOfGroups = (int)FastMath.pow(2, log2IntVal);       
        if (minNumberOfGroups <= 0) {
            minNumberOfGroups = 1;
            throw new JobAnalyzeException("Min. Number of groups must be greater than 0"); 
        }
        
		localGeometry = new LocalGeometry();
		localGeometry.localSizeX = columnsPerWorkGroup;
		localGeometry.localSizeY = linesPerWorkGroup;
		localGeometry.minNumberOfGroups = minNumberOfGroups;

		localGeometry.w = initEulerTable(outputGeometry[1]);
		localGeometry = perfectShuffleFFTOrder(localGeometry);
		localGeometry = refineLocalGeometry(localGeometry);		
	}
	
	protected void computeGlobalGPUGeometry(final int numberOfUsedTiles, int maxTilesInI, int maxTilesInJ) {
		int numberOfPasses = 1;
		int factorJ = (int)FastMath.pow(2, FastMath.floor(FastMath.log(2, maxTilesInJ)));
		int factorI = (int)FastMath.pow(2, FastMath.floor(FastMath.log(2, maxTilesInI)));
		
		int numberOfGroups = localGeometry.minNumberOfGroups;
		
		int numberOfGroupsInK = 0;
		int numberOfGroupsInI = 0;
		int numberOfGroupsInJ = factorJ;
	    if (numberOfGroups < numberOfGroupsInJ) {
			numberOfDimensionsUsed = 2;
			numberOfGroupsInI = 1;
			numberOfGroupsInJ = numberOfGroups;
		} else if (numberOfGroups/factorJ < factorI) {
			numberOfDimensionsUsed = 2;
			numberOfGroupsInI = numberOfGroups / factorJ;
			if (numberOfGroupsInI * factorJ != numberOfGroups) {
				throw new JobAnalyzeException("Not a power of two when computing GPU geometry");
			}
		} else {
			numberOfDimensionsUsed = 3;
			numberOfGroupsInI = factorI;
			numberOfGroupsInK = numberOfGroups / (factorI * factorJ);
			if (numberOfGroupsInK * (factorI * factorJ) != numberOfGroups) {
				throw new JobAnalyzeException("Not a power of two when computing GPU geometry");
			}
		}
		
		if (numberOfUsedTiles < numberOfGroupsInJ) {
			numberOfDimensionsUsed = 2;
			numberOfGroupsInI = 1;
			numberOfGroupsInK = 0;
		} else if (numberOfUsedTiles < numberOfGroupsInJ * numberOfGroupsInI) {
			numberOfDimensionsUsed = 2;
			numberOfGroupsInI = numberOfUsedTiles / numberOfGroupsInJ;
			numberOfGroupsInK = 0;
			if (numberOfGroupsInI * numberOfGroupsInJ < numberOfUsedTiles) {
				numberOfGroupsInI++;
			}
		} else if (numberOfUsedTiles < numberOfGroupsInJ * numberOfGroupsInI * numberOfGroupsInK) {
			numberOfDimensionsUsed = 3;
			numberOfGroupsInK = numberOfUsedTiles / (numberOfGroupsInJ * numberOfGroupsInI);
			if (numberOfGroupsInK * numberOfGroupsInI * numberOfGroupsInJ < numberOfUsedTiles) {
				numberOfGroupsInK++;
			}
		}
		
		numberOfGroups = FastMath.min(numberOfGroups, numberOfUsedTiles);
		
		numberOfPasses = numberOfUsedTiles / numberOfGroups;
		if (numberOfPasses * numberOfGroups < numberOfUsedTiles) {
			numberOfPasses++;
		}

		
		globalGeometry = new GlobalGeometry();
		globalGeometry.numberOfUsedTiles = numberOfUsedTiles;
		globalGeometry.numberOfPasses = numberOfPasses;
		globalGeometry.workItemsK = numberOfGroupsInK;
		globalGeometry.matricesI = numberOfGroupsInI;
		globalGeometry.matricesJ = numberOfGroupsInJ;
		
		globalGeometry.workItemsPerMatrixI = localGeometry.localSizeY;
		globalGeometry.workItemsPerMatrixJ = localGeometry.localSizeX;
		
		globalGeometry.workItemsI = numberOfGroupsInI * localGeometry.localSizeY;
		globalGeometry.workItemsJ = numberOfGroupsInJ * localGeometry.localSizeX;
	}

	private void computeGlobalCPUGeometry(final int numberOfUsedTiles, int maxTilesInI, int maxTilesInJ) {
		int numberOfPasses = 1;
		
		if (numberOfUsedTiles <= maxTilesInJ) {
			if (globalGeometry == null || globalGeometry.numberOfUsedTiles != numberOfUsedTiles) {
				globalGeometry = new GlobalGeometry();
				globalGeometry.workItemsK = 1;
				globalGeometry.workItemsI = localGeometry.localSizeY;
				globalGeometry.workItemsJ = localGeometry.localSizeX * numberOfUsedTiles;
				
		    	globalGeometry.matricesI = 1;
		    	globalGeometry.matricesJ = numberOfUsedTiles;
		    	
		    	globalGeometry.workItemsPerMatrixI = localGeometry.localSizeY;
		    	globalGeometry.workItemsPerMatrixJ = localGeometry.localSizeX;

				globalGeometry.wastedWorkItems = 0;
				
				globalGeometry.numberOfUsedTiles = numberOfUsedTiles;
			}
		} else if (numberOfUsedTiles <= maxTilesInI * maxTilesInJ){
			/*bestGeometry = new OptimizedGeometry();
			
			bestGeometry.workItemsK = 1;
			bestGeometry.workItemsI = numberOfMatrices / maxTilesInJ;
			boolean isWastedWorkItems = false;
			if (bestGeometry.workItemsI * maxTilesInJ < numberOfMatrices) {
				bestGeometry.workItemsI++;
				isWastedWorkItems = true;
			}
			bestGeometry.workItemsI *= geom.localSizeY;
			bestGeometry.workItemsJ = maxTilesInJ*geom.localSizeX;
			
			bestGeometry.matricesI = bestGeometry.workItemsI/geom.localSizeY;
			bestGeometry.matricesJ = maxTilesInJ;
			
			bestGeometry.workItemsPerMatrixI = geom.localSizeY;
			bestGeometry.workItemsPerMatrixJ = geom.localSizeX;
			
			bestGeometry.wastedWorkItems = 0;
			if (isWastedWorkItems) {
				bestGeometry.wastedWorkItems = bestGeometry.workItemsI*maxTilesInJ - numberOfMatrices%maxTilesInJ;
			}
			
			bestGeometry.usedMatrices = numberOfMatrices;*/

			
			if (globalGeometry == null || globalGeometry.numberOfUsedTiles != numberOfUsedTiles) {
				globalGeometry = 
						WorkItemsGeometryOptimizer.optimizeGeometry2D(numberOfUsedTiles, localGeometry.localSizeY, localGeometry.localSizeX, computeDeviceGeometry[0], computeDeviceGeometry[1]);
			}
		} else {
			//TODO Also optimize me...
			globalGeometry = new GlobalGeometry();
			globalGeometry.workItemsK = numberOfUsedTiles / (maxTilesInJ * maxTilesInI);
			boolean isWastedWorkItems = false;
			if (globalGeometry.workItemsK * (maxTilesInJ * maxTilesInI) < numberOfUsedTiles) {
				globalGeometry.workItemsK++;
				isWastedWorkItems = true;
			}
			globalGeometry.workItemsI = maxTilesInI*localGeometry.localSizeY;
			globalGeometry.workItemsJ = maxTilesInJ*localGeometry.localSizeX;
			
			globalGeometry.matricesI = maxTilesInI;
			globalGeometry.matricesJ = maxTilesInJ;
			
			globalGeometry.workItemsPerMatrixI = localGeometry.localSizeY;
			globalGeometry.workItemsPerMatrixJ = localGeometry.localSizeX;
			
			globalGeometry.wastedWorkItems = 0;
			if (isWastedWorkItems) {
				//TODO FIXME wrongly computed
				globalGeometry.wastedWorkItems = maxTilesInI*globalGeometry.workItemsI*maxTilesInJ - numberOfUsedTiles%(maxTilesInI*maxTilesInJ);
			}
			
			globalGeometry.numberOfUsedTiles = numberOfUsedTiles;
			
		}
		
		globalGeometry.numberOfPasses = numberOfPasses;
	}
	
	protected abstract void analyzeTemplate(final int numberOfUsedTiles);

	private void analyzeTilesHelper(List<Tile> tilesF, List<Tile> tilesG) {
		if (tilesF.size() < 1 || tilesG.size() < 1) {
			return;
		}
		
		Matrix refMatrix = tilesF.get(0).getMatrix();
		int dimI = refMatrix.getHeight();
		int dimJ = refMatrix.getWidth();
				
		for (Tile tile : tilesF) {
			if (dimI != tile.getMatrix().getHeight()) {
				throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along I)");
			}
			
			if (dimJ != tile.getMatrix().getWidth()) {
				throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along J)");
			}
		}
		
		for (Tile tile : tilesG) {
			if (dimI != tile.getMatrix().getHeight()) {
				throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along I)");
			}
			
			if (dimJ != tile.getMatrix().getWidth()) {
				throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along J)");
			}
		}

		if (dimI != dimJ) {
			throw new RuntimeException("This FFT standard job can only handle square matrices.");
		}

		
		if (tilesF.size() != tilesG.size()) {
			throw new RuntimeException("The number of matrices in F must be matched with the number of matrices G");
		}
	
		inputGeometry = new int[2];
		inputGeometry[0] = dimI;
		inputGeometry[1] = dimJ;
		
		outputGeometry = new int[2];
		outputGeometry[0] = dimI * 2;
		outputGeometry[1] = dimJ * 2;
		
		inputMatricesF = null;
		inputMatricesG = null;		
	}

	@Override
	public void analyze() {
		Logger logger = getLogger();
		
		List<Tile> tilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
		List<Tile> tilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);

		int numberOfUsedTiles;
		if (inputMatricesF != null && inputMatricesG != null &&
			tilesF == null && tilesG == null) {
			numberOfUsedTiles = inputMatricesF.size();
		} else {
			numberOfUsedTiles = tilesF.size();
			
			analyzeTilesHelper(tilesF, tilesG);
		}
		
        logger.info("Compute Device: {}.", computeDevice.getDeviceName());
        logger.trace("Max. work-group size: {}.", computeDevice.getMaxThreadsPerThreadGroup());  //1024 (MaxGroupSize) - number of WorkGroups of work items
		
        Device device = computeDevice.getAparapiDevice();
        if (computeDeviceGeometry == null) {
        	computeDeviceGeometry = new int[] {computeDevice.getMaxWorkItems()[0], 
        			computeDevice.getMaxWorkItems()[1], computeDevice.getMaxWorkItems()[2]};
        	logger.info("Using compute device work item sizes - I: {}" + 
        			", J: {}, K: {}",computeDeviceGeometry[0], computeDeviceGeometry[1], computeDeviceGeometry[2]);
        	computeMaxDimensions = computeDevice.getMaxWorkItemDimensions();
        	logger.info("Using compute device maximum compute item dimensions= {}", computeMaxDimensions);

        } else {
        	logger.info("Using specified maximum compute item dimensions= {}", computeMaxDimensions);
        	logger.info("Using specified work item sizes - I: {}" + 
        			", J: {}, K: ", computeDeviceGeometry[0], computeDeviceGeometry[1], computeDeviceGeometry[2]);
        }
                
        if (computeDeviceGeometry[0] < inputGeometry[0]) {
        	throw new JobAnalyzeException("Input matrix dimensions along I exceed computation capabilities of device");
        }
        
        if (computeDeviceGeometry[1] < inputGeometry[1]) {
        	throw new JobAnalyzeException("Input matrix dimensions along J exceed computation capabilities of device");
        }

        localGeometry = cache.getGeometry(inputGeometry[0]);
        if (localGeometry == null) {
			computeLocalGeometry(logger, device);

			cache.setGeometry(inputGeometry[0], localGeometry);
        }
        
        
        int maxTilesInI = computeDeviceGeometry[0] / localGeometry.localSizeY;
        int maxTilesInJ = computeDeviceGeometry[1] / localGeometry.localSizeX;
        int maxTilesInK = 0;
        numberOfDimensionsUsed = 2;
        if ((maxTilesInI * maxTilesInJ) < numberOfUsedTiles) {
        	//3rd dimension of work-item will be needed
        	numberOfDimensionsUsed = 3;
        }
        
        if (computeMaxDimensions < numberOfDimensionsUsed) {
        	throw new JobAnalyzeException(numberOfDimensionsUsed + " dimensions required for compute device, but it only supports " + 
        			computeDevice.getMaxWorkItemDimensions() + " dimensions");
        }
        
        if (numberOfDimensionsUsed == 3) {
        	maxTilesInK = computeDeviceGeometry[2];
        	
        	if (maxTilesInK * maxTilesInI * maxTilesInJ < numberOfUsedTiles) {
        		if (!Device.TYPE.GPU.equals(device.getType()) && !isEmulateGPU()) { //GPU is running with multiple passes, so no issue.
        			throw new JobAnalyzeException("Input problem dimensions/size exceed computation capabilities of device");
        		}
        	}
        }

        if (isEmulateGPU()) {
        	computeGlobalGPUGeometry(numberOfUsedTiles, maxTilesInI, maxTilesInJ);
        } else if (isEmulateCPU()) {
        	computeGlobalCPUGeometry(numberOfUsedTiles, maxTilesInI, maxTilesInJ);
        } else if (Device.TYPE.CPU.equals(device.getType())) {
	        computeGlobalCPUGeometry(numberOfUsedTiles, maxTilesInI, maxTilesInJ);
		} else if (Device.TYPE.GPU.equals(device.getType())) {  //GPU is running with multiple passes, so is just a single matrix.
			computeGlobalGPUGeometry(numberOfUsedTiles, maxTilesInI, maxTilesInJ);
        } else {
        	throw new JobAnalyzeException("Unknown execution mode for device: " + computeDevice.getDeviceName());
        }
        //totalNumberOfTiles = numberOfTilesInK * numberOfTilesInI * numberOfTilesInJ;
        
        analyzeTemplate(numberOfUsedTiles);
	}
	
	protected Range checkAndUpdateRangeIfRequired() {
		Device aparapiDevice = computeDevice.getAparapiDevice();
		
		if (range == null ||
			range.getGlobalSize(0) != globalGeometry.workItemsJ ||
			range.getGlobalSize(1) != globalGeometry.workItemsI ||
			range.getGlobalSize(2) != globalGeometry.workItemsK) {
	        if (Device.TYPE.JTP.equals(aparapiDevice.getType()) || Device.TYPE.SEQ.equals(aparapiDevice.getType())) {
	        	if (numberOfDimensionsUsed == 2) { 
			        range = Range.create2D(
					        globalGeometry.workItemsJ, globalGeometry.workItemsI,
			        		localGeometry.localSizeX, localGeometry.localSizeY);
	        	} else {
	        		range = Range.create3D(
	        				globalGeometry.workItemsJ, globalGeometry.workItemsI, globalGeometry.workItemsK,
	        				localGeometry.localSizeX, localGeometry.localSizeY, 1);
	        	}
			} else {
				if (numberOfDimensionsUsed == 2) {
			        range = Range.create2D(aparapiDevice,
			        		globalGeometry.workItemsJ, globalGeometry.workItemsI,
			        		localGeometry.localSizeX, localGeometry.localSizeY);
				} else {
					range = Range.create3D(aparapiDevice,
							globalGeometry.workItemsJ, globalGeometry.workItemsI, globalGeometry.workItemsK,
							localGeometry.localSizeX, localGeometry.localSizeY, 1);
				}
			}
		}
		
        getLogger().info("Passes: {} - K: {} - items In I: {} - items In J: {}", 
                globalGeometry.numberOfPasses, 
                globalGeometry.workItemsK, globalGeometry.workItemsI, globalGeometry.workItemsJ);
		
		return range;
	}
}
