// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeGpuKernel;

public class DenseLucasKanadeGpuTestKernel extends DenseLucasKanadeGpuKernel {

    private float pixelValuesGlobal [][][][];
    private float dIsGlobal [][][][];
    private float dJsGlobal [][][][];
    
    public DenseLucasKanadeGpuTestKernel(int localSizeI, int localSizeJ, int _blockSizeI, int _blockSizeJ, int _blockItemsPerWorkGroupI,
            int _blockItemsPerWorkGroupJ, int _windowSize, int _iterations, int _imageHeight, int _imageWidth) {
        super(localSizeI, localSizeJ, _blockSizeI, _blockSizeJ, _blockItemsPerWorkGroupI, _blockItemsPerWorkGroupJ, _windowSize, _iterations,
                _imageHeight, _imageWidth);
        testing = 1;
        pixelValuesGlobal = new float [blockSizeI][blockSizeJ][_windowSize][_windowSize];
        dIsGlobal = new float [blockSizeI][blockSizeJ][_windowSize][_windowSize];
        dJsGlobal = new float [blockSizeI][blockSizeJ][_windowSize][_windowSize];
    }

    public interface IDenseLucasKanadeListener {
        public void imageRegionRead(float[] localImgBuffer, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ, int localIdI, int localIdJ);
        public void matrixAComputed(float[] A00, float[] A01, float[] A11, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ, int localIdI, int localIdJ);
        public void matrixAInverted(float[] A00, float[] A01, float[] A11, float detA, int globalIdI, int globalIdJ, int groupIdI, int groupIdJ, int localIdI, int localIdJ);
        public void matrixBComputed(float[][] b0s, float[][] b1s, int iterations, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
        public void pixelValuesAndDIsAndDJsComputed(float[][][][] pixelValues, float[][][][] dIs,
                float[][][][] dJs, int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ);
    }
    
    private IDenseLucasKanadeListener listener;
    
    @NoCL
    public void registerListener(IDenseLucasKanadeListener _listener) {
        listener = _listener;
    }
    
    protected void imageReadCompleted() {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (tidI == 0 && tidJ == 0) {
            if (listener != null) {
                listener.imageRegionRead(getLocalImgBuffer(), idI, idJ, gidI, gidJ, tidI, tidJ);
            }
        }
        //Ensure that all threads wait for the listener thread, just until the test checking is completed.
        //IF this is not done, all other threads can start processing the next group before this group is completed.
        //Maybe this is an Aparapi Bug... maybe Aparapi should wait for all threads before starting a new group computation, when in ThreadPool mode.
        waitOnThreads();
    }

    @NoCL
    protected void computedPixelValuesDJsDIs(float[] pixelValues, float[] dJs, float[] dIs) {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int localSizeI = getLocalSize(1);
        int localSizeJ = getLocalSize(0);

        for (int bI = 0; bI < blockSizeI; bI++) {
            for (int bJ = 0; bJ < blockSizeJ; bJ++) {
                for (int bigI = 0; bigI < blockItemsPerWorkGroupI; bigI++) {
                    int pixelI = bigI * localSizeI + tidI;
                    for (int bigJ = 0; bigJ < blockItemsPerWorkGroupJ; bigJ++) {
                        int pixelJ = bigJ * localSizeJ + tidJ;
                        //Index in the private memory to read the data
                        int idxSource = (bI * blockSizeJ + bJ) * blockItemsPerWorkGroupI*blockItemsPerWorkGroupJ + bigI * blockItemsPerWorkGroupJ + bigJ;

                        if (pixelI >= bI && pixelI < windowSize + bI && pixelJ >= bJ && pixelJ < windowSize + bJ) {
                            pixelValuesGlobal[bI][bJ][pixelI-bI][pixelJ-bJ] = pixelValues[idxSource];
                            dIsGlobal[bI][bJ][pixelI-bI][pixelJ-bJ] = dIs[idxSource];
                            dJsGlobal[bI][bJ][pixelI-bI][pixelJ-bJ] = dJs[idxSource];
                        }
                    }
                }
            }
        }            

        waitOnThreads();
        if (tidI == 0 && tidJ == 0) {
            if (listener != null) {
                listener.pixelValuesAndDIsAndDJsComputed(pixelValuesGlobal, dIsGlobal, dJsGlobal, idI, idJ, gidI, gidJ, tidI, tidJ);
            }
        }
        waitOnThreads();
    }
    
    @NoCL
    protected void matrixAComputed(float A00[], float A01[], float A11[]) {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (tidI == 0 && tidJ == 0) {
            if (listener != null) {
                listener.matrixAComputed(A00, A01, A11, idI, idJ, gidI, gidJ, tidI, tidJ);
            }
        }
    }
    
    @NoCL
    protected void matrixAInverted(float A00[], float A01[], float A11[], float detA) {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);

        if (listener != null) {
            listener.matrixAInverted(A00, A01, A11, detA, idI, idJ, gidI, gidJ, tidI, tidJ);
        }
    }

    @NoCL
    protected void matrixBComputed(int iter) {
        int idI = getGlobalId(1);
        int idJ = getGlobalId(0);
        
        int tidI = getLocalId(1);
        int tidJ = getLocalId(0);
        
        int gidI = getGroupId(1);
        int gidJ = getGroupId(0);
        
        int localSizeI = getLocalSize(0);
        int localSizeJ = getLocalSize(1);
        
        int threadsSize = localSizeI * localSizeJ;
        int b1Offset = blockSizeI * blockSizeJ * threadsSize;
        
        if (tidI == 0 && tidJ == 0) {
            if (listener != null) {
                float b0s[][] = new float[blockSizeI][blockSizeJ];
                float b1s[][] = new float[blockSizeI][blockSizeJ];
                
                for (int bI = 0; bI < blockSizeI; bI++) {
                    int blockOffsetI = bI * blockSizeJ;
                    for (int bJ = 0; bJ < blockSizeJ; bJ++) {
                        int offset = (blockOffsetI + bJ) * threadsSize;
                        b0s[bI][bJ] = multiWorkBuffer[offset];
                        b1s[bI][bJ] = multiWorkBuffer[offset + b1Offset];
                    }
                }
                
                listener.matrixBComputed(b0s, b1s, iter, idI, idJ, gidI, gidJ, tidI, tidJ);
            }
        }        
    }
    
    @NoCL
    protected void waitOnThreads() {
        //Ensure that all threads wait for the listener thread, just until the test checking is completed.
        //IF this is not done, all other threads can start processing the next group before this group is completed.
        //Maybe this is an Aparapi Bug... maybe Aparapi should wait for all threads before starting a new group computation, when in ThreadPool mode.
        
        //NOTE: A CyclicBarrier could be used instead, however, it could make Aparapi unable to terminate all the worker threads, leaving some sleeping in this
        //cyclic barrier, thus causing the ExecutorsThreadPool to stall without finishing. Such situations can happen when Junit test assertion fails with some
        //threads sleeping in the cyclic barrier, thus failing to notify the user of the validation error and making the problem look like a deadlock in the code.
        localBarrier();
    }

    @NoCL
    public float[] getUs() {
        return us;
    }
    
    @NoCL
    public float[] getVs() {
        return vs;
    }
    
    @Override
    public void run() {
        super.run();
    }
}
