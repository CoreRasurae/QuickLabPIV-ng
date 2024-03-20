// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import java.util.ArrayList;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;

public class AdaptiveInterVelocityInheritanceStrategyArea
		implements IInterAreaVelocityInheritanceStrategy {
	protected IAdaptiveInterVelocityInheritanceLogger logger;
	protected boolean requiresRounding = false;
	
	final class OverlapResult {
		protected Tile parentTile;
		protected float overlapFactor;
		protected float overlapArea;
		protected boolean moveUp;
		protected boolean moveDown;
		protected boolean moveLeft;
		protected boolean moveRight;
	}
	
	public AdaptiveInterVelocityInheritanceStrategyArea() {
	    PIVContextSingleton pivContext = PIVContextSingleton.getSingleton();
		logger = pivContext.getPIVRunParameters().getVelocityInheritanceLogger();
		requiresRounding = pivContext.getPIVParameters().getWarpingMode().isRequiresRounding();
	}
	
	
	@Override
	public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTiles) {
		if (currentStepTiles.getCurrentStep() == 0) {
			if (currentStepTiles.getParentStepTiles() != null) {
				throw new InvalidStateException("Parent tiles should be null for base iteration step");
			}
			return;
		}
		
		IterationStepTiles parentStepTiles = currentStepTiles.getParentStepTiles();
		Tile[][] parentTiles = parentStepTiles.getTilesArray();
		Tile[][] currentTiles = currentStepTiles.getTilesArray();

		int parentIndexI = 0;
		int parentIndexJ = 0;
		
		ArrayList<OverlapResult> contributingAreas = new ArrayList<OverlapResult>(6);
		int resultIndex = 0;
		for (int index = 0;  index < 6; index++) {
			contributingAreas.add(new OverlapResult());
		}
		
		Tile referenceTile;
		Tile currentTile;
		/*
		 * Start with top-lefter parent tile (reference tile) and top-lefter current step tile (indices [0,0]),
		 * and compute for current tile, then reuse current parent tile until parent tile becomes completely outside (no overlap).
		 * 
		 * Have a inner cycle which tracks adjacent parent tiles that may overlap, stopping when overlap ends (track in 2D).
		 * Taking note of horizontal ending index to use in next horizontal row, until no overlap occurs in I direction. 
		 */
		for (int indexI = 0; indexI < currentStepTiles.getNumberOfTilesInI(); indexI++) {
			parentIndexJ = 0;
			
			referenceTile = parentTiles[parentIndexI][parentIndexJ];
			for (int indexJ = 0; indexJ < currentStepTiles.getNumberOfTilesInJ(); indexJ++) {
				currentTile = currentTiles[indexI][indexJ];
				resultIndex = 0;
				
				OverlapResult referenceArea = contributingAreas.get(resultIndex);
				referenceArea = checkOverlappingFactor(parentStepTiles, referenceTile, currentStepTiles, currentTile, referenceArea);
				if (referenceArea.moveUp || referenceArea.moveLeft) {
					throw new IllegalStateException("Shouldn't have to move up or to move left on top-left parent tile");
				}
				
				if (referenceArea.moveRight && referenceArea.moveLeft || referenceArea.moveUp && referenceArea.moveDown) {
					throw new IllegalStateException("Inconsistent move hint for parent tile");
				}
								
				if (referenceArea.moveRight) {
					if (referenceArea.moveDown) {
						throw new IllegalStateException("Shouldn't have to move down when moving right on top-left parent tile");
					}
					
					if (parentIndexJ + 1 == parentStepTiles.getNumberOfTilesInJ()) {
						throw new IllegalStateException("No more tiles to the right, cannot advance reference tile");
					}
					
					parentIndexJ++;
					referenceTile = parentTiles[parentIndexI][parentIndexJ];
				}
				
				if (referenceArea.moveDown) {
					if (parentIndexI + 1 == parentStepTiles.getNumberOfTilesInI()) {
						throw new IllegalStateException("No more tiles below, cannot advance reference tile");
					}
					
					parentIndexI++;
					referenceTile = parentTiles[parentIndexI][parentIndexJ];
				}	
				
				float accumulatedArea = 0.0f;
				int offsetI = 0;
				int offsetJ = 0;
				int maxTilesInJ = 0;
				boolean endCycles = false;
				do {
					Tile parentTile = parentTiles[parentIndexI + offsetI][parentIndexJ + offsetJ];
					OverlapResult contributingArea = contributingAreas.get(resultIndex);
					contributingArea = checkOverlappingFactor(parentStepTiles, parentTile, currentStepTiles, currentTile, contributingArea);
					contributingArea.parentTile = parentTile;
					if (contributingArea.overlapFactor == 0) {
						if (contributingArea.moveRight && contributingArea.moveLeft || contributingArea.moveUp && contributingArea.moveDown) {
							throw new IllegalStateException("Inconsistent move hint for parent tile");
						}

						if (contributingArea.moveRight) {
							throw new IllegalStateException("Shouldn't have to move to right parent tile");
						}
						
						if (contributingArea.moveDown) {
							throw new IllegalStateException("Shouldn't have to move down when moving right on top-left parent tile");
						}

						if (contributingArea.moveLeft) {
							maxTilesInJ = offsetJ;
							offsetJ = 0;
							offsetI++;
							
							if (parentIndexI + offsetI == parentStepTiles.getNumberOfTilesInI()) {
								endCycles = true;
							}
						} else if (contributingArea.moveUp) {
							if (offsetJ != 0) {
								throw new IllegalStateException("Inconsistent move up hint, at middle of row");	
							}
							endCycles = true;
						}
					} else {
						accumulatedArea += contributingArea.overlapArea;
						resultIndex++;
						if (resultIndex == contributingAreas.size()) {
							contributingAreas.add(new OverlapResult());
						}
						
						offsetJ++;
						
						boolean incrementI = false;
						if (parentIndexJ + offsetJ == parentStepTiles.getNumberOfTilesInJ()) {
							incrementI = true;
						}
						
						if (maxTilesInJ > 0 && offsetJ == maxTilesInJ) {
							incrementI = true;
						}				
						
						if (incrementI) {
							offsetI++;
							offsetJ = 0;
							
							if (parentIndexI + offsetI == parentStepTiles.getNumberOfTilesInI()) {
								endCycles = true;
							}
						}
					}
				} while (!endCycles);
				
				//Import weighted displacement vector...
				float displacementU = 0.0f;
				float displacementV = 0.0f;
				boolean tileIsBeingLogged = false;
				for (int index = 0; index < resultIndex; index++) {
					OverlapResult contributingArea = contributingAreas.get(index);	
					float weight = contributingArea.overlapArea / accumulatedArea;
					Tile parentTile = contributingArea.parentTile;
					
					if (logger != null && logger.isToBeLogged(currentStepTiles, currentTile, parentTile)) {
						tileIsBeingLogged = true;
						logger.logTileContribution(currentStepTiles, currentTile, parentTile, weight, weight);
					}
					
					displacementU += weight * parentTile.getDisplacementU();
					displacementV += weight * parentTile.getDisplacementV();
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

	/**
	 * Computes the area and percentage that overlaps parent adaptive step tile with current adaptive step (child step) 
	 * @param parentStepTiles the parent adaptive step tile bean holding the interrogation areas geometry and organization
	 * @param parentTile the parent tile to check for overlap
	 * @param currentStepTiles the current adaptive step tile bean holding the interrogation areas geometry and organization
	 * @param currentTile the current tile to check for overlap
	 * @param result the OverlapResult instance to be reused for retrieving the result
	 * @return the reused OverlapResult instance with overlap results
	 */
	static OverlapResult checkOverlappingFactor(IterationStepTiles parentStepTiles, Tile parentTile, 
			IterationStepTiles currentStepTiles, Tile currentTile, OverlapResult result) {
		float overlapFactor;
		float overlapArea;
		
		int parentWidth = parentStepTiles.getTileWidth();
		int parentHeight = parentStepTiles.getTileHeight();
		
		int width = currentStepTiles.getTileWidth();
		int height = currentStepTiles.getTileHeight();
		
		boolean moveDown = false;
		boolean moveUp = false;
		boolean moveRight = false;
		boolean moveLeft = false;
		
		int verticalOverlapPixels = 0;
		int horizontalOverlapPixels = 0;
		if (parentTile.getTopPixel() <= currentTile.getTopPixel()) {
			//currentTile is at same level or below of parentTile
			if (parentTile.getTopPixel() + parentHeight > currentTile.getTopPixel()) {
				//currentTile is totally or partially contained within vertical direction
				int endVerticalPixel = currentTile.getTopPixel() + height;// - 1; Should subtract one pixel, but it will be compensated by subtraction below
				if (endVerticalPixel - 1 >= parentTile.getTopPixel() + parentHeight) {
					//At least one pixel in vertical direction is outside the parentTile
					endVerticalPixel = parentTile.getTopPixel() + parentHeight; // - 1; Should subtract one pixel, but it will be compensated by subtraction below
				}
				verticalOverlapPixels = endVerticalPixel - currentTile.getTopPixel();
			} else {
				//currentTile is not contained vertically 
				moveDown = true;
			}
		} else {
			//currentTile is above parentTile, but may overlap partially
			if (currentTile.getTopPixel() + height > parentTile.getTopPixel()) { //It must be > only, not >=, because topPixel + height is the top pixel of the tile below 
				//currentTile is above parentTile, still partially contained by parentTile
				verticalOverlapPixels = currentTile.getTopPixel() + height - parentTile.getTopPixel();
			} else {
				//CurrentTile is completely above parentTile, thus no overlap
				moveUp = true;
			}
		}
		
		if (parentTile.getLeftPixel() <= currentTile.getLeftPixel()) {
			//currentTile is at same level or to the right of parentTile
			if (parentTile.getLeftPixel() + parentWidth > currentTile.getLeftPixel()) { //It must be > only, not >=, because leftPixel + width is the left pixel of the next tile
				//currentTile is totally or partially contained within horizontal direction
				int endHorizontalPixel = currentTile.getLeftPixel() + width;// - 1; Should subtract one pixel, but it will be compensated by subtraction below
				if (endHorizontalPixel - 1 >= parentTile.getLeftPixel() + parentWidth) {
					//At least one pixel in horizontal direction is outside the parentTile
					endHorizontalPixel = parentTile.getLeftPixel() + parentWidth; // -1; Should subtract one pixel, but it will be compensated by subtraction below
				}
				horizontalOverlapPixels = endHorizontalPixel - currentTile.getLeftPixel();
			} else {
				//currentTile is horizontally completely to the right of the parent tile
				moveRight = true;
			}
		} else {
			//currentTile is to the left of parentTile, but may overlap partially
			if (currentTile.getLeftPixel() + width > parentTile.getLeftPixel()) { //It must be > only, not >=, because leftPixel + width is the left pixel of the next tile
				//currentTile is to the left of parentTile, still partially contained by parentTile
				horizontalOverlapPixels = currentTile.getLeftPixel() + width - parentTile.getLeftPixel();
			} else {
				//currentTile is completely to the left of parentTile, thus no overlap
				moveLeft = true;
			}
		}
		
		overlapArea = (verticalOverlapPixels * horizontalOverlapPixels);
		overlapFactor = overlapArea / (float)(width * height);
		
		result.moveUp = moveUp;
		result.moveDown = moveDown;
		result.moveRight = moveRight;
		result.moveLeft = moveLeft;
		result.overlapArea = overlapArea;
		result.overlapFactor = overlapFactor;
		
		return result;
	}

}
