// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public enum InterAreaDivisionStrategiesFactoryEnum {
	NoSuperPositionStrategy,
	SuperPositionStrategy,
	MixedSuperPositionStrategy;

	public static IInterAreaDivisionStrategy create(PIVInputParameters parameters) {
		switch (parameters.getAreaDivisionStrategy()) {
		case NoSuperPositionStrategy:
			return new AdaptiveInterAreaStrategyNoSuperPosition();
		case SuperPositionStrategy:
			return new AdaptiveInterAreaStrategySuperPosition();
		case MixedSuperPositionStrategy:
			return new AdaptiveInterAreaStrategyMixedSuperPosition();
		}
		
		return null;
	}
	
}
