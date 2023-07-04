package pt.quickLabPIV.interpolators;

public class BiCubicInterpolatorConfiguration {
	public final static String IDENTIFIER = "InterpBiCubic";
	
	private int interpolationSteps;
	private int interpolationPixels;
	
	public void setProperties(int interpolationSteps, int interpolationPixels) {
		if (interpolationPixels % 2 != 1) {
			throw new InterpolatorStateException("Number of interpolation pixels be an odd number");
		}
		
		if (interpolationSteps < 10 || interpolationSteps > 1000) {
			throw new InterpolatorStateException("Invalid number of interpolation steps specified. Must be between 10 and 100");
		}
		
		this.interpolationSteps = interpolationSteps;
		this.interpolationPixels = interpolationPixels;
	}
	
	public int getInterpolationSteps() {
		return interpolationSteps;
	}
	
	public int getInterpolationPixels() {
		return interpolationPixels;
	}
}
