// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

import pt.quickLabPIV.jobs.interpolators.DenseLiuShenGpuKernel;

public class DenseLiuShenGpuTestKernel extends DenseLiuShenGpuKernel {
    public DenseLiuShenGpuTestKernel(int localSizeI, int localSizeJ, int _blockSizeI, int _blockSizeJ,
            int _blockItemsPerWorkGroupI, int _blockItemsPerWorkGroupJ, int _windowSize, int _iterationsLK,
            int _imageHeight, int _imageWidth, int _iterationsLS, float _lambda) {
        super(localSizeI, localSizeJ, _blockSizeI, _blockSizeJ, _blockItemsPerWorkGroupI, _blockItemsPerWorkGroupJ, _windowSize,
                _iterationsLK, _imageHeight, _imageWidth, _iterationsLS, _lambda);
        testing = 1;
    }

    
    @NoCL
    @Override
    protected void waitOnThreads() {
        //Ensure that all threads wait for the listener thread, just until the test checking is completed.
        //IF this is not done, all other threads can start processing the next group before this group is completed.
        //Maybe this is an Aparapi Bug... maybe Aparapi should wait for all threads before starting a new group computation, when in ThreadPool mode.
        
        //NOTE: A CyclicBarrier could be used instead, however, it could make Aparapi unable to terminate all the worker threads, leaving some sleeping in this
        //cyclic barrier, thus causing the ExecutorsThreadPool to stall without finishing. Such situations can happen when Junit test assertion fails with some
        //threads sleeping in the cyclic barrier, thus failing to notify the user of the validation error and making the problem look like a deadlock in the code.
        localBarrier();
    }

    public interface IDenseLiuShenListener {
        void imageAClippedAndLoaded(float[] localImgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
        void imageBClippedAndLoaded(float[] localImgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
        void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
        void fixedImageDerivativesComputed(float IIx, float IIy, float II, float Ixt, float Iyt, float[] Bs, int idI, int idJ,
                int gidI, int gidJ, int tidI, int tidJ);
        void vectorsRefined(int w, float bu, float bv, float unew, float vnew, float totalError, float privUs[], float privVs[], int idI, int idJ,
                int gidI, int gidJ, int tidI, int tidJ);
        void vectorsCopied(float[] us, float[] vs, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
        void totalErrorComputed(float[] totalErrorGlobal, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
    }
    
    private IDenseLiuShenListener listener;
    
    @NoCL
    public void registerListener(IDenseLiuShenListener _listener) {
        listener = _listener;
    }

    @NoCL
    @Override
    protected void imageAClippedAndLoaded() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            listener.imageAClippedAndLoaded(getLocalImgBuffer(), idI, idJ, gidI, gidJ, tidI, tidJ);
            waitOnThreads();
        }

    };
    
    @NoCL
    @Override
    protected void imageBClippedAndLoaded() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            listener.imageBClippedAndLoaded(getLocalImgBuffer(), idI, idJ, gidI, gidJ, tidI, tidJ);
            waitOnThreads();
        }
    };
    
    @NoCL
    @Override
    protected void pixelValuesLoaded() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            listener.pixelValuesLoaded(pixelValues, idI, idJ, gidI, gidJ, tidI, tidJ);
            waitOnThreads();
        }
    }
    
    @NoCL
    @Override
    protected void fixedImageDerivativesComputed() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            listener.fixedImageDerivativesComputed(A01_IIxy[0], A01_IIxy[1], A11_II[0], locIPrv_Ixt[0], locJPrv_Iyt[0], A00_B, idI, idJ, gidI, gidJ, tidI, tidJ);
        }
    };
    
    @NoCL
    @Override
    protected void vectorsRefined(int w, float bu, float bv, float unew, float vnew, float totalError) {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        if (listener != null) {
            listener.vectorsRefined(w, bu, bv, unew, vnew, totalError, dI_privUs, dJ_privVs, idI, idJ, gidI, gidJ, tidI, tidJ);
        }
    };
    
    @NoCL
    @Override
    protected void vectorsCopied() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            if (gidI == 0 && gidJ == 0) {
                listener.vectorsCopied(us, vs, idI, idJ, gidI, gidJ, tidI, tidJ);
            }
            waitOnThreads();
        }
    };
    
    @NoCL
    @Override
    protected void totalErrorComputed() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            if (gidI == 0 && gidJ == 0) {
                listener.totalErrorComputed(totalErrorGlobal, idI, idJ, gidI, gidJ, tidI, tidJ);
            }
            waitOnThreads();
        }        
    }
}
