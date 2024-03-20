// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public interface IAdaptiveInterVelocityInheritanceLogger {
	public boolean isToBeLogged(IterationStepTiles currentStepTiles, Tile currenTile, Tile parentTile);
	
	public void logTileContribution(IterationStepTiles currentStepTiles, Tile currenTile, Tile parentTile, 
			float weightU, float weightV);

	public void logCompletedForTile(IterationStepTiles currentStepTiles, Tile currentTile);
}
