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

public class CrossCorrelationFFTStdJob extends CrossCorrelationFFTTemplate {
	private static Logger logger = LoggerFactory.getLogger(CrossCorrelationFFTStdJob.class);
	
	private Kernel kernel;

	private float[] matrixInFRe;  //Input matrix F real (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInFIm;  //Input matrix F imaginary (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInGRe;  //Input matrix G real (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	private float[] matrixInGIm;  //Input matrix G imaginary (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	
	private int maxMemorySize = 0;
	
	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationFFTStdJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		super(device, normalized, matricesF, matricesG);
	}

	public CrossCorrelationFFTStdJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		super(normalized, device, computeDeviceGeometry);
	}
		
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	@Override
	protected LocalGeometry refineLocalGeometry(LocalGeometry geom) {
		geom.localSizeX = 1;
		return geom;
	}
	
	@Override
	public void analyzeTemplate(final int numberOfUsedTiles) {
		final List<Tile> tilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
		final List<Tile> tilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);
              
        int[] outputGeometry = getOutputGeometry();
        
        int memorySize = numberOfUsedTiles * outputGeometry[0] * outputGeometry[1];
        if (matrixInFRe == null || memorySize > maxMemorySize) {
	        matrixInFRe = new float[memorySize];
	        matrixInFIm = new float[memorySize];
	        matrixInGRe = new float[memorySize];
	        matrixInGIm = new float[memorySize];
	        maxMemorySize = memorySize;
        } else {
        	//Zero all matrices, because now we only copy partial data from input to the working matrices...
			Arrays.fill(matrixInFRe, 0.0f);
			Arrays.fill(matrixInFIm, 0.0f);
			Arrays.fill(matrixInGRe, 0.0f);
			Arrays.fill(matrixInGIm, 0.0f);
        }
        
        int matrixThreadIndex = 0;
        for (int tileIndex = 0; tileIndex < numberOfUsedTiles; tileIndex++) {
        	int offset = tileIndex * outputGeometry[0] * outputGeometry[1];	        		
        	
        	Matrix matrixF = null;
			Matrix matrixG = null;
			if (inputMatricesF != null || inputMatricesG != null) {
    			matrixF = inputMatricesF.get(matrixThreadIndex);
    			matrixG = inputMatricesG.get(matrixThreadIndex);
			} else {
				matrixF = tilesF.get(matrixThreadIndex).getMatrix();
				matrixG = tilesG.get(matrixThreadIndex).getMatrix();
			}
    		
			matrixF.copyMirroredMatrixToArray(matrixInFRe, offset, outputGeometry[0]);
			matrixG.copyMatrixToArray(matrixInGRe, offset, outputGeometry[0]);
        }
   	}
	
    private void compileKernel() {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null) {
            if (isNormalized()) {
                throw new NotImplementedException("Normalized cross correlation is not implemented in FFTStd job");
            } else {
                if (Device.TYPE.GPU.equals(device.getType()) || isEmulateGPU()) {
                    kernel = new CrossCorrelationFFTKernel();
                } else {
                    kernel = new CrossCorrelationFFTCpuKernel();
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
	
	@Override
	public void compute() {
		final int[] inputGeometry = getInputGeometry();
		final int[] outputGeometry = getOutputGeometry();
		final LocalGeometry localGeom = getLocalGeometry();
		final GlobalGeometry globalGeom = getGlobalGeometry();
		
		ComputationDevice computeDevice = getComputeDevice();
		Device aparapiDevice = computeDevice.getAparapiDevice();
        
        if (Device.TYPE.GPU.equals(aparapiDevice.getType()) || isEmulateGPU()) {
        	((CrossCorrelationFFTKernel)kernel).setKernelParams(localGeom.shuffleOrder, localGeom.w, matrixInFRe, matrixInFIm, matrixInGRe, matrixInGIm,
        														globalGeom.workItemsPerMatrixI, inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
        } else {
        	((CrossCorrelationFFTCpuKernel)kernel).setKernelParams(localGeom.shuffleOrder, localGeom.w, matrixInFRe, matrixInFIm, matrixInGRe, matrixInGIm,
        														   globalGeom.workItemsPerMatrixI, inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
        }
	    
        Range range = checkAndUpdateRangeIfRequired();

        //localWidth * localHeight <= #OfWorkGroups
        //globalWidth / localHeight must be an integer
        //globalHeight / localHeight must be an integer   
        if (isExplicit()) {
	        kernel.setExplicit(true); //Causes a memory leak - if kernel.dispose() is not called each time
	        kernel.put(localGeom.shuffleOrder);
	        kernel.put(localGeom.w);	        
	        kernel.put(matrixInFRe);
	        kernel.put(matrixInFIm);
	        kernel.put(matrixInGRe);
	        kernel.put(matrixInGIm);
	        kernel.put(inputGeometry);
	        kernel.put(outputGeometry);     
        }
        
        //FIXME must update kernel to make use of the number of Passes
        kernel.execute(range, globalGeom.numberOfPasses);
        
        if (isExplicit()) {
        	kernel.get(matrixInFRe);
        }

        if (isAlwaysDispose()) {
        	kernel.dispose();
        }
        
        logger.info("OpenCL completed");
	
        final int numberOfUsedTiles = globalGeom.numberOfUsedTiles;
        
        //Split matrix out into a List of
        Matrix currentMatrix = null;
        List<Matrix> outputMatrices = new ArrayList<Matrix>(numberOfUsedTiles);
        for (int tilesIndex = 0; tilesIndex < numberOfUsedTiles; tilesIndex++) {
    		currentMatrix = new MatrixFloat(outputGeometry[0] - 1, outputGeometry[1] - 1);

    		int sourceOffset = tilesIndex * outputGeometry[0] * outputGeometry[1];
    		//Destination is smaller than source, but bottom row and right column are zeros, copy will discard those zero values,
    		//corresponding to the 2D array regions that fall outside the matrix. 
    		currentMatrix.copyMatrixFromLargerArray(matrixInFRe, sourceOffset, outputGeometry[0]);

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
}
 
