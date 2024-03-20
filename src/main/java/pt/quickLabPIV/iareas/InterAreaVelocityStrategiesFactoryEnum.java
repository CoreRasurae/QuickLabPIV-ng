// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public enum InterAreaVelocityStrategiesFactoryEnum {
	Direct,
	Area,
	Distance,
	BiCubicSpline;
	
	public static IInterAreaVelocityInheritanceStrategy create(PIVInputParameters parameters) {
		switch (parameters.getVelocityInheritanceStrategy()) {
		case Direct:
			return new AdaptiveInterVelocityInheritanceStrategyDirect();
		case Area:
			return new AdaptiveInterVelocityInheritanceStrategyArea();
		case Distance:
			return new AdaptiveInterVelocityInheritanceStrategyDistanceGeneric();
		case BiCubicSpline:
		    return new AdaptiveInterVelocityInheritanceStrategyBiCubicSpline();
		}
		
		return null;
	}

}
