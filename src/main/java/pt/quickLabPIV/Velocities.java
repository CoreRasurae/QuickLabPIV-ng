// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

public class Velocities {
	private int frameNumber;
	private float u[][];
	private float v[][];
	
	public Velocities(int frameNumber, int mapHeight, int mapWidth) {
		this.frameNumber = frameNumber;
		u = new float[mapHeight][mapWidth];
		v = new float[mapHeight][mapWidth];
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public float[][] getU() {
		return u;
	}
	
	public float[][] getV() {
		return v;
	}

    public void clear() {
        u = null;
        v = null;
    }
}
