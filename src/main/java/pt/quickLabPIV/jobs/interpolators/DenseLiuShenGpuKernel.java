// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

import java.util.concurrent.atomic.AtomicInteger;

public class DenseLiuShenGpuKernel extends DenseLucasKanadeGpuKernel implements IDenseLiuShenKernel {
    public static final int MAX_PRIVATE_ITEMS_LIU_SHEN = 9;

    int iterationsLS;
    float lambda;

    float imageLSA[];
    float imageLSB[];
    float usNew[];
    float vsNew[];
    float totalErrorGlobal[];
    AtomicInteger initCompleted[];

    public DenseLiuShenGpuKernel(int localSizeI, int localSizeJ, int _blockSizeI, int _blockSizeJ,
            int _blockItemsPerWorkGroupI, int _blockItemsPerWorkGroupJ, int _windowSize, int _iterationsLK,
            int _imageHeight, int _imageWidth, int _iterationsLS, float _lambda) {
        super(localSizeI, localSizeJ, _blockSizeI, _blockSizeJ, _blockItemsPerWorkGroupI, _blockItemsPerWorkGroupJ, _windowSize,
                _iterationsLK, _imageHeight, _imageWidth);

        assert pixelValues.length >= 18 : "pixelValues array is too small for Liu-Shen";
        assert dI_privUs.length >= 9 : "dI array is too small for Liu-Shen";
        assert dJ_privVs.length >= 9 : "dJ array is too small for Liu-shen";
        assert A00_B.length >= 3 : "A00 array is too small for Liu-Shen";
        assert A01_IIxy.length >= 2 : "A01 array is too small for Liu-Shen";
        assert localSizeI == 8 && localSizeJ == 8 : "loadImageWithMargins and loadImage depend on the localSizeI and localSizeJ having a value of 8";
        
        iterationsLS = _iterationsLS;
        lambda = _lambda;
    }
    
    public void setKernelArgs(final float _imageLKA[], final float[] _imageLKB, final float _imageLSA[], final float[] _imageLSB,
                              final float[] _us, final float[] _vs, final float[] _usNew, final float[] _vsNew, 
                              final float[] _totalError, boolean halfPixelOffset) {
        super.setKernelArgs(_imageLKA, _imageLKB, _us, _vs, halfPixelOffset);
        imageLSA = _imageLSA;
        imageLSB = _imageLSB;
        usNew = _usNew;
        vsNew = _vsNew;
        totalErrorGlobal = _totalError;
        initCompleted = new AtomicInteger[3];
        initCompleted[0] = new AtomicInteger();
        initCompleted[1] = new AtomicInteger();
        initCompleted[2] = new AtomicInteger();
    }
      
    private static final int topLeftIdx = 0;
    private static final int topIdx = 1;
    private static final int topRightIdx = 2;
    private static final int leftIdx = 3;
    private static final int centerIdx = 4;
    private static final int rightIdx = 5;
    private static final int bottomLeftIdx = 6;
    private static final int bottomIdx = 7;
    private static final int bottomRightIdx = 8;
    
    protected void loadWithMarginsAndDoAverage(final float[] img) {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int startLocI = mad24(gidI, localSizeI, - 1);
        int startLocJ = mad24(gidJ, localSizeJ, - 1);

        int pixelI = startLocI + tidI;
        int pixelJ = startLocJ + tidJ;
        
        int idxTarget = mad24(tidI, localSizeJ + 3, tidJ);
        multiWorkBuffer[idxTarget] = readPixel(img, pixelI, pixelJ);
        if (tidJ < 3) {
            int idxTargetB = mad24(tidI, localSizeJ + 3, tidJ + localSizeJ);
            multiWorkBuffer[idxTargetB] = readPixel(img, pixelI, pixelJ + localSizeJ);
        }
        if (tidI < 3) {
            int idxTargetB = mad24(tidI + localSizeI, localSizeJ + 3, tidJ);
            multiWorkBuffer[idxTargetB] = readPixel(img, pixelI + localSizeI, pixelJ);            
        }
        if (tidI < 3 && tidJ < 3) {
            int idxTargetB = mad24(tidI + localSizeI, localSizeJ + 3, tidJ + localSizeJ);
            multiWorkBuffer[idxTargetB] = readPixel(img, pixelI + localSizeI, pixelJ + localSizeJ);
        }
        localBarrier();
        
        float value = 0.0f;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int idxSourceL = mad24(tidI + i, localSizeJ + 3, tidJ + j);
                value = mad(multiWorkBuffer[idxSourceL], 1.0f/4.0f, value);
            }
        }        
        localImgBuffer[mad24(tidI, localSizeJ + 2, tidJ)] = value;
        
        if (tidJ < 2) {
            float valueB = 0.0f;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    int idxSourceL = mad24(tidI + i, localSizeJ + 3, tidJ + j + localSizeJ);
                    valueB = mad(multiWorkBuffer[idxSourceL], 1.0f/4.0f, valueB);
                }
            }        
            localImgBuffer[mad24(tidI, localSizeJ + 2, tidJ + localSizeJ)] = valueB;    
        }
        
        if (tidI < 2) {
            float valueB = 0.0f;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    int idxSourceL = mad24(tidI + i + localSizeI, localSizeJ + 3, tidJ + j);
                    valueB = mad(multiWorkBuffer[idxSourceL], 1.0f/4.0f, valueB);
                }
            }        
            localImgBuffer[mad24(tidI + localSizeI, localSizeJ + 2, tidJ)] = valueB;    
        }
        
        if (tidI < 2 && tidJ < 2) {
            float valueB = 0.0f;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    int idxSourceL = mad24(tidI + i + localSizeI, localSizeJ + 3, tidJ + j + localSizeJ);
                    valueB = mad(multiWorkBuffer[idxSourceL], 1.0f/4.0f, valueB);
                }
            }        
            localImgBuffer[mad24(tidI + localSizeI, localSizeJ + 2, tidJ + localSizeJ)] = valueB;                
        }
        localBarrier();
    }
    
    protected void loadWithMargins(float[] img) {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int startLocI = mad24(gidI, localSizeI, - 1);
        int startLocJ = mad24(gidJ, localSizeJ, - 1);
        
        int pixelI = startLocI + tidI;
        int pixelJ = startLocJ + tidJ;
        
        int idxTarget = mad24(tidI, localSizeJ + 2, tidJ);
        localImgBuffer[idxTarget] = readPixel(img, pixelI, pixelJ);
        if (tidJ < 2) {
            int idxTargetB = mad24(tidI, localSizeJ + 2, tidJ + localSizeJ);
            localImgBuffer[idxTargetB] = readPixel(img, pixelI, pixelJ + localSizeJ);
        }
        if (tidI < 2) {
            int idxTargetB = mad24(tidI + localSizeI, localSizeJ + 2, tidJ);
            localImgBuffer[idxTargetB] = readPixel(img, pixelI + localSizeI, pixelJ);            
        }
        if (tidI < 2 && tidJ < 2) {
            int idxTargetB = mad24(tidI + localSizeI, localSizeJ + 2, tidJ + localSizeJ);
            localImgBuffer[idxTargetB] = readPixel(img, pixelI + localSizeI, pixelJ + localSizeJ);
        }
        localBarrier();        
    }    
    
    @NoCL
    protected void imageAClippedAndLoaded() {};
    
    @NoCL
    protected void imageBClippedAndLoaded() {};
    
    @NoCL
    protected void pixelValuesLoaded() {};
    
    protected void doClipImage() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeJ = getLocalSize(0);
        
        if (halfPixelOffset == 1) {
            loadWithMarginsAndDoAverage(imageLSA);
        } else {
            loadWithMargins(imageLSA);
        }
        imageAClippedAndLoaded();
        
        final int width = localSizeJ + 2;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int localIdx = mad24(i + tidI, width, j + tidJ);
                int idx = mad24(i, 3, j);
                pixelValues[idx] = localImgBuffer[localIdx];
            }
        }        
        //Avoid partial threads from updating the local buffer, before all threads have read from it
        localBarrier();
        
        if (halfPixelOffset == 1) {
            loadWithMarginsAndDoAverage(imageLSB);
        } else {
            loadWithMargins(imageLSB);
        }        
        imageBClippedAndLoaded();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int localIdx = mad24(i + tidI, width, j + tidJ);
                int idx = mad24(i, 3, j);
                pixelValues[idx + 9] = localImgBuffer[localIdx];
            }
        }
        //Avoid partial threads from updating the local buffer, before all threads have read from it
        localBarrier();
        
        pixelValuesLoaded();
    }
    
    @NoCL
    protected void fixedImageDerivativesComputed() {};
    
    protected void computeFixedImageDerivatives() {
        float centerPixelValueA = pixelValues[centerIdx];
        
        A01_IIxy[0] = centerPixelValueA * (pixelValues[topIdx] - pixelValues[bottomIdx]) / 2.0f;
        A01_IIxy[1] = centerPixelValueA * (pixelValues[leftIdx] - pixelValues[rightIdx]) / 2.0f;
        A11_II[0]   = centerPixelValueA * centerPixelValueA;
        locIPrv_Ixt[0]  = centerPixelValueA * ((pixelValues[topIdx  + 9] - pixelValues[topIdx]) - (pixelValues[bottomIdx + 9] - pixelValues[bottomIdx])) / 2.0f;
        locJPrv_Iyt[0]  = centerPixelValueA * ((pixelValues[leftIdx + 9] - pixelValues[leftIdx]) - (pixelValues[rightIdx + 9]) - pixelValues[rightIdx]) / 2.0f;
        
        //Generate inverted matrix B
        A00_B[0] = mad(centerPixelValueA, (pixelValues[bottomIdx] + pixelValues[topIdx] - 2.0f*centerPixelValueA), - 2.0f * centerPixelValueA - lambda * 8.0f);
        A00_B[2] = mad(centerPixelValueA, (pixelValues[rightIdx] + pixelValues[leftIdx] - 2.0f*centerPixelValueA), - 2.0f * centerPixelValueA - lambda * 8.0f); 
        A00_B[1] = centerPixelValueA * (pixelValues[topLeftIdx] - pixelValues[topRightIdx] - pixelValues[bottomLeftIdx] + pixelValues[bottomRightIdx]) / 4.0f;
        
        float detA = mad(A00_B[0], A00_B[2], - A00_B[1] * A00_B[1]);
        
        float temp = A00_B[0];
        A00_B[0] =   A00_B[2] / detA;
        A00_B[1] = - A00_B[1] / detA;
        A00_B[2] =   temp / detA;
        
        fixedImageDerivativesComputed();
    }

    protected void loadVectors() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeJ = getLocalSize(0);
        
        int width = localSizeJ + 2;
        
        loadWithMargins(us);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int localIdx = mad24(i + tidI, width, j + tidJ);
                int idx = mad24(i, 3, j);
                dI_privUs[idx] = localImgBuffer[localIdx];
            }
        }
        //Avoid partial threads from updating the local buffer, before all threads have read from it
        localBarrier();
        
        loadWithMargins(vs);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int localIdx = mad24(i + tidI, width, j + tidJ);
                int idx = mad24(i, 3, j);
                dJ_privVs[idx] = localImgBuffer[localIdx];
            }
        }
        //Avoid partial threads from updating the local buffer, before all threads have read from it        
        localBarrier();
    }
    
    @NoCL
    protected void vectorsRefined(int w, float bu, float bv, float unew, float vnew, float totalError) {};
    
    protected float refineVectors() {
        float totalError = 0.0f;
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        int pixelI = mad24(gidI, localSizeI, tidI);
        int pixelJ = mad24(gidJ, localSizeJ, tidJ);
        int w = pixelI < imageHeight && pixelJ < imageWidth ? 1 : 0;
     
        float bu = mad(2.0f * A01_IIxy[0], (dI_privUs[topIdx] - dI_privUs[bottomIdx]) / 2.0f, 
                   mad(A01_IIxy[0], (dJ_privVs[leftIdx]    - dJ_privVs[rightIdx]) / 2.0f,
                   mad(A01_IIxy[1], (dJ_privVs[topIdx]     - dJ_privVs[bottomIdx]) / 2.0f, 
                   mad(A11_II[0]  , (dI_privUs[topIdx]     + dI_privUs[bottomIdx]),
                   mad(A11_II[0]  , (dJ_privVs[topLeftIdx] - dJ_privVs[topRightIdx] - dJ_privVs[bottomLeftIdx] + dJ_privVs[bottomRightIdx]) / 4.0f,
                   mad(lambda, (dI_privUs[topLeftIdx]    + dI_privUs[topIdx] + dI_privUs[topRightIdx] +
                                dI_privUs[leftIdx]       + dI_privUs[rightIdx] +
                                dI_privUs[bottomLeftIdx] + dI_privUs[bottomIdx] + dI_privUs[bottomRightIdx]),  locIPrv_Ixt[0]))))));

        float bv = mad(2.0f * A01_IIxy[1], (dJ_privVs[leftIdx] - dJ_privVs[rightIdx]) / 2.0f, 
                   mad(A01_IIxy[0], (dI_privUs[leftIdx]    - dI_privUs[rightIdx]) / 2.0f, 
                   mad(A01_IIxy[1], (dI_privUs[topIdx]     - dI_privUs[bottomIdx]) / 2.0f,
                   mad(A11_II[0]  , (dJ_privVs[leftIdx]    + dJ_privVs[rightIdx]),
                   mad(A11_II[0]  , (dI_privUs[topLeftIdx] - dI_privUs[topRightIdx] - dI_privUs[bottomLeftIdx] + dI_privUs[bottomRightIdx]) / 4.0f,
                   mad(lambda, (dJ_privVs[topLeftIdx]    + dJ_privVs[topIdx]    + dJ_privVs[topRightIdx] +
                                dJ_privVs[leftIdx]       + dJ_privVs[rightIdx]  +
                                dJ_privVs[bottomLeftIdx] + dJ_privVs[bottomIdx] + dJ_privVs[bottomRightIdx]), locJPrv_Iyt[0]))))));

        float unew = -mad(A00_B[0], bu, A00_B[1]*bv);
        float vnew = -mad(A00_B[1], bu, A00_B[2]*bv);
                        
        totalError = sqrt(mad((unew - dI_privUs[centerIdx]),(unew - dI_privUs[centerIdx]), (vnew - dJ_privVs[centerIdx])*(vnew - dJ_privVs[centerIdx]))) * w;
        
        if (w == 1) {
            int idx = mad24(pixelI, imageWidth, pixelJ);
            usNew[idx] = unew;
            vsNew[idx] = vnew;
        }
        
        vectorsRefined(w, bu, bv, unew, vnew, totalError);
        
        return totalError;
    }
    
    protected void copyVectors() {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);
        
        int validGroupsI = imageHeight / localSizeI;
        int validGroupsJ = imageWidth / localSizeJ;
        if (imageHeight % localSizeI != 0) {
            validGroupsI++;
        }
        if (imageWidth % localSizeJ != 0) {
            validGroupsJ++;
        }
        
        for (int groupIdxI = 0; groupIdxI < validGroupsI; groupIdxI++) {
            for (int groupIdxJ = 0; groupIdxJ < validGroupsJ; groupIdxJ++) {
                int pixelI = mad24(groupIdxI, localSizeI, tidI);
                int pixelJ = mad24(groupIdxJ, localSizeJ, tidJ);
                int idx = mad24(pixelI, imageWidth, pixelJ);
                int w = pixelI < imageHeight && pixelJ < imageWidth ? 1 : 0;

                if (w == 1) {
                    us[idx] = usNew[idx];
                    vs[idx] = vsNew[idx];
                }       
            }
        }        
    }
    
    protected void computeTotalError() {
        float totalError = 0.0f;

        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        int validGroupsI = imageHeight / localSizeI;
        int validGroupsJ = imageWidth / localSizeJ;
        if (imageHeight % localSizeI != 0) {
            validGroupsI++;
        }
        if (imageWidth % localSizeJ != 0) {
            validGroupsJ++;
        }
        
        int groupsPerTidI = validGroupsI / localSizeI;
        if (validGroupsI % localSizeI != 0) {
            groupsPerTidI++;
        }

        int groupsPerTidJ = validGroupsJ / localSizeJ;
        if (validGroupsJ % localSizeJ != 0) {
            groupsPerTidJ++;
        }

        for (int groupsPerTidIdxI = 0; groupsPerTidIdxI < groupsPerTidI; groupsPerTidIdxI++) {
            int groupIdxI = mad24(groupsPerTidIdxI, localSizeI, tidI); 
            for (int groupsPerTidIdxJ = 0; groupsPerTidIdxJ < groupsPerTidJ; groupsPerTidIdxJ++) {
                int groupIdxJ = mad24(groupsPerTidIdxJ, localSizeJ, tidJ);
                int idx = mad24(groupIdxI, validGroupsJ, groupIdxJ);
                float w = groupIdxI >= validGroupsI || groupIdxJ >= validGroupsJ ? 0.0f : 1.0f;
                totalError = mad(totalErrorGlobal[idx + 1], w, totalError);
            }
        }
        totalError = reduceTotalErrorToLocal(totalError);
        if (tidI == 0 && tidJ == 0) {
            totalError /= (float)(imageHeight * imageWidth);
            totalErrorGlobal[0] = totalError;
        }
    }
    
    protected float reduceTotalErrorToLocal(float totalError) {
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        int idx = mad24(tidI, localSizeJ, tidJ);
        
        multiWorkBuffer[idx] = totalError;
        localBarrier();
        for (int halfSize = localSizeI * localSizeJ / 2; halfSize >= 1; halfSize /= 2) {
            if (idx < halfSize) {
                multiWorkBuffer[idx] += multiWorkBuffer[idx + halfSize];
            }
            localBarrier();
        }
        
        return multiWorkBuffer[0];
    }
        
    protected void doDenseLiuShen(int passId) {
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);

        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        int numGroupsI = getNumGroups(1);
        int numGroupsJ = getNumGroups(0);
        
        int startLocI = gidI * localSizeI;
        int startLocJ = gidJ * localSizeJ;

        int validGroupsI = imageHeight / localSizeI;
        int validGroupsJ = imageWidth / localSizeJ;
        if (imageHeight % localSizeI != 0) {
            validGroupsI++;
        }
        if (imageWidth % localSizeJ != 0) {
            validGroupsJ++;
        }
        int validGroups = validGroupsI * validGroupsJ;

        if (startLocI >= imageHeight || startLocJ >= imageWidth) {
            //Reject workgroups that are completely outside of the image
            waitOnThreads();
            return;
        }
        
        if (totalErrorGlobal[0] < 1e-8f) {
            waitOnThreads();
            return;
        }
        
        doClipImage();
        computeFixedImageDerivatives();
        loadVectors();
        float totalError = refineVectors();
        totalError = reduceTotalErrorToLocal(totalError);
        
        if (gidI < validGroupsI && gidJ < validGroupsJ && tidI == 0 && tidJ == 0) {
            int groupIdx = mad24(gidI, validGroupsJ, gidJ);
            totalErrorGlobal[groupIdx + 1] = totalError;
        }
        globalBarrier();

        //Signal when the last workgroup passes through
        if (tidI == 0 && tidJ == 0 && atomicInc(initCompleted[1]) == validGroups - 1) {
            atomicSet(initCompleted[0], -1);
            atomicSet(initCompleted[1],  0);
        }

        int grpIdx = mad24(gidI, numGroupsJ, gidJ);
        //Ensure that all the same workgroup threads wait for the atomic update
        globalBarrier();
        atomicCmpXchg(initCompleted[0], -1, grpIdx);
        //Ensure that all threads see the change above (this should be a mem_fence instead of barrier, but Aparapi does not support it yet).
        globalBarrier();
        //Ensure that all the workitems from the last work-group that runs, work together to copy the results vectors
        if (grpIdx == atomicGet(initCompleted[0])) {
            copyVectors();
            computeTotalError();
        }

        waitOnThreads();
        vectorsCopied();
        totalErrorComputed();
    }

    @NoCL
    protected void vectorsCopied() {};

    @NoCL
    protected void totalErrorComputed() {};
        
    /**
     * A Bug in Aparapi makes it unable to detect inherited methods marked with NoCL.
     */
    @NoCL
    protected void waitOnThreads() {};
    
    @Override
    public void run() {
        setUpPrivateMemory();
        int passId = getPassId();
        if (passId == 0) {
            doDenseLucasKanade();
        } else {
            doDenseLiuShen(passId);
        }
        waitOnThreads();
    }

}
