// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public class InterAreaDisplacementStableConfiguration {
	public static final String IDENTIFIER = "IAREA_STABLE_DISPCONFIG";
	private float maxDisplacement;
	private int maxRetries;
	
	public InterAreaDisplacementStableConfiguration(float maxDisplacement, int maxRetries) {
		this.maxDisplacement = maxDisplacement;
		this.maxRetries = maxRetries;
	}
	
	public float getMaxDisplacement() {
		return maxDisplacement;
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}
}
