// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.maximum.MaxCrossResult;

/**
 * Follows Raffel Gaussian1D approximated polynomial interpolation.
 * @author lpnm
 *
 */
public class Gaussian1DPolynomialInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Gaussian1DPolynomialInterpolator.class);
    
	@Override
	public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
	    for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
    		int maxI = (int)result.getNthPeakI(peakIndex);
    		int maxJ = (int)result.getNthPeakJ(peakIndex);
    		
    		if (maxI == 0.0f && maxJ == 0.0f) {
    		    result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
    		    continue;
    		}
    		
            if (maxI - 1 < 0 || maxI + 1 >= m.getHeight()) {
                logger.warn("Peak index: {} - Cannot interpolate for 3 interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            if (maxJ - 1 < 0 || maxJ + 1 >= m.getWidth()) {
                logger.warn("Peak index: {} - Cannot interpolate for 3 interpolating points. It would access outside matrix M[dimI: " + 
                        m.getHeight() + ", dimJ: " + m.getWidth() + "], max. is at: [I: " + maxI + ", J: " + maxJ + "]",
                        peakIndex, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }

            boolean doGaussian = true;
            float valuesY[] = new float[3];
            float valuesX[] = new float[3];
            for (int  i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    float value = m.getElement(maxI + i, j + maxJ);
                    if (j == 0) {
                        if (m.getElement(maxI + i, j + maxJ) < 0.0f) {
                            doGaussian = false;
                        }
                        valuesY[i + 1] = value + 1e-8f;
                    }
                    if (i == 0) {
                        if (m.getElement(maxI + i, j + maxJ) < 0.0f) {
                            doGaussian = false;
                        }
                        valuesX[j + 1] = value + 1e-8f;
                    }
                }
            }

            float peakI = result.getNthPeakI(peakIndex);
            float peakJ = result.getNthPeakJ(peakIndex);

            if (doGaussian) {                
                float tempPeakI = maxI + (float)(FastMath.log(valuesY[0])-FastMath.log(valuesY[2]))/
                        (float)(2.0f*FastMath.log(valuesY[0]) - 4.0f*FastMath.log(valuesY[1]) + 2.0f*FastMath.log(valuesY[2]));
                if (Float.isFinite(tempPeakI) && FastMath.abs(tempPeakI - maxI) <= 1.0f) {
                    peakI = tempPeakI;
                } //Otherwise preserve the maxI peak location
        
                float tempPeakJ = maxJ + (float)(FastMath.log(valuesX[0])-FastMath.log(valuesX[2]))/
                        (float)(2.0f*FastMath.log(valuesX[0]) - 4.0f*FastMath.log(valuesX[1]) + 2.0f*FastMath.log(valuesX[2]));
                if (Float.isFinite(tempPeakJ) && FastMath.abs(tempPeakJ - maxJ) <= 1.0f) {
                    peakJ = tempPeakJ;
                } //Otherwise preserve the maxJ peak location
            } else {
                logger.warn("Using Parabolic sub-pixel instead of Gaussian");
                float tempPeakI = maxI + (float)(valuesY[0]-valuesY[2])/
                        (float)(2.0f*valuesY[0] - 4.0f*valuesY[1] + 2.0f*valuesY[2]);
                if (Float.isFinite(tempPeakI) && FastMath.abs(tempPeakI - maxI) <= 1.0f) {
                    peakI = tempPeakI;
                } //Otherwise preserve the maxI peak location
        
                float tempPeakJ = maxJ + (float)(valuesX[0]-valuesX[2])/
                        (float)(2.0f*valuesX[0] - 4.0f*valuesX[1] + 2.0f*valuesX[2]);
                if (Float.isFinite(tempPeakJ) && FastMath.abs(tempPeakJ - maxJ) <= 1.0f) {
                    peakJ = tempPeakJ;
                } //Otherwise preserve the maxJ peak location
            }
            
            result.setNthRelativeDisplacementFromPeak(peakIndex, peakI, peakJ);
	    }
	    
		return result;
	}

}
