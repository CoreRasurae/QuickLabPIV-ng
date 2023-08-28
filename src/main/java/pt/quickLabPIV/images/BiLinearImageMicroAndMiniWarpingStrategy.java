// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.BiCubicSplineInterpolatorWithBiLinearBackup;
import pt.quickLabPIV.iareas.InvalidStateException;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TileStableStateEnum;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public final class BiLinearImageMicroAndMiniWarpingStrategy implements IImageWarpingStrategy {
    private static Logger logger = LoggerFactory.getLogger(BiLinearImageMicroAndMiniWarpingStrategy.class);
    
    private float[][][] interpolatedPixelDisplacements = null;
    private Matrix imageMatrixA = null;
    private Matrix imageMatrixB = null;
    private BiLinearImageWarpingModeEnum warpingMode;
    private boolean miniWarpingMode;
    
    public BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum mode, boolean isMiniWarping) {
        warpingMode = mode;
        miniWarpingMode = isMiniWarping;
    }
    
    @Override
    public void warpAndClipImage(IImage imageA, IImage imageB, IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
        final PIVInputParameters pivParameters = PIVContextSingleton.getSingleton().getPIVParameters();
        int imageHeight = pivParameters.getImageHeightPixels();
        int imageWidth = pivParameters.getImageWidthPixels();

        short tileHeight = stepTilesB.getTileHeight();
        short tileWidth = stepTilesB.getTileWidth();

        if (stepTilesB.getTilesOrder() != TilesOrderEnum.SecondImage) {
            throw new ImageClippingException("Failed to do clipping and warping, because tiles are in wrong tile order from the expected");
        }
                
        for (int tileI = 0; tileI < stepTilesB.getNumberOfTilesInI(); tileI++) {
            for (int tileJ = 0; tileJ < stepTilesB.getNumberOfTilesInJ(); tileJ++) {
                Tile tileA = stepTilesA.getTile(tileI, tileJ);
                Tile tileB = stepTilesB.getRelatedTile(tileA);
                
                if (tileA.getStableState() != TileStableStateEnum.EVALUATING || tileB.getStableState() != TileStableStateEnum.EVALUATING) {
                    continue;
                }
                
                try {
                    if (stepTilesB.getCurrentStep() > 0) {                        
                        switch (warpingMode) {
                        case SecondImage:
                            Matrix matrixA = imageA.clipImageMatrix(tileA.getTopPixel(), tileA.getLeftPixel(), tileHeight, tileWidth, false, tileA.getMatrix());
                            tileA.setMatrix(matrixA);
                            break;
                        case FirstImage:
                            float uOffset = 0.0f;
                            float vOffset = 0.0f;
                            short tileTopA = (short)(tileA.getTopPixel() - FastMath.floor(tileB.getDisplacementU()));
                            short tileLeftA = (short)(tileA.getLeftPixel() - FastMath.floor(tileB.getDisplacementV()));
                            short tileTopRefB = (short)(tileB.getTopPixel() + FastMath.floor(tileB.getDisplacementU()));
                            short tileLeftRefB = (short)(tileB.getLeftPixel() + FastMath.floor(tileB.getDisplacementV()));

                            if (miniWarpingMode) {
                                interpolatedPixelDisplacements = createMiniDisplacements(stepTilesB, tileB, tileTopRefB, tileLeftRefB, interpolatedPixelDisplacements); 
                             } else {
                                interpolatedPixelDisplacements = createMicroDisplacements(stepTilesB, tileB, interpolatedPixelDisplacements);
                             }
                             imageMatrixA = doBiLinearWarp(imageMatrixA, 0.0f, 0.0f, 
                                                           tileTopA, tileLeftA, stepTilesB.getTileHeight(), stepTilesB.getTileWidth(), 
                                                           imageA, interpolatedPixelDisplacements, warpingMode, TilesOrderEnum.FirstImage);
                             matrixA = imageMatrixA.copyMatrixRegion((short)0, (short)0, tileHeight, tileWidth, tileA.getMatrix());
                             tileA.setMatrix(matrixA);

                            break;
                        case BothImages:
                            tileTopA = (short)(tileA.getTopPixel() - FastMath.floor(tileB.getDisplacementU()/2.0f));
                            tileLeftA = (short)(tileA.getLeftPixel() - FastMath.floor(tileB.getDisplacementV()/2.0f));
                            tileTopRefB = (short)(tileB.getTopPixel() + FastMath.floor(tileB.getDisplacementU()));
                            tileLeftRefB = (short)(tileB.getLeftPixel() + FastMath.floor(tileB.getDisplacementV()));
                            uOffset = (float)(tileB.getDisplacementU()/2.0f - FastMath.floor(tileB.getDisplacementU()/2.0f)) - (float)(tileB.getDisplacementU() - FastMath.floor(tileB.getDisplacementU()));
                            vOffset = (float)(tileB.getDisplacementV()/2.0f - FastMath.floor(tileB.getDisplacementV()/2.0f)) - (float)(tileB.getDisplacementV() - FastMath.floor(tileB.getDisplacementV()));
                            //uOffset and vOffset are set to compensate the rounding differences introduced in the positioning/distance of the IAs and the velocity offset by the employed math in
                            //the both images case.
                            //So let's say that displacementU is 15.25 so that floor(15.25) = 15. but floor(15.25/2) = 7 and 2*7=14 not 15, 
                            //also the velocity (15.25 - 15)/2 = 0.25 / 2 = 0.125 is not (15.25/2 - floor(15/2)) = 7.625 - 7 = 0.625
                            //
                            //So to compensate this, do for the first image:
                            //uOffset1 = (dispU / 2 - floor(dispU/2.0)) - (dispU - floor(dispU)) = - dispU / 2.0 + floor(dispU) - floor(dispU/2.0)
                            //u1 = (dispU - floor(dispU)) / 2.0
                            //so that newU1 = uOffset2 + u1 = uOffset2 +  = (-dispU / 2.0 + dispU / 2.0) - floor(dispU)/2.0 + floor(dispU) - floor(dispU/2.0) =  0 + floor(dispU)/2.0 - floor(dispU/2.0)
                            //in the warping process: newU1 = floor(dispU)/2.0 - floor(dispU/2.0) becomes -newU1 = floor(dispU/2.0) - floor(dispU)/2.0
                            //
                            //and for the second image:
                            //uOffset2 = (dispU/2.0) - floor(dispU/2.0)
                            //newU2 = u2 + uOffset2 = (dispU - floor(dispU)) / 2.0 + dispU/2.0 - floor(dispU/2.0) = dispU/2 + dispU/2 - floor(dispU)/2.0 - floor(dispU/2.0) =
                            // = dispU - floor(dispU)/2.0 - floor(dispU / 2.0)
                            //
                            //Now the overall velocity is:
                            //newU2 - newU1 = dispU + [- floor(dispU/2.0) + floor(dispU/2.0)]  - floor(dispU)/2.0 - floor(dispU)/2.0 = dispU + 0 - floor(dispU) = dispU - floor(dispU), as intended
                            //
                            if (miniWarpingMode) {
                               interpolatedPixelDisplacements = createMiniDisplacements(stepTilesB, tileB, tileTopRefB, tileLeftRefB, interpolatedPixelDisplacements); 
                            } else {
                               interpolatedPixelDisplacements = createMicroDisplacements(stepTilesB, tileB, interpolatedPixelDisplacements);
                            }
                            imageMatrixA = doBiLinearWarp(imageMatrixA, uOffset, vOffset, 
                                                          tileTopA, tileLeftA, stepTilesB.getTileHeight(), stepTilesB.getTileWidth(), 
                                                          imageA, interpolatedPixelDisplacements, warpingMode, TilesOrderEnum.FirstImage);
                            matrixA = imageMatrixA.copyMatrixRegion((short)0, (short)0, tileHeight, tileWidth, tileA.getMatrix());
                            tileA.setMatrix(matrixA);
                            break;
                        default:
                            throw new ImageClippingException("Unknown BiLinear image warping mode: " + warpingMode.toString());
                        }
                        switch (warpingMode) {
                        case FirstImage:                            
                            Matrix matrixB = imageB.clipImageMatrix(tileB.getTopPixel(), tileB.getLeftPixel(), tileHeight, tileWidth, false, tileB.getMatrix());                   
                            tileB.setMatrix(matrixB);
                            break;
                        case SecondImage:
                            float uOffset = 0.0f;
                            float vOffset = 0.0f;
                            short tileTopB = (short)(tileB.getTopPixel() + FastMath.floor(tileB.getDisplacementU()));
                            short tileLeftB = (short)(tileB.getLeftPixel() + FastMath.floor(tileB.getDisplacementV()));

                            if (miniWarpingMode) {
                                interpolatedPixelDisplacements = createMiniDisplacements(stepTilesB, tileB, tileTopB, tileLeftB, interpolatedPixelDisplacements); 
                             } else {
                                interpolatedPixelDisplacements = createMicroDisplacements(stepTilesB, tileB, interpolatedPixelDisplacements);
                             }
                             imageMatrixB = doBiLinearWarp(imageMatrixB, 0.0f, 0.0f, 
                                                           tileTopB, tileLeftB, stepTilesB.getTileHeight(), stepTilesB.getTileWidth(), 
                                                           imageB, interpolatedPixelDisplacements, warpingMode, TilesOrderEnum.SecondImage);
                             matrixB = imageMatrixB.copyMatrixRegion((short)0, (short)0, tileHeight, tileWidth, tileB.getMatrix());
                             tileB.setMatrix(matrixB);
                             break;
                        case BothImages:
                            tileTopB = (short)(tileB.getTopPixel() + FastMath.floor(tileB.getDisplacementU()/2.0f));
                            tileLeftB = (short)(tileB.getLeftPixel() + FastMath.floor(tileB.getDisplacementV()/2.0f));
                            uOffset = (float)(tileB.getDisplacementU()/2.0f - FastMath.floor(tileB.getDisplacementU()/2.0f));
                            vOffset = (float)(tileB.getDisplacementV()/2.0f - FastMath.floor(tileB.getDisplacementV()/2.0f));
                            short tileTopRefB = (short)(tileB.getTopPixel() + FastMath.floor(tileB.getDisplacementU()));
                            short tileLeftRefB = (short)(tileB.getLeftPixel() + FastMath.floor(tileB.getDisplacementV()));

                            if (miniWarpingMode) {
                               interpolatedPixelDisplacements = createMiniDisplacements(stepTilesB, tileB, tileTopRefB, tileLeftRefB, interpolatedPixelDisplacements); 
                            } else {
                               interpolatedPixelDisplacements = createMicroDisplacements(stepTilesB, tileB, interpolatedPixelDisplacements);
                            }
                            imageMatrixB = doBiLinearWarp(imageMatrixB, uOffset, vOffset, 
                                                          tileTopB, tileLeftB, stepTilesB.getTileHeight(), stepTilesB.getTileWidth(), 
                                                          imageB, interpolatedPixelDisplacements, warpingMode, TilesOrderEnum.SecondImage);
                            matrixB = imageMatrixB.copyMatrixRegion((short)0, (short)0, tileHeight, tileWidth, tileB.getMatrix());
                            tileB.setMatrix(matrixB);
                            break;
                        default:
                            throw new ImageClippingException("Unknown BiLinear image warping mode: " + warpingMode.toString());
                        }                       
                    } else {
                        Matrix matrixA = imageA.clipImageMatrix(tileA.getTopPixel(), tileA.getLeftPixel(), tileHeight, tileWidth, false, tileA.getMatrix());
                        tileA.setMatrix(matrixA);

                        Matrix matrixB = imageB.clipImageMatrix(tileB.getTopPixel(), tileB.getLeftPixel(), tileHeight, tileWidth, false, tileB.getMatrix());
                        tileB.setMatrix(matrixB);
                    }

 
                }  catch (ImageStateException ex) {
                    throw new InvalidStateException("Failed to clip tile at [I: " + tileA.getTileIndexI() + ", J: " + tileA.getTileIndexJ() + 
                            "] - Displacement: [U: " + tileB.getDisplacementU() + ", V: " + tileB.getDisplacementV() + "] - [Top: " + tileB.getTopPixel() + ", Left: " + tileB.getLeftPixel() + "]", ex);
                }
            }
        }   
    }

    private float[][][] createMicroDisplacements(final IterationStepTiles stepTiles, final Tile tile, float[][][] interpolatedResults) {
        if (interpolatedResults == null || interpolatedResults.length < stepTiles.getTileHeight() || interpolatedResults[0].length < stepTiles.getTileWidth()) {
            interpolatedResults = new float[stepTiles.getTileHeight()][stepTiles.getTileWidth()][2];
        }
        
        for (int i = 0; i < stepTiles.getTileHeight(); i++) {
            for (int j = 0; j < stepTiles.getTileWidth(); j++) {
                interpolatedResults[i][j][0] = tile.getDisplacementU() - (float)FastMath.floor(tile.getDisplacementU()); 
                interpolatedResults[i][j][1] = tile.getDisplacementV() - (float)FastMath.floor(tile.getDisplacementV());
            }
        }
        
        return interpolatedResults;
    }
    
    private float[][][] createMiniDisplacements(final IterationStepTiles stepTiles, final Tile tile, short tileTop, short tileLeft, float[][][] interpolatedResults) {
        if (interpolatedResults == null || interpolatedResults.length < stepTiles.getTileHeight() || interpolatedResults[0].length < stepTiles.getTileWidth()) {
            interpolatedResults = new float[stepTiles.getTileHeight()][stepTiles.getTileWidth()][2];
        }

        final int radius = 7;
        Tile[][] tiles = new Tile[radius][radius];
        tiles[radius / 2][radius / 2] = tile;
        
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i == radius / 2 && j == radius / 2) {
                    continue;
                }
                
                int offsetI = i - radius / 2;
                int offsetJ = j - radius / 2;
                
                if (tile.getTileIndexI() >= -offsetI && tile.getTileIndexI() < stepTiles.getNumberOfTilesInI() - offsetI &&
                    tile.getTileIndexJ() >= -offsetJ && tile.getTileIndexJ() < stepTiles.getNumberOfTilesInJ() - offsetJ) {
                    tiles[i][j] = stepTiles.getTile(tile.getTileIndexI() + offsetI, tile.getTileIndexJ() + offsetJ);
                }
            }
        }
        
        int top = 0;
        int left = 0;
        boolean foundA = false;
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (tiles[i][j] != null) {
                    top = i;
                    left = j;
                    foundA = true;
                    break;
                }
            }
            if (foundA) {
                break;
            }            
        }

        int bottom = radius - 1;
        int right = radius - 1;
        boolean foundB = false;
        for (int i = radius - 1; i >= 0; i--) {
            for (int j = radius - 1; j >= 0; j--) {
                if (tiles[i][j] != null) {
                    foundB = true;
                    bottom = i;
                    right = j;
                    break;
                }
            }
            if (foundB) {
                break;
            }            
        }

        if (!foundA || !foundB) {
            throw new InvalidStateException("Could not find tiles to interpolate");
        }

        int dimI = bottom - top + 1;
        int dimJ = right - left + 1;

        double[][] us = new double[dimI][dimJ];
        double[][] vs = new double[dimI][dimJ];
        double[] ys = new double[dimI];
        double[] xs = new double[dimJ];

        for (int i = top; i <= bottom; i++) {
            for (int j = left; j <= right; j++) {
                if (j == left) {
                    ys[i - top] = tiles[i][j].getTopPixel() + stepTiles.getTileHeight() / 2.0f - 0.5f;
                }
                if (i == top) {
                    xs[j - left] = tiles[i][j].getLeftPixel() + stepTiles.getTileWidth() / 2.0f - 0.5f;
                }
                us[i - top][j - left] = tiles[i][j].getDisplacementU() - FastMath.floor(tile.getDisplacementU());
                vs[i - top][j - left] = tiles[i][j].getDisplacementV() - FastMath.floor(tile.getDisplacementV());
            }
        }

        BiCubicSplineInterpolatorWithBiLinearBackup interpolator = BiCubicSplineInterpolatorWithBiLinearBackup.createDisplacementInterpolator(ys, xs, us, vs);
        interpolatedResults = interpolator.interpolateDisplacements(tileTop, tileLeft, stepTiles.getTileHeight(), stepTiles.getTileWidth(), interpolatedResults);
        return interpolatedResults;
    }

    private final Matrix doBiLinearWarp(Matrix imageMatrix, float uOffset, float vOffset, 
                                      final short tileTop, final short tileLeft, final short tileHeight, final short tileWidth, 
                                      final IImage image,
                                      float[][][] interpolatedPixelsDisplacements,
                                      final BiLinearImageWarpingModeEnum warpingMode, final TilesOrderEnum imageOrder) {
        if (imageMatrix == null || imageMatrix.getHeight() != tileHeight || imageMatrix.getWidth() != tileWidth) {
            imageMatrix = new MatrixFloat(tileHeight, tileWidth, image.getMaximumValue());
        }
        imageMatrix.zeroMatrix();
        
        //Apply shifts/warping to image - Move pixels from their original positions (i,j) to their new displaced destination
        //Create new image considering only integer displacements
        //
        //The Java image pixels coordinate system is like this:
        //
        //Top-Left image corner
        //        (0,0) -------------> x
        //          |
        //          |
        //          |
        //          |
        //          |
        //          v
        //          y
        //
        
        for (short i = 0; i < imageMatrix.getHeight(); i++) {
            for (short j = 0; j < imageMatrix.getWidth(); j++) {      
                float u = interpolatedPixelDisplacements[i][j][0];
                float v = interpolatedPixelDisplacements[i][j][1];
                
                if (warpingMode == BiLinearImageWarpingModeEnum.BothImages) {                    
                    u = u / 2.0f + uOffset;
                    v = v / 2.0f + vOffset;
                }
                                
                float finalValue = warpPixelOnImage(image, u, v, (short)(i + tileTop), (short)(j + tileLeft), imageOrder);
                imageMatrix.setElement(finalValue, i, j);
            }
        }
        
        return imageMatrix;
    }

    private final float warpPixelOnImage(final IImage image, final float u, final float v, 
                                        final short i, final short j, final TilesOrderEnum order) {
        float deltaU;
        float deltaV;
                        
        //to target coordinates in warping buffer        
        float sourceI = i + u;
        float sourceJ = j + v;
        if (order == TilesOrderEnum.FirstImage) {
            sourceI = i - u;
            sourceJ = j - v;            
        }
        short sourcePixelI = (short)FastMath.round(sourceI);
        short sourcePixelJ = (short)FastMath.round(sourceJ);

        if (sourcePixelI < 0) {
            sourceI = 0;
            sourcePixelI = 0;
        } else if (sourcePixelI >= image.getHeight()) {
            sourceI = image.getHeight() - 1;
            sourcePixelI = (short)(image.getHeight() -  1);
        }
        
        if (sourcePixelJ < 0) {
            sourceJ = 0;
            sourcePixelJ = 0;
        } else if (sourcePixelJ >= image.getWidth()) {
            sourceJ = image.getWidth() - 1;
            sourcePixelJ = (short)(image.getWidth() - 1);
        }
           
        deltaU = sourceI - sourcePixelI;
        deltaV = sourceJ - sourcePixelJ;
        
        short offsetMapI = deltaU >= 0 ? (short)1 : (short)-1;
        short offsetMapJ = deltaV >= 0 ? (short)1 : (short)-1;
        if (offsetMapI + sourcePixelI < 0 || offsetMapI + sourcePixelI >= image.getHeight()) {
            offsetMapI = (short)0;
        }
        if (offsetMapJ + sourcePixelJ < 0 || offsetMapJ + sourcePixelJ >= image.getWidth()) {
            offsetMapJ = (short)0;
        }
        
        //We can now get rid of the sign, and turn the fractional displacement into a weight value 
        float weightU = FastMath.abs(deltaU);
        float weightV = FastMath.abs(deltaV);
        
        if (weightU > 1.0f || weightU < 0.0f) {
            throw new ImageWarpingException("Invalid weight for U component: " + weightU);
        }
        
        if (weightV > 1.0f || weightV < 0.0) {
            throw new ImageWarpingException("Invalid weight for V component: " + weightV);
        }
        
        //Read from the integer locations of the 4 pixel corners and move the weighted fractional contribution 
        //from each of these 4 corners to the target pixel location.
        
        float valueTopLeft = image.readPixel(sourcePixelI, sourcePixelJ);
        float valueTopRight = image.readPixel(sourcePixelI, sourcePixelJ + offsetMapJ);
        float valueBottomLeft = image.readPixel(sourcePixelI + offsetMapI, sourcePixelJ);
        float valueBottomRight = image.readPixel(sourcePixelI + offsetMapI, sourcePixelJ + offsetMapJ);
           
        float finalValue = (1.0f - weightU) * (1.0f - weightV) * valueTopLeft +
                           (1.0f - weightU) *         weightV  * valueTopRight +
                                   weightU  * (1.0f - weightV) * valueBottomLeft +
                                   weightU  *         weightV  * valueBottomRight;

        finalValue = FastMath.round(finalValue);
                
        return finalValue;
    }
}