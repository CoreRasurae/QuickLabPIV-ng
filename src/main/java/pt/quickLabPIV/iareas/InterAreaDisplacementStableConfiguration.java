// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
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
