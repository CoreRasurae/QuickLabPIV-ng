// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public class TileMatcher {
	private int parentI;
	private int parentJ;
	private int currentI;
	private int currentJ;
	private short currentStep;
	
	public TileMatcher(int currentIndexI, int currentIndexJ, int currentStep, int parentIndexI, int parentIndexJ) {
		parentI = parentIndexI;
		parentJ = parentIndexJ;
		currentI = currentIndexI;
		currentJ = currentIndexJ;
		this.currentStep = (short)currentStep;
		
	}
	
	public boolean matches(final Tile currentTile, final Tile parentTile) {
		if (currentTile.getParentIterationStepTiles() == null) {
			return false;
		}
		
		if (currentTile.getParentIterationStepTiles().getCurrentStep() != currentStep) {
			return false;
		}
		
		if (parentI >= 0 && parentTile.getTileIndexI() != parentI) {
			return false;
		}
		
		if (parentJ >= 0 && parentTile.getTileIndexJ() != parentJ) {
			return false;
		}
		
		if (currentI >= 0 && currentTile.getTileIndexI() != currentI) {
			return false;
		}
		
		if (currentJ >= 0 && currentTile.getTileIndexJ() != currentJ) {
			return false;
		}
		
		return true;
	}
}
