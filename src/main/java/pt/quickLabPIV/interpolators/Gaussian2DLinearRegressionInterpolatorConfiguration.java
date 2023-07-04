package pt.quickLabPIV.interpolators;

public class Gaussian2DLinearRegressionInterpolatorConfiguration {
	public final static String IDENTIFIER = "InterpLinRegGaussian2D";
	
	private int interpolationPixels;
	
	public void setInterpolationPixels(int interpolationPixels) {
		if (interpolationPixels % 2 != 1) {
			throw new InterpolatorStateException("Number of interpolation pixels must be an odd number");
		}
		
		this.interpolationPixels = interpolationPixels;
	}
		
	public int getInterpolationPixels() {
		return interpolationPixels;
	}
}
