package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.maximum.MaxCrossResult;

/**
 * Defines a LeastSquares optimizer model and fitter for a 2D Symmetric Gaussian function
 * 
 * @author lpnm
 */
public class Gaussian2DSymmetricFitter implements IGaussian2DFitter {
	private static Logger logger = LoggerFactory.getLogger(Gaussian2DSymmetricFitter.class);
	
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
	
	public Gaussian2DSymmetricFitter(Gaussian2DInterpolatorConfiguration configuration) {
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
			double sigma = args[3];
			double b = args[4];
			
			double[] functionValues = new double[numberOfPointsInX * numberOfPointsInY];

			double sigmaSquared = sigma * sigma;
			double constant = 1.0/(2.0 * FastMath.PI * sigmaSquared);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					functionValues[y * numberOfPointsInX + x] = a * constant * 
							FastMath.exp((-FastMath.pow((startX + x - meanX), 2)- FastMath.pow((startY + y - meanY), 2))/(2.0 * sigmaSquared)) + b;
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
			double sigma = args[3];
			//double b = args[4];
			
			double[][] jacobianValues = new double[numberOfPointsInX * numberOfPointsInY][5];
			
			double sigmaSquared = sigma * sigma;
			double constant = 1.0/(2.0 * FastMath.PI * sigmaSquared);
			
			for (int y = 0; y < numberOfPointsInY; y++) {
				for (int x = 0; x < numberOfPointsInX; x++) {
					double expXY = FastMath.exp((-FastMath.pow((startX + x - meanX), 2)-FastMath.pow((startY + y - meanY), 2))/(2.0 * sigmaSquared));
							
					int pointIndex = y * numberOfPointsInX + x;
					
					//df(v)/da
					jacobianValues[pointIndex][0] = constant * expXY;
					
					//df(v)/dmeanX
					jacobianValues[pointIndex][1] = a * jacobianValues[pointIndex][0] * (startX + x - meanX) / sigmaSquared;
					
					//df(v)/dmeanY
					jacobianValues[pointIndex][2] = a * jacobianValues[pointIndex][0] * (startY + y - meanY) / sigmaSquared;
					
					//df(v)/sigma
					jacobianValues[pointIndex][3] = jacobianValues[pointIndex][1] * (startX + x - meanX) / sigma + 
							jacobianValues[pointIndex][2] * (startY + y - meanY) / sigma - 
							a * jacobianValues[pointIndex][0] * 2.0/sigma;
										
					//df(v)/b
					jacobianValues[pointIndex][4] = 1;
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
        float estimatedPeakX = (float)optimalValues[1]; //meanX
        float estimatedPeakY = (float)optimalValues[2]; //meanY

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
			logger.trace("sigma: " + optimalValues[3]);
			logger.trace("b: " + optimalValues[4]);

			logger.info("Interpolated peak index: " + peakIndex+ ", X: " + result.getMainPeakJ() + ", Y: " + result.getMainPeakI() + ",Discarded: " + discarded);
		}
		
		return result;
	}

	@Override
	public double[] getStartVector(double[] targetValues, double maxValue) {
		double[] startValues = new double[5];
		
		startValues[0] = maxValue;
        startValues[1] = initialEstimateX;
        startValues[2] = initialEstimateY;
        startValues[3] = 1.0f;
        startValues[4] = 0.0;
        
		return startValues;
	}

	@Override
	public RealVector validate(RealVector params) {
	    if (params.getEntry(1) > startX + numberOfPointsInX/2 + 1 || params.getEntry(1) < startX + numberOfPointsInX/2 - 1 ||
	            params.getEntry(2) > startY + numberOfPointsInY/2 + 1 || params.getEntry(2) < startY + numberOfPointsInY/2 - 1) {
	        params.setEntry(0, maxValue);
	        params.setEntry(1, startX + numberOfPointsInX/2);
	        params.setEntry(2, startY + numberOfPointsInY/2);
	    };
		return params;
	}
}
