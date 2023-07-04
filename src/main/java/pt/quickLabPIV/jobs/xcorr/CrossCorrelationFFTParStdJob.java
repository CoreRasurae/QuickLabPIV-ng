package pt.quickLabPIV.jobs.xcorr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.exception.CompileFailedException;
import com.aparapi.exception.QueryFailedException;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.NotImplementedException;
import pt.quickLabPIV.jobs.optimizers.GlobalGeometry;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class CrossCorrelationFFTParStdJob extends CrossCorrelationFFTTemplate {
	private static Logger logger = LoggerFactory.getLogger(CrossCorrelationFFTParStdJob.class);
	
	private Kernel kernel;
	private float[] matrixInFRe;  //Input matrix F real (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInFIm;  //Input matrix F imaginary (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInGRe;  //Input matrix G real (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	private float[] matrixInGIm;  //Input matrix G imaginary (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	private float[] maxs;
	private List<Matrix> outputMatrices;
	
	private int maxNumberOfUsedTiles = 0;
	private int maxMemorySize = 0;
	
	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationFFTParStdJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		super(device, normalized, matricesF, matricesG);
	}

	public CrossCorrelationFFTParStdJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		super(normalized, device, computeDeviceGeometry);
	}
	
	public CrossCorrelationFFTParStdJob(final boolean normalized, final ComputationDevice device) {
		super(normalized, device);
	}
		
	@Override 
	protected Logger getLogger() {
		return logger;
	}

	
	@Override
	protected void analyzeTemplate(final int numberOfUsedTiles) {
		int[] outputGeometry = getOutputGeometry();
        final int newMemorySize = numberOfUsedTiles * outputGeometry[0] * outputGeometry[1];
        if (matrixInFRe == null || newMemorySize > maxMemorySize) {
	        matrixInFRe = new float[newMemorySize];
	        matrixInFIm = new float[newMemorySize];
	        matrixInGRe = new float[newMemorySize];
	        matrixInGIm = new float[newMemorySize];
	        maxMemorySize = newMemorySize;
        } else {
        	//Zero all matrices, because now we only copy partial data from input to the working matrices...
			Arrays.fill(matrixInFRe, 0.0f);
			Arrays.fill(matrixInFIm, 0.0f);
			Arrays.fill(matrixInGRe, 0.0f);
			Arrays.fill(matrixInGIm, 0.0f);
			Arrays.fill(maxs, 0.0f);
        }

        if (maxs == null || numberOfUsedTiles > maxNumberOfUsedTiles) {
	        maxs = new float[numberOfUsedTiles * 4];
	        maxNumberOfUsedTiles = numberOfUsedTiles;
        }
                
        List<Tile> inputTilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
        List<Tile> inputTilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);
        int matrixIndex = 0;
        for (int tileIndex = 0; tileIndex < numberOfUsedTiles; tileIndex++) {
        	int offset = tileIndex * outputGeometry[0] * outputGeometry[1];
			Matrix matrixF = null;
			Matrix matrixG = null;
			
			if (inputMatricesF != null || inputMatricesG != null) {
    			matrixF = inputMatricesF.get(matrixIndex);
    			matrixG = inputMatricesG.get(matrixIndex);
			} else {
				matrixF = inputTilesF.get(matrixIndex).getMatrix();
				matrixG = inputTilesG.get(matrixIndex).getMatrix();
			}
    		
			matrixF.copyMirroredMatrixToArray(matrixInFRe, offset, outputGeometry[1]);
			matrixG.copyMatrixToArray(matrixInGRe, offset, outputGeometry[1]);
    			        		
    		matrixIndex++;
        }
	}
		
   private void compileKernel() {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null) {
            if (isNormalized()) {
                throw new NotImplementedException("Normalized cross correlation is not implemented in FFTParStd job");
            } else {
                if (Device.TYPE.GPU.equals(device.getType()) || isEmulateGPU()) {
                    kernel = new CrossCorrelationFFTParallelKernel();
                } else {
                    kernel = new CrossCorrelationFFTCpuParallelKernel();
                }
            }
        } 

        try {
            kernel.compile(device);
        } catch (CompileFailedException e) {
            throw new JobAnalyzeException("Failed to compile kernel for device: " + computeDevice.getDeviceName() + ", with id"
                    + computeDevice.getDeviceId());
        }
    }
    
    @Override
    protected int getCompiledKernelPreferredWorkItemMultipleImpl() throws QueryFailedException {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();

        compileKernel();
        return kernel.getKernelPreferredWorkGroupSizeMultiple(device);
    }
    
    @Override
    protected int getCompiledKernelMaxGroupThreadsImpl() throws QueryFailedException {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();
        
        compileKernel();
        return kernel.getKernelMaxWorkGroupSize(device);
    }	    	
	
	public void compute() {
		final int[] inputGeometry = getInputGeometry();
		final int[] outputGeometry = getOutputGeometry();
		final LocalGeometry localGeom = getLocalGeometry();
		final GlobalGeometry globalGeom = getGlobalGeometry();
		
		ComputationDevice computeDevice = getComputeDevice();
		Device device = computeDevice.getAparapiDevice();
        
        if (Device.TYPE.GPU.equals(device.getType()) || isEmulateGPU()) {
	        ((CrossCorrelationFFTParallelKernel)kernel).setKernelParams(localGeom.inputOrder, localGeom.shuffleOrder, localGeom.w, matrixInFRe, matrixInFIm, matrixInGRe, matrixInGIm, maxs,
					globalGeom.workItemsPerMatrixI, inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
	        if (isExplicit()) {
	        	kernel.setExplicit(true);       
		        kernel.put(localGeom.inputOrder);
		        kernel.put(localGeom.shuffleOrder);
		        kernel.put(localGeom.w);
		        kernel.put(matrixInFRe);
		        kernel.put(matrixInFIm);
		        kernel.put(matrixInGRe);
		        kernel.put(matrixInGIm);
		        kernel.put(maxs);
		        //kernel.put(linesPerWorkGroup);
		        kernel.put(inputGeometry);
		        kernel.put(outputGeometry);
	        }
        } else {
        	((CrossCorrelationFFTCpuParallelKernel)kernel).setKernelParams(localGeom.shuffleOrder, localGeom.w, matrixInFRe, matrixInFIm, matrixInGRe, matrixInGIm, maxs,
        			globalGeom.workItemsPerMatrixI, inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
            if (isExplicit()) {
            	kernel.setExplicit(true);       
    	        kernel.put(localGeom.shuffleOrder);
    	        kernel.put(localGeom.w);
    	        kernel.put(matrixInFRe);
    	        kernel.put(matrixInFIm);
    	        kernel.put(matrixInGRe);
    	        kernel.put(matrixInGIm);
    	        kernel.put(maxs);
    	        //kernel.put(linesPerWorkGroup);
    	        kernel.put(inputGeometry);
    	        kernel.put(outputGeometry);
            }
        }
	    
        Range range = checkAndUpdateRangeIfRequired();
        
        //localWidth * localHeight <= #OfWorkGroups
        //globalWidth / localHeight must be an integer
        //globalHeight / localHeight must be an integer              
        
        kernel.execute(range, globalGeom.numberOfPasses);
        
        if (isExplicit()) {
	        kernel.get(matrixInFRe);
	        kernel.get(maxs);
        }
        
        if (isAlwaysDispose()) {
        	kernel.dispose();
        }
        
        logger.info("OpenCL completed");
        
        final int numberOfUsedTiles = globalGeom.numberOfUsedTiles;
        outputMatrices = new ArrayList<Matrix>(numberOfUsedTiles);
        
        //Split matrix out into a List of
        Matrix currentMatrix = null;
        for (int tilesIndex = 0; tilesIndex < numberOfUsedTiles; tilesIndex++) {
    		currentMatrix = new MatrixFloat((short)(outputGeometry[0] - 1), (short)(outputGeometry[1] - 1));
    		int offset = tilesIndex * outputGeometry[0] * outputGeometry[1];
    		
    		//Since cross-correlation maximum is also pre-computed by openCL, for current processing needs, it suffices to copy
    		//11 pixels around the maximum.
    		/*final int maxI = (int)maxs[tilesIndex * 4];
    		final int maxJ = (int)maxs[tilesIndex * 4 + 1];
    		final int minIndexI = FastMath.max(maxI-11, 0);
    		final int maxIndexI = FastMath.min(maxI+11, outputGeometry[0]-1);
    		final int maxIndexJ = FastMath.min(maxJ+11, outputGeometry[1]-1);
    		final int minIndexJ = FastMath.max(maxJ-11, 0);     	
   			currentMatrix.copySubMatrixFromLargerArray(matrixInFRe, offset, minIndexI, minIndexJ, maxIndexI-minIndexI+1, maxIndexJ-minIndexJ+1,
   					outputGeometry[1]);*/
    		
    		//Destination is smaller than source, but bottom row and right column are zeros, copy will discard those zero values,
    		//corresponding to the 2D array regions that fall outside the matrix. 
    		currentMatrix.copyMatrixFromLargerArray(matrixInFRe, offset, outputGeometry[1]); 		

    		outputMatrices.add(currentMatrix);
    	}

		List<MaxCrossResult> crossResults = Collections.emptyList();//new ArrayList<MaxCrossResult>(numberOfUsedTiles);
		/*	
		for (int i = 0; i < numberOfUsedTiles; i++) {
			MaxCrossResult maxResult = new MaxCrossResult();
			maxResult.i = maxs[i * 4];
			maxResult.j = maxs[i * 4 + 1];
			maxResult.value = maxs[i * 4 + 2];
			crossResults.add(maxResult);
		}*/
        XCorrelationResults results = new XCorrelationResults(outputMatrices, crossResults, matrixInFRe, outputGeometry[0], outputGeometry[1], numberOfUsedTiles);
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
 
