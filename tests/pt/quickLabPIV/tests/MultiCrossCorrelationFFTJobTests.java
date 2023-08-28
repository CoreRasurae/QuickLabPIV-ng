// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTBasicJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class MultiCrossCorrelationFFTJobTests {
	private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
	private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

    private class DefaultKernelManager extends KernelManager {
        
    }
    
    @Before
    public void setup() {
        KernelManager.setKernelManager(new DefaultKernelManager());
    }
	
	@Test
	public void simpleTestLocalCrossCorrelationPass() {
	    assumeTrue("No OpenCL CPU device is available", cpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTStdJob job = null;
		try {
			job = new CrossCorrelationFFTStdJob(cpuDevice, false, inputMatricesF, inputMatricesG);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
        for (Matrix result : outputMatricesLocal) {
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], result.getElement(i, j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }		
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}


	@Test
	public void multiMatrixCrossCorrelationPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesF.add(g);
		inputMatricesF.add(g);
		inputMatricesF.add(f);
		inputMatricesG.add(g);
		inputMatricesG.add(f);
		inputMatricesG.add(f);
		inputMatricesG.add(g);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTStdJob job = null;
		try {
			job = new CrossCorrelationFFTStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}

	@Test
	public void multiMatrixCrossCorrelationTilesPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Tile> inputTilesF = new ArrayList<Tile>();
		List<Tile> inputTilesG = new ArrayList<Tile>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		Tile tf1 = new Tile(null, f);
		Tile tg1 = new Tile(null, g);
		inputTilesF.add(tf1);
		inputTilesF.add(tg1);
		inputTilesF.add(tg1);
		inputTilesF.add(tf1);
		inputTilesG.add(tg1);
		inputTilesG.add(tf1);
		inputTilesG.add(tf1);
		inputTilesG.add(tg1);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTStdJob job = null;
		try {
			job = new CrossCorrelationFFTStdJob(false, gpuDevice, new int[] {2*4,2,2});
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, inputTilesF);
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, inputTilesG);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelationTile(inputTilesF, inputTilesG);

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}

	@Test
	public void simpleTestLocalCrossCorrelationParStdPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTParStdJob job = null;
		try {
			job = new CrossCorrelationFFTParStdJob(gpuDevice, false, inputMatricesF, inputMatricesG, false);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
        for (Matrix result : outputMatricesLocal) {
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], result.getElement(i, j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }		
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}
	
	@Test
	public void multiMatrixCrossCorrelationParStdPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesF.add(g);
		inputMatricesF.add(g);
		inputMatricesF.add(f);
		inputMatricesG.add(g);
		inputMatricesG.add(f);
		inputMatricesG.add(f);
		inputMatricesG.add(g);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTParStdJob job = null;
		try {
			job = new CrossCorrelationFFTParStdJob(gpuDevice, false, inputMatricesF, inputMatricesG, false);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}
	
	@Test
	public void multiMatrixCrossCorrelationTilesParStdPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Tile> inputTilesF = new ArrayList<Tile>();
		List<Tile> inputTilesG = new ArrayList<Tile>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		Tile tf1 = new Tile(null, f);
		Tile tg1 = new Tile(null, g);
		inputTilesF.add(tf1);
		inputTilesF.add(tg1);
		inputTilesF.add(tg1);
		inputTilesF.add(tf1);
		inputTilesG.add(tg1);
		inputTilesG.add(tf1);
		inputTilesG.add(tf1);
		inputTilesG.add(tg1);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTParStdJob job = null;
		try {
			job = new CrossCorrelationFFTParStdJob(false, gpuDevice, new int[] {2*4,4,2}, false);
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, inputTilesF);
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, inputTilesG);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelationTile(inputTilesF, inputTilesG);

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}

	@Test
	public void multiMatrixCrossCorrelationBasicTilesPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Tile> inputTilesF = new ArrayList<Tile>();
		List<Tile> inputTilesG = new ArrayList<Tile>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		Tile tf1 = new Tile(null, f);
		Tile tg1 = new Tile(null, g);
		inputTilesF.add(tf1);
		inputTilesF.add(tg1);
		inputTilesF.add(tg1);
		inputTilesF.add(tf1);
		inputTilesG.add(tg1);
		inputTilesG.add(tf1);
		inputTilesG.add(tf1);
		inputTilesG.add(tg1);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTBasicJob job = null;
		try {
			job = new CrossCorrelationFFTBasicJob(false, gpuDevice, new int[] {2,2,2}, false);
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, inputTilesF);
			job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, inputTilesG);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelationTile(inputTilesF, inputTilesG);

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}
	
	@Test
	public void multiMatrixCrossCorrelationBasicPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesF.add(g);
		inputMatricesF.add(g);
		inputMatricesF.add(f);
		inputMatricesG.add(g);
		inputMatricesG.add(f);
		inputMatricesG.add(f);
		inputMatricesG.add(g);

		float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 2.0f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 5.5f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 3.0f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 7.0f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 15.0f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)
	    
		CrossCorrelationFFTBasicJob job = null;
		try {
			job = new CrossCorrelationFFTBasicJob(gpuDevice, false, inputMatricesF, inputMatricesG, false);
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
        Matrix resultLocal = outputMatricesLocal.get(0);
    	for (short i = 0;  i < resultLocal.getHeight(); i++) {
    		for (short j = 0; j < resultLocal.getWidth(); j++) {
    			assertEquals("Computed local cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], resultLocal.getElement(i, j), 1e-10);
    		}
    	}
    	matrixIndex++;
        
        matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
        		}
        	}
        	matrixIndex++;
        }				        
	}

}
