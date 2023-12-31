// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import java.util.Iterator;
import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public interface IMaximumFinder {
	public MaxCrossResult findMaximum(Matrix m);
	
	public void dispose();

	public List<MaxCrossResult> findAllPeaks(List<XCorrelationResults> xCorrResults, Iterator<Tile> tileAIterator,
			Iterator<Tile> tileBIterator);
	
}
