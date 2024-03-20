// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas.replacement;

import java.util.List;

import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IVectorReplacement {

    public void replaceVector(boolean firstPass, int frameNumber, Tile vector, Tile[][] adjacents, List<MaxCrossResult> maxCrosses);
}
