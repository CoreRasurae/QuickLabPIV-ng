package pt.quickLabPIV.images;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assume.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.IMatrixExporterVisitor;
import pt.quickLabPIV.exporter.StructSingleFrameFloatMatlabExporter;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.jobs.ImageReaderJob;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.FindMaximumFromCenter;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;
import pt.quickLabPIV.jobs.xcorr.FastRealFFTXCorr;

public class ImageCrossCorrelationFFTParStdJobTests {
	private static ComputationDevice gpuDevice;
	private static ComputationDevice cpuDevice;
	private static ComputationDevice device;

    private static class CLKernelManager extends KernelManager {
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.ACC, Device.TYPE.GPU, Device.TYPE.CPU);
    	}
    }
    
    private static class DefaultKernelManager extends KernelManager {
        
    }
	
	@BeforeClass
	public static void setup() {
	    PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image8Bit);

		gpuDevice = DeviceManager.getSingleton().getGPU();
		cpuDevice = DeviceManager.getSingleton().getCPU();
		
		if (gpuDevice != null) {
			device = gpuDevice;
		} else {
			device = cpuDevice;
		}
		
		KernelManager.setKernelManager(new CLKernelManager());
		
		assumeTrue("No OpenCL device available", device != null);
	}
	
	@AfterClass
	public static void tearDown() {
	    KernelManager.setKernelManager(new DefaultKernelManager());
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, cpuDevice);
	}

	//@Test
	public void simpleCrossCorrelationFromImagePass() {
		String imageFile1 = "testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg";
		String imageFile2 = "testFiles" + File.separator + "image_1.3or93zbi.000000b.jpg";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(1);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(1);
		
		int height = 32;
		int width = 32;
		
		Matrix matrixA = img1.clipImageMatrix(200, 200, height, width, false, null);
		Matrix matrixB = img2.clipImageMatrix(200, 200, height, width, false, null);
		inputMatricesF.add(matrixA);
		inputMatricesG.add(matrixB);
		
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("matA.mat");
        exporter.setPIVContext();
        matrixA.exportToFile(exporter);
        exporter.closeFile();
        exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("matB.mat");
        exporter.setPIVContext();
        matrixB.exportToFile(exporter);
        exporter.closeFile();
        exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("matA_B.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
    }
	
	public void testSyntheticImageCrossCorrelationTopLeftPass(int height, int width, ComputationDevice device) {
		String imageFile1 = "testFiles" + File.separator + "img1.png";
		String imageFile2 = "testFiles" + File.separator + "img2.png";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(4);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(4);
		
		int dimCrossI = height*2 - 1;
		int dimCrossJ = width*2 - 1;
		
		Matrix matrixTopLeftA = img1.clipImageMatrix(320-height, 320-width, height, width, false, null);
		Matrix matrixTopLeftB = img2.clipImageMatrix(320-height, 320-width, height, width, false, null);
		inputMatricesF.add(matrixTopLeftA);
		inputMatricesG.add(matrixTopLeftB);
		
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        
        float[][] matA = new float[height][width];
        matrixTopLeftA.copyMatrixTo2DArrayAndNormalizeAndOffset(matA, 0, 0);
        float[][] matB = new float[height][width];
        matrixTopLeftB.copyMatrixTo2DArrayAndNormalizeAndOffset(matB, 0, 0);        
        
        float[][] resultMat = FastRealFFTXCorr.computeXCorr(matA, matB);
        SimpleFFT.dump2DArray("XCorr Java", resultMat);

        Matrix resultCPU = new MatrixFloat(resultMat.length, resultMat[0].length);
        resultCPU.copyMatrixFrom2DArray(resultMat, 0, 0);
        
        float[][] resultCL = new float[2*height][2*width];
        outputMatrices.get(0).copyMatrixTo2DArray(resultCL, 0, 0);
        SimpleFFT.dump2DArray("XCorr OpenCL", resultCL);
        
        for (int i = 0; i < 2*height-1; i++) {
            for (int j = 0; j < 2*width-1; j++) {
              assertEquals("Cross correlation doesn't match expected at [i=" + i + ", j=" + j + "]", 
                      resultMat[i][j] >= 0.0 ? resultMat[i][j] : 0.0f, resultCL[i][j], 1.0f);  
            }
        }    
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_upLeft.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        IMaximumFinder finder = new FindMaximumFromCenter();
        MaxCrossResult result = finder.findMaximum(outputMatrices.get(0));
        System.out.println(result);
        
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI/2 - 10, result.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ/2 - 10, result.getMainPeakJ(), 1e-10f);
        
        assertEquals("Displacement is wrong", -10, result.getMainPeakI() -  (height - 1), 1e-10f);
        assertEquals("Displacement is wrong", -10, result.getMainPeakJ() -  (width - 1), 1e-10f);
	}

	public void testSyntheticImageCrossCorrelationBottomLeftPass(int height, int width, ComputationDevice device) {
		String imageFile1 = "testFiles" + File.separator + "img1.png";
		String imageFile2 = "testFiles" + File.separator + "img2.png";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(4);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(4);
		
		int dimCrossI = height*2 - 1;
		int dimCrossJ = width*2 - 1;
		
		Matrix matrixBottomLeftA = img1.clipImageMatrix(320, 320-width, height, width, false, null);
		Matrix matrixBottomLeftB = img2.clipImageMatrix(320, 320-width, height, width, false, null);
		inputMatricesF.add(matrixBottomLeftA);
		inputMatricesG.add(matrixBottomLeftB);
		
		/*Image t1 = new Image((MatrixByte)matrixBottomLeftA, width, height, "t1.png");
		t1.writeToFile(true);
		Image t2 = new Image((MatrixByte)matrixBottomLeftB, width, height, "t2.png");
		t2.writeToFile(true);*/
				
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        float[][] matA = new float[height][width];
        matrixBottomLeftA.copyMatrixTo2DArrayAndNormalizeAndOffset(matA, 0, 0);
        float[][] matB = new float[height][width];
        matrixBottomLeftB.copyMatrixTo2DArrayAndNormalizeAndOffset(matB, 0, 0);
        
        float[][] resultMat = FastRealFFTXCorr.computeXCorr(matA, matB);
        SimpleFFT.dump2DArray("XCorr Java", resultMat);

        Matrix resultCPU = new MatrixFloat(resultMat.length, resultMat[0].length);
        resultCPU.copyMatrixFrom2DArray(resultMat, 0, 0);
        
        float[][] resultCL = new float[2*height][2*width];
        outputMatrices.get(0).copyMatrixTo2DArray(resultCL, 0, 0);
        SimpleFFT.dump2DArray("XCorr OpenCL", resultCL);
        
        double maxValueValidation = 0.0f;
        double maxValueComputed = 0.0f;
        for (int i = 0; i < 2*height-1; i++) {
            for (int j = 0; j < 2*width-1; j++) {
                if (resultMat[i][j] > maxValueValidation) {
                    maxValueValidation = resultMat[i][j]; 
                }
                if (resultCL[i][j] > maxValueComputed) {
                    maxValueComputed = resultCL[i][j]; 
                }
            }
        }
        
        for (int i = 0; i < 2*height-1; i++) {
            for (int j = 0; j < 2*width-1; j++) {
              assertEquals("Cross correlation doesn't match expected at [i=" + i + ", j=" + j + "]", 
                      resultMat[i][j]/maxValueValidation*maxValueComputed, resultCL[i][j], 1.4f);  
            }
        }
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_botLeft.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        IMaximumFinder finder = new FindMaximumFromCenter();
        MaxCrossResult result = finder.findMaximum(outputMatrices.get(0));
        System.out.println(result);
        
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI/2 + 10, result.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ/2 - 10, result.getMainPeakJ(), 1e-10f);
        
        assertEquals("Displacement is wrong", 10, result.getMainPeakI() -  (height - 1), 1e-10f);
        assertEquals("Displacement is wrong", -10, result.getMainPeakJ() -  (width - 1), 1e-10f);
  	}

	public void testSyntheticImageCrossCorrelationTopRightPass(int height, int width, ComputationDevice device) {
		String imageFile1 = "testFiles" + File.separator + "img1.png";
		String imageFile2 = "testFiles" + File.separator + "img2.png";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(4);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(4);
		
		int dimCrossI = height*2 - 1;
		int dimCrossJ = width*2 - 1;
		
		Matrix matrixTopRightA = img1.clipImageMatrix(320-height, 320, height, width, false, null);
		Matrix matrixTopRightB = img2.clipImageMatrix(320-height, 320, height, width, false, null);
		inputMatricesF.add(matrixTopRightA);
		inputMatricesG.add(matrixTopRightB);
		
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        float[][] matA = new float[height][width];
        matrixTopRightA.copyMatrixTo2DArrayAndNormalizeAndOffset(matA, 0, 0);
        float[][] matB = new float[height][width];
        matrixTopRightB.copyMatrixTo2DArrayAndNormalizeAndOffset(matB, 0, 0);
        
        float[][] resultMat = FastRealFFTXCorr.computeXCorr(matA, matB);
        SimpleFFT.dump2DArray("XCorr Java", resultMat);

        Matrix resultCPU = new MatrixFloat(resultMat.length, resultMat[0].length);
        resultCPU.copyMatrixFrom2DArray(resultMat, 0, 0);
        
        float[][] resultCL = new float[2*height][2*width];
        outputMatrices.get(0).copyMatrixTo2DArray(resultCL, 0, 0);
        SimpleFFT.dump2DArray("XCorr OpenCL", resultCL);

        float maxValueCL = computeMaxValue(resultCL);
        
        for (int i = 0; i < 2*height-1; i++) {
            for (int j = 0; j < 2*width-1; j++) {
              assertEquals("Cross correlation doesn't match expected at [i=" + i + ", j=" + j + "]", 
                      resultMat[i][j], resultCL[i][j], 1.0f);  
            }
        }    
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_upRight.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        IMaximumFinder finder = new FindMaximumFromCenter();
        MaxCrossResult result = finder.findMaximum(outputMatrices.get(0));
        System.out.println(result);
        
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI/2 - 10, result.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ/2 + 10, result.getMainPeakJ(), 1e-10f);
        
        assertEquals("Displacement is wrong", -10, result.getMainPeakI() -  (height - 1), 1e-10f);
        assertEquals("Displacement is wrong", 10, result.getMainPeakJ() -  (width - 1), 1e-10f);        
	}

	public void testSyntheticImageCrossCorrelationBottomRightPass(int height, int width, ComputationDevice device) {
		String imageFile1 = "testFiles" + File.separator + "img1.png";
		String imageFile2 = "testFiles" + File.separator + "img2.png";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(4);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(4);
		
		int dimCrossI = height*2 - 1;
		int dimCrossJ = width*2 - 1;
		
		Matrix matrixBottomRightA = img1.clipImageMatrix(320, 320, height, width, false, null);
		Matrix matrixBottomRightB = img2.clipImageMatrix(320, 320, height, width, false, null);
		inputMatricesF.add(matrixBottomRightA);
		inputMatricesG.add(matrixBottomRightB);
		
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        float[][] matA = new float[height][width];
        matrixBottomRightA.copyMatrixTo2DArrayAndNormalizeAndOffset(matA, 0, 0);
        float[][] matB = new float[height][width];
        matrixBottomRightB.copyMatrixTo2DArrayAndNormalizeAndOffset(matB, 0, 0);
        
        float[][] resultMat = FastRealFFTXCorr.computeXCorr(matA, matB);
        SimpleFFT.dump2DArray("XCorr Java", resultMat);

        Matrix resultCPU = new MatrixFloat(resultMat.length, resultMat[0].length);
        resultCPU.copyMatrixFrom2DArray(resultMat, 0, 0);
        
        float[][] resultCL = new float[2*height][2*width];
        outputMatrices.get(0).copyMatrixTo2DArray(resultCL, 0, 0);
        SimpleFFT.dump2DArray("XCorr OpenCL", resultCL);
                
        for (int i = 0; i < 2*height-1; i++) {
            for (int j = 0; j < 2*width-1; j++) {
              assertEquals("Cross correlation doesn't match expected at [i=" + i + ", j=" + j + "]", 
                      resultMat[i][j], resultCL[i][j], 1.0f);  
            }
        }
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_botRight.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        IMaximumFinder finder = new FindMaximumFromCenter();
        MaxCrossResult result = finder.findMaximum(outputMatrices.get(0));
        //MaxCrossResult result = finder.findMaximum(resultCPU);
        System.out.println(result);
        
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI/2 + 10, result.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ/2 + 10, result.getMainPeakJ(), 1e-10f);
        
        assertEquals("Displacement is wrong", 10, result.getMainPeakI() -  (height - 1), 1e-10f);
        assertEquals("Displacement is wrong", 10, result.getMainPeakJ() -  (width - 1), 1e-10f);
	}
	
	private float computeMaxValue(float[][] matrix) {
	    float maxValue = 0;
	    
	    for (int i = 0; i < matrix.length; i++) {
	        for (int j = 0; j < matrix.length; j++) {
	            if (matrix[i][j] > maxValue) {
	                maxValue = matrix[i][j];
	            }
	        }
	    }
	    
	    return maxValue;
    }

    public void testSyntheticImageCrossCorrelation() {
		String imageFile1 = "testFiles" + File.separator + "img1.png";
		String imageFile2 = "testFiles" + File.separator + "img2.png";
		
		ImageReaderJob job1 = new ImageReaderJob(imageFile1);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		ImageReaderJob job2 = new ImageReaderJob(imageFile2);
		job2.analyze();
		job2.compute();
		
		IImage img2 = job2.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);

		List<Matrix> inputMatricesF = new ArrayList<Matrix>(4);
		List<Matrix> inputMatricesG = new ArrayList<Matrix>(4);
		
		int width = 256;
		int height = 256;

		int dimCrossI = height * 2 + 1;
		int dimCrossJ = width * 2 + 1;
		
		Matrix matrixTopLeftA = img1.clipImageMatrix(320-height, 320-width, height, width, false, null);
		Matrix matrixTopLeftB = img2.clipImageMatrix(320-height, 320-width, height, width, false, null);
		inputMatricesF.add(matrixTopLeftA);
		inputMatricesG.add(matrixTopLeftB);
		
		Matrix matrixBottomLeftA = img1.clipImageMatrix(320, 320-width, height, width, false, null);
		Matrix matrixBottomLeftB = img2.clipImageMatrix(320, 320-width, height, width, false, null);
		inputMatricesF.add(matrixBottomLeftA);
		inputMatricesG.add(matrixBottomLeftB);
		
		/*Image t1 = new Image((MatrixByte)matrixBottomLeftA, width, height, "t1.png");
		t1.writeToFile(true);
		Image t2 = new Image((MatrixByte)matrixBottomLeftB, width, height, "t2.png");
		t2.writeToFile(true);*/
		
		Matrix matrixTopRightA = img1.clipImageMatrix(320-height, 320, height, width, false, null);
		Matrix matrixTopRightB = img2.clipImageMatrix(320-height, 320, height, width, false, null);
		inputMatricesF.add(matrixTopRightA);
		inputMatricesG.add(matrixTopRightB);
		
		Matrix matrixBottomRightA = img1.clipImageMatrix(320, 320, height, width, false, null);
		Matrix matrixBottomRightB = img2.clipImageMatrix(320, 320, height, width, false, null);
		inputMatricesF.add(matrixBottomRightA);
		inputMatricesG.add(matrixBottomRightB);
		
		CrossCorrelationFFTParStdJob job = new CrossCorrelationFFTParStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        assertEquals("Number of computed cross-correlation matrices is incorrect" , 4, outputMatrices.size());
        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_botRight.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        FindMaximumFromCenter finder1 = new FindMaximumFromCenter();
        MaxCrossResult result1 = finder1.findMaximum(outputMatrices.get(0));
        System.out.println(result1);
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI / 2 - 10, result1.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ / 2 - 10, result1.getMainPeakJ(), 1e-10f);
        
        FindMaximumFromCenter finder2 = new FindMaximumFromCenter();
        MaxCrossResult result2 = finder2.findMaximum(outputMatrices.get(1));
        System.out.println(result2);
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI / 2 + 10, result2.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ / 2 - 10, result2.getMainPeakJ(), 1e-10f);
        
        FindMaximumFromCenter finder3 = new FindMaximumFromCenter();
        MaxCrossResult result3 = finder3.findMaximum(outputMatrices.get(2));
        System.out.println(result3);
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI / 2 - 10, result3.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ / 2 + 10, result3.getMainPeakJ(), 1e-10f);

        FindMaximumFromCenter finder4 = new FindMaximumFromCenter();
        MaxCrossResult result4 = finder4.findMaximum(outputMatrices.get(3));
        System.out.println(result4);
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI / 2 + 10, result4.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ / 2 + 10, result4.getMainPeakJ(), 1e-10f);
	}

}
