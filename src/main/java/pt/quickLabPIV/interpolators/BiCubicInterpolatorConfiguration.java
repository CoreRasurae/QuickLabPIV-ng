// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
