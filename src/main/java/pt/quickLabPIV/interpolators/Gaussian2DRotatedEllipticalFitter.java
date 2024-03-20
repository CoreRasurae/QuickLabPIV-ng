// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.maximum.MaxCrossResult;

/**
 * Defines a LeastSquares optimizer model and fitter for a 2D Elliptical Gaussian function with rotation
 * 
 * @author lpnm
 */
public class Gaussian2DRotatedEllipticalFitter implements IGaussian2DFitter {
	private static final Logger logger = LoggerFactory.getLogger(Gaussian2DRotatedEllipticalFitter.class);
	
	private int numberOfPointsInX;
	private int numberOfPointsInY;
	private int startX;
	private int startY;
	private float initialEstimateX;
	private float initialEstimateY;
	private boolean logResults;

	private Gaussian2DFunction gaussian = null;
	private Gaussian2DJacobianMatrix jacobian = null;
	
	public Gaussian2DRotatedEllipticalFitter(Gaussian2DInterpolatorConfiguration configuration) {
		numberOfPointsInX = configuration.getNumberOfPointsInX();
		numberOfPointsInY = configuration.getNumberOfPointsInY();
		logResults = configuration.isLogResults();
	}
	
	public final class Gaussian2DFunction implements MultivariateVectorFunction {
		@Override
		public double[] value(double[] args) throws IllegalArgumentException {
			double a = args[0];
			double meanX = args[1];
			double meanY = args[2];
			double sigmaX = args[3];
			double sigmaY = args[4];
			double theta = args[5];
			double b = args[6];
			
			double[] functionValues = new double[numberOfPointsInX * numberOfPointsInY];
			
			double sigmaXSquared = sigmaX * sigmaX;
			double sigmaYSquared = sigmaY * sigmaY;
			
			double a1 = FastMath.pow(FastMath.cos(theta), 2) / (2.0 * sigmaXSquared) + FastMath.pow(FastMath.sin(theta), 2) / (2.0 * sigmaYSquared);
			double a2 = - FastMath.sin(2.0 * theta) / (4.0 * sigmaXSquared) + FastMath.sin(2.0 * theta) / (4.0 * sigmaYSquared);
			double a3 = FastMath.pow(FastMath.sin(theta), 2) / (2.0 * sigmaXSquared) + FastMath.pow(FastMath.cos(theta), 2) / (2.0 * sigmaYSquared);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					functionValues[y * numberOfPointsInX + x] = a *
							FastMath.exp(-a1*FastMath.pow((startX + x - meanX), 2)) *
							FastMath.exp(-2.0*a2*(startX + x - meanX)*(startY + y - meanY)) *
							FastMath.exp(-a3*FastMath.pow((startY + y - meanY), 2)) * + b;
				}
			}
			
			return functionValues;
		}
	}
	
	/**
	 * 
	 * 
	 * @author lpnm
	 */
	public final class Gaussian2DJacobianMatrix implements MultivariateMatrixFunction {

		@Override
		public double[][] value(double[] args) {
			double a = args[0];
			double meanX = args[1];
			double meanY = args[2];
			double sigmaX = args[3];
			double sigmaY = args[4];
			double theta = args[5];
			//double b = args[6];
			
			double[][] jacobianValues = new double[numberOfPointsInX * numberOfPointsInY][7];
			
			double sigmaXSquared = sigmaX * sigmaX;
			double sigmaYSquared = sigmaY * sigmaY;

			double a1 = FastMath.pow(FastMath.cos(theta), 2) / (2.0 * sigmaXSquared) + FastMath.pow(FastMath.sin(theta), 2) / (2.0 * sigmaYSquared);
			double a2 = - FastMath.sin(2.0 * theta) / (4.0 * sigmaXSquared) + FastMath.sin(2.0 * theta) / (4.0 * sigmaYSquared);
			double a3 = FastMath.pow(FastMath.sin(theta), 2) / (2.0 * sigmaXSquared) + FastMath.pow(FastMath.cos(theta), 2) / (2.0 * sigmaYSquared);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					double expX = FastMath.exp(-a1*FastMath.pow((startX + x - meanX), 2));
					double expXY = FastMath.exp(-2.0*a2*(startX + x - meanX)*(startY + y - meanY));
					double expY = FastMath.exp(-a3*FastMath.pow((startY + y - meanY), 2));
							
					int pointIndex = y * numberOfPointsInX + x;
					
					//df(v)/da
					jacobianValues[pointIndex][0] = expX * expXY * expY;
					
					//df(v)/dmeanX
					jacobianValues[pointIndex][1] = a * jacobianValues[pointIndex][0] * (2.0 * a1 * (startX + x - meanX) + 2.0 * a2 * (startY + y - meanY));
					
					//df(v)/dmeanY
					jacobianValues[pointIndex][2] = a * jacobianValues[pointIndex][0] * (2.0 * a2 * (startX + x - meanX) + 2.0 * a3 * (startY + y - meanY));
					
					//df(v)/dsigmaX
					jacobianValues[pointIndex][3] = a * jacobianValues[pointIndex][0] * (FastMath.pow(FastMath.cos(theta), 2) * FastMath.pow(startX + x - meanX, 2) - 
							FastMath.sin(2.0 * theta) * (startX + x - meanX) * (startY + y - meanY) + 
							FastMath.pow(FastMath.sin(theta), 2) * FastMath.pow(startY + y - meanY, 2)) /
							(sigmaXSquared * sigmaX);
					
					//df(v)/dsigmaY
					jacobianValues[pointIndex][4] = a * jacobianValues[pointIndex][0] * (FastMath.pow(FastMath.sin(theta), 2) * FastMath.pow(startX + x - meanX, 2) + 
							FastMath.sin(2.0 * theta) * (startX + x - meanX) * (startY + y - meanY) + 
							FastMath.pow(FastMath.cos(theta), 2) * FastMath.pow(startY + y - meanY, 2)) /
							(sigmaYSquared * sigmaY);
					
					//df(v)/dtheta
					jacobianValues[pointIndex][5] = a * jacobianValues[pointIndex][0] * -(
							((- 2.0 * FastMath.cos(theta) * FastMath.sin(theta)) / (2.0 * sigmaXSquared) + 
							 (2.0 * FastMath.sin(theta) * FastMath.cos(theta)) / (2.0 * sigmaYSquared)) * FastMath.pow(startX + x - meanX, 2) +
							2.0 * ((- FastMath.cos(2.0 * theta) * 2.0) / (4.0 * sigmaXSquared) + 
							 (FastMath.cos(2.0 * theta) * 2.0) / (4.0 * sigmaYSquared)) * (startX + x - meanX) * (startY + y - meanY) +
							((2.0 * FastMath.sin(theta) * FastMath.cos(theta))/(2.0 * sigmaXSquared) -
							 (2.0 * FastMath.cos(theta) * FastMath.sin(theta))/(2.0 * sigmaYSquared)) * FastMath.pow(startY + y - meanY, 2));
					
					//df(v)/db
					jacobianValues[pointIndex][6] = 1;
				}
			}
			
			return jacobianValues;
		}
	}
	
    @Override
    public void setPeakPointXY(int x, int y, float _maxValue, float _initialEstimateX, float _initialEstimateY) {
        startX = x - numberOfPointsInX/2;
        startY = y - numberOfPointsInY/2;
        initialEstimateX = _initialEstimateX;
        initialEstimateY = _initialEstimateY;
    }
	
	@Override
	public MultivariateVectorFunction getOrCreateFunction() {
		if (gaussian == null) {
			gaussian = new Gaussian2DFunction();
		}
		
		return gaussian;
	}
	
	@Override
	public MultivariateMatrixFunction getOrCreateJacobianMatrix() {
		if (jacobian == null) {
			jacobian = new Gaussian2DJacobianMatrix();
		}
		
		return jacobian;
	}

	@Override
	public MaxCrossResult updateMaxResult(int peakIndex, MaxCrossResult result, double[] optimalValues) {
		double theta = optimalValues[5];
		double meanX = optimalValues[1];
		double meanY = optimalValues[2];
				
		float estimatedPeakX = (float)(FastMath.cos(theta) * meanX - FastMath.sin(theta) * meanY);
		float estimatedPeakY = (float)(FastMath.sin(theta) * meanX + FastMath.cos(theta) * meanY);

        boolean discarded = false;
		if (FastMath.abs(estimatedPeakX - initialEstimateX) > 1.5f || FastMath.abs(estimatedPeakY - initialEstimateY) > 1.5f) {
		    estimatedPeakX = initialEstimateX;
		    estimatedPeakY = initialEstimateY;
		    discarded = true;
		}
        result.setNthRelativeDisplacementFromPeak(peakIndex, estimatedPeakY, estimatedPeakX);
		result.setNthPeakValue(peakIndex, (float)optimalValues[0]); //a - coefficient
		
		if (logResults) {
			logger.trace("a: " + optimalValues[0]);
			logger.trace("meanX: " + optimalValues[1]);
			logger.trace("meanY: " + optimalValues[2]);
			logger.trace("sigmaX: " + optimalValues[3]);
			logger.trace("sigmaY: " + optimalValues[4]);
			logger.trace("theta: " + optimalValues[5]);
			logger.trace("b: " + optimalValues[6]);

			logger.info("Interpolated Peak index: " + peakIndex + ", X: " + result.getMainPeakJ() + ", Y: " + result.getMainPeakI() + ",Discarded: " + discarded);
		}
		
		return result;
	}

	@Override
	public double[] getStartVector(double[] targetValues, double maxValue) {
		double[] startValues = new double[7];
		
		startValues[0] = maxValue;
		startValues[1] = initialEstimateX;
		startValues[2] = initialEstimateY;
		startValues[3] = 1.0;
		startValues[4] = 1.0;
		startValues[5] = 0.0;
		startValues[6] = 0.0;
		
		return startValues;
	}

	@Override
	public RealVector validate(RealVector params) {
		return params;
	}
}
