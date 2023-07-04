package pt.quickLabPIV.iareas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyMixedSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public class AdaptiveInterAreaMixedGeometryTests {
	@Before
	public void setup() {
		PIVContextTestsSingleton.setSingleton();
	}

	private void verifier(IterationStepTiles stepTiles, PIVInputParameters parameters, int maxSteps, int marginComputedLeft, int marginComputedRight,
			int marginComputedTop, int marginComputedBottom, int initialTileWidth, int initialTileHeight, int tileWidth, int tileHeight) {
		final int imageHeightNoMargin = parameters.getImageHeightPixels() - marginComputedTop - marginComputedBottom;
		final int imageWidthNoMargin = parameters.getImageWidthPixels() - marginComputedLeft - marginComputedRight;
				
		assertEquals("Number of max. steps is not correct", maxSteps, stepTiles.getMaxAdaptiveSteps());
				
		assertEquals("Tile height doesn't match", tileWidth, stepTiles.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles.getTileWidth());
				
		int tilesInI = stepTiles.getNumberOfTilesInI();
		int tilesInJ = stepTiles.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal
		int expectedTilesInJ = 0;
		float incrementPixelsI = tileHeight * parameters.getOverlapFactor();
		float incrementPixelsJ = tileWidth * parameters.getOverlapFactor();
		
		float xPos = 0.0f;
		for (xPos = 0.0f; xPos <= imageWidthNoMargin - tileWidth + 0.01; xPos+=incrementPixelsJ) {
			expectedTilesInJ++;
		}
		
		int expectedTilesInI = 0;
		float yPos = 0.0f;
		for (yPos = 0.0f; yPos <= imageHeightNoMargin - tileHeight + 0.01; yPos+=incrementPixelsI) {
			expectedTilesInI++;
		}
		
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
				
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", marginComputedLeft, stepTiles.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", marginComputedRight, stepTiles.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", marginComputedTop, stepTiles.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", marginComputedBottom, stepTiles.getMarginBottom());
		
		Tile tile = stepTiles.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", marginComputedLeft, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", marginComputedTop, tile.getTopPixel()); 

		
		Tile[][] tiles = stepTiles.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), indexI doesn't match tile index information: " + 
						tile.getTileIndexI(), tileIndexI, tile.getTileIndexI());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), indexJ doesn't match tile index information: " + 
						tile.getTileIndexJ(), tileIndexJ, tile.getTileIndexJ());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 
						marginComputedTop + FastMath.round(incrementPixelsI * tileIndexI), tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct",  
						marginComputedLeft + FastMath.round(incrementPixelsJ * tileIndexJ), tile.getLeftPixel());
			}
		}
	}

	@Test
	public void simpleIAStep0NoSuperpositionTest1Pass() {		
		PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);
		
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;
		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16 + 16, stepTiles.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16 + 16, stepTiles.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16 + 8, stepTiles.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16 + 8, stepTiles.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles.getTileWidth());
		
		Tile tile = stepTiles.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 32, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 8 + 16, tile.getTopPixel()); 
		
		int tilesInI = stepTiles.getNumberOfTilesInI();
		int tilesInJ = stepTiles.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 32) / 128;
		int expectedTilesInJ = (1600 - 32) / 128;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 24 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 32 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}
	
	@Test
	public void simpleIAStep0NoSuperpositionTest2Pass() {		
	    PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1201);
		parameters.setImageWidthPixels(1601);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);
		
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16 + 16, stepTiles.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16 + 17, stepTiles.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16 + 8, stepTiles.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16 + 9, stepTiles.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles.getTileWidth());
		
		Tile tile = stepTiles.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 32, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 8 + 16, tile.getTopPixel()); 
		
		int tilesInI = stepTiles.getNumberOfTilesInI();
		int tilesInJ = stepTiles.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 32) / 128;
		int expectedTilesInJ = (1600 - 32) / 128;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 24 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 32 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}

	@Test
	public void simpleIAStep1NoSuperpositionTest1Pass() {		
	    PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(2);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16 + 16, stepTiles1.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16 + 16, stepTiles1.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16 + 8, stepTiles1.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16 + 8, stepTiles1.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles1.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles1.getTileWidth());
		
		Tile tile = stepTiles1.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 32, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 8 + 16, tile.getTopPixel()); 
		
		int tilesInI = stepTiles1.getNumberOfTilesInI();
		int tilesInJ = stepTiles1.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 48) / tileHeight;
		int expectedTilesInJ = (1600 - 64) / tileWidth;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles1.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 24 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 32 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}

	@Test
	public void simpleIAStep2NoSuperpositionTest1Pass() {		
	    PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setSuperPositionIterationStepStart(3);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(16);
		
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		assertTrue("Iterations ended to early", stepTiles2 != null);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16, stepTiles2.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16, stepTiles2.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16 + 8, stepTiles2.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16 + 8, stepTiles2.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles2.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles2.getTileWidth());
		
		Tile tile = stepTiles2.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 16, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 8 + 16, tile.getTopPixel()); 
		
		int tilesInI = stepTiles2.getNumberOfTilesInI();
		int tilesInJ = stepTiles2.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 48) / tileHeight;
		int expectedTilesInJ = (1600 - 32) / tileWidth;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles2.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 24 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 16 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}

	@Test
	public void simpleIAStep3NoSuperpositionTest1Pass() {		
	    PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setSuperPositionIterationStepStart(4);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(16);
		
		//Current iteration step tile geometry
		final int tileWidth = 16;
		final int tileHeight = 16;

		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);

		assertTrue("Iterations ended to early", stepTiles3 != null);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/128 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16, stepTiles3.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16, stepTiles3.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/128 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16, stepTiles3.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16, stepTiles3.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles3.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles3.getTileWidth());
		
		Tile tile = stepTiles3.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 16, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 16, tile.getTopPixel()); 
		
		int tilesInI = stepTiles3.getNumberOfTilesInI();
		int tilesInJ = stepTiles3.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 32) / tileHeight;
		int expectedTilesInJ = (1600 - 32) / tileWidth;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles3.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 16 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 16 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}

	@Test
	public void simpleIAStep4NoSuperpositionAfterFinalTest1Pass() {		
	    PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setSuperPositionIterationStepStart(5);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(16);
		
		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		IterationStepTiles stepTiles4 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles3);

		assertTrue("Iterations didn't end at their step", stepTiles4 == null);
	}
	
	@Test
	public void simpleIASuperPositionValidationTest1Pass() {		
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(107+32);
		parameters.setImageWidthPixels(107+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		//Compute margin:
		//107 - (64 - 64/3) / (64/3) =  3,015625
		//0,015625 * 64/3 = 0,33333 additional pixels left = 0
		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;			
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);

		verifier(stepTiles, parameters, 2, 
				marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
				tileWidth, tileHeight, tileWidth, tileHeight);			

	}
	
	@Test
	public void simpleIASuperPositionValidationTest2Pass() {		
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(108+32);
		parameters.setImageWidthPixels(107+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		//Compute margin:
		//108 - (64 - 64/3) / (64/3) =  3,0625
		//0,0625 * 64/3 = 1,33333 additional pixels left = 1px which will add to the bottom margin
		final int marginComputedTop = 16;
		final int marginComputedBottom = 17;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;			
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 2,
				marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
				tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionValidationTest3Pass() {		
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(107+1+32);
		parameters.setImageWidthPixels(107+3+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 16;
		final int marginComputedBottom = 17;
		final int marginComputedLeft = 17;
		final int marginComputedRight = 18;
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);

		verifier(stepTiles, parameters, 2,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);	
	}


	@Test
	public void simpleIASuperPositionStep1ValidationTest1Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 24;
		final int marginComputedBottom = 24;
		final int marginComputedLeft = 32;
		final int marginComputedRight = 32;
			
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep1ValidationTest2Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 24;
		final int marginComputedBottom = 24;
		final int marginComputedLeft = 21;
		final int marginComputedRight = 21;
			
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 2,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionStep1ValidationTest3Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/2.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 24;
		final int marginComputedBottom = 24;
		final int marginComputedLeft = 32;
		final int marginComputedRight = 32;
			
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep1ValidationTest4Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 17;
		final int marginComputedBottom = 18;
		final int marginComputedLeft = 16+3;
		final int marginComputedRight = 16+3;
			
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep1ValidationTest5Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(0);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(9.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 17;
		final int marginComputedBottom = 18;
		final int marginComputedLeft = 44;
		final int marginComputedRight = 45;
			
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		
		verifier(stepTiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep2ValidationTest1Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 24;
		final int marginComputedBottom = 24;
		final int marginComputedLeft = 21;
		final int marginComputedRight = 21;
			
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles step1Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles step2Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step1Tiles);
		
		verifier(step2Tiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			128, 128, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep2ValidationTest2Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(107+32);
		parameters.setImageWidthPixels(107+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles step1Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles step2Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step1Tiles);
		
		verifier(step2Tiles, parameters, 2,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			64, 64, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep2ValidationTest3Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/2.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 24;
		final int marginComputedBottom = 24;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		
		verifier(stepTiles2, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionStep2ValidationTest4Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 17;
		final int marginComputedBottom = 18;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		
		verifier(stepTiles2, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep2ValidationTest5Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(9.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 20;
		final int marginComputedBottom = 21;
		final int marginComputedLeft = 19;
		final int marginComputedRight = 19;
			
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		
		verifier(stepTiles2, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionStep3ValidationTest1Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		//Margins must be the same as in step 1...
		final int marginComputedTop = 18;
		final int marginComputedBottom = 19;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles step1Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles step2Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step1Tiles);
		IterationStepTiles step3Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step2Tiles);
		
		verifier(step3Tiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			128, 128, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep3ValidationTest2Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(107+32);
		parameters.setImageWidthPixels(107+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(16);

		//Margins must be the same as in step 1...
		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 16;
		final int tileHeight = 16;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles step1Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles step2Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step1Tiles);
		IterationStepTiles step3Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step2Tiles);
		
		verifier(step3Tiles, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			64, 64, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep3ValidationTest3Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/2.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		
		verifier(stepTiles3, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionStep3ValidationTest4Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		
		verifier(stepTiles3, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep3ValidationTest5Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(9.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(32);

		final int marginComputedTop = 22;
		final int marginComputedBottom = 22;
		final int marginComputedLeft = 20;
		final int marginComputedRight = 21;
			
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		
		verifier(stepTiles3, parameters, 3,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
	
	@Test
	public void simpleIASuperPositionStep4ValidationTest1Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(16);

		//Margins must be the same as in step 1...
		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;
			
		//Current iteration step tile geometry
		final int tileWidth = 16;
		final int tileHeight = 16;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles step1Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles step2Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step1Tiles);
		IterationStepTiles step3Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step2Tiles);
		IterationStepTiles step4Tiles = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, step3Tiles);
		
		verifier(step4Tiles, parameters, 4,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			128, 128, tileWidth, tileHeight);			
	}

	@Test
	public void simpleIASuperPositionStep4ValidationTest5Pass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setSuperPositionIterationStepStart(1);
		parameters.setImageHeightPixels(1200);
		parameters.setImageWidthPixels(1600);
		parameters.setOverlapFactor(9.0f/10.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(128);
		parameters.setInterrogationAreaEndIPixels(16);
		parameters.setInterrogationAreaStartJPixels(128);
		parameters.setInterrogationAreaEndJPixels(16);

		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 21;
		final int marginComputedRight = 22;
			
		//Current iteration step tile geometry
		final int tileWidth = 16;
		final int tileHeight = 16;

		AdaptiveInterAreaStrategyMixedSuperPosition strategy = new AdaptiveInterAreaStrategyMixedSuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		IterationStepTiles stepTiles4 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles3);
		
		verifier(stepTiles4, parameters, 4,
			marginComputedLeft, marginComputedRight, marginComputedTop, marginComputedBottom,
			tileWidth, tileHeight, tileWidth, tileHeight);			
	}
}
