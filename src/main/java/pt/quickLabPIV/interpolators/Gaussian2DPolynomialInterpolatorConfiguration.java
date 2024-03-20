// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
