// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;

public enum MaximumFinderFactoryEnum {
	MaximumFinderSimple,
	MaximumFinderFromCenter,
	MaximumFinderMultiPeaks;
	
	public static IMaximumFinder create() {
		final PIVContextSingleton singleton = PIVContextSingleton.getSingleton();        
        final PIVInputParameters pivParameters = singleton.getPIVParameters();
        final MaximumFinderFactoryEnum strategy = pivParameters.getMaximumFinderStrategy();
		return create(strategy);
	}

	public static IMaximumFinder create(MaximumFinderFactoryEnum strategy) {
		IMaximumFinder finder = null;
		switch (strategy) {
		case MaximumFinderFromCenter:
			finder = new FindMaximumFromCenter();
			break;
		case MaximumFinderMultiPeaks:
			finder = new FindMaximumMultiPeaks();
			break;
		case MaximumFinderSimple:
			finder = new FindMaximumSimple();
			break;
		default:
			throw new MaximumFinderException("Unknown maximum finder strategy");	
		}
		
		return finder;
	}
	
}
