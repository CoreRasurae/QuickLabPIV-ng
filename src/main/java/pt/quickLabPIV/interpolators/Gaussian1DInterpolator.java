package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.InterpolateException;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian1DInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Gaussian1DInterpolator.class);
    
	private int pixels;
	
	public Gaussian1DInterpolator() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER);
		if (configurationObject == null) {
			throw new InterpolatorStateException("Couldn't retrieve Gaussian1D interpolator configuration");
		}
		Gaussian1DInterpolatorConfiguration configuration = (Gaussian1DInterpolatorConfiguration)configurationObject;
		pixels = configuration.getInterpolationPixels();
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
    		
    		double[] parametersI;
    		double[] parametersJ;
    		int resultIndex;
    		
    		resultIndex = 1;
    		
            float minFloor = result.getMinFloor();
            if (minFloor < -3.15f) {
                throw new InterpolateException("Gaussian 1D-1D cannot handle large negative floor level of: " + minFloor);
            }
            
            float addMinFloor = 0.0f;
            if (minFloor < 0.0f) {
                addMinFloor = -minFloor + 1e-8f;
            }
    		
    		WeightedObservedPoints pointsI = new WeightedObservedPoints();
    		for (int i = maxI - pixels/2; i <= maxI + pixels/2; i++) {
    			pointsI.add(i, m.getElement(i, maxJ) + addMinFloor);
    		}
            
            GaussianCurveFitter fitterI = GaussianCurveFitter.create();
            fitterI.withMaxIterations(1);
            parametersI = fitterI.fit(pointsI.toList());
    
            WeightedObservedPoints pointsJ = new WeightedObservedPoints();
            for (int j = maxJ - pixels/2; j <= maxJ + pixels/2; j++) {
                pointsJ.add(j, m.getElement(maxI, j) + addMinFloor);
            }
            
            GaussianCurveFitter fitterJ = GaussianCurveFitter.create();
            fitterJ.withMaxIterations(1);
            parametersJ = fitterJ.fit(pointsJ.toList());
    		
            float peakI = result.getNthPeakI(peakIndex);
            float peakJ = result.getNthPeakJ(peakIndex);
    		if (FastMath.abs(parametersI[resultIndex] - result.getNthPeakI(peakIndex)) < 2.0f) {
    			peakI = (float)parametersI[resultIndex];
    		} else {
    		    //TODO Log failed interpolation?
    		}
    		
    		if (FastMath.abs(parametersJ[resultIndex] - result.getNthPeakJ(peakIndex)) < 2.0f) {
    			peakJ = (float)parametersJ[resultIndex];
    		} else {
    		    //TODO Log failed interpolation?
    		}
    		
            result.setNthRelativeDisplacementFromPeak(peakIndex, peakI, peakJ);
	    }
	    
		return result;
	}

}
