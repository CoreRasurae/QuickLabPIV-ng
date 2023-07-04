package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import javax.naming.InvalidNameException;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.LiuShenFloat;
import pt.quickLabPIV.interpolators.LiuShenInterpolatorConfiguration;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.interpolators.DenseLiuShenAparapiJob;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LiuShenOptions;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;

public class DenseLiuShenGpuTest {
    private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    private float[][] imageValidLSA;
    private float[][] imageValidLSB;
    private float[][] beforeLK2ndStepU;
    private float[][] beforeLK2ndStepV;
    private float[][] afterLK2ndStepU;
    private float[][] afterLK2ndStepV;
    private float[][] afterLS2ndStepU;
    private float[][] afterLS2ndStepV;

    private void readTestData() throws IOException, InvalidNameException {
        String testDataFilename = "testFiles" + File.separator + "denseLucasKanade_with_LiuShen_2LevelsPyr_Fs2_0_Fs_0_48_validation.matFloat";
        int imageAIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "imageA");
        if (imageAIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        imageValidLSA = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, imageAIndex);

        int imageBIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "imageB");
        if (imageBIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }        
        imageValidLSB = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, imageBIndex);
        //Input data for the final pyramidal step (requires Lucas-Kanade followed by Liu-Shen processing step
        int beforeLK2ndStepUIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "beforeLK2nd1_u");
        if (beforeLK2ndStepUIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        beforeLK2ndStepU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, beforeLK2ndStepUIndex);
        int beforeLK2ndStepVIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "beforeLK2nd1_v");
        if (beforeLK2ndStepVIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        beforeLK2ndStepV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, beforeLK2ndStepVIndex);
        //Input data for the final Liu-Shen processing step (alternate option to not run Lucas-Kanade and jump directly to Liu-Shen)
        int afterLK2ndStepUIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "afterLK2nd1_u");
        if (afterLK2ndStepUIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }

        afterLK2ndStepU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, afterLK2ndStepUIndex);
        int afterLK2ndStepVIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "afterLK2nd1_v");
        if (afterLK2ndStepVIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        afterLK2ndStepV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, afterLK2ndStepVIndex);
        //Read final Liu-Shen validation data (after second pyramidal step - final step)
        int afterLS2ndStepUIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "afterLS2nd1_u");
        if (afterLS2ndStepUIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        afterLS2ndStepU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, afterLS2ndStepUIndex);
        int afterLS2ndStepVIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(testDataFilename, "afterLS2nd1_v");
        if (afterLS2ndStepVIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        afterLS2ndStepV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(testDataFilename, afterLS2ndStepVIndex);
        
        //Normalize Liu-Shen images
        float maxA = 0.0f;
        float maxB = 0.0f;
        for (int i = 0; i < imageValidLSA.length; i++) {
            for (int j = 0; j < imageValidLSA[0].length; j++) {
                if (imageValidLSA[i][j] > maxA) {
                    maxA = imageValidLSA[i][j];
                }
                if (imageValidLSB[i][j] > maxB) {
                    maxB = imageValidLSB[i][j];
                }
            }
        }
        for (int i = 0; i < imageValidLSA.length; i++) {
            for (int j = 0; j < imageValidLSA[0].length; j++) {
                imageValidLSA[i][j] /= maxA;
                imageValidLSB[i][j] /= maxB;
            }
        }
    }
    
    @Before
    public void setup() throws IOException, InvalidNameException {
        readTestData();
    }
    
    @Test 
    public void denseLiuShenGpuTest1IterationPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        IImage imgLK1;
        IImage imgLK2;
        IImage imgLS1 = null;
        IImage imgLS2 = null;
        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);
        imgLS1 = ImageFloat.convertFrom(img1);
        imgLS2 = ImageFloat.convertFrom(img2);
        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        imgLS1 = filter2.applyFilter(imgLS1, imgLS1);
        imgLS2 = filter2.applyFilter(imgLS2, imgLS2);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);

        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];
        
        DenseLiuShenAparapiJob job = new DenseLiuShenAparapiJob(gpuDevice);
        LiuShenOptions options = new LiuShenOptions();
        options.iterationsLK = 5;
        options.windowSizeLK = 27;
        options.lambdaLS = 1000.0f;
        options.iterationsLS = 1;
        options.imageLSA = imgLS1;
        options.imageLSB = imgLS2;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        float[][] usVal = new float[img1.getHeight()][img1.getWidth()];
        float[][] vsVal = new float[img1.getHeight()][img1.getWidth()];
        
        computeValidationUsAndVs(img1, img2, usVal, vsVal, 1);
        
        validateMatrices("Liu-Shen us", usVal, us, 0, 0, img1.getHeight(), img1.getWidth(), 1e-2f);
        validateMatrices("Liu-Shen vs", vsVal, vs, 0, 0, img1.getHeight(), img1.getWidth(), 1e-2f);
    }

    
    @Test 
    public void denseLiuShenGpuTest2IterationPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        IImage imgLK1;
        IImage imgLK2;
        IImage imgLS1 = null;
        IImage imgLS2 = null;
        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);
        imgLS1 = ImageFloat.convertFrom(img1);
        imgLS2 = ImageFloat.convertFrom(img2);
        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        imgLS1 = filter2.applyFilter(imgLS1, imgLS1);
        imgLS2 = filter2.applyFilter(imgLS2, imgLS2);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);

        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];
        
        DenseLiuShenAparapiJob job = new DenseLiuShenAparapiJob(gpuDevice);
        LiuShenOptions options = new LiuShenOptions();
        options.iterationsLK = 5;
        options.windowSizeLK = 27;
        options.lambdaLS = 1000.0f;
        options.iterationsLS = 2;
        options.imageLSA = imgLS1;
        options.imageLSB = imgLS2;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        float[][] usVal = new float[img1.getHeight()][img1.getWidth()];
        float[][] vsVal = new float[img1.getHeight()][img1.getWidth()];
        
        computeValidationUsAndVs(img1, img2, usVal, vsVal, 2);
        
        validateMatrices("Liu-Shen us", usVal, us, 0, 0, img1.getHeight(), img1.getWidth(), 1e-2f);
        validateMatrices("Liu-Shen vs", vsVal, vs, 0, 0, img1.getHeight(), img1.getWidth(), 1e-2f);
    }

    @Test 
    public void denseLiuShenGpuTest60IterationPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        IImage imgLK1;
        IImage imgLK2;
        IImage imgLS1 = null;
        IImage imgLS2 = null;
        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);
        imgLS1 = ImageFloat.convertFrom(img1);
        imgLS2 = ImageFloat.convertFrom(img2);
        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        imgLS1 = filter2.applyFilter(imgLS1, imgLS1);
        imgLS2 = filter2.applyFilter(imgLS2, imgLS2);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);

        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];
        
        DenseLiuShenAparapiJob job = new DenseLiuShenAparapiJob(gpuDevice);
        LiuShenOptions options = new LiuShenOptions();
        options.iterationsLK = 5;
        options.windowSizeLK = 27;
        options.lambdaLS = 1000.0f;
        options.iterationsLS = 60;
        options.imageLSA = imgLS1;
        options.imageLSB = imgLS2;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        float[][] usVal = new float[img1.getHeight()][img1.getWidth()];
        float[][] vsVal = new float[img1.getHeight()][img1.getWidth()];
        
        computeValidationUsAndVs(img1, img2, usVal, vsVal, 60);
        
        validateMatrices("Liu-Shen us", usVal, us, 0, 0, img1.getHeight(), img1.getWidth(), 1e-3f);
        validateMatrices("Liu-Shen vs", vsVal, vs, 0, 0, img1.getHeight(), img1.getWidth(), 1e-3f);
    }

    /**
     * The purpose of this test is to validate the Java side Liu-Shen implementation, as it will be used to validate
     * the Aparapi/GPU Liu-Shen implementation. The sanity of the Java side Liu-Shen implementation is checked against
     * the Python based implementations.
     */
    @Test
    public void denseLiuShenBasicJavaTest() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        

        float[] imageLS1 = new float[imageValidLSA.length * imageValidLSA[0].length];
        float[] imageLS2 = new float[imageValidLSA.length * imageValidLSA[0].length];
        
        float[] us = new float[imageValidLSA.length * imageValidLSA[0].length];
        float[] vs = new float[imageValidLSA.length * imageValidLSA[0].length];
      
        for (int i = 0; i < beforeLK2ndStepU.length; i++) {
            for (int j = 0; j < beforeLK2ndStepU[0].length; j++) {
                int idx = i * imageValidLSA[0].length + j;
                imageLS1[idx] = imageValidLSA[i][j];
                imageLS2[idx] = imageValidLSB[i][j];
                us[idx] = afterLK2ndStepU[i][j];
                vs[idx] = afterLK2ndStepV[i][j];
            }
        }

        PIVContextTestsSingleton.setSingleton();
        LiuShenInterpolatorConfiguration lsConfig = new LiuShenInterpolatorConfiguration();
        lsConfig.setFilterSigmaLK(2.0f);
        lsConfig.setFilterWidthPxLK(3);
        lsConfig.setNumberOfIterationsLK(5);
        lsConfig.setWindowSizeLK(27);
        lsConfig.setFilterSigmaLS(0.48f);
        lsConfig.setFilterWidthPxLS(5);
        lsConfig.setMultiplierLagrangeLS(1000.0f);
        lsConfig.setNumberOfIterationsLS(60);
        lsConfig.setVectorsWindowSizeLS(3);
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsConfig);
        
        LiuShenFloat liuShenValidation = new LiuShenFloat();
        liuShenValidation.updateImageA(img1);
        liuShenValidation.updateImageB(img2);
        liuShenValidation.computeFromVelocities(us, vs);        

        //Here the Liu-Shen implementation computes the values based on the Python/OpenCL Lucas-Kanade implementation, and as such the margins effects are smaller,
        //and due to the difference in implementation between the Python Liu-Shen and the Java Liu-Shen, where the Java Liu-Shen handles the image margins differently
        //from what is proposed in the reference algorithm implementation.
        validateMatrices("Liu-Shen us", afterLS2ndStepU, us, 7, 7, img1.getHeight()-7, img1.getWidth()-7, 2e-2f);
        validateMatrices("Liu-Shen vs", afterLS2ndStepV, vs, 7, 7, img1.getHeight()-7, img1.getWidth()-7, 2e-2f);
    }
    
    /**
     * The purpose of this test is to validate the sanity of the algorithms used for the validation of Aparapi/GPU Liu-Shen implementation,
     * by validating against the Python implementations.
     */
    @Test
    public void denseLiuShenJavaTest() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");        
        IImage imgLK1;
        IImage imgLK2;
        IImage imgLS1T = null;
        IImage imgLS2T = null;
        IImage imgLS1 = null;
        IImage imgLS2 = null;
                
        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);

        imgLS1T = ImageFloat.convertFrom(img1);
        imgLS2T = ImageFloat.convertFrom(img2);
        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        imgLS1T = filter2.applyFilter(imgLS1T, imgLS1T);
        imgLS2T = filter2.applyFilter(imgLS2T, imgLS2T);
        imgLS1 = ImageFloat.copyAndNormalize(imgLS1T, (ImageFloat)imgLS1);
        imgLS2 = ImageFloat.copyAndNormalize(imgLS2T, (ImageFloat)imgLS2);
        float[] imgLS1Arr = imgLS1.exportTo1DFloatArray(null);
        float[] imgLS2Arr = imgLS2.exportTo1DFloatArray(null);
        
        //Validate filtered image
        validateMatrices("imageLSA", imageValidLSA, imgLS1Arr, 0, 0, img1.getHeight(), img1.getWidth(), 1e-6f);
        validateMatrices("imageLSB", imageValidLSB, imgLS2Arr, 0, 0, img1.getHeight(), img1.getWidth(), 1e-6f);        
        
        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];
      
        for (int i = 0; i < beforeLK2ndStepU.length; i++) {
            for (int j = 0; j < beforeLK2ndStepU[0].length; j++) {
                int idx = i * img1.getWidth() + j;
                us[idx] = beforeLK2ndStepU[i][j];
                vs[idx] = beforeLK2ndStepV[i][j];
            }
        }
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 5;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        
        //Validate Lucas-Kanade, while removing the boundary effects, since the handling of the OpenCL sampler used in the Python code has a different
        //behavior near the image borders. 
        validateMatrices("Lucas-Kanade us", afterLK2ndStepU, us, 13, 13, img1.getHeight()-13, img1.getWidth()-13, 2.5e-2f);
        validateMatrices("Lucas-Kanade vs", afterLK2ndStepV, vs, 13, 13, img1.getHeight()-13, img1.getWidth()-13, 2.5e-2f);        
        
        PIVContextTestsSingleton.setSingleton();
        LiuShenInterpolatorConfiguration lsConfig = new LiuShenInterpolatorConfiguration();
        lsConfig.setFilterSigmaLK(2.0f);
        lsConfig.setFilterWidthPxLK(3);
        lsConfig.setNumberOfIterationsLK(5);
        lsConfig.setWindowSizeLK(27);
        lsConfig.setFilterSigmaLS(0.48f);
        lsConfig.setFilterWidthPxLS(5);
        lsConfig.setMultiplierLagrangeLS(1000.0f);
        lsConfig.setNumberOfIterationsLS(60);
        lsConfig.setVectorsWindowSizeLS(3);
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsConfig);
        
        LiuShenFloat liuShenValidation = new LiuShenFloat();
        liuShenValidation.updateImageA(img1);
        liuShenValidation.updateImageB(img2);
        liuShenValidation.computeFromVelocities(us, vs);
        
        validateMatrices("Liu-Shen us", afterLS2ndStepU, us, 13, 13, img1.getHeight()-13, img1.getWidth()-13, 1.4e-2f);
        validateMatrices("Liu-Shen vs", afterLS2ndStepV, vs, 13, 13, img1.getHeight()-13, img1.getWidth()-13, 1.4e-2f);
    }

    void computeValidationUsAndVs(IImage img1, IImage img2, float[][] usInOut, float[][] vsInOut, int iterations) {
        IImage imgLK1;
        IImage imgLK2;

        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);

        float us[] = new float[usInOut.length * usInOut[0].length];
        float vs[] = new float[usInOut.length * usInOut[0].length];
        
        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                us[idx] = usInOut[i][j];
                vs[idx] = usInOut[i][j];
            }
        }
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 5;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = false;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        PIVContextTestsSingleton.setSingleton();
        LiuShenInterpolatorConfiguration lsConfig = new LiuShenInterpolatorConfiguration();
        lsConfig.setFilterSigmaLK(2.0f);
        lsConfig.setFilterWidthPxLK(3);
        lsConfig.setNumberOfIterationsLK(5);
        lsConfig.setWindowSizeLK(27);
        lsConfig.setFilterSigmaLS(0.48f);
        lsConfig.setFilterWidthPxLS(5);
        lsConfig.setMultiplierLagrangeLS(1000.0f);
        lsConfig.setNumberOfIterationsLS(iterations);
        lsConfig.setVectorsWindowSizeLS(3);
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsConfig);
        
        LiuShenFloat liuShenValidation = new LiuShenFloat();
        liuShenValidation.updateImageA(img1);
        liuShenValidation.updateImageB(img2);
        liuShenValidation.computeFromVelocities(us, vs);

        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                usInOut[i][j] = us[idx];
                vsInOut[i][j] = vs[idx];
            }
        }
    }
    
    private void validateMatrices(String title, float[][] validMat, float[] checkMat, int startI, int startJ, int endI, int endJ, float tolerance) {
        for (int i = startI; i < endI; i++) {
            for (int j = startJ; j < endJ; j++) {
                int idx = i * validMat[0].length + j;
                assertEquals(title + " do not match at I: " + i + ", J: " + j, validMat[i][j], checkMat[idx], tolerance);
            }
        }
    }
    
}
