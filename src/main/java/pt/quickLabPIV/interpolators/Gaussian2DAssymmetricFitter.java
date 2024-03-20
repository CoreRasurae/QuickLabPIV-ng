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
 * Defines a LeastSquares optimizer model and fitter for a 2D Assymmetric Gaussian function
 * @author lpnm
 *
 */
public class Gaussian2DAssymmetricFitter implements IGaussian2DFitter {
	private static Logger logger = LoggerFactory.getLogger(Gaussian2DAssymmetricFitter.class);
	
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
	
	public Gaussian2DAssymmetricFitter(Gaussian2DInterpolatorConfiguration configuration) {
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
			double b = args[5];
			
			double[] functionValues = new double[numberOfPointsInX * numberOfPointsInY];

			double sigmaXSquared = sigmaX * sigmaX;
			double sigmaYSquared = sigmaY * sigmaY;
			double constant = 1.0/(2.0 * FastMath.PI * sigmaX * sigmaY);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					functionValues[y * numberOfPointsInX + x] = a * constant * 
							FastMath.exp(-FastMath.pow((startX + x - meanX), 2)/(2.0 * sigmaXSquared)) *
							FastMath.exp(-FastMath.pow((startY + y - meanY), 2)/(2.0 * sigmaYSquared)) + b;
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
			//double b = args[5];
			
			double[][] jacobianValues = new double[numberOfPointsInX * numberOfPointsInY][6];
			
			double sigmaXSquared = sigmaX * sigmaX;
			double sigmaYSquared = sigmaY * sigmaY;
			double constant = 1.0/(2.0 * FastMath.PI * sigmaX * sigmaY);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					double expX = FastMath.exp(-FastMath.pow((startX + x - meanX), 2)/(2.0 * sigmaXSquared));
					double expY = FastMath.exp(-FastMath.pow((startY + y - meanY), 2)/(2.0 * sigmaYSquared));
							
					int pointIndex = y * numberOfPointsInX + x;
					
					//df(v)/da
					jacobianValues[pointIndex][0] = constant * expX * expY;
					
					//df(v)/dmeanX
					jacobianValues[pointIndex][1] = a * jacobianValues[pointIndex][0] * (startX + x - meanX) / sigmaXSquared;
					
					//df(v)/dmeanY
					jacobianValues[pointIndex][2] = a * jacobianValues[pointIndex][0] * (startY + y - meanY) / sigmaYSquared;
					
					//df(v)/sigmaX
					jacobianValues[pointIndex][3] = jacobianValues[pointIndex][1] * (startX + x - meanX) / sigmaX - 
							a * jacobianValues[pointIndex][0] * 1.0/sigmaX;
					
					//df(v)/sigmaY
					jacobianValues[pointIndex][4] = jacobianValues[pointIndex][2] * (startY + y - meanY) / sigmaY - 
							a * jacobianValues[pointIndex][0] * 1.0/sigmaY;
					
					//df(v)/b
					jacobianValues[pointIndex][5] = 1;
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
        result.setNthPeakI(peakIndex, estimatedPeakY);
        result.setNthPeakJ(peakIndex, estimatedPeakX);
        result.setNthPeakValue(peakIndex, (float)optimalValues[0]);//a - coefficient
		
		if (logResults) {
			logger.trace("a: " + optimalValues[0]);
			logger.trace("meanX: " + optimalValues[1]);
			logger.trace("meanY: " + optimalValues[2]);
			logger.trace("sigmaX: " + optimalValues[3]);
			logger.trace("sigmaY: " + optimalValues[4]);
			logger.trace("b: " + optimalValues[5]);
			
			logger.info("Interpolated Peak index: " + peakIndex + ", X: " + result.getNthPeakJ(peakIndex) + ", Y: " + result.getNthPeakI(peakIndex) + ",Discarded: " + discarded);
		}
		
		return result;
	}

	@Override
	public double[] getStartVector(double[] targetValues, double maxValue) {
		double[] startValues = new double[6];
		
		startValues[0] = maxValue;
		startValues[1] = initialEstimateX;
		startValues[2] = initialEstimateY;
		startValues[3] = 1.0; //TODO Replace by unbiased variance estimator
		startValues[4] = 1.0;
		startValues[5] = 0.0;
		
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
        return params;
	}
}
