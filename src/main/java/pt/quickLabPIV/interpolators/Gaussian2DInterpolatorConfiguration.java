// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

public class Gaussian2DInterpolatorConfiguration {
	public final static String IDENTIFIER = "InterpGaussian2D";
	
	private int numberOfPointsInX;
	private int numberOfPointsInY;
	private Gaussian2DSubTypeFactoryEnum gaussianSubType;
	private boolean logResults = false;
	
	public void setProperties(int numberOfPointsInX, int numberOfPointsInY, 
			Gaussian2DSubTypeFactoryEnum gaussianSubType) {
		this.numberOfPointsInX = numberOfPointsInX;
		this.numberOfPointsInY = numberOfPointsInY;
		this.gaussianSubType = gaussianSubType;
	}

	public void setLogResults(boolean logResults) {
		this.logResults = logResults;
	}
	
	public int getNumberOfPointsInY() {
		return numberOfPointsInY;
	}
	
	public int getNumberOfPointsInX() {
		return numberOfPointsInX;
	}
	
	public Gaussian2DSubTypeFactoryEnum getGaussianSubType() {
		return gaussianSubType;
	}
	
	public boolean isLogResults() {
		return logResults;
	}
}
