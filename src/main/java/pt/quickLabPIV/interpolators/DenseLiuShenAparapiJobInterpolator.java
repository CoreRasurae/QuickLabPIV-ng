package pt.quickLabPIV.interpolators;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.IgnorePIVBaseDisplacementsModeEnum;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.BiCubicSplineInterpolatorWithBiLinearBackup;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.interpolators.DenseLiuShenAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LiuShenOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class DenseLiuShenAparapiJobInterpolator implements IOpticalFlowInterpolator {
	private final static Logger logger = LoggerFactory.getLogger(DenseLiuShenAparapiJobInterpolator.class);

	private boolean ignorePIV;
	private IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;
	//--
    private int iterationsLK;
    private int windowSizeLK;
    private float filterSigmaLK;
    private int filterWidthPxLK;
    //---
    private int iterationsLS;
    private float lambdaLS;
    private float filterSigmaLS;
    private int filterWidthPxLS;
    private int vectorsWindowSizeLS;
    
    private IFilter filterLK;
    private IFilter filterLS;
    private IImage filteredImgLKA;
    private IImage filteredImgLKB;
    private ImageFloat filteredImgLSA;
    private ImageFloat filteredImgLSB;

    private float usAndVs[][];
    private boolean denseVectors;
    
    private DenseLiuShenAparapiJob dLSJob;
    private OpticalFlowInterpolatorInput input;
    
    
    public DenseLiuShenAparapiJobInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters pivParameters = singleton.getPIVParameters(); 
        Object configurationObject = pivParameters.getSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER_APARAPI);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Liu-Shen with Lucas-Kanade interpolator configuration");
        }
        
        LiuShenInterpolatorConfiguration configuration = (LiuShenInterpolatorConfiguration)configurationObject;
        
        ignorePIV = configuration.isIgnorePIVBaseDisplacements();
        ignorePIVMode = configuration.getIgnorePIVBaseDisplacementsMode();
        //-------
        iterationsLK = configuration.getNumberOfIterationsLK();
        windowSizeLK = configuration.getWindowSizeLK();
        filterSigmaLK = configuration.getFilterSigmaLK();
        filterWidthPxLK = configuration.getFilterWidthPxLK();
        //-------
        iterationsLS = configuration.getNumberOfIterationsLS();
        lambdaLS = configuration.getMultiplierLagrangeLS();
        filterSigmaLS = configuration.getFilterSigmaLS();
        filterWidthPxLS = configuration.getFilterWidthPxLS();
        vectorsWindowSizeLS = configuration.getVectorsWindowSizeLS();

        denseVectors = configuration.isDenseVectors();
        
        filterLK = new GaussianFilter2D(filterSigmaLK, filterWidthPxLK);
        filterLS = new GaussianFilter2D(filterSigmaLS, filterWidthPxLS);        
    }
    
    private void initdLSJobIfRequired() {
    	if (dLSJob == null) {
    		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
    		PIVRunParameters runParameters = singleton.getPIVRunParameters();
    		ComputationDevice gpuDevice = runParameters.getComputationDeviceForThread();
    		if (gpuDevice == null) {
    			logger.warn("Could not find GPU device for thread. Using default GPU device");
    			gpuDevice = DeviceManager.getSingleton().getGPU();
    		}
            dLSJob = new DenseLiuShenAparapiJob(gpuDevice);
    	}
    }

    @Override
    public List<MaxCrossResult> interpolate(List<MaxCrossResult> results) {
    	initdLSJobIfRequired();
    	//
        IImage localImgLKA = null;
        IImage localImgLKB = null;
        IImage localImgLSA = null;
        IImage localImgLSB = null;
        float us[] = new float[(vectorsWindowSizeLS + windowSizeLK - 1) * (vectorsWindowSizeLS + windowSizeLK - 1)];
        float vs[] = new float[(vectorsWindowSizeLS + windowSizeLK - 1) * (vectorsWindowSizeLS + windowSizeLK - 1)];
        
        
        for (MaxCrossResult result : results) {
            Tile tileB = result.getTileB();
            IterationStepTiles stepTilesB =  tileB.getParentIterationStepTiles();
            for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
                final float fractionalTileU = tileB.getDisplacementV() - (float)FastMath.floor(tileB.getDisplacementV());
                final float fractionalTileV = tileB.getDisplacementU() - (float)FastMath.floor(tileB.getDisplacementU());
                float u = result.getDisplacementFromNthPeakV(peakIndex) + fractionalTileU;
                float v = result.getDisplacementFromNthPeakU(peakIndex) + fractionalTileV;
                if (tileB.isMaskedDisplacement()) {
                    u = 0.0f;
                    v = 0.0f;
                } else if (ignorePIV) {
                    boolean ignoreU = false;
                    boolean ignoreV = false;
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.Auto) {
                        if (FastMath.abs(result.getDisplacementFromNthPeakV(peakIndex)) < 3.0f) {
                            ignoreU = true;
                        }
                        if (FastMath.abs(result.getDisplacementFromNthPeakU(peakIndex)) < 3.0f) {
                            ignoreV = true;
                        }
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.AutoSmall) {
                        if (FastMath.abs(result.getDisplacementFromNthPeakV(peakIndex)) < 0.9f) {
                            ignoreU = true;
                        }
                        if (FastMath.abs(result.getDisplacementFromNthPeakU(peakIndex)) < 0.9f) {
                            ignoreV = true;
                        }
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreV || 
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreV) {
                        //We cannot disregard previous window displacements contribution, we can only ignore the current cross-correlation results
                        v = fractionalTileV;
                    }
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                        u = fractionalTileU;
                    }
                }
                
                int centerI = tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1;
                int centerJ = tileB.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1;
                localImgLKA = filteredImgLKA.clipImage(centerI - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       centerJ - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       vectorsWindowSizeLS + windowSizeLK - 1, vectorsWindowSizeLS + windowSizeLK - 1, true, localImgLKA);
                localImgLKB = filteredImgLKB.clipImage((int)(centerI - vectorsWindowSizeLS/2 - windowSizeLK/2 + FastMath.floor(tileB.getDisplacementU())), 
                                                       (int)(centerJ - vectorsWindowSizeLS/2 - windowSizeLK/2 + FastMath.floor(tileB.getDisplacementV())), 
                                                       vectorsWindowSizeLS + windowSizeLK - 1, vectorsWindowSizeLS + windowSizeLK - 1, true, localImgLKB);
                
                localImgLSA = filteredImgLSA.clipImage(centerI - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       centerJ - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       vectorsWindowSizeLS + windowSizeLK - 1, vectorsWindowSizeLS + windowSizeLK - 1, true, localImgLSA);
                localImgLSB = filteredImgLSB.clipImage(centerI - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       centerJ - vectorsWindowSizeLS/2 - windowSizeLK/2, 
                                                       vectorsWindowSizeLS + windowSizeLK - 1, vectorsWindowSizeLS + windowSizeLK - 1, true, localImgLSB);
                
                if (input == null) {
                    input = new OpticalFlowInterpolatorInput();
                    if (input.options == null) {
                        input.options = new LiuShenOptions();
                    }
                }

                Arrays.fill(us, u);
                Arrays.fill(vs, v);
                
                final LiuShenOptions options = (LiuShenOptions)input.options;         
                options.iterationsLK = iterationsLK;
                options.windowSizeLK = windowSizeLK;
                options.iterationsLS = iterationsLS;
                options.lambdaLS = lambdaLS;
                options.imageLSA = localImgLSA;
                options.imageLSB = localImgLSB;        
                input.imageA = localImgLKA;
                input.imageB = localImgLKB;
                //Swap U and V since PIV has swapped axis
                input.us = us;
                input.vs = vs;
                input.halfPixelOffset = true;
                
                dLSJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, input);
                
                dLSJob.analyze();
                dLSJob.compute();
                
                OpticalFlowInterpolatorInput opfResult = dLSJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
                int idx = (vectorsWindowSizeLS / 2 + windowSizeLK / 2) * (vectorsWindowSizeLS + windowSizeLK - 1) + (vectorsWindowSizeLS / 2 + windowSizeLK / 2);
                u = opfResult.us[idx];
                v = opfResult.vs[idx];
                
                if (tileB.isMaskedDisplacement()) {
                    result.setNthAbsoluteDisplacement(peakIndex, 0.0f, 0.0f);
                } else {
                    u -= fractionalTileU;
                    v -= fractionalTileV;
                    
                    result.setNthRelativeDisplacementFromVelocities(peakIndex, v, u);                    
                }
            }
        }
        
        return results;
    }

    @Override
    public void interpolate(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
    	initdLSJobIfRequired();
    	//
    	BiCubicSplineInterpolatorWithBiLinearBackup interp = BiCubicSplineInterpolatorWithBiLinearBackup.createTileDisplacementInterpolator(stepTilesB);
        
    	usAndVs = interp.interpolateDisplacements(filteredImgLKA.getHeight(), filteredImgLKA.getWidth(), denseVectors ? 0.0f : 0.5f, denseVectors ? 0.0f : 0.5f, usAndVs);
    	if (ignorePIV) {
            if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.Auto) {
                final int height = filteredImgLKA.getHeight();
                final int width = filteredImgLKA.getWidth();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        float u = usAndVs[0][i * width + j];
                        float v = usAndVs[1][i * width + j];
                        if (FastMath.abs(u) < 3.0f) { 
                            usAndVs[0][i * width + j] = 0.0f;
                        }
                        
                        if (FastMath.abs(v) < 3.0f) {
                            usAndVs[1][i * width + j] = 0.0f;
                        }
                    }
                }                
            }
            
            if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.AutoSmall) {
                final int height = filteredImgLKA.getHeight();
                final int width = filteredImgLKA.getWidth();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        float u = usAndVs[0][i * width + j];
                        float v = usAndVs[1][i * width + j];
                        if (FastMath.abs(u) < 0.9f) { 
                            usAndVs[0][i * width + j] = 0.0f;
                        }
                        
                        if (FastMath.abs(v) < 0.9f) {
                            usAndVs[1][i * width + j] = 0.0f;
                        }
                    }
                }                
            }            

    	    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreV || 
                ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV) {
                Arrays.fill(usAndVs[0], denseVectors ? 0.0f : 0.5f);
            }
            if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV) { 
                Arrays.fill(usAndVs[1], denseVectors ? 0.0f : 0.5f);
            }
    	}    	    
        
        if (input == null) {
            input = new OpticalFlowInterpolatorInput();
            if (input.options == null) {
                input.options = new LiuShenOptions();
            }
        }

        final LiuShenOptions options = (LiuShenOptions)input.options;         
        options.iterationsLK = iterationsLK;
        options.windowSizeLK = windowSizeLK;
        options.iterationsLS = iterationsLS;
        options.lambdaLS = lambdaLS;
        options.imageLSA = filteredImgLSA;
        options.imageLSB = filteredImgLSB;        
        input.imageA = filteredImgLKA;
        input.imageB = filteredImgLKB;
        //Swap U and V since PIV has swapped axis
        input.us = usAndVs[1];
        input.vs = usAndVs[0];
        input.halfPixelOffset = denseVectors && stepTilesB.getCurrentStep() == stepTilesB.getMaxAdaptiveSteps() - 1 ? false : true;
        
        dLSJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, input);
        
        dLSJob.analyze();
        dLSJob.compute();
        
        OpticalFlowInterpolatorInput opfResult = dLSJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        
        for (int tileI = 0; tileI < stepTilesB.getNumberOfTilesInI(); tileI++) {
            for (int tileJ = 0; tileJ < stepTilesB.getNumberOfTilesInJ(); tileJ++) {
                Tile tile = stepTilesB.getTile(tileI, tileJ);
                int i = tile.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1;
                int j = tile.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1;                
                int idx = i * filteredImgLKA.getWidth() + j;
                
                if (!denseVectors || stepTilesB.getCurrentStep() != stepTilesB.getMaxAdaptiveSteps() - 1) {
                    float u = opfResult.us[idx];
                    float v = opfResult.vs[idx];
                    if (tile.isMaskedDisplacement()) {
                        v = 0.0f;
                        u = 0.0f;
                    }
                    tile.replaceDisplacement(v, u);
                } else {
                    int idx2 = (i + 1) * filteredImgLKA.getWidth() + j;
                    float u = (opfResult.us[idx] + opfResult.us[idx + 1] + opfResult.us[idx2] + opfResult.us[idx2 + 1]) / 4.0f;
                    float v = (opfResult.vs[idx] + opfResult.vs[idx + 1] + opfResult.vs[idx2] + opfResult.vs[idx2 + 1]) / 4.0f;
                    if (tile.isMaskedDisplacement()) {
                        u = 0.0f;
                        v = 0.0f;
                    }
                    tile.replaceDisplacement(v, u);
                }
            }
        }
        
        if (denseVectors && stepTilesB.getCurrentStep() == stepTilesB.getMaxAdaptiveSteps() - 1) {
            stepTilesB.setUpdateDenseVectors(filteredImgLKA.getHeight(), filteredImgLKA.getWidth(), opfResult.vs, opfResult.us);
        }
    }
    
    @Override
    public void updateImageA(IImage img) {
        if (filteredImgLKA == null || img.getHeight() != filteredImgLKA.getHeight() || img.getWidth() != filteredImgLKA.getWidth()) {
            filteredImgLKA = ImageFloat.sizeFrom(img);
        }
        filteredImgLKA = filterLK.applyFilter(img, filteredImgLKA);

        IImage filteredImgTemp = filterLS.applyFilter(img, filteredImgLSA);
        filteredImgLSA = filteredImgTemp.normalize(filteredImgLSA);
    }
    
    @Override
    public void updateImageB(IImage img) {
        if (filteredImgLKB == null || img.getHeight() != filteredImgLKB.getHeight() || img.getWidth() != filteredImgLKB.getWidth()) {
            filteredImgLKB = ImageFloat.sizeFrom(img);
        }
        filteredImgLKB = filterLK.applyFilter(img, filteredImgLKB);

        IImage filteredImgTemp = filterLS.applyFilter(img, filteredImgLSB);
        filteredImgLSB = filteredImgTemp.normalize(filteredImgLSB);
    }

}
