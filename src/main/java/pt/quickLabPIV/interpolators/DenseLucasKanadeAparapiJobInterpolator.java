// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.NotImplementedException;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class DenseLucasKanadeAparapiJobInterpolator implements IOpticalFlowInterpolator, ILiuShenOpticalFlowHelper {
	private static final Logger logger = LoggerFactory.getLogger(DenseLucasKanadeAparapiJobInterpolator.class);
	
	private boolean ignorePIV;
	private IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;
    private boolean denseVectors;

    final boolean avgOfFourPixels;
    final int iterations;
    final int windowSize;
    final float filterSigma;
    final int filterWidthPx;
    
    private IFilter filter;
    private IImage filteredImgA;
    private IImage filteredImgB;
    private float usAndVs[][];
    private Job<OpticalFlowInterpolatorInput, OpticalFlowInterpolatorInput> dLKJob;
    private OpticalFlowInterpolatorInput input;
    
    public DenseLucasKanadeAparapiJobInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters pivParameters = singleton.getPIVParameters(); 
        Object configurationObject = pivParameters.getSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER_APARAPI);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Lucas-Kanade interpolator configuration");
        }
        
        LucasKanadeInterpolatorConfiguration configuration = (LucasKanadeInterpolatorConfiguration)configurationObject;
        ignorePIV = configuration.isIgnorePIVBaseDisplacements();
        ignorePIVMode = configuration.getIgnorePIVBaseDisplacementsMode();
        
        avgOfFourPixels = configuration.getAverageOfFourPixels();
        iterations = configuration.getNumberOfIterations();
        windowSize = configuration.geWindowSize();
        filterSigma = configuration.getFilterSigma();
        filterWidthPx = configuration.getFilterWidthPx();
        denseVectors = configuration.isDenseExport();

        filter = new GaussianFilter2D(filterSigma, filterWidthPx);
    }
    
    private void zeroUsAndVs() {
        for (int idx = 0; idx < 2; idx++) {
            Arrays.fill(usAndVs[idx], 0.0f);
        }
    }
    
    private void initdLKJobIfRequired() {
    	if (dLKJob == null) {
    		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
    		PIVRunParameters runParameters = singleton.getPIVRunParameters();
    		ComputationDevice gpuDevice = runParameters.getComputationDeviceForThread();
    		if (gpuDevice == null) {
    			logger.warn("Could not find logger for thread. Using default GPU, if available.");
    			gpuDevice = DeviceManager.getSingleton().getGPU();
    		}
            dLKJob = new DenseLucasKanadeAparapiJob(gpuDevice);
    	}
    }
    
    @Override
    public List<MaxCrossResult> interpolate(final List<MaxCrossResult> results) {
    	initdLKJobIfRequired();
    	//
        MaxCrossResult result0 = results.get(0);
        //It is assumed that all Max Cross-Correlation results have the same number of peaks and belong to the same iteration parent step tile.
        final int totalPeaks = result0.getTotalPeaks();
        IterationStepTiles stepTilesB = result0.getTileB().getParentIterationStepTiles();
        
        if (usAndVs == null) {
            usAndVs = new float[2][filteredImgA.getHeight() * filteredImgA.getWidth()];
        }
        
        //zeroUsAndVs();
        
        for (int peakIdx = 0; peakIdx < totalPeaks; peakIdx++) {
           for (final MaxCrossResult result : results) {
                final Tile tileA = result.getTileA(); 
                final Tile tileB = result.getTileB();
                float v = result.getDisplacementFromNthPeakU(peakIdx) + tileB.getDisplacementU();
                float u = result.getDisplacementFromNthPeakV(peakIdx) + tileB.getDisplacementV();

                if (tileB.isMaskedDisplacement()) {
                    u = 0.0f;
                    v = 0.0f;
                } else if (ignorePIV){
                    boolean ignoreU = false;
                    boolean ignoreV = false;
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.Auto) {
                        if (FastMath.abs(tileB.getDisplacementV()) < 3.0f) {
                            ignoreU = true;
                        }
                        if (FastMath.abs(tileB.getDisplacementU()) < 3.0f) {
                            ignoreV = true;
                        }
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.AutoSmall) {
                        if (FastMath.abs(tileB.getDisplacementV()) < 0.9f) {
                            ignoreU = true;
                        }
                        if (FastMath.abs(tileB.getDisplacementU()) < 0.9f) {
                            ignoreV = true;
                        }
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreV || 
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreV) {
                        v = tileB.getDisplacementU();    
                    }
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                        u = tileB.getDisplacementV();
                    }
                }
                
                final int pixelV = tileA.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1;
                final int pixelH = tileA.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1;
                final int idx = pixelV * filteredImgA.getWidth() + pixelH;
                usAndVs[0][idx] = u;
                usAndVs[1][idx] = v;
                if (avgOfFourPixels) {
                    usAndVs[0][idx+1] = u;
                    usAndVs[1][idx+1] = v;
                    //
                    final int idxB = (pixelV+1) * filteredImgA.getWidth() + pixelH;
                    usAndVs[0][idxB] = u;
                    usAndVs[1][idxB] = v;
                    //
                    usAndVs[0][idxB+1] = u;
                    usAndVs[1][idxB+1] = v;
                }
            }
            
            LucasKanadeOptions options = new LucasKanadeOptions();
            options.iterations = iterations;
            options.windowSize = windowSize;
            OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
            jobInput.imageA = filteredImgA;
            jobInput.imageB = filteredImgB;
            jobInput.halfPixelOffset = avgOfFourPixels ? false : true;
            jobInput.us = usAndVs[0];
            jobInput.vs = usAndVs[1];
            jobInput.options = options;

            dLKJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
            dLKJob.analyze();
            dLKJob.compute();
            OpticalFlowInterpolatorInput jobResult = dLKJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
            float[] resultUs = jobResult.us;
            float[] resultVs = jobResult.vs;

            for (final MaxCrossResult result : results) {
                final Tile tileA = result.getTileA();
                final Tile tileB = result.getTileB();
                final int pixelV = tileA.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1;
                final int pixelH = tileA.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1;
                final int idx = pixelV * filteredImgA.getWidth() + pixelH;
                
                float u = resultUs[idx];
                float v = resultVs[idx];
                if (avgOfFourPixels) {
                    u += resultUs[idx+1];
                    v += resultVs[idx+1];
                    //
                    final int idxB = (pixelV+1) * filteredImgA.getWidth() + pixelH;
                    u += resultUs[idxB];
                    v += resultVs[idxB];
                    //
                    u += resultUs[idxB+1];
                    v += resultVs[idxB+1];
                    
                    u /= 4.0f;
                    v /= 4.0f;
                }
                
                if (!tileB.isMaskedDisplacement()) {
                    result.setNthRelativeDisplacementFromVelocities(peakIdx, v - tileB.getDisplacementU(), u - tileB.getDisplacementV());
                } else {
                    result.setNthAbsoluteDisplacement(peakIdx, 0.0f, 0.0f);
                }
            }            
        }

        return results;
    }

    @Override
    public final void interpolate(final IterationStepTiles stepTilesA, final IterationStepTiles stepTilesB) {        
    	initdLKJobIfRequired();
    	//
    	BiCubicSplineInterpolatorWithBiLinearBackup interp = BiCubicSplineInterpolatorWithBiLinearBackup.createTileDisplacementInterpolator(stepTilesB);

        usAndVs = interp.interpolateDisplacements(filteredImgA.getHeight(), filteredImgA.getWidth(), denseVectors ? 0.0f : 0.5f, denseVectors ? 0.0f : 0.5f, usAndVs);

        if (ignorePIV) {
            if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.Auto) {
                final int height = filteredImgA.getHeight();
                final int width = filteredImgA.getWidth();
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
                final int height = filteredImgA.getHeight();
                final int width = filteredImgA.getWidth();
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
                input.options = new LucasKanadeOptions();
            }
        }

        final LucasKanadeOptions options = (LucasKanadeOptions)input.options;         
        options.iterations = iterations;
        options.windowSize = windowSize;
        input.imageA = filteredImgA;
        input.imageB = filteredImgB;
        //Swap U and V since PIV has swapped axis
        input.us = usAndVs[1];
        input.vs = usAndVs[0];
        input.halfPixelOffset = denseVectors && stepTilesB.getCurrentStep() == stepTilesB.getMaxAdaptiveSteps() - 1 ? false : true;
        
        dLKJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, input);
        
        dLKJob.analyze();
        dLKJob.compute();
        
        OpticalFlowInterpolatorInput opfResult = dLKJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
        
        for (int tileI = 0; tileI < stepTilesB.getNumberOfTilesInI(); tileI++) {
            for (int tileJ = 0; tileJ < stepTilesB.getNumberOfTilesInJ(); tileJ++) {
                Tile tile = stepTilesB.getTile(tileI, tileJ);
                int i = tile.getTopPixel() + stepTilesB.getTileHeight() / 2 - 1;
                int j = tile.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 1;                
                int idx = i * filteredImgA.getWidth() + j;
                if (!denseVectors || stepTilesB.getCurrentStep() != stepTilesB.getMaxAdaptiveSteps() - 1) {
                    float u = opfResult.us[idx];
                    float v = opfResult.vs[idx];
                    if (tile.isMaskedDisplacement()) {
                        u = 0.0f;
                        v = 0.0f;
                    }
                    tile.replaceDisplacement(v, u);
                } else {
                    int idx2 = (i + 1) * filteredImgA.getWidth() + j;
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
            stepTilesB.setUpdateDenseVectors(filteredImgA.getHeight(), filteredImgA.getWidth(), opfResult.vs, opfResult.us);
        }
    }

    @Override
    public void getVelocitiesMatrix(float centerLocI, float centerLocJ, float finalLocI, float finalLocJ, float[] us,
            float[] vs) {
        throw new NotImplementedException("dense Lucas-Kanade interpolator: Method intentionally not implemented. Should not be needed.");
    }
    
    @Override
    public void updateImageA(IImage img) {
        if (filteredImgA == null || img.getHeight() != filteredImgA.getHeight() || img.getWidth() != filteredImgA.getWidth()) {
            filteredImgA = ImageFloat.sizeFrom(img);
        }
        filteredImgA = filter.applyFilter(img, filteredImgA);
    }
    
    @Override
    public void updateImageB(IImage img) {
        if (filteredImgB == null || img.getHeight() != filteredImgB.getHeight() || img.getWidth() != filteredImgB.getWidth()) {
            filteredImgB = ImageFloat.sizeFrom(img);
        }
        filteredImgB = filter.applyFilter(img, filteredImgB);
    }

    @Override
    public void receiveImageA(IImage img) {
        updateImageA(img);
    }

    @Override
    public void receiveImageB(IImage img) {
        updateImageB(img);
    }
}
