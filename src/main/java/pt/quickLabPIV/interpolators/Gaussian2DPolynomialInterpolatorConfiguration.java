package pt.quickLabPIV.interpolators;

public class Gaussian2DPolynomialInterpolatorConfiguration {
	public final static String IDENTIFIER = "InterpGaussian2DPoly";
	
	private int interpolationPixelsForCentroid2D;
	
	public void setInterpolationPixelsForCentroid2D(int interpolationPixels) {
		if (interpolationPixels % 2 != 1) {
			throw new InterpolatorStateException("Number of interpolation pixels must be an odd number");
		}
		
		this.interpolationPixelsForCentroid2D = interpolationPixels;
	}
		
	public int getInterpolationPixelsForCentroid2D() {
		return interpolationPixelsForCentroid2D;
	}
}
