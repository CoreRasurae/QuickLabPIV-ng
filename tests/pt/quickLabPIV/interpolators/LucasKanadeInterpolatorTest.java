package pt.quickLabPIV.interpolators;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.LucasKanadeFloat;
import pt.quickLabPIV.interpolators.LucasKanadeInterpolatorConfiguration;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class LucasKanadeInterpolatorTest {
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
    }
    
    @Test
    public void validateAgainstDenseLKPythonTestPass() {
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
                                              
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");                                      
        
        
        ICrossCorrelationInterpolator interp = new LucasKanadeFloat();

        interp.updateImageA(img1);
        interp.updateImageB(img2);
        
        List<MaxCrossResult> maxResults = new ArrayList<>(16);
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                MaxCrossResult maxResult = new MaxCrossResult();
                maxResult.tileA = stepTilesA.getTile(i, j);
                maxResult.tileB = stepTilesB.getTile(i, j);
                maxResult.setMainPeakI(maxIs[i][j]);
                maxResult.setMainPeakJ(maxJs[i][j]);
                maxResult.setCrossDims(255, 255);
                
                List<MaxCrossResult> results = new ArrayList<>();
                results.add(maxResult);
                
                interp.interpolate(results);
                maxResults.add(maxResult);
            }
        }
        
        for (MaxCrossResult result : maxResults) {
            int i = result.tileA.getTileIndexI();
            int j = result.tileA.getTileIndexJ();
            float expectedU = 0.0f;
            float expectedV = 0.0f;
            for (int idx = 0; idx < 4; idx++) {
               expectedU += validationDataU[idx][i][j];
               expectedV += validationDataV[idx][i][j];               
            }
            expectedU /= 4.0f;
            expectedV /= 4.0f;
            
            float validationMaxI = expectedV + (stepTilesB.getTileHeight() - 1);
            float validationMaxJ = expectedU + (stepTilesB.getTileWidth() - 1);

            assertEquals("Top-left Tile I position does not match the expected at I:" + i + ", J:" + j, iPixel[i][j], result.tileA.getTopPixel() + stepTilesA.getTileHeight() / 2 - 1);
            assertEquals("Top-left Tile J position does not match the expected at I:" + i + ", J:" + j, jPixel[i][j], result.tileA.getLeftPixel() + stepTilesA.getTileWidth() / 2 - 1);
            
            assertEquals("MaxI does not match at I:" + i + ", J:" + j, validationMaxI, result.getMainPeakI(), 1e-2);
            assertEquals("MaxJ does not match at I:" + i + ", J:" + j, validationMaxJ, result.getMainPeakJ(), 1e-2);
        }
    }

    @Test
    public void validateTestImplAgainstDenseLKPythonTestPass() {
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
                                              
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");                                      
        
        
        ICrossCorrelationInterpolator interp = new SimpleLucasKanadeImpl(2.0f, 3, true, 27, 5);

        interp.updateImageA(img1);
        interp.updateImageB(img2);
        
        List<MaxCrossResult> maxResults = new ArrayList<>(16);
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                MaxCrossResult maxResult = new MaxCrossResult();
                maxResult.setCrossDims(255, 255);
                maxResult.tileA = stepTilesA.getTile(i, j);
                maxResult.tileB = stepTilesB.getTile(i, j);
                maxResult.setMainPeakI(maxIs[i][j]);
                maxResult.setMainPeakJ(maxJs[i][j]);

                List<MaxCrossResult> results = new ArrayList<>();
                results.add(maxResult);

                interp.interpolate(results);
                maxResults.add(maxResult);
            }
        }
        
        for (MaxCrossResult result : maxResults) {
            int i = result.tileA.getTileIndexI();
            int j = result.tileA.getTileIndexJ();
            float expectedU = 0.0f;
            float expectedV = 0.0f;
            for (int idx = 0; idx < 4; idx++) {
               expectedU += validationDataU[idx][i][j];
               expectedV += validationDataV[idx][i][j];               
            }
            expectedU /= 4.0f;
            expectedV /= 4.0f;
            
            float validationMaxI = expectedV + (stepTilesB.getTileHeight() - 1);
            float validationMaxJ = expectedU + (stepTilesB.getTileWidth() - 1);

            assertEquals("Top-left Tile I position does not match the expected at I:" + i + ", J:" + j, iPixel[i][j], result.tileA.getTopPixel() + stepTilesA.getTileHeight() / 2 - 1);
            assertEquals("Top-left Tile J position does not match the expected at I:" + i + ", J:" + j, jPixel[i][j], result.tileA.getLeftPixel() + stepTilesA.getTileWidth() / 2 - 1);
            
            assertEquals("MaxI does not match at I:" + i + ", J:" + j, validationMaxI, result.getMainPeakI(), 1e-2);
            assertEquals("MaxJ does not match at I:" + i + ", J:" + j, validationMaxJ, result.getMainPeakJ(), 1e-2);
        }
    }    

    @Test
    public void validateOPtimizedImplementationAgainstTestImplementationTestPass() {
        PIVContextTestsSingleton.setSingleton();
        LucasKanadeInterpolatorConfiguration lkConfig = new LucasKanadeInterpolatorConfiguration();
        lkConfig.setAverageOfFourPixels(true);
        lkConfig.setFilterSigma(2.0f);
        lkConfig.setFilterWidthPx(3);
        lkConfig.setNumberOfIterations(1);
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

        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
                                              
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");                                      
        
        
        ICrossCorrelationInterpolator interp1 = new LucasKanadeFloat();
        SimpleLucasKanadeImpl interp2 = new SimpleLucasKanadeImpl();

        boolean avgOf4 = true;
        int windowSize = 27;
        
        int size = avgOf4 ? 4 : 1;
        double[][][] imgPatchA = new double[size][windowSize][windowSize];
        double[][][] imgPatchB = new double[size][windowSize][windowSize];
        double[][][] dI = new double[size][windowSize][windowSize];
        double[][][] dJ = new double[size][windowSize][windowSize];
        double[][][] b0 = new double[size][windowSize][windowSize];
        double[][][] b1 = new double[size][windowSize][windowSize];
        double[][] A = new double[size][3];
        double[] detA = new double[size];
        double[][] invA = new double[size][3];
        
        ((LucasKanadeFloat)interp1).registerListener(new LucasKanadeFloat.ILucasKanadeListener() {
            @Override
            public void readingImageBPixel(float pixelValue, float dT, float b0Inc, float b1Inc, int i, int j, float locI,
                    float locJ, int patchIndex) {
                imgPatchB[patchIndex][i][j] = pixelValue;
                b0[patchIndex][i][j] = b0Inc;
                b1[patchIndex][i][j] = b1Inc;
            }
            
            @Override
            public void computedPostInversion(float[] _detA, float[][] _A) {
                for (int idx = 0; idx < (avgOf4 ? 4 : 1); idx++) {
                    detA[idx] = _detA[idx];
                    invA[idx][0] = _A[idx][0];
                    invA[idx][1] = _A[idx][1];
                    invA[idx][2] = _A[idx][2];
                }
            }
            
            @Override
            public void computedDerivativesPreInversion(float[] _imgPatchA, float[] _dI, float[] _dJ, float[][] _A) {
                int margin = 0;
                if (avgOf4) {
                    margin = 1;
                }

                for (int idx = 0; idx < (avgOf4 ? 4 : 1); idx++) {
                    A[idx][0] = _A[idx][0];
                    A[idx][1] = _A[idx][1];
                    A[idx][2] = _A[idx][2];
                }
                for (int i = 0; i < windowSize; i++) {
                    for (int j = 0; j < windowSize; j++) {
                        imgPatchA[0][i][j] = _imgPatchA[i * (windowSize + margin) + j];
                        dI[0][i][j] = _dI[i * (windowSize + margin) + j];
                        dJ[0][i][j] = _dJ[i * (windowSize + margin) + j];
                        if (avgOf4) {
                            imgPatchA[1][i][j] = _imgPatchA[i * (windowSize + margin) + (j + 1)];
                            dI[1][i][j] = _dI[i * (windowSize + margin) + (j + 1)];
                            dJ[1][i][j] = _dJ[i * (windowSize + margin) + (j + 1)];
                            imgPatchA[2][i][j] = _imgPatchA[(i+1) * (windowSize + margin) + j];
                            dI[2][i][j] = _dI[(i + 1) * (windowSize + margin) + j];
                            dJ[2][i][j] = _dJ[(i + 1) * (windowSize + margin) + j];
                            imgPatchA[3][i][j] = _imgPatchA[(i+1) * (windowSize + margin) + (j+1)];
                            dI[3][i][j] = _dI[(i + 1) * (windowSize + margin) + (j + 1)];
                            dJ[3][i][j] = _dJ[(i + 1) * (windowSize + margin) + (j + 1)];
                        }
                    }
                }
            }
            
            @Override
            public void computedBs(float b0, float b1, float incU, float incV, int iter, int patchIndex) {
                
            }
        });
        
        interp1.updateImageA(img1);
        interp1.updateImageB(img2);
        interp2.updateImageA(img1);
        interp2.updateImageB(img2);
        
        List<MaxCrossResult> maxResults1 = new ArrayList<>(16);
        List<MaxCrossResult> maxResults2 = new ArrayList<>(16);
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                MaxCrossResult maxResult1 = new MaxCrossResult();
                maxResult1.tileA = stepTilesA.getTile(i, j);
                maxResult1.tileB = stepTilesB.getTile(i, j);
                maxResult1.setMainPeakI(maxIs[i][j]);
                maxResult1.setMainPeakJ(maxJs[i][j]);
                maxResult1.setCrossDims(255, 255);

                MaxCrossResult maxResult2 = new MaxCrossResult();
                maxResult2.tileA = stepTilesA.getTile(i, j);
                maxResult2.tileB = stepTilesB.getTile(i, j);
                maxResult2.setMainPeakI(maxIs[i][j]);
                maxResult2.setMainPeakJ(maxJs[i][j]);
                maxResult2.setCrossDims(255, 255);
                
                List<MaxCrossResult> results1 = new ArrayList<>();
                results1.add(maxResult1);

                List<MaxCrossResult> results2 = new ArrayList<>();
                results2.add(maxResult2);

                
                interp1.interpolate(results1);
                interp2.interpolate(results2);

                //Check all matrices directly derived from first image
                comparePatches(interp2.getImgPatchAResult(), imgPatchA);
                compareA(interp2.getA(), A);
                compareDetA(interp2.getDetA(), detA);
                compareA(interp2.getInvA(), invA);
                comparePatches(interp2.getDIs(), dI);
                comparePatches(interp2.getDJs(), dJ);
                
                //Check all matrices derived from the second image
                comparePatches(interp2.getImgPatchBResult(), imgPatchB);
                
                maxResults1.add(maxResult1);
                maxResults2.add(maxResult2);
            }
        }
        
        for (int idx = 0; idx < maxResults1.size(); idx++) {
            MaxCrossResult result1 = maxResults1.get(idx);
            MaxCrossResult result2 = maxResults2.get(idx);
            
            int i = result1.tileA.getTileIndexI();
            int j = result1.tileA.getTileIndexJ();
            
            assertEquals("MaxI does not match at I:" + i + ", J:" + j, result1.getMainPeakI(), result2.getMainPeakI(), 1e0);
            assertEquals("MaxJ does not match at I:" + i + ", J:" + j, result1.getMainPeakJ(), result2.getMainPeakJ(), 1e0);
        }
    }

    private void compareDetA(double[] refDetA, double[] testDetA) {
        assertEquals("A matrix multiplicity does not match", refDetA.length, testDetA.length);
        for (int idx = 0; idx < refDetA.length; idx++) {
            assertEquals("Determinant of A does not match at Idx: " + idx, refDetA[idx], testDetA[idx], 1e4);
        }   
    }

    private void compareA(double[][] refA, double[][] testA) {
        assertEquals("A matrix multiplicity does not match", refA.length, testA.length);
        for (int idx = 0; idx < refA.length; idx++) {
            assertEquals("Matrix entry A00 does not match for Idx: " + idx, refA[idx][0], testA[idx][0], 1e-1);
            assertEquals("Matrix entry A01 does not match for Idx: " + idx, refA[idx][1], testA[idx][1], 1e-2);
            assertEquals("Matrix entry A11 does not match for Idx: " + idx, refA[idx][2], testA[idx][2], 1e-1);
        }
        
    }

    private void comparePatches(double[][][] refPatch, double[][][] testPatch) {
        assertEquals("Patch multiplicity does not match", refPatch.length, testPatch.length);
        assertEquals("Patch dimensions do not agree for dimI", refPatch[0].length, testPatch[0].length);
        assertEquals("Patch dimensions do not agree for dimJ", refPatch[0][0].length, testPatch[0][0].length);
        
        for (int idx = 0; idx < refPatch.length; idx++) {
            for (int i = 0; i < refPatch[idx].length; i++) {
                for (int j = 0; j < refPatch[idx][idx].length; j++) {
                    assertEquals("Image patches do not match for Idx: " + idx + ", I: " + i + ", J: " + j, refPatch[idx][i][j], testPatch[idx][i][j], 1e-5);
                }
            }
        }
    }
}

