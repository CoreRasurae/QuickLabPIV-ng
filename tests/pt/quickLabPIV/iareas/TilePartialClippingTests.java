// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageTestHelper;

public class TilePartialClippingTests {
	
	@Test
	public void testPartialClippingNotAllowed1Pass() {
		IImage img = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		
		PIVContextTestsSingleton.setSingleton();
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setClippingMode(ClippingModeEnum.NoOutOfBoundClipping);
		parameters.setImageHeightPixels(107+64);
		parameters.setImageWidthPixels(107+64);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(32);
		parameters.setMarginPixelsIBottom(32);
		parameters.setMarginPixelsJLeft(32);
		parameters.setMarginPixelsJRight(32);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);
		
		Tile t = new Tile(null);
		
		t.accumulateDisplacement(-1.0f, -1.0f);
		assertEquals("Displacement in U should have been reset", 0.0f, t.getDisplacementU(), 1e-4f);
		assertEquals("Displacement in V should have been reset", 0.0f, t.getDisplacementV(), 1e-4f);
	}

	@Test
	public void testPartialClippingAllowed1Pass() {
		IImage img = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		
		PIVContextTestsSingleton.setSingleton();
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setClippingMode(ClippingModeEnum.AllowedOutOfBoundClipping);
		parameters.setImageHeightPixels(107+64);
		parameters.setImageWidthPixels(107+64);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(32);
		parameters.setMarginPixelsIBottom(32);
		parameters.setMarginPixelsJLeft(32);
		parameters.setMarginPixelsJRight(32);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);
		
		Tile t = new Tile(null);
		
		t.accumulateDisplacement(-1.0f, -1.0f);
		assertEquals("Displacement in U should have been reset", -1.0f, t.getDisplacementU(), 1e-4f);
		assertEquals("Displacement in V should have been reset", -1.0f, t.getDisplacementV(), 1e-4f);
	}
	
	@Test
	public void testPartialClippingLogged1Pass() {
		IImage img = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		
		PIVContextTestsSingleton.setSingleton();
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setClippingMode(ClippingModeEnum.LoggedOutOfBoundClipping);
		parameters.setImageHeightPixels(107+64);
		parameters.setImageWidthPixels(107+64);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(32);
		parameters.setMarginPixelsIBottom(32);
		parameters.setMarginPixelsJLeft(32);
		parameters.setMarginPixelsJRight(32);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);
		
		Tile t = new Tile(null);
		
		t.accumulateDisplacement(-1.0f, -1.0f);
		assertEquals("Displacement in U should have been reset", -1.0f, t.getDisplacementU(), 1e-4f);
		assertEquals("Displacement in V should have been reset", -1.0f, t.getDisplacementV(), 1e-4f);
	}

}
