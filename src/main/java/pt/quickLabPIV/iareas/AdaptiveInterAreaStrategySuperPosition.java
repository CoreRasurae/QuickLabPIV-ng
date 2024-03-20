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

public class AdaptiveInterAreaStrategySuperPosition implements IInterAreaDivisionStrategy {
	private PIVInputParameters parameters;
	private int maxSteps;
	private int computedMarginTop;
	private int computedMarginLeft;
	private int computedMarginBottom;
	private int computedMarginRight;

	public AdaptiveInterAreaStrategySuperPosition() {
		this.parameters = PIVContextSingleton.getSingleton().getPIVParameters();
	}
	
	@Override
	public IterationStepTiles createIterationStepTilesParameters(TilesOrderEnum order, IterationStepTiles currentStepTilesParams) {
		return createIterationStepTilesParametersInternal(order, currentStepTilesParams, 0);
	}
		
	IterationStepTiles createIterationStepTilesParametersInternal(TilesOrderEnum order, IterationStepTiles currentStepTilesParams, int iterationStepOffset) {	
		int height = parameters.getImageHeightPixels();
		int width = parameters.getImageWidthPixels();
		
		int marginITop = parameters.getMarginPixelsITop();
		int marginIBottom = parameters.getMarginPixelsIBottom();
		int marginJLeft = parameters.getMarginPixelsJLeft();
		int marginJRight = parameters.getMarginPixelsJRight();
		float overlapFactor = parameters.getOverlapFactor();

		if (overlapFactor < 1/10f || overlapFactor > 1 - 1/10f) {
			throw new IterationStepTilesParametersException("Overlap factor must greater than 1/10 and less than 9/10");
		}
		
		int nextIterationStep = 0;
		
		IterationStepTiles nextIterationStepTilesParams = null;
		
		if (currentStepTilesParams != null) {
			//Create second and subsequent adaptive step parameters...
			nextIterationStep = currentStepTilesParams.getCurrentStep() + 1;

	        if (order != currentStepTilesParams.getTilesOrder()) {
                throw new IterationStepTilesParametersException("Inconsistent tiles order specified");
            }
			
			if (maxSteps != currentStepTilesParams.getMaxAdaptiveSteps())  {
				throw new IterationStepTilesParametersException("Inconsistent number of adaptive steps, expected steps: " + maxSteps + 
						" iterationStepTiles steps: " + currentStepTilesParams.getMaxAdaptiveSteps());
			}
			
			if (computedMarginTop != currentStepTilesParams.getMarginTop()) {
				throw new IterationStepTilesParametersException("Inconsistent top margin, expected: " + computedMarginTop + 
						" iterationStepTiles top margin: " + currentStepTilesParams.getMarginTop());
			}
			
			if (nextIterationStep >= maxSteps) {
				return null;
			}
					
			int newTileWidth = currentStepTilesParams.getTileWidth() / 2;
			int newTileHeight = currentStepTilesParams.getTileHeight() / 2;

			float heightIncrement = overlapFactor * newTileHeight;
			float widthIncrement = overlapFactor * newTileWidth;

			//With overlapping and interrogation area size reduction at the next adaptive step there maybe room for more overlapped tiles...
			//even with margins are kept the same. The last row or column of tiles may leave enough room for more tiles.
			int numberOfTilesInJ = (int)(0.01f+((float)(width - (marginJLeft + marginJRight)) - (newTileWidth  - widthIncrement)) / widthIncrement);
			int numberOfTilesInI = (int)(0.01f+((float)(height - (marginITop + marginIBottom)) - (newTileHeight  - heightIncrement)) / heightIncrement);

			//(width - (marginJLeft - marginJRight)) = available pixels
			//(tileWidth - widthIncrement) = subtract excess pixels of last overlapping window
			int remainingPixelsI = height - (int)FastMath.ceil((numberOfTilesInI - 1) * heightIncrement + newTileHeight) - (marginITop + marginIBottom);
			int remainingPixelsJ = width - (int)FastMath.ceil((numberOfTilesInJ - 1) * widthIncrement + newTileWidth) - (marginJLeft + marginJRight);
			
			computedMarginTop = marginITop + FastMath.floorDiv(remainingPixelsI, 2);
			computedMarginBottom = marginIBottom + (int)FastMath.ceil(remainingPixelsI / 2.0f);
			computedMarginLeft = marginJLeft + FastMath.floorDiv(remainingPixelsJ, 2);
			computedMarginRight = marginJRight + (int)FastMath.ceil(remainingPixelsJ / 2.0f);

						
			nextIterationStepTilesParams = new IterationStepTiles(this, 
					InterAreaStableStrategiesFactoryEnum.create(parameters),
					InterAreaVelocityStrategiesFactoryEnum.create(parameters),
					order,
					nextIterationStep, currentStepTilesParams.getMaxAdaptiveSteps(),
					(short)newTileWidth, (short)newTileHeight, 
					(short)numberOfTilesInI, (short)numberOfTilesInJ,
					(short)computedMarginTop, (short)computedMarginLeft,
					(short)computedMarginBottom, (short)computedMarginRight);

			nextIterationStepTilesParams.setParentStepTiles(currentStepTilesParams);
			
			Tile[][] nextIterationTiles = nextIterationStepTilesParams.getTilesArray();
			
			float topPixel = computedMarginTop;
			float leftPixel = computedMarginLeft; 
			for (short tileIndexI = 0; tileIndexI < numberOfTilesInI; tileIndexI++, topPixel += heightIncrement, leftPixel = computedMarginLeft) {
				for (short tileIndexJ = 0; tileIndexJ < numberOfTilesInJ; tileIndexJ++, leftPixel += widthIncrement) {	
					Tile tile = nextIterationTiles[tileIndexI][tileIndexJ];
					tile.setTileIndexI(tileIndexI);
					tile.setTileIndexJ(tileIndexJ);
					tile.setTopPixel((short)(FastMath.round(topPixel)));
					tile.setLeftPixel((short)(FastMath.round(leftPixel)));
				}
			}
		} else {
			//Create first adaptive step tiles
			int totalNumberOfStepsI = FastMath.floorDiv(parameters.getInterrogationAreaStartIPixels(), parameters.getInterrogationAreaEndIPixels());
			if (FastMath.floorMod(parameters.getInterrogationAreaStartIPixels(), parameters.getInterrogationAreaEndIPixels()) != 0) {
				throw new IterationStepTilesParametersException(
						"Interrogation Area start pixels is not a multiple of Interrogation Area end pixels (Height wise)");
			}
	
			int totalNumberOfStepsJ = FastMath.floorDiv(parameters.getInterrogationAreaStartJPixels(), parameters.getInterrogationAreaEndJPixels());
			if (FastMath.floorMod(parameters.getInterrogationAreaStartJPixels(), parameters.getInterrogationAreaEndJPixels()) != 0) {
				throw new IterationStepTilesParametersException(
						"Interrogation Area start pixels is not a multiple of Interrogation Area end pixels (Width wise)");
			}
			
			if (totalNumberOfStepsI != totalNumberOfStepsJ) {
				throw new IterationStepTilesParametersException(
						"Total number of steps computed from width is different from the number of steps computed from height.");
			}
	
			int totalNumberOfSteps = (int)FastMath.floor(FastMath.log(2, totalNumberOfStepsI));
			if (FastMath.pow(2, totalNumberOfSteps) != totalNumberOfStepsI) {
				throw new IterationStepTilesParametersException(
						"Total number of steps could not be properly computed since step sizes ratio is not a power of two");
			}
			
			maxSteps = totalNumberOfSteps + 1; //Account for the initial step iteration

			if (iterationStepOffset >= maxSteps) {
				//Typically this doesn't happen, however if this code is called from mixed super position strategy, it can happen that
				//the initial super position step occurs exactly at the end step, executing one extra adaptive step that wasn't requested.
				return null;
			}

			
			int tileHeight = parameters.getInterrogationAreaStartIPixels() / (1 << (nextIterationStep + iterationStepOffset));
			int tileWidth = parameters.getInterrogationAreaStartJPixels() / (1 << (nextIterationStep + iterationStepOffset));

			
			float heightIncrement = overlapFactor * tileHeight;
			float widthIncrement = overlapFactor * tileWidth;
			
			//With overlapping and interrogation area size reduction at the next adaptive step there maybe room for more overlapped tiles...
			//even with margins are kept the same. The last row or column of tiles may leave enough room for more tiles.
			int numberOfTilesInJ = (int)(0.01f+((float)(width - (marginJLeft + marginJRight)) - (tileWidth  - widthIncrement)) / widthIncrement);
			int numberOfTilesInI = (int)(0.01f+((float)(height - (marginITop + marginIBottom)) - (tileHeight  - heightIncrement)) / heightIncrement);

			//(width - (marginJLeft - marginJRight)) = available pixels
			//(tileWidth - widthIncrement) = subtract excess pixels of last overlapping window
			int remainingPixelsI = height - (int)FastMath.ceil((numberOfTilesInI - 1) * heightIncrement + tileHeight) - (marginITop + marginIBottom);
			int remainingPixelsJ = width - (int)FastMath.ceil((numberOfTilesInJ - 1) * widthIncrement + tileWidth) - (marginJLeft + marginJRight);
			
			computedMarginTop = marginITop + FastMath.floorDiv(remainingPixelsI, 2);
			computedMarginBottom = marginIBottom + (int)FastMath.ceil(remainingPixelsI / 2.0f);
			computedMarginLeft = marginJLeft + FastMath.floorDiv(remainingPixelsJ, 2);
			computedMarginRight = marginJRight + (int)FastMath.ceil(remainingPixelsJ / 2.0f);
					
			nextIterationStepTilesParams = new IterationStepTiles(this, 
					InterAreaStableStrategiesFactoryEnum.create(parameters),
					InterAreaVelocityStrategiesFactoryEnum.create(parameters),
					order,
					nextIterationStep + iterationStepOffset, maxSteps,
					(short)tileWidth, (short)tileHeight, (short)numberOfTilesInI, (short)numberOfTilesInJ,
					(short)computedMarginTop, (short)computedMarginLeft, (short)computedMarginBottom, (short)computedMarginRight);
			
			Tile[][] tiles = nextIterationStepTilesParams.getTilesArray();
			for (int tileIndexI = 0; tileIndexI < numberOfTilesInI; tileIndexI++) {
				for (int tileIndexJ = 0; tileIndexJ < numberOfTilesInJ; tileIndexJ++) {
					short leftPixel = (short)(computedMarginLeft + FastMath.round(tileIndexJ * widthIncrement));
					short topPixel = (short)(computedMarginTop + FastMath.round(tileIndexI * heightIncrement));
					Tile tile = tiles[tileIndexI][tileIndexJ];
					tile.setTileIndexI((short)tileIndexI);
					tile.setTileIndexJ((short)tileIndexJ);
					tile.setLeftPixel(leftPixel);
					tile.setTopPixel(topPixel);
				}
			}
		}
		
		return nextIterationStepTilesParams;
	}
}
