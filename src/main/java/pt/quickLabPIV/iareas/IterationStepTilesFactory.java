// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public class IterationStepTilesFactory {
	public static IterationStepTiles create(TilesOrderEnum order, PIVInputParameters parameters) {
		IInterAreaDivisionStrategy areaDivisionStrategy = InterAreaDivisionStrategiesFactoryEnum.create(parameters);

		IterationStepTiles stepTiles = areaDivisionStrategy.createIterationStepTilesParameters(order, null);
		
		return stepTiles;
	}
}
