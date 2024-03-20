// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.maximum;

public class FindMaximumMultiPeaksConfiguration {
	public final static String IDENTIFIER = "MAX_MULTI_PEAKS_CFG";
	
	private int numberOfPeaks;
	private int kernelWidth;
	
	public FindMaximumMultiPeaksConfiguration(int _numberOfPeaks, int _kernelWidth) {
		numberOfPeaks = _numberOfPeaks;
		kernelWidth = _kernelWidth;
	}
	
	public int getNumberOfPeaks() {
		return numberOfPeaks;
	}
	
	public int getKernelWidth() {
		return kernelWidth;
	}
}
