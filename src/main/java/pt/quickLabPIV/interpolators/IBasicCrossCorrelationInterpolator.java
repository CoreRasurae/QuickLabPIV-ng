// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IBasicCrossCorrelationInterpolator {
	public MaxCrossResult interpolate(final Matrix m, MaxCrossResult result);
}
