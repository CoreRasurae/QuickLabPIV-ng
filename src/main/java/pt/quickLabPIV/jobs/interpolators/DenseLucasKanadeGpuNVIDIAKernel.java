// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

import com.aparapi.Kernel;

/**
 * This kernel is a copy of DenseLucasKanadeGpuKernel with MAX_BLOCK_SIZE_I and MAX_BLOCK_SIZE_J modified to avoid register spill on NVIDIA GPUs which seem 
 * to only have 64KBytes of private memory per work group, thus in our case 1KB max. per thread/work item.
 * @author lpnm
 *
 */
public class DenseLucasKanadeGpuNVIDIAKernel extends Kernel implements IDenseLucasKanadeKernel {
    public static final int MAX_BLOCK_SIZE_I = 2;
    public static final int MAX_BLOCK_SIZE_J = 2;
    public static final int MAX_BLOCK_ITEMS_PER_WORKGROUP_I = 4;
    public static final int MAX_BLOCK_ITEMS_PER_WORKGROUP_J = 4;
    protected final int blockSizeI;
    protected final int blockSizeJ;
    protected final int blockItemsPerWorkGroupI;
    protected final int blockItemsPerWorkGroupJ;
    protected final int windowSize;
    protected final int iterations;
    protected final int imageWidth;
    protected final int imageHeight;

    //Testing flag to allow testing in JTP environment.
    //Currently Aparapi as of v3.0.0 is broken regarding boolean handling.
    protected int testing = 0;
    
    protected float us[];
    protected float vs[];
    protected float imageA[];
    protected float imageB[];
    protected int halfPixelOffset = 0;
    
    @Local
    protected float localImgBuffer[]; 
    
    @Local
    protected float multiWorkBuffer[];
    
    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J)
    protected float pixelValues[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalPixelValues = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J)
    protected float dI_privUs[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
    @NoCL private static ThreadLocal<float[]> _threadLocaldI = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
        }
    };
    
    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J)
    protected float dJ_privVs[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
    @NoCL private static ThreadLocal<float[]> _threadLocaldJ = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J * MAX_BLOCK_ITEMS_PER_WORKGROUP_I * MAX_BLOCK_ITEMS_PER_WORKGROUP_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected float A00_B[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalA00 = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };
    
    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected float A01_IIxy[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalA01 = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected float A11_II[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalA11 = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected float locIPrv_Ixt[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalLocIPrv = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected float locJPrv_Iyt[] = new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<float[]> _threadLocalLocJPrv = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
           return new float[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };

    @PrivateMemorySpace(MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J)
    protected byte status[] = new byte[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
    @NoCL private static ThreadLocal<byte[]> _threadLocalStatus = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
           return new byte[MAX_BLOCK_SIZE_I * MAX_BLOCK_SIZE_J];
        }
    };
    
    @NoCL
    public void setUpPrivateMemory() {
       pixelValues = _threadLocalPixelValues.get();
       dI_privUs = _threadLocaldI.get();
       dJ_privVs = _threadLocaldJ.get();
       A00_B = _threadLocalA00.get();
       A01_IIxy = _threadLocalA01.get();
       A11_II = _threadLocalA11.get();
       locIPrv_Ixt = _threadLocalLocIPrv.get();
       locJPrv_Iyt = _threadLocalLocJPrv.get();
       status = _threadLocalStatus.get();
    }
    
    @NoCL
    protected int getWindowSize() {
        return windowSize;
    }
    
    @NoCL
    protected float[] getLocalImgBuffer() {
        return localImgBuffer;
    }
    
    @NoCL
    protected int getBlockSizeI() {
        return blockSizeI;
    }

    @NoCL
    protected int getBlockSizeJ() {
        return blockSizeJ;
    }

    @NoCL
    protected int getBlockItemsPerWorkGroupI() {
        return blockItemsPerWorkGroupI;
    }
    
    @NoCL
    protected int getBlockItemsPerWorkGroupJ() {
        return blockItemsPerWorkGroupJ;
    }
    
    @NoCL
    protected int getImageWidth() {
        return imageWidth;
    }
    
    @NoCL
    protected int getImageHeight() {
        return imageHeight;
    }

    
    public DenseLucasKanadeGpuNVIDIAKernel(int localSizeI, int localSizeJ, int _blockSizeI, int _blockSizeJ, 
                                     int _blockItemsPerWorkGroupI, int _blockItemsPerWorkGroupJ, 
                                     int _windowSize, int _iterations, int _imageHeight, int _imageWidth) {
        blockSizeI = _blockSizeI;
        blockSizeJ = _blockSizeJ;
        blockItemsPerWorkGroupI = _blockItemsPerWorkGroupI;
        blockItemsPerWorkGroupJ = _blockItemsPerWorkGroupJ;
        windowSize = _windowSize;
        iterations = _iterations;
        imageHeight = _imageHeight;
        imageWidth = _imageWidth;
        
        localImgBuffer = new float[(windowSize + blockSizeI-1 + 2) * (windowSize + blockSizeJ-1 + 2)];
        
        multiWorkBuffer = new float[max(localSizeI * localSizeJ * blockSizeI * blockSizeJ * 4,
                                        (windowSize + blockSizeI-1 + 3) * (windowSize + blockSizeJ-1 + 3))];
    }
    
    @NoCL
    public void setKernelArgs(final float _imageA[], final float[] _imageB, final float[] _us, final float[] _vs, boolean halfPixelOffset) {
        imageA = _imageA;
        imageB = _imageB;
        us = _us;
        vs = _vs;
        this.halfPixelOffset = halfPixelOffset ? 1 : 0;
    }
    
    protected float readPixel(float[] image, int i, int j) {
        int idx = min(max(i, 0), imageHeight-1) * imageWidth + min(max(j, 0), imageWidth-1); 
        return image[idx];
    }
    
    //TODO: This code makes 4 global memory accesses per pixel, and as such it should not operate directly on the global memory,
    //the imageB should be read into a local buffer and then worked upon. Since each velocity vector estimation location may
    //require its own pixel map, it should be operated one velocity vector at a time, using a localBarrier, to sync the 
    //reading of the imageB to a local buffer.
    protected float readPixelWithWarp(float[] img, float locI, float locJ) {
        int i = (int) locI;
        int j = (int) locJ;
        
        float deltaI = locI - i;
        float deltaJ = locJ - j;
        
        int signI = deltaI < 0 ? 1 : 0; 
        i -= signI;
        deltaI += signI;

        int signJ = deltaJ < 0 ? 1 : 0;
        j -= signJ;
        deltaJ += signJ;
        
        float value = mad((1.0f - deltaI), (mad((1.0f - deltaJ), readPixel(img, i  ,j), deltaJ * readPixel(img, i  ,j+1))),  
                                  deltaI * (mad((1.0f - deltaJ), readPixel(img, i+1,j), deltaJ * readPixel(img, i+1,j+1))));
        
        return value;
    }

    protected void doImageRegionToLocaLBufferDirect() {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int startLocI = gidI * blockSizeI;
        int startLocJ = gidJ * blockSizeJ;
        
        //Check if we need an extra block to accommodate for the margin around the interest image region (for helping computing the derivatives)
        int localBlocksPerGroupI = blockItemsPerWorkGroupI * localSizeI >= windowSize + blockSizeI-1 + 2 ? blockItemsPerWorkGroupI : blockItemsPerWorkGroupI + 1;
        int localBlocksPerGroupJ = blockItemsPerWorkGroupJ * localSizeJ >= windowSize + blockSizeJ-1 + 2 ? blockItemsPerWorkGroupJ : blockItemsPerWorkGroupJ + 1;
        
        //Find out which threads need to be active in the last blocks
        int wBI = (localBlocksPerGroupI-1) * localSizeI + tidI >= windowSize + blockSizeI-1 + 2 ? 0 : 1;
        int wBJ = (localBlocksPerGroupJ-1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 + 2 ? 0 : 1;
        
        for (int bI = 0; bI < localBlocksPerGroupI; bI++) {
            int offsetI = bI * localSizeI + tidI;
            for (int bJ = 0; bJ < localBlocksPerGroupJ; bJ++) {
                int offsetJ = bJ * localSizeJ + tidJ;
                if ((wBI == 1 || bI < localBlocksPerGroupI - 1) && (wBJ == 1 || bJ < localBlocksPerGroupJ - 1)) {
                    int idx = offsetI * (windowSize + blockSizeJ-1 + 2) + offsetJ;
                    int imIdx = max(min(startLocI + offsetI - windowSize/2 - 1, imageHeight-1), 0) * imageWidth + max(min(startLocJ + offsetJ - windowSize/2 - 1, imageWidth-1), 0);
                    localImgBuffer[idx] = imageA[imIdx];
                }
            }
        }
    }

    protected void doImageRegionToLocaLBufferHalfPixel() {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int startLocI = gidI * blockSizeI;
        int startLocJ = gidJ * blockSizeJ;
        
        //Check if we need an extra block to accommodate for the margin around the interest image region plus 1 pixel for the pixel averaging
        int localBlocksPerGroupIPartA = blockItemsPerWorkGroupI * localSizeI >= windowSize + blockSizeI-1 + 3 ? blockItemsPerWorkGroupI : blockItemsPerWorkGroupI + 1;
        int localBlocksPerGroupJPartA = blockItemsPerWorkGroupJ * localSizeJ >= windowSize + blockSizeJ-1 + 3 ? blockItemsPerWorkGroupJ : blockItemsPerWorkGroupJ + 1;
        
        //Find out which threads need to be active in the last blocks
        int wBIPartA = (localBlocksPerGroupIPartA-1) * localSizeI + tidI >= windowSize + blockSizeI-1 + 3 ? 0 : 1;
        int wBJPartA = (localBlocksPerGroupJPartA-1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 + 3 ? 0 : 1;

        //Read once to local memory multi-purpose work buffer, in order to reduce memory accesses
        for (int bI = 0; bI < localBlocksPerGroupIPartA; bI++) {
            int offsetI = bI * localSizeI + tidI;
            for (int bJ = 0; bJ < localBlocksPerGroupJPartA; bJ++) {
                int offsetJ = bJ * localSizeJ + tidJ;
                if ((wBIPartA == 1 || bI < localBlocksPerGroupIPartA - 1) && (wBJPartA == 1 || bJ < localBlocksPerGroupJPartA - 1)) {
                    int idx = offsetI * (windowSize + blockSizeJ-1 + 3) + offsetJ;
                    int imIdx = max(min(startLocI + offsetI - windowSize/2 - 1, imageHeight-1), 0) * imageWidth + max(min(startLocJ + offsetJ - windowSize/2 - 1, imageWidth-1), 0);
                    multiWorkBuffer[idx] = imageA[imIdx];
                }
            }
        }
        
        localBarrier();
        
        //Check if we need an extra block to accommodate for the margin around the interest image region (for helping computing the derivatives)
        int localBlocksPerGroupI = blockItemsPerWorkGroupI * localSizeI >= windowSize + blockSizeI-1 + 2 ? blockItemsPerWorkGroupI : blockItemsPerWorkGroupI + 1;
        int localBlocksPerGroupJ = blockItemsPerWorkGroupJ * localSizeJ >= windowSize + blockSizeJ-1 + 2 ? blockItemsPerWorkGroupJ : blockItemsPerWorkGroupJ + 1;
        
        //Find out which threads need to be active in the last blocks
        int wBI = (localBlocksPerGroupI-1) * localSizeI + tidI >= windowSize + blockSizeI-1 + 2 ? 0 : 1;
        int wBJ = (localBlocksPerGroupJ-1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 + 2 ? 0 : 1;
        
        //Now compute the half pixel offset values, working exclusively on the local memory
        for (int bI = 0; bI < localBlocksPerGroupI; bI++) {
            int offsetI = bI * localSizeI + tidI;
            for (int bJ = 0; bJ < localBlocksPerGroupJ; bJ++) {
                int offsetJ = bJ * localSizeJ + tidJ;
                if ((wBI == 1 || bI < localBlocksPerGroupI - 1) && (wBJ == 1 || bJ < localBlocksPerGroupJ - 1)) {
                    int idx = offsetI * (windowSize + blockSizeJ-1 + 2) + offsetJ;
                    int idxWrk00 = offsetI * (windowSize + blockSizeJ-1 + 3) + offsetJ;
                    int idxWrk01 = idxWrk00 + 1;
                    int idxWrk10 = (offsetI + 1) * (windowSize + blockSizeJ-1 + 3) + offsetJ;
                    int idxWrk11 = idxWrk10 + 1;
                    localImgBuffer[idx] = (multiWorkBuffer[idxWrk00] + multiWorkBuffer[idxWrk01] + multiWorkBuffer[idxWrk10] + multiWorkBuffer[idxWrk11]) / 4.0f;
                }
            }
        }        
    }

    //For debugging purposes
    @NoCL
    protected void imageReadCompleted() {};
    
    private void computeDerivatives() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        //Find out which threads need to be active in the last blocks
        int wBI = (blockItemsPerWorkGroupI - 1) * localSizeI + tidI >= windowSize + blockSizeI-1 ? 0 : 1;
        int wBJ = (blockItemsPerWorkGroupJ - 1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 ? 0 : 1;        

        for (int indexI  = 0; indexI < blockSizeI; indexI++) {
            for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                int Aindex = indexI * blockSizeJ + indexJ;
                A00_B[Aindex] = 0.0f;
                A01_IIxy[Aindex] = 0.0f;
                A11_II[Aindex] = 0.0f;
            }
        }
        
        final int totalBlockItems =  (blockItemsPerWorkGroupI * blockItemsPerWorkGroupJ);
        
        for (int bI = 0; bI < blockItemsPerWorkGroupI; bI++) {
            final int offsetI = bI * localSizeI + tidI;
            final int idxSourceOffsetI = (offsetI + 1) * (windowSize + blockSizeJ-1 + 2);
            for (int bJ = 0; bJ < blockItemsPerWorkGroupJ; bJ++) {
                final int offsetJ = bJ * localSizeJ + tidJ;
                //Index in the local image buffer to read the pixel from
                int idxSource = idxSourceOffsetI + (offsetJ + 1);
                //Employ w to reject out-of-bound accesses
                float w = (wBI == 1 || bI < blockItemsPerWorkGroupI - 1) && (wBJ == 1 || bJ < blockItemsPerWorkGroupJ - 1) ? 1.0f : 0.0f;
                idxSource = w == 1.0f ? idxSource : 1;
                //
                int idxSourceTopLeft = offsetI * (windowSize + blockSizeJ-1 + 2) + offsetJ;
                idxSourceTopLeft = w == 1.0f ? idxSourceTopLeft : 1;
                //
                int idxSourceTop = idxSourceTopLeft + 1;
                int idxSourceTopRight = idxSourceTop + 1;
                int idxSourceLeft = idxSource - 1;
                int idxSourceRight = idxSource + 1;
                int idxSourceBottomLeft = (offsetI + 2) * (windowSize + blockSizeJ-1 + 2) + offsetJ;
                idxSourceBottomLeft = w == 1.0f ? idxSourceBottomLeft : 1;
                //
                int idxSourceBottom = idxSourceBottomLeft + 1;
                int idxSourceBottomRight = idxSourceBottom + 1;
                
                float value = localImgBuffer[idxSource];
                float dJL = mad(localImgBuffer[idxSourceTopLeft] + localImgBuffer[idxSourceBottomLeft] - localImgBuffer[idxSourceTopRight] - localImgBuffer[idxSourceBottomRight], 3.0f/32.0f,
                        (localImgBuffer[idxSourceLeft] - localImgBuffer[idxSourceRight]) * 10.0f/32.0f);
                float dIL = mad(localImgBuffer[idxSourceTopRight] + localImgBuffer[idxSourceTopLeft] - localImgBuffer[idxSourceBottomRight] - localImgBuffer[idxSourceBottomLeft], 3.0f/32.0f,
                        (localImgBuffer[idxSourceTop] - localImgBuffer[idxSourceBottom]) * 10.0f/32.0f);

                int currentTargetOffset = bI * blockItemsPerWorkGroupJ + bJ;
                
                for (int indexI  = 0; indexI < blockSizeI; indexI++) {
                    int blockIndexOffsetI = indexI * blockSizeJ;
                    for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                        int Aindex = indexI * blockSizeJ + indexJ;
                        //Reuse w to reject unwanted data
                        float w2 = offsetI >= indexI && offsetI < windowSize + indexI && 
                                   offsetJ >= indexJ && offsetJ < windowSize + indexJ ? w : 0.0f;
                            
                        //Index in the private memory to store the data
                        int idxTarget = (blockIndexOffsetI + indexJ) * totalBlockItems + currentTargetOffset;
                        
                        pixelValues[idxTarget] = value * w2;
                        //
                        dJ_privVs[idxTarget] = dJL * w2;
                        dI_privUs[idxTarget] = dIL * w2;
                        
                        A00_B[Aindex] = mad(dJL, dJL * w2, A00_B[Aindex]);
                        A01_IIxy[Aindex] = mad(dJL, dIL * w2, A01_IIxy[Aindex]);
                        A11_II[Aindex] = mad(dIL, dIL * w2, A11_II[Aindex]);                        
                    }
                }
            }
        }
        
        computedPixelValuesDJsDIs(pixelValues, dJ_privVs, dI_privUs);
    }
    
    @NoCL
    protected void computedPixelValuesDJsDIs(float[] pixelValues, float[] dJs, float[] dIs) {}

    protected void reduceAs() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

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
                multiWorkBuffer[targetIndexA00] = A00_B[Aindex];
                multiWorkBuffer[targetIndexA01] = A01_IIxy[Aindex];
                multiWorkBuffer[targetIndexA11] = A11_II[Aindex];
            }
        }
        localBarrier();
        for (int startSize = threadsSize / 2; startSize >= 1; startSize /=  2) {
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
            localBarrier();
        }
    
        for (int indexI  = 0; indexI < blockSizeI; indexI++) {
            int blockOffsetI = indexI * blockSizeJ;
            for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                int Aindex = blockOffsetI + indexJ;
                int accumIndex1 = Aindex * threadsSize;
                int accumIndex2 = accumIndex1 + AsOffset;
                int accumIndex3 = accumIndex2 + AsOffset;
                A00_B[Aindex] = multiWorkBuffer[accumIndex1];
                A01_IIxy[Aindex] = multiWorkBuffer[accumIndex2];
                A11_II[Aindex] = multiWorkBuffer[accumIndex3];
            }
        }
        matrixAComputed(A00_B, A01_IIxy, A11_II);
        waitOnThreads();
    }
       
    @NoCL
    protected void matrixAComputed(float[] A00, float[] A01, float[] A11) {}
    
    @NoCL
    protected void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA) {};
    
    @NoCL
    protected void waitOnThreads() {};
    
    private int invertMatrices() {
        int removedCount = 0;
        for (int bI = 0; bI < blockSizeI; bI++) {
            int offsetI = bI * blockSizeJ;
            for (int bJ = 0; bJ < blockSizeJ; bJ++) {
                int Aidx = offsetI + bJ;
                float detA = mad(A00_B[Aidx], A11_II[Aidx], -A01_IIxy[Aidx]*A01_IIxy[Aidx]);
                
                if (detA < 1.192092896e-7f && status[Aidx] == 1) {
                    status[Aidx] = 0;
                    removedCount++;
                }
                
                float temp = A00_B[Aidx];
                A00_B[Aidx] =   A11_II[Aidx] / detA;
                A01_IIxy[Aidx] = - A01_IIxy[Aidx] / detA;
                A11_II[Aidx] =   temp / detA;
            }
        }        
        matrixAInverted(A00_B, A01_IIxy, A11_II, 0.0f);
        waitOnThreads();
        
        return removedCount;
    }
    
    protected void computeBs(int blockIdxI, int blockIdxJ) {
        final int blockOffset = blockIdxI * blockSizeJ + blockIdxJ;
                
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        float locI = locIPrv_Ixt[blockOffset];
        float locJ = locJPrv_Iyt[blockOffset];
        
        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        //Find out which threads need to be active in the last blocks
        int wBI = (blockItemsPerWorkGroupI - 1) * localSizeI + tidI >= windowSize + blockSizeI-1 ? 0 : 1;
        int wBJ = (blockItemsPerWorkGroupJ - 1) * localSizeJ + tidJ >= windowSize + blockSizeJ-1 ? 0 : 1;        

        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;

        int b1sOffset = blocksSize * threadsSize;
        
        int threadsOffset = tidI * localSizeJ + tidJ;
        
        int idxTarget = blockOffset * threadsSize + threadsOffset;
        
        multiWorkBuffer[idxTarget] = 0.0f;
        multiWorkBuffer[idxTarget + b1sOffset] = 0.0f;
        
        int idxSourceOffset = blockOffset * blockItemsPerWorkGroupI * blockItemsPerWorkGroupJ;
        for (int bigIdxI = 0; bigIdxI < blockItemsPerWorkGroupI; bigIdxI++) {
            int offsetI = bigIdxI * localSizeI + tidI;
            for (int bigIdxJ = 0; bigIdxJ < blockItemsPerWorkGroupJ; bigIdxJ++) {
                int offsetJ = bigIdxJ * localSizeJ + tidJ;
                int idxSource = idxSourceOffset + bigIdxI * blockItemsPerWorkGroupJ + bigIdxJ;
                
                float w = (wBI == 1 || bigIdxI < blockItemsPerWorkGroupI - 1) && (wBJ == 1 || bigIdxJ < blockItemsPerWorkGroupJ - 1) ? 1.0f : 0.0f;
                idxSource *= w;

                //Reuse w to reject unwanted data, that does not belong to the current vector window.
                float w2 = offsetI >= blockIdxI && offsetI < windowSize + blockIdxI && 
                           offsetJ >= blockIdxJ && offsetJ < windowSize + blockIdxJ ? w : 0.0f;

                //Since our matrices are displaced to the right with increasing (blockIdxI, blockIdxJ), we have to compensate for that displacement in the actual window, by subtracting such value
                //from locI and locJ, respectively.
                float dT = (readPixelWithWarp(imageB, locI-windowSize/2+offsetI-blockIdxI, locJ-windowSize/2+offsetJ-blockIdxJ) - pixelValues[idxSource])*w2;
                multiWorkBuffer[idxTarget] = 
                        mad(dT, dJ_privVs[idxSource], multiWorkBuffer[idxTarget]);
                multiWorkBuffer[idxTarget + b1sOffset] = 
                        mad(dT, dI_privUs[idxSource], multiWorkBuffer[idxTarget + b1sOffset]);
            }
        }        
    }
   
    @NoCL
    protected void matrixBComputed(int iter) {};
    
    protected void reduceBs() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;
        
        int bsOffset = blocksSize * threadsSize;
        
        int threadOffset = tidI * localSizeJ + tidJ;
        
        for (int startSize = threadsSize / 2; startSize >= 1; startSize /=  2) {
            if (threadOffset < startSize) {
                for (int indexI  = 0; indexI < blockSizeI; indexI++) {
                    int blockOffsetI = indexI * blockSizeJ;
                    for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                        int Aindex = blockOffsetI + indexJ;
                        //
                        int idx1a = Aindex * threadsSize + threadOffset;
                        int idx2a = Aindex * threadsSize + threadOffset + startSize;
                        multiWorkBuffer[idx1a] += multiWorkBuffer[idx2a];
                        int idx1b = idx1a + bsOffset;
                        int idx2b = idx2a + bsOffset;
                        multiWorkBuffer[idx1b] += multiWorkBuffer[idx2b];
                    }
                }
            }
            localBarrier();
        }    
    }
    
    protected void initStatus() {
        for (int bI = 0; bI < blockSizeI; bI++) {
            int blockOffsetI = bI * blockSizeJ;
            for (int bJ = 0; bJ < blockSizeJ; bJ++) {
                int blockOffset = blockOffsetI + bJ;
                status[blockOffset] = 1;
            }
        }
    }
    
    protected void doDenseLucasKanade() {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int threadsSize = localSizeI*localSizeJ;
        int blocksSize = blockSizeI*blockSizeJ;
        int bsOffset = blocksSize * threadsSize;
        
        int validPixels = blocksSize;
                
        setUpPrivateMemory();
        
        initStatus();
        
        if (halfPixelOffset == 1) {
            doImageRegionToLocaLBufferHalfPixel();
        } else {
            doImageRegionToLocaLBufferDirect();
        }
        localBarrier();
        //Inform unit tests that image has been read        
        imageReadCompleted();
        waitOnThreads();        
        computeDerivatives();
        reduceAs();
        int removedCount = invertMatrices();
        validPixels -= removedCount;
        
        int startLocI = gidI * blockSizeI;
        int startLocJ = gidJ * blockSizeJ;
        
        for (int blockIdxI = 0; blockIdxI < blockSizeI; blockIdxI++) {
            final int pixelI = startLocI + blockIdxI;            
            final int pxOffsetI = pixelI < imageHeight ? pixelI * imageWidth : 0;
            final int blkOffsetI = blockIdxI * blockSizeJ;            
            for (int blockIdxJ = 0; blockIdxJ < blockSizeJ; blockIdxJ++) {
                final int pixelJ = startLocJ + blockIdxJ < imageWidth ? startLocJ + blockIdxJ : 0;
                locIPrv_Ixt[blkOffsetI + blockIdxJ] = pixelI + vs[pxOffsetI + pixelJ];
                locJPrv_Iyt[blkOffsetI + blockIdxJ] = pixelJ + us[pxOffsetI + pixelJ];
                if (halfPixelOffset == 1) {
                    locIPrv_Ixt[blkOffsetI + blockIdxJ] += 0.5f;
                    locJPrv_Iyt[blkOffsetI + blockIdxJ] += 0.5f;
                }
            }
        }
                
        for (int iter = 0; iter < iterations; iter++) {
            if (testing == 1) {
               //Ensure that all threads exit at the same time (mainly for JTP purposes), to avoid deadlock
               localBarrier();
            }
            if (validPixels == 0) {
                if (tidI < blockSizeI && tidJ < blockSizeJ && startLocI + tidI < imageHeight && startLocJ + tidJ < imageWidth) {
                    int blkOffset = tidI * blockSizeJ + tidJ;
                    locIPrv_Ixt[blkOffset] -= startLocI + tidI;
                    locJPrv_Iyt[blkOffset] -= startLocJ + tidJ;
                    if (halfPixelOffset == 1) {
                        locIPrv_Ixt[blkOffset] -= 0.5f;
                        locJPrv_Iyt[blkOffset] -= 0.5f;
                    }
                    
                    int pixelIdx = (startLocI + tidI) * imageWidth + (startLocJ + tidJ);
                    vs[pixelIdx] = locIPrv_Ixt[blkOffset];
                    us[pixelIdx] = locJPrv_Iyt[blkOffset];
                }
                
                return;
            }

            for (int blockIdxI = 0; blockIdxI < blockSizeI; blockIdxI++) {
                int blockOffsetI = blockIdxI * blockSizeJ;
                for (int blockIdxJ = 0; blockIdxJ < blockSizeJ; blockIdxJ++) {
                    int Aindex = blockOffsetI + blockIdxJ;
                    if ((locIPrv_Ixt[Aindex] < 0 || locIPrv_Ixt[Aindex] >= imageHeight + windowSize/2) && status[Aindex] == 1) {
                        status[Aindex] = 0;
                        validPixels--;
                    }
                    
                    if ((locJPrv_Iyt[Aindex] < 0 || locJPrv_Iyt[Aindex] >= imageWidth + windowSize/2) && status[Aindex] == 1) {
                        status[Aindex] = 0;
                        validPixels--;
                    }

                    if (status[Aindex] == 1) {
                        computeBs(blockIdxI, blockIdxJ);
                    }
                    localBarrier();
                }
            }

            reduceBs();
            matrixBComputed(iter);
            waitOnThreads();        
   
            for (int indexI  = 0; indexI < blockSizeI; indexI++) {
                int blockOffsetI = indexI * blockSizeJ;
                for (int indexJ = 0; indexJ < blockSizeJ; indexJ++) {
                    int Aindex = blockOffsetI + indexJ;
                    int accumIndex1 = Aindex * threadsSize;
                    int accumIndex2 = accumIndex1 + bsOffset;
                    float b0 = multiWorkBuffer[accumIndex1];
                    float b1 = multiWorkBuffer[accumIndex2];
                    
                    float incU = mad(b0, A00_B[Aindex], b1 * A01_IIxy[Aindex]);
                    float incV = mad(b0, A01_IIxy[Aindex], b1 * A11_II[Aindex]);
                    if (abs(incU) < 1e-2f && abs(incV) < 1e-2f && status[Aindex] == 1) {
                        status[Aindex] = 0;
                        validPixels--;
                    }
                    if (status[Aindex] == 1) {
                        locIPrv_Ixt[Aindex] += incV;
                        locJPrv_Iyt[Aindex] += incU;
                    }                    
                }
            }            
        }
        
        if (tidI < blockSizeI && tidJ < blockSizeJ && startLocI + tidI < imageHeight && startLocJ + tidJ < imageWidth) {
            int blkOffset = tidI * blockSizeJ + tidJ;
            locIPrv_Ixt[blkOffset] -= startLocI + tidI;
            locJPrv_Iyt[blkOffset] -= startLocJ + tidJ;
            if (halfPixelOffset == 1) {
                locIPrv_Ixt[blkOffset] -= 0.5f;
                locJPrv_Iyt[blkOffset] -= 0.5f;
            }

            int pixelIdx = (startLocI + tidI) * imageWidth + (startLocJ + tidJ);
            vs[pixelIdx] = locIPrv_Ixt[blkOffset];
            us[pixelIdx] = locJPrv_Iyt[blkOffset];
        }
    }
    
    @Override
    public void run() {
        if (getPassId() == 0) {
           doDenseLucasKanade();
        }
    }
}
