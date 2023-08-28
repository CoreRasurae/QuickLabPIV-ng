// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas;

/**
* This interface provides the API for defining/specializing the strategy that comprehend the tiles creation and their placement, also identifying area/sub-area
* relationships.
*/
public interface IInterAreaDivisionStrategy {
	/**
	 * Strategy interface method for determining the interrogation areas tiling for a given iteration step in an Adaptive PIV process.<br/>
	 * This method determines the tile geometry between successive PIV iteration steps based on strategy implementation and current
	 * iteration step tiles.
	 * It also determines the margins along the input image borders, which can exist depending on various factors, at least the selected strategy
	 * configuration, the super-position factor and the current step tile geometry.
	 * @param order the PIV image pair order to which the tiles belong to, either first image or second image 
	 * @param currentStepTiles the current iteration step tiles - state and tiles holder<br/>
	 * @return <ul><li>the IterationStepTiles instance with the tiling for the next iteration step</li>
	 * <li>or, null if no more iteration steps can exist.</li></ul>
	 */
	IterationStepTiles createIterationStepTilesParameters(TilesOrderEnum order, IterationStepTiles currentStepTiles);
}
