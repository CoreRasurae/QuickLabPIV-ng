package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.InterpolateException;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian2DInterpolator implements IBasicCrossCorrelationInterpolator {
	private IGaussian2DFitter fitter;
	private int pixelsX, pixelsY;
	private boolean logResults;
	private double[] targetValues;
	private static Logger logger = LoggerFactory.getLogger(Gaussian2DInterpolator.class);

    public static float gaussian2DAssymmetric(float gain, float uX, float sigmaX, float uY, float sigmaY, float x, float y) {
        return (float)(gain * 1.0f/(2.0f * FastMath.PI * sigmaX * sigmaY) *
                FastMath.exp(-(FastMath.pow(x - uX, 2)/(2.0f * FastMath.pow(sigmaX, 2)) + FastMath.pow(y - uY, 2)/(2.0f * FastMath.pow(sigmaY, 2)))));
    }

	public Gaussian2DInterpolator() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER);
		if (configurationObject == null) {
			throw new InterpolatorStateException("Couldn't retrieve Gaussian2D interpolator configuration");
		}
		Gaussian2DInterpolatorConfiguration configuration = (Gaussian2DInterpolatorConfiguration)configurationObject;
		Gaussian2DSubTypeFactoryEnum subType = configuration.getGaussianSubType();
		fitter = Gaussian2DSubTypeFactoryEnum.create(subType, configuration);
		
		pixelsX = configuration.getNumberOfPointsInX();
		pixelsY = configuration.getNumberOfPointsInY();
		logResults = configuration.isLogResults();
		
		targetValues = new double[pixelsX * pixelsY];
	}
	
	@Override
	public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
	    MaxCrossResult temp = new MaxCrossResult();
        temp.importOtherMax(result);
        Gaussian1DPolynomialInterpolator interp = new Gaussian1DPolynomialInterpolator();
        interp.interpolate(m, temp);

        float minFloor = result.getMinFloor();
        if (minFloor < -5.15f) {
            throw new InterpolateException("Gaussian 2D cannot handle large negative floor level of: " + minFloor);
        }
        
        float addMinFloor = 0.0f;
        if (minFloor < 0.0f) {
            addMinFloor = -minFloor + 1e-8f;
        }
        
	    for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
    		int maxI = (int)result.getNthPeakI(peakIndex);
    		int maxJ = (int)result.getNthPeakJ(peakIndex);
    		
    		if (maxI - pixelsY/2 < 0 || maxI + pixelsY/2 >= m.getHeight()) {
    			logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
    					"{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
    					peakIndex, pixelsY, m.getHeight(), m.getWidth(), maxI, maxJ);
    			result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
    			continue;
    		}
    		
    		if (maxJ - pixelsX/2 < 0 || maxJ + pixelsX/2 >= m.getWidth()) {
    			logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
    			        "{}, dimJ: {}], max. is at: [I: {}, J: {}]",
    					peakIndex, pixelsX, m.getHeight(), m.getWidth(), maxI, maxJ);
    			result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
    			continue;
    		}
    		
    		float maxValue = 0;
    		for (int i = maxI - pixelsY/2, indexI = 0; i <= maxI + pixelsY/2; i++, indexI++) {
    			for (int j = maxJ - pixelsX/2, indexJ = 0; j <= maxJ + pixelsX/2; j++, indexJ++) {
    				float value = m.getElement(i, j) + addMinFloor;
    				targetValues[indexI * pixelsX + indexJ] = value;
    				if (value > maxValue) {
    					maxValue = value;
    				}
    			}
    		}		
    		
    		//Use 1D Polynomial interpolator as an hint for the initial sub-pixel peak location -> Disabled: This fails 456 tests.
    		//fitter.setPeakPointXY(maxJ, maxI, temp.getNthPeakValue(peakIndex), temp.getNthPeakJ(peakIndex), temp.getNthPeakI(peakIndex));
    		//Instead, use the integer correlation result as an hint for the initial sub-pixel peak location
    		fitter.setPeakPointXY(maxJ, maxI, result.getNthPeakValue(peakIndex) + addMinFloor, result.getNthPeakJ(peakIndex), result.getNthPeakI(peakIndex));
    		
    		LeastSquaresBuilder lsb = new LeastSquaresBuilder();
    		lsb.model(fitter.getOrCreateFunction(), fitter.getOrCreateJacobianMatrix());
    		lsb.parameterValidator(fitter);
    		lsb.target(targetValues);
    		lsb.start(fitter.getStartVector(targetValues, maxValue));		
    		lsb.lazyEvaluation(false);
    		lsb.maxEvaluations(2000000);
    		lsb.maxIterations(200000);
    		lsb.checker(new EvaluationRmsChecker(1e-8f, 1e-12f));
    		LeastSquaresProblem problem = lsb.build();
    		
    		
    		/*RealMatrix rm = 
    		LeastSquaresProblem problem = LeastSquaresFactory.create(fitter.getOrCreateFunction(), fitter.getOrCreateJacobianMatrix(), targetValues,
    				fitter.getStartVector(targetValues, maxValue), , null, 20000, 2000);*/
    	
    		LeastSquaresOptimizer lmo = new LevenbergMarquardtOptimizer().
                    withCostRelativeTolerance(1.0e-10).
                    withParameterRelativeTolerance(1.0e-10);			
    		//LeastSquaresOptimizer lmo = new GaussNewtonOptimizer().withDecomposition(GaussNewtonOptimizer.Decomposition.SVD);
    		LeastSquaresOptimizer.Optimum lsoo = null;
    		try {
    			lsoo = lmo.optimize(problem);
    		} catch (TooManyIterationsException ex) {
    			throw new InterpolatorStateException("Cannot interpolate. Exceeded number of iterations: M[dimI: " + 
    					m.getHeight() + ", dimJ: " + m.getWidth() + "], max. is at: [I: " + maxI + ", J: " + maxJ + "]", ex);
    		}
    			
    		final double[] optimalValues = lsoo.getPoint().toArray();
    		
    		if (logResults) {
    			logger.trace("Peak index: {}, Iteration number: {}, Evaluation number: {}", 
    			        peakIndex, lsoo.getIterations(), lsoo.getEvaluations());
    		 }    			
    		result = fitter.updateMaxResult(peakIndex, result, optimalValues);
	    }
		
		return result;
	}

}
