// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.InvalidStateException;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TileStableStateEnum;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public class NoImageWarpingStrategy implements IImageWarpingStrategy {
    private static Logger logger = LoggerFactory.getLogger(NoImageWarpingStrategy.class);

    @Override
    public void warpAndClipImage(IImage imageA, IImage imageB, IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
        final PIVInputParameters pivParameters = PIVContextSingleton.getSingleton().getPIVParameters();
        int imageHeight = pivParameters.getImageHeightPixels();
        int imageWidth = pivParameters.getImageWidthPixels();

        noWarpAndClipForSingleImage(imageB, stepTilesB, pivParameters, imageHeight, imageWidth);
        noWarpAndClipForSingleImage(imageA, stepTilesA, pivParameters, imageHeight, imageWidth);
    }

    private void noWarpAndClipForSingleImage(IImage image, IterationStepTiles stepTiles,
        final PIVInputParameters pivParameters, int imageHeight, int imageWidth) {
        short tileHeight = stepTiles.getTileHeight();
        short tileWidth = stepTiles.getTileWidth();
                
        for (int tileI = 0; tileI < stepTiles.getNumberOfTilesInI(); tileI++) {
            for (int tileJ = 0; tileJ < stepTiles.getNumberOfTilesInJ(); tileJ++) {
                Tile tile = stepTiles.getTile(tileI, tileJ);
                
                if (tile.getStableState() != TileStableStateEnum.EVALUATING) {
                    continue;
                }
                
                boolean partialClippingAllowed = false;
                if (stepTiles.getTilesOrder() == TilesOrderEnum.SecondImage) {
                    //Only the second image clip area may move
                    ClippingModeEnum clippingMode = pivParameters.getClippingMode();
                    
                    if (clippingMode != ClippingModeEnum.AllowedOutOfBoundClipping) {
                        if (FastMath.round(tile.getDisplacedTileTop()) < 0 || FastMath.round(tile.getDisplacedTileLeft()) < 0 ||
                                FastMath.round(tile.getDisplacedTileTop()) + tileHeight > imageHeight || FastMath.round(tile.getDisplacedTileLeft()) + tileWidth > imageWidth) {
                        
                            if (clippingMode == ClippingModeEnum.NoOutOfBoundClipping) {
                                throw new InvalidStateException("Out of bound clipping isn't allowed, but would be required for tile: " + this);
                            }
                            
                            if (clippingMode == ClippingModeEnum.LoggedOutOfBoundClipping) {
                                logger.warn("WARNING: Tile to be clipped is partially out of bounds. Tile: {}.", toString());
                            }
                            partialClippingAllowed = true;
                        }           
                    } else {
                        partialClippingAllowed = true;
                    }
                    
                    if (tile.getDisplacedTileTop() + tileHeight < 0 || 
                        tile.getDisplacedTileLeft() + tileWidth < 0 ||
                        tile.getDisplacedTileTop() >= imageHeight ||
                        tile.getDisplacedTileLeft() >= imageWidth) {
                        logger.warn("WARNING: Tile to be clipped is completely outside the image. Tile: {}", toString());
                    }
                }
                    
                try {                    
                    Matrix matrix = image.clipImageMatrix(FastMath.round(tile.getDisplacedTileTop()), FastMath.round(tile.getDisplacedTileLeft()), 
                                                          tileHeight, tileWidth, partialClippingAllowed, tile.getMatrix());
                    tile.setMatrix(matrix);
                }  catch (ImageStateException ex) {
                    throw new InvalidStateException("Failed to clip tile at [I: " + tile.getTileIndexI() + ", J: " + tile.getTileIndexJ() + 
                            "] - Displacement: [U: " + tile.getDisplacementU() + ", V: " + tile.getDisplacementV() + "] - [Top: " + tile.getTopPixel() + ", Left: " + tile.getLeftPixel() + "]", ex);
                }
            }
        }
    }
}
