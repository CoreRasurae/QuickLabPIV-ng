// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs;

import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.images.IImage;

public class ImageWarpingInputData {
    public IImage imageA;
    public IImage imageB;
    public IterationStepTiles stepTilesA;
    public IterationStepTiles stepTilesB;
}
