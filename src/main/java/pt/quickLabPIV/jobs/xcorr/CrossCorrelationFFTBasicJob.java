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

public class CrossCorrelationFFTBasicJob extends CrossCorrelationFFTTemplate {
	private static Logger logger = LoggerFactory.getLogger(CrossCorrelationFFTBasicJob.class);
	
	private Kernel kernel;

	private float[] matrixInFRe;  //Input matrix F real (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInFIm;  //Input matrix F imaginary (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry (k, i, j)
	private float[] matrixInGRe;  //Input matrix G real (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	private float[] matrixInGIm;  //Input matrix G imaginary (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry (k, i, j)
	private List<Matrix> outputMatrices;
	
	private int maxNumberOfUsedTiles = 0;
	private int maxMemorySize = 0;
	
	private boolean normalizeAndRegularizeInputMatrices = true;

	/**
	 * Creates a cross-correlation Job from a list of pair of matrices F and G.
	 * The number of matrices in F is related with their dimensions and target device computing capabilities.
	 * A Job must be able do to all the computation in a single device.
	 * @param device is the OpenCL compute device to use  
	 * @param matricesF the list of input matrices F (to do xcorr(F,G))
	 * @param matricesG the list of input matrices G (to do xcorr(F,G))
	 */
	public CrossCorrelationFFTBasicJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG) {
		super(device, normalized, matricesF, matricesG);
	}

	public CrossCorrelationFFTBasicJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry) {
		super(normalized, device, computeDeviceGeometry);
	}
	
	public CrossCorrelationFFTBasicJob(final boolean normalized, final ComputationDevice device, int[] computeDeviceGeometry, boolean _normalizeAndRegularizeInputMatrices) {
        super(normalized, device, computeDeviceGeometry);
        normalizeAndRegularizeInputMatrices =  _normalizeAndRegularizeInputMatrices;
    }
	
	public CrossCorrelationFFTBasicJob(final ComputationDevice device, final boolean normalized, final List<Matrix> matricesF, final List<Matrix> matricesG, boolean _normalizeAndRegularizeInputMatrices) {
	    super(device, normalized, matricesF, matricesG);
	    normalizeAndRegularizeInputMatrices =  _normalizeAndRegularizeInputMatrices;
    }

    @Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected LocalGeometry refineLocalGeometry(LocalGeometry geom) {
		geom.localSizeX = 1;
		geom.localSizeY = 1;
		return geom;
	}
	
	@Override
	public void analyzeTemplate(final int numberOfUsedTiles) {
		List<Tile> inputTilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
		List<Tile> inputTilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);

		int[] outputGeometry = getOutputGeometry();
		
        int memorySize = numberOfUsedTiles * outputGeometry[0] * outputGeometry[1];
        if (matrixInFRe == null || memorySize > maxMemorySize) {
	        matrixInFRe = new float[memorySize];
	        matrixInFIm = new float[memorySize];
	        matrixInGRe = new float[memorySize];
	        matrixInGIm = new float[memorySize];
        } else {
			Arrays.fill(matrixInFRe, 0.0f);
			Arrays.fill(matrixInFIm, 0.0f);
			Arrays.fill(matrixInGRe, 0.0f);
			Arrays.fill(matrixInGIm, 0.0f);
        }
        
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
    		
			//FIXME Ideally all the cross-correlations should be normalized at each FFT step, even if no regularization is employed.
		     //There is no point in scaling the FFT results, because:
	        //- The FFT is not normalized, it overflows and overflows the float representation with 16Bits images, and does not cause Inf or NaN,
	        //  thus producing completely invalid results.
			if (!normalizeAndRegularizeInputMatrices) {
    			matrixF.copyMirroredMatrixToArray(matrixInFRe, offset, outputGeometry[1]);
    			matrixG.copyMatrixToArray(matrixInGRe, offset, outputGeometry[1]);
			} else {
    			matrixF.copyMirroredMatrixToArrayNormalizeAndOffset(matrixInFRe, offset, outputGeometry[1]);
    			matrixG.copyMatrixToArrayAndNormalizeAndOffset(matrixInGRe, offset, outputGeometry[1]);
			}
    		
    		matrixIndex++;
        }        
   	}


    private void compileKernel() {
        ComputationDevice computeDevice = getComputeDevice();
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null) {
            if (isNormalized()) {
                throw new NotImplementedException("");
            } else {
                kernel = new CrossCorrelationFFTBasicKernel();
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
		                
        ((CrossCorrelationFFTBasicKernel)kernel).setKernelParams(localGeom.shuffleOrder, localGeom.w, matrixInFRe, matrixInFIm, matrixInGRe, matrixInGIm,
        							inputGeometry, outputGeometry, globalGeom.numberOfUsedTiles);
        
        if (isExplicit() == true) {
        	kernel.setExplicit(true);
        	kernel.put(localGeom.shuffleOrder);
        	kernel.put(localGeom.w);
        	kernel.put(matrixInFRe);
            kernel.put(matrixInFIm);
            kernel.put(matrixInGRe);
            kernel.put(matrixInGIm);
            kernel.put(inputGeometry);
            kernel.put(outputGeometry);        
        }

        //localWidth * localHeight <= #OfWorkGroups
        //globalWidth / localHeight must be an integer
        //globalHeight / localHeight must be an integer
                
        Range range = checkAndUpdateRangeIfRequired();
        
        kernel.execute(range);
        
        if (isExplicit()) {
        	kernel.get(matrixInFRe);
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
    		//Destination is smaller than source, but bottom row and right column are zeros, copy will discard those zero values,
    		//corresponding to the 2D array regions that fall outside the matrix. 
    		currentMatrix.copyMatrixFromLargerArray(matrixInFRe, offset, outputGeometry[1]);

    		outputMatrices.add(currentMatrix);
    	}
        
		List<MaxCrossResult> crossResults = Collections.emptyList();//new ArrayList<MaxCrossResult>(numberOfUsedTiles);
		/*		for (int i = 0; i < numberOfUsedTiles; i++) {
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
 
