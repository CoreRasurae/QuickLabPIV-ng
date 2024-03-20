// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.SimpleLucasKanadeImpl;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeGpuTestKernel.IDenseLucasKanadeListener;

public class DenseLucasKanadeGpuJTPTest {
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
    private int iterations;
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
    
    private class DefaultKernelManager extends KernelManager {
        
    }
    
    private float[][] clipToLocalBufferHelper(Matrix region1, int locI, int locJ) {
        float testImgBuffer[][] = new float[windowSize + blockSizeI-1 + 2][windowSize + blockSizeJ-1 + 2];
        for (int i = 0; i < windowSize + blockSizeI-1 + 2; i++) {
            for (int j = 0; j < windowSize + blockSizeJ-1 + 2; j++) {
                int imgLocI = locI + i - windowSize/2 - 1;
                int imgLocJ = locJ + j - windowSize/2 - 1;
                if (imgLocI < 0) {
                    imgLocI = 0;
                }
                if (imgLocI >= imageHeight) {
                    imgLocI = imageHeight - 1;
                }
                if (imgLocJ < 0) {
                    imgLocJ = 0;
                }
                if (imgLocJ >= imageWidth) {
                    imgLocJ = imageWidth - 1;
                }
                testImgBuffer[i][j] = region1.getElement(imgLocI, imgLocJ);
            }
        }
        
        return testImgBuffer;
    }

    public float getNearestPixel(Matrix mat, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= mat.getHeight()) {
            i = mat.getHeight() - 1;
        }
        
        if (j < 0) {
            j = 0;
        }
        if (j >= mat.getWidth()) {
            j = mat.getWidth() - 1;
        }
        
        return mat.getElement(i, j);
    }
    
    public float getNearestPixelWithWarp(Matrix mat, float locI, float locJ) {
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
        
        float value = (1.0f - deltaI) * ((1.0f - deltaJ) * getNearestPixel(mat, i,j) + deltaJ * getNearestPixel(mat, i,j+1)) + 
                              deltaI  * ((1.0f - deltaJ) * getNearestPixel(mat, i+1,j) + deltaJ * getNearestPixel(mat, i+1,j+1));
        
        return value;
    }

    private float[][] clipToLocalBufferHalfPixelHelper(Matrix region1, int locI, int locJ) {
        float testImgBuffer[][] = new float[windowSize + blockSizeI-1 + 2][windowSize + blockSizeJ-1 + 2];
        for (int i = 0; i < windowSize + blockSizeI-1 + 2; i++) {
            for (int j = 0; j < windowSize + blockSizeJ-1 + 2; j++) {
                int imgLocI = locI + i - windowSize/2 - 1;
                int imgLocJ = locJ + j - windowSize/2 - 1;
                testImgBuffer[i][j] = getNearestPixelWithWarp(region1, imgLocI + 0.5f, imgLocJ + 0.5f);
            }
        }
        
        return testImgBuffer;
    }

    private void checkPass(float[][] testImgBuffer, float[] localImgBuffer) {
        for (int i = 0; i < windowSize + blockSizeI-1 + 2; i++) {
            for (int j = 0; j < windowSize + blockSizeJ-1 + 2; j++) {
                int idx = i * (windowSize + blockSizeJ-1 + 2) + j;
                assertEquals("localImgBuffer value does not match the expected at I:" + i+ ", J:" + j, testImgBuffer[i][j], localImgBuffer[idx], 1e-8f);
            }
        }
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
        iterations = 1;
        
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
    public void testImageToLocalImgBufferDirectTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);

        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region2.copyMatrixToArray(imageB, 0);

        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        
        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                                 blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                                 windowSize, iterations, imageHeight, imageWidth);
        kernel.registerListener(new IDenseLucasKanadeListener() {
            
            @Override
            public void imageRegionRead(float[] localImgBuffer, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ,
                    int localIdI, int localIdJ) {
                
                int baseLocI = groupIdI * blockSizeI;
                int baseLocJ = groupIdJ * blockSizeJ;
                
                float testImageBuffer[][] = clipToLocalBufferHelper(region1, baseLocI, baseLocJ);
                checkPass(testImageBuffer, localImgBuffer);
            }

            @Override
            public void matrixAComputed(float[] A00, float[] A01, float[] A11, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                
            }

            @Override
            public void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                
            }

            @Override
            public void matrixBComputed(float[][] b0s, float[][] b1s, int iter, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                
            }

            @Override
            public void pixelValuesAndDIsAndDJsComputed(float[][][][] pixelValues, float[][][][] dIs, float[][][][] dJs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                
            }
        });
        
        kernel.setKernelArgs(imageA, imageB, us, vs, false);
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI));

    }

    @Test
    public void testImageToLocalImgBufferHalfPixelTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);

        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region2.copyMatrixToArray(imageB, 0);

        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];

        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ, 
                                                                                 blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                                 windowSize, iterations, imageHeight, imageWidth);
        kernel.registerListener(new IDenseLucasKanadeListener() {
            
            @Override
            public void imageRegionRead(float[] localImgBuffer, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ,
                    int localIdI, int localIdJ) {
                
                int baseLocI = groupIdI * blockSizeI;
                int baseLocJ = groupIdJ * blockSizeJ;
                
                float testImageBuffer[][] = clipToLocalBufferHalfPixelHelper(region1, baseLocI, baseLocJ);
                checkPass(testImageBuffer, localImgBuffer);
            }

            @Override
            public void matrixAComputed(float[] A00, float[] A01, float[] A11, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
            }

            @Override
            public void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
            }

            @Override
            public void matrixBComputed(float[][] b0s, float[][] b1s, int iter, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                
            }

            @Override
            public void pixelValuesAndDIsAndDJsComputed(float[][][][] pixelValues, float[][][][] dIs, float[][][][] dJs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                
            }
        });
        
        kernel.setKernelArgs(imageA, imageB, us, vs, true);
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI));

    }


    @Test
    public void testLocalImgBufferDirectToMatrixATestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);

        final boolean halfPixelOffset = false;
        
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
        
        float A   [][][] = new float[blockSizeI + 1][blockSizeJ + 1][3];
        float AInv[][][] = new float[blockSizeI + 1][blockSizeJ + 1][3];
        float B0s[][]    = new float[blockSizeI + 1][blockSizeJ + 1];
        float B1s[][]    = new float[blockSizeI + 1][blockSizeJ + 1];
        float DIs[][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        float DJs[][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        float patch1[][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        for (int i = 0; i < blockSizeI + 1; i++) {
            for (int j = 0; j < blockSizeJ + 1; j++) {
                double tempAInv[][] = new double[4][3];
                double tempB0s[] = new double[4];
                double tempB1s[] = new double[4];
                double tempDIs[][][] = new double[4][windowSize][windowSize];
                double tempDJs[][][] = new double[4][windowSize][windowSize];
                double tempPatch1[][][] = new double[4][windowSize][windowSize];
                double tempA[][] = computeTestMatrixAAtLoc(img1, img2, i, j, tempAInv, tempDIs, tempDJs, tempPatch1, tempB0s, tempB1s, halfPixelOffset);
                A[i][j][0] = (float)tempA[0][0];
                A[i][j][1] = (float)tempA[0][1];
                A[i][j][2] = (float)tempA[0][2];

                AInv[i][j][0] = (float)tempAInv[0][0];
                AInv[i][j][1] = (float)tempAInv[0][1];
                AInv[i][j][2] = (float)tempAInv[0][2];
                
                B0s[i][j] = (float)tempB0s[0];
                B1s[i][j] = (float)tempB1s[0];
                
                for (int wi = 0; wi < windowSize; wi++) {
                    for (int wj = 0; wj < windowSize; wj++) {
                        DIs[i][j][wi][wj] = (float)tempDIs[0][wi][wj];
                        DJs[i][j][wi][wj] = (float)tempDJs[0][wi][wj];
                        patch1[i][j][wi][wj] = (float)tempPatch1[0][wi][wj];
                    }
                }
            }
        }
        
        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);

        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region2.copyMatrixToArray(imageB, 0);
        
        float us[] = new float[imageWidth * imageHeight];
        float vs[] = new float[imageWidth * imageHeight];
        
        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                                 blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                                 windowSize, iterations, imageHeight, imageWidth);
        kernel.registerListener(new IDenseLucasKanadeListener() {
            
            @Override
            public void imageRegionRead(float[] localImgBuffer, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ,
                    int localIdI, int localIdJ) {
                
                int baseLocI = groupIdI * blockSizeI;
                int baseLocJ = groupIdJ * blockSizeJ;
                
                float testImageBuffer[][] = clipToLocalBufferHelper(region1, baseLocI, baseLocJ);
                checkPass(testImageBuffer, localImgBuffer);
            }

            @Override
            public void matrixAComputed(float[] A00, float[] A01, float[] A11, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;
                for (int i = 0; i < blockSizeI; i++) {
                    for (int j = 0; j < blockSizeJ; j++) {
                        if (locI + i < A.length && locJ + j < A[0].length) {
                            checkAMatrices(locI + i, locJ + j, A[locI + i][locJ + j], A00[i * blockSizeJ + j], A01[i * blockSizeJ + j], A11[i * blockSizeJ + j], false);
                        }
                    }
                }
            }

            @Override
            public void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {

                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;
                
                int i = localIdI;
                int j = localIdJ;
                if (i < blockSizeI && j < blockSizeJ && locI + i < A.length && locJ + j < A[0].length) {
                    checkAMatrices(locI + i, locJ + j, AInv[locI + i][locJ + j], A00[i * blockSizeJ + j], A01[i * blockSizeJ + j], A11[i * blockSizeJ + j], true);
                }
            }

            @Override
            public void matrixBComputed(float[][] b0s, float[][] b1s, int iter, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                
            }

            @Override
            public void pixelValuesAndDIsAndDJsComputed(float[][][][] pixelValues, float[][][][] dIs, float[][][][] dJs,
                    int idI, int idJ, int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;

                for (int i = 0; i < blockSizeI; i++) {
                    for (int j = 0; j < blockSizeJ; j++) {
                        if (locI + i < patch1.length && locJ + j < patch1[0].length) {
                            checkMatrices("pixelValues", locI + i, locJ + j, i, j, patch1, pixelValues);
                            checkMatrices("dI", locI + i, locJ + j, i, j, DIs, dIs);
                            checkMatrices("dJ", locI + i, locJ + j, i, j, DJs, dJs);
                        }
                    }
                }
            }
        });
        
        kernel.setKernelArgs(imageA, imageB, us, vs, halfPixelOffset);
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI));

    }

    @Test
    public void testLocalImgBufferDirectToMatrixBTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);

        final boolean halfPixelOffset = false;
        
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
        
        float A     [][][]    = new float[blockSizeI + 1][blockSizeJ + 1][3];
        float AInv  [][][]    = new float[blockSizeI + 1][blockSizeJ + 1][3];
        float B0s   [][]      = new float[blockSizeI + 1][blockSizeJ + 1];
        float B1s   [][]      = new float[blockSizeI + 1][blockSizeJ + 1];
        float DIs   [][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        float DJs   [][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        float patch1[][][][]  = new float[blockSizeI + 1][blockSizeJ + 1][windowSize][windowSize];
        for (int i = 0; i < blockSizeI + 1; i++) {
            for (int j = 0; j < blockSizeJ + 1; j++) {
                double tempAInv[][] = new double[4][3];
                double tempB0s[] = new double[4];
                double tempB1s[] = new double[4];
                double tempDIs[][][] = new double[4][windowSize][windowSize];
                double tempDJs[][][] = new double[4][windowSize][windowSize];
                double tempPatch1[][][] = new double[4][windowSize][windowSize];
                double tempA[][] = computeTestMatrixAAtLoc(img1, img2, i, j, tempAInv, tempDIs, tempDJs, tempPatch1, tempB0s, tempB1s, halfPixelOffset);
                A[i][j][0] = (float)tempA[0][0];
                A[i][j][1] = (float)tempA[0][1];
                A[i][j][2] = (float)tempA[0][2];

                AInv[i][j][0] = (float)tempAInv[0][0];
                AInv[i][j][1] = (float)tempAInv[0][1];
                AInv[i][j][2] = (float)tempAInv[0][2];
                
                B0s[i][j] = (float)tempB0s[0];
                B1s[i][j] = (float)tempB1s[0];
                
                for (int wi = 0; wi < windowSize; wi++) {
                    for (int wj = 0; wj < windowSize; wj++) {
                        DIs[i][j][wi][wj] = (float)tempDIs[0][wi][wj];
                        DJs[i][j][wi][wj] = (float)tempDJs[0][wi][wj];
                        patch1[i][j][wi][wj] = (float)tempPatch1[0][wi][wj];
                    }
                }
            }
        }
                        
        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);

        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region2.copyMatrixToArray(imageB, 0);
        
        float us[] = new float[imageWidth * imageHeight];
        float vs[] = new float[imageWidth * imageHeight];
        
        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                                 blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                                 windowSize, iterations, imageHeight, imageWidth);
        kernel.registerListener(new IDenseLucasKanadeListener() {
            
            @Override
            public void imageRegionRead(float[] localImgBuffer, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ,
                    int localIdI, int localIdJ) {
                
                int baseLocI = groupIdI * blockSizeI;
                int baseLocJ = groupIdJ * blockSizeJ;
                
                float testImageBuffer[][] = clipToLocalBufferHelper(region1, baseLocI, baseLocJ);
                checkPass(testImageBuffer, localImgBuffer);
            }

            @Override
            public void matrixAComputed(float[] A00, float[] A01, float[] A11, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;
                for (int i = 0; i < blockSizeI; i++) {
                    for (int j = 0; j < blockSizeJ; j++) {
                        if (locI + i < A.length && locJ + j < A[0].length) {
                            checkAMatrices(locI + i, locJ + j, A[locI + i][locJ + j], A00[i * blockSizeJ + j], A01[i * blockSizeJ + j], A11[i * blockSizeJ + j], halfPixelOffset);
                        }
                    }
                }
            }

            @Override
            public void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA, int globalIdI, int globalIdJ,
                    int groupIdI, int groupIdJ, int localIdI, int localIdJ) {

                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;
                
                int i = localIdI;
                int j = localIdJ;
                if (i < blockSizeI && j < blockSizeJ && locI + i < A.length && locJ + j < A[0].length) {
                    checkAMatrices(locI + i, locJ + j, AInv[locI + i][locJ + j], A00[i * blockSizeJ + j], A01[i * blockSizeJ + j], A11[i * blockSizeJ + j], true);
                }
            }

            @Override
            public void matrixBComputed(float[][] b0s, float[][] b1s, int iteration, int idI, int idJ, int groupIdI, int groupIdJ, int localIdI,
                    int localIdJ) {
                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;
                
                checkBMatrices(locI, locJ, B0s, B1s, b0s, b1s);
            }

            @Override
            public void pixelValuesAndDIsAndDJsComputed(float[][][][] pixelValues, float[][][][] dIs, float[][][][] dJs,
                    int idI, int idJ, int groupIdI, int groupIdJ, int localIdI, int localIdJ) {
                int locI = groupIdI * blockSizeI;
                int locJ = groupIdJ * blockSizeJ;

                for (int i = 0; i < blockSizeI; i++) {
                    for (int j = 0; j < blockSizeJ; j++) {
                        if (locI + i < patch1.length && locJ + j < patch1[0].length) {
                            checkMatrices("pixelValues", locI + i, locJ + j, i, j, patch1, pixelValues);
                            checkMatrices("dI", locI + i, locJ + j, i, j, DIs, dIs);
                            checkMatrices("dJ", locI + i, locJ + j, i, j, DJs, dJs);
                        }
                    }
                }
            }
        });
        
        kernel.setKernelArgs(imageA, imageB, us, vs, halfPixelOffset);
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI));

    }

    
    @Test
    public void testCompleteLucasKanadeTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);

        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];

        globalSizeI = imageHeight;
        if (imageHeight % localSizeI != 0) {
            globalSizeI = (imageHeight / localSizeI + 1) * localSizeI;
        }
        globalSizeJ = imageWidth;
        if (imageWidth % localSizeJ != 0) {
            globalSizeJ = (imageWidth / localSizeJ + 1) * localSizeJ;
        }
        
        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);

        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region2.copyMatrixToArray(imageB, 0);
        
        float testUs[][] = new float[blockSizeI + 1][blockSizeJ + 1];
        float testVs[][] = new float[blockSizeI + 1][blockSizeJ + 1];
        
        final boolean halfPixelOffset = false; 
        computeTestVelocities(img1, img2, testUs, testVs, halfPixelOffset);
        
        float us[] = new float[imageWidth * imageHeight];
        float vs[] = new float[imageWidth * imageHeight];
        
        DenseLucasKanadeGpuTestKernel kernel = new DenseLucasKanadeGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                                 blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                                 windowSize, iterations, imageHeight, imageWidth);

        kernel.setKernelArgs(imageA, imageB, us, vs, halfPixelOffset);
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI));
        us = kernel.getUs();
        vs = kernel.getVs();
        checkVelocities("us", testUs, us);
        checkVelocities("vs", testVs, vs);
    }

    private void checkVelocities(String title, float[][] testVelocities, float[] checkVelocities) {
        String msg = "Velocities " + title + " do not match at I:";
        for (int locI = 0; locI < blockSizeI + 1; locI++) {
            for (int locJ = 0; locJ < blockSizeJ + 1; locJ++) {
                assertEquals(msg + locI + ", J:" + locJ, testVelocities[locI][locJ], checkVelocities[locI * imageWidth + locJ], 1e-4f);
            }
        }
    }
    
    private void computeTestVelocities(IImage img1, IImage img2, float[][] testUs, float[][] testVs, boolean halfPixelOffset) {
        SimpleLucasKanadeImpl impl = new SimpleLucasKanadeImpl(0.0f, 3, false, windowSize, iterations);
        impl.updateImageA(img1);
        impl.updateImageB(img2);
        for (int locI = 0; locI < blockSizeI + 1; locI++) {
            for (int locJ = 0; locJ < blockSizeJ + 1; locJ++) {
                double velocities[] = impl.interpolate(locI, locJ, 0.0f, 0.0f, halfPixelOffset);
                testVs[locI][locJ] = (float)velocities[0];
                testUs[locI][locJ] = (float)velocities[1];
            }
        }
    }

    protected void checkMatrices(String title, int locI, int locJ, int checkLocI, int checkLocJ, float[][][][] testMatrix, float[][][][] checkMatrix) {
        float test[][] = testMatrix[locI][locJ];
        float check[][] = checkMatrix[checkLocI][checkLocJ];
        
        for (int i = 0; i < windowSize; i++) {
            for (int  j = 0; j < windowSize; j++) {
                assertEquals("Matrix at locI: " + locI + ", locJ:" + locJ + " named: "+ title + " does not match at I:" + i + ", J:" + j, test[i][j], check[i][j], 1e-3f);
            }
        }
        
    }

    protected void checkBMatrices(int locI, int locJ, float[][] testb0s, float[][] testb1s, float[][] b0s, float[][] b1s) {
        String errB0 = "Value for B0 does not match the expected at pixel location I:"+ locI + ", J: " + locJ;
        String errB1 = "Value for B1 does not match the expected at pixel location I:"+ locI + ", J: " + locJ;

        for (int i = 0; i < blockSizeI; i++) {
            for (int j = 0; j < blockSizeJ; j++) {
                if (locI + i < testb0s.length && locJ + j < testb0s[0].length) { 
                    assertEquals(errB0 + "block I:" + i + ", J:" + j, testb0s[locI + i][locJ + j], b0s[i][j], 1e-2);
                    assertEquals(errB1 + "block I:" + i + ", J:" + j, testb1s[locI + i][locJ + j], b1s[i][j], 1e-2);
                }
            }
        }

    }

    protected void checkAMatrices(int locI, int locJ, float[] testA, float A00, float A01, float A11, boolean inverted) {
        String errA00 = "Value for A" + (inverted ? "Inv" : "") + "00 does not match the expected at pixel location I:"+ locI + ", J: " + locJ;
        String errA01 = "Value for A" + (inverted ? "Inv" : "") + "01 does not match the expected at pixel location I:"+ locI + ", J: " + locJ;
        String errA11 = "Value for A" + (inverted ? "Inv" : "") + "11 does not match the expected at pixel location I:"+ locI + ", J: " + locJ;
        assertEquals(errA00, testA[0], A00, 1e-2);
        assertEquals(errA01, testA[1], A01, 1e-2);
        assertEquals(errA11, testA[2], A11, 1e-2);
    }
    
    private double[][] computeTestMatrixAAtLoc(IImage img1, IImage img2, double locI, double locJ, 
                                               double AInv[][], double[][][] DIs, double DJs[][][], double patch1[][][], double B0s[], double B1s[], boolean halfPixelOffset) {
        SimpleLucasKanadeImpl impl = new SimpleLucasKanadeImpl(0.0f, 3, false, windowSize, iterations);
        impl.updateImageA(img1);
        impl.updateImageB(img2);
        impl.interpolate(locI, locJ, 0.0f, 0.0f, halfPixelOffset);
        
        double A[][] = impl.getA();
        double Inv[][] = impl.getInvA();
        for (int idx = 0; idx < Inv.length; idx++) {
            AInv[idx][0] = Inv[idx][0];
            AInv[idx][1] = Inv[idx][1];
            AInv[idx][2] = Inv[idx][2];
        }
        
        double dIs[][][] = impl.getDIs();
        double dJs[][][] = impl.getDJs();
        double patchI[][][] = impl.getPatchI();
        for (int idx = 0; idx < dIs.length; idx++) {
            for (int i = 0; i < windowSize; i++) {
                for (int j = 0; j < windowSize; j++) {
                    DIs[idx][i][j] = dIs[idx][i][j];
                    DJs[idx][i][j] = dJs[idx][i][j];
                    patch1[idx][i][j] = patchI[idx][i][j];
                }
            }
        }
        
        double b1s[] = impl.getB1s();
        double b0s[] = impl.getB0s();
        for (int i = 0; i < b0s.length; i++) {
            B1s[i] = b1s[i];
            B0s[i] = b0s[i];
        }
        
        return A;
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
