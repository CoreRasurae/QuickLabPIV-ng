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
import pt.quickLabPIV.iareas.InterpolateException;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.maximum.MaxCrossResult;

/**
 * This class implements sparse Lucas-Kanade optical flow for a single pixel of a set of 4 neighboring pixels.
 * It can deal with "single conceptual pixels" located at the center of 4 neighboring pixels.
 * NOTE: This class can be easily be converted for a double precision implementation by using find and replace from
 * float to double.
 * NOTE2: A double precision implementation was tested, but the gains were so small that aren't justifiable when
 * compared to the computational cost of the employing double precision.
 * @author lpnm
 *
 */
public final class LucasKanadeFloat implements IOpticalFlowInterpolator, ILiuShenOpticalFlowHelper {
    private static Logger logger = LoggerFactory.getLogger(LucasKanadeFloat.class);
    
    private final boolean ignorePIV;
    private final IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;
    
    private final boolean absoluteDisplacementMode;
    private final boolean avgOfFourPixels;
    private final int iterations;
    private final int windowSize;
    private final float filterSigma;
    private final int filterWidthPx;
    private final IFilter filter;
    
    private IImage filteredImgA;
    private IImage filteredImgB;

    final int marginLeft;
    final int marginRight;
    final int margins;
    final float imgPatchA[];
    final float dI[];
    final float dJ[];
    final float A[][];
    final float detA[];
    final boolean status[];
    final float positionsU[];
    final float positionsV[];
    final float b[][];
    ILucasKanadeListener listener = null;

    interface ILucasKanadeListener {
        public void computedDerivativesPreInversion(float imgPatchA[], float dI[], float dJ[], float A[][]);
        public void computedPostInversion(float detA[], float A[][]);
        public void readingImageBPixel(float value, float dT, float b0Inc, float b1Inc, int i, int j, float locI, float locJ, int patchIndex);
        public void computedBs(float b0, float b1, float incU, float incV, int iter, int patchIndex);
    }
    
    void registerListener(ILucasKanadeListener _listener) {
        listener = _listener;
    }
    
    public LucasKanadeFloat() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters pivParameters = singleton.getPIVParameters(); 
        Object configurationObject = pivParameters.getSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER);
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
        
        //This is currently mostly indifferent, it seems. However it maybe useful to test the impact on precision.
        //if (pivParameters.getWarpingMode() == WarpingModeFactoryEnum.NoImageWarping) {
        //    absoluteDisplacementMode = false;
        //} else {
        //    absoluteDisplacementMode = true;
        //}        
        absoluteDisplacementMode = ignorePIV ? true : false;
        
        filter = new GaussianFilter2D(filterSigma, filterWidthPx);
    
        marginLeft = 0;
        int lrmargin = 0;
        if (avgOfFourPixels) {
           lrmargin = 1;
        }
        marginRight = lrmargin;
        margins = lrmargin;
        imgPatchA = new float[(windowSize + margins) * (windowSize + margins)];
        dI = new float[(windowSize + margins) * (windowSize + margins)];
        dJ = new float[(windowSize + margins) * (windowSize + margins)];
        A = new float[(margins + 1) * (margins + 1)][3];
        detA = new float[(margins + 1) * (margins + 1)];
        status = new boolean[(margins + 1) * (margins + 1)];
        positionsU = new float[(margins + 1) * (margins + 1)];
        positionsV = new float[(margins + 1) * (margins + 1)];
        b = new float[(margins + 1) * (margins + 1)][2];
    }
    
    public LucasKanadeFloat(int _vectorsSideSize, float _filterSigma, int _filterWidthPx, int _windowSize, int iters, boolean _ignorePIV) {
        ignorePIV = _ignorePIV;
        ignorePIVMode = IgnorePIVBaseDisplacementsModeEnum.AutoSmall;
        absoluteDisplacementMode = false;
        iterations = iters;
        windowSize = _windowSize;
        filterSigma = _filterSigma;
        filterWidthPx = _filterWidthPx;

        filter = new GaussianFilter2D(filterSigma, filterWidthPx);
        avgOfFourPixels = false;
        
        marginLeft = _vectorsSideSize/2;
        marginRight = _vectorsSideSize/2;
        margins = marginLeft + marginRight;
        imgPatchA = new float[(windowSize + margins) * (windowSize + margins)];
        dI = new float[(windowSize + margins) * (windowSize + margins)];
        dJ = new float[(windowSize + margins) * (windowSize + margins)];
        A = new float[(margins + 1) * (margins + 1)][3];
        detA = new float[(margins + 1) * (margins + 1)];
        status = new boolean[(margins + 1) * (margins + 1)];
        positionsU = new float[(margins + 1) * (margins + 1)];
        positionsV = new float[(margins + 1) * (margins + 1)];
        b = new float[(margins + 1) * (margins + 1)][2];
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
        
        float value = (1.0f - deltaI) * ((1.0f - deltaJ) * getNearestPixel(img, i  ,j) + deltaJ * getNearestPixel(img, i  ,j+1)) + 
                              deltaI  * ((1.0f - deltaJ) * getNearestPixel(img, i+1,j) + deltaJ * getNearestPixel(img, i+1,j+1));
        
        return value;
    }

    private final float[] getImagePatch(final float[] imgPatch, final IImage img, final float locI, final float locJ, final int patchIndex) {
        if (patchIndex == -1) {
            Arrays.fill(imgPatch, 0.0f);
            if (dI != null) {
                Arrays.fill(dI, 0.0f);
            }
            if (dJ != null) {
                Arrays.fill(dJ, 0.0f);
            }
            if (A != null) {
                for (int i = 0; i < A.length; i++) {
                    Arrays.fill(A[i], 0.0f);
                }
            }
        } else {
            Arrays.fill(b[patchIndex], 0.0f);
        }
        
        float deltaI = locI - (int)locI;
        float deltaJ = locJ - (int)locJ;
        
        int marginRLocal = 0;
        int marginLLocal = 0;
        if (patchIndex == -1) {
            marginRLocal = marginRight;
            marginLLocal = marginLeft;
        }
        
        for (int i = -windowSize/2 - marginLLocal; i <= windowSize/2 + marginRLocal; i++) {
            for (int j = -windowSize/2 - marginLLocal; j <= windowSize/2 + marginRLocal; j++) {
                //Only employed when patchIndex is -1
                int index = (i + windowSize/2 + marginLLocal) * (windowSize + margins) + (j + windowSize/2 + marginLLocal);
                
                float pixelValue;
                float dIPx = 0.0f;
                float dJPx = 0.0f;
                
                if (deltaI == 0.0 && deltaJ == 0.0) {
                   pixelValue = getNearestPixel(img, (int)locI + i, (int)locJ + j);
                   if (patchIndex == -1) {
                       dIPx =
                          3.0f*(getNearestPixel(img, (int)locI + i-1, (int)locJ + j-1) + getNearestPixel(img, (int)locI + i-1, (int)locJ + j+1) - 
                               getNearestPixel(img, (int)locI + i+1, (int)locJ + j-1) -  getNearestPixel(img, (int)locI + i+1, (int)locJ + j+1)) +
                          10.0f*(getNearestPixel(img, (int)locI + i-1, (int)locJ +j) - getNearestPixel(img, (int)locI + i+1, (int)locJ + j));
                       dIPx *= 1.0f/32.0f;
                       dJPx = 
                          3.0f*(getNearestPixel(img, (int)locI + i-1, (int)locJ + j-1) + getNearestPixel(img, (int)locI + i+1, (int)locJ + j-1) - 
                               getNearestPixel(img, (int)locI + i-1, (int)locJ + j+1) -  getNearestPixel(img, (int)locI + i+1, (int)locJ + j+1)) +
                          10.0f*(getNearestPixel(img, (int)locI + i, (int)locJ + j-1) - getNearestPixel(img, (int)locI + i, (int)locJ + j+1));
                       dJPx *= 1.0f/32.0f;
                              
                       dI[index] = dIPx;
                       dJ[index] = dJPx;
                   }
                } else {
                   //TODO Improve later with a 2D convolution kernel (same warp for all pixels)
                   pixelValue = getNearestPixelWithWarp(img, locI + i, locJ + j);
    
                   if (patchIndex == -1) {
                       dIPx =
                          3.0f*(getNearestPixelWithWarp(img, locI + i-1, locJ + j-1) + getNearestPixelWithWarp(img, locI + i-1, locJ + j+1) - 
                               getNearestPixelWithWarp(img, locI + i+1, locJ + j-1) - getNearestPixelWithWarp(img, locI + i+1, locJ + j+1)) +
                          10.0f*(getNearestPixelWithWarp(img, locI + i-1, locJ + j) - getNearestPixelWithWarp(img, locI + i+1, locJ + j));
                       dIPx *= 1.0f/32.0f;
                       dJPx = 
                          3.0f*(getNearestPixelWithWarp(img, locI + i-1, locJ + j-1) + getNearestPixelWithWarp(img, locI + i+1, locJ + j-1) - 
                               getNearestPixelWithWarp(img, locI + i-1, locJ + j+1) -  getNearestPixelWithWarp(img, locI + i+1, locJ + j+1)) +
                          10.0f*(getNearestPixelWithWarp(img, locI + i, locJ + j-1) - getNearestPixelWithWarp(img, locI + i, locJ + j+1));
                       dJPx *= 1.0f/32.0f;
                       
                       dI[index] = dIPx;
                       dJ[index] = dJPx;
                   }
                }
                
                if (imgPatch != null) {                    
                    imgPatch[index] = pixelValue;
                }
                
                if (patchIndex == -1) {
                    if (margins == 0) {
                        A[0][0] += dJPx * dJPx;
                        A[0][1] += dJPx * dIPx;
                        A[0][2] += dIPx * dIPx;
                    } else {
                        for (int subIdxI = -marginLeft; subIdxI <= marginRight; subIdxI++) {
                            for (int subIdxJ = -marginLeft; subIdxJ <= marginRight; subIdxJ++) {
                                int subIndex = (subIdxI + marginLeft) * (margins + 1) + subIdxJ + marginLeft;
                                if (i >= -windowSize/2 + subIdxI && i <= windowSize/2 + subIdxI &&
                                    j >= -windowSize/2 + subIdxJ && j <= windowSize/2 + subIdxJ) {
                                    A[subIndex][0] += dJPx * dJPx;
                                    A[subIndex][1] += dJPx * dIPx;
                                    A[subIndex][2] += dIPx * dIPx;                                    
                                }
                            }
                        }
                    }
                }
                
                if (patchIndex >= 0) {
                    int offsetI = patchIndex / (margins + 1);
                    int offsetJ = patchIndex % (margins + 1);
                    int newIndex = (i + offsetI + windowSize/2) * (windowSize + margins) + (j + windowSize/2 + offsetJ);
                                        
                    dIPx = dI[newIndex];
                    dJPx = dJ[newIndex];
                    float dT = pixelValue - imgPatchA[newIndex];
                    
                    if (listener != null) {
                        listener.readingImageBPixel(pixelValue, dT, dT*dJPx, dT*dIPx, i + windowSize/2, j + windowSize/2, locI, locJ, patchIndex);
                    }
                    
                    if (margins == 0) {
                        b[0][0] += dT * dJPx;
                        b[0][1] += dT * dIPx;
                    } else {
                        if (i <= windowSize/2 && j <= windowSize/2) {
                            //Top-Left pixel
                            b[patchIndex][0] += dT * dJPx;
                            b[patchIndex][1] += dT * dIPx;
                        } 
                    }
                }
            }
        }

        return imgPatch;        
    }
    
    @Override
    public void getVelocitiesMatrix(float centerLocI, float centerLocJ, float finalLocI, float finalLocJ, float us[], float vs[]) {
        try {
            computeDerivativesAndMatA(centerLocI, centerLocJ);
        } catch (InterpolateException e) {
            Arrays.fill(vs, finalLocI - centerLocI);
            Arrays.fill(us, finalLocJ - centerLocJ);
            return;
        }
        
                
        if (finalLocI < 0 || finalLocI >= filteredImgA.getHeight() + windowSize / 2) {
            Arrays.fill(vs, finalLocI - centerLocI);
            Arrays.fill(us, finalLocJ - centerLocJ);
            return;
        }
        
        if (finalLocJ < 0 || finalLocJ >= filteredImgB.getWidth() + windowSize / 2) {
            Arrays.fill(vs, finalLocI - centerLocI);
            Arrays.fill(us, finalLocJ - centerLocJ);
            return;
        }
        
        computeOpticalFlow(finalLocI, finalLocJ);

        for (int offsetI = -marginLeft; offsetI <= marginRight; offsetI++) {
            for (int offsetJ = -marginLeft; offsetJ <= marginRight; offsetJ++) {
                final float startLocI = centerLocI + offsetI;
                final float startLocJ = centerLocJ + offsetJ;
                                
                final int idx = (offsetI + marginLeft) * (margins + 1) + offsetJ + marginLeft;
                
                us[idx] = positionsU[idx] - startLocJ;
                vs[idx] = positionsV[idx] - startLocI;
            }
        }
    }
    
    @Override
    public void interpolate(final IterationStepTiles stepTilesA, final IterationStepTiles stepTilesB) {
        for (int i = 0; i < stepTilesA.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTilesA.getNumberOfTilesInJ(); j++) {
                interpolate(stepTilesA.getTile(i, j), stepTilesB.getTile(i, j));
            }
        }
    }
    
    private void interpolate(final Tile tileA, final Tile tileB) {
        int topA = tileA.getTopPixel();
        int leftA = tileA.getLeftPixel();
        IterationStepTiles stepTilesB = tileB.getParentIterationStepTiles();

        float initialPosI = topA + stepTilesB.getTileHeight() / 2;
        float initialPosJ = leftA + stepTilesB.getTileWidth() / 2;
        if (avgOfFourPixels) {
            initialPosI -= 1.0;
            initialPosJ -= 1.0;
        } else {
            initialPosI -= 0.5;
            initialPosJ -= 0.5;
        }

        computeDerivativesAndMatA(initialPosI, initialPosJ);

        float positionV = initialPosI + tileB.getDisplacementU();
        float positionU = initialPosJ + tileB.getDisplacementV();
        if (ignorePIV) {
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
                //We cannot disregard previous window displacements contribution, we can only ignore the current cross-correlation results
                positionV = initialPosI;
            }
            if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                positionU = initialPosJ;
            }
        }
        
        if (tileB.isMaskedDisplacement()) {
            positionV = initialPosI;
            positionU = initialPosJ;
        }

        if (positionV < 0 || positionV >= filteredImgA.getHeight() + windowSize / 2) {
            logger.warn("Tile I:{},J:{} - Cannot apply Lucas-Kanade optical flow because window is completely oustide of the image (Vertical)", 
                    tileA.getTileIndexI(), tileA.getTileIndexJ());
            return;
        }
        
        if (positionU < 0 || positionU >= filteredImgA.getWidth() + windowSize / 2) {
            logger.warn("Tile I:{},J:{} - Peak index: {} - Cannot apply Lucas-Kanade optical flow because window is completely oustide of the image (Horizontal)", 
                    tileA.getTileIndexI(), tileA.getTileIndexJ());
            return;
        }

        computeOpticalFlow(positionV, positionU);

        float finalU = 0.0f;
        float finalV = 0.0f;

        if (avgOfFourPixels) {
            finalV = (positionsV[0] + positionsV[1] + positionsV[2] + positionsV[3]) / 4.0f;
            finalU = (positionsU[0] + positionsU[1] + positionsU[2] + positionsU[3]) / 4.0f;
            finalV = finalV - tileB.getTopPixel() - stepTilesB.getTileHeight() / 2 + 0.5f;
            finalU = finalU - tileB.getLeftPixel() - stepTilesB.getTileWidth() / 2 + 0.5f;

            //The equivalent could be achieved by:
            /*for (int idx = 0; idx < 4; idx++) {
                float V = positionsV[idx] - tileB.getTopPixel() - stepTilesB.getTileHeight() / 2 + 1.0f;
                float U = positionsU[idx] - tileB.getLeftPixel() - stepTilesB.getTileWidth() / 2 + 1.0f;
                if (idx == 1) {
                    U -= 1.0f;
                }
                if (idx == 2) {
                    V -= 1.0f;
                }
                if (idx == 3) {
                    U -= 1.0f;
                    V -= 1.0f;
                }
                finalU += U;
                finalV += V;
            }
            
            finalU /= 4.0f;
            finalV /= 4.0f;*/
        } else {
            finalV = positionsV[0] - tileB.getTopPixel() - stepTilesB.getTileHeight() / 2 + 0.5f; 
            finalU = positionsU[0] - tileB.getLeftPixel() - stepTilesB.getTileWidth() / 2 + 0.5f;
        }
        
        
        if (tileB.isMaskedDisplacement()) {
            finalU = 0.0f;
            finalV = 0.0f;
        }
        
        //Note: V and U are switched on the remaining QuickLab code (Here U is horizontal, and V is longitudinal, while on the remaining code is swapped) 
        tileB.replaceDisplacement(finalV, finalU);
    }
    
    @Override
    public List<MaxCrossResult> interpolate(final List<MaxCrossResult> results) {
        for (MaxCrossResult result : results) {
            Tile tileA = result.getTileA();
            int topA = tileA.getTopPixel();
            int leftA = tileA.getLeftPixel();
            
            Tile tileB = result.getTileB();
            IterationStepTiles stepTilesB = tileB.getParentIterationStepTiles();
                        
            //Reserve a border around the image patch for computing the derivatives
            float initialPosI = topA + stepTilesB.getTileHeight() / 2;
            float initialPosJ = leftA + stepTilesB.getTileWidth() / 2;
            if (avgOfFourPixels) {
                initialPosI -= 1.0;
                initialPosJ -= 1.0;
            } else {
                initialPosI -= 0.5;
                initialPosJ -= 0.5;
            }
    
            computeDerivativesAndMatA(initialPosI, initialPosJ);
            
            for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
                float maxI = result.getNthPeakI(peakIndex);
                float maxJ = result.getNthPeakJ(peakIndex);
            
                if (tileB.isMaskedDisplacement()) {
                    result.setNthAbsoluteDisplacement(peakIndex, 0.0f, 0.0f);
                    continue;
                }

                if (maxI == 0.0f && maxJ == 0.0f) {
                    result.setNthRelativeDisplacementFromPeak(peakIndex, maxI, maxJ);
                    continue;
                }
              
                float positionV = result.getDisplacementFromNthPeakU(peakIndex) + tileB.getDisplacedTileTop() + stepTilesB.getTileHeight() / 2;
                float positionU = result.getDisplacementFromNthPeakV(peakIndex) + tileB.getDisplacedTileLeft() + stepTilesB.getTileWidth() / 2;
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
                        positionV = tileB.getDisplacedTileTop() + stepTilesB.getTileHeight() / 2;
                    }
                    if (ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreU ||
                        ignorePIVMode == IgnorePIVBaseDisplacementsModeEnum.IgnoreUV || ignoreU) { 
                        positionU = tileB.getDisplacedTileLeft() + stepTilesB.getTileWidth() / 2;
                    }
                }
                if (avgOfFourPixels) {
                    //Top-left corner of four pixels
                    positionU -= 1.0;
                    positionV -= 1.0;
                } else {
                    //Center of four pixels
                    positionU -= 0.5;
                    positionV -= 0.5;
                }
                
                if (positionV < 0 || positionV >= filteredImgA.getHeight() + windowSize / 2) {
                    logger.warn("Tile I:{},J:{} - Peak index: {} - Cannot apply Lucas-Kanade optical flow because window is completely oustide of the image (Vertical)", 
                            tileA.getTileIndexI(), tileA.getTileIndexJ(), peakIndex);
                    result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                    continue;
                }
                
                if (positionU < 0 || positionU >= filteredImgA.getWidth() + windowSize / 2) {
                    logger.warn("Tile I:{},J:{} - Peak index: {} - Cannot apply Lucas-Kanade optical flow because window is completely oustide of the image (Horizontal)", 
                            tileA.getTileIndexI(), tileA.getTileIndexJ(), peakIndex);
                    result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                    continue;
                }
                
                computeOpticalFlow(positionV, positionU);
    
                float finalU = 0.0f;
                float finalV = 0.0f;
    
                if (avgOfFourPixels) {
                    //The average of the four positions will result in displaced position centered in the middle of the 4 center pixels.
                    finalV = (positionsV[0] + positionsV[1] + positionsV[2] + positionsV[3]) / 4.0f;
                    finalU = (positionsU[0] + positionsU[1] + positionsU[2] + positionsU[3]) / 4.0f;
                    
                    //The equivalent could be achieved by:
                    /*for (int idx = 0; idx < (avgOfFourPixels ? 4 : 1); idx++) {
                        float V = positionsV[idx] - tileB.getDisplacedTileTop() - stepTilesB.getTileHeight() / 2 + 1.0f;
                        float U = positionsU[idx] - tileB.getDisplacedTileLeft() - stepTilesB.getTileWidth() / 2 + 1.0f;
                        if (idx == 1) {
                            U -= 1.0f;
                        }
                        if (idx == 2) {
                            V -= 1.0f;
                        }
                        if (idx == 3) {
                            U -= 1.0f;
                            V -= 1.0f;
                        }
                        finalU += U;
                        finalV += V;
                    }
                    
                    finalU /= 4.0f;
                    finalV /= 4.0f;*/

                    if (absoluteDisplacementMode) {
                        //So we have to take back only 0.5 at the end, from the final position, to obtain the final velocity component.
                        finalV = finalV - tileB.getTopPixel() - (stepTilesB.getTileHeight() / 2 - 0.5f);
                        finalU = finalU - tileB.getLeftPixel() - (stepTilesB.getTileWidth() / 2 - 0.5f);
                    } else {
                        //So we have to take back only 0.5 at the end, from the final position, to obtain the final velocity component.
                        finalV = finalV - tileB.getDisplacedTileCenterV();
                        finalU = finalU - tileB.getDisplacedTileCenterH();                    
                    }    
                } else {
                    if (absoluteDisplacementMode) {
                        finalV = positionsV[0] - tileB.getTopPixel() - (stepTilesB.getTileHeight() / 2 - 0.5f);
                        finalU = positionsU[0] - tileB.getLeftPixel() - (stepTilesB.getTileWidth() / 2 - 0.5f);
                    } else {
                        finalV = positionsV[0] - tileB.getDisplacedTileCenterV();
                        finalU = positionsU[0] - tileB.getDisplacedTileCenterH();                    
                    }
                }
                
                if (absoluteDisplacementMode) {
                    result.setNthAbsoluteDisplacement(peakIndex, finalV, finalU);
                } else {
                    result.setNthRelativeDisplacementFromVelocities(peakIndex, finalV, finalU);
                }
            }
        }
        
        return results;
    }

    private void computeOpticalFlow(float positionV, float positionU) {
        Arrays.fill(positionsU, positionU);
        Arrays.fill(positionsV, positionV);
        
        for (int offsetI = -marginLeft; offsetI <= marginRight; offsetI++) {
            for (int offsetJ = -marginLeft; offsetJ <= marginRight; offsetJ++) {
                int idx = (offsetI + marginLeft) * (margins + 1) + offsetJ + marginLeft; 
                positionsU[idx] += offsetJ;
                positionsV[idx] += offsetI;
            }
        }

        for (int patchIndex = 0; patchIndex < (margins + 1)*(margins + 1); patchIndex++) {
            for (int iter = 0; iter < iterations; iter++) {
                if (!status[patchIndex]) {
                    break;
                }

                if (positionsV[patchIndex] < 0 || positionsV[patchIndex] >= filteredImgA.getHeight() + windowSize / 2) {
                    status[patchIndex] = false;
                    break;
                }
                
                if (positionsU[patchIndex] < 0 || positionsU[patchIndex] >= filteredImgA.getWidth() + windowSize / 2) {
                    status[patchIndex] = false;
                    break;
                }
                
                getImagePatch(null, filteredImgB, positionsV[patchIndex], positionsU[patchIndex], patchIndex);
                float incU = b[patchIndex][0] * A[patchIndex][0] + b[patchIndex][1] * A[patchIndex][1];
                float incV = b[patchIndex][0] * A[patchIndex][1] + b[patchIndex][1] * A[patchIndex][2];
                if (listener != null) {
                    listener.computedBs(b[patchIndex][0], b[patchIndex][1], incU, incV, iter, patchIndex);
                }
                if (FastMath.abs(incU) < 1e-2 && FastMath.abs(incV) < 1e-2) {
                    status[patchIndex] = false;
                }
                
                positionsU[patchIndex] += incU;
                positionsV[patchIndex] += incV;                    
            }
        }
    }

    private void computeDerivativesAndMatA(float initialPosI, float initialPosJ) {
        Arrays.fill(status, true);

        getImagePatch(imgPatchA, filteredImgA, initialPosI, initialPosJ, -1); 

        if (listener != null) {
            listener.computedDerivativesPreInversion(imgPatchA, dI, dJ, A);
        }
        
        for (int i = 0; i < (margins + 1)*(margins + 1); i++) {
            detA[i] = A[i][0] * A[i][2] - A[i][1] * A[i][1];
            if (detA[i] < 1.2e-7) {
                status[i] = false;
                continue;
            }
            
            //Proceed with matrix inversion
            for (int j = 0; j < 3; j++) {
                A[i][j] /= detA[i];
            }
            
            float temp = A[i][0];
            A[i][0] = A[i][2];
            A[i][2] = temp;
            A[i][1] = - A[i][1];            
        }
        
        if (listener != null) {
            listener.computedPostInversion(detA, A);
        }
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
