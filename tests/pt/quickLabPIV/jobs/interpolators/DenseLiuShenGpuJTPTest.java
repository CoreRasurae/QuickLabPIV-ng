// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.util.FastMath;
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
import pt.quickLabPIV.jobs.interpolators.DenseLiuShenGpuTestKernel.IDenseLiuShenListener;
import pt.quickLabPIV.jobs.interpolators.DenseLiuShenGpuTestKernel.IDenseLucasKanadeListener;

public class DenseLiuShenGpuJTPTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    
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
    private float imageA[];
    private float imageB[];

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

    private class GPUKernelManager extends KernelManager {
        private GPUKernelManager() {
            LinkedHashSet<Device> preferredDevices = new LinkedHashSet<Device>(1);
            preferredDevices.add(gpuDevice.getAparapiDevice());
            setDefaultPreferredDevices(preferredDevices);
        }
        @Override
        protected List<Device.TYPE> getPreferredDeviceTypes() {
            return Arrays.asList(Device.TYPE.GPU);
        }
    }

    private class DefaultKernelManager extends KernelManager {
        
    }
    
    @Before
    public void setup() {
        KernelManager.setKernelManager(new JTPKernelManager());        
        windowSize = 27;
        blockSizeI = 4;
        blockSizeJ = 4;
        localSizeI = 8;
        localSizeJ = 8;

        imageWidth = 43;
        imageHeight = 39;
        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];
        
        blockItemsPerWorkGroupI = (windowSize + blockSizeI-1) / localSizeI;
        if ((windowSize + blockSizeI-1) % localSizeI != 0) {
            blockItemsPerWorkGroupI++;
        }
        blockItemsPerWorkGroupJ = (windowSize + blockSizeJ-1) / localSizeJ;
        if ((windowSize + blockSizeJ-1) % localSizeJ != 0) {
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

    @Test
    public void testDenseLiuShenAgainstPythonPass() throws Throwable {
        imageWidth = 64;
        imageHeight = 64;
        final int iters = 5;

        final boolean exportOnlyPass = false;
        
        String filename = "testFiles" + File.separator + "Python_denseLiuShen_lambda4_Rankine_vortex_64x64.matFloat";        
        if (!exportOnlyPass) {
            filename = "testFiles" + File.separator + "Python_with_JavaLK_denseLiuShen_lambda4_Rankine_vortex_64x64.matFloat";
        }
        int aIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im1");
        int bIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im2");
        if (aIndex < 0 || bIndex < 0) {
            throw new Error("Couldn't find input image data in matFloat file: " + filename);
        }

        float[][] img1F = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, aIndex);
        float[][] img2F = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bIndex);

        
        int aLKFilterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im1LK_Filtered");
        int bLKFilterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im2LK_Filtered");
        if (aLKFilterIndex < 0 || bLKFilterIndex < 0) {
            throw new Error("Couldn't find input image data in matFloat file: " + filename);
        }

        float[][] img1LKFF = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, aLKFilterIndex);
        float[][] img2LKFF = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bLKFilterIndex);

        
        int aLSFilterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im1LS_Filtered");
        int bLSFilterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Im2LS_Filtered");
        if (aLSFilterIndex < 0 || bLSFilterIndex < 0) {
            throw new Error("Couldn't find input image data in matFloat file: " + filename);
        }

        float[][] img1LSFF = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, aLSFilterIndex);
        float[][] img2LSFF = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bLSFilterIndex);

        
        int usLKBeforeIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_u");
        int vsLKBeforeIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "beforeLK2nd5_v");
        if (usLKBeforeIndex < 0 || vsLKBeforeIndex < 0) {
            throw new Error("Couldn't find before 2nd Pyramidal LK step input velocity data in matFloat file: " + filename);
        }

        final float[][] usLKBefore = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, usLKBeforeIndex);
        final float[][] vsLKBefore = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vsLKBeforeIndex);

        
        int usLKAfterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_u");
        int vsLKAfterIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "afterLK2nd5_v");
        if (usLKAfterIndex < 0 || vsLKAfterIndex < 0) {
            throw new Error("Couldn't find after 2nd Pyramidal LK step velocity data in matFloat file: " + filename);
        }

        final float[][] usLKAfter = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, usLKAfterIndex);
        final float[][] vsLKAfter = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vsLKAfterIndex);

        int usLKJavaIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "uLK");
        int vsLKJavaIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "vLK");
        if (usLKJavaIndex < 0 || vsLKJavaIndex < 0) {
            throw new Error("Couldn't find after 2nd Pyramidal Java LK step velocity data in matFloat file: " + filename);
        }
        
        final float[][] usLKJava = exportOnlyPass ? null : SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, usLKJavaIndex);
        final float[][] vsLKJava = exportOnlyPass ? null : SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vsLKJavaIndex);
                
        int cmtxIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "cmtx");
        if (cmtxIndex < 0) {
            throw new Error("Couldn't find validation cmtx data in matFloat file: " + filename);
        }
        
        final float[][] cmtxVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, cmtxIndex);
        
        int iixIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "IIx");
        int iiyIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "IIy");
        if (iixIndex < 0 || iiyIndex < 0) {
            throw new Error("Couldn't find validation IIx or IIy data in matFloat file: " + filename);
        }

        final float[][] iixVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, iixIndex);
        final float[][] iiyVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, iiyIndex);

        int iiIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "II");
        final float[][] iiVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, iiIndex);
        if (iiIndex < 0) {
            throw new Error("Couldn't find validation II data in matFloat file: " + filename);
        }

        int ixtIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Ixt");
        int iytIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Iyt");
        if (ixtIndex < 0 || iytIndex < 0) {
            throw new Error("Couldn't find validation matrices Ixt or Iyt data in matFloat file: " + filename);
        }

        final float[][] ixtVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, ixtIndex);
        final float[][] iytVal = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, iytIndex);

        int a11Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "A11");
        int a12Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "A12");
        int a22Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "A22");
        if (a11Index < 0 || a12Index < 0 || a22Index < 0) {
            throw new Error("Couldn't find validation matrices B11, or B12, or B22 data in matFloat file: " + filename);
        }

        final float[][] a11Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, a11Index);
        final float[][] a12Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, a12Index);
        final float[][] a22Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, a22Index);

        int b11Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "B11");
        int b12Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "B12");
        int b22Index = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "B22");
        if (b11Index < 0 || b12Index < 0 || b22Index < 0) {
            throw new Error("Couldn't find validation matrices B11, or B12, or B22 data in matFloat file: " + filename);
        }

        final float[][] b11Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, b11Index);
        final float[][] b12Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, b12Index);
        final float[][] b22Val = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, b22Index);

        final float[][][] HusVal   = new float[iters][imageHeight][imageWidth];
        final float[][][] HvsVal   = new float[iters][imageHeight][imageWidth];
        final float[][][] busVal   = new float[iters][imageHeight][imageWidth];
        final float[][][] bvsVal   = new float[iters][imageHeight][imageWidth];
        final float[][][] unewsVal = new float[iters][imageHeight][imageWidth];
        final float[][][] vnewsVal = new float[iters][imageHeight][imageWidth];
        for (int i = 0; i < iters; i++) {
            int HusIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Hus" + i);
            int HvsIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "Hvs" + i);
            if (HusIndex < 0 || HvsIndex < 0) {
                throw new Error("Couldn't find validation matrices Hus" + i + " or Hvs" + i + " data in matFloat file: " + filename);
            }
    
            HusVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, HusIndex);
            HvsVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, HvsIndex);

            int busIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "bus" + i);
            int bvsIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "bvs" + i);
            if (busIndex < 0 || bvsIndex < 0) {
                throw new Error("Couldn't find validation matrices bus" + i + " or bvs" + i + " data in matFloat file: " + filename);
            }
    
            busVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, busIndex);
            bvsVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, bvsIndex);

            int unewsIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "unews" + i);
            int vnewsIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(filename, "vnews" + i);
            if (unewsIndex < 0 || vnewsIndex < 0) {
                throw new Error("Couldn't find validation matrices unews" + i + "or vnews" + i + " data in matFloat file: " + filename);
            }
    
            unewsVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, unewsIndex);
            vnewsVal[i] = SimpleFloatMatrixImporterExporter.readFromFormattedFile(filename, vnewsIndex);
        }
        
        //Create image1 and image2 from Python data
        /*Matrix img1M = new MatrixFloat(imageHeight, imageWidth);
        img1M.copyMatrixFrom2DArray(img1F, 0, 0);

        Matrix img2M = new MatrixFloat(imageHeight, imageWidth);
        img2M.copyMatrixFrom2DArray(img2F, 0, 0);
                
        IImage img1 = new ImageFloat(img1M, imageWidth, imageHeight, "rankine_vortex01_0.matFloat");
        IImage img2 = new ImageFloat(img2M, imageWidth, imageHeight, "rankine_vortex01_1.matFloat");
        
        //Filter images with QuickLabPIV filter implementation for Lucas-Kanade
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);
        
        //Filter and normalize images for Liu-Shen
        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);*/

        //Use Python filtered images
        Matrix img1LKFM = new MatrixFloat(imageHeight, imageWidth);
        img1LKFM.copyMatrixFrom2DArray(img1LKFF, 0, 0);

        Matrix img2LKFM = new MatrixFloat(imageHeight, imageWidth);
        img2LKFM.copyMatrixFrom2DArray(img2LKFF, 0, 0);

        IImage imgLK1 = new ImageFloat(img1LKFM, imageWidth, imageHeight, "LK_Filtered_rankine_vortex_01_0.matFloat");
        IImage imgLK2 = new ImageFloat(img2LKFM, imageWidth, imageHeight, "LK_Filtered_rankine_vortex_01_1.matFloat");

        Matrix img1LSFM = new MatrixFloat(imageHeight, imageWidth);
        img1LSFM.copyMatrixFrom2DArray(img1LSFF, 0, 0);

        Matrix img2LSFM = new MatrixFloat(imageHeight, imageWidth);
        img2LSFM.copyMatrixFrom2DArray(img2LSFF, 0, 0);

        IImage imgLS1 = new ImageFloat(img1LSFM, imageWidth, imageHeight, "LS_Filtered_rankine_vortex_01_0.matFloat");
        IImage imgLS2 = new ImageFloat(img2LSFM, imageWidth, imageHeight, "LS_Filtered_rankine_vortex_01_1.matFloat");

        final int iterationsLK = 5;
        final int iterationsLS = 5;
        final float lambdaLS = 4.0f;

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
        
        if (usLKJava == null && vsLKJava == null) {
            System.out.println("Doing Export only pass with partial verification");
        }
        
        final float us[] = new float[imageHeight * imageWidth];
        final float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];
        
        //Initial estimated velocities are almost the same as the expected Lucas-Kanade computed ones
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                int idx = i * imageWidth + j;
                us[idx] = usLKBefore[i][j];
                vs[idx] = vsLKBefore[i][j];
            }
        }

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];

        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);

        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);
        
        final float usLKJavaOut[][] = new float[imageHeight][imageWidth];
        final float vsLKJavaOut[][] = new float[imageHeight][imageWidth];
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                windowSize, iterationsLK, imageHeight, imageWidth, iterationsLS, lambdaLS);
        //kernel.enableDebugBarriers();

        //Crucial initialization for the JTP test to have any meaningful result, otherwise Liu-Shen will end before the first iteration
        totalError[0] = 1e8f;

        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, false);

        kernel.registerLKListener(new IDenseLucasKanadeListener() {

            @Override
            public void denseLucasKanadeCompleted(float us[], float vs[], float u, float v, int pixelI, int pixelJ, int idI, int idJ, int gidI,
                    int gidJ, int tidI, int tidJ) {
                assertEquals("Velocity U at I:" + pixelI + ", J:" + pixelJ + " does not match the Python data", usLKAfter[pixelI][pixelJ], u, 6e-3f);
                assertEquals("Velocity V at I:" + pixelI + ", J:" + pixelJ + " does not match the Python data", vsLKAfter[pixelI][pixelJ], v, 6e-3f);
                
                usLKJavaOut[pixelI][pixelJ] = u;
                vsLKJavaOut[pixelI][pixelJ] = v;
            }
        });
        
        
        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
            
            public IDenseLiuShenListener setVars(IImage _img1, IImage _img2) {
                img1 = _img1;
                img2 = _img2;
                return this;
            }

            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                if (pixelI >= -1 && pixelJ >= - 1)
                   validationBuffer2 = validateImgBuffer("img2", img2, imgBuffer, pixelI, pixelJ, false);
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                if (pixelI >= -1 && pixelJ >= - 1)
                   validationBuffer1 = validateImgBuffer("img1", img1, imgBuffer, pixelI, pixelJ, false);
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }

                        float pixelValue1 = getNearestPixelWithWarp(img1, sourceImgI, sourceImgJ);
                        float pixelValue2 = getNearestPixelWithWarp(img2, sourceImgI, sourceImgJ);
                        assertEquals("Img1 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue1, pixelValues[(i+1) * 3 + (j+1)], 1e-3f);
                        assertEquals("Img2 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue2, pixelValues[(i+1) * 3 + (j+1) + 9], 1e-3f);
                    }                
                }
            }               

            @Override
            public void fixedImageDerivativesComputed(float w, float IIx, float IIy, float II, float Ixt, float Iyt, float A11, float A12, float A22, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
                
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    float tolA = 1e-5f;
                    float tolB = 1e-5f;
                    
                    if (!exportOnlyPass) {
                        assertEquals("cmtx at I: " + sourceImgI + ", J: " + sourceImgJ, cmtxVal[sourceImgI][sourceImgJ], w    , 1e-5f);
                        assertEquals("IIx at I: "  + sourceImgI + ", J: " + sourceImgJ, iixVal [sourceImgI][sourceImgJ], IIx  , tolA);
                        assertEquals("IIy at I: "  + sourceImgI + ", J: " + sourceImgJ, iiyVal [sourceImgI][sourceImgJ], IIy  , tolA);
                        assertEquals("II  at I: "  + sourceImgI + ", J: " + sourceImgJ, iiVal  [sourceImgI][sourceImgJ], II   , tolA);
                        assertEquals("Ixt at I: "  + sourceImgI + ", J: " + sourceImgJ, ixtVal [sourceImgI][sourceImgJ], Ixt  , tolA);
                        assertEquals("Iyt at I: "  + sourceImgI + ", J: " + sourceImgJ, iytVal [sourceImgI][sourceImgJ], Iyt  , tolA);
                        assertEquals("A11 at I: "  + sourceImgI + ", J: " + sourceImgJ, a11Val [sourceImgI][sourceImgJ], A11  , tolA);
                        assertEquals("A12 at I: "  + sourceImgI + ", J: " + sourceImgJ, a12Val [sourceImgI][sourceImgJ], A12  , tolA);
                        assertEquals("A22 at I: "  + sourceImgI + ", J: " + sourceImgJ, a22Val [sourceImgI][sourceImgJ], A22  , tolA);
                        assertEquals("B00 at I: "  + sourceImgI + ", J: " + sourceImgJ, b11Val [sourceImgI][sourceImgJ], Bs[0], tolB);
                        assertEquals("B01 at I: "  + sourceImgI + ", J: " + sourceImgJ, b12Val [sourceImgI][sourceImgJ], Bs[1], tolB);
                        assertEquals("B11 at I: "  + sourceImgI + ", J: " + sourceImgJ, b22Val [sourceImgI][sourceImgJ], Bs[2], tolB);
                    }
                }
            }

            @Override
            public void vectorsRefined(int w, float huComposite, float hvComposite, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[], 
                                       int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {

                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < imageHeight && sourceImgJ < imageWidth) {
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int idx = (i + 1) * 3 + (j + 1);
                            if (iterLS == 0) {
                                //Intentionally swapped vector coordinates because Lucas-Kanade uses u for horizontal and v for vertical
                                float usLiuInVal = getValueComplete(vsLKAfter, sourceImgI + i, sourceImgJ + j, false);
                                float vsLiuInVal = getValueComplete(usLKAfter, sourceImgI + i, sourceImgJ + j, false);
                                float tolerance = 6e-3f;
                                if (!exportOnlyPass) {
                                    tolerance = 1e-6f;
                                    usLiuInVal = getValueComplete(vsLKJava, sourceImgI + i, sourceImgJ + j, false);
                                    vsLiuInVal = getValueComplete(usLKJava, sourceImgI + i, sourceImgJ + j, false);
                                }
                                assertEquals("usLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), usLiuInVal, usLiuIn[idx], tolerance);
                                assertEquals("vsLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), vsLiuInVal, vsLiuIn[idx], tolerance);
                            } else {
                                if (!exportOnlyPass) {
                                    float usLiuInVal = getValueComplete(unewsVal[iterLS-1], sourceImgI + i, sourceImgJ + j, false);
                                    float vsLiuInVal = getValueComplete(vnewsVal[iterLS-1], sourceImgI + i, sourceImgJ + j, false);
                                    assertEquals("usLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), usLiuInVal, usLiuIn[idx], 1e-5f);
                                    assertEquals("vsLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), vsLiuInVal, vsLiuIn[idx], 1e-5f);
                                }
                            }
                        }
                    }
                    
                    if (!exportOnlyPass) {
                        float hu = huComposite - ixtVal[sourceImgI][sourceImgJ];
                        float hv = hvComposite - iytVal[sourceImgI][sourceImgJ];
                        assertEquals("hu at I: "         + sourceImgI + ", J: " + sourceImgJ, HusVal  [iterLS][sourceImgI][sourceImgJ], hu        , 1e-4f);
                        assertEquals("hv at I: "         + sourceImgI + ", J: " + sourceImgJ, HvsVal  [iterLS][sourceImgI][sourceImgJ], hv        , 1e-4f);
                        assertEquals("bu at I: "         + sourceImgI + ", J: " + sourceImgJ, busVal  [iterLS][sourceImgI][sourceImgJ], bu        , 1e-4f);
                        assertEquals("bv at I: "         + sourceImgI + ", J: " + sourceImgJ, bvsVal  [iterLS][sourceImgI][sourceImgJ], bv        , 1e-4f);
                        assertEquals("usNew at I: "      + sourceImgI + ", J: " + sourceImgJ, unewsVal[iterLS][sourceImgI][sourceImgJ], unew      , 1e-3f);
                        assertEquals("vsNew at I: "      + sourceImgI + ", J: " + sourceImgJ, vnewsVal[iterLS][sourceImgI][sourceImgJ], vnew      , 1e-3f);
                        //assertEquals("totalErrot at I: " + sourceImgI + ", J: " + sourceImgJ, totalErrorVal[sourceImgI][sourceImgJ], totalError, 1e-5f);
                    }
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = 0; i < imageHeight; i++) {
                    for (int j = 0; j < imageWidth; j++) {
                        int idx = i * imageWidth + j;
                        //Intentionally swapped vector coordinates because Lucas-Kanade uses u for horizontal and v for vertical,
                        //and here we output Lucas-Kanade orientation compatible vectors
                        if (!exportOnlyPass) {
                            assertEquals("us at I: " + i + ", J: " + j, unewsVal[iterLS][i][j], vs[idx], 1e-3f);
                            assertEquals("vs at I: " + i + ", J: " + j, vnewsVal[iterLS][i][j], us[idx], 1e-3f);
                        }
                    }
                }
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                
            }
        }.setVars(imgLS1, imgLS2));

        try {
            kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), iterationsLS + 1);
        } catch (AparapiKernelFailedException ex) {
            if (ex.getCause().getClass().equals(AssertionError.class)) {
                throw ex.getCause();
            }
            throw ex;
        }

        if (exportOnlyPass) {
            //Export LK matrices to combine with Python script output to further approximate Lucas-Kanade from OpenCL GPU floats vs JTP Java CPU floats        
            ArrayList<String> names = new ArrayList<>(2);
            ArrayList<float[][]> matrices = new ArrayList<>(2);
            names.add("JavaLK2ndPass_5iter_u");
            names.add("JavaLK2ndPass_5iter_v");
            matrices.add(usLKJavaOut);
            matrices.add(vsLKJavaOut);
            SimpleFloatMatrixImporterExporter.writeToFormattedFile("Java_PythonLK1stPass_JavaLK2ndPass_Rankine_64x64.matFloat", names, matrices);
        }
    }
    
    @Test
    public void testAllPixelsComputedPass() throws Throwable {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];

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

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        
        img1 = new ImageFloat(region1, imageHeight, imageWidth, "img1.png");
        img2 = new ImageFloat(region2, imageHeight, imageWidth, "img2.png");

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);

        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);

        
        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);
        
        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);

        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];
        final AtomicInteger usageMap[][] = new AtomicInteger[imageHeight][imageWidth];
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                usageMap[i][j] = new AtomicInteger(0);
            }
        }

        int iterationsLK = 5;
        int iterationsLS = 1;
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                windowSize, iterationsLK, imageHeight, imageWidth, iterationsLS, 1000.0f);

        //Crucial initialization for the JTP test to have any meaningful result, otherwise Liu-Shen will end before the first iteration
        totalError[0] = 1e8f;

        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, false);

        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
            private AtomicInteger[][] map;
            
            public IDenseLiuShenListener setVars(IImage _img1, IImage _img2, AtomicInteger[][] _map) {
                img1 = _img1;
                img2 = _img2;
                map = _map;
                return this;
            }

            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }                        
                    }
                }
                
            }

            @Override
            public void fixedImageDerivativesComputed(float w, float IIx, float IIy, float II, float Ixt, float Iyt, float A11, float A12, float A22, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    map[sourceImgI][sourceImgJ].incrementAndGet(); 
                }
            }

            @Override
            public void vectorsRefined(int w, float huComposite, float hvComposite, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[], 
                                       int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {

                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                
            }
        }.setVars(imgLS1, imgLS2, usageMap));

        try {
            kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 2);
        } catch (AparapiKernelFailedException ex) {
            if (ex.getCause().getClass().equals(AssertionError.class)) {
                throw ex.getCause();
            }
            throw ex;
        }
        
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                assertEquals(usageMap[i][j].get(), 1);
            }
        }        
    }
    
    @Test
    public void testImageToLocalImgBufferDirectTestPass() throws Throwable {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];

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

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        
        img1 = new ImageFloat(region1, imageHeight, imageWidth, "img1.png");
        img2 = new ImageFloat(region2, imageHeight, imageWidth, "img2.png");

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);

        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);
        
        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];

        float[][] iixVal = new float[imageHeight][imageWidth];
        float[][] iiyVal = new float[imageHeight][imageWidth];
        float[][] iiVal = new float[imageHeight][imageWidth];
        float[][] ixtVal = new float[imageHeight][imageWidth];
        float[][] iytVal = new float[imageHeight][imageWidth];
        float[][] b00Val = new float[imageHeight][imageWidth];
        float[][] b01Val = new float[imageHeight][imageWidth];
        float[][] b11Val = new float[imageHeight][imageWidth];
        float[][] usLiuInVal = new float[imageHeight][imageWidth];
        float[][] vsLiuInVal = new float[imageHeight][imageWidth];
        float[][] usLiuOutVal = new float[imageHeight][imageWidth];
        float[][] vsLiuOutVal = new float[imageHeight][imageWidth];
        float[][] busVal = new float[imageHeight][imageWidth];
        float[][] bvsVal = new float[imageHeight][imageWidth];
        float[][] totalErrorVal = new float[imageHeight][imageWidth];
        final float lambda = 4.0f;
        int iterationsLK = 5;
        int iterationsLS = 1;
        
        //Crucial initialization for the JTP test to have any meaningful result, otherwise Liu-Shen will end before the first iteration
        totalError[0] = 1e8f;
        
        computeValidationUsAndVs(img1, img2, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, usLiuOutVal, vsLiuOutVal, iterationsLK, iterationsLS, lambda, false);
        
        float totalErrorResultVal = computeBusBvsAndTotalError(lambda, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, busVal, bvsVal, totalErrorVal);
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                         blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                         windowSize, iterationsLK, imageHeight, imageWidth, iterationsLS, lambda);
        
        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);
        
        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);
        
        
        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, false);

        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
            
            public IDenseLiuShenListener setImages(IImage _img1, IImage _img2) {
                img1 = _img1;
                img2 = _img2;
                return this;
            }
            
            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer1 = validateImgBuffer("img2", img2, imgBuffer, pixelI, pixelJ, false);
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer2 = validateImgBuffer("img1", img1, imgBuffer, pixelI, pixelJ, false);                
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }
                        
                        float pixelValue1 = getNearestPixelWithWarp(img1, sourceImgI, sourceImgJ);
                        float pixelValue2 = getNearestPixelWithWarp(img2, sourceImgI, sourceImgJ);
                        assertEquals("Img1 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue1, pixelValues[(i+1) * 3 + (j+1)], 1e-5f);
                        assertEquals("Img2 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue2, pixelValues[(i+1) * 3 + (j+1) + 9], 1e-5f);
                    }
                }
                
            }

            @Override
            public void fixedImageDerivativesComputed(float w, float IIx, float IIy, float II, float Ixt, float Iyt, float A11, float A12, float A22, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertEquals("IIx at I: " + sourceImgI + ", J: " + sourceImgJ, iixVal[sourceImgI][sourceImgJ], IIx  , 1e-5f);
                    assertEquals("IIy at I: " + sourceImgI + ", J: " + sourceImgJ, iiyVal[sourceImgI][sourceImgJ], IIy  , 1e-5f);
                    assertEquals("II  at I: " + sourceImgI + ", J: " + sourceImgJ, iiVal [sourceImgI][sourceImgJ], II   , 1e-5f);
                    assertEquals("Ixt at I: " + sourceImgI + ", J: " + sourceImgJ, ixtVal[sourceImgI][sourceImgJ], Ixt  , 1e-5f);
                    assertEquals("Iyt at I: " + sourceImgI + ", J: " + sourceImgJ, iytVal[sourceImgI][sourceImgJ], Iyt  , 1e-5f);
                    assertEquals("B00 at I: " + sourceImgI + ", J: " + sourceImgJ, b00Val[sourceImgI][sourceImgJ], Bs[0], 1e-5f);
                    assertEquals("B01 at I: " + sourceImgI + ", J: " + sourceImgJ, b01Val[sourceImgI][sourceImgJ], Bs[1], 1e-5f);
                    assertEquals("B11 at I: " + sourceImgI + ", J: " + sourceImgJ, b11Val[sourceImgI][sourceImgJ], Bs[2], 1e-5f);
                }
            }

            @Override
            public void vectorsRefined(int w, float huComposite, float hvComposite, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[], 
                                       int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {

                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertTrue("Weight should be one at I: " + sourceImgI + ", J: " + sourceImgJ, w == 1);
                    if (sourceImgI == 0 && sourceImgJ == 6) {
                        System.out.println("Here we are");
                    }
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int idx = (i + 1) * 3 + (j + 1);
                            float uLiuInVal = getValueComplete(usLiuInVal, sourceImgI + i, sourceImgJ + j, false);
                            float vLiuInVal = getValueComplete(vsLiuInVal, sourceImgI + i, sourceImgJ + j, false);
                            
                            //Intentionally swap usLiuIn (privUs) with usLiuIn (privVs) because Liu-Shen Aparapi internally uses the PIV/Liu-Shen coordinate system
                            //which is swapped with respect to the Lucas-Kanade OpF, or OpF.
                            assertEquals("usLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), uLiuInVal, vsLiuIn[idx], 1e-5f);
                            assertEquals("vsLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), vLiuInVal, usLiuIn[idx], 1e-5f);
                        }
                    }
                    assertEquals("bu at I: "         + sourceImgI + ", J: " + sourceImgJ, busVal       [sourceImgI][sourceImgJ], bu        , 5e-5f);
                    assertEquals("bv at I: "         + sourceImgI + ", J: " + sourceImgJ, bvsVal       [sourceImgI][sourceImgJ], bv        , 5e-5f);
                    //Intentionally swap usNew with vsNew because Liu-Shen Aparapi internally uses the PIV/Liu-Shen coordinate system
                    //which is swapped with respect to the Lucas-Kanade OpF, or OpF.
                    assertEquals("usNew at I: "      + sourceImgI + ", J: " + sourceImgJ, usLiuOutVal  [sourceImgI][sourceImgJ], vnew      , 1e-5f);
                    assertEquals("vsNew at I: "      + sourceImgI + ", J: " + sourceImgJ, vsLiuOutVal  [sourceImgI][sourceImgJ], unew      , 1e-5f);
                    assertEquals("totalErrot at I: " + sourceImgI + ", J: " + sourceImgJ, totalErrorVal[sourceImgI][sourceImgJ], totalError, 1e-5f);
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = 0; i < usLiuOutVal.length; i++) {
                    for (int j = 0; j < usLiuOutVal[0].length; j++) {
                        int idx = i * usLiuOutVal[0].length + j;
                        assertEquals("us at I: " + i + ", J: " + j, usLiuOutVal[i][j], us[idx], 1e-5f);
                        assertEquals("vs at I: " + i + ", J: " + j, vsLiuOutVal[i][j], vs[idx], 1e-5f);
                    }
                }                
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                assertEquals("totalError does not match", totalErrorResultVal, totalErrorGlobal[0], 1e-5f);
            }
        }.setImages(imgLS1, imgLS2));

        try {
            kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 2);
        } catch (AparapiKernelFailedException ex) {
            if (ex.getCause().getClass().equals(AssertionError.class)) {
                throw ex.getCause();
            }
            throw ex;
        }
    }

    @Test
    public void testImageToLocalImgBufferOffsetTestPass() throws Throwable {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];
        

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

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        
        img1 = new ImageFloat(region1, imageHeight, imageWidth, "img1.png");
        img2 = new ImageFloat(region2, imageHeight, imageWidth, "img2.png");

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);

        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);
        
        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];

        float[][] iixVal = new float[imageHeight][imageWidth];
        float[][] iiyVal = new float[imageHeight][imageWidth];
        float[][] iiVal = new float[imageHeight][imageWidth];
        float[][] ixtVal = new float[imageHeight][imageWidth];
        float[][] iytVal = new float[imageHeight][imageWidth];
        float[][] b00Val = new float[imageHeight][imageWidth];
        float[][] b01Val = new float[imageHeight][imageWidth];
        float[][] b11Val = new float[imageHeight][imageWidth];
        float[][] usLiuInVal = new float[imageHeight][imageWidth];
        float[][] vsLiuInVal = new float[imageHeight][imageWidth];
        float[][] usLiuOutVal = new float[imageHeight][imageWidth];
        float[][] vsLiuOutVal = new float[imageHeight][imageWidth];
        float[][] busVal = new float[imageHeight][imageWidth];
        float[][] bvsVal = new float[imageHeight][imageWidth];
        float[][] totalErrorVal = new float[imageHeight][imageWidth];
        final float lambda = 4.0f;
        int iterationsLK = 5;
        int iterationsLS = 1;

        //Crucial initialization for the JTP test to have any meaningful result, otherwise Liu-Shen will end before the first iteration
        totalError[0] = 1e8f;
        
        computeValidationUsAndVs(img1, img2, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, usLiuOutVal, vsLiuOutVal, iterationsLK, iterationsLS, lambda, true);
        
        float totalErrorResultVal = computeBusBvsAndTotalError(lambda, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, busVal, bvsVal, totalErrorVal);
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                         blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                         windowSize, iterationsLK, imageHeight, imageWidth, iterationsLS, lambda);
        
        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);
        
        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);
        
        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, true);

        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
    
            public IDenseLiuShenListener setImages(IImage _img1, IImage _img2) {
                img1 = _img1;
                img2 = _img2;
                return this;
            }
            
            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer2 = validateImgBuffer("img2", img2, imgBuffer, pixelI, pixelJ, true);
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer1 = validateImgBuffer("img1", img1, imgBuffer, pixelI, pixelJ, true);                
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }

                        float pixelValue1 = getNearestPixelWithWarp(img1, sourceImgI + 0.5f, sourceImgJ + 0.5f);
                        float pixelValue2 = getNearestPixelWithWarp(img2, sourceImgI + 0.5f, sourceImgJ + 0.5f);
                        assertEquals("Img1 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue1, pixelValues[(i+1) * 3 + (j+1)], 1e-3f);
                        assertEquals("Img2 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue2, pixelValues[(i+1) * 3 + (j+1) + 9], 1e-3f);
                    }
                }                
            }

            @Override
            public void fixedImageDerivativesComputed(float w, float IIx, float IIy, float II, float Ixt, float Iyt, float A11, float A12, float A22, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
                
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertEquals("IIx at I: " + sourceImgI + ", J: " + sourceImgJ, iixVal[sourceImgI][sourceImgJ], IIx  , 1e-5f);
                    assertEquals("IIy at I: " + sourceImgI + ", J: " + sourceImgJ, iiyVal[sourceImgI][sourceImgJ], IIy  , 1e-5f);
                    assertEquals("II  at I: " + sourceImgI + ", J: " + sourceImgJ, iiVal [sourceImgI][sourceImgJ], II   , 1e-5f);
                    assertEquals("Ixt at I: " + sourceImgI + ", J: " + sourceImgJ, ixtVal[sourceImgI][sourceImgJ], Ixt  , 1e-5f);
                    assertEquals("Iyt at I: " + sourceImgI + ", J: " + sourceImgJ, iytVal[sourceImgI][sourceImgJ], Iyt  , 1e-5f);
                    assertEquals("B00 at I: " + sourceImgI + ", J: " + sourceImgJ, b00Val[sourceImgI][sourceImgJ], Bs[0], 1e-5f);
                    assertEquals("B01 at I: " + sourceImgI + ", J: " + sourceImgJ, b01Val[sourceImgI][sourceImgJ], Bs[1], 1e-5f);
                    assertEquals("B11 at I: " + sourceImgI + ", J: " + sourceImgJ, b11Val[sourceImgI][sourceImgJ], Bs[2], 1e-5f);
                }
            }

            @Override
            public void vectorsRefined(int w, float huComposite, float hvComposite, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[],
                                       int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertTrue("Weight should be one at I: " + sourceImgI + ", J: " + sourceImgJ, w == 1);
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int idx = (i + 1) * 3 + (j + 1);
                            float uLiuInVal = getValueComplete(usLiuInVal, sourceImgI + i, sourceImgJ + j, false);
                            float vLiuInVal = getValueComplete(vsLiuInVal, sourceImgI + i, sourceImgJ + j, false);

                            //Intentionally swap usLiuIn (privUs) with usLiuIn (privVs) because Liu-Shen Aparapi internally uses the PIV/Liu-Shen coordinate system
                            //which is swapped with respect to the Lucas-Kanade OpF, or OpF.
                            assertEquals("usLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), uLiuInVal, vsLiuIn[idx], 1e-5f);
                            assertEquals("vsLiuIn at I: "    + (sourceImgI+i) + ", J: " + (sourceImgJ+j), vLiuInVal, usLiuIn[idx], 1e-5f);
                        }
                    }
                    assertEquals("bu at I: "         + sourceImgI + ", J: " + sourceImgJ, busVal       [sourceImgI][sourceImgJ], bu        , 5e-5f);
                    assertEquals("bv at I: "         + sourceImgI + ", J: " + sourceImgJ, bvsVal       [sourceImgI][sourceImgJ], bv        , 5e-5f);
                    //Intentionally swap usLiuIn (privUs) with usLiuIn (privVs) because Liu-Shen Aparapi internally uses the PIV/Liu-Shen coordinate system
                    //which is swapped with respect to the Lucas-Kanade OpF, or OpF.                    
                    assertEquals("usNew at I: "      + sourceImgI + ", J: " + sourceImgJ, usLiuOutVal  [sourceImgI][sourceImgJ], vnew      , 1e-5f);
                    assertEquals("vsNew at I: "      + sourceImgI + ", J: " + sourceImgJ, vsLiuOutVal  [sourceImgI][sourceImgJ], unew      , 1e-5f);
                    assertEquals("totalErrot at I: " + sourceImgI + ", J: " + sourceImgJ, totalErrorVal[sourceImgI][sourceImgJ], totalError, 1e-5f);
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }                
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, 
                                      int iterLS, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                for (int i = 0; i < usLiuOutVal.length; i++) {
                    for (int j = 0; j < usLiuOutVal[0].length; j++) {
                        int idx = i * usLiuOutVal[0].length + j;
                        assertEquals("us at I: " + i + ", J: " + j, usLiuOutVal[i][j], us[idx], 1e-5f);
                        assertEquals("vs at I: " + i + ", J: " + j, vsLiuOutVal[i][j], vs[idx], 1e-5f);
                    }
                }
                
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                assertEquals("totalError does not match", totalErrorResultVal, totalErrorGlobal[0], 1e-5f);
            }
        }.setImages(imgLS1, imgLS2));

        try {
            kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 2);
        } catch (AparapiKernelFailedException ex) {
            if (ex.getCause().getClass().equals(AssertionError.class)) {
                throw ex.getCause();
            }
            throw ex;
        }
    }

    public float getNearestPixel(IImage img, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= img.getHeight()) {
            i = img.getHeight() - 1;
        }
        
        if (j < 0) {
            j = 0;
        }
        if (j >= img.getWidth()) {
            j = img.getWidth() - 1;
        }
        
        return img.readPixel(i, j);
    }
    
    public float getNearestPixelWithWarp(IImage img, float locI, float locJ) {
        int i = (int) locI;
        int j = (int) locJ;
        
        float deltaI = locI - i;
        float deltaJ = locJ - j;
        
        if (deltaI < 0) {
            i--;
            deltaI += 1.0;
        }
        
        if (deltaJ < 0) {
            j--;
            deltaJ += 1.0;
        }
        
        float value = (1.0f - deltaI) * ((1.0f - deltaJ) * getNearestPixel(img, i  ,j) + deltaJ * getNearestPixel(img, i  ,j+1)) + 
                              deltaI  * ((1.0f - deltaJ) * getNearestPixel(img, i+1,j) + deltaJ * getNearestPixel(img, i+1,j+1));
        
        return value;
    }
    
    protected float[][] validateImgBuffer(String title, IImage img, float[] imgBuffer, int pixelI, int pixelJ, boolean offsetPixel) {
        float[][] validationBuffer = new float[(localSizeI + 2)][(localSizeJ + 2)];
        
        float offset = 0.0f;
        if (offsetPixel) {
            offset = 0.5f;
        }
        
        for (int i = 0; i < localSizeI + 2; i++) {
            for (int j = 0; j < localSizeJ + 2; j++) {
                validationBuffer[i][j] = getNearestPixelWithWarp(img, pixelI + i + offset, pixelJ + j + offset);
                if (i == -1) {
                    throw new Error("Severe memory corruption");
                }
                assertEquals("Pixel value does not match for " + title + " at I: " + i + ", J:" + j, validationBuffer[i][j], imgBuffer[i * (localSizeJ + 2) + j], 1e-3f);
            }
        }
        
        return validationBuffer;
    }

    void computeValidationUsAndVs(IImage img1, IImage img2, float[][] IIx, float[][] IIy, float[][] II, float[][] Ixt, float[][] Iyt, float[][] B00, float[][] B01, float[][] B11, 
                                                            float[][] usVal, float[][] vsVal, float[][] usInOut, float[][] vsInOut, int iterationsLK, int iterationsLS, float lambda, boolean offset) {
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
                vs[idx] = vsInOut[i][j];
            }
        }
        
        KernelManager.setKernelManager(new GPUKernelManager());
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = iterationsLK;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = offset;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                usVal[i][j] = us[idx];
                vsVal[i][j] = vs[idx];
            }
        }
        
        PIVContextTestsSingleton.setSingleton();
        LiuShenInterpolatorConfiguration lsConfig = new LiuShenInterpolatorConfiguration();
        lsConfig.setFilterSigmaLK(2.0f);
        lsConfig.setFilterWidthPxLK(3);
        lsConfig.setNumberOfIterationsLK(iterationsLK);
        lsConfig.setWindowSizeLK(27);
        lsConfig.setFilterSigmaLS(0.48f);
        lsConfig.setFilterWidthPxLS(5);
        lsConfig.setMultiplierLagrangeLS(lambda);
        lsConfig.setNumberOfIterationsLS(iterationsLS);
        lsConfig.setVectorsWindowSizeLS(33);
        lsConfig.setDenseVectors(offset ? false : true);
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsConfig);
        
        LiuShenFloat liuShenValidation = new LiuShenFloat();
        liuShenValidation.updateImageA(img1);
        liuShenValidation.updateImageB(img2);
        liuShenValidation.computeFromVelocities(us, vs);

        float IIxL []   = liuShenValidation.getIIx();
        float IIyL []   = liuShenValidation.getIIy();
        float IIL  []   = liuShenValidation.getII();
        float IxtL []   = liuShenValidation.getIxt();
        float IytL []   = liuShenValidation.getIyt();
        float BsL  [][] = liuShenValidation.getBs();
        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                usInOut[i][j] = us[idx];
                vsInOut[i][j] = vs[idx];
                IIx[i][j]     = IIxL[idx];
                IIy[i][j]     = IIyL[idx];
                II[i][j]      = IIL[idx];
                Ixt[i][j]     = IxtL[idx];
                Iyt[i][j]     = IytL[idx];
                B00[i][j]     = BsL[idx][0];
                B01[i][j]     = BsL[idx][1];
                B11[i][j]     = BsL[idx][2];
            }
        }
        
        KernelManager.setKernelManager(new JTPKernelManager());
    }

    @FunctionalInterface
    private interface GVec {
        float get(int i, int j);
    }
    
    private final float getValueComplete(float[][] arr, int i, int j, boolean isWeigh) {
        float w = 1.0f;
        
        if (i < 0) {
            i = 0;
            if (isWeigh)
                w = 0.0f;
        }
        if (i >= arr.length) {
            i = arr.length - 1;
            if (isWeigh)
                w = 0.0f;
        }
        if (j < 0) {
            j = 0;
            if (isWeigh)
                w = 0.0f;

        }
        if (j >= arr[0].length) {
            j = arr[0].length - 1;
            if (isWeigh)
                w = 0.0f;
        }
        
        return arr[i][j]*w;
    }

    
    public float computeBusBvsAndTotalError(float lambda, float IIx[][], float[][] IIy, float[][] II, float[][] Ixt, float[][] Iyt, 
                                           float B00[][], float B01[][], float B11[][],
                                           float us[][], float vs[][],
                                           float bus[][], float bvs[][], float totalError[][]) {
        //Intentionally swapped because Liu-Shen U,V velocity components are swapped with respect to Lucas-Kanade
        final GVec gus = (i, j) -> getValueComplete(vs, i, j, false);
        final GVec gvs = (i, j) -> getValueComplete(us, i, j, false);
        final GVec gusW = (i, j) -> getValueComplete(vs, i, j, true);
        final GVec gvsW = (i, j) -> getValueComplete(us, i, j, true);
        
        float totalErrorResult = 0.0f;
        
        for (int i = 0; i < B00.length; i++) {
            for (int j = 0; j < B00[0].length; j++) {                
                //
                float bu = 2.0f * IIx[i][j] * (gus.get(i+1, j  ) - gus.get(i-1, j  )) / 2.0f + 
                                  IIx[i][j] * (gvs.get(i  , j+1) - gvs.get(i  , j-1)) / 2.0f +
                                  IIy[i][j] * (gvs.get(i+1, j  ) - gvs.get(i-1, j  )) / 2.0f + 
                                   II[i][j] * (gus.get(i-1, j  ) + gus.get(i+1, j  )) +
                                   II[i][j] * (gvs.get(i-1, j-1) - gvs.get(i-1, j+1) - gvs.get(i+1, j-1) + gvs.get(i+1, j+1)) / 4.0f +
                                    lambda * (gusW.get(i-1, j-1) + gusW.get(i-1, j  ) + gusW.get(i-1, j+1) +
                                              gusW.get(i  , j-1) + gusW.get(i  , j+1) +
                                              gusW.get(i+1, j-1) + gusW.get(i+1, j  ) + gusW.get(i+1, j+1)) + Ixt[i][j];
                
                float bv = 2.0f * IIy[i][j] * (gvs.get(i  , j+1) - gvs.get(i  , j-1)) / 2.0f +
                                  IIy[i][j] * (gus.get(i+1, j  ) - gus.get(i-1, j  )) / 2.0f +
                                  IIx[i][j] * (gus.get(i  , j+1) - gus.get(i  , j-1)) / 2.0f +                                       
                                   II[i][j] * (gvs.get(i  , j-1) + gvs.get(i  , j+1)) +
                                   II[i][j] * (gus.get(i-1, j-1) - gus.get(i-1, j+1) - gus.get(i+1, j-1) + gus.get(i+1, j+1)) / 4.0f +
                                    lambda * (gvsW.get(i-1, j-1) + gvsW.get(i-1, j  ) + gvsW.get(i-1, j+1) +
                                              gvsW.get(i  , j-1) + gvsW.get(i  , j+1) +
                                              gvsW.get(i+1, j-1) + gvsW.get(i+1, j  ) + gvsW.get(i+1, j+1)) + Iyt[i][j];

                bus[i][j] = bu;
                bvs[i][j] = bv;
                
                float unew = (float)-(B00[i][j]*bu + B01[i][j]*bv);
                float vnew = (float)-(B01[i][j]*bu + B11[i][j]*bv);
                
                totalError[i][j] = (float)FastMath.sqrt((unew - gus.get(i, j))*(unew - gus.get(i, j)) + (vnew - gvs.get(i, j))*(vnew - gvs.get(i, j)));
                totalErrorResult += totalError[i][j];                    
            }                     
        }

        totalErrorResult /= (float)(B00.length * B00[0].length);
        
        return totalErrorResult;
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
