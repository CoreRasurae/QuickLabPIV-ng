// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.Matrix;
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
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParBlockStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.FindMaximumFromCenter;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class ImageCrossCorrelationFFTParBlockStdJobTests {
	private static ComputationDevice gpuDevice;
	private static ComputationDevice cpuDevice;

    private static class CLKernelManager extends KernelManager {
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.ACC, Device.TYPE.GPU, Device.TYPE.CPU);
    	}
    }
	
	@BeforeClass
	public static void setup() {
        PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image8Bit);

		gpuDevice = DeviceManager.getSingleton().getGPU();
		cpuDevice = DeviceManager.getSingleton().getCPU();
		
		KernelManager.setKernelManager(new CLKernelManager());
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

	//@Test
	public void simpleCrossCorrelationFromImagePass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
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
		
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
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
		
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        
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
				
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        
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
		
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 
        
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
		
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
		XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices(); 

        
        IMatrixExporterVisitor exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("img1_2_botRight.mat");
        exporter.setPIVContext();
        outputMatrices.get(0).exportToFile(exporter);
        exporter.closeFile();
        
        IMaximumFinder finder = new FindMaximumFromCenter();
        MaxCrossResult result = finder.findMaximum(outputMatrices.get(0));
        System.out.println(result);
        
        assertEquals("Max Cross result occurs at wrong I position", dimCrossI/2 + 10, result.getMainPeakI(), 1e-10f);
        assertEquals("Max Cross result occurs at wrong J position", dimCrossJ/2 + 10, result.getMainPeakJ(), 1e-10f);
        
        assertEquals("Displacement is wrong", 10, result.getMainPeakI() -  (height - 1), 1e-10f);
        assertEquals("Displacement is wrong", 10, result.getMainPeakJ() -  (width - 1), 1e-10f);
	}
	
	public void testSyntheticImageCrossCorrelation() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
	    
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
		
		CrossCorrelationFFTParBlockStdJob job = new CrossCorrelationFFTParBlockStdJob(gpuDevice, false, inputMatricesF, inputMatricesG);
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
