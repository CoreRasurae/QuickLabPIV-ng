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
 * Defines a LeastSquares optimizer model and fitter for a 2D Assymmetric Gaussian function with rotation
 * @author lpnm
 *
 */
public class Gaussian2DRotatedAssymmetricFitter implements IGaussian2DFitter {
	private static Logger logger = LoggerFactory.getLogger(Gaussian2DRotatedAssymmetricFitter.class);
	
	private int numberOfPointsInX;
	private int numberOfPointsInY;
	private int startX;
	private int startY;
	private float initialEstimateX;
	private float initialEstimateY;
	private float maxValue;
	private boolean logResults;

	private Gaussian2DFunction gaussian = null;
	private Gaussian2DJacobianMatrix jacobian = null;
	
	public Gaussian2DRotatedAssymmetricFitter(Gaussian2DInterpolatorConfiguration configuration) {
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
			double constant = 1.0/(2.0 * FastMath.PI * sigmaX * sigmaY);
			double cosTheta = FastMath.cos(theta);
			double sinTheta = FastMath.sin(theta);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					double thetaX = cosTheta*(startX + x - meanX) - sinTheta*(startY + y - meanY);
					double thetaY = sinTheta*(startX + x - meanX) + cosTheta*(startY + y - meanY);
					
					functionValues[y * numberOfPointsInX + x] = a * constant * 
							FastMath.exp(-FastMath.pow(thetaX, 2)/(2.0 * sigmaXSquared)) *
							FastMath.exp(-FastMath.pow(thetaY, 2)/(2.0 * sigmaYSquared)) + b;
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
			double constant = 1.0/(2.0 * FastMath.PI * sigmaX * sigmaY);
			double cosTheta = FastMath.cos(theta);
			double sinTheta = FastMath.sin(theta);			
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					double thetaX = cosTheta*(startX + x - meanX) - sinTheta*(startY + y - meanY);
					double thetaY = sinTheta*(startX + x - meanX) + cosTheta*(startY + y - meanY);

					double expX = FastMath.exp(-FastMath.pow(thetaX, 2)/(2.0 * sigmaXSquared));
					double expY = FastMath.exp(-FastMath.pow(thetaY, 2)/(2.0 * sigmaYSquared));
							
					int pointIndex = y * numberOfPointsInX + x;
					
					//df(v)/da
					jacobianValues[pointIndex][0] = constant * expX * expY;
					
					double hu = a * jacobianValues[pointIndex][0] * (thetaX) / sigmaXSquared;
					double mu = a * jacobianValues[pointIndex][0] * (thetaY) / sigmaYSquared;
					
					//df(v)/dmeanX
					jacobianValues[pointIndex][1] = hu * cosTheta + mu * sinTheta;
					
					//df(v)/dmeanY
					jacobianValues[pointIndex][2] = mu * cosTheta - hu * sinTheta;
					
					//df(v)/sigmaX
					jacobianValues[pointIndex][3] = hu * thetaX / sigmaX - 
							a * jacobianValues[pointIndex][0] * 1.0/sigmaX;
					
					//df(v)/sigmaY
					jacobianValues[pointIndex][4] = mu * (thetaY) / sigmaY - 
							a * jacobianValues[pointIndex][0] * 1.0/sigmaY;
					
					//df(v)/theta
					jacobianValues[pointIndex][5] = hu * (sinTheta * (startX + x - meanX) + cosTheta * (startY + y - meanY)) -
							mu * (cosTheta * (startX + x - meanX) - sinTheta* (startY + y - meanY));
					
					//df(v)/b
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
		maxValue = _maxValue;
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
        float estimatedPeakX = (float)optimalValues[1]; //meanX
        float estimatedPeakY = (float)optimalValues[2]; //meanY

        boolean discarded = false;
        if (FastMath.abs(estimatedPeakX - initialEstimateX) > 1.5f || FastMath.abs(estimatedPeakY - initialEstimateY) > 1.5f) {
            estimatedPeakX = initialEstimateX;
            estimatedPeakY = initialEstimateY;
            discarded = true;
        }
        result.setNthRelativeDisplacementFromPeak(peakIndex, estimatedPeakY, estimatedPeakX);

	    result.setNthPeakValue(peakIndex, (float)optimalValues[0]);//a - coefficient
	    	
		if (logResults) {
			logger.trace("a: " + optimalValues[0]);
			logger.trace("meanX: " + optimalValues[1]);
			logger.trace("meanY: " + optimalValues[2]);
			logger.trace("sigmaX: " + optimalValues[3]);
			logger.trace("sigmaY: " + optimalValues[4]);
			logger.trace("theta: " + optimalValues[5]);
			logger.trace("b: " + optimalValues[6]);
			
			logger.info("Interpolated peak index: " + peakIndex + ", X: " + result.getNthPeakJ(peakIndex) + ", Y: " + result.getNthPeakI(peakIndex) + ",Discarded: " + discarded);
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
	    if (params.getEntry(0) < maxValue / 20.0 || 
	        params.getEntry(1) > startX + numberOfPointsInX/2 + 1 || params.getEntry(1) < startX + numberOfPointsInX/2 - 1 ||
            params.getEntry(2) > startY + numberOfPointsInY/2 + 1 || params.getEntry(2) < startY + numberOfPointsInY/2 - 1) {
            params.setEntry(0, maxValue);
            params.setEntry(1, startX + numberOfPointsInX/2);
            params.setEntry(2, startY + numberOfPointsInY/2);
        };
        return params;	}
}
