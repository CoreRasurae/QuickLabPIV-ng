// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

import pt.quickLabPIV.iareas.IterationStepTiles;

public interface IImageWarpingStrategy {
    
    /**
     * Performs no image warping or warping in one, or both images and then proceeds with the clipping procedure for each tile in the step tiles.
     * If a given tile is marked as stable then it should not be clipped.
     * 
     * @param imageA the imageA (the first image in the image pair)
     * @param imageB the imageB (the second image in the image pair)
     * @param stepTilesA the managing class for all the tiles in a given adaptive step for the first image
     * @param stepTilesB the managing class for all the tiles in a given adaptive step for the second image
     */
    public void warpAndClipImage(final IImage imageA, final IImage imageB, final IterationStepTiles stepTilesA, final IterationStepTiles stepTilesB);
}
