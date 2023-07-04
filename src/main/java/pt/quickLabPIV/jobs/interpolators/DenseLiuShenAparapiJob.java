package pt.quickLabPIV.jobs.interpolators;

import java.util.Arrays;

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

public class DenseLiuShenAparapiJob extends Job<OpticalFlowInterpolatorInput, OpticalFlowInterpolatorInput> {
    private static final Logger logger = LoggerFactory.getLogger(DenseLiuShenAparapiJob.class);
    //
    private IDenseLiuShenKernel kernelLiuShen;
    private Kernel kernel;
    private ComputationDevice computeDevice;
    private boolean compiled = false;
    //
    private int imageHeight;
    private int imageWidth;
    private float imageLKA[];
    private float imageLKB[];
    private float imageLSA[];
    private float imageLSB[];
    private float us[];
    private float vs[];
    private float usNew[];
    private float vsNew[];
    private float totalError[];
    private boolean halfPixelOffset;
    private int windowSizeLK;
    private int iterationsLK;
    private float lambdaLS;
    private int iterationsLS;
    //
    private int workGroupSizeI;
    private int workGroupSizeJ;
    private int blockSizeI;
    private int blockSizeJ;
    private int numberOfBlocksIPerWorkGroup;
    private int numberOfBlocksJPerWorkGroup;
    private int workItemsI;
    private int workItemsJ;
    
    public DenseLiuShenAparapiJob(ComputationDevice _device) {
        computeDevice = _device;
    }
    
    @Override
    public void analyze() {
        OpticalFlowInterpolatorInput input = getInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        us = input.us;
        vs = input.vs;
        imageLKA = input.imageA.exportTo1DFloatArray(imageLKA);
        imageLKB = input.imageB.exportTo1DFloatArray(imageLKB);
        imageHeight = input.imageA.getHeight();
        imageWidth = input.imageA.getWidth();
        halfPixelOffset = input.halfPixelOffset;
        LiuShenOptions options = (LiuShenOptions)input.options;
        
        imageLSA = options.imageLSA.exportTo1DFloatArray(imageLSA);
        imageLSB = options.imageLSB.exportTo1DFloatArray(imageLSB);
        
        blockSizeI = 2;
        blockSizeJ = 2;
        workGroupSizeI = 8;
        workGroupSizeJ = 8;
        
        if (us.length != vs.length) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Input velocity vectors are invalid, as they do not have the same dimensions");
        }
        
        if (imageLKA.length != imageLKB.length) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Input images are invalid, as they do not have the same dimensions");
        }
        
        if (imageLKA.length != us.length) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Image dimensions do not match vectors dimensions");
        }
        
        if (options.windowSizeLK < 3) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Window size too small");
        }

        if (options.windowSizeLK > 31) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Window size too large");
        }
        
        if (options.iterationsLK <= 0) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Number of iterations for Lucas-Kanade must be greater or equal to 1");
        }

        if (options.iterationsLS <= 0) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Number of iterations for Liu-Shen must be greater or equal to 1");
        }

        if (blockSizeI > workGroupSizeI) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Block size I must be a less or equal to work group size I");
        }

        if (blockSizeJ > workGroupSizeJ) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Block size J must be a less or equal to work group size J");
        }

        if (blockSizeI > DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_I) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Block size I is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_I + ")");
        }

        if (blockSizeJ > DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_J) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Block size J is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_SIZE_J + ")");
        }

        int _numberOfBlocksIPerWorkGroup = (options.windowSizeLK + blockSizeI) / workGroupSizeI;
        if ((options.windowSizeLK + blockSizeI) % workGroupSizeI != 0) {
            _numberOfBlocksIPerWorkGroup++;
        }
        int _numberOfBlocksJPerWorkGroup = (options.windowSizeLK + blockSizeJ) / workGroupSizeJ;
        if ((options.windowSizeLK + blockSizeJ) % workGroupSizeJ != 0) {
            _numberOfBlocksJPerWorkGroup++;
        }

        if (_numberOfBlocksIPerWorkGroup > DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_I) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Number of blocks per workgroup I is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_I + ")");
        }

        if (_numberOfBlocksJPerWorkGroup > DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_J) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Number of blocks per workgroup J is too large for the static kernel configuration (max. allowed: " + DenseLucasKanadeGpuKernel.MAX_BLOCK_ITEMS_PER_WORKGROUP_J + ")");
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
            options.windowSizeLK != windowSizeLK ||
            options.iterationsLK != iterationsLK ||
            options.lambdaLS != lambdaLS) {
            changed = true;            
        }

        windowSizeLK = options.windowSizeLK;
        iterationsLK = options.iterationsLK;
        lambdaLS = options.lambdaLS;
        iterationsLS = options.iterationsLS;
        
        numberOfBlocksIPerWorkGroup = _numberOfBlocksIPerWorkGroup;
        numberOfBlocksJPerWorkGroup = _numberOfBlocksJPerWorkGroup; 

        if (usNew == null || usNew.length < imageHeight * imageWidth) {
            usNew = new float[imageHeight * imageWidth];
        }
        
        if (vsNew == null || vsNew.length < imageHeight * imageWidth) {
            vsNew = new float[imageHeight * imageWidth];
        }
        
        int validGroupsI = imageHeight / workGroupSizeI;
        if (imageHeight % workGroupSizeI != 0) {
            validGroupsI++;
        }
        int validGroupsJ = imageWidth / workGroupSizeJ;
        if (imageWidth % workGroupSizeJ != 0) {
            validGroupsJ++;
        }
        
        if (totalError == null || totalError.length < validGroupsI * validGroupsJ + 1) {
            totalError = new float[validGroupsI * validGroupsJ + 1];
        }
        
        Arrays.fill(usNew, 0.0f);
        Arrays.fill(vsNew, 0.0f);
        Arrays.fill(totalError, 0.0f);
        
        compileKernel(changed);
        
        try {
            if (kernel.getKernelMaxWorkGroupSize(computeDevice.getAparapiDevice()) < (workGroupSizeI * workGroupSizeJ)) {
                throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Device " + computeDevice.getDeviceName() + 
                                              " does not allow the required workgroup size of: " + (workGroupSizeI * workGroupSizeJ));
            }
        } catch (QueryFailedException e) {
            throw new JobAnalyzeException("dense Liu-Shen Aparapi job: Failed to query compiled kernel for the maximum allowed work group size.", e);
        }
    }

    private void compileKernel(boolean changed) {
        Device device = computeDevice.getAparapiDevice();
        
        if (kernel == null || changed) {
            if (computeDevice.getDeviceName().toUpperCase().contains("NVIDIA")) {
                kernelLiuShen = new DenseLiuShenGpuNVIDIAKernel(workGroupSizeI, workGroupSizeJ, blockSizeI, blockSizeJ, 
                                                         numberOfBlocksIPerWorkGroup, numberOfBlocksJPerWorkGroup,
                                                         windowSizeLK, iterationsLK, imageHeight, imageWidth, iterationsLS, lambdaLS);
            } else {
                kernelLiuShen = new DenseLiuShenGpuKernel(workGroupSizeI, workGroupSizeJ, blockSizeI, blockSizeJ, 
                                                   numberOfBlocksIPerWorkGroup, numberOfBlocksJPerWorkGroup,
                                                   windowSizeLK, iterationsLK, imageHeight, imageWidth, iterationsLS, lambdaLS);
            }
            compiled = false;
            kernel = (Kernel)kernelLiuShen;
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

        totalError[0] = Float.MAX_VALUE;
        
        kernelLiuShen.setKernelArgs(imageLKA, imageLKB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, halfPixelOffset);
        kernel.put(imageLKA);
        kernel.put(imageLKB);
        kernel.put(imageLSA);
        kernel.put(imageLSB);
        kernel.put(us);
        kernel.put(vs);
        kernel.put(usNew);
        kernel.put(vsNew);
        kernel.put(totalError);
        kernel.execute(range, iterationsLS + 1);
        kernel.get(us);
        kernel.get(vs);
        kernel.get(totalError);
        System.err.println(totalError[0]);
        logger.info("Dense Liu-Shen finished computing on the GPU device: {}", computeDevice);       
        
        OpticalFlowInterpolatorInput inputConfig = getInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
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
