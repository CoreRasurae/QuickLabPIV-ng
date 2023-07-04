package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class BiCubicInterpolator implements IBasicCrossCorrelationInterpolator {
    private static Logger logger = LoggerFactory.getLogger(BiCubicInterpolator.class);
	private int pixels;
	private int steps; 
	
	public BiCubicInterpolator() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(BiCubicInterpolatorConfiguration.IDENTIFIER);
		if (configurationObject == null) {
			throw new InterpolatorStateException("Couldn't retrieve BiCubic interpolator configuration");
		}
		BiCubicInterpolatorConfiguration configuration = (BiCubicInterpolatorConfiguration)configurationObject;
		pixels = configuration.getInterpolationPixels();
		steps = configuration.getInterpolationSteps();
	}

	@Override
	public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
	    BicubicInterpolator interpolator = new BicubicInterpolator();
		
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
     		
    		double[] xval = new double[pixels];
    		double[] yval = new double[pixels];
    		double[][] fval = new double[pixels][pixels];
    		
    		int indexI = 0;
    		int indexJ = 0;
    		for (int i = maxI - pixels/2; i <= maxI + pixels/2; i++) {
    			yval[indexI++] = i;
    		}
    
    		for (int j = maxJ - pixels/2; j <= maxJ + pixels/2; j++) {
    			xval[indexJ++] = j;
    		}
    
    		
    		indexI = 0;
    		for (int i = maxI - pixels/2; i <= maxI + pixels/2; i++) {
    			indexJ = 0;
    			for (int j = maxJ - pixels/2; j <= maxJ + pixels/2; j++) {
    				fval[indexJ][indexI] = m.getElement(i, j);
    				indexJ++;
    			}
    			indexI++;
    		}
    		
    		BicubicInterpolatingFunction f = interpolator.interpolate(xval, yval, fval);
    		
    		
    		//Find max at designed steps
    		float interpolatedMaxI = 0.0f;
    		float interpolatedMaxJ = 0.0f;
    		float interpolatedMax = 0.0f;
    		boolean failed = false;
    		float failedI = 0;
    		float failedJ = 0;
    		for (float i = maxI - 1; i <= maxI + 1; i += pixels*1.0f/steps) {
    			for (float j = maxJ - 1; j <= maxJ + 1; j += pixels*1.0f/steps) {
    				if (!f.isValidPoint(j, i)) {
				        failed = true;
				        failedI = i;
				        failedJ = j;
				        break;
    				}
    				
    				float value = (float)f.value(j, i);
    				
    				if (value > interpolatedMax) {
    					interpolatedMaxI = i;
    					interpolatedMaxJ = j;
    					interpolatedMax = value;
    				}
    			}
    			
    			if (failed) {
    			    break;
    			}
    		}
    		
    		if (failed) {
    		    logger.warn("Peak index {} - Cannot interpolate point at [I: {}, J: {}]", peakIndex, failedI, failedJ);
    		    result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
    		    continue;
    		}

            result.setNthRelativeDisplacementFromPeak(peakIndex, interpolatedMaxI, interpolatedMaxJ);                
		}
		
		return result;
	}

}
