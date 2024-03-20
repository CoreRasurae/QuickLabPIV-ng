// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public class AdaptiveInterAreaNoSuperPositionGeometryTests {

	@Before
	public void setup() {
	    PIVContextTestsSingleton.setSingleton();
	}
	
	@Test
	public void simpleIAStep0Test1Pass() {		
	    PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;
		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
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
	public void simpleIAStep0Test2Pass() {		
	    PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		//Current iteration step tile geometry
		final int tileWidth = 128;
		final int tileHeight = 128;

		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
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
	public void simpleIAStep1Test1Pass() {		
		PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		
		//16=32/2 are the left over pixels from the decimal values of (1600-(16+16))/64 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16 + 16, stepTiles1.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16 + 16, stepTiles1.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/64 when equally distributed to Top and Bottom margins
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
	public void simpleIAStep2Test1Pass() {		
	    PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		//Current iteration step tile geometry
		final int tileWidth = 32;
		final int tileHeight = 32;

		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		assertTrue("Iterations ended to early", stepTiles2 != null);
		
		//0 are the left over pixels from the decimal values of (1600-(16+16))/32 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16, stepTiles2.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16, stepTiles2.getMarginRight());
		//8=16/2 are the left over pixels from the decimal values of (1200-(16+16))/32 when equally distributed to Top and Bottom margins
		assertEquals("Top margin in pixels is not correct", 16+8, stepTiles2.getMarginTop());
		assertEquals("Bottom margin in pixels is not correct", 16+8, stepTiles2.getMarginBottom());
		
		assertEquals("Tile height doesn't match", tileWidth, stepTiles2.getTileHeight());
		assertEquals("Tile width doesn't match", tileHeight, stepTiles2.getTileWidth());
		
		Tile tile = stepTiles2.getTile(0, 0);
		assertEquals("Tile indices (0,0), J left pixel position is not correct", 16, tile.getLeftPixel());
		assertEquals("Tile indices (0,0), I top pixel position is not correct", 16+8, tile.getTopPixel()); 
		
		int tilesInI = stepTiles2.getNumberOfTilesInI();
		int tilesInJ = stepTiles2.getNumberOfTilesInJ();
		
		//Number of tiles in I should equal 
		int expectedTilesInI = (1200 - 32) / tileHeight;
		int expectedTilesInJ = (1600 - 32) / tileWidth;
		assertEquals("Expected number of tiles in I is not correct", expectedTilesInI, tilesInI);
		assertEquals("Expected number of tiles in J is not correct", expectedTilesInJ, tilesInJ);
		
		Tile[][] tiles = stepTiles2.getTilesArray();
		
		for (int tileIndexI = 0; tileIndexI < expectedTilesInI; tileIndexI++) {
			for (int tileIndexJ = 0; tileIndexJ < expectedTilesInJ; tileIndexJ++) {
				tile = tiles[tileIndexI][tileIndexJ];
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), I top pixel position is not correct", 16+8 + tileHeight*tileIndexI, tile.getTopPixel());
				assertEquals("Tile indices (" + tileIndexI + ", " + tileIndexJ + "), J left pixel position is not correct", 16 + tileWidth*tileIndexJ, tile.getLeftPixel());
			}
			
		}
	}

	@Test
	public void simpleIAStep3Test1Pass() {		
	    PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		//Current iteration step tile geometry
		final int tileWidth = 16;
		final int tileHeight = 16;

		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);

		assertTrue("Iterations ended to early", stepTiles3 != null);
		
		//0 are the left over pixels from the decimal values of (1600-(16+16))/16 when equally distributed to Left and Right margins
		assertEquals("Left margin in pixels is not correct", 16, stepTiles3.getMarginLeft());
		assertEquals("Right margin in pixels is not correct", 16, stepTiles3.getMarginRight());
		//0 are the left over pixels from the decimal values of (1200-(16+16))/16 when equally distributed to Top and Bottom margins
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
	public void simpleIAStep4AfterFinalTest1Pass() {		
	    PIVInputParameters parameters = PIVContextTestsSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
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
		parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
		IterationStepTiles stepTiles0 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles0);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);
		IterationStepTiles stepTiles3 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles2);
		IterationStepTiles stepTiles4 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles3);

		assertTrue("Iterations didn't end at their step", stepTiles4 == null);
	}
}
