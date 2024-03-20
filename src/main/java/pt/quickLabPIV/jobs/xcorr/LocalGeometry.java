// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.xcorr;

class LocalGeometry {
	public int localSizeX;
	public int localSizeY;
    public int minNumberOfGroups;
	
	public float[] w;
	public int[] inputOrder;
	public int[] shuffleOrder;
}
