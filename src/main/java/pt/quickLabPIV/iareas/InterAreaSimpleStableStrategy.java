// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public class InterAreaSimpleStableStrategy implements IInterAreaStableStrategy {

	@Override
	public TileStableStateEnum computeStableState(IterationStepTiles iterTiles, Tile tile, float displacementDeltaU,
			float displacementDeltaV) {
		if (iterTiles.getCurrentStepRetries() == 0) {
			return TileStableStateEnum.EVALUATING;
		} else {
			return TileStableStateEnum.STABLE;
		}
	}

}
