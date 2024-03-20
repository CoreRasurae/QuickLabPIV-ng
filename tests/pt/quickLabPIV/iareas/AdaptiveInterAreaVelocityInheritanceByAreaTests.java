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

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategySuperPosition;
import pt.quickLabPIV.iareas.AdaptiveInterVelocityInheritanceStrategyArea;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public class AdaptiveInterAreaVelocityInheritanceByAreaTests {

	@Before
	public void setup() {
	}
	
	@Test
	public void testOverlapFactorPass1() {
		//Current tile is completely contained within parent tile (position1)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)16);
		currentTile.setLeftPixel((short)16);
		
		AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
		
		AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
		
		result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 1.0f, result.overlapFactor, 0.0001f);
	}
	
	@Test
	public void testOverlapFactorPass2() {
		//Current tile is completely contained within parent tile (position2 - border match)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)32);
		currentTile.setLeftPixel((short)32);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
				
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 1.0f, result.overlapFactor, 0.0001f);
	}

	@Test
	public void testOverlapFactorPass3() {
		//Current tile is partially outside (bottom side) of parent tile
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)48);
		currentTile.setLeftPixel((short)32);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 0.5f, result.overlapFactor, 0.0001f);
	}

	@Test
	public void testOverlapFactorPass4() {
		//Current tile is partially outside (right side) of parent tile
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)32);
		currentTile.setLeftPixel((short)48);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 0.5f, result.overlapFactor, 0.0001f);
	}

	@Test
	public void testOverlapFactorPass5() {
		//Current tile is partially outside (bottom and right sides) of parent tile (position 1)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)48);
		currentTile.setLeftPixel((short)48);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 0.25f, result.overlapFactor, 0.0001f);
	}

	@Test
	public void testOverlapFactorPass6() {
		//Current tile is partially outside (bottom and right sides) of parent tile (position 2)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)56);
		currentTile.setLeftPixel((short)56);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
		
		assertEquals("Overlap factor is incorrect", 0.0625f, result.overlapFactor, 0.0001f);
	}
	
	@Test
	public void testOverlapFactorPass7() {
		//Current tile is completely outside (down and to the right of the current tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)0);
		parentTile.setLeftPixel((short)0);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)64);
		currentTile.setLeftPixel((short)64);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertTrue("Should have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertTrue("Should have to move right", result.moveRight);

	}

	@Test
	public void testOverlapFactorPass8() {
		//Current tile is completely outside (down and to the right of the current tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)64);
		parentTile.setLeftPixel((short)64);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)32);
		currentTile.setLeftPixel((short)32);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertTrue("Should have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertTrue("Should have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);

	}

	@Test
	public void testOverlapFactorPass9() {
		//Current tile is partially outside (top and left sides) of parent tile
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)16);
		parentTile.setLeftPixel((short)16);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)0);
		currentTile.setLeftPixel((short)0);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.25f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);

	}

	@Test
	public void testOverlapFactorPass10() {
		//Current tile is partially outside (left side) of parent tile
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)16);
		parentTile.setLeftPixel((short)16);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)16);
		currentTile.setLeftPixel((short)0);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.5f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);

	}

	@Test
	public void testOverlapFactorPass11() {
		//Current tile is partially outside (top side) of parent tile
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)16);
		parentTile.setLeftPixel((short)16);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)0);
		currentTile.setLeftPixel((short)16);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.5f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);

	}

	@Test
	public void testOverlapFactorPass12() {
		//Current tile is completely outside (to the left of the parent tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)64);
		parentTile.setLeftPixel((short)64);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)64);
		currentTile.setLeftPixel((short)32);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertTrue("Should have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
	}

	@Test
	public void testOverlapFactorPass13() {
		//Current tile is completely outside (up of the parent tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)64);
		parentTile.setLeftPixel((short)64);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)32);
		currentTile.setLeftPixel((short)64);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertTrue("Should have to move up", result.moveUp); //Parent tile must move up
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
	}

	@Test
	public void testOverlapFactorPass14() {
		//Current tile is completely outside (right of the  parent tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)64);
		parentTile.setLeftPixel((short)64);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)64);
		currentTile.setLeftPixel((short)128);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertFalse("Shouldn't have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertTrue("Should have to move right", result.moveRight);
	}

	@Test
	public void testOverlapFactorPass15() {
		//Current tile is completely outside (down of the parent tile)
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
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
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile parentTile = new Tile(null);
		parentTile.setTopPixel((short)64);
		parentTile.setLeftPixel((short)64);
		
		Tile currentTile = new Tile(null);
		currentTile.setTopPixel((short)128);
		currentTile.setLeftPixel((short)64);
		
        AdaptiveInterVelocityInheritanceStrategyArea area = new AdaptiveInterVelocityInheritanceStrategyArea();
        
        AdaptiveInterVelocityInheritanceStrategyArea.OverlapResult result = area.new OverlapResult();
        
        result = AdaptiveInterVelocityInheritanceStrategyArea.checkOverlappingFactor(stepTiles1, parentTile, stepTiles2, currentTile, result);
			
		assertEquals("Overlap factor is incorrect", 0.0f, result.overlapFactor, 0.0001f);
		
		assertFalse("Shouldn't have to move up", result.moveUp);
		assertTrue("Should have to move down", result.moveDown);
		assertFalse("Shouldn't have to move left", result.moveLeft);
		assertFalse("Shouldn't have to move right", result.moveRight);
	}
}
