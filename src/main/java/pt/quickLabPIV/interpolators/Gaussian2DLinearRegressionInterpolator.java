// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian2DLinearRegressionInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Gaussian2DLinearRegressionInterpolator.class);
    private int pixels;
    private final RealMatrix matInv;
    private final double coefs[] = new double[5];
    
    public Gaussian2DLinearRegressionInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian2DLinearRegressionInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Gaussian1D interpolator configuration");
        }
        Gaussian2DLinearRegressionInterpolatorConfiguration configuration = (Gaussian2DLinearRegressionInterpolatorConfiguration)configurationObject;
        pixels = configuration.getInterpolationPixels();
        
        double mat[][] = new double[pixels * pixels][5];
        for (int i = -pixels/2; i <= pixels/2; i++) {
            for (int j = -pixels/2; j <= pixels/2; j++) {
                mat[(i + pixels/2) * pixels + (j + pixels/2)][0] = j*j;
                mat[(i + pixels/2) * pixels + (j + pixels/2)][1] = i*i;
                mat[(i + pixels/2) * pixels + (j + pixels/2)][2] = j;
                mat[(i + pixels/2) * pixels + (j + pixels/2)][3] = i;
                mat[(i + pixels/2) * pixels + (j + pixels/2)][4] = 1;
            }
        }
        RealMatrix r = new Array2DRowRealMatrix(mat);
        SingularValueDecomposition svd = new SingularValueDecomposition(r);
        RealMatrix s = svd.getS();
        for (int i = 0; i < 5; i++) {
            double v = s.getEntry(i, i);
            if (v > 0.0) {
                s.setEntry(i, i, 1.0/v);
            }
        }
        matInv = svd.getU().multiply(s).multiply(svd.getVT());
    }

    @Override
    public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
        for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
            int maxI = (int)result.getNthPeakI(peakIndex);
            int maxJ = (int)result.getNthPeakJ(peakIndex);
            
            if (maxI == 0.0f && maxJ == 0.0f) {
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
                        
            if (maxI - pixels/2 < 0 || maxI + pixels/2 >= m.getHeight()) {
                logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            if (maxJ - pixels/2 < 0 || maxJ + pixels/2 >= m.getWidth()) {
                logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                        m.getHeight() + ", dimJ: " + m.getWidth() + "], max. is at: [I: " + maxI + ", J: " + maxJ + "]",
                        peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            RealVector rv = new ArrayRealVector(pixels * pixels);
            for (int i = -pixels/2; i <= pixels/2; i++) {
                for (int j = -pixels/2; j <= pixels/2; j++) {
                    float value = m.getElement(maxI + i, maxJ + j);
                    if (value <= 0.0f) {
                        value = 1e-6f;
                    }
                    rv.setEntry((i + pixels/2) * pixels + (j + pixels/2), FastMath.log(value));
                }
            }
            
            
            for (int i = 0; i < 5; i++) {
                coefs[i] = (float)rv.dotProduct(matInv.getColumnVector(i));
            }
            
            double sigmax = 1.0f / FastMath.sqrt(-2.0 * coefs[0]);
            double sigmay = 1.0f / FastMath.sqrt(-2.0 * coefs[1]);
            float dispX = (float) (coefs[2] * sigmax * sigmax);
            float dispY = (float) (coefs[3] * sigmay * sigmay);
            
            float peakI;
            float peakJ;
            
            if (Float.isNaN(dispX) || Float.isNaN(dispY)) {
               MaxCrossResult rTemp = new MaxCrossResult();
               rTemp.setTotalPeaks(1, true);
               rTemp.setAssociatedTileA(result.tileA);
               rTemp.setAssociatedTileB(result.tileB);
               rTemp.setMainPeakI(maxI);
               rTemp.setMainPeakJ(maxJ);
               rTemp.setMainPeakValue(result.getNthPeakValue(peakIndex));
               Gaussian1DPolynomialInterpolator interpolator = new Gaussian1DPolynomialInterpolator();
               rTemp = interpolator.interpolate(m, rTemp);
               peakI = rTemp.getMainPeakI();
               peakJ = rTemp.getMainPeakJ();
               result.setNthPeakValue(peakIndex, rTemp.getMainPeakValue());
            } else {            
               peakI = maxI + dispY;
               peakJ = maxJ + dispX;
            }
            
            result.setNthRelativeDisplacementFromPeak(peakIndex, peakI, peakJ);
        }
        
        return result;
    }
    
    
}
