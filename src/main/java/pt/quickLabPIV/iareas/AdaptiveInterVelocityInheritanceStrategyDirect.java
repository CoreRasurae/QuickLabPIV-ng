// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;

public class AdaptiveInterVelocityInheritanceStrategyDirect
		implements IInterAreaVelocityInheritanceStrategy {
	protected IAdaptiveInterVelocityInheritanceLogger logger;
	protected boolean requiresRounding;
	
	public AdaptiveInterVelocityInheritanceStrategyDirect() {
	    PIVContextSingleton pivContext = PIVContextSingleton.getSingleton();
		logger = pivContext.getPIVRunParameters().getVelocityInheritanceLogger();
		requiresRounding = pivContext.getPIVParameters().getWarpingMode().isRequiresRounding();
	}
	

	@Override
	public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTilesParams) {
		int nextIterationStep = currentStepTilesParams.getCurrentStep();
		
		if (nextIterationStep >= currentStepTilesParams.getMaxAdaptiveSteps()) {
			throw new IterationStepTilesParametersException(
					"Next iteration step to be reused has an iteration step that is outside the adaptive range parameters");
		}

		currentStepTilesParams.resetDisplacements();
		
		//Inherit displacements from corresponding parent tile displacements
		IterationStepTiles parentStepTilesParams = currentStepTilesParams.getParentStepTiles();
		if (parentStepTilesParams != null) {
			Tile[][] parentTiles = parentStepTilesParams.getTilesArray();
			Tile[][] currentTiles = currentStepTilesParams.getTilesArray();
			
			for (short tileIndexI = 0; tileIndexI < parentStepTilesParams.getNumberOfTilesInI(); tileIndexI++) {
				for (short tileIndexJ = 0; tileIndexJ < parentStepTilesParams.getNumberOfTilesInJ(); tileIndexJ++) {
					Tile parentStepTile = parentTiles[tileIndexI][tileIndexJ];
					
					Tile tile = currentTiles[tileIndexI*2][tileIndexJ*2];
					if (logger != null && logger.isToBeLogged(currentStepTilesParams, tile, parentStepTile)) {
						logger.logTileContribution(currentStepTilesParams, tile, parentStepTile, 1.0f, 1.0f);
						logger.logCompletedForTile(currentStepTilesParams, tile);
					}
					
					if (requiresRounding) {
					   tile.accumulateDisplacement(FastMath.round(parentStepTile.getDisplacementU()), FastMath.round(parentStepTile.getDisplacementV()));
					} else {
					   tile.accumulateDisplacement(parentStepTile.getDisplacementU(), parentStepTile.getDisplacementV());
					}
					
					tile = currentTiles[tileIndexI*2][tileIndexJ*2 + 1];
					if (logger != null && logger.isToBeLogged(currentStepTilesParams, tile, parentStepTile)) {
						logger.logTileContribution(currentStepTilesParams, tile, parentStepTile, 1.0f, 1.0f);
						logger.logCompletedForTile(currentStepTilesParams, tile);
					}
                    if (requiresRounding) {
                        tile.accumulateDisplacement(FastMath.round(parentStepTile.getDisplacementU()), FastMath.round(parentStepTile.getDisplacementV()));
                     } else {
                        tile.accumulateDisplacement(parentStepTile.getDisplacementU(), parentStepTile.getDisplacementV());
                     }

					tile = currentTiles[tileIndexI*2 + 1][tileIndexJ*2];
					if (logger != null && logger.isToBeLogged(currentStepTilesParams, tile, parentStepTile)) {
						logger.logTileContribution(currentStepTilesParams, tile, parentStepTile, 1.0f, 1.0f);
						logger.logCompletedForTile(currentStepTilesParams, tile);
					}
                    if (requiresRounding) {
                        tile.accumulateDisplacement(FastMath.round(parentStepTile.getDisplacementU()), FastMath.round(parentStepTile.getDisplacementV()));
                     } else {
                        tile.accumulateDisplacement(parentStepTile.getDisplacementU(), parentStepTile.getDisplacementV());
                     }

					tile = currentTiles[tileIndexI*2 + 1][tileIndexJ*2 + 1];;
					if (logger != null && logger.isToBeLogged(currentStepTilesParams, tile, parentStepTile)) {
						logger.logTileContribution(currentStepTilesParams, tile, parentStepTile, 1.0f, 1.0f);
						logger.logCompletedForTile(currentStepTilesParams, tile);
					}
                    if (requiresRounding) {
                        tile.accumulateDisplacement(FastMath.round(parentStepTile.getDisplacementU()), FastMath.round(parentStepTile.getDisplacementV()));
                     } else {
                        tile.accumulateDisplacement(parentStepTile.getDisplacementU(), parentStepTile.getDisplacementV());
                     }
				}
			}			
		}
	}
}
