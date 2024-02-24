// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images.filters;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;

public class GaussianFilter2DTest {
    float[][] imgFiltered;
    float[][] imgFiltered5px1;
    float[][] imgFiltered5px2;
    
    @Before
    public void setupTest() throws IOException {
        String filename = "testFiles" + File.separator + "GaussianFilter2D_3px_2.0f_rankine_vortex01_0.matFloat";
        //This is the filtered image rankine_vortex01_0.tif with a 3 point 2.0 sigma Gaussian filter
        /*int numberOfMatrices = FloatMatrixReaderHelper.getNumberOfMatrices(filename);
        String names[] = FloatMatrixReaderHelper.getMatricesNames(filename);
        int index = FloatMatrixReaderHelper.getMatrixIndexFromName(filename, "gaussianFiltered");*/
        imgFiltered = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, 0);
        
        String filename5px = "testFiles" + File.separator + "GaussianFilter2D_5px_0.48f_rankine_vortex01_0_1.matFloat";
        imgFiltered5px1 = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename5px, 0);
        imgFiltered5px2 = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename5px, 1);
        PIVContextTestsSingleton.setSingleton();
    }
    
    @Test
    public void simpleConvolutionTest() {
        float a[] = new float[] {1.5f,2.5f,3.5f};
        float b[] = new float[] {1.0f, 2.0f};
        float result[] = new float[] {1.5f, 5.5f, 8.5f};
        
        IConvolution1D convolver = new DirectConvolution();
        float[] testResult = convolver.convolve1D(a, 3, b, 2, null);
        
        assertEquals("Both results arrays should have the same size", result.length, testResult.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Failed at index i= " + i, result[i], testResult[i], 1e-6f);
        }
    }
    
    @Test    
    public void gaussianFilterMatrixPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        Matrix matrix = imgA.clipImageMatrix(0, 0, imgA.getHeight(), imgA.getWidth(), false, null);
        Matrix filtered = filter.applyFilter(matrix, null);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = filtered.getElement(i, j);
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }

    @Test    
    public void gaussianFilter5pxMatrixPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(0.48f, 5);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        Matrix matrix = imgA.clipImageMatrix(0, 0, imgA.getHeight(), imgA.getWidth(), false, null);
        Matrix filtered = filter.applyFilter(matrix, null);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = filtered.getElement(i, j);
                float expectedValue = imgFiltered5px1[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 1e-4f);
            }
        }
    }
    
    @Test    
    public void gaussianFilterLargerMatrixPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        Matrix tempM = new MatrixFloat(imgA.getHeight() + 55, imgA.getWidth() * 2);
        filter.applyFilter(tempM, null);
        
        Matrix matrix = imgA.clipImageMatrix(0, 0, imgA.getHeight(), imgA.getWidth(), false, null);
        Matrix filtered = filter.applyFilter(matrix, null);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = filtered.getElement(i, j);
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }
    
    @Test    
    public void gaussianFilter2DArrayPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        float testArray[][] = new float[imgA.getHeight()][imgA.getWidth()];
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                testArray[i][j] = imgA.readPixel(i, j);
            }
        }

        float[][] filtered = filter.applyFilter(testArray, null);
        
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = filtered[i][j];
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }

    @Test    
    public void gaussianFilter2DLargerArrayPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();

        float tempArray[][] = new float[2*imgA.getHeight()][imgA.getWidth() + 55];
        for (int i = 0; i < tempArray.length; i++) {
            for (int j = 0; j < tempArray[0].length; j++) {
                tempArray[i][j] = 1.0f;
            }
        }
        
        filter.applyFilter(tempArray, null);
        
        
        float testArray[][] = new float[imgA.getHeight()][imgA.getWidth()];
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                testArray[i][j] = imgA.readPixel(i, j);
            }
        }

        float[][] filtered = filter.applyFilter(testArray, null);
        
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = filtered[i][j];
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }

    
    @Test    
    public void gaussianFilterImagePassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        Matrix matrix = imgA.clipImageMatrix(0, 0, imgA.getHeight(), imgA.getWidth(), false, null);
        IImage imgF = new ImageFloat(matrix, imgA.getHeight(), imgA.getWidth(), "img1.png");
        imgF = filter.applyFilter(imgF, imgF);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = imgF.readPixel(i, j);
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }

    @Test    
    public void gaussianFilterImagePassTest2() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();

        IImage imgF = ImageFloat.convertFrom(imgA);
        imgF = filter.applyFilter(imgF, imgF);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = imgF.readPixel(i, j);
                float expectedValue = imgFiltered[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-5f);
            }
        }
    }

    @Test    
    public void gaussianFilterImage5pxPassTest() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(0.48f, 5);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        Matrix matrix = imgA.clipImageMatrix(0, 0, imgA.getHeight(), imgA.getWidth(), false, null);
        IImage imgF = new ImageFloat(matrix, imgA.getHeight(), imgA.getWidth(), "img1.png");
        imgF = filter.applyFilter(imgF, imgF);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = imgF.readPixel(i, j);
                float expectedValue = imgFiltered5px1[i][j];
                assertEquals("Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-4f);
            }
        }
    }
    
    @Test    
    public void gaussianFilterImage5pxPassTest2() {
        ((PIVContextTestsSingleton)PIVContextTestsSingleton.getSingleton()).reset();
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage imgB = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(0.48f, 5);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        IImage imgF1 = ImageFloat.convertFrom(imgA);
        imgF1 = filter.applyFilter(imgF1, imgF1);
        IImage imgF2 = ImageFloat.convertFrom(imgB);
        imgF2 = filter.applyFilter(imgF2, imgF2);
        for (int i = 0; i < imgA.getHeight(); i++) {
            for (int j = 0; j < imgA.getWidth(); j++) {
                float computedValue = imgF1.readPixel(i, j);
                float expectedValue = imgFiltered5px1[i][j];
                assertEquals("F1 - Failed at: [I=" + i + ", J=" + j + "]", expectedValue, computedValue, 4e-4f);
                float computedValue2 = imgF2.readPixel(i, j);
                float expectedValue2 = imgFiltered5px2[i][j];
                assertEquals("F2 - Failed at: [I=" + i + ", J=" + j + "]", expectedValue2, computedValue2, 4e-4f);
            }
        }
    }
}