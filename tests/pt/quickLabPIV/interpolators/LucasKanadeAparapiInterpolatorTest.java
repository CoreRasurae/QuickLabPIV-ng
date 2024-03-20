// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.interpolators.DenseLucasKanadeAparapiJobInterpolator;
import pt.quickLabPIV.interpolators.LucasKanadeFloat;
import pt.quickLabPIV.interpolators.LucasKanadeInterpolatorConfiguration;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class LucasKanadeAparapiInterpolatorTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    
    private String filename;
    private float pivHongweiGuoU[][];
    private float pivHongweiGuoV[][];
    private float validationLkU[][];
    private float validationLkV[][];
    private IImage imageA;
    private IImage imageB;

    private void setConfig(int iterations) {
        PIVContextTestsSingleton.setSingleton();
        
        LucasKanadeInterpolatorConfiguration lkConfig = new LucasKanadeInterpolatorConfiguration();
        lkConfig.setAverageOfFourPixels(false);
        lkConfig.setFilterSigma(2.0f);
        lkConfig.setFilterWidthPx(3);
        lkConfig.setNumberOfIterations(iterations);
        lkConfig.setWindowSize(27);
        
        PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
        parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
        parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
        parameters.setImageHeightPixels(imageA.getHeight());
        parameters.setImageWidthPixels(imageA.getWidth());
        parameters.setMarginPixelsITop(0);
        parameters.setMarginPixelsIBottom(0);
        parameters.setMarginPixelsJLeft(0);
        parameters.setMarginPixelsJRight(0);
        parameters.setInterrogationAreaStartIPixels(16);
        parameters.setInterrogationAreaEndIPixels(16);
        parameters.setInterrogationAreaStartJPixels(16);
        parameters.setInterrogationAreaEndJPixels(16);
        parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.LucasKanadeAparapi);
        parameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER_APARAPI, lkConfig);
        parameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER, lkConfig);
        
        PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
        runParameters.setTotalNumberOfThreads(1);
        runParameters.setUseOpenCL(true);
        DeviceRuntimeConfiguration config = new DeviceRuntimeConfiguration();
        config.setCpuThreadAssignments(new int[] {0});
        config.setDevice(gpuDevice);
        config.setScore(1.0f);
        runParameters.putDeviceConfiguration(config);
        runParameters.mapThreadToThreadIndex(0);
    }
    
    @Before
    public void setup() throws IOException {
        filename = "testFiles" + File.separator + "lucasKanadeInterpolatorTest.matFloat";
        int uIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "hongwei16x16U");
        int vIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "hongwei16x16V");
        int validUIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "validation5Iter16x16U");
        int validVIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "validation5Iter16x16V");
        
        //Here U and V are in PIV order, where U is longitudinal, not in OpF order, where U is lateral.
        pivHongweiGuoU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndex);
        pivHongweiGuoV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndex);
        
        //Here U and V are in PIV order, where U is longitudinal, not in OpF order, where U is lateral.
        validationLkU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, validUIndex);
        validationLkV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, validVIndex);
        
        imageA = ImageTestHelper.getImage("testFiles" + File.separator + "11-16-47.000-00001.tif");
        imageB = ImageTestHelper.getImage("testFiles" + File.separator + "11-16-47.000-00002.tif");        
        
        setConfig(5);
    }
    
    /**
     * This test is based on real QuickLab-PIV output, based on the PIVDatabase from Trial2B-Pump12.1p-500fpd-2.0ms-CSMount-50mm-M3-Comporta, using an
     * adaptive PIV from 128x128 down to 16x16, no superposition, with 2nd image micro-warping and Hongwei Guo interpolation.
     * The validation data is taken from the Java only LucasKanadeFloatInterpolator on top of the output from Hongwei Guo, thus employing combined
     * sub-pixel with all PIV steps employing Hongwei Guo with 5pixels and 20 iterations and a final Lucas-Kanade step at then end of PIV, with
     * a filter of str=2.0 and width=3px, using a windowSize=27, iters=5.
     */
    @Test
    public void testLucasKanadeAparapiInterpolatorAgainstLucasKanadeFloatExpectedPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                tileB.replaceDisplacement(pivHongweiGuoV[i][j], pivHongweiGuoU[i][j]);
            }
        }
        
        DenseLucasKanadeAparapiJobInterpolator interpolator = new DenseLucasKanadeAparapiJobInterpolator();
        interpolator.updateImageA(imageA);
        interpolator.updateImageB(imageB);
        
        List<MaxCrossResult> maxCrossResults = new ArrayList<MaxCrossResult>(stepTilesB.getNumberOfTilesInI() * stepTilesB.getNumberOfTilesInJ());
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                MaxCrossResult maxResult = new MaxCrossResult();
                maxResult.tileA = stepTilesA.getTile(i, j);
                maxResult.tileB = stepTilesB.getTile(i, j);
                maxResult.setCrossDims(31, 31);
                maxResult.setMainPeakI(15);
                maxResult.setMainPeakJ(15);
                maxCrossResults.add(maxResult);
            }
        }
        
        interpolator.interpolate(maxCrossResults);
        stepTilesB.updateDisplacementsFromMaxCrossResults(0, maxCrossResults);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                assertEquals("Velocity U does not match for tile I:" + i + ", J:" + j, validationLkU[i][j], tileB.getDisplacementV(), 1.3e-2f);
                assertEquals("Velocity V does not match for tile I:" + i + ", J:" + j, validationLkV[i][j], tileB.getDisplacementU(), 1.3e-2f);
            }
        }

    }

    @Test
    public void testLucasKanadeAparapiInterpolatorTilesAgainstLucasKanadeFloatExpectedPass() {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                tileB.replaceDisplacement(pivHongweiGuoV[i][j], pivHongweiGuoU[i][j]);
            }
        }
        
        DenseLucasKanadeAparapiJobInterpolator interpolator = new DenseLucasKanadeAparapiJobInterpolator();
        interpolator.updateImageA(imageA);
        interpolator.updateImageB(imageB);
                
        interpolator.interpolate(stepTilesA, stepTilesB);

        for (int i = 1; i < stepTilesB.getNumberOfTilesInI()-1; i++) {
            for (int j = 1; j < stepTilesB.getNumberOfTilesInJ()-1; j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                assertEquals("Velocity U does not match for tile I:" + i + ", J:" + j, validationLkU[i][j], tileB.getDisplacementV(), 3e-2f);
                assertEquals("Velocity V does not match for tile I:" + i + ", J:" + j, validationLkV[i][j], tileB.getDisplacementU(), 3e-2f);
            }
        }

    }

    @Test
    public void testLucasKanadeAparapiJobDirectAgainstLucasKanadeFloatExpected1IterationPass() throws IOException {
        setConfig(1);

        int validUIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "validation1Iter16x16U");
        int validVIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "validation1Iter16x16V");

        //Here U and V are in PIV order, where U is longitudinal, not in OpF order, where U is lateral.
        validationLkU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, validUIndex);
        validationLkV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, validVIndex);

        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
                        
        GaussianFilter2D filter = new GaussianFilter2D(2.0f, 3);
        imageA = ImageFloat.convertFrom(imageA);
        imageB = ImageFloat.convertFrom(imageB);
        imageA = filter.applyFilter(imageA, imageA);
        imageB = filter.applyFilter(imageB, imageB);
        
        float us[] = new float[imageA.getHeight() * imageA.getWidth()];
        float vs[] = new float[imageA.getHeight() * imageA.getWidth()];

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                int pixelI = (tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1);
                int pixelJ = (tileB.getLeftPixel() + stepTilesB.getTileWidth()/2 - 1);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                tileB.replaceDisplacement(pivHongweiGuoV[i][j], pivHongweiGuoU[i][j]);
                int idx = pixelI * imageA.getWidth() + pixelJ;
                
                us[idx] = pivHongweiGuoU[i][j];
                vs[idx] = pivHongweiGuoV[i][j];
            }
        }
        
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 1;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imageA;
        jobInput.imageB = imageB;
        jobInput.halfPixelOffset = true;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;

        DenseLucasKanadeAparapiJob dLKJob = new DenseLucasKanadeAparapiJob(gpuDevice);
        dLKJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        dLKJob.analyze();
        dLKJob.compute();
        OpticalFlowInterpolatorInput jobResult = dLKJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                int pixelI = (tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1);
                int pixelJ = (tileB.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1);
                int idx = pixelI * imageA.getWidth() + pixelJ;
                tileB.replaceDisplacement(vs[idx], us[idx]);
            }
        }
 
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                assertEquals("Velocity U does not match for tile I:" + i + ", J:" + j, validationLkU[i][j], tileB.getDisplacementV(), 1e-2f);
                assertEquals("Velocity V does not match for tile I:" + i + ", J:" + j, validationLkV[i][j], tileB.getDisplacementU(), 1e-2f);
            }
        }
    }

    @Test
    public void testLucasKanadeAparapiJobDirectAgainstLucasKanadeFloatExpected5IterationPass() throws IOException {
        assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
        
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
                        
        GaussianFilter2D filter = new GaussianFilter2D(2.0f, 3);
        imageA = ImageFloat.convertFrom(imageA);
        imageB = ImageFloat.convertFrom(imageB);
        imageA = filter.applyFilter(imageA, imageA);
        imageB = filter.applyFilter(imageB, imageB);
        
        float us[] = new float[imageA.getHeight() * imageA.getWidth()];
        float vs[] = new float[imageA.getHeight() * imageA.getWidth()];

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                int pixelI = (tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1);
                int pixelJ = (tileB.getLeftPixel() + stepTilesB.getTileWidth()/2 - 1);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                tileB.replaceDisplacement(pivHongweiGuoV[i][j], pivHongweiGuoU[i][j]);
                int idx = pixelI * imageA.getWidth() + pixelJ;
                
                us[idx] = pivHongweiGuoU[i][j];
                vs[idx] = pivHongweiGuoV[i][j];
            }
        }
        
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 5;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imageA;
        jobInput.imageB = imageB;
        jobInput.halfPixelOffset = true;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;

        DenseLucasKanadeAparapiJob dLKJob = new DenseLucasKanadeAparapiJob(gpuDevice);
        dLKJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        dLKJob.analyze();
        dLKJob.compute();
        OpticalFlowInterpolatorInput jobResult = dLKJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                int pixelI = (tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1);
                int pixelJ = (tileB.getLeftPixel() + stepTilesB.getTileWidth()/2 - 1);
                int idx = pixelI * imageA.getWidth() + pixelJ;
                tileB.replaceDisplacement(vs[idx], us[idx]);
            }
        }
 
        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                assertEquals("Velocity U does not match for tile I:" + i + ", J:" + j, validationLkU[i][j], tileB.getDisplacementV(), 1.3e-2f);
                assertEquals("Velocity V does not match for tile I:" + i + ", J:" + j, validationLkV[i][j], tileB.getDisplacementU(), 1.3e-2f);
            }
        }
    }
    
    @Test
    public void testLucasKanadeFloatInterpolatorAgainstOwnImportedDataPass() {
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        IterationStepTiles stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                tileB.replaceDisplacement(pivHongweiGuoV[i][j], pivHongweiGuoU[i][j]);
            }
        }
        
        LucasKanadeFloat interpolator = new LucasKanadeFloat();
        interpolator.updateImageA(imageA);
        interpolator.updateImageB(imageB);
        interpolator.interpolate(stepTilesA, stepTilesB);

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                Tile tileB = stepTilesB.getTile(i, j);
                //Intentionally swapped reference data, because QuickLabPIV exported matFloat data has U,V coordinates swapped with respect
                //internal QuickLabPIV Tile computations
                assertEquals("Velocity U does not match for tile I:" + i + ", J:" + j, validationLkU[i][j], tileB.getDisplacementV(), 1e-7f);
                assertEquals("Velocity V does not match for tile I:" + i + ", J:" + j, validationLkV[i][j], tileB.getDisplacementU(), 1e-7f);
            }
        }        
    }

    public static void dump1DArray(String name, int[] xr) {
        System.out.println(name);
        printArray(xr);
    }
    
    public static void dump1DArray(String name, float[] xr) {
        System.out.println(name);
        printArray(xr);
    }
    
    public static void dump2DArray(String name, int[][] xr) {
        System.out.println(name);
        for (int i = 0; i < xr.length; i++) {
            printArray(xr[i]);
        }
     }

    public static void dump2DArray(String name, float[][] xr) {
         System.out.println(name);
         for (int i = 0; i < xr.length; i++) {
             printArray(xr[i]);
         }
    }

    public static void dump2DArray(String name, double[][] xr) {
        System.out.println(name);
        for (int i = 0; i < xr.length; i++) {
            printArray(xr[i]);
        }
    }

    public static void printArray(int[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }
     
    public static void printArray(float[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }

    public static void printArray(double[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }

}
