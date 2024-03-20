// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.xcorr;

import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class XCorrelationResults {
	private List<Matrix> crossMatrices;
	private List<MaxCrossResult> maxCrossResults;
	private float[] array;
	private int dimI;
	private int dimJ;
	private int stride;
	private int numberOfMatrices;
	
	public XCorrelationResults(List<Matrix> _crossMatrices, List<MaxCrossResult> _maxResults, float[] _array, int _dimI, int _dimJ, int _numberOfMatrices) {
		crossMatrices = _crossMatrices;
		maxCrossResults = _maxResults;
		array = _array;
		dimI = _dimI;
		dimJ = _dimJ;
		numberOfMatrices = _numberOfMatrices;
	}
	
	public List<Matrix> getCrossMatrices() {
		return crossMatrices;
	}
	
	public List<MaxCrossResult> getMaxCrossResults() {
		return maxCrossResults;
	}
	
	public float[] getArray() {
		return array;
	}
	
	public int getDimI() {
		return dimI;
	}
	
	public int getDimJ() {
		return dimJ;
	}
	
	public int getNumberOfMatrices() {
		return numberOfMatrices;
	}
}
