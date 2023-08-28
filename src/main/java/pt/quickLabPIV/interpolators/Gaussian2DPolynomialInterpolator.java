// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian2DPolynomialInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Gaussian2DPolynomialInterpolator.class);
    
    private int interpPoints;
    private final Centroid2DInterpolator centroidInterpolator;
    
    public Gaussian2DPolynomialInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian2DPolynomialInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Gaussian2D interpolator configuration");
        }
        Gaussian2DPolynomialInterpolatorConfiguration configuration = (Gaussian2DPolynomialInterpolatorConfiguration)configurationObject;
        
        interpPoints = configuration.getInterpolationPixelsForCentroid2D();
        centroidInterpolator = new Centroid2DInterpolator();
    }
    
    @Override
    public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
        //TODO create an object pool for this...
        float peakMatrix[][] = new float[3][3];
        
        float minFloor = result.getMinFloor();
        
        float addMinFloor = 0.0f;
        if (minFloor < 0.0f) {
            addMinFloor = -minFloor + 1e-8f;
        }
        
        for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
            int maxI = (int)result.getNthPeakI(peakIndex);
            int maxJ = (int)result.getNthPeakJ(peakIndex);
            
            if (maxI - 1 < 0 || maxI + 1 >= m.getHeight()) {
                logger.warn("Peak index: {} - Cannot interpolate for 3 interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            if (maxJ - 1 < 0 || maxJ + 1 >= m.getWidth()) {
                logger.warn("Peak index: {} - Cannot interpolate for 3 interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            for (int i = maxI - 1, iOffset = 0; i <= maxI + 1; i ++, iOffset++) {
                for (int j = maxJ - 1, jOffset = 0; j <= maxJ + 1; j++, jOffset++) {
                    float peakValue = m.getElement(i, j) + addMinFloor;
                    peakMatrix[iOffset][jOffset] = peakValue;
                }
            }
                    
            double c00 = 0.0;
            double c10 = 0.0;
            double c01 = 0.0;
            double c11 = 0.0;
            double c20 = 0.0;
            double c02 = 0.0;
            
            for (int iOffset = 0; iOffset < 3; iOffset ++) {
                for (int jOffset = 0; jOffset < 3; jOffset ++) {
                    float peak = peakMatrix[iOffset][jOffset];
                    
                    c10 += (jOffset - 1.0) * FastMath.log(peak);
                    c01 += (iOffset - 1.0) * FastMath.log(peak);
                    c11 += (jOffset - 1.0) * (iOffset - 1.0) * FastMath.log(peak);
                    c20 += (3.0 * (jOffset - 1.0f)*(jOffset - 1.0f) - 2.0f) * FastMath.log(peak);
                    c02 += (3.0 * (iOffset - 1.0f)*(iOffset - 1.0f) - 2.0f) * FastMath.log(peak);
                    c00 = ((5.0 - 3.0 * (jOffset - 1.0f)*(jOffset - 1.0f) - 3.0f * (iOffset - 1.0f)*(iOffset - 1.0f)) * FastMath.log(peak));
                }
            }

            c00 = c00 / 9.0;
            c10 = c10 / 6.0;
            c01 = c01 / 6.0;
            c11 = c11 / 4.0;
            c20 = c20 / 6.0;
            c02 = c02 / 6.0;
            
            double subPixelX = (c11 * c01 - 2 * c10 * c02) / (4 * c20 * c02 - c11 * c11);
            double subPixelY = (c11 * c10 - 2 * c01 * c20) / (4 * c20 * c02 - c11 * c11);

            float peakI = result.getNthPeakI(peakIndex);
            float peakJ = result.getNthPeakJ(peakIndex);
            
            if (subPixelX * subPixelX + subPixelY * subPixelY > 2 * (0.5 + interpPoints) * (0.5 + interpPoints)) {
                //Employ the Centroid method in this case...
                logger.info("Failed to compute the sub-pixel with Polynomial 2D Gaussian, trying with the Centroid method");
                MaxCrossResult tempResult = new MaxCrossResult();
                tempResult.setCrossMatrix(m);
                tempResult.setAssociatedTileB(result.tileB);
                tempResult.setAssociatedTileA(result.tileA);
                tempResult.setMainPeakI(peakI);
                tempResult.setMainPeakI(peakJ);
                
                tempResult = centroidInterpolator.interpolate(m, tempResult);
                subPixelY = tempResult.getMainPeakI() - maxI;
                subPixelX = tempResult.getMainPeakJ() - maxJ;
                
                if (subPixelX * subPixelX + subPixelY * subPixelY > 2 * (0.5 + interpPoints) * (0.5 + interpPoints)) {
                    logger.warn("Failed to compute sub-pixel for");
                } else {
                    peakI = maxI + (float)subPixelY;
                    peakJ = maxJ + (float)subPixelX;
                }
            } else {
                peakI = maxI + (float)subPixelY;
                peakJ = maxJ + (float)subPixelX;
            }
            
            result.setNthRelativeDisplacementFromPeak(peakIndex, peakI, peakJ);
        }
                    
        return result;
    }

}
