// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
