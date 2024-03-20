// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.LucasKanadeFloat;
import pt.quickLabPIV.interpolators.SimpleLucasKanadeImpl;
import pt.quickLabPIV.jobs.JobResultEnum;

public class DenseLucasKanadeGpuDenseValidationTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    private String filename;
    private float[][] beforeU;
    private float[][] beforeV;
    private float[][] imageA;
    private float[][] imageB;
    
    @Before
    public void setup() throws IOException {
        filename = "testFiles" + File.separator + "denseLucasKanade_2LevelsPyr_NoLiuShen_Fs2_0_validation.matFloat";
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "before1_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "before1_v");
        int aIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "imageA");
        int bIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "imageB");
        
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);
        imageA = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, aIndex);
        imageB = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bIndex);
    }
    
    private void copyUsVs(float[] us, float[] vs) {
        for (int i = 0; i < beforeU.length; i++) {
            for (int j = 0; j< beforeU[0].length; j++) {
                int idx = i * beforeU[0].length + j;
                us[idx] = beforeU[i][j];
                vs[idx] = beforeV[i][j];
            } 
        }
    }
    
    @Test
    public void denseLucasKanadeValidationWith1Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after1_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after1_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testDenseLucasKanadeGpu(afterU, afterV, 1);
    }

    @Test
    public void simpleLucasKanadeValidationWith1Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after1_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after1_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testSimpleLucasKanade(afterU, afterV, 1);
    }

    //Disabled test, there are differences between the native OpenCL/Python implementation with sampler
    //all the other implementations with coded biLinear samples, however it is not the root cause of the
    //differences found.
    //@Test
    public void denseLucasKanadeValidationWith2Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after2_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after2_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testDenseLucasKanadeGpu(afterU, afterV, 2);
    }

    @Test
    public void denseLucasKanadeValidationAgainsLucasKanadeFloatWith2Iteration() throws IOException {
        testDenseLucasKanadeGpuPixelOffsetAgainstLucasKandeFloat(2);
    }

    @Test
    public void denseLucasKanadeValidationWith2IterationAgainstSimpleLK() throws IOException {
        testDenseLucasKanadeGpuAgainstSimpleLK(2);
    }

    //Disabled test, there are differences between the native OpenCL/Python implementation with sampler
    //all the other implementations with coded biLinear samples, however it is not the root cause of the
    //differences found.
    //@Test
    public void simpleLucasKanadeValidationWith2Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after2_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after2_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testSimpleLucasKanade(afterU, afterV, 2);
    }

    //Disabled test, there are differences between the native OpenCL/Python implementation with sampler
    //all the other implementations with coded biLinear samples, however it is not the root cause of the
    //differences found.
    //@Test
    public void denseLucasKanadeValidationWith3Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after3_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after3_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testDenseLucasKanadeGpu(afterU, afterV, 3);
    }

    @Test
    public void denseLucasKanadeValidationWith3IterationAgainstSimpleLK() throws IOException {
        testDenseLucasKanadeGpuAgainstSimpleLK(3);
    }

    @Test
    public void denseLucasKanadeValidationAgainsLucasKanadeFloatWith3Iteration() throws IOException {
        testDenseLucasKanadeGpuPixelOffsetAgainstLucasKandeFloat(3);
    }

    //Disabled test, there are differences between the native OpenCL/Python implementation with sampler
    //all the other implementations with coded biLinear samples, however it is not the root cause of the
    //differences found.
    //@Test
    public void denseLucasKanadeValidationWith4Iteration() throws IOException {
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after4_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after4_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testDenseLucasKanadeGpu(afterU, afterV, 4);
    }

    @Test
    public void denseLucasKanadeValidationWith4IterationAgainstSimpleLK() throws IOException {
        testDenseLucasKanadeGpuAgainstSimpleLK(4);
    }

    @Test
    public void denseLucasKanadeValidationAgainsLucasKanadeFloatWith4Iteration() throws IOException {
        testDenseLucasKanadeGpuPixelOffsetAgainstLucasKandeFloat(4);
    }

    //Disabled test, there are differences between the native OpenCL/Python implementation with sampler
    //all the other implementations with coded biLinear samples, however it is not the root cause of the
    //differences found.
    //@Test
    public void denseLucasKanadeValidationWith5Iteration() throws IOException {
        int uIndexB = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "before5_u");
        int vIndexB = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "before5_v");

        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexB);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexB);
        
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after5_u");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "after5_v");

        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);

        testDenseLucasKanadeGpu(afterU, afterV, 5);
    }

    @Test
    public void denseLucasKanadeValidationWith5IterationAgainstSimpleLK() throws IOException {
        testDenseLucasKanadeGpuAgainstSimpleLK(5);
    }

    @Test
    public void denseLucasKanadePixelOffsetValidationWith5IterationAgainstSimpleLK() throws IOException {
        testDenseLucasKanadeGpuPixelOffsetAgainstSimpleLK(5);
    }

    @Test
    public void denseLucasKanadeValidationAgainsLucasKanadeFloatWith5Iteration() throws IOException {
        testDenseLucasKanadeGpuPixelOffsetAgainstLucasKandeFloat(5);
    }

    private void testDenseLucasKanadeGpu(final float[][] afterU, final float[][] afterV, final int iterations) {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);
        
        for (int i = 0; i < img1.getHeight(); i++) {
            for (int j = 0; j < img1.getWidth(); j++) {
                float validationPixelA = imageA[i][j];
                float validationPixelB = imageB[i][j];
                float checkA = img1.readPixel(i, j);
                float checkB = img2.readPixel(i, j);
                assertEquals("Image A diffferent than expected at I:" + i + ", J:" + j, validationPixelA, checkA, 1e-3f);
                assertEquals("Image B diffferent than expected at I:" + i + ", J:" + j, validationPixelB, checkB, 1e-3f);
            }
        }
        
        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];

        copyUsVs(us, vs);
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = iterations;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = img1;
        jobInput.imageB = img2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        us = jobResult.us;
        vs = jobResult.vs;
        
        for (int i = 0; i < img1.getHeight(); i++) {
            for (int j = 0; j < img1.getWidth(); j++) {
                int idx = i * img1.getWidth() + j;
                float validationU = afterU[i][j];
                float validationV = afterV[i][j];
                float checkU = us[idx];
                float checkV = vs[idx];
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, checkU, 6e-3f);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, checkV, 6e-3f);
            }
        }
    }

    private void testDenseLucasKanadeGpuAgainstSimpleLK(final int iterations) {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);
                
        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];

        copyUsVs(us, vs);
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = iterations;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = img1;
        jobInput.imageB = img2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        us = jobResult.us;
        vs = jobResult.vs;

        IImage img1T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        

        SimpleLucasKanadeImpl testImpl = new SimpleLucasKanadeImpl(2.0f, 3, false, 27, iterations);
        testImpl.updateImageA(img1T);
        testImpl.updateImageB(img2T);

        for (int i = 8; i < img1.getHeight() - 8; i++) {
            for (int j = 8; j < img1.getWidth() - 8; j++) {
                int idx = i * img1.getWidth() + j;
                double Us[] = testImpl.interpolate(i, j, beforeV[i][j], beforeU[i][j], false);
                float validationU = (float)Us[1];
                float validationV = (float)Us[0];
                float checkU = us[idx];
                float checkV = vs[idx];
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, checkU, 1.2e-2f);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, checkV, 1.2e-2f);
            }
        }
    }

    private void testDenseLucasKanadeGpuPixelOffsetAgainstSimpleLK(final int iterations) {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);
                
        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];

        copyUsVs(us, vs);
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = iterations;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = img1;
        jobInput.imageB = img2;
        jobInput.halfPixelOffset = true;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        us = jobResult.us;
        vs = jobResult.vs;

        IImage img1T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        

        SimpleLucasKanadeImpl testImpl = new SimpleLucasKanadeImpl(2.0f, 3, false, 27, iterations);
        testImpl.updateImageA(img1T);
        testImpl.updateImageB(img2T);

        for (int i = 8; i < img1.getHeight() - 8; i++) {
            for (int j = 8; j < img1.getWidth() - 8; j++) {
                int idx = i * img1.getWidth() + j;
                double Us[] = testImpl.interpolate(i, j, beforeV[i][j], beforeU[i][j], true);
                float validationU = (float)Us[1];
                float validationV = (float)Us[0];
                float checkU = us[idx];
                float checkV = vs[idx];
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, checkU, 1.2e-2f);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, checkV, 1.2e-2f);
            }
        }
    }
    
    private void testSimpleLucasKanade(final float[][] afterU, final float[][] afterV, final int iterations) {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");

        SimpleLucasKanadeImpl testImpl = new SimpleLucasKanadeImpl(2.0f, 3, false, 27, iterations);
        testImpl.updateImageA(img1);
        testImpl.updateImageB(img2);

        for (int i = 8; i < img1.getHeight() - 8; i++) {
            for (int j = 8; j < img1.getWidth() - 8; j++) {
                float validationU = afterU[i][j];
                float validationV = afterV[i][j];
                double Us[] = testImpl.interpolate(i, j, beforeV[i][j], beforeU[i][j], false);
                float checkU = (float)Us[1];
                float checkV = (float)Us[0];
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, checkU, 1e-2f);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, checkV, 1e-2f);
            }
        }
    }
    public void testDenseLucasKanadeGpuPixelOffsetAgainstLucasKandeFloat(int iterations) {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);
                
        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];

        copyUsVs(us, vs);
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = iterations;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = img1;
        jobInput.imageB = img2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        us = jobResult.us;
        vs = jobResult.vs;

        IImage img1T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2T = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        
        LucasKanadeFloat lk = new LucasKanadeFloat(1, 2.0f, 3, 27, iterations, false);
        lk.updateImageA(img1T);
        lk.updateImageB(img2T);
        
        float validationU[] = new float[1];
        float validationV[] = new float[1];
        for (int i = 0; i < img1.getHeight(); i++) {
            for (int j = 0; j < img1.getWidth(); j++) {                
                int idx = i * img1.getWidth() + j;

                float checkU = us[idx];
                float checkV = vs[idx];

                lk.getVelocitiesMatrix(i, j, i + beforeV[i][j], j + beforeU[i][j], validationU, validationV);
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU[0], checkU, 1.1e-2f);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV[0], checkV, 1.1e-2f);

            }
        }
                
    }
    
}
