package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;

import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IGaussian2DFitter extends ParameterValidator {
	public void setPeakPointXY(int x, int y, float _maxValue, float _initialEstimateX, float _initialEstimateY);
	
	public MultivariateVectorFunction getOrCreateFunction();
	
	public MultivariateMatrixFunction getOrCreateJacobianMatrix();

	public MaxCrossResult updateMaxResult(int peakIndex, MaxCrossResult result, double[] optimalValues);

	public double[] getStartVector(double[] targetValues, double maxValue);
}
