// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

public enum Gaussian2DSubTypeFactoryEnum {
	Symmetric,
	Assymmetric,
	AssymmetricWithRotation,
	EllipticalWithRotation;

	public static IGaussian2DFitter create(Gaussian2DSubTypeFactoryEnum subType, Gaussian2DInterpolatorConfiguration configuration) {
		IGaussian2DFitter result = null;
		
		switch (subType) {
		case Symmetric:
				result = new Gaussian2DSymmetricFitter(configuration);
				break;
				
		case Assymmetric:
				result = new Gaussian2DAssymmetricFitter(configuration);
				break;
				
		case EllipticalWithRotation:
				result = new Gaussian2DRotatedEllipticalFitter(configuration);
				break;
				
		case AssymmetricWithRotation:
				result = new Gaussian2DRotatedAssymmetricFitter(configuration);
				break;
				
		default:
			throw new IllegalArgumentException();
		}
		
		return result;
	}
	
}
