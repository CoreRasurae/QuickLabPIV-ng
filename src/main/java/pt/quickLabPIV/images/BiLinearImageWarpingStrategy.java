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

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.BiCubicSplineInterpolatorWithBiLinearBackup;
import pt.quickLabPIV.iareas.InvalidStateException;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TileStableStateEnum;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public final class BiLinearImageWarpingStrategy implements IImageWarpingStrategy {
    private static Logger logger = LoggerFactory.getLogger(BiLinearImageWarpingStrategy.class);
    
    private float[][][] interpolatedPixelDisplacements = null;
    private Matrix imageMatrixA = null;
    private Matrix imageMatrixB = null;
    private BiLinearImageWarpingModeEnum warpingMode;
    
    public BiLinearImageWarpingStrategy(BiLinearImageWarpingModeEnum mode) {
        warpingMode = mode;
    }
    
    //FIXME This is likely to not work properly when margins are set...?? or do Tiles account for the margins?
    
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
        
        if (stepTilesB.getCurrentStep() > 0) {
           BiCubicSplineInterpolatorWithBiLinearBackup interpolator = BiCubicSplineInterpolatorWithBiLinearBackup.createTileDisplacementInterpolator(stepTilesB);//.getParentStepTiles()); 
           interpolatedPixelDisplacements = interpolator.interpolateDisplacements(0, 0, imageWidth, imageHeight, interpolatedPixelDisplacements);
        }
        
        //Perform warping before anything else
        if (warpingMode == BiLinearImageWarpingModeEnum.SecondImage) {
            //TODO Optimize pool by adaptive step           
            //imageMatrixB = new MatrixFloat(imageHeight, imageWidth, imageA.getMaximumValue());
            imageMatrixB = imageB.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixB);
            if (stepTilesB.getCurrentStep() > 0) {
                imageMatrixB.zeroMatrix();
                //There is no need to perform warping on the first level of iteration for the step tiles, since displacements are all 0
                doBiLinearWarp(imageA, imageB, warpingMode);
                //Image im = new Image(imageMatrixB, imageA.getWidth(), imageA.getHeight(), "warped2nd_Step" + String.format("%02d", stepTilesB.getCurrentStep()) + ".tif");
                //im.writeToFile(true);
            } else {
                //imageMatrixB = imageB.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixB);
            }
        } else if (warpingMode == BiLinearImageWarpingModeEnum.FirstImage) {
            //imageMatrixA = new MatrixFloat(imageHeight, imageWidth, imageA.getMaximumValue());
            imageMatrixA = imageA.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixA);
            if (stepTilesA.getCurrentStep() > 0) {
                imageMatrixA.zeroMatrix();
                //There is no need to perform warping on the first level of iteration for the step tiles, since displacements are all 0
                doBiLinearWarp(imageA, imageB, warpingMode);
                //Image im = new Image(imageMatrixA, imageA.getWidth(), imageA.getHeight(), "warped1st_Step" + String.format("%02d", stepTilesB.getCurrentStep()) + ".tif");
                //im.writeToFile(true);
            } else {
                //imageMatrixA = imageA.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixA);
            }
        } else if (warpingMode == BiLinearImageWarpingModeEnum.BothImages) {
            //imageMatrixA = new MatrixFloat(imageHeight, imageWidth, imageA.getMaximumValue());
            //imageMatrixB = new MatrixFloat(imageHeight, imageWidth, imageA.getMaximumValue());
            imageMatrixA = imageA.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixA);
            imageMatrixB = imageB.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixB);
            if (stepTilesA.getCurrentStep() > 0 || stepTilesB.getCurrentStep() > 0) {
                imageMatrixA.zeroMatrix();
                imageMatrixB.zeroMatrix();
                //There is no need to perform warping on the first level of iteration for the step tiles, since displacements are all 0
                doBiLinearWarp(imageA, imageB, warpingMode);
            } else {
                //imageMatrixA = imageA.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixA);
                //imageMatrixB = imageB.clipImageMatrix(0, 0, imageHeight, imageWidth, false, imageMatrixB);                
            }
        } else {
            throw new ImageClippingException("Unknown BiLinear image warping mode: " + warpingMode.toString());
        }
        
        for (int tileI = 0; tileI < stepTilesB.getNumberOfTilesInI(); tileI++) {
            for (int tileJ = 0; tileJ < stepTilesB.getNumberOfTilesInJ(); tileJ++) {
                Tile tileA = stepTilesA.getTile(tileI, tileJ);
                Tile tileB = stepTilesB.getRelatedTile(tileA);
                
                if (tileA.getStableState() != TileStableStateEnum.EVALUATING || tileB.getStableState() != TileStableStateEnum.EVALUATING) {
                    continue;
                }
                
                try {
                    switch (warpingMode) {
                    case SecondImage:
                        Matrix matrixA = imageA.clipImageMatrix(tileA.getTopPixel(), tileA.getLeftPixel(), tileHeight, tileWidth, false, tileA.getMatrix());                   
                        tileA.setMatrix(matrixA);
                        break;
                    case FirstImage:
                    case BothImages:
                        matrixA = imageMatrixA.copyMatrixRegion(tileA.getTopPixel(), tileA.getLeftPixel(), tileHeight, tileWidth, tileA.getMatrix());                   
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
                    case BothImages:
                        matrixB = imageMatrixB.copyMatrixRegion(tileB.getTopPixel(), tileB.getLeftPixel(), tileHeight, tileWidth, tileB.getMatrix());
                        tileB.setMatrix(matrixB);
                        break;
                    default:
                        throw new ImageClippingException("Unknown BiLinear image warping mode: " + warpingMode.toString());
                    }
                }  catch (ImageStateException ex) {
                    throw new InvalidStateException("Failed to clip tile at [I: " + tileA.getTileIndexI() + ", J: " + tileA.getTileIndexJ() + 
                            "] - Displacement: [U: " + tileB.getDisplacementU() + ", V: " + tileB.getDisplacementV() + "] - [Top: " + tileB.getTopPixel() + ", Left: " + tileB.getLeftPixel() + "]", ex);
                }
            }
        }   
    }
        
    private final void doBiLinearWarp(final IImage imageA, final IImage imageB, final BiLinearImageWarpingModeEnum imageOrder) {
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
        short occurrencesMapA[][] = new short[imageA.getHeight()][imageA.getWidth()];
        short occurrencesMapB[][] = new short[imageA.getHeight()][imageA.getWidth()];
        
        for (short i = 0; i < imageA.getHeight(); i++) {
            for (short j = 0; j < imageA.getWidth(); j++) {      
                float u = interpolatedPixelDisplacements[i][j][0];
                float v = interpolatedPixelDisplacements[i][j][1];
                
                if (warpingMode == BiLinearImageWarpingModeEnum.BothImages) {
                    u /= 2.0f;
                    v /= 2.0f;
                }
                
                if (warpingMode == BiLinearImageWarpingModeEnum.BothImages || warpingMode == BiLinearImageWarpingModeEnum.FirstImage) {
                    float finalValue = warpPixelOnImage(imageA, u, v, i, j, TilesOrderEnum.FirstImage, occurrencesMapA);
                    imageMatrixA.setElement(finalValue, i, j);
                }
                
                if (warpingMode == BiLinearImageWarpingModeEnum.BothImages || warpingMode == BiLinearImageWarpingModeEnum.SecondImage) {
                    float finalValue = warpPixelOnImage(imageB, u, v, i, j, TilesOrderEnum.SecondImage, occurrencesMapB);
                    imageMatrixB.setElement(finalValue, i, j);                    
                }
            }
        }        
    }

    private final float warpPixelOnImage(final IImage image, final float u, final float v, 
                                        final short i, final short j, final TilesOrderEnum order, short[][] map) {
        float deltaU;
        float deltaV;
        
        //Transfer pixel to target coordinates in target image
        short targetI = i;
        short targetJ = j;
                
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
        
        map[targetI][targetJ]++;
        if (map[targetI][targetJ] > 1) {
            throw new ImageWarpingException("The target pixel should only be assigned once with backward warping: [I=" + (int)targetI + ", J=" + (int)targetJ + "]");
        }
        
        return finalValue;
    }
}