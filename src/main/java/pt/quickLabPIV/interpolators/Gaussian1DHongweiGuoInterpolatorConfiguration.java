package pt.quickLabPIV.interpolators;

public class Gaussian1DHongweiGuoInterpolatorConfiguration {
	public final static String IDENTIFIER = "InterpGaussian1DHongweiGuo";
	
	private int interpolationPixels;
	private int iterations;
	
	public void setInterpolationPixels(int _interpolationPixels) {
		if (_interpolationPixels % 2 != 1) {
			throw new InterpolatorStateException("Number of interpolation pixels be an odd number");
		}
		
		interpolationPixels = _interpolationPixels;
	}
		
	public int getInterpolationPixels() {
		return interpolationPixels;
	}
	
	public void setInteporlationIterations(int _iterations) {
	    if (_iterations < 6 || _iterations > 50) {
	        throw new InterpolatorStateException("Invalid number of interpolations: should be >= 6 and <= 50");
	    }
	    
	    iterations = _iterations;
	}
	
	public int getIterations() {
	    return iterations;
	}
}
