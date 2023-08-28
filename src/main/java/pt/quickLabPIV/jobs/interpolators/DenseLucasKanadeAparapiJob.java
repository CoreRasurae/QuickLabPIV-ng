// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.exception.CompileFailedException;
import com.aparapi.exception.QueryFailedException;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobResultEnum;

public class DenseLucasKanadeAparapiJob extends Job<OpticalFlowInterpolatorInput, OpticalFlowInterpolatorInput> {
    private static final Logger logger = LoggerFactory.getLogger(DenseLucasKanadeAparapiJob.class);
    //
    private IDenseLucasKanadeKernel kernelLK;
    private Kernel kernel;
    private ComputationDevice computeDevice;    
    //
    private int imageHeight;
    private int imageWidth;
    private float imageA[];
    private float imageB[];
    private float us[];
    private float vs[];
    private boolean halfPixelOffset;
    private int windowSize;
    private int iterations;
    //
    private int workGroupSizeI;
    private int workGroupSizeJ;
    private int blockSizeI;
    private int blockSizeJ;
    private int numberOfBlocksIPerWorkGroup;
    private int numberOfBlocksJPerWorkGroup;
    private int workItemsI;
    private int workItemsJ;
    
    private boolean compiled = false;
    
    public DenseLucasKanadeAparapiJob(ComputationDevice _device) {
        computeDevice = _device;
    }
    
    @Override
    public void analyze() {
        OpticalFlowInterpolatorInput input = getInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        us = input.us;
        vs = input.vs;
        //LM Alternate Symmetric Method - continued below (seems to be less precise)
        /*imageB = input.imageA.exportTo1DFloatArray(imageB);
        imageA = input.imageB.exportTo1DFloatArray(imageA);*/
        imageA = input.imageA.exportTo1DFloatArray(imageA);
        imageB = input.imageB.exportTo1DFloatArray(imageB);
        imageHeight = input.imageA.getHeight();
        imageWidth = input.imageA.getWidth();
        halfPixelOffset = input.halfPixelOffset;
        LucasKanadeOptions options = (LucasKanadeOptions)input.options;
        
        workGroupSizeI = 8;
        workGroupSizeJ = 8;
        
        if (computeDevice.getDeviceName().toUpperCase().contains("NVIDIA")) {
            blockSizeI = DenseLucasKanadeGpuNVIDIAKernel.MAX_BLOCK_SIZE_I;
            blockSizeJ = DenseLucasKanadeGpuNVIDIAKernel.MAX_BLOCK_SIZE_J;            
        } else {
            blockSizeI = DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_I;
            blockSizeJ = DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_J;            
        }
        
        if (us.length != vs.length) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Input velocity vectors are invalid, as they do not have the same dimensions");
        }
        
        if (imageA.length != imageB.length) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Input images are invalid, as they do not have the same dimensions");
        }
        
        if (imageA.length != us.length) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Image dimensions do not match vectors dimensions");
        }
        
        if (options.windowSize < 3) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Window size too small");
        }

        if (options.windowSize > 31) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Window size too large");
        }
        
        if (options.iterations <= 0) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Number of iterations must greater or equal to 1");
        }
        
        if (blockSizeI > workGroupSizeI) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Block size I must be a less or equal to work group size I");
        }

        if (blockSizeJ > workGroupSizeJ) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Block size J must be a less or equal to work group size J");
        }

        if (blockSizeI > DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_I) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Block size I is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_I + ")");
        }

        if (blockSizeJ > DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_J) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Block size J is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_J + ")");
        }

        int _numberOfBlocksIPerWorkGroup = (options.windowSize + blockSizeI) / workGroupSizeI;
        if ((options.windowSize + blockSizeI) % workGroupSizeI != 0) {
            _numberOfBlocksIPerWorkGroup++;
        }
        int _numberOfBlocksJPerWorkGroup = (options.windowSize + blockSizeJ) / workGroupSizeJ;
        if ((options.windowSize + blockSizeJ) % workGroupSizeJ != 0) {
            _numberOfBlocksJPerWorkGroup++;
        }

        if (_numberOfBlocksIPerWorkGroup > DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_I) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Number of blocks per workgroup I is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_I + ")");
        }

        if (_numberOfBlocksJPerWorkGroup > DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_J) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Number of blocks per workgroup J is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_J + ")");
        }
        
        workItemsI = imageHeight / blockSizeI;
        if (imageHeight % blockSizeI != 0) {
            workItemsI++;
        }
        workItemsI *= workGroupSizeI;
        
        workItemsJ = imageWidth / blockSizeJ;
        if (imageWidth % blockSizeJ != 0) {
            workItemsJ++;
        }
        workItemsJ *= workGroupSizeJ;
        
        boolean changed = false;
        if (_numberOfBlocksIPerWorkGroup != numberOfBlocksIPerWorkGroup ||
            _numberOfBlocksJPerWorkGroup != numberOfBlocksJPerWorkGroup ||
            options.windowSize != windowSize ||
            options.iterations != iterations) {
            changed = true;            
        }

        windowSize = options.windowSize;
        iterations = options.iterations;
        
        numberOfBlocksIPerWorkGroup = _numberOfBlocksIPerWorkGroup;
        numberOfBlocksJPerWorkGroup = _numberOfBlocksJPerWorkGroup; 

        compileKernel(changed);
        
        try {
            if (kernel.getKernelMaxWorkGroupSize(computeDevice.getAparapiDevice()) < (workGroupSizeI * workGroupSizeJ)) {
                throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Device " + computeDevice.getDeviceName() + 
                                              " does not allow the required workgroup size of: " + (workGroupSizeI * workGroupSizeJ));
            }
        } catch (QueryFailedException e) {
            throw new JobAnalyzeException("dense Lucas-Kanade Aparapi job: Failed to query compiled kernel for the maximum allowed work group size.", e);
        }
    }

    private void compileKernel(boolean changed) {
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null || changed) {
            if (computeDevice.getDeviceName().toUpperCase().contains("NVIDIA")) {
                kernelLK = new DenseLucasKanadeGpuNVIDIAKernel(workGroupSizeI, workGroupSizeJ, blockSizeI, blockSizeJ, 
                                                       numberOfBlocksIPerWorkGroup, numberOfBlocksJPerWorkGroup,
                                                       windowSize, iterations, imageHeight, imageWidth);
            } else {
                kernelLK = new DenseLucasKanadeGpuKernel(workGroupSizeI, workGroupSizeJ, blockSizeI, blockSizeJ, 
                                                       numberOfBlocksIPerWorkGroup, numberOfBlocksJPerWorkGroup,
                                                       windowSize, iterations, imageHeight, imageWidth);
            }
            kernel = (Kernel)kernelLK;
            kernel.setExplicit(true);
        }
        try {
        	if (!compiled) {
        		kernel.compile(device);
        		compiled = true;
        	}
        } catch (CompileFailedException e) {
            throw new JobAnalyzeException("Failed to compile kernel for device: " + computeDevice.getDeviceName() + ", with id"
                    + computeDevice.getDeviceId());
        }
    }
    
    @Override
    public void compute() {
        Range range = Range.create2D(computeDevice.getAparapiDevice(), workItemsJ, workItemsI, workGroupSizeJ, workGroupSizeI);

        kernelLK.setKernelArgs(imageA, imageB, us, vs, halfPixelOffset);
        kernel.put(imageA);
        kernel.put(imageB);
        kernel.put(us);
        kernel.put(vs);
        kernel.execute(range);
        kernel.get(us);
        kernel.get(vs);
        logger.info("Dense Lucas-Kanade finished computing on the GPU device: {}", computeDevice);
        
        OpticalFlowInterpolatorInput inputConfig = getInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        //LM Alternate Symmetric Method
        /*for (int i = 0; i < us.length; i++) {
            us[i] = -us[i];
            vs[i] = -vs[i];
        }*/
        inputConfig.us = us;
        inputConfig.vs = vs;
        
        setJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, inputConfig);
    }

    @Override
    public void dispose() {
        if (kernel != null) {
            kernel.dispose();
            kernel = null;
            compiled = false;
        }
    }
    
    @Override
    public void finalize() {
    	dispose();
    }

}
