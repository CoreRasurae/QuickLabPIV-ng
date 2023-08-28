// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.exception.QueryFailedException;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobComputeException;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.maximum.MaxCrossResult;
import pt.quickLabPIV.util.Factorization;

public class CrossCorrelationJob extends Job<List<Tile>, XCorrelationResults> {
	private static Logger logger = LoggerFactory.getLogger(CrossCorrelationJob.class);
	
	private Kernel kernel;
	private int[] tilesGeometry;
	private float[] matrixOut;
	
	private ComputationDevice computeDevice;
	private int[] computeDeviceGeometry;
	private int computeMaxDimensions;
	private boolean normalized = false;
	private List<Matrix> inputMatricesF;
	private List<Matrix> inputMatricesG;
	private int numberOfUsedTiles;
	
	private int[] inputGeometry;  //Geometry of the input sub-Matrix (I,J)
	private int[] outputGeometry; //Geometry of the output sub-Matrix (I,J)
	
	private float[] matrixInF;  //Input matrix F (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry
	private float[] matrixInG;  //Input matrix G (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry
	private int numberOfTilesInI; //Number of tiled matrices across rows
	private int numberOfTilesInJ; //Number of tiled matrices across columns
	private int numberOfTilesInK; //Number of tiled matrices across K(Z) third-dimension
	private int totalNumberOfTiles;
	private short numberOfDimensions;
	private List<Matrix> outputMatrices;
	private int[] threadOutputStart;  //Contains the start index in the global tiles 1D array for the corresponding output matrix (that contains the sub-matrix to process)
	private int[] threadOffsetI; //Contains the result I offset within the output subMatrix that the work-item is to compute
	private int[] threadOffsetJ; //Contains the result J offset within the output subMatrix that the work-item is to compute
	
	private boolean normalizeAndRegularizeInputMatrices = true;
	
	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		constructorHelper(device, normalized, matricesF, matricesG);
	}

    private boolean constructorHelper(final ComputationDevice device, final boolean normalized,
            final List<Matrix> matricesF, final List<Matrix> matricesG) {
        if (matricesF.size() < 1 || matricesG.size() < 1) {
			return false;
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
		
		if (matricesF.size() != matricesG.size()) {
			throw new RuntimeException("The number of matrices in F must be matched with the number of matrices G");
		}
	
		computeDevice = device;
		this.normalized = normalized;
		
		inputGeometry = new int[2];
		inputGeometry[0] = dimI;
		inputGeometry[1] = dimJ;
		
		outputGeometry = new int[2];
		outputGeometry[0] = dimI * 2 - 1;
		outputGeometry[1] = dimJ * 2 - 1;
		
		inputMatricesF = matricesF;
		inputMatricesG = matricesG;
		
		return true;
    }

    public CrossCorrelationJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG, final boolean _normalizeAndRegularizeInputMatrices) {
        constructorHelper(device, normalized, matricesF, matricesG);
        normalizeAndRegularizeInputMatrices = _normalizeAndRegularizeInputMatrices;
    }
    
	public CrossCorrelationJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		computeDevice = device;
		this.normalized = normalized;
		this.computeDeviceGeometry = computeDeviceGeometry;
		this.computeMaxDimensions = computeDeviceGeometry != null ? computeDeviceGeometry.length : 0;
	}

    public CrossCorrelationJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry, final boolean _normalizeAndRegularizeInputMatrices) {
        computeDevice = device;
        this.normalized = normalized;
        this.computeDeviceGeometry = computeDeviceGeometry;
        this.computeMaxDimensions = computeDeviceGeometry != null ? computeDeviceGeometry.length : 0;
        normalizeAndRegularizeInputMatrices = _normalizeAndRegularizeInputMatrices;
    }

	
    private void analyzeTilesHelper(final List<Tile> tilesF, final List<Tile> tilesG) {
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
		
		if (tilesF.size() != tilesG.size()) {
			throw new RuntimeException("The number of matrices in F must be matched with the number of matrices G");
		}
	
		inputGeometry = new int[2];
		inputGeometry[0] = dimI;
		inputGeometry[1] = dimJ;
		
		outputGeometry = new int[2];
		outputGeometry[0] = dimI * 2 - 1;
		outputGeometry[1] = dimJ * 2 - 1;
		
		inputMatricesF = null;
		inputMatricesG = null;
	}
	
	public void analyze() {
		List<Tile> inputTilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
		List<Tile> inputTilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);
		
		if (inputMatricesF == null || inputMatricesG == null || 
				(inputTilesF != null && inputTilesG != null)) {
			analyzeTilesHelper(inputTilesF, inputTilesG);
			numberOfUsedTiles = inputTilesF.size();
		} else {
			numberOfUsedTiles = inputMatricesF.size();
		}
		
		logger.info("Compute Device: " + computeDevice.getDeviceName());
		logger.trace("Max work-group size: " + computeDevice.getMaxThreadsPerThreadGroup()); //1024 (MaxGroupSize) - number of WorkGroups of work items
		
        if (computeDeviceGeometry == null) {
        	computeDeviceGeometry = new int[] {computeDevice.getMaxWorkItems()[0], 
        			computeDevice.getMaxWorkItems()[1], computeDevice.getMaxWorkItems()[2]};
        	logger.info("Using compute device work item sizes - I:" + computeDeviceGeometry[0] +
        			", J: " + computeDeviceGeometry[1] + ", K: " + computeDeviceGeometry[2]);
        	computeMaxDimensions = computeDevice.getMaxWorkItemDimensions();
        	logger.info("Using compute device maximum compute item dimensions= " + computeMaxDimensions);

        } else {
        	logger.info("Using specified maximum compute item dimensions= " + computeMaxDimensions);
        	logger.info("Using specified work item sizes - I:" + computeDeviceGeometry[0] +
        			", J: " + computeDeviceGeometry[1] + ", K: " + computeDeviceGeometry[2]);
        }
                
        if (computeDeviceGeometry[0] < inputGeometry[0]) {
        	throw new JobAnalyzeException("Input matrix dimensions along I exceed computation capabilities of device");
        }
        
        if (computeDeviceGeometry[1] < inputGeometry[1]) {
        	throw new JobAnalyzeException("Input matrix dimensions along J exceed computation capabilities of device");
        }
        
        int maxTilesInI = computeDeviceGeometry[0] / outputGeometry[0];
        int maxTilesInJ = computeDeviceGeometry[1] / outputGeometry[1];
        int maxTilesInK = 0;

        numberOfDimensions = 2;
        if ((maxTilesInI * maxTilesInJ) < numberOfUsedTiles) {
        	//3rd dimension of work-item will be needed
        	numberOfDimensions = 3;
        }
        
        if (computeMaxDimensions < numberOfDimensions) {
        	throw new JobAnalyzeException(numberOfDimensions + " dimensions required for compute device, but it only supports " + 
        			computeDevice.getMaxWorkItemDimensions() + " dimensions");
        }
        
        if (numberOfDimensions == 3) {
        	maxTilesInK = computeDeviceGeometry[2];
        	
        	if (maxTilesInK * maxTilesInI * maxTilesInJ < numberOfUsedTiles) {
        		throw new JobAnalyzeException("Input problem dimensions/size exceed computation capabilities of device");
        	}
        }
                
        if (numberOfUsedTiles <= maxTilesInJ) {
        	numberOfTilesInK = 1;
        	numberOfTilesInI = 1;
        	numberOfTilesInJ = numberOfUsedTiles;
        } else if (numberOfUsedTiles <= maxTilesInI * maxTilesInJ){
        	numberOfTilesInK = 1;
        	numberOfTilesInI = numberOfUsedTiles/maxTilesInJ;
        	if (numberOfTilesInI * maxTilesInJ < numberOfUsedTiles) {
        		numberOfTilesInI++;
        	}
        	numberOfTilesInJ = maxTilesInJ;
        } else {
        	numberOfTilesInK = numberOfUsedTiles / (maxTilesInJ * maxTilesInI);
        	if (numberOfTilesInK * (maxTilesInJ * maxTilesInI) < numberOfUsedTiles) {
        		numberOfTilesInK++;
        	}
        	numberOfTilesInI = maxTilesInI;
        	numberOfTilesInJ = maxTilesInJ;
        }
        
        totalNumberOfTiles = numberOfTilesInK * numberOfTilesInI * numberOfTilesInJ;

        if (matrixInF == null || matrixInF.length < totalNumberOfTiles * (inputGeometry[1]+1) * (inputGeometry[0]+1)) {
	        matrixInF = new float[totalNumberOfTiles * (inputGeometry[1]+1) * (inputGeometry[0]+1)]; //Total matrix size including required left and up zero padding
	        matrixInG = new float[totalNumberOfTiles * (inputGeometry[1]+1) * (inputGeometry[0]+1)]; //Total matrix size including required left and up zero padding
	        threadOutputStart = new int[totalNumberOfTiles * outputGeometry[1] * outputGeometry[0]]; //Top-left of the current tile (I)
	        threadOffsetI = new int[totalNumberOfTiles * outputGeometry[0]];                         //Xcorr(i,j) i parameters for the current sub-matrix
	        threadOffsetJ = new int[totalNumberOfTiles * outputGeometry[1]];                         //Xcorr(i,j) j parameters for the current sub-matrix
        }
            
        int matrixIndex = 0;
        for (int tileIndexK = 0; tileIndexK < numberOfTilesInK; tileIndexK++) {
	        for (int tileIndexI = 0; tileIndexI < numberOfTilesInI; tileIndexI++) {
	        	for (int tileIndexJ = 0; tileIndexJ < numberOfTilesInJ; tileIndexJ++) {
	        		int destinationOffset = (tileIndexK * numberOfTilesInI * numberOfTilesInJ * (inputGeometry[0]+1) * (inputGeometry[1]+1)) +
	        								(tileIndexI * numberOfTilesInJ * (inputGeometry[0]+1) * (inputGeometry[1]+1)) + 
	        								(tileIndexJ * (inputGeometry[0]+1) * (inputGeometry[1]+1));
	        		
	        		if (matrixIndex < numberOfUsedTiles) {
	        			Matrix matrixF = null;
	        			Matrix matrixG = null;
	        			
	        			if (inputMatricesF != null || inputMatricesG != null) {
		        			matrixF = inputMatricesF.get(matrixIndex);
		        			matrixG = inputMatricesG.get(matrixIndex);
	        			} else {
	        				matrixF = inputTilesF.get(matrixIndex).getMatrix();
	        				matrixG = inputTilesG.get(matrixIndex).getMatrix();
	        			}
		        		
	        			//FIXME Ideally all the cross-correlations should be normalized at each FFT step, even if no regularization is employed.
	                    //There is no point in scaling the FFT results, because:
	                    //- The FFT is not normalized, it overflows and overflows the float representation with 16Bits images, and does not cause Inf or NaN,
	                    //  thus producing completely invalid results.
	                    //So we artificially normalize it before doing the computation. This must be done in the implementation.
	        			if (normalizeAndRegularizeInputMatrices) {
    	                    float[][] newMatrixFF = new float[matrixF.getHeight()][matrixF.getWidth()];
    	                    matrixF.copyMatrixTo2DArrayAndNormalizeAndOffset(newMatrixFF, 0, 0);
    	                    float[][] newMatrixGF = new float[matrixF.getHeight()][matrixF.getWidth()];
    	                    matrixG.copyMatrixTo2DArrayAndNormalizeAndOffset(newMatrixGF, 0, 0);
    
    	                    Matrix newMatrixF = new MatrixFloat(matrixF.getHeight(), matrixF.getWidth(), 17.0f);
    	        			newMatrixF.copyMatrixFrom2DArray(newMatrixFF, 0, 0);
    	        			Matrix newMatrixG = new MatrixFloat(matrixG.getHeight(), matrixG.getWidth(), 17.0f);
    	        			newMatrixG.copyMatrixFrom2DArray(newMatrixGF, 0, 0);

                            newMatrixF.copyMatrixTo1PaddedArray(matrixInF, destinationOffset);
                            newMatrixG.copyMatrixTo1PaddedArray(matrixInG, destinationOffset);
	        			} else {
	        			    matrixF.copyMatrixTo1PaddedArray(matrixInF, destinationOffset);
                            matrixG.copyMatrixTo1PaddedArray(matrixInG, destinationOffset);
	        			}
	        		}
	        			        		
	        		matrixIndex++;
	    		}
	        }
        }
        
        for (int tileIndexI = 0; tileIndexI < numberOfTilesInI; tileIndexI++) {
        	//Fill in tiles Offset in I direction (Offset within the tile)
    		int offset=0;
    		for (int i = -outputGeometry[0]/2; i <= outputGeometry[0]/2; i++) {
    			threadOffsetI[tileIndexI * outputGeometry[0] + offset++] = i;
    		}

    		//Fill in the given tile top-left index in the input tiles 1D array (will be the same for every K plus a known constant offset)
    		//Maps from 3D GPU coordinates to 1D array coordinates
        	for (int tileIndexJ = 0; tileIndexJ < numberOfTilesInJ; tileIndexJ++) {
        		for (int i = 0; i < outputGeometry[0]; i++) {
        			for (int j = 0; j < outputGeometry[1]; j++) {
        				//Start index of the tile in I + offset off the i index in the tile + offset of the J tile index +  offset of J index =
        				//linear index of the start of output result matrix
        				threadOutputStart[(tileIndexI * outputGeometry[0] * numberOfTilesInJ * outputGeometry[1]) + 
        				                   (i * numberOfTilesInJ * outputGeometry[1]) + (tileIndexJ *  outputGeometry[0]) + j] = 
        				                   tileIndexI * numberOfTilesInJ * outputGeometry[1] * outputGeometry[0] + 
        				                   tileIndexJ * outputGeometry[1] * outputGeometry[0];
        			}
        		}        			        	
        	}
        }
        
    	for (int tileIndexJ = 0; tileIndexJ < numberOfTilesInJ; tileIndexJ++) {
    		//Fill in tiles Offset in J direction (Offset within the tile)
    		int offset = 0;
    		for (int j = -outputGeometry[1]/2; j <= outputGeometry[1]/2; j++) {
    			threadOffsetJ[tileIndexJ * outputGeometry[1] + offset++] = j;
    		}
    	}
	}
	
	public void compute() {
		Device aparapiDevice = computeDevice.getAparapiDevice();
		if (tilesGeometry == null) {
           tilesGeometry = new int[3];
		}
		if (matrixOut == null || matrixOut.length < totalNumberOfTiles * outputGeometry[1] * outputGeometry[0]) {
	       matrixOut = new float[totalNumberOfTiles * outputGeometry[1] * outputGeometry[0]];
		}
		
        if (kernel == null) {
	        if (normalized) {
	        	kernel = new NormCrossCorrelationKernel();
	        } else {
	        	kernel = new CrossCorrelationKernel();
	        }
        }
        
        if (normalized) {
            ((NormCrossCorrelationKernel)kernel).setParameters(matrixInF, matrixInG, matrixOut, 
                                                               threadOutputStart, threadOffsetI, threadOffsetJ, 
                                                               inputGeometry, outputGeometry, tilesGeometry);
        } else {
            ((CrossCorrelationKernel)kernel).setParameters(matrixInF, matrixInG, matrixOut, 
                                                           threadOutputStart, threadOffsetI, threadOffsetJ, 
                                                           inputGeometry, outputGeometry, tilesGeometry);
        }
        
        //TODO Must ensure that MaxWorkGroup allowed for the kernel instance is not exceeded
        //(outputGeometry[0]/cellsPerThreadJ) * (outputGeometry[1]/cellsPerThreadI) <= MaxWorkGroup for the Kernel
        
        int maxThreadGroupSizeForKernel = computeDevice.getMaxThreadsPerThreadGroup();
        if (!Device.TYPE.JTP.equals(aparapiDevice.getType()) && !Device.TYPE.SEQ.equals(aparapiDevice.getType()) ) {
            try {
                maxThreadGroupSizeForKernel = kernel.getKernelMaxWorkGroupSize((OpenCLDevice)aparapiDevice);
            } catch (QueryFailedException e) {
                throw new JobComputeException("Couldn't compile kernel for selected OpenCL device", e);
            }
        }
        
        if (outputGeometry[0] * outputGeometry[1] > maxThreadGroupSizeForKernel) {
            System.out.println("Group Size exceeds: " + outputGeometry[0] * outputGeometry[1]);
        }
        
        
        tilesGeometry[0] = numberOfTilesInI;
        tilesGeometry[1] = numberOfTilesInJ;
        tilesGeometry[2] = numberOfTilesInK;
        
        kernel.setExplicit(true);
                
        kernel.put(matrixInF);
        kernel.put(matrixInG);
        kernel.put(threadOffsetI);
        kernel.put(threadOffsetJ);
        kernel.put(inputGeometry);
        kernel.put(outputGeometry);
        kernel.put(tilesGeometry);
        kernel.put(matrixOut);
        kernel.put(threadOutputStart);        
        
        logger.info("Number of tiles In K: " + numberOfTilesInK + " - tiles In I: " + numberOfTilesInI + " - tiles In J: " + numberOfTilesInJ);
        
        //localWidth * localHeight < #OfWorkGroups
        //globalWidth / localHeight must be an integer
        //globalHeight / localHeight must be an integer
        
        final Range range;
        if (Device.TYPE.JTP.equals(aparapiDevice.getType()) || Device.TYPE.SEQ.equals(aparapiDevice.getType()) ) {
        	if (numberOfDimensions == 2) { 
                int local[] = Factorization.getSuggestedLocalWorkgroup2D(numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0], 
                        maxThreadGroupSizeForKernel);

        	    range = Range.create2D(
		        		numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0],
						local[0], local[1]);
        	} else {
        		range = Range.create3D(
        				numberOfTilesInJ * outputGeometry[1],  numberOfTilesInI * outputGeometry[0], numberOfTilesInK);
        	}
		} else {
			if (numberOfDimensions == 2) {
			    int local[] = Factorization.getSuggestedLocalWorkgroup2D(numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0], 
			            maxThreadGroupSizeForKernel);
		        range = Range.create2D(aparapiDevice,
		        		numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0],
		        		local[0], local[1]);
		        System.out.println("Computing with localX= " + local[0] + ", localY= " + local[1]);
		        		
			} else {
			    int local[] = Factorization.getSuggestedLocalWorkgroup2D(numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0], 
                        maxThreadGroupSizeForKernel);
				range = Range.create3D(aparapiDevice,
						numberOfTilesInJ * outputGeometry[1], numberOfTilesInI * outputGeometry[0], numberOfTilesInK,
						local[0], local[1], 1);
				System.out.println("Computing with localX= " + local[0] + ", localY= " + local[1]);
			}
		}
        
        kernel.execute(range);
        
        kernel.get(matrixOut);
        
        //kernel.dispose();
        
        logger.info("OpenCL completed");
        
        outputMatrices = new ArrayList<Matrix>(numberOfUsedTiles);
        
        //Split matrix out into a List of
        Matrix currentMatrix = null;
        int processedMatrices = 0;
        for (int tilesIndexK = 0; tilesIndexK < numberOfTilesInK; tilesIndexK++) {
	        for (int tilesIndexI = 0;  tilesIndexI < numberOfTilesInI; tilesIndexI++) {
	        	for (int tilesIndexJ = 0;  tilesIndexJ < numberOfTilesInJ; tilesIndexJ++) {
	        		currentMatrix = new MatrixFloat((short)outputGeometry[0], (short)outputGeometry[1]);
	        		int sourceOffset = (tilesIndexK * numberOfTilesInI * numberOfTilesInJ * outputGeometry[0] * outputGeometry[1]) + 
	        						    (tilesIndexI * numberOfTilesInJ * outputGeometry[0] * outputGeometry[1] +
	        						    tilesIndexJ * outputGeometry[0] * outputGeometry[1]);
	        		currentMatrix.copyMatrixFromArray(matrixOut, sourceOffset);
	
	        		outputMatrices.add(currentMatrix);
	        		processedMatrices++;
	        		if (processedMatrices == numberOfUsedTiles) {
	        			break;
	        		}
	        	}
	    		if (processedMatrices == numberOfUsedTiles) {
	    			break;
	    		}
	
	        }
        }
        
        List<MaxCrossResult> crossResults = Collections.emptyList();//new ArrayList<MaxCrossResult>(numberOfUsedTiles);
		/*		for (int i = 0; i < numberOfUsedTiles; i++) {
					MaxCrossResult maxResult = new MaxCrossResult();
					maxResult.i = maxs[i * 4];
					maxResult.j = maxs[i * 4 + 1];
					maxResult.value = maxs[i * 4 + 2];
					crossResults.add(maxResult);
				}*/
				
		XCorrelationResults results = new XCorrelationResults(outputMatrices, crossResults, matrixOut, outputGeometry[0], outputGeometry[1], numberOfUsedTiles);
		setJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES, results);
	}
		
	public void dispose() {
		if (kernel != null) {
			kernel.dispose();
			kernel = null;
		}
	}
	
	@Override
	public void finalize() {
		dispose();
	}
}
 