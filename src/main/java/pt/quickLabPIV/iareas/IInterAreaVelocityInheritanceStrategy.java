// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public interface IInterAreaVelocityInheritanceStrategy {
	/**
	 * Resets the state for the iteration step tiles for instance re-use purposes where the base PIV Parameters remains unchanged. 
	 * @param currentStepTiles the iteration step tiles instance to be reset for reuse
	 */
	public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTiles);
}
