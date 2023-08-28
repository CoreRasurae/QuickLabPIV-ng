// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IBasicCrossCorrelationInterpolator {
	public MaxCrossResult interpolate(final Matrix m, MaxCrossResult result);
}
