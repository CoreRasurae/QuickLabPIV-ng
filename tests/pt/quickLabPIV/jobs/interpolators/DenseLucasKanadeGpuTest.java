package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.DenseLucasKanadeAparapiJobInterpolator;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.LucasKanadeInterpolatorConfiguration;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class DenseLucasKanadeGpuTest {
    private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

    //Tiles center top-left pixel location
    int iPixel[][] = new int[][] {{ 63,  63,  63,  63},
                                  {191, 191, 191, 191},
                                  {319, 319, 319, 319},
                                  {447, 447, 447, 447}};
                             
    int jPixel[][] = new int[][] {{ 63, 191, 319, 447},
                                  { 63, 191, 319, 447},
                                  { 63, 191, 319, 447},
                                  { 63, 191, 319, 447}};
                                                      
    float absoluteCenterU[][] = new float[][] {{ 64, 193, 321, 448},
                                               { 64, 193, 321, 448},
                                               { 62, 189, 317, 446},
                                               { 62, 189, 317, 448}};
    
    float absoluteCenterV[][] = new float[][] {{ 62,  62,  64,  63},
                                               {189, 189, 193, 193},
                                               {317, 317, 321, 321},
                                               {446, 446, 448, 448}};
                                         
    float maxIs[][] = new float[][] {{126, 126, 128, 128},
                                     {125, 125, 129, 129},
                                     {125, 125, 129, 129},
                                     {126, 126, 128, 128}};
    
    float maxJs[][] = new float[][] {{128, 129, 129, 128},
                                     {128, 129, 129, 128},
                                     {126, 125, 125, 126},
                                     {126, 125, 125, 126}};       
                                     
    float topLeftU[][] = {  { 1.0637721f, 1.8681719f, 1.8472635f, 1.0686594f },
                            { 0.6137566f, 2.4922092f, 2.3605154f, 0.6032251f },
                            { -0.627932f, -2.5686827f, -2.6700258f, -0.6378484f },
                            { -1.0528795f, -1.8588967f, -1.8712513f, -1.038297f } };

    float topLeftV[][] = { { -1.0689481f, -0.61453104f, 0.6350748f, 1.0597013f },
                           { -1.8892959f, -2.42813f, 2.6533148f, 1.8658934f },
                           { -1.8622798f, -2.5096738f, 2.5215335f, 1.8495257f },
                           { -1.033813f, -0.5907859f, 0.6209221f, 1.0422188f } };

    float topRightU[][] = { { 1.0726591f, 1.8699218f, 1.841456f, 1.0640063f },
                            { 0.6181561f, 2.498034f, 2.3766506f, 0.60506225f },
                            { -0.62758297f, -2.571669f, -2.658413f, -0.6289585f },
                            { -1.0552263f, -1.8682343f, -1.8608178f, -1.0323583f } };

    float topRightV[][] = { { -1.0696652f, -0.6138992f, 0.64987487f, 1.0632641f },
                            { -1.9104767f, -2.3960445f, 2.7035704f, 1.8647307f },
                            { -1.8667985f, -2.4998767f, 2.5622158f, 1.8443561f },
                            { -1.030764f, -0.5895036f, 0.6240096f, 1.044444f } };

    float bottomLeftU[][] = { { 1.065608f, 1.8725015f, 1.8541833f, 1.0693189f },
                              { 0.6071053f, 2.4646065f, 2.3535554f, 0.5956265f },
                              { -0.6301277f, -2.6040695f, -2.6901097f, -0.64756095f },
                              { -1.0537819f, -1.8447999f, -1.8697939f, -1.037656f } };

    float bottomLeftV[][] = { { -1.0804887f, -0.62381816f, 0.6474271f, 1.0621992f },
                              { -1.896659f, -2.4295337f, 2.6558723f, 1.8648204f },
                              { -1.8615748f, -2.5264385f, 2.512388f, 1.8411692f },
                              { -1.0279329f, -0.5816744f, 0.6233693f, 1.0374491f } };
                
    float bottomRightU[][] = { { 1.0746177f, 1.8743447f, 1.8492279f, 1.0649034f },
                               { 0.6104621f, 2.4704108f, 2.3702216f, 0.5973356f },
                               { -0.62983054f, -2.6065388f, -2.6801333f, -0.64062476f },
                               { -1.0558376f, -1.851503f, -1.8598498f, -1.031867f } };

    float bottomRightV[][] = { { -1.0813998f, -0.6233064f, 0.66052973f, 1.0658274f },
                               { -1.9175066f, -2.3983943f, 2.705872f, 1.8629825f },
                               { -1.8660951f, -2.5165122f, 2.550467f, 1.8372253f },
                               { -1.0252105f, -0.5791429f, 0.6268926f, 1.0397555f } };

    float validationDataU[][][] = new float[4][4][4];
    float validationDataV[][][] = new float[4][4][4];

    
    @Before
    public void setup() {
        PIVContextTestsSingleton.setSingleton();

        validationDataU[0] = topLeftU;
        validationDataV[0] = topLeftV;
        validationDataU[1] = topRightU;
        validationDataV[1] = topRightV;
        validationDataU[2] = bottomLeftU;
        validationDataV[2] = bottomLeftV;
        validationDataU[3] = bottomRightU;
        validationDataV[3] = bottomRightV;
        
        LucasKanadeInterpolatorConfiguration lkConfig = new LucasKanadeInterpolatorConfiguration();
        lkConfig.setAverageOfFourPixels(true);
        lkConfig.setFilterSigma(2.0f);
        lkConfig.setFilterWidthPx(3);
        lkConfig.setNumberOfIterations(5);
        lkConfig.setWindowSize(27);
        
        PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
        parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
        parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
        parameters.setImageHeightPixels(512);
        parameters.setImageWidthPixels(512);
        parameters.setMarginPixelsITop(0);
        parameters.setMarginPixelsIBottom(0);
        parameters.setMarginPixelsJLeft(0);
        parameters.setMarginPixelsJRight(0);
        parameters.setInterrogationAreaStartIPixels(128);
        parameters.setInterrogationAreaEndIPixels(128);
        parameters.setInterrogationAreaStartJPixels(128);
        parameters.setInterrogationAreaEndJPixels(128);
        parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
        parameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER, lkConfig);
        parameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER_APARAPI, lkConfig);
        
        PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
        runParameters.setUseOpenCL(true);
        runParameters.setTotalNumberOfThreads(1);
        DeviceRuntimeConfiguration config = new DeviceRuntimeConfiguration();
        config.setCpuThreadAssignments(new int[] {0});
        config.setDevice(gpuDevice);
        config.setScore(1.0f);
        runParameters.putDeviceConfiguration(config);
        runParameters.clearThreadMappings();
        runParameters.mapThreadToThreadIndex(0);
    }

    @Test 
    public void denseLucasKanadeTest() {
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
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 1;
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
        System.out.println(jobResult.us.length);
    }

    @Test 
    public void denseLucasKanadeAgainstPythonLKTest() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
        
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        ICrossCorrelationInterpolator interp = new DenseLucasKanadeAparapiJobInterpolator();
        interp.updateImageA(img1);
        interp.updateImageB(img2);

        float[] us = new float[img1.getWidth() * img1.getHeight()];
        float[] vs = new float[img1.getWidth() * img1.getHeight()];

        List<MaxCrossResult> maxResults = new ArrayList<>(16);
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                MaxCrossResult maxResult = new MaxCrossResult();
                maxResult.tileA = stepTilesA.getTile(i, j);
                maxResult.tileB = stepTilesB.getTile(i, j);
                maxResult.setMainPeakI(maxIs[i][j]);
                maxResult.setMainPeakJ(maxJs[i][j]);
                maxResult.setCrossDims(255, 255);
                maxResults.add(maxResult);
            }
        }

        maxResults = interp.interpolate(maxResults);
    
        for (MaxCrossResult result : maxResults) {
            int i = result.tileA.getTileIndexI();
            int j = result.tileA.getTileIndexJ();
            float expectedV = 0.0f;
            float expectedU = 0.0f;
            for (int idx = 0; idx < 4; idx++) {
               expectedV += validationDataU[idx][i][j];
               expectedU += validationDataV[idx][i][j];               
            }
            expectedV /= 4.0f;
            expectedU /= 4.0f;
            
            assertEquals("Top-left Tile I position does not match the expected at I:" + i + ", J:" + j, iPixel[i][j], result.tileA.getTopPixel() + stepTilesA.getTileHeight() / 2 - 1);
            assertEquals("Top-left Tile J position does not match the expected at I:" + i + ", J:" + j, jPixel[i][j], result.tileA.getLeftPixel() + stepTilesA.getTileWidth() / 2 - 1);
            
            assertEquals("ExpectedU does not match at I:" + i + ", J:" + j, expectedU, result.getNthDisplacementU(0), 1e-2);
            assertEquals("ExpectedV does not match at I:" + i + ", J:" + j, expectedV, result.getNthDisplacementV(0), 1e-2);
        }
    }

}
