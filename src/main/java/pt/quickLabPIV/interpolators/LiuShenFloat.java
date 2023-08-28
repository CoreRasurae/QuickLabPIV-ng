// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
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
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class LiuShenFloat implements IOpticalFlowInterpolator {
    private static Logger logger = LoggerFactory.getLogger(LiuShenFloat.class);
    private boolean absoluteMode;
    private boolean ignorePIV;
    private IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;
    private boolean denseVectors;
    
    private final float filterSigma;
    private final int filterWidthPx;
    private final float lambda;
    private final int iterations;
    private final IFilter filterLS;
    private final IFilter filterLK;
    private ILiuShenOpticalFlowHelper helper;
    private int vectorsWindowSizeI;
    private int vectorsWindowSizeJ;
    //
    private final float filterSigmaLK;
    private final int filterWidthPxLK;
    private final int iterationsLK;
    private final int windowSizeLK;
    
    private IImage imgA;
    private IImage filteredImgA;
    private IImage filteredImgLKA;
    private IImage imgB;
    private IImage filteredImgB;
    private IImage filteredImgLKB;
    private float IIx[];
    private float IIy[];
    private float II[];
    private float Ixt[];
    private float Iyt[];
    private float pixelMapA[];
    private float pixelMapB[];
    private float A[][];
    private float B[][];
    private float us[];
    private float vs[];
    private float usNew[];
    private float vsNew[];
    private float usAndVs[][];
    private OpticalFlowInterpolatorInput input;
    private DenseLucasKanadeAparapiJob dLKJob;
    
    public LiuShenFloat() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters pivParameters = singleton.getPIVParameters(); 
        Object configurationObject = pivParameters.getSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Liu-Shen: Couldn't retrieve Liu-Shen optical flow estimator/interpolator configuration");
        }
        
        LiuShenInterpolatorConfiguration configuration = (LiuShenInterpolatorConfiguration)configurationObject;
        ignorePIV = configuration.isIgnorePIVBaseDisplacements();
        ignorePIVMode = configuration.getIgnorePIVBaseDisplacementsMode();
        absoluteMode = ignorePIV ? true : false;
        
        filterSigma = configuration.getFilterSigmaLS();
        filterWidthPx = configuration.getFilterWidthPxLS();
        lambda = configuration.getMultiplierLagrangeLS();
        iterations = configuration.getNumberOfIterationsLS();
        vectorsWindowSizeI = vectorsWindowSizeJ = configuration.getVectorsWindowSizeLS();
        
        filterSigmaLK = configuration.getFilterSigmaLK();
        filterWidthPxLK = configuration.getFilterWidthPxLK();
        windowSizeLK = configuration.getWindowSizeLK();
        iterationsLK = configuration.getNumberOfIterationsLK();
        denseVectors = configuration.isDenseVectors();
        
        if (iterations <= 0) {
            throw new InterpolatorStateException("Liu-Shen: Invalid number of iterations specified");
        }
        
        if (vectorsWindowSizeI < 3 || vectorsWindowSizeI % 2 == 0) {
            throw new InterpolatorStateException("Liu-Shen: Invalid vector window side size");
        }
                
        filterLS = new GaussianFilter2D(filterSigma, filterWidthPx);
        filterLK = new GaussianFilter2D(filterSigmaLK, filterWidthPxLK);

        IIx = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        IIy = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        II = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        Ixt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        Iyt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        pixelMapA = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
        pixelMapB = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
        A = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
        B = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
        us = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        vs = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        usNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        vsNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
    }

    private final float getNearestPixel(IImage img, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= img.getHeight()) {
            i = img.getHeight() - 1;
        }
        
        if (j < 0) {
            j = 0;
        }
        if (j >= img.getWidth()) {
            j = img.getWidth() - 1;
        }
        
        return img.readPixel(i, j);
    }
    
    private final float getNearestPixelWithWarp(IImage img, float locI, float locJ) {
        int i = (int) locI;
        int j = (int) locJ;
        
        float deltaI = locI - i;
        float deltaJ = locJ - j;
        
        if (deltaI < 0) {
            i--;
            deltaI += 1.0f;
        }
        
        if (deltaJ < 0) {
            j--;
            deltaJ += 1.0f;
        }
        
        float value = (1.0f - deltaI) * ((1.0f - deltaJ) * getNearestPixel(img, i,j) + deltaJ * getNearestPixel(img, i,j+1)) + 
                              deltaI  * ((1.0f - deltaJ) * getNearestPixel(img, i+1,j) + deltaJ * getNearestPixel(img, i+1,j+1));
        
        return value;
    }
    
    private final void computeFixedImageDerivatives(final float locI, final float locJ, final int startI, final int endI, final int startJ, final int endJ, final int dx) {
        for (int i = startI - 1; i <= endI + 1; i++) {
            for (int j = startJ - 1; j <= endJ + 1; j++) {
                int index = (i - startI + 1) * (vectorsWindowSizeJ + 2) + j - startJ + 1;
                
                float targetLocI = locI + i*dx;
                float targetLocJ = locJ + j*dx;
                if (dx > 1) {
                    if (targetLocI < 0) {
                        targetLocI += dx;
                    }
                    if (targetLocJ < 0) {
                        targetLocJ += dx;
                    }
                    if (targetLocI >= filteredImgA.getHeight()) {
                        targetLocI -= dx;
                    }
                    if (targetLocJ >= filteredImgA.getWidth()) {
                        targetLocJ -= dx;
                    }
                }
                pixelMapA[index] = getNearestPixelWithWarp(filteredImgA, targetLocI, targetLocJ);
                pixelMapB[index] = getNearestPixelWithWarp(filteredImgB, targetLocI, targetLocJ);
            }
        }
                
        for (int i = startI; i <= endI; i++) {
            for (int j = startJ; j <= endJ; j++) {
                final int idx = (i - startI) * vectorsWindowSizeJ + j - startJ;
                final int centerIndex = (i - startI + 1) * (vectorsWindowSizeJ + 2) + j - startJ + 1;
                final int leftIndex = centerIndex - 1;
                final int rightIndex = centerIndex + 1;
                final int topIndex = (i - 1 - startI + 1) * (vectorsWindowSizeJ + 2) + j - startJ + 1;
                final int bottomIndex = (i + 1 - startI + 1) * (vectorsWindowSizeJ + 2) + j - startJ + 1;
                final int topLeftIndex = topIndex - 1;
                final int topRightIndex = topIndex + 1;
                final int bottomLeftIndex = bottomIndex - 1;
                final int bottomRightIndex = bottomIndex + 1;
                
                float centerPixelValueA = pixelMapA[centerIndex];
                                
                IIx[idx] = centerPixelValueA * (pixelMapA[topIndex] - pixelMapA[bottomIndex]) / (2.0f * dx);
                IIy[idx] = centerPixelValueA * (pixelMapA[leftIndex] - pixelMapA[rightIndex]) / (2.0f * dx);
                II[idx]  = centerPixelValueA * centerPixelValueA;
                Ixt[idx] = centerPixelValueA * ((pixelMapB[topIndex] - pixelMapA[topIndex]) - (pixelMapB[bottomIndex] - pixelMapA[bottomIndex])) / (2.0f * dx);
                Iyt[idx] = centerPixelValueA * ((pixelMapB[leftIndex] - pixelMapA[leftIndex]) - (pixelMapB[rightIndex]) - pixelMapA[rightIndex]) / (2.0f * dx);
                
                //Generate inverted matrix B
                A[idx][0] = centerPixelValueA * (pixelMapA[bottomIndex] + pixelMapA[topIndex] - 2.0f*centerPixelValueA)/(dx*dx) - 2.0f * centerPixelValueA / (dx*dx) - lambda * 8.0f / (dx*dx);
                A[idx][2] = centerPixelValueA * (pixelMapA[rightIndex] + pixelMapA[leftIndex] - 2.0f*centerPixelValueA)/(dx*dx) - 2.0f * centerPixelValueA / (dx*dx) - lambda * 8.0f / (dx*dx); 
                A[idx][1] = centerPixelValueA * (pixelMapA[topLeftIndex] - pixelMapA[topRightIndex] - pixelMapA[bottomLeftIndex] + pixelMapA[bottomRightIndex]) / (4.0f*dx*dx);
                
                float detA = A[idx][0] * A[idx][2] - A[idx][1] * A[idx][1];
                
                B[idx][0] =   A[idx][2] / detA;
                B[idx][1] = - A[idx][1] / detA;
                B[idx][2] =   A[idx][0] / detA;
            }
        }
    }
    
    @FunctionalInterface
    private interface GVec {
        float get(int i, int j);
    }
    
    private final float getValueComplete(float[] arr, int i, int j, final int startI, final int endI, final int startJ, final int endJ) {
        if (i < startI) {
            i = startI;
        }
        if (i > endI) {
            i = endI;
        }
        if (j < startJ) {
            j = startJ;
        }
        if (j > endJ) {
            j = endJ;
        }
        
        return arr[(i - startI) * vectorsWindowSizeJ + j - startJ];
    }
    
    @Override
    public List<MaxCrossResult> interpolate(final List<MaxCrossResult> results) {
        if (helper == null) {
            helper = new LucasKanadeFloat(vectorsWindowSizeI, filterSigmaLK, filterWidthPxLK, 
                    windowSizeLK, iterationsLK, false);
            helper.receiveImageA(imgA);
            helper.receiveImageB(imgB);
        }
        for (MaxCrossResult result : results) {
            final int dx = 1;
            final int startI = -vectorsWindowSizeI/2;
            final int endI = vectorsWindowSizeI/2;
            final int startJ = -vectorsWindowSizeJ/2;
            final int endJ = vectorsWindowSizeJ/2;
            final Tile tileA = result.getTileA();
            final int topA = tileA.getTopPixel();
            final int leftA = tileA.getLeftPixel();
            final int centerIndex = (vectorsWindowSizeI / 2) * vectorsWindowSizeJ + vectorsWindowSizeJ / 2;
            
            final Tile tileB = result.getTileB();
            final IterationStepTiles stepTilesB = tileB.getParentIterationStepTiles();
            
            //Location of the position of the center of the current IA
            final float initialPosI = topA + stepTilesB.getTileHeight() / 2 - 0.5f;
            final float initialPosJ = leftA + stepTilesB.getTileWidth() / 2 - 0.5f;
                
            for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
                float startLocI = result.getDisplacementFromNthPeakU(peakIndex) + tileB.getDisplacedTileCenterV();
                float startLocJ = result.getDisplacementFromNthPeakV(peakIndex) + tileB.getDisplacedTileCenterH();
                
                if (tileB.isMaskedDisplacement()) {
                    result.setNthAbsoluteDisplacement(peakIndex, 0.0f, 0.0f);
                    continue;
                }

                if (ignorePIV) {
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
                        startLocI = tileB.getDisplacedTileCenterV();
                    }
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                        startLocJ = tileB.getDisplacedTileCenterH();
                    }
                }

                float usLocal[] = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
                float vsLocal[] = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
                
                helper.getVelocitiesMatrix(initialPosI, initialPosJ, startLocI, startLocJ, usLocal, vsLocal);
                
                for (int idx = 0; idx < usLocal.length; idx++) {
                    us[idx] = usLocal[idx];
                    vs[idx] = vsLocal[idx];
                }
                
                computeFixedImageDerivatives(initialPosI, initialPosJ, startI, endI, startJ, endJ, dx);
        
                refineVectors(startI, endI, startJ, endJ, dx);

                if (absoluteMode) {
                    result.setNthAbsoluteDisplacement(peakIndex, (float)vs[centerIndex], (float)us[centerIndex]);
                } else {
                    result.setNthRelativeDisplacementFromVelocities(peakIndex, vs[centerIndex] - tileB.getDisplacementU(), us[centerIndex] - tileB.getDisplacementV());
                }
            }
        }
        
        return results;
    }

    private void refineVectors(final int startI, final int endI, final int startJ, final int endJ, final int dx) {
        final GVec gus = (i, j) -> getValueComplete(us, i, j, startI, endI, startJ, endJ);
        final GVec gvs = (i, j) -> getValueComplete(vs, i, j, startI, endI, startJ, endJ);
        
        float totalError = Float.MAX_VALUE;
        float tolerance = 1e-8f;
        for (int iter = 0; iter < iterations; iter++) {
            if (totalError <= tolerance) {
                break;
            }

            totalError = 0;
            
            for (int i = startI; i <= endI; i++) {
                for (int j = startJ; j <= endJ; j++) {
                    int idx = (i - startI) * vectorsWindowSizeJ + j - startJ;
                    
                    float bu = 2.0f * IIx[idx] * (gus.get(i-1, j  ) - gus.get(i+1, j  )) / (2.0f * dx) + 
                                      IIx[idx] * (gvs.get(i  , j-1) - gvs.get(i  , j+1)) / (2.0f * dx) +
                                      IIy[idx] * (gvs.get(i-1, j  ) - gvs.get(i+1, j  )) / (2.0f * dx) + 
                                       II[idx] * (gus.get(i-1, j  ) + gus.get(i+1, j  )) / (dx * dx) +
                                       II[idx] * (gvs.get(i-1, j-1) - gvs.get(i-1, j+1) - gvs.get(i+1, j-1) + gvs.get(i+1, j+1)) / (4.0f * dx * dx)+
                                        lambda * (gus.get(i-1, j-1) + gus.get(i-1, j  ) + gus.get(i-1, j+1) +
                                                  gus.get(i  , j-1) + gus.get(i  , j+1) +
                                                  gus.get(i+1, j-1) + gus.get(i+1, j  ) + gus.get(i+1, j+1)) / (dx * dx)+ Ixt[idx];
                    
                    float bv = 2.0f * IIy[idx] * (gvs.get(i  , j-1) - gvs.get(i  , j+1)) / (2.0f * dx) +
                                      IIy[idx] * (gus.get(i-1, j  ) - gus.get(i+1, j  )) / (2.0f * dx) +
                                      IIx[idx] * (gus.get(i  , j-1) - gus.get(i  , j+1)) / (2.0f * dx) +                                       
                                       II[idx] * (gvs.get(i  , j-1) + gvs.get(i  , j+1)) / (dx * dx) +
                                       II[idx] * (gus.get(i-1, j-1) - gus.get(i-1, j+1) - gus.get(i+1, j-1) + gus.get(i+1, j+1)) / (4.0f * dx * dx) +
                                        lambda * (gvs.get(i-1, j-1) + gvs.get(i-1, j  ) + gvs.get(i-1, j+1) +
                                                  gvs.get(i  , j-1) + gvs.get(i  , j+1) +
                                                  gvs.get(i+1, j-1) + gvs.get(i+1, j  ) + gvs.get(i+1, j+1)) / (dx * dx) + Iyt[idx];
                            
                    float unew = (float)-(B[idx][0]*bu + B[idx][1]*bv);
                    float vnew = (float)-(B[idx][1]*bu + B[idx][2]*bv);
                    
                    totalError += FastMath.sqrt((unew - gus.get(i, j))*(unew - gus.get(i, j)) + (vnew - gvs.get(i, j))*(vnew - gvs.get(i, j)));

                    usNew[idx] = unew;
                    vsNew[idx] = vnew;
                }                    
            }
            
            totalError /= (float)us.length;
            
            for (int idx = 0; idx < vectorsWindowSizeI*vectorsWindowSizeJ; idx++) {
                us[idx] = usNew[idx];
                vs[idx] = vsNew[idx];
            }
        }
    }

    public void interpolateFromFullImageWithAparapi(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
        if (vectorsWindowSizeI != filteredImgA.getHeight() || vectorsWindowSizeJ != filteredImgA.getWidth()) {
            vectorsWindowSizeI = filteredImgA.getHeight();
            vectorsWindowSizeJ = filteredImgA.getWidth();
            IIx = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            IIy = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            II = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            Ixt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            Iyt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            pixelMapA = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
            pixelMapB = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
            A = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
            B = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
            us = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            vs = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            usNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            vsNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            usAndVs = new float[2][vectorsWindowSizeI * vectorsWindowSizeJ];
            //Swapped on purpose to match PIV vector orientation, which is swapped from OpF orientation
            usAndVs[0] = vs;
            usAndVs[1] = us;
        }
       
        if (dLKJob == null) {
            ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
            dLKJob = new DenseLucasKanadeAparapiJob(gpuDevice);
        }
        
        if (filteredImgLKA == null) {
            filteredImgLKA = ImageFloat.sizeFrom(imgA);
            filteredImgLKA = filterLK.applyFilter(imgA, filteredImgLKA);               
        }

        if (filteredImgLKB == null) {
            filteredImgLKB = ImageFloat.sizeFrom(imgB);
            filteredImgLKB = filterLK.applyFilter(imgB, filteredImgLKB);               
        }

        
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
                //We cannot disregard previous window displacements contribution, we can only ignore the current cross-correlation results
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
        options.iterations = iterationsLK;
        options.windowSize = windowSizeLK;
        input.imageA = filteredImgLKA;
        input.imageB = filteredImgLKB;
        //Swap U and V since PIV has swapped axis
        input.us = us;
        input.vs = vs;
        input.halfPixelOffset = denseVectors ? false : true;
        
        dLKJob.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, input);
        
        dLKJob.analyze();
        dLKJob.compute();
        
        OpticalFlowInterpolatorInput opfResult = dLKJob.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);
                
        final int dx = 1;
        final int startI = 0;
        final int endI = vectorsWindowSizeI-1;
        final int startJ = 0;
        final int endJ = vectorsWindowSizeJ-1;
        float startLocI = denseVectors && stepTilesB.getCurrentStep() == stepTilesB.getMaxAdaptiveSteps() - 1 ? 0.0f : 0.5f;
        float startLocJ = denseVectors && stepTilesB.getCurrentStep() == stepTilesB.getMaxAdaptiveSteps() - 1 ? 0.0f : 0.5f;
        computeFixedImageDerivatives(startLocI, startLocJ, startI, endI, startJ, endJ, dx);        
        refineVectors(startI, endI, startJ, endJ, dx);

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
            stepTilesB.setUpdateDenseVectors(filteredImgA.getHeight(), filteredImgA.getWidth(), vs, us);
        }
    }
    
    @Override
    public void interpolate(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
        if (helper == null) {
            helper = new LucasKanadeFloat(vectorsWindowSizeI, filterSigmaLK, filterWidthPxLK, 
                    windowSizeLK, iterationsLK, false);
            helper.receiveImageA(imgA);
            helper.receiveImageB(imgB);
        }
        
        final int dx = 1;
        final int startI = -vectorsWindowSizeI/2;
        final int endI = vectorsWindowSizeI/2;
        final int startJ = -vectorsWindowSizeJ/2;
        final int endJ = vectorsWindowSizeJ/2;
        final int centerIndex = (vectorsWindowSizeI / 2) * vectorsWindowSizeJ + vectorsWindowSizeJ / 2;

        float usLocal[] = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
        float vsLocal[] = new float[vectorsWindowSizeI * vectorsWindowSizeJ];

        for (int i = 0; i < stepTilesB.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesB.getNumberOfTilesInJ(); j++) {
                final Tile tileB = stepTilesB.getTile(i, j);

                //Location of the position of the center of the current IA
                final float initialPosI = tileB.getTopPixel() + stepTilesB.getTileHeight() / 2 - 0.5f;
                final float initialPosJ = tileB.getLeftPixel() + stepTilesB.getTileWidth() / 2 - 0.5f;
                    
                float startLocI = tileB.getDisplacedTileCenterV();
                float startLocJ = tileB.getDisplacedTileCenterH();
                if (ignorePIV) {
                    boolean ignoreV = false;
                    boolean ignoreU = false;
                    
                    float v = FastMath.abs(startLocI - initialPosI);
                    float u = FastMath.abs(startLocJ - initialPosJ);
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.Auto) {
                        if (v < 3.0f) {
                            ignoreV = true;
                        }
                        if (u < 3.0f) {
                            ignoreU = true;
                        }
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.AutoSmall) {
                        if (v < 0.9f) {
                            ignoreV = true;
                        }
                        if (u < 0.9f) {
                            ignoreU = true;
                        }                        
                    }
                    
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreV || 
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreV) {
                        //We cannot disregard previous window displacements contribution, we can only ignore the current cross-correlation results
                        startLocI = initialPosI;
                    }
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                        startLocJ = initialPosJ;
                    }
                }
                                
                helper.getVelocitiesMatrix(initialPosI, initialPosJ, startLocI, startLocJ, usLocal, vsLocal);
                
                for (int idx = 0; idx < usLocal.length; idx++) {
                    us[idx] = usLocal[idx];
                    vs[idx] = vsLocal[idx];
                }
                
                computeFixedImageDerivatives(initialPosI, initialPosJ, startI, endI, startJ, endJ, dx);
        
                refineVectors(startI, endI, startJ, endJ, dx);

                if (tileB.isMaskedDisplacement()) {
                    tileB.replaceDisplacement(0.0f, 0.0f);
                } else {
                    tileB.replaceDisplacement(vs[centerIndex], us[centerIndex]);
                }
            }
        }
    }

    public void computeFromVelocities(float[] _us, float[] _vs) {
        if (vectorsWindowSizeI != filteredImgA.getHeight() || vectorsWindowSizeJ != filteredImgA.getWidth()) {
            vectorsWindowSizeI = filteredImgA.getHeight();
            vectorsWindowSizeJ = filteredImgA.getWidth();
            IIx = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            IIy = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            II = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            Ixt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            Iyt = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            pixelMapA = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
            pixelMapB = new float[(vectorsWindowSizeI+2) * (vectorsWindowSizeJ+2)];
            A = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
            B = new float[vectorsWindowSizeI * vectorsWindowSizeJ][3];
            us = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            vs = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            usNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            vsNew = new float[vectorsWindowSizeI * vectorsWindowSizeJ];
            usAndVs = new float[2][vectorsWindowSizeI * vectorsWindowSizeJ];
            //Swapped on purpose to match PIV vector orientation, which is swapped from OpF orientation
            usAndVs[0] = vs;
            usAndVs[1] = us;
        }
        
        final int dx = 1;
        final int startI = 0;
        final int endI = filteredImgA.getHeight()-1;
        final int startJ = 0;
        final int endJ = filteredImgA.getWidth()-1;
        float initialPosI = denseVectors ? 0.0f : 0.5f;
        float initialPosJ = denseVectors ? 0.0f : 0.5f;

        for (int idx = 0; idx < _us.length; idx++) {
            us[idx] = _us[idx];
            vs[idx] = _vs[idx];
        }
                
        computeFixedImageDerivatives(initialPosI, initialPosJ, startI, endI, startJ, endJ, dx);
        
        refineVectors(startI, endI, startJ, endJ, dx);

        for (int idx = 0; idx < _us.length; idx++) {
            _us[idx] = us[idx];
            _vs[idx] = vs[idx];
        }
    }
    
    @Override
    public void updateImageA(IImage img) {
        if (filteredImgA == null || img.getHeight() != filteredImgA.getHeight() || img.getWidth() != filteredImgA.getWidth()) {
            filteredImgA = ImageFloat.sizeFrom(img);
        }
        imgA = img;
        filteredImgA = filterLS.applyFilter(img, filteredImgA);
        filteredImgA = filteredImgA.normalize((ImageFloat)filteredImgA);
        
        if (helper != null) {
            helper.receiveImageA(img);
        }

        if (dLKJob != null) {
            if (filteredImgLKA == null || img.getHeight() != filteredImgLKA.getHeight() || img.getWidth() != filteredImgLKA.getWidth()) {
                filteredImgLKA = ImageFloat.sizeFrom(img);
            }
            filteredImgLKA = filterLK.applyFilter(img, filteredImgLKA);
        }
    }
    
    @Override
    public void updateImageB(IImage img) {
        if (filteredImgB == null || img.getHeight() != filteredImgB.getHeight() || img.getWidth() != filteredImgB.getWidth()) {
            filteredImgB = ImageFloat.sizeFrom(img);
        }
        imgB = img;
        filteredImgB = filterLS.applyFilter(img, filteredImgB);
        filteredImgB = filteredImgB.normalize((ImageFloat)filteredImgB);
        
        if (helper != null) {
            helper.receiveImageB(img);
        }
        
        if (dLKJob != null) {
            if (filteredImgLKB == null || img.getHeight() != filteredImgLKB.getHeight() || img.getWidth() != filteredImgLKB.getWidth()) {
                filteredImgLKB = ImageFloat.sizeFrom(img);
            }
            filteredImgLKB = filterLK.applyFilter(img, filteredImgLKB);
        }
    }
    
    public float[] getIIx() {
        return Arrays.copyOf(IIx, IIx.length);
    }

    public float[] getIIy() {
        return Arrays.copyOf(IIy, IIy.length);
    }

    public float[] getII() {
        return Arrays.copyOf(II, II.length);
    }
    
    public float[] getIxt() {
        return Arrays.copyOf(Ixt, Ixt.length);
    }

    public float[] getIyt() {
        return Arrays.copyOf(Iyt, Iyt.length);
    }
    
    public float[][] getBs() {
        float[][] bs = new float[B.length][3]; 
        for (int i  = 0; i < B.length; i++) {
            for (int j = 0; j < 3; j++) {
                bs[i][j] = B[i][j];
            }
        }
        
        return bs;
    }

    public float[] getUsNew() {
        return Arrays.copyOf(usNew, usNew.length);
    }

    public float[] getVsNew() {
        return Arrays.copyOf(vsNew, vsNew.length);
    }
}
