package pt.quickLabPIV.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixByte;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.IMatrixExporterVisitor;
import pt.quickLabPIV.exporter.StructSingleFrameFloatMatlabExporter;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTBasicJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParInterleavedStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationRealFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTTemplate.EmulationModeEnum;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;

public class CrossCorrelationJobTests {
	private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
	private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

	private class DefaultKernelManager extends KernelManager {
	    
	}
	
	@Before
	public void setup() {
	    KernelManager.setKernelManager(new DefaultKernelManager());
	}
	
	@Test
	public void simpleCrossCorrelation2x2GPUPass() {
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
	    
		CrossCorrelationJob job = new CrossCorrelationJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        for (Matrix result : outputMatrices) {
        	for (int i = 0;  i < result.getHeight(); i++) {
        		for (int j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for i=" + i + ", j=" + j + " is wrong", 
        					matrixCross[i][j], result.getElement((short)i, (short)j), 1e-10);
        		}
        	}
        }
        
        //IMatrixExporterVisitor exporter = new SingleFrameFloatMatlabExporter();
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("test1.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
	}

	@Test
	public void simpleCrossCorrelationFFTParInterleavedStd2x2GPUPass() {
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
	    
	    CrossCorrelationFFTParInterleavedStdJob job = new CrossCorrelationFFTParInterleavedStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        for (Matrix result : outputMatrices) {
        	for (int i = 0;  i < result.getHeight(); i++) {
        		for (int j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for i=" + i + ", j=" + j + " is wrong", 
        					matrixCross[i][j], result.getElement((short)i, (short)j), 1e-10);
        		}
        	}
        }        
	}
	
	@Test
	public void simpleCrossCorrelation2x2CPUPass() {
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
	    
		CrossCorrelationJob job = new CrossCorrelationJob(cpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        int matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	for (int i = 0;  i < result.getHeight(); i++) {
        		for (int j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + i + ", j=" + j + " is wrong", matrixCross[i][j], result.getElement((short)i, (short)j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }
	}
	
	@Test
	public void simpleCrossCorrelation2x2x2GPUPass() {
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
		inputMatricesF.add(f);
		inputMatricesG.add(g);
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
	    
		CrossCorrelationJob job = new CrossCorrelationJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        assertEquals("Number of results is incorrect", 2, outputMatrices.size());
        int matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	for (int i = 0;  i < result.getHeight(); i++) {
        		for (int j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + i + ", j=" + j + " is wrong", matrixCross[i][j], result.getElement((short)i, (short)j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }
	}
		
	private List<float[][]> localNormCrossBCorrelation(final List<float[][]> matricesF, final List<float[][]> matricesG) {
		float[][] matrixF = matricesF.get(0);
		float[][] matrixG;
		
		final int dimCrossI = 2*matrixF.length-1;
		final int dimCrossJ = 2*matrixF[0].length-1;
		
		final int dimI = matrixF.length;
		final int dimJ = matrixF[0].length;
		
		List<float[][]> results = new ArrayList<float[][]>(matricesF.size());
		
		for (int matrixIndex = 0; matrixIndex < matricesF.size(); matrixIndex++) {
			float[][] result = new float[dimCrossI][dimCrossJ];
			
			matrixF = matricesF.get(matrixIndex);
			matrixG = matricesG.get(matrixIndex);

			float avgF = 0.0f;
			for (int i = 0; i < dimI; i++) {
				for (int j = 0; j < dimJ; j++) {
					avgF += matrixF[i][j];
				}
			}
			avgF /= (float)(dimI * dimJ);
			
			for (int i = -dimCrossI/2; i <= dimCrossI/2; i++) {
				for (int j = -dimCrossJ/2; j <= dimCrossJ/2; j++) {
					float accum = 0;

					float avgG = 0.0f;
					for (int n = -dimCrossI/2; n <= dimCrossI/2; n++) {
						for (int m = -dimCrossJ/2; m <= dimCrossJ/2; m++) {
							if (i + n < 0 || j + m < 0 || i + n >= dimI || j + m >= dimJ) {
								continue;
							}
							avgG += matrixG[i + n][j + m];
						}
					}
					avgG /= (float)(dimI * dimJ);
					
					float stdDevF = 0.0f;
					float stdDevG = 0.0f;
					for (int n = -dimCrossI/2; n <= dimCrossI/2; n++) {
						for (int m = -dimCrossJ/2; m <= dimCrossJ/2; m++) {
							if (n < 0 || m < 0 || n >= dimI || m >= dimJ) {
								continue;
							}
							float a = matrixF[n][m] - avgF;
							stdDevF += a*a;
						}
					}
					
					for (int n = -dimCrossI/2; n <= dimCrossI/2; n++) {
						for (int m = -dimCrossI/2; m <= dimCrossJ/2; m++) {
							if (i + n < 0 || j + m < 0 || i + n >= dimI || j + m >= dimJ) {
								continue;
							}
							float a = matrixG[i + n][j + m] - avgG;
							stdDevG += a*a;
						}
					}
					
					for (int n = -dimCrossI/2; n <= dimCrossI/2; n++) {
						for (int m = -dimCrossJ/2; m <= dimCrossJ/2; m++) {
							if (n < 0 || m < 0 || i + n < 0 || j + m < 0) {
								continue;
							}
							if (i + n >= dimI || j + m >= dimJ) {
								continue;
							}

							
							accum += (matrixF[n][m]-avgF)*(matrixG[i + n][j + m]-avgG)/Math.sqrt(stdDevF*stdDevG);
						}
					}
					
					result[i+dimCrossI/2][j+dimCrossJ/2] = accum;
				}
			}
			results.add(result);
		}
		
		return results;
	}

	private List<Matrix> localNormCrossACorrelation(final List<Matrix> matricesF, final List<Matrix> matricesG) {
		Matrix matrixF = matricesF.get(0);
		Matrix matrixG;
		
		final int dimCrossI = 2*matrixF.getHeight()-1;
		final int dimCrossJ = 2*matrixF.getWidth()-1;
		
		final int dimI = matrixF.getHeight();
		final int dimJ = matrixF.getWidth();
		
		List<Matrix> results = new ArrayList<Matrix>(matricesF.size());
		
		for (int matrixIndex = 0; matrixIndex < matricesF.size(); matrixIndex++) {
			float[][] result = new float[dimCrossI][dimCrossJ];
			
			matrixF = matricesF.get(matrixIndex);
			matrixG = matricesG.get(matrixIndex);

			float avgF = 0.0f;
			float avgG = 0.0f;
			for (short i = 0; i < dimI; i++) {
				for (short j = 0; j < dimJ; j++) {
					avgF += matrixF.getElement(i, j);
					avgG += matrixG.getElement(i, j);
				}
			}
			avgF /= (float)(dimI * dimJ);
			avgG /= (float)(dimI * dimJ);
			
			float stdDevF = 0.0f;
			for (short n = 0; n < dimI; n++) {
				for (short m = 0; m < dimJ; m++) {
					if (n < 0 || m < 0 || n >= dimI || m >= dimJ) {
						continue;
					}
					float a = matrixF.getElement(n, m) - avgF;
					stdDevF += a*a;
				}
			}
			
			for (short i = (short)(-dimCrossI/2); i <= dimCrossI/2; i++) {
				for (short j = (short)(-dimCrossJ/2); j <= dimCrossJ/2; j++) {
					float accum = 0;
					
					float stdDevG = 0.0f;
					for (int n = -dimCrossI/2; n <= dimCrossI/2; n++) {
						for (int m = -dimCrossJ/2; m <= dimCrossJ/2; m++) {
							if (i + n < 0 || j + m < 0 || i + n >= dimI || j + m >= dimJ) {
								continue;
							}
							float a = matrixG.getElement((short)(i + n),(short)(j + m)) - avgG;
							stdDevG += a*a;
						}
					}
					
					for (short n = (short)(-dimCrossI/2); n <= dimCrossI/2; n++) {
						for (short m = (short)(-dimCrossJ/2); m <= dimCrossJ/2; m++) {
							if (n < 0 || m < 0 || i + n < 0 || j + m < 0) {
								continue;
							}
							if (i + n >= dimI || j + m >= dimJ) {
								continue;
							}

							
							accum += (matrixF.getElement(n,m)-avgF)*(matrixG.getElement((short)(i + n),(short)(j + m))-avgG)/Math.sqrt(stdDevF*stdDevG);
						}
					}
					
					result[i+dimCrossI/2][j+dimCrossJ/2] = accum;
				}
			}
			Matrix mResult = new MatrixFloat((short)dimCrossI, (short)dimCrossJ);
			mResult.copyMatrixFrom2DArray(result, 0, 0);
			results.add(mResult);
		}
		
		return results;
	}
	
	@Test
	public void simpleTestLocalCrossCorrelationPass() {
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
	    
        List<Matrix> outputMatrices = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        
        int matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], result.getElement(i, j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }		
	}

	@Test
	public void simpleTestLocalCross3x3CorrelationPass() {
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[3][3];
		float[][] matrixG = new float[3][3];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)3,(short)3);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)3,(short)3);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(g);//Invert order to match MATLAB xcorr2
		inputMatricesG.add(f);

		float[][] matrixCross = new float[5][5];
		matrixCross[0][0] = 42.0f;  //Xcorr(-2,-2)
	    matrixCross[0][1] = 84.0f;  //Xcorr(-2, -1)
	    matrixCross[0][2] = 126.0f; //Xcorr(-2, 0)
	    matrixCross[0][3] = 84.0f;  //Xcorr(-2, 1)
	    matrixCross[0][4] = 42.0f;  //Xcorr(-2, 2)
	    matrixCross[1][0] = 91.0f;  //Xcorr(-1,-2)
	    matrixCross[1][1] = 169.0f; //Xcorr(-1,-1)
	    matrixCross[1][2] = 247.0f; //Xcorr(-1, 0)
	    matrixCross[1][3] = 156.0f; //Xcorr(-1, 1)
	    matrixCross[1][4] = 78.0f;  //Xcorr(-1, 2)
	    matrixCross[2][0] = 134.0f; //Xcorr( 0,-2)
	    matrixCross[2][1] = 248.0f; //Xcorr( 0,-1)
	    matrixCross[2][2] = 356.0f; //Xcorr( 0, 0)
	    matrixCross[2][3] = 223.0f; //Xcorr( 0, 1)
	    matrixCross[2][4] = 108.0f; //Xcorr( 0, 2)
	    matrixCross[3][0] = 91.0f;  //Xcorr( 1,-2)
	    matrixCross[3][1] = 162.0f; //Xcorr( 1,-1)
	    matrixCross[3][2] = 227.0f; //Xcorr( 1, 0)
	    matrixCross[3][3] = 138.0f; //Xcorr( 1, 1)
	    matrixCross[3][4] = 66.0f;  //Xcorr( 1, 2)
	    matrixCross[4][0] = 42.0f;  //Xcorr( 2,-2)
	    matrixCross[4][1] = 77.0f;  //Xcorr( 2,-1)
	    matrixCross[4][2] = 106.0f; //Xcorr( 2, 0)
	    matrixCross[4][3] = 65.0f;  //Xcorr( 2, 1)
	    matrixCross[4][4] = 30.0f;  //Xcorr( 2, 2)
	    
        List<Matrix> outputMatrices = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        
        int matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", matrixCross[i][j], result.getElement(i, j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }				
	}


	@Test
	public void simpleCrossCorrelation3x3GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[3][3];
		float[][] matrixG = new float[3][3];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)3,(short)3);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)3,(short)3);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationJob job = new CrossCorrelationJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
        					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-10);
        		}
        	}
        	matrixIndex++;
        }				

	}

	@Test
	public void simpleCrossCorrelationFFT4x4GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[4][4];
		float[][] matrixG = new float[4][4];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)4,(short)4);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)4,(short)4);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationFFTStdJob job = new CrossCorrelationFFTStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
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
	public void simpleCrossCorrelationFFTParInterleavedStd4x4GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[4][4];
		float[][] matrixG = new float[4][4];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)4,(short)4);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)4,(short)4);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationFFTParInterleavedStdJob job = new CrossCorrelationFFTParInterleavedStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
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
	public void simpleCrossCorrelationFFTBasic4x4GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[4][4];
		float[][] matrixG = new float[4][4];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)4,(short)4);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)4,(short)4);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationFFTBasicJob job = new CrossCorrelationFFTBasicJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
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
	public void simpleCrossCorrelationFFT4x4JavaPass() {
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[4][4];
		float[][] matrixG = new float[4][4];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)4,(short)4);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)4,(short)4);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		
		inputMatricesF.add(f);
		inputMatricesG.add(g);
		
		float[][] xr = new float[8][8];
		float[][] xi = new float[8][8];
		float[][] yr = new float[8][8];
		float[][] yi = new float[8][8];
		
		f.copyMatrixTo2DArray(xr, 0, 0);
		g.copyMatrixTo2DArray(yr, 0, 0);
		
		SimpleFFT simpleFFT = new SimpleFFT(xr.length, xr[0].length);
		simpleFFT.computeCrossCorrelationFFT2DSerial(xr, xi, yr, yi);

		
		List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

		/*for (int i = 0; i < xr.length; i++) {
			simpleFFT.printArray(xr[i]);
		}*/
		
        int matrixIndex = 0;
    	Matrix refMatrix = outputMatricesLocal.get(0);
    	for (short i = 0;  i < refMatrix.getHeight(); i++) {
    		for (short j = 0; j < refMatrix.getWidth(); j++) {
    			System.out.println("Vaule: " + refMatrix.getElement(i, j));
    			assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
    					", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), xr[i][j], 1e-4);
    		}
    	}
    	matrixIndex++;
	}

	public void crossCorrelationFFTParStd2x2(ComputationDevice device) {
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
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
	public void simpleCrossCorrelationFFTParStd2x2GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
		crossCorrelationFFTParStd2x2(gpuDevice);
	}
	
	@Test
	public void simpleCrossCorrelationFFTParStd2x2CPUPass() {
	    assumeTrue("No OpenCL CPU device is available", cpuDevice != null);
		crossCorrelationFFTParStd2x2(cpuDevice);
	}
	
	public void crossCorrelationFFTParStd4x4(ComputationDevice device) {
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[4][4];
		float[][] matrixG = new float[4][4];
		matrixF[0][0] = 6.0f;
		matrixF[0][1] = 6.0f;
		matrixF[0][2] = 6.0f;
		matrixF[1][0] = 7.0f;
		matrixF[1][1] = 6.0f;
		matrixF[1][2] = 6.0f;
		matrixF[2][0] = 7.0f;
		matrixF[2][1] = 7.0f;
		matrixF[2][2] = 6.0f;
		
		matrixG[0][0] = 5.0f;
		matrixG[0][1] = 5.0f;
		matrixG[0][2] = 6.0f;
		matrixG[1][0] = 6.0f;
		matrixG[1][1] = 6.0f;
		matrixG[1][2] = 7.0f;
		matrixG[2][0] = 7.0f;
		matrixG[2][1] = 7.0f;
		matrixG[2][2] = 7.0f;
		
		Matrix f = new MatrixFloat((short)4,(short)4);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)4,(short)4);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);

		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device,false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);

        int matrixIndex = 0;
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
	public void simpleCrossCorrelationFFTParStd4x4GPUPass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
		crossCorrelationFFTParStd4x4(gpuDevice);
	}
	
	@Test
	public void simpleCrossCorrelationFFTParStd4x4CPUPass() {
	    assumeTrue("No OpenCL CPU device is available", cpuDevice != null);
		crossCorrelationFFTParStd4x4(cpuDevice);
	}

	public void simpleNormCrossCorrelation2x2GPUPass(ComputationDevice device) {
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

		List<Matrix> results = localNormCrossACorrelation(inputMatricesF, inputMatricesG);
		
		/*float[][] matrixCross = new float[3][3];
		matrixCross[0][0] = 0.3826858103275299f;  //Xcorr(-1,-1)
	    matrixCross[0][1] = 0.5093021988868713f;  //Xcorr(-1, 0)
	    matrixCross[0][2] = 0.3115149438381195f;  //Xcorr(-1, 1)
	    matrixCross[1][0] = 0.6471832394599915f;  //Xcorr( 0,-1)
	    matrixCross[1][1] = 1.2969194650650024f; //Xcorr( 0, 0)
	    matrixCross[1][2] = 7.0f;  //Xcorr( 0, 1)
	    matrixCross[2][0] = 3.0f;  //Xcorr( 1,-1)
	    matrixCross[2][1] = 5.5f;  //Xcorr( 1, 0)
	    matrixCross[2][2] = 2.0f;  //Xcorr( 1, 1)*/
	    
		CrossCorrelationJob job = new CrossCorrelationJob(device, true, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        
        XCorrelationResults resultsCross = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = resultsCross.getCrossMatrices();
        int resultIndex = 0;
        for (Matrix result : outputMatrices) {
        	Matrix matrixCross = results.get(resultIndex);
        	for (short i = 0;  i < result.getHeight(); i++) {
        		for (short j = 0; j < result.getWidth(); j++) {
        			assertEquals("Computed cross-correlation value for i=" + i + ", j=" + j + " is wrong", 
        					matrixCross.getElement(i, j), 
        					result.getElement(i, j), 1e-10);
        		}
        	}
        	resultIndex++;
        }
	}
	
	@Test
	public void testSimpleCrossCorrelationFromTestImage1Pass() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    
		String filenameA = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif";
		String filenameB = "testFiles" + File.separator +"Rota_Iso_D4_C4_N0_OUT0_LT0_0010_B.tif";
		
		int top = 0;
		int left = 0;
		int width = 32;
		int height = 32;
		
		Matrix matrixA = ImageTestHelper.getMatrixFromClippedImage(filenameA, top, left, height, width);
		Matrix matrixB = ImageTestHelper.getMatrixFromClippedImage(filenameB, top, left, height, width);
		ImageTestHelper.writeMatrixToImage((MatrixByte)matrixA, "testA.png");
		ImageTestHelper.writeMatrixToImage((MatrixByte)matrixB, "testB.png");
		
		List<Matrix> inputMatricesF = new ArrayList<Matrix>(1);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(1);
		inputMatricesF.add(matrixA);
		inputMatricesG.add(matrixB);
		
		CrossCorrelationJob job = new CrossCorrelationJob(gpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix resultLocal = outputMatricesLocal.get(0);
        
        XCorrelationResults resultsCross = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = resultsCross.getCrossMatrices();
        Matrix result = outputMatrices.get(0);
                
        assertEquals("Cross correlation doesn't match expected dimI dimensions", resultLocal.getHeight(), result.getHeight());
        assertEquals("Cross correlation doesn't match expected dimJ dimensions", resultLocal.getWidth(), result.getWidth());
        
        for (int i = 0; i < resultLocal.getHeight(); i++) {
        	for (int j = 0; j < resultLocal.getWidth(); j++) {
        		assertEquals("Cross-correlation matrix at I=" + i + ", J=" + j + " is wrong", resultLocal.getElement(i, j), result.getElement(i, j), 1e-10f);
        	}
        }
	}
	
	@Test
	public void testSimpleTileCrossCorrelationFromTestImage1Pass() {
	    assumeTrue("No OpenCL CPU device is available", cpuDevice != null);
	    
		String filenameA = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif";
		String filenameB = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_B.tif";
		
		int top = 0;
		int left = 0;
		int width = 32;
		int height = 32;
		
		Matrix matrixA = ImageTestHelper.getMatrixFromClippedImage(filenameA, top, left, height, width);
		Matrix matrixB = ImageTestHelper.getMatrixFromClippedImage(filenameB, top, left, height, width);
		ImageTestHelper.writeMatrixToImage((MatrixByte)matrixA, "testA.png");
		ImageTestHelper.writeMatrixToImage((MatrixByte)matrixB, "testB.png");
		
		List<Matrix> inputMatricesF = new ArrayList<Matrix>(1);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(1);
		inputMatricesF.add(matrixA);
		inputMatricesG.add(matrixB);
		
		CrossCorrelationJob job = new CrossCorrelationJob(cpuDevice, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix resultLocal = outputMatricesLocal.get(0);
        
        XCorrelationResults resultsCross = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = resultsCross.getCrossMatrices();
        Matrix result = outputMatrices.get(0);
                
        assertEquals("Cross correlation doesn't match expected dimI dimensions", resultLocal.getHeight(), result.getHeight());
        assertEquals("Cross correlation doesn't match expected dimJ dimensions", resultLocal.getWidth(), result.getWidth());
        
        for (int i = 0; i < resultLocal.getHeight(); i++) {
        	for (int j = 0; j < resultLocal.getWidth(); j++) {
        		assertEquals("Cross-correlation matrix at I=" + i + ", J=" + j + " is wrong", resultLocal.getElement(i, j), result.getElement(i, j), 1e-10f);
        	}
        }
	}

    @Test
    public void simpleCrossCorrelationRealFFTParStd4x4GPUPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        crossCorrelationRealFFTParStd4x4(gpuDevice);
    }


   public void crossCorrelationRealFFTParStd4x4(ComputationDevice device) {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();
        List<Matrix> inputMatricesFLocal = new ArrayList<Matrix>();
        List<Matrix> inputMatricesGLocal = new ArrayList<Matrix>();
    
        float[][] matrixF = new float[4][4];
        float[][] matrixG = new float[4][4];
        matrixF[0][0] = 6.0f;
        matrixF[0][1] = 6.0f;
        matrixF[0][2] = 6.0f;
        matrixF[1][0] = 7.0f;
        matrixF[1][1] = 6.0f;
        matrixF[1][2] = 6.0f;
        matrixF[2][0] = 7.0f;
        matrixF[2][1] = 7.0f;
        matrixF[2][2] = 6.0f;
        
        matrixG[0][0] = 5.0f;
        matrixG[0][1] = 5.0f;
        matrixG[0][2] = 6.0f;
        matrixG[1][0] = 6.0f;
        matrixG[1][1] = 6.0f;
        matrixG[1][2] = 7.0f;
        matrixG[2][0] = 7.0f;
        matrixG[2][1] = 7.0f;
        matrixG[2][2] = 7.0f;
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixF, 0, 0);
        f.computeMaxValue();
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixG, 0, 0);
        g.computeMaxValue();
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        //Match normalizing behavior of Matrix.copyMirroredMatrixToArrayAndNormalize(...)
        float maxValueF = 0.0f;
        float maxValueG = 0.0f;
        for (int i = 0; i < matrixF.length; i++) {
            for (int j = 0; j < matrixF[0].length; j++) {
                if (matrixF[i][j] > maxValueF) {
                    maxValueF = matrixF[i][j];
                }
                if (matrixG[i][j] > maxValueG) {
                    maxValueG = matrixG[i][j];
                }
            }
        }
        
        for (int i = 0; i < matrixF.length; i++) {
            for (int j = 0; j < matrixF[0].length; j++) {
                matrixF[i][j] = matrixF[i][j] / maxValueF * 16.0f;
                matrixF[i][j] = matrixF[i][j] / maxValueF * 16.0f;
            }
        }

        Matrix fl = new MatrixFloat((short)4,(short)4);
        fl.copyMatrixFrom2DArray(matrixF, 0, 0);
        fl.computeMaxValue();
        Matrix gl = new MatrixFloat((short)4,(short)4);
        gl.copyMatrixFrom2DArray(matrixG, 0, 0);
        gl.computeMaxValue();
        inputMatricesFLocal.add(fl);
        inputMatricesGLocal.add(gl);

        float gr[][] = new float[8][8];
        float gi[][] = new float[8][8];
        float fr[][] = new float[8][8];
        float fi[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    gr[i][j] = matrixG[i][j];
                    fr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                } else {
                    gr[i][j] = 0.0f;
                    fr[i][j] = 0.0f;
                }
                gi[i][j] = 0.0f;
                fi[i][j] = 0.0f;                
            }
        }
        
        SimpleFFT fft = new SimpleFFT(8, 8);
        fft.computeFFT2D(fr, fi);
        SimpleFFT.dump2DArray("FFT F Real", fr);
        SimpleFFT.dump2DArray("FFT F Imag", fi);
        fft.computeFFT2D(gr, gi);
        SimpleFFT.dump2DArray("FFT G Real", gr);
        SimpleFFT.dump2DArray("FFT G Imag", gi);

        float tr[][] = new float[8][8];
        float ti[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    tr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                    ti[i][j] = matrixG[i][j];
                } else {
                    tr[i][j] = 0.0f;
                    ti[i][j] = 0.0f;
                }
            }
        }
        
        float Fr[][] = new float[8][8];
        float Fi[][] = new float[8][8];
        float Gr[][] = new float[8][8];
        float Gi[][] = new float[8][8];
        SimpleFFT fftReal = new SimpleFFT(8, 8);
        fftReal.computeFFT2D(tr, ti);
        for (int i = 0; i <= gr.length/2; i++) {
            for (int j = 0; j <= gr[0].length/2; j++) {
                int k1 = i;
                int Nk1 = (gr.length - k1) % gr.length;
                int k2 = j;
                int Nk2 = (gr.length - k2) % gr.length;

                Gr[k1][k2] = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                Gi[k1][k2] = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                
                Gr[Nk1][Nk2] = Gr[k1][k2];
                Gi[Nk1][Nk2] = -Gi[k1][k2];

                Fr[k1][k2] = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                Fi[k1][k2] = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                
                Fr[Nk1][Nk2] = Fr[k1][k2];
                Fi[Nk1][Nk2] = -Fi[k1][k2];

                
                if (k1 < gr.length/2 && k2 < gr.length/2) {
                    k1 = i + gr.length/2;
                    Nk1 = (gr.length - k1) % gr.length;
                    
                    Gr[k1][k2] = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                    Gi[k1][k2] = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                    
                    Gr[Nk1][Nk2] = Gr[k1][k2];
                    Gi[Nk1][Nk2] = -Gi[k1][k2];
                    
                    Fr[k1][k2] = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                    Fi[k1][k2] = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                    
                    Fr[Nk1][Nk2] = Fr[k1][k2];
                    Fi[Nk1][Nk2] = -Fi[k1][k2];
                }
            }
        }
        SimpleFFT.dump2DArray("Reconstructed F Real", Fr);
        SimpleFFT.dump2DArray("Reconstructed F Imag", Fi);

        SimpleFFT.dump2DArray("Reconstructed G Real", Gr);
        SimpleFFT.dump2DArray("Reconstructed G Imag", Gi);
        
        for (short i = 0; i < fr.length; i++) {
            for (short j = 0; j < fr[0].length; j++) {
                assertEquals("Computed FFT value for Fr" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", fr[i][j], Fr[i][j], 1e-3);
                assertEquals("Computed FFT value for Fi" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", fi[i][j], Fi[i][j], 1e-3);
                assertEquals("Computed FFT value for Gr" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", gr[i][j], Gr[i][j], 1e-3);
                assertEquals("Computed FFT value for Gi" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", gi[i][j], Gi[i][j], 1e-3);
            }
        }

        for (short i = 0; i < fr.length; i++) {
            for (short j = 0; j < fr[0].length; j++) {
                //Complex product
                //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
                float tempRe = Fr[i][j] * Gr[i][j] - Fi[i][j] * Gi[i][j]; 
                float tempIm = Fr[i][j] * Gi[i][j] + Fi[i][j] * Gr[i][j];
                Fr[i][j] = tempRe;
                Fi[i][j] = tempIm;
            }
        }
        
        fftReal.computeIFFT2D(Fr, Fi);

        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesFLocal, inputMatricesGLocal);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < Fr.length - 1; i++) {
            for (short j = 0; j < Fr[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), Fr[i][j], 1e-3);
            }
        }
        
        
        
        float resultM[][] = new float[8][8];
        CrossCorrelationRealFFTParStdJob job = new CrossCorrelationRealFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
        //job.setEmulationMode(EmulationModeEnum.GPU);
        try {            
            job.analyze();
            job.compute();
            XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
            List<Matrix> outputMatrices = results.getCrossMatrices(); 

            for (short i = 0;  i < resultM.length; i++) {
                for (short j = 0; j < resultM[0].length; j++) {
                    resultM[i][j]=results.getArray()[i * resultM[0].length + j];
                }
            }
            SimpleFFT.dump2DArray("GPU computed FRe", resultM);
            
            //List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
    
            int matrixIndex = 0;
            for (Matrix result : outputMatrices) {
                //Matrix refMatrix = outputMatricesLocal.get(matrixIndex);
                refMatrix.copyMatrixTo2DArray(resultM, 0, 0);
                SimpleFFT.dump2DArray("Locally computed XCorr", resultM);                
                result.copyMatrixTo2DArray(resultM, 0, 0);
                SimpleFFT.dump2DArray("GPU computed XCorr", resultM);
                for (short i = 0;  i < result.getHeight(); i++) {
                    for (short j = 0; j < result.getWidth(); j++) {
                        assertEquals("Computed cross-correlation value for matrixIndex=" + matrixIndex +
                                ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result.getElement(i, j), 1e-3);
                    }
                }
                matrixIndex++;
            }
        } finally {
            job.dispose();
        }
    }

}

