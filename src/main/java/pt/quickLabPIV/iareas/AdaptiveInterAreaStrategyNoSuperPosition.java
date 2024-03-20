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

public class AdaptiveInterAreaStrategyNoSuperPosition implements IInterAreaDivisionStrategy {
	private final PIVInputParameters parameters;

	private int marginLeft;
	private int marginTop;
	private int marginRight;
	private int marginBottom;
	private int maxSteps;
	
	public AdaptiveInterAreaStrategyNoSuperPosition() {
		parameters = PIVContextSingleton.getSingleton().getPIVParameters();
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
		
		int nextIterationStep = 0;
		
		IterationStepTiles nextIterationStepTilesParams = null;
		
		if (currentStepTilesParams != null) {
			nextIterationStep = currentStepTilesParams.getCurrentStep() + 1;
			
			if (order != currentStepTilesParams.getTilesOrder()) {
			    throw new IterationStepTilesParametersException("Inconsistent tiles order specified");
			}
			
			if (maxSteps != currentStepTilesParams.getMaxAdaptiveSteps())  {
				throw new IterationStepTilesParametersException("Inconsistent number of adaptive steps, expected steps: " + maxSteps + 
						" iterationStepTiles steps: " + currentStepTilesParams.getMaxAdaptiveSteps());
			}
			
			if (nextIterationStep >= maxSteps) {
				return null;
			}
			
			int tileWidth = currentStepTilesParams.getTileWidth() / 2;
			int tileHeight = currentStepTilesParams.getTileHeight() / 2;
			
			int numberOfTilesInI = FastMath.floorDiv(height-(marginITop + marginIBottom), tileHeight);
			int numberOfTilesInJ = FastMath.floorDiv(width-(marginJLeft + marginJRight), tileWidth);
			
			int marginVerticalLeftOverPixels = height - marginITop - marginIBottom - tileHeight*numberOfTilesInI;
			int marginHorizontalLeftOverPixels = width - marginJLeft - marginJRight - tileWidth*numberOfTilesInJ;
			marginTop = marginITop + marginVerticalLeftOverPixels / 2;
			marginBottom = height - tileHeight*numberOfTilesInI - marginTop;
			marginLeft = marginJLeft + marginHorizontalLeftOverPixels / 2;
			marginRight = width - tileWidth*numberOfTilesInJ - marginLeft;
			
			nextIterationStepTilesParams = new IterationStepTiles(this,
					InterAreaStableStrategiesFactoryEnum.create(parameters),
					InterAreaVelocityStrategiesFactoryEnum.create(parameters),
					currentStepTilesParams.getTilesOrder(),
					nextIterationStep, currentStepTilesParams.getMaxAdaptiveSteps(),
					(short)tileWidth, (short)tileHeight, 
					(short)numberOfTilesInI, (short)numberOfTilesInJ,
					(short)marginTop, (short)marginLeft, (short)marginBottom, (short)marginRight);

			nextIterationStepTilesParams.setParentStepTiles(currentStepTilesParams);
			
			Tile[][] nextIterationTiles = nextIterationStepTilesParams.getTilesArray();
			
			for (int tileIndexI = 0, pixelTop = marginTop; tileIndexI < numberOfTilesInI; tileIndexI++, pixelTop += tileHeight) {
				for (int tileIndexJ = 0, pixelLeft = marginLeft; tileIndexJ < numberOfTilesInJ; tileIndexJ++, pixelLeft += tileWidth) {			
					Tile tile = nextIterationTiles[tileIndexI][tileIndexJ];
					tile.setTileIndexI((short)(tileIndexI));
					tile.setTileIndexJ((short)(tileIndexJ));
					tile.setTopPixel((short)pixelTop);
					tile.setLeftPixel((short)pixelLeft);
				}
			}
		} else {
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
			
			int tileWidth = parameters.getInterrogationAreaStartIPixels() / (1 << nextIterationStep);
			int tileHeight = parameters.getInterrogationAreaStartJPixels() / (1 << nextIterationStep);
			
			int numberOfTilesInI = FastMath.floorDiv(height-(marginITop + marginIBottom), tileHeight);
			int numberOfTilesInJ = FastMath.floorDiv(width-(marginJLeft + marginJRight), tileWidth);
			
			int marginVerticalLeftOverPixels = height - marginITop - marginIBottom - tileHeight*numberOfTilesInI;
			int marginHorizontalLeftOverPixels = width - marginJLeft - marginJRight - tileWidth*numberOfTilesInJ;
			marginTop = marginITop + marginVerticalLeftOverPixels / 2;
			marginBottom = height - tileHeight*numberOfTilesInI - marginTop;
			marginLeft = marginJLeft + marginHorizontalLeftOverPixels / 2;
			marginRight = width - tileWidth*numberOfTilesInJ - marginLeft;
			
			nextIterationStepTilesParams = new IterationStepTiles(this, 
					InterAreaStableStrategiesFactoryEnum.create(parameters),
					InterAreaVelocityStrategiesFactoryEnum.create(parameters),
					order,
					nextIterationStep, maxSteps,
					(short)tileWidth, (short)tileHeight, (short)numberOfTilesInI, (short)numberOfTilesInJ,
					(short)marginTop, (short)marginLeft, (short)marginBottom, (short)marginRight);


			Tile[][] tiles = nextIterationStepTilesParams.getTilesArray();
			for (int tileIndexI = 0, pixelTop = marginTop; tileIndexI < numberOfTilesInI; tileIndexI++, pixelTop += tileHeight) {
				for (int tileIndexJ = 0, pixelLeft = marginLeft; tileIndexJ < numberOfTilesInJ; tileIndexJ++, pixelLeft += tileWidth) {	
					Tile tile = tiles[tileIndexI][tileIndexJ];					
					tile.setTileIndexI((short)tileIndexI);
					tile.setTileIndexJ((short)tileIndexJ);
					tile.setLeftPixel((short)(pixelLeft));
					tile.setTopPixel((short)(pixelTop));
				}
			}
		}
		
		return nextIterationStepTilesParams;
	}
}
