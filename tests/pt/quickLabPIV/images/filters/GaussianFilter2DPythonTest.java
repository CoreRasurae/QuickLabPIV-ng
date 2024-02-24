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

public class GaussianFilter2DPythonTest {
    static final int I512x512 = 0, I128x512 = 1, I512x128 = 2;
    
    int  []     width;
    int  []     height;
    float[][][] img1;
    float[][][] img2;
    float[][][] img1F;
    float[][][] img2F;
    
    @Before
    public void setupTest() throws IOException {
        width  = new int[3];
        height = new int[3];
        img1   = new float[3][][];
        img2   = new float[3][][];
        img1F  = new float[3][][];
        img2F  = new float[3][][];
        
        width[I512x512]  = 512;
        height[I512x512] = 512;
        
        width[I128x512]  = 512;
        height[I128x512] = 128;
        
        width[I512x128]  = 128;
        height[I512x128] = 512;
         
        String filename = "testFiles" + File.separator + "Python_GaussianFilter_2px_3W.matFloat";
        int index1 = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1");
        int index2 = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2");
        if (index1 < 0 || index2 < 0) {
            throw new Error("Couldn't obtain I1 or I2 from " + filename);
        }
        
        img1[I512x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1);
        img2[I512x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2);

        int index1H = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1H");
        int index2H = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2H");
        if (index1H < 0 || index2H < 0) {
            throw new Error("Couldn't obtain I1H or I2H from " + filename);
        }

        img1[I128x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1H);
        img2[I128x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2H);

        int index1V = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1V");
        int index2V = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2V");
        if (index1V < 0 || index2V < 0) {
            throw new Error("Couldn't obtain I1V or I2V from " + filename);
        }

        img1[I512x128] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1V);
        img2[I512x128] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2V);

        int index1F = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1F");
        int index2F = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2F");
        if (index1F < 0 || index2F < 0) {
            throw new Error("Couldn't obtain I1F or I2F from " + filename);
        }
        
        img1F[I512x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1F);
        img2F[I512x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2F);

        int index1FH = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1FH");
        int index2FH = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2FH");
        if (index1FH < 0 || index2FH < 0) {
            throw new Error("Couldn't obtain I1FH or I2FH from " + filename);
        }

        img1F[I128x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1FH);
        img2F[I128x512] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2FH);

        int index1FV = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I1FV");
        int index2FV = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "I2FV");
        if (index1FV < 0 || index2FV < 0) {
            throw new Error("Couldn't obtain I1FV or I2FV from " + filename);
        }

        img1F[I512x128] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index1FV);
        img2F[I512x128] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, index2FV);
        
        for (int i = 0; i < 3; i++) {
            assertEquals("Height does not match at index " + i, img1[i].length, height[i]);
            assertEquals("Width does not match at index " + i, img1[i][0].length, width[i]);
            assertEquals("Height does not match at index " + i, img2[i].length, height[i]);
            assertEquals("Width does not match at index " + i, img2[i][0].length, width[i]);

            assertEquals("Height does not match at index " + i, img1F[i].length, height[i]);
            assertEquals("Width does not match at index " + i, img1F[i][0].length, width[i]);
            assertEquals("Height does not match at index " + i, img2F[i].length, height[i]);
            assertEquals("Width does not match at index " + i, img2F[i][0].length, width[i]);
        }
        
        PIVContextTestsSingleton.setSingleton();
    }

    @Test
    public void testImage512x512Pass() {
        
        final int targetIndex = I512x512;

        testImageForTargetIndex(targetIndex);
    }

    @Test
    public void testImage128x512Pass() {
        
        final int targetIndex = I128x512;

        testImageForTargetIndex(targetIndex);
    }

    @Test
    public void testImage512x128Pass() {
        
        final int targetIndex = I512x128;

        testImageForTargetIndex(targetIndex);
    }

    @Test
    public void testMatrix512x512Pass() {
        
        final int targetIndex = I512x512;

        testMatrixForTargetIndex(targetIndex);
    }

    @Test
    public void testMatrix128x512Pass() {
        
        final int targetIndex = I128x512;

        testMatrixForTargetIndex(targetIndex);
    }

    @Test
    public void testMatrix512x128Pass() {
        
        final int targetIndex = I512x128;

        testMatrixForTargetIndex(targetIndex);
    }

    @Test
    public void testArray512x512Pass() {
        
        final int targetIndex = I512x512;

        testMatrixForTargetIndex(targetIndex);
    }

    @Test
    public void testArray128x512Pass() {
        
        final int targetIndex = I128x512;

        testMatrixForTargetIndex(targetIndex);
    }

    @Test
    public void testArray512x128Pass() {
        
        final int targetIndex = I512x128;

        testMatrixForTargetIndex(targetIndex);
    }

    public void testImageForTargetIndex(int targetIndex) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();

        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        final int w  = width[targetIndex];
        final int h = height[targetIndex];
                
        
        Matrix img1M = new MatrixFloat(h, w);
        img1M.copyMatrixFrom2DArray(img1[targetIndex], 0, 0);

        Matrix img2M = new MatrixFloat(h, w);
        img2M.copyMatrixFrom2DArray(img2[targetIndex], 0, 0);
        
        ImageFloat img1II = new ImageFloat(img1M, w, h, "Img1Python");
        ImageFloat img2II = new ImageFloat(img2M, w, h, "Img2Python");

        IImage img1IIF = filter.applyFilter(img1II, null);
        IImage img2IIF = filter.applyFilter(img2II, null);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                assertEquals("Filtered Image1 does not match at: I=" + i + ", J=" + j, img1F[targetIndex][i][j], img1IIF.readPixel(i, j), 1e-4f);
                assertEquals("Filtered Image2 does not match at: I=" + i + ", J=" + j, img2F[targetIndex][i][j], img2IIF.readPixel(i, j), 1e-4f);
            }
        }
    }    

    public void testMatrixForTargetIndex(int targetIndex) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();

        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        final int w  = width[targetIndex];
        final int h = height[targetIndex];
                
        
        Matrix img1M = new MatrixFloat(h, w);
        img1M.copyMatrixFrom2DArray(img1[targetIndex], 0, 0);

        Matrix img2M = new MatrixFloat(h, w);
        img2M.copyMatrixFrom2DArray(img2[targetIndex], 0, 0);
        
        Matrix img1MF = filter.applyFilter(img1M, null);
        Matrix img2MF = filter.applyFilter(img2M, null);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                assertEquals("Filtered Image1 does not match at: I=" + i + ", J=" + j, img1F[targetIndex][i][j], img1MF.getElement(i, j), 1e-4f);
                assertEquals("Filtered Image2 does not match at: I=" + i + ", J=" + j, img2F[targetIndex][i][j], img2MF.getElement(i, j), 1e-4f);
            }
        }
    }    

    public void testArrayForTargetIndex(int targetIndex) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();

        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(2.0f, 3);
        singleton.getPIVParameters().setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
        IFilter filter = new GaussianFilter2D();
        
        final int w  = width[targetIndex];
        final int h = height[targetIndex];
                
        float[][] img1FTest = filter.applyFilter(img1[targetIndex], null);
        float[][] img2FTest = filter.applyFilter(img2[targetIndex], null);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                assertEquals("Filtered Image1 does not match at: I=" + i + ", J=" + j, img1F[targetIndex][i][j], img1FTest[i][j], 1e-4f);
                assertEquals("Filtered Image2 does not match at: I=" + i + ", J=" + j, img2F[targetIndex][i][j], img2FTest[i][j], 1e-4f);
            }
        }
    }    

}
