package pt.quickLabPIV.interpolators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Centroid2DInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Centroid2DInterpolator.class);
    
    private final int interpPoints;
    private final float centroidX[][];
    private final float centroidY[][];
    
    public Centroid2DInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Centroid2DInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            //If there is no configuration object for centroid 2D, than maybe we are be called as a fallback for the Polynomial Gaussian 2D,
            //to the 2D Centroid instead as a fallback, so grab the configuration from the 2D Gaussian,
            configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian2DPolynomialInterpolatorConfiguration.IDENTIFIER);
            if (configurationObject == null) {   
                throw new InterpolatorStateException("Couldn't retrieve neither the Centroid2D interpolator configuration, nor the Gaussian2DPolynomial configuration");
            }
            Gaussian2DPolynomialInterpolatorConfiguration gaussianConfig = (Gaussian2DPolynomialInterpolatorConfiguration)configurationObject;
            interpPoints = gaussianConfig.getInterpolationPixelsForCentroid2D();
        } else {
            Centroid2DInterpolatorConfiguration configuration = (Centroid2DInterpolatorConfiguration)configurationObject;        
            interpPoints = configuration.getInterpolationPixels();
        }
        
        float tempCentroidX[][] = new float[interpPoints][interpPoints];
        float tempCentroidY[][] = new float[interpPoints][interpPoints];
        
        for (int iPos = -interpPoints/2, i = 0; iPos <= interpPoints/2; iPos++, i++) {
            for (int jPos = -interpPoints/2, j = 0; jPos <= interpPoints/2; jPos++, j++) {
                tempCentroidX[i][j] = jPos;
                tempCentroidY[i][j] = iPos;
            }
        }
        
        centroidX = tempCentroidX;
        centroidY = tempCentroidY;
    }
    
    @Override
    public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
        float peakMatrix[][] = new float[interpPoints][interpPoints];
        
        for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
            int peakI = (int)result.getNthPeakI(peakIndex);
            int peakJ = (int)result.getNthPeakJ(peakIndex);
            
            if (peakI - interpPoints/2 < 0 || peakI + interpPoints/2 >= m.getHeight()) {
                logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, interpPoints, m.getHeight(), m.getWidth(), peakI, peakJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            if (peakJ - interpPoints/2 < 0 || peakJ + interpPoints/2 >= m.getWidth()) {
                logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                        "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                        peakIndex, interpPoints, m.getHeight(), m.getWidth(), peakI, peakJ);
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            double peakSum = 0.0f;
            for (int peakIndexI = peakI - interpPoints/2, offsetI = 0; peakIndexI <= peakI + interpPoints/2; peakIndexI++, offsetI++) {
                for (int peakIndexJ = peakJ - interpPoints/2, offsetJ = 0; peakIndexJ <= peakJ + interpPoints/2; peakIndexJ++, offsetJ++) {
                    peakMatrix[offsetI][offsetJ] = m.getElement(peakIndexI, peakIndexJ);
                    peakSum += m.getElement(peakIndexI, peakIndexJ);
                }
            }
    
            double subPixelX = 0.0f;
            double subPixelY = 0.0f;
            for (int offsetI = 0; offsetI  < interpPoints; offsetI++) {
                for (int offsetJ = 0; offsetJ < interpPoints; offsetJ++) {
                    subPixelX += (centroidX[offsetI][offsetJ] + peakJ) * peakMatrix[offsetI][offsetJ];
                    subPixelY += (centroidY[offsetI][offsetJ] + peakI) * peakMatrix[offsetI][offsetJ];
                }
            }
            
            subPixelX /= peakSum;
            subPixelY /= peakSum;
            
            result.setNthRelativeDisplacementFromPeak(peakIndex, (float)subPixelY, (float)subPixelX);                
        }
        
        return result;
    }

}
