// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
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

public class CrossCorrelationFFTParInterleavedStdJob extends CrossCorrelationFFTTemplate {
	private static Logger logger = LoggerFactory.getLogger(CrossCorrelationFFTParInterleavedStdJob.class);
	
	private Kernel kernel;

	private float[] matrices;  //Input matrices interleaved F real,imag G real,imag
	private List<Matrix> outputMatrices;
	
	private int maxMemorySize = 0;

	
	@Override 
	protected Logger getLogger() {
		return logger;
	}
	
	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationFFTParInterleavedStdJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		super(device, normalized, matricesF, matricesG);
	}

	public CrossCorrelationFFTParInterleavedStdJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		super(normalized, device, computeDeviceGeometry);
	}
		
	@Override
	public void analyzeTemplate(final int numberOfUsedTiles) {
		int[] outputGeometry = getOutputGeometry();

		List<Tile> inputTilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
		List<Tile> inputTilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);
		
		int memorySize = numberOfUsedTiles * outputGeometry[0] * outputGeometry[1] * 4;
		if (matrices == null || memorySize > maxMemorySize) {
	        matrices = new float[memorySize];
	        maxMemorySize = memorySize;
        } else {
        	//Zero all matrices, because now we only copy partial data from input to the working matrices...
			Arrays.fill(matrices, 0.0f);
        }
        
        int matrixIndex = 0;
        for (int tileIndex = 0; tileIndex < numberOfUsedTiles; tileIndex++) {
        	int offset = tileIndex * outputGeometry[0] * outputGeometry[1] * 4;
			Matrix matrixF = null;
			Matrix matrixG = null;
			
			if (inputMatricesF != null || inputMatricesG != null) {
    			matrixF = inputMatricesF.get(matrixIndex);
    			matrixG = inputMatricesG.get(matrixIndex);
			} else {
				matrixF = inputTilesF.get(matrixIndex).getMatrix();
				matrixG = inputTilesG.get(matrixIndex).getMatrix();
			}
    		
			matrixF.copyMirroredMatrixToStridedArray(matrices, offset, 4, outputGeometry[1]);
			matrixG.copyMatrixToStridedArray(matrices, offset+2, 4, outputGeometry[1]);
    			        		
    		matrixIndex++;
        }
   	}
	

   private void compileKernel() {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null) {
            if (isNormalized()) {
                throw new NotImplementedException("Normalized cross correlation is not implemented in FFTInterleavedStd job");
            } else {
                    kernel = new CrossCorrelationFFTParallelInterleavedKernel();
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
		
        if (kernel == null) {
	        if (isNormalized()) {
	        	throw new NotImplementedException("Normalized cross correlation is not implemented in FFTInterleavedStd job");
	        } else {
	        		kernel = new CrossCorrelationFFTParallelInterleavedKernel();
	        }
        }

        ((CrossCorrelationFFTParallelInterleavedKernel)kernel).setKernelParams(localGeom.inputOrder, localGeom.shuffleOrder, localGeom.w, matrices,
        															globalGeom.workItemsPerMatrixI, inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
	        
        Range range = checkAndUpdateRangeIfRequired();
	    
        if (isExplicit()) {
        	kernel.setExplicit(true);
	        kernel.put(localGeom.inputOrder);
	        kernel.put(localGeom.shuffleOrder);
	        kernel.put(localGeom.w);
	        kernel.put(matrices);
	        //kernel.put(geom.localSizeY);
	        kernel.put(inputGeometry);
	        kernel.put(outputGeometry);
        }
        
        //localWidth * localHeight <= #OfWorkGroups
        //globalWidth / localHeight must be an integer
        //globalHeight / localHeight must be an integer              
        //Kernel.invalidateCaches();
        
        kernel.execute(range, globalGeom.numberOfPasses);
        
        if (isExplicit()) {
        	kernel.get(matrices);
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
    		int offset = tilesIndex * outputGeometry[0] * outputGeometry[1] * 4;
    		//Destination is smaller than source, but bottom row and right column are zeros, copy will discard those zero values,
    		//corresponding to the 2D array regions that fall outside the matrix. 
    		currentMatrix.copyMatrixFromLargerStridedArray(matrices, offset, 4, outputGeometry[1]);

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
				
		XCorrelationResults results = new XCorrelationResults(outputMatrices, crossResults, matrices, outputGeometry[0], outputGeometry[1], numberOfUsedTiles);
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
 
