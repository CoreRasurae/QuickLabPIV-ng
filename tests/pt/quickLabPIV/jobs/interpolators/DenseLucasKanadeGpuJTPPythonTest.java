package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.exception.AparapiKernelFailedException;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.interpolators.LucasKanadeFloat;
import pt.quickLabPIV.interpolators.SimpleLucasKanadeImpl;

public class DenseLucasKanadeGpuJTPPythonTest {
    private String filename;
    private float[][] beforeU;
    private float[][] beforeV;
    private float[][] imageA;
    private float[][] imageB;
    private int blockItemsPerWorkGroupI;
    private int blockItemsPerWorkGroupJ;
    private int blockSizeI;
    private int blockSizeJ;
    private int globalSizeI;
    private int globalSizeJ;
    private int localSizeI;
    private int localSizeJ;
    private int windowSize;
    private int imageWidth;
    private int imageHeight;
    
    private class JTPKernelManager extends KernelManager {
        private JTPKernelManager() {
            LinkedHashSet<Device> preferredDevices = new LinkedHashSet<Device>(1);
            preferredDevices.add(JavaDevice.THREAD_POOL);
            setDefaultPreferredDevices(preferredDevices);
        }
        @Override
        protected List<Device.TYPE> getPreferredDeviceTypes() {
            return Arrays.asList(Device.TYPE.JTP);
        }
    }
    
    private class DefaultKernelManager extends KernelManager {
        
    }
    
    @Before
    public void setup() throws IOException {
        filename = "testFiles" + File.separator + "Python_denseLucasKanade_PyrLvs2_Fs2_0_Iter5_64x64.matFloat";
        int aIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "imageA");
        int bIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "imageB");

        if (aIndex < 0 || bIndex < 0) {
            throw new Error("Input data matrix or image data not found in file: " + filename);
        }

        imageA = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, aIndex);
        imageB = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bIndex);

        KernelManager.setKernelManager(new JTPKernelManager());        
        windowSize = 27;
        blockSizeI = 4;
        blockSizeJ = 4;
        localSizeI = 8;
        localSizeJ = 8;

        imageWidth  = 64;
        imageHeight = 64;
        
        blockItemsPerWorkGroupI = (windowSize + blockSizeI - 1) / localSizeI;
        if ((windowSize + blockSizeI - 1) % localSizeI != 0) {
            blockItemsPerWorkGroupI++;
        }
        blockItemsPerWorkGroupJ = (windowSize + blockSizeJ - 1) / localSizeJ;
        if ((windowSize + blockSizeJ - 1) % localSizeJ != 0) {
            blockItemsPerWorkGroupJ++;
        }
        
        globalSizeI = imageHeight / blockSizeI;
        if (imageHeight % blockSizeI != 0) {
            globalSizeI++;
        }
        globalSizeI *= localSizeI;
        
        globalSizeJ = imageWidth / blockSizeJ;
        if (imageWidth % blockSizeJ != 0) {
            globalSizeJ++;
        }
        globalSizeJ *= localSizeJ;
    }

    @After
    public void tearDown() {
        KernelManager.setKernelManager(new DefaultKernelManager());
    }
    
    private void copyUsVs(float[] us, float[] vs) {
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j< imageWidth; j++) {
                int idx = i * imageWidth + j;
                us[idx] = beforeU[i][j];
                vs[idx] = beforeV[i][j];
            } 
        }
    }

    private void copyImgs(float[] img1, float[] img2) {
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j< imageWidth; j++) {
                int idx = i * imageWidth + j;
                img1[idx] = imageA[i][j];
                img2[idx] = imageB[i][j];
            } 
        }
    }

    @Test
    public void denseLucasKanadeValidationWith1IterationTest() throws Throwable {
        int uIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd1_u");
        int vIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd1_v");

        if (uIndexIn < 0 || vIndexIn < 0) {
            throw new Error("Input data matrix not found in file: " + filename);
        }

        int uIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd1_u");
        int vIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd1_v");

        if (uIndexVal < 0 || vIndexVal < 0) {
            throw new Error("Validation data matrix not found in file: " + filename);
        }
 
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexIn);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexIn);

        
        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexVal);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexVal);

        testDenseLucasKanadeGpuJTP(afterU, afterV, 1, 6e-3f);
    }
    
    @Test
    public void denseLucasKanadeValidationWith4IterationTest() throws Throwable {
        int uIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd4_u");
        int vIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd4_v");

        if (uIndexIn < 0 || vIndexIn < 0) {
            throw new Error("Input data matrix not found in file: " + filename);
        }

        int uIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd4_u");
        int vIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd4_v");

        if (uIndexVal < 0 || vIndexVal < 0) {
            throw new Error("Validation data matrix not found in file: " + filename);
        }
 
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexIn);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexIn);

        
        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexVal);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexVal);

        testDenseLucasKanadeGpuJTP(afterU, afterV, 4, 6e-3f);
    }

    @Test
    public void denseLucasKanadeValidationWith5IterationsTest() throws Throwable {
        int uIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_u");
        int vIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_v");

        if (uIndexIn < 0 || vIndexIn < 0) {
            throw new Error("Input data matrix not found in file: " + filename);
        }

        int uIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_u");
        int vIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_v");

        if (uIndexVal < 0 || vIndexVal < 0) {
            throw new Error("Validation data matrix not found in file: " + filename);
        }
 
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexIn);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexIn);

        
        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexVal);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexVal);

        testDenseLucasKanadeGpuJTP(afterU, afterV, 5, 6e-3f);
    }
    
    @Test
    public void denseLucasKanadeFloatJavaValidationWith5IterationsTest() throws Throwable {
        int uIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_u");
        int vIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_v");

        if (uIndexIn < 0 || vIndexIn < 0) {
            throw new Error("Input data matrix not found in file: " + filename);
        }

        int uIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_u");
        int vIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_v");

        if (uIndexVal < 0 || vIndexVal < 0) {
            throw new Error("Validation data matrix not found in file: " + filename);
        }
 
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexIn);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexIn);

        
        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexVal);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexVal);

        testDenseLucasKanadeFloatJava(afterU, afterV, 5, 6e-3f);
    }

    @Test
    public void denseLucasKanadeSimpleImplJavaValidationWith5IterationsTest() throws Throwable {
        int uIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_u");
        int vIndexIn = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_v");

        if (uIndexIn < 0 || vIndexIn < 0) {
            throw new Error("Input data matrix not found in file: " + filename);
        }

        int uIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_u");
        int vIndexVal = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_v");

        if (uIndexVal < 0 || vIndexVal < 0) {
            throw new Error("Validation data matrix not found in file: " + filename);
        }
 
        beforeU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexIn);
        beforeV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexIn);

        
        float[][] afterU = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, uIndexVal);
        float[][] afterV = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vIndexVal);

        testDenseLucasKanadeSimpleImplJava(afterU, afterV, 5, 6e-3f);
    }

    private void testDenseLucasKanadeGpuJTP(float[][] afterU, float[][] afterV, int iterations, float tolerance) throws Throwable {
        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                windowSize, iterations, imageHeight, imageWidth);
        //kernel.enableDebugBarriers();

        final float[] us = new float[imageHeight * imageWidth];
        final float[] vs = new float[imageHeight * imageWidth];

        final float[] img1 = new float[imageHeight * imageWidth];
        final float[] img2 = new float[imageHeight * imageWidth];
        
        //Use Python unfiltered images as an image source
        /*Matrix img1M = new MatrixFloat(imageHeight, imageWidth, 255);
        Matrix img2M = new MatrixFloat(imageHeight, imageWidth, 255);
        img1M.copyMatrixFrom2DArray(imageA, 0, 0);
        img2M.copyMatrixFrom2DArray(imageB, 0, 0);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage img1I = new ImageFloat(img1M, imageWidth, imageHeight, "python_rankine_vortex01_0.matFloat");
        IImage img2I = new ImageFloat(img2M, imageWidth, imageHeight, "python_rankine_vortex01_1.matFloat");
        img1I = filter.applyFilter(img1, img1I);
        img2I = filter.applyFilter(img2, img2I);
        img1 = img1I.exportTo1DFloatArray(img1);
        img2 = img2I.exportTo1DFloatArray(img2);*/
        
        //Use Python filtered images as image source
        copyImgs(img1, img2);
        
        //Use Java filtered image source
        /*IImage img1I = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2I = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        img1I = ImageFloat.convertFrom(img1I.clipImage(0, 0, imageHeight, imageWidth, false, null));
        img2I = ImageFloat.convertFrom(img2I.clipImage(0, 0, imageHeight, imageWidth, false, null));
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1I = filter.applyFilter(img1I, img1I);
        img2I = filter.applyFilter(img2I, img2I);
        img1I.exportTo1DFloatArray(img1);
        img2I.exportTo1DFloatArray(img2);*/

        
        copyUsVs(us, vs);
        
        kernel.setKernelArgs(img1, img2, us, vs, false);

        try {
            kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 1);
        } catch (AparapiKernelFailedException ex) {
            if (ex.getCause().getClass().equals(AssertionError.class)) {
                throw ex.getCause();
            }
            throw ex;
        }
        
        //There is a problem with clamping in the y direction first row
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                float validationU = afterU[i][j];
                float validationV = afterV[i][j];
                float testingU = us[i * imageWidth + j];
                float testingV = vs[i * imageWidth + j];
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, testingU, tolerance);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, testingV, tolerance);
            }
        }

    }
    
    private void testDenseLucasKanadeFloatJava(float[][] afterU, float[][] afterV, int iterations, float tolerance) throws Throwable {
        Matrix img1M = new MatrixFloat(imageHeight, imageWidth, 255);
        Matrix img2M = new MatrixFloat(imageHeight, imageWidth, 255);
        img1M.copyMatrixFrom2DArray(imageA, 0, 0);
        img2M.copyMatrixFrom2DArray(imageB, 0, 0);       
        IImage img1I = new ImageFloat(img1M, imageWidth, imageHeight, "python_rankine_vortex01_0.matFloat");
        IImage img2I = new ImageFloat(img2M, imageWidth, imageHeight, "python_rankine_vortex01_1.matFloat");

        LucasKanadeFloat lk = new LucasKanadeFloat(1, 0.0f, 3, 27, iterations, false);
        lk.updateImageA(img1I);
        lk.updateImageB(img2I);

        float testingU[] = new float[1];
        float testingV[] = new float[1];
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                float validationU = afterU[i][j];
                float validationV = afterV[i][j];
                lk.getVelocitiesMatrix(i, j, i + beforeV[i][j], j + beforeU[i][j], testingU, testingV);
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, testingU[0], tolerance);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, testingV[0], tolerance);
            }
        }
    }
    
    private void testDenseLucasKanadeSimpleImplJava(float[][] afterU, float[][] afterV, int iterations, float tolerance) throws Throwable {
        Matrix img1M = new MatrixFloat(imageHeight, imageWidth, 255);
        Matrix img2M = new MatrixFloat(imageHeight, imageWidth, 255);
        img1M.copyMatrixFrom2DArray(imageA, 0, 0);
        img2M.copyMatrixFrom2DArray(imageB, 0, 0);       
        IImage img1I = new ImageFloat(img1M, imageWidth, imageHeight, "python_rankine_vortex01_0.matFloat");
        IImage img2I = new ImageFloat(img2M, imageWidth, imageHeight, "python_rankine_vortex01_1.matFloat");

        SimpleLucasKanadeImpl testImpl = new SimpleLucasKanadeImpl(0.0f, 3, false, 27, iterations);
        testImpl.updateImageA(img1I);
        testImpl.updateImageB(img2I);
        
        final boolean halfPixelOffset = false;
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                float validationU = afterU[i][j];
                float validationV = afterV[i][j];
                double testingUs[] = testImpl.interpolate(i, j, beforeV[i][j], beforeU[i][j], halfPixelOffset);
                
                assertEquals("Velocity U diffferent than expected at I:" + i + ", J:" + j, validationU, testingUs[1], tolerance);
                assertEquals("Velocity V diffferent than expected at I:" + i + ", J:" + j, validationV, testingUs[0], tolerance);
            }
        }
    }
}
