// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;

public class AdaptiveInterVelocityInheritanceStrategyDistanceGeneric implements IInterAreaVelocityInheritanceStrategy {
	protected PIVInputParameters parameters;
	protected IAdaptiveInterVelocityInheritanceLogger logger;
	protected boolean requiresRounding;
		
	public AdaptiveInterVelocityInheritanceStrategyDistanceGeneric() {
		PIVContextSingleton pivContext = PIVContextSingleton.getSingleton();  		
		logger = pivContext.getPIVRunParameters().getVelocityInheritanceLogger();
		requiresRounding = pivContext.getPIVParameters().getWarpingMode().isRequiresRounding();
	}
	
	protected final class DistanceResult {
		protected Tile parentTile;
		protected float distance;
		protected boolean isUp;
		protected boolean isDown;
		protected boolean isLeft;
		protected boolean isRight;
	}
	
	@Override
	public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTiles) {        
		if (currentStepTiles.getCurrentStep() == 0) {
			if (currentStepTiles.getParentStepTiles() != null) {
				throw new InvalidStateException("Parent tiles should be null for base iteration step");
			}
			currentStepTiles.resetDisplacements();
			return;
		}
		
		IterationStepTiles parentStepTiles = currentStepTiles.getParentStepTiles();
		Tile[][] parentTiles = parentStepTiles.getTilesArray();
		Tile[][] currentTiles = currentStepTiles.getTilesArray();

		int parentIndexI = 0;
		int parentIndexJ = 0;
		
		Tile referenceTile;
		Tile currentTile;
		
		DistanceResult contributingAreas[] = new DistanceResult[4];
		for (int i = 0; i < 4; i++) {
			contributingAreas[i] = new DistanceResult();
		}
		
		/*
		 * Start with top-lefter parent tile (reference tile) and top-lefter current step tile (indices [0,0]),
		 * and compute for current tile, then reuse current parent tile until parent tile becomes completely outside 
		 * (distance is greater than half tile size).
		 */
		for (int indexI = 0; indexI < currentStepTiles.getNumberOfTilesInI(); indexI++) {
			parentIndexJ = 0;
			
			referenceTile = parentTiles[parentIndexI][parentIndexJ];
			for (int indexJ = 0; indexJ < currentStepTiles.getNumberOfTilesInJ(); indexJ++) {
				currentTile = currentTiles[indexI][indexJ];
				
				boolean moreAdjustmentsNeeded = true;
				while (moreAdjustmentsNeeded) {
					boolean adjustmentPerformed = false;
					referenceTile = parentTiles[parentIndexI][parentIndexJ];
					DistanceResult referenceAreaDistance = contributingAreas[0];
					referenceAreaDistance = checkDistance(parentStepTiles, referenceTile, currentStepTiles, currentTile, referenceAreaDistance);
					referenceAreaDistance.parentTile = referenceTile;

					referenceAreaDistance = contributingAreas[3];
					if (parentIndexI + 1 < parentStepTiles.getNumberOfTilesInI() && parentIndexJ + 1 < parentStepTiles.getNumberOfTilesInJ()) {
						Tile nextDownRight = parentTiles[parentIndexI+1][parentIndexJ+1];
						referenceAreaDistance = checkDistance(parentStepTiles, nextDownRight, currentStepTiles, currentTile, referenceAreaDistance);
						referenceAreaDistance.parentTile = nextDownRight;
												
						if (!referenceAreaDistance.isRight) {
							parentIndexJ++;
							adjustmentPerformed = true;
						}
						
						if (!referenceAreaDistance.isDown) {
							parentIndexI++;
							adjustmentPerformed = true;
						}
						
						if (referenceAreaDistance.isDown && referenceAreaDistance.isRight) {
							moreAdjustmentsNeeded = false;
						} else {
							continue;
						}
					} else {
						referenceAreaDistance.parentTile = null;
					}
	
					referenceAreaDistance = contributingAreas[1];
					if (parentIndexJ + 1 < parentStepTiles.getNumberOfTilesInJ()) {
						Tile nextRightArea = parentTiles[parentIndexI][parentIndexJ+1];
						referenceAreaDistance = checkDistance(parentStepTiles, nextRightArea, currentStepTiles, currentTile, referenceAreaDistance);
						referenceAreaDistance.parentTile = nextRightArea;
						
						if (!referenceAreaDistance.isRight) {
							if (!moreAdjustmentsNeeded) {
								throw new InvalidStateException("No more adjustments should be needed");
							}
							
							parentIndexJ++;
							adjustmentPerformed = true;
							continue;
						} else {
							moreAdjustmentsNeeded = false;
						}
					} else {
						referenceAreaDistance.parentTile = null;
					}
					
					referenceAreaDistance = contributingAreas[2];
					if (parentIndexI + 1 < parentStepTiles.getNumberOfTilesInI()) {
						Tile nextDownArea =  parentTiles[parentIndexI + 1][parentIndexJ];
						contributingAreas[2] = checkDistance(parentStepTiles, nextDownArea, currentStepTiles, currentTile, contributingAreas[2]);
						referenceAreaDistance.parentTile = nextDownArea;
						
						if (!referenceAreaDistance.isDown) {
							if (!moreAdjustmentsNeeded) {
								throw new InvalidStateException("No more adjustments should be needed");
							}

							adjustmentPerformed = true;
							parentIndexI++;
							continue;
						} else {
							moreAdjustmentsNeeded = false;
						}
					} else {
						referenceAreaDistance.parentTile = null;
					}
					
					if (moreAdjustmentsNeeded && !adjustmentPerformed) {
						//When the lower, right tiles are reached no adjustments can be performed because
						//there no more tiles in range, nor further right, nor further down.
						moreAdjustmentsNeeded = false;
					}
				}
					
				if (contributingAreas[0].isRight) {
					//Initial parent tile is already to the right
					contributingAreas[1].parentTile = null;
					contributingAreas[3].parentTile = null;
				}
				
				if (contributingAreas[0].isDown) {
					//Initial parent tile is already below the current tile
					contributingAreas[2].parentTile = null;
					contributingAreas[3].parentTile = null;
				}

				float totalInvertedDistance = 0.0f;
				int zeroDistanceIndex = -1;
				for (int i = 0; i < 4; i++) {
					if (contributingAreas[i].parentTile != null) {
						if (contributingAreas[i].distance == 0.0f) {
							zeroDistanceIndex = i;
							break;
						}
						
						totalInvertedDistance += 1.0f / contributingAreas[i].distance;
					}
				}
		
				boolean tileIsBeingLogged = false;
				
				float displacementU = 0.0f;
				float displacementV = 0.0f;
				
				if (zeroDistanceIndex < 0) {
					for (int i = 0; i < 4; i++) {
						if (contributingAreas[i].parentTile != null) {
							float weight = (1.0f / contributingAreas[i].distance) / totalInvertedDistance;
							Tile parentTile = contributingAreas[i].parentTile;
							
							if (logger != null && logger.isToBeLogged(currentStepTiles, currentTile, parentTile)) {
								tileIsBeingLogged = true;
								logger.logTileContribution(currentStepTiles, currentTile, parentTile, weight, weight);
							}
							
							displacementU += weight * parentTile.getDisplacementU();
							displacementV += weight * parentTile.getDisplacementV();
						}
					}
				} else {
					Tile parentTile = contributingAreas[zeroDistanceIndex].parentTile;
					
					if (logger != null && logger.isToBeLogged(currentStepTiles, currentTile, parentTile)) {
						tileIsBeingLogged = true;
						logger.logTileContribution(currentStepTiles, currentTile, parentTile, 1.0f, 1.0f);
					}
					
					displacementU = parentTile.getDisplacementU();
					displacementV = parentTile.getDisplacementV();
				}
				
				if (tileIsBeingLogged) {
					logger.logCompletedForTile(currentStepTiles, currentTile);
				}

				if (requiresRounding) {
                   //Non-warping modes require rounding because of adaptive steps, for which the window can only be
				   //displaced by integer values, and could accumulate errors between successive adaptive steps,
				   //due to wrong sub-pixel accumulation.
                   currentTile.accumulateDisplacement(FastMath.round(displacementU), FastMath.round(displacementV));
				} else {
				   //Warping modes on the other hand, do not have to displace any window, instead they absorb all
				   //sub-pixel contributions into the warping process at each step, so the sub-pixel accumulation
				   //is indeed correct.
				   currentTile.accumulateDisplacement(displacementU, displacementV);
				}
			}
		}
	}

	private DistanceResult checkDistance(IterationStepTiles parentStepTiles, Tile referenceTile,
			IterationStepTiles currentStepTiles, Tile currentTile, DistanceResult referenceArea) {
		int parentWidth = parentStepTiles.getTileWidth();
		int parentHeight = parentStepTiles.getTileHeight();
		
		int currentWidth = currentStepTiles.getTileWidth();
		int currentHeight = currentStepTiles.getTileHeight();
		
		/*float centerY = referenceTile.getTopPixel() + (parentHeight - 1) / 2.0f;
		float centerX = referenceTile.getLeftPixel() + (parentWidth - 1) / 2.0f;
		
		float currentCenterY = currentTile.getTopPixel() + (currentHeight - 1) / 2.0f;
		float currentCenterX = currentTile.getLeftPixel() + (currentWidth - 1) / 2.0f;*/
		float centerY = referenceTile.getTopPixel() + parentHeight / 2.0f;
        float centerX = referenceTile.getLeftPixel() + parentWidth / 2.0f;
        
        float currentCenterY = currentTile.getTopPixel() + currentHeight / 2.0f;
        float currentCenterX = currentTile.getLeftPixel() + currentWidth / 2.0f;
		
		if (centerY <= currentCenterY) {
			referenceArea.isUp = true;
			referenceArea.isDown = false;
		} else {
			referenceArea.isUp = false;
			referenceArea.isDown = true;
		}
		
		if (centerX <= currentCenterX) {
			referenceArea.isLeft = true;
			referenceArea.isRight = false;
		} else {
			referenceArea.isLeft = false;
			referenceArea.isRight = true;
		}
				
		float distance = (float)FastMath.sqrt(FastMath.pow(currentCenterX - centerX, 2) + FastMath.pow(currentCenterY - centerY, 2));
		referenceArea.distance = distance;
		
		return referenceArea;
	}

}
