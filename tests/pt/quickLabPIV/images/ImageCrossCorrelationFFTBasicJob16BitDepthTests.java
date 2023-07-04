package pt.quickLabPIV.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
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
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTBasicJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.FindMaximumFromCenter;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class ImageCrossCorrelationFFTBasicJob16BitDepthTests extends ImageCrossCorrelationFFTBasicJobTests {
	private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
	private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

    private class DefaultKernelManager extends KernelManager {
        
    }
	    
    @Before
    public void setup() {
        KernelManager.setKernelManager(new DefaultKernelManager());
        PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image16Bit);
    }

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x128CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x128CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x128CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x128CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x128GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x128GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x128GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x128GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, gpuDevice);
	}

	@Test
	public void simpleCrossCorrelationFromImagePassCPU() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
	    simpleCrossCorrelationFromImagePass(cpuDevice);
	}
	
	@Test
	public void simpleCrossCorrelationFromImagePassGPU() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
	    simpleCrossCorrelationFromImagePass(gpuDevice);
	}
	
	public void simpleCrossCorrelationFromImagePass(ComputationDevice device) {
		String imageFile1 = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif";
		String imageFile2 = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_B.tif";
		
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
		
		int height = 16;
		int width = 16;
		
		Matrix matrixA = img1.clipImageMatrix(80, 256, height, width, false, null);
		Matrix matrixB = img2.clipImageMatrix(80, 256, height, width, false, null);
		matrixA.computeMaxValue();
		matrixB.computeMaxValue();
		inputMatricesF.add(matrixA);
		inputMatricesG.add(matrixB);
		
		CrossCorrelationFFTBasicJob jobA = new CrossCorrelationFFTBasicJob(device, false, inputMatricesF, inputMatricesG);
		CrossCorrelationJob jobB = new CrossCorrelationJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        jobA.analyze();
	        jobA.compute();
	        jobB.analyze();
	        jobB.compute();
		} finally {
			jobA.dispose();
			jobB.dispose();
		}
        XCorrelationResults resultsA = jobA.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatricesA = resultsA.getCrossMatrices();
        XCorrelationResults resultsB = jobB.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatricesB = resultsB.getCrossMatrices();
        
        Matrix outMatrixFFT = outputMatricesA.get(0);
        Matrix outMatrixDef = outputMatricesB.get(0);

        outMatrixFFT.computeMaxValue();
        outMatrixDef.computeMaxValue();
        float maxValueFFT = outMatrixFFT.getMaxValue();
        float maxValueDef = outMatrixDef.getMaxValue();
        float ratio = maxValueFFT / maxValueDef;
        for (int i = 0; i < outMatrixDef.getHeight(); i++) {
            for (int j = 0; j < outMatrixDef.getWidth(); j++) {
                float value = outMatrixDef.getElement(i, j);
                value = value * ratio;
                outMatrixDef.setElement(value, i, j);
            }
        }
        
        for (int i = 0; i < outMatrixFFT.getHeight(); i++) {
        	for (int j = 0; j < outMatrixFFT.getWidth(); j++) {
        		assertEquals("Cross-correlation doesn't match", outMatrixFFT.getElement(i, j), outMatrixDef.getElement(i, j), 5e-1f);
        	}
        }
        
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
        exporter.openFile("matA_B_FFT.mat");
        exporter.setPIVContext();
        outputMatricesA.get(0).exportToFile(exporter);
        exporter.closeFile();
        exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("matA_B_Def.mat");
        exporter.setPIVContext();
        outputMatricesB.get(0).exportToFile(exporter);
        exporter.closeFile();
    }

	@Test
	public void multiCrossCorrelationFromImagePassCPU() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
	    multiCrossCorrelationFromImagePass(cpuDevice);
	}
	
    @Test
    public void multiCrossCorrelationFromImagePassGPU() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        multiCrossCorrelationFromImagePass(gpuDevice);
    }
	
	public void multiCrossCorrelationFromImagePass(ComputationDevice device) {	    
		String imageFile1 = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif";
		String imageFile2 = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_B.tif";
		
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
		
		int height = 2;
		int width = 2;
		
		Matrix matrixA = img1.clipImageMatrix(80, 256, height, width, false, null);
		Matrix matrixB = img2.clipImageMatrix(80, 256, height, width, false, null);
        matrixA.computeMaxValue();
        matrixB.computeMaxValue();
		inputMatricesF.add(matrixA);
		inputMatricesF.add(matrixB);
		inputMatricesG.add(matrixB);
		inputMatricesG.add(matrixA);

		CrossCorrelationFFTBasicJob jobA = new CrossCorrelationFFTBasicJob(device, false, inputMatricesF, inputMatricesG);
		CrossCorrelationJob jobB = new CrossCorrelationJob(device, false, inputMatricesF, inputMatricesG);
		try {
	        jobA.analyze();
	        jobA.compute();
	        jobB.analyze();
	        jobB.compute();
		} finally {
			jobA.dispose();
			jobB.dispose();
		}
        XCorrelationResults resultsA = jobA.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatricesA = resultsA.getCrossMatrices();
        XCorrelationResults resultsB = jobB.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatricesB = resultsB.getCrossMatrices();
        
        for (int matrixIndex = 0; matrixIndex < outputMatricesA.size(); matrixIndex++) {
            Matrix outMatrixFFT = outputMatricesA.get(matrixIndex);
            Matrix outMatrixDef = outputMatricesB.get(matrixIndex);

            outMatrixFFT.computeMaxValue();
            outMatrixDef.computeMaxValue();
            float maxValueFFT = outMatrixFFT.getMaxValue();
            float maxValueDef = outMatrixDef.getMaxValue();
            float ratio = maxValueFFT / maxValueDef;
            for (int i = 0; i < outMatrixDef.getHeight(); i++) {
                for (int j = 0; j < outMatrixDef.getWidth(); j++) {
                    float value = outMatrixDef.getElement(i, j);
                    value = value * ratio;
                    outMatrixDef.setElement(value, i, j);
                }
            }
            
	        for (int i = 0; i < outMatrixFFT.getHeight(); i++) {
	        	for (int j = 0; j < outMatrixFFT.getWidth(); j++) {
	        		assertEquals("Cross-correlation doesn't match - matrixIndex: "  + matrixIndex + 
	        				", i: " + i + ", j: " + j, outMatrixDef.getElement(i, j), outMatrixFFT.getElement(i, j), 6.5e-2f);
	        	}
	        }
        }

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
        exporter.openFile("matA_B_FFT.mat");
        exporter.setPIVContext();
        outputMatricesA.get(0).exportToFile(exporter);
        exporter.closeFile();
        exporter = new StructSingleFrameFloatMatlabExporter();
        exporter.openFile("matA_B_Def.mat");
        exporter.setPIVContext();
        outputMatricesB.get(0).exportToFile(exporter);
        exporter.closeFile();
    }
}
