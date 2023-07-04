package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.exporter.InvalidStateException;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.SimpleLucasKanadeImpl;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;

public class DenseLucasKanadeGpuBasicTest {
    private int blockItemsPerWorkGroupI;
    private int blockItemsPerWorkGroupJ;
    private int blockSizeI;
    private int blockSizeJ;
    private int windowSize;
    private int imageWidth;
    private int imageHeight;
    private float imageA[];
    private float localImgBuffer[];
    private float multiWorkBuffer[];
    //Per thread private memory
    private float pixelValues[];
    private float dJ[];
    private float dI[];
    private float A00[];
    private float A01[];
    private float A11[];
    //Threads private buffers allocation
    private float pixelValuesTs[][][];
    private float dJTs[][][];
    private float dITs[][][];
    private float A00Ts[][][];
    private float A01Ts[][][];
    private float A11Ts[][][];
    
    private final int threadsInI = 8;
    private final int threadsInJ = 8;
    
    private int globalId[] = new int[2];
    private int localId[] = new int[2];
    
    private int min(int v1, int v2) {
        return v1 < v2 ? v1 : v2;
    }
    
    private int max(int v1, int v2) {
        return v1 > v2 ? v1 : v2;
    }
    
    private float mad(float a, float b, float c) {
        return a * b + c;
    }
    
    private int getGlobalId(int idx) {
        return globalId[idx];
    }
    
    private int getLocalId(int idx) {
        return localId[idx];
    }

    private int getLocalSize(int idx) {
        if (idx == 0) {
            return threadsInI;
        } else {
            return threadsInJ;
        }
    }
    
    private void getImageRegionToLocaLBufferDirectWrapper(int baseIdI, int baseIdJ) {
        for (int tidI = 0; tidI < threadsInI; tidI++) {
            globalId[0] = baseIdI + tidI;
            localId[0] = tidI;
            for (int tidJ = 0; tidJ < threadsInJ; tidJ++) {                
                globalId[1] = baseIdJ + tidJ;
                localId[1] = tidJ;
                getImageRegionToLocaLBufferDirect();
            }
        }        
    }
    
    private void getImageRegionToLocaLBufferDirect() {
        int idI = getGlobalId(0);
        int idJ = getGlobalId(1);
        
        int tidI = getLocalId(0);
        int tidJ = getLocalId(1);

        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);
        
        //Check if we need an extra block to accommodate for the margin around the interest image region (for helping computing the derivatives)
        int localBlocksPerGroupI = blockItemsPerWorkGroupI * localSizeI >= windowSize + blockSizeI-1 + 2 ? blockItemsPerWorkGroupI : blockItemsPerWorkGroupI + 1;
        int localBlocksPerGroupJ = blockItemsPerWorkGroupJ * localSizeJ >= windowSize + blockSizeJ-1 + 2 ? blockItemsPerWorkGroupJ : blockItemsPerWorkGroupJ + 1;
        
        //Find out which threads need to be active in the last blocks
        int wBI = (localBlocksPerGroupI-1) * localSizeI + tidI >= windowSize + blockSizeI-1 + 2 ? 0 : 1;
        int wBJ = (localBlocksPerGroupJ-1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 + 2 ? 0 : 1;
        
        for (int bI = 0; bI < localBlocksPerGroupI; bI++) {
            int offsetI = bI * localSizeI;
            for (int bJ = 0; bJ < localBlocksPerGroupJ; bJ++) {
                int offsetJ = bJ * localSizeJ;
                if ((wBI == 1 || bI < localBlocksPerGroupI - 1) && (wBJ == 1 || bJ < localBlocksPerGroupJ - 1)) {
                    int idx = (offsetI + tidI) * (windowSize + blockSizeJ-1 + 2) + (offsetJ + tidJ);
                    int imIdx = max(min(idI + offsetI - windowSize/2 - 1, imageHeight-1), 0) * imageWidth + max(min(idJ + offsetJ - windowSize/2 - 1, imageWidth-1), 0);
                    localImgBuffer[idx] = imageA[imIdx];
                }
            }
        }
    }

    @Before
    public void setup() {
        imageWidth = 50;
        imageHeight = 50;
        imageA = new float[imageHeight * imageWidth];
        windowSize = 27;
        blockSizeI = 4;
        blockSizeJ = 4;
        blockItemsPerWorkGroupI = (windowSize + blockSizeI) / threadsInI;
        if ((windowSize + blockSizeI) % threadsInI != 0) {
            blockItemsPerWorkGroupI++;
        }
        blockItemsPerWorkGroupJ = (windowSize + blockSizeJ) / threadsInJ;
        if ((windowSize + blockSizeJ) % threadsInJ != 0) {
            blockItemsPerWorkGroupJ++;
        }
        localImgBuffer = new float[(windowSize + blockSizeI-1 + 2) * (windowSize + blockSizeJ-1 + 2)];
        
        multiWorkBuffer = new float[max(threadsInI * threadsInJ * blockSizeI * blockSizeJ * 4,
                                        (windowSize + blockSizeI + 3) * (windowSize + blockSizeJ + 3))];
        
        pixelValuesTs = new float[threadsInI][threadsInJ][blockItemsPerWorkGroupI*blockItemsPerWorkGroupJ * blockSizeI*blockSizeJ];
        dJTs          = new float[threadsInI][threadsInJ][blockItemsPerWorkGroupI*blockItemsPerWorkGroupJ * blockSizeI*blockSizeJ];
        dITs          = new float[threadsInI][threadsInJ][blockItemsPerWorkGroupI*blockItemsPerWorkGroupJ * blockSizeI*blockSizeJ];
        A00Ts         = new float[threadsInI][threadsInJ][blockSizeI*blockSizeJ];
        A01Ts         = new float[threadsInI][threadsInJ][blockSizeI*blockSizeJ];
        A11Ts         = new float[threadsInI][threadsInJ][blockSizeI*blockSizeJ];
    }
    
    @Test
    public void readImageRegionAtTheTopLeftBorderTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");                                      

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);
        
        int locI = 0;
        int locJ = 0;
        getImageRegionToLocaLBufferDirectWrapper(locI, locJ);
        
        float[][] testImgBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        checkPass(testImgBuffer, localImgBuffer);
    }

    @Test
    public void readImageRegionAtTheTopRightBorderTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");                                      

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);
        
        int locI = 0;
        int locJ = imageWidth - 1;
        getImageRegionToLocaLBufferDirectWrapper(locI, locJ);
        
        float[][] testImgBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        checkPass(testImgBuffer, localImgBuffer);
    }

    @Test
    public void readImageRegionAtTheBottomLeftBorderTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");                                      

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);
        
        int locI = imageHeight - 1;
        int locJ = 0;
        getImageRegionToLocaLBufferDirectWrapper(locI, locJ);
        
        float[][] testImgBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        checkPass(testImgBuffer, localImgBuffer);
    }

    @Test
    public void readImageRegionAtTheBottomRightBorderTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");                                      

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);
        
        int locI = imageHeight - 1;
        int locJ = imageWidth - 1;
        getImageRegionToLocaLBufferDirectWrapper(locI, locJ);
        
        float[][] testImgBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        checkPass(testImgBuffer, localImgBuffer);
    }
    
    @Test
    public void readImageRegionInTheInsideTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");                                      

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        region1.copyMatrixToArray(imageA, 0);
        
        int locI = 20;
        int locJ = 20;
        getImageRegionToLocaLBufferDirectWrapper(locI, locJ);
        
        float[][] testImgBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        checkPass(testImgBuffer, localImgBuffer);
    }

    @Test
    public void computeDerivatesDirectTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        img1 = ImageFloat.convertFrom(img1);
        img2 = ImageFloat.convertFrom(img2);
        img1 = filter.applyFilter(img1, img1);
        img2 = filter.applyFilter(img2, img2);

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);

        float A[][][] = new float[blockSizeI + 1][blockSizeJ + 1][3];
        for (int i = 0; i < blockSizeI + 1; i++) {
            for (int j = 0; j < blockSizeJ + 1; j++) {
                double tempA[][] = computeTestMatrixAAtLoc(img1, img2, i, j);
                A[i][j][0] = (float)tempA[0][0];
                A[i][j][1] = (float)tempA[0][1];
                A[i][j][2] = (float)tempA[0][2];
            }
        }
        
        int locI = 0;
        int locJ = 0;
        float[][] myBuffer = clipToLocalBufferHelper(region1, locI, locJ);
        for (int i = 0; i < myBuffer.length; i++) {
            for (int j = 0; j < myBuffer[0].length; j++) {
                localImgBuffer[i * (windowSize + blockSizeJ-1 + 2) + j] = myBuffer[i][j];
            }
        }
        
        float AOpt[] = new float[3];
        getImagePatchA(AOpt);
        computeDerivativesWrapper(locI, locJ);
        
        float sumsA00s[][] = new float[blockSizeI][blockSizeJ];
        float sumsA01s[][] = new float[blockSizeI][blockSizeJ];
        float sumsA11s[][] = new float[blockSizeI][blockSizeJ];
        
        for (int i = 0; i < threadsInI; i++) {
            for (int j = 0; j < threadsInJ; j++) {
                for (int blkIdxI = 0; blkIdxI < blockSizeI; blkIdxI++) {
                    for (int blkIdxJ = 0; blkIdxJ < blockSizeJ; blkIdxJ++) {
                        sumsA00s[blkIdxI][blkIdxJ] += A00Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ];
                        sumsA01s[blkIdxI][blkIdxJ] += A01Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ];
                        sumsA11s[blkIdxI][blkIdxJ] += A11Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ];
                    }
                }
            }
        }
        
        reduceAsWrapperPartA(locI, locJ);
        reduceAsWrapperPartB(locI, locJ);
        reduceAsWrapperPartC(locI, locJ);
        for (int i = 0; i < threadsInI; i++) {
            for (int j = 0; j < threadsInJ; j++) {
                for (int blkIdxI = 0; blkIdxI < blockSizeI; blkIdxI++) {
                    for (int blkIdxJ = 0; blkIdxJ < blockSizeJ; blkIdxJ++) {
                        assertEquals("A00 does not match at TID I:" + i + ", J:" + j + " - Block I:" + blkIdxI + ", J:" +blkIdxJ, 
                                     sumsA00s[blkIdxI][blkIdxJ], A00Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ], 1e-1);
                        assertEquals("A01 does not match at TID I:" + i + ", J:" + j + " - Block I:" + blkIdxI + ", J:" +blkIdxJ,
                                     sumsA01s[blkIdxI][blkIdxJ], A01Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ], 1e-1);
                        assertEquals("A11 does not match at TID I:" + i + ", J:" + j + " - Block I:" + blkIdxI + ", J:" +blkIdxJ,
                                     sumsA11s[blkIdxI][blkIdxJ], A11Ts[i][j][blkIdxI * blockSizeJ + blkIdxJ], 1e-1);
                    }
                }
            }
        }
        
        for (int i = 0; i < 4; i++) {
            System.out.println(A[i][0]);
        }
        
        for (int i = 0; i < blockSizeI; i++) {
            for (int j = 0; j < blockSizeJ; j++) {
                checkAMatrices(locI + i, locJ + j, A[locI + i][locJ + j], A00[i * blockSizeJ + j], A01[i * blockSizeJ + j], A11[i * blockSizeJ + j]);
            }
        }
    }
    
    protected void checkAMatrices(int locI, int locJ, float[] testA, float A00, float A01, float A11) {
        assertEquals("Value for A00 does not match the expected at pixel location I:"+ locI + ", J: " + locJ, testA[0], A00, 1e-2);
        assertEquals("Value for A01 does not match the expected at pixel location I:"+ locI + ", J: " + locJ, testA[1], A01, 1e-2);
        assertEquals("Value for A11 does not match the expected at pixel location I:"+ locI + ", J: " + locJ, testA[2], A11, 1e-2);
    }

    private double[][] computeTestMatrixAAtLoc(IImage img1, IImage img2, double locI, double locJ) {
        SimpleLucasKanadeImpl impl = new SimpleLucasKanadeImpl(0.0f, 3, true, windowSize, 5);
        impl.updateImageA(img1);
        impl.updateImageB(img2);
        impl.interpolate(locI, locJ, 0.0f, 0.0f);
        double A[][] = impl.getA();
        return A;
    }
    
    private void computeDerivativesWrapper(int baseIdI, int baseIdJ) {
        for (int tidI = 0; tidI < threadsInI; tidI++) {
            globalId[0] = baseIdI + tidI;
            localId[0] = tidI;
            for (int tidJ = 0; tidJ < threadsInJ; tidJ++) {                
                globalId[1] = baseIdJ + tidJ;
                localId[1] = tidJ;
                
                pixelValues = pixelValuesTs[tidI][tidJ];
                dI          = dITs[tidI][tidJ];
                dJ          = dJTs[tidI][tidJ];                
                A00         = A00Ts[tidI][tidJ];
                A01         = A01Ts[tidI][tidJ];
                A11         = A11Ts[tidI][tidJ];
                computeDerivatives();
            }
        }
        System.out.println("Sums: " + sums[0] + ", " + sums[1] + ", " + sums[2] + ", " + sums[3]);
        dump2DArray("pixelCounts", pixelCounts);
        dump1DArray("offsetsI", offsetsI);
        dump1DArray("offsetsJ", offsetsJ);
    }
    
    int sums[] = new int[4];
    int pixelCounts[][] = new int [27 + 4][27 + 4];
    int offsetsI[] = new int[27 + 4];
    int offsetsJ[] = new int[27 + 4];
    private void computeDerivatives() {
        int tidI = getLocalId(0);
        int tidJ = getLocalId(1);

        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);
        
        //Find out which threads need to be active in the last blocks
        int wBI = (blockItemsPerWorkGroupI - 1) * localSizeI + tidI >= windowSize + blockSizeI - 1 ? 0 : 1;
        int wBJ = (blockItemsPerWorkGroupJ - 1) * localSizeJ + tidJ >= windowSize + blockSizeJ - 1 ? 0 : 1;        
        
        for (int indexI  = 0; indexI < blockSizeI; indexI++) {
            for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                int Aindex = indexI * blockSizeJ + indexJ;
                A00[Aindex] = 0.0f;
                A01[Aindex] = 0.0f;
                A11[Aindex] = 0.0f;
            }
        }
        
        final int totalBlockItems =  (blockItemsPerWorkGroupI * blockItemsPerWorkGroupJ);
        
        for (int bI = 0; bI < blockItemsPerWorkGroupI; bI++) {
            final int pixelI = bI * localSizeI + tidI;
            final int idxSourceOffsetI = (pixelI + 1) * (windowSize + blockSizeJ-1 + 2);
            for (int bJ = 0; bJ < blockItemsPerWorkGroupJ; bJ++) {
                final int pixelJ = bJ * localSizeJ + tidJ;
                //Index in the local image buffer to read the pixel from
                int idxSource = idxSourceOffsetI + (pixelJ + 1);
                //Employ w to reject out-of-bound accesses
                float w = (wBI == 1 || bI < blockItemsPerWorkGroupI - 1) && (wBJ == 1 || bJ < blockItemsPerWorkGroupJ - 1) ? 1.0f : 0.0f;
                if (w != 0.0f) {
                    offsetsI[pixelI]++;
                    offsetsJ[pixelJ]++;
                }
                idxSource = w == 1.0f ? idxSource : 1;
                //
                int idxSourceTopLeft = pixelI * (windowSize + blockSizeJ-1 + 2) + pixelJ;
                idxSourceTopLeft = w == 1.0f ? idxSourceTopLeft : 1;
                //
                int idxSourceTop = idxSourceTopLeft + 1;
                int idxSourceTopRight = idxSourceTop + 1;
                int idxSourceLeft = idxSource - 1;
                int idxSourceRight = idxSource + 1;
                int idxSourceBottomLeft = (pixelI + 2) * (windowSize + blockSizeJ-1 + 2) + pixelJ;
                idxSourceBottomLeft = w == 1.0f ? idxSourceBottomLeft : 1;
                //
                int idxSourceBottom = idxSourceBottomLeft + 1;
                int idxSourceBottomRight = idxSourceBottom + 1;
                
                float value = localImgBuffer[idxSource];
                float dJL = mad(localImgBuffer[idxSourceTopRight] + localImgBuffer[idxSourceBottomRight] - localImgBuffer[idxSourceTopLeft] - localImgBuffer[idxSourceBottomLeft], 3.0f/32.0f,
                               (localImgBuffer[idxSourceRight] - localImgBuffer[idxSourceLeft]) * 10.0f/32.0f);
                float dIL = mad(localImgBuffer[idxSourceBottomRight] + localImgBuffer[idxSourceBottomLeft] - localImgBuffer[idxSourceTopRight] - localImgBuffer[idxSourceTopLeft], 3.0f/32.0f,
                               (localImgBuffer[idxSourceBottom] - localImgBuffer[idxSourceTop]) * 10.0f/32.0f);

                int currentTargetOffset = bI * blockItemsPerWorkGroupJ + bJ;
                for (int indexI  = 0; indexI < blockSizeI; indexI++) {
                    int blockIndexOffsetI = indexI * blockSizeJ;
                    for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                        int Aindex = blockIndexOffsetI + indexJ;
                        //Reuse w to reject unwanted data
                        float w2 = pixelI >= indexI && pixelI < windowSize + indexI && 
                                   pixelJ >= indexJ && pixelJ < windowSize + indexJ ? w : 0.0f;
                        if (pixelI == 28 && pixelJ == 0) {
                            System.out.println("HEre");
                        }
                        if (w2 != 0.0f) {
                            pixelCounts[pixelI][pixelJ] ++;
                        }
                            
                        if (Aindex == 0 && w2 != 0.0f) {
                            sums[0] = sums[0] + 1;
                        }
                        if (Aindex == 1 && w2 != 0.0f) {
                            sums[1] = sums[1] + 1;
                        }
                        if (Aindex == 2 && w2 != 0.0f) {
                            sums[2] = sums[2] + 1;
                        }
                        if (Aindex == 3 && w2 != 0.0f) {
                            sums[3] = sums[3] + 1;
                        }

                        //Index in the private memory to store the data
                        int idxTarget = (blockIndexOffsetI + indexJ) * totalBlockItems + currentTargetOffset;
                        
                        pixelValues[idxTarget] = value * w2;
                        //
                        dJ[idxTarget] = dJL * w2;
                        dI[idxTarget] = dIL * w2;
                        
                        A00[Aindex] = mad(dJL, dJL * w2, A00[Aindex]);
                        A01[Aindex] = mad(dJL, dIL * w2, A01[Aindex]);
                        A11[Aindex] = mad(dIL, dIL * w2, A11[Aindex]);                        
                    }
                }
            }
        }
    }

    private void reduceAsWrapperPartA(int baseIdI, int baseIdJ) {
        for (int tidI = 0; tidI < threadsInI; tidI++) {
            globalId[0] = baseIdI + tidI;
            localId[0] = tidI;
            for (int tidJ = 0; tidJ < threadsInJ; tidJ++) {                
                globalId[1] = baseIdJ + tidJ;
                localId[1] = tidJ;

                pixelValues = pixelValuesTs[tidI][tidJ];
                dI          = dITs[tidI][tidJ];
                dJ          = dJTs[tidI][tidJ];                
                A00         = A00Ts[tidI][tidJ];
                A01         = A01Ts[tidI][tidJ];
                A11         = A11Ts[tidI][tidJ];

                reduceAsPartA();
            }
        }
    }

    private void reduceAsWrapperPartB(int baseIdI, int baseIdJ) {
        int threadsSize = threadsInI * threadsInJ;
        
        for (int startSize = threadsSize / 2; startSize >= 1; startSize /=  2) {
            for (int tidI = 0; tidI < threadsInI; tidI++) {
                globalId[0] = baseIdI + tidI;
                localId[0] = tidI;
                for (int tidJ = 0; tidJ < threadsInJ; tidJ++) {                
                    globalId[1] = baseIdJ + tidJ;
                    localId[1] = tidJ;
    
                    pixelValues = pixelValuesTs[tidI][tidJ];
                    dI          = dITs[tidI][tidJ];
                    dJ          = dJTs[tidI][tidJ];                
                    A00         = A00Ts[tidI][tidJ];
                    A01         = A01Ts[tidI][tidJ];
                    A11         = A11Ts[tidI][tidJ];
    
                    reduceAsPartB(startSize);
                }
            }
        }
    }

    private void reduceAsWrapperPartC(int baseIdI, int baseIdJ) {
        for (int tidI = 0; tidI < threadsInI; tidI++) {
            globalId[0] = baseIdI + tidI;
            localId[0] = tidI;
            for (int tidJ = 0; tidJ < threadsInJ; tidJ++) {                
                globalId[1] = baseIdJ + tidJ;
                localId[1] = tidJ;

                pixelValues = pixelValuesTs[tidI][tidJ];
                dI          = dITs[tidI][tidJ];
                dJ          = dJTs[tidI][tidJ];                
                A00         = A00Ts[tidI][tidJ];
                A01         = A01Ts[tidI][tidJ];
                A11         = A11Ts[tidI][tidJ];

                reduceAsPartC();
            }
        }
    }

    protected void reduceAsPartA() {
        int tidI = getLocalId(0);
        int tidJ = getLocalId(1);

        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);

        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;
        
        int AsOffset = blocksSize * threadsSize;
        
        int threadOffset = tidI * localSizeJ + tidJ;
        for (int indexI  = 0; indexI < blockSizeI; indexI++) {
            int blockOffsetI = indexI * blockSizeJ;
            for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                int Aindex = blockOffsetI + indexJ;
                int targetIndexA00 = Aindex * threadsSize + threadOffset;
                int targetIndexA01 = targetIndexA00 + AsOffset;
                int targetIndexA11 = targetIndexA01 + AsOffset;
                multiWorkBuffer[targetIndexA00] = A00[Aindex];
                multiWorkBuffer[targetIndexA01] = A01[Aindex];
                multiWorkBuffer[targetIndexA11] = A11[Aindex];
            }
        }
    }

    protected void reduceAsPartB(int startSize) {
        int tidI = getLocalId(0);
        int tidJ = getLocalId(1);

        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);

        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;
        
        int AsOffset = blocksSize * threadsSize;
        
        int threadOffset = tidI * localSizeJ + tidJ;
        
        if (threadOffset < startSize) {
            for (int indexI  = 0; indexI < blockSizeI; indexI++) {
                int blockOffsetI = indexI * blockSizeJ;
                for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                    int Aindex = blockOffsetI + indexJ;
                    //
                    int idx1a = Aindex * threadsSize + threadOffset;
                    int idx2a = Aindex * threadsSize + threadOffset + startSize;
                    multiWorkBuffer[idx1a] += multiWorkBuffer[idx2a];
                    int idx1b = idx1a + AsOffset;
                    int idx2b = idx2a + AsOffset;
                    multiWorkBuffer[idx1b] += multiWorkBuffer[idx2b];
                    int idx1c = idx1b + AsOffset;
                    int idx2c = idx2b + AsOffset;
                    multiWorkBuffer[idx1c] += multiWorkBuffer[idx2c];
                }
            }
        }
    }

    protected void reduceAsPartC() {
        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);

        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;
        
        int AsOffset = blocksSize * threadsSize;
            
        for (int indexI  = 0; indexI < blockSizeI; indexI++) {
            int blockOffsetI = indexI * blockSizeJ;
            for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                int Aindex = blockOffsetI + indexJ;
                int accumIndex1 = Aindex * threadsSize;
                int accumIndex2 = accumIndex1 + AsOffset;
                int accumIndex3 = accumIndex2 + AsOffset;
                A00[Aindex] = multiWorkBuffer[accumIndex1];
                A01[Aindex] = multiWorkBuffer[accumIndex2];
                A11[Aindex] = multiWorkBuffer[accumIndex3];
            }
        }
    }

    private float getPixel(int i, int j) {
        i += windowSize/2;
        j += windowSize/2;
        return localImgBuffer[(i + 1) * (windowSize + blockSizeI-1 + 2) + (j + 1)];
    }
    
    void getImagePatchA(float A[]) {
        for (int i = -windowSize/2; i <= windowSize/2; i++) {
            for (int j = -windowSize/2; j <= windowSize/2; j++) {
                double dI = 3.0 * (getPixel(i + 1, j - 1) + getPixel(i + 1, j + 1) -
                                   getPixel(i - 1, j - 1) - getPixel(i - 1, j + 1)) +
                           10.0 * (getPixel(i + 1, j) - getPixel(i - 1, j));
                dI *= -1.0 / 32.0;
                
                double dJ = 3.0 * (getPixel(i - 1, j + 1) + getPixel(i + 1, j + 1) -
                                   getPixel(i - 1, j - 1) - getPixel(i + 1, j - 1)) +
                            10.0 * (getPixel(i, j + 1) - getPixel(i, j - 1));
                dJ *= -1.0 / 32.0;
                                                
                A[0] += dJ * dJ;
                A[1] += dI * dJ;
                A[2] += dI * dI;
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
    
    private float[][] clipToLocalBufferHelper(Matrix region1, int locI, int locJ) {
        float testImgBuffer[][] = new float[windowSize + blockSizeI-1 + 2][windowSize + blockSizeJ-1 + 2];
        for (int i = 0; i < windowSize + blockSizeI-1 + 2; i++) {
            int imgLocI = locI + i - windowSize/2 - 1;
            if (imgLocI < 0) {
                imgLocI = 0;
            }
            if (imgLocI >= imageHeight) {
                imgLocI = imageHeight - 1;
            }
            for (int j = 0; j < windowSize + blockSizeJ-1 + 2; j++) {
                int imgLocJ = locJ + j - windowSize/2 - 1;
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
    
    private void checkPass(float[][] testImgBuffer, float[] localImgBuffer) {
        for (int i = 0; i < windowSize + blockSizeI-1 + 2; i++) {
            for (int j = 0; j < windowSize + blockSizeJ-1 + 2; j++) {
                int idx = i * (windowSize + blockSizeJ-1 + 2) + j;
                assertEquals("localImgBuffer value does not match the expected at I:" + i+ ", J:" + j, testImgBuffer[i][j], localImgBuffer[idx], 1e-8f);
            }
        }
    }
}
