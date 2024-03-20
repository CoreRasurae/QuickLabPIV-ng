// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas.replacement;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.iareas.InterpolateException;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class BilinearReplacement implements IVectorReplacement {
    private static Logger logger = LoggerFactory.getLogger(BilinearReplacement.class);
    
    private float interpV(Tile t1, Tile t2, Tile interp) {
        //Horizontal
        float uV = 0.0f;

        
        final float v1 = t1.getDisplacementV();
        final float v2 = t2.getDisplacementV();
       
        final float leftPixel1 = t1.getLeftPixel();
        final float leftPixel2 = t2.getLeftPixel();
        
        final float leftPixelInterp = interp.getLeftPixel();
        
        float weightedV1 = 0.0f;
        float weightedV2 = 0.0f;;
        if ((leftPixel2 - leftPixelInterp) > 1e-6f && (leftPixelInterp - leftPixel1) > 1e-6f && (leftPixel2 - leftPixel1) > 1e-6f) {
            final float weightV1 = (leftPixel2 - leftPixelInterp)/(leftPixel2 - leftPixel1); 
            if (weightV1 < 0.0f || weightV1 > 1.0f) {
                throw new VectorReplacementException("Invalid weightV1 obtained for tile: " + interp);
            }
            weightedV1 = weightV1*v1;

            final float weightV2 = (leftPixelInterp - leftPixel1)/(leftPixel2 - leftPixel1); 
            if (weightV2 < 0.0f || weightV2 > 1.0f) {
                throw new VectorReplacementException("Invalid weightV2 obtained for tile: " + interp);
            }
            weightedV2 = weightV2*v2;
        } else {
            int count = 0;
            if (FastMath.abs(leftPixelInterp - leftPixel2) < 1e-6f) {
                //At least one point is over the interpolation location, so lets use it as the estimate
                weightedV2 = v2;
                count++;
            }
            if (FastMath.abs(leftPixelInterp - leftPixel1) < 1e-6f) {
                //At least one point is over the interpolation location, so lets use it as the estimate
                weightedV1 = v1;
                count++;
            }
            if (count == 0) {
                //The points are not over the interpolation location and in lack of best information use the average between the two
                /*if (FastMath.abs(leftPixel1 - leftPixel2) < 1e-6f) {
                    weightedV2 = v2;
                    weightedV1 = v1;
                    count = 2;
                } else {*/
                    throw new VectorReplacementException("Unexpected corner case: don't know how to interpolate over V");
                //}            
            }
            if (count > 1) {
                weightedV1 /= (float)count;
                weightedV2 /= (float)count;
            }
        }

        uV = weightedV1 + weightedV2;
        
        return uV;      
    }

    private float interpU(Tile t1, Tile t2, Tile interp) {
        //Vertical
        float uU = 0.0f;

        final float u1 = t1.getDisplacementU();

        final float u2 = t2.getDisplacementU();
       
        final float topPixel1 = t1.getTopPixel();
        final float topPixel2 = t2.getTopPixel();
        
        final float topPixelInterp = interp.getTopPixel();
        
        float weightedU1 = 0.0f;
        float weightedU2 = 0.0f;
        if ((topPixel2- topPixelInterp) > 1e-6f && (topPixelInterp - topPixel1) > 1e-6f && (topPixel2 - topPixel1) > 1e-6f) {
            final float weightU1 = (topPixel2 - topPixelInterp)/(topPixel2 - topPixel1); 
            if (weightU1 < 0.0f || weightU1 > 1.0f) {
                throw new VectorReplacementException("Invalid weightU1 obtained for tile: " + interp);
            }
            weightedU1 = weightU1*u1;

            final float weightU2 = (topPixelInterp - topPixel1)/(topPixel2 - topPixel1);
            if (weightU2 < 0.0f || weightU2 > 1.0f) {
                throw new VectorReplacementException("Invalid weightU2 obtained for tile: " + interp);
            }
            weightedU2 = weightU2*u2;

        } else {
            int count = 0;
            
            if (FastMath.abs(topPixelInterp - topPixel2) < 1e-6f) {
                //At least one point is over the interpolation location, so lets use it as the estimate
                weightedU2 = u2;
                count++;
            }
            if (FastMath.abs(topPixelInterp - topPixel1) < 1e-6f) {
                //At least one point is over the interpolation location, so lets use it as the estimate
                weightedU1 = u1;
                count++;
            }
            
            if (count == 0) {
                /*if (FastMath.abs(topPixel2 - topPixel1) < 1e-6f) {
                    //The points are not over the interpolation location and in lack of best information use the average between the two
                    weightedU2 = u2;
                    weightedU1 = u1;
                    count = 2;
                } else {*/
                    throw new VectorReplacementException("Unexpected corner case: don't know how to interpolate over U");
               // }            
            }
            if (count > 1) {
                weightedU1 /= (float)count;
                weightedU2 /= (float)count;
            }

        }

        uU = weightedU1 + weightedU2;
        
        return uU;
    }

    @Override
    public void replaceVector(boolean firstPass, int frameNumber, Tile vector, Tile[][] adjacents, List<MaxCrossResult> maxCrosses) {
        if (!vector.isInvalidDisplacement() || vector.isMaskedDisplacement()) {
            return;
        }
        
        Tile left   = adjacents[1][0];
        Tile right  = adjacents[1][2];
        Tile top    = adjacents[0][1];
        Tile bottom = adjacents[2][1];
        
        Tile topLeft     = adjacents[0][0];
        Tile bottomRight = adjacents[2][2];
        Tile bottomLeft  = adjacents[2][0];
        Tile topRight    = adjacents[0][2];
        
        final int candidatePoints = 4;
        
        int count = 0;
        float uV[] = new float[candidatePoints];
        float uU[] = new float[candidatePoints];
        int classificationU[] = new int[candidatePoints];
        int classificationV[] = new int[candidatePoints];
        Arrays.fill(uV, 0.0f);
        Arrays.fill(uU, 0.0f);
        Arrays.fill(classificationU, 0);
        Arrays.fill(classificationV, 0);

        if (topLeft != null && bottomLeft != null && topLeft.getLeftPixel() - bottomLeft.getLeftPixel() > 1.0e-6f) {
            throw new VectorReplacementException("Unexpected horizontal-left tile alignment");
        }
        
        if (topRight != null && bottomRight != null && topRight.getLeftPixel() - bottomRight.getLeftPixel() > 1.0e-6f) {
            throw new VectorReplacementException("Unexpected horizontal-right tile alignment");
        }
        
        if (topLeft != null && topRight != null && topLeft.getTopPixel() - topRight.getTopPixel() > 1.0e-6f) {
            throw new VectorReplacementException("Unexpected vertical-top tile alignment");
        }
        
        if (bottomLeft != null && bottomRight != null && bottomLeft.getTopPixel() - bottomRight.getTopPixel() > 1.0e-6f) {
            throw new VectorReplacementException("Unexpected vertical-bottom tile alignment");
        }
        
        if (left != null && !left.isInvalidDisplacement() && right != null && !right.isInvalidDisplacement()) {
            uV[0] = interpV(left, right, vector);
            uU[0] = interpU(left, right, vector);
            classificationV[0] = 2; //Good estimate for V
            classificationU[0] = 1; //Poor estimate for U
            count++;
        }
        
        if (bottom != null && !bottom.isInvalidDisplacement() && top != null && !top.isInvalidDisplacement()) {
            uV[1] = interpV(top, bottom, vector);
            uU[1] = interpU(top, bottom, vector);
            classificationV[1] = 1; //Poor estimate for V
            classificationU[1] = 2; //Good estimate for U
            count++;
        }
        
        if (topLeft != null && !topLeft.isInvalidDisplacement() && bottomRight != null && !bottomRight.isInvalidDisplacement() &&
            bottomLeft != null && !bottomLeft.isInvalidDisplacement() && topRight != null && !topRight.isInvalidDisplacement()) {
            
            float weightTopRight = (float)(vector.getLeftPixel() - topLeft.getLeftPixel()) / (float)(topRight.getLeftPixel() - topLeft.getLeftPixel());
            if (weightTopRight < 0.0f || weightTopRight > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, top right weight: " + weightTopRight);
            }
            
            float weightTopLeft = (float)(topRight.getLeftPixel() - vector.getLeftPixel()) / (float)(topRight.getLeftPixel() - topLeft.getLeftPixel());
            if (weightTopLeft < 0.0f || weightTopLeft > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, top left weight: " + weightTopLeft);
            }

            float weightBottomRight = (float)(vector.getLeftPixel() - bottomLeft.getLeftPixel()) / (float)(bottomRight.getLeftPixel() - bottomLeft.getLeftPixel());
            if (weightBottomRight < 0.0f || weightBottomRight > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, bottom right weight: " + weightBottomRight);
            }

            float weightBottomLeft = (float)(bottomRight.getLeftPixel() - vector.getLeftPixel()) / (float)(bottomRight.getLeftPixel() - bottomLeft.getLeftPixel());
            if (weightBottomLeft < 0.0f || weightBottomLeft > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, bottom left weight: " + weightBottomLeft);
            }
            
            float weightTop = (float)(bottomLeft.getTopPixel() - vector.getTopPixel()) / (float)(bottomLeft.getTopPixel() - topLeft.getTopPixel());
            if (weightTop < 0.0f || weightTop > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, top weight: " + weightTop);
            }
            
            float weightBottom = (float)(vector.getTopPixel() - topLeft.getTopPixel()) / (float)(bottomLeft.getTopPixel() - topLeft.getTopPixel());
            if (weightBottom < 0.0f || weightBottom > 1.0f ) {
                throw new VectorReplacementException("Invalid weight for BiLinear interpolation, bottom weight: " + weightBottom);
            }
            
            if (FastMath.abs(1.0f - weightTopRight - weightTopLeft) > 1.0e-6) {
                throw new VectorReplacementException("Invalid combination of top-right and top-left weights: " + (weightTopRight + weightTopLeft));
            }
            
            if (FastMath.abs(1.0f - weightBottomRight - weightBottomLeft) > 1.0e-6) {
                throw new VectorReplacementException("Invalid combination of bottom-right and bottom-left weights: " + (weightBottomRight + weightBottomLeft));
            }
            
            if (FastMath.abs(1.0f - weightTop - weightBottom) > 1.0e-6 ) {
                throw new VectorReplacementException("Invalid combination of top and bottom weights: " + (weightTop + weightBottom));
            }
            
            uV[2] = weightTop*(weightTopRight*topRight.getDisplacementV() + weightTopLeft*topLeft.getDisplacementV()) +
                    weightBottom*(weightBottomRight*bottomRight.getDisplacementV() + weightBottomLeft*bottomLeft.getDisplacementV());
            uU[2] = weightTop*(weightTopRight*topRight.getDisplacementU() + weightTopLeft*topLeft.getDisplacementU()) +
                    weightBottom*(weightBottomRight*bottomRight.getDisplacementU() + weightBottomLeft*bottomLeft.getDisplacementU());
            classificationV[2] = 3; //Very good estimate for V
            classificationU[2] = 3; //Very good estimate for U
            count++;
        } else {
            if (topLeft != null && !topLeft.isInvalidDisplacement() && bottomRight != null && !bottomRight.isInvalidDisplacement()) {
                //Cross estimates should be computed in 2D, using a line equation in 3D (x,y,f(x,y)) assuming they pass through the center, or near the center of the interpolation tile
                uV[2] = interpV(topLeft, bottomRight, vector);
                uU[2] = interpU(topLeft, bottomRight, vector);
                classificationV[2] = 1; //Poor estimate for V
                classificationU[2] = 1; //Poor estimate for U
                count++;
            }
            
            if (bottomLeft != null && !bottomLeft.isInvalidDisplacement() && topRight != null && !topRight.isInvalidDisplacement()) {
                uV[3] = interpV(bottomLeft, topRight, vector);
                uU[3] = interpU(topRight, bottomLeft, vector);
                classificationV[3] = 1; //Poor estimate for V
                classificationU[3] = 1; //Poor estimate for U
                count++;
            }    
        }
                        
        if (count == 0) {
            for (int i = 0; i < adjacents.length; i++) {
                for (int j = 0; j < adjacents[0].length; j++) {
                    if (i == 1 && j == 1) {
                        continue;
                    }
                    
                    if (adjacents[i][j] != null && !adjacents[i][j].isInvalidDisplacement()) {
                        uV[0] = adjacents[i][j].getDisplacementV();
                        uU[0] = adjacents[i][j].getDisplacementU();
                        count = 1;
                        break;
                    }
                }
            }
            
            if (count == 0) {
                logger.warn("Frame: {}, IArea: (I: {}, J: {}) - Couldn't replace invalid vector, unable to compute Bilinear interpolated vector",
                    frameNumber, vector.getTileIndexI(), vector.getTileIndexJ());
                vector.setInvalidDisplacement(true);
            } else {
                logger.warn("Frame: {}, IArea: (I: {}, J: {}) - Unable to compute Bilinear interpolated vector, but replaced with neighbor valid vector",
                        frameNumber, vector.getTileIndexI(), vector.getTileIndexJ());
                if (vector.replaceDisplacement(uU[0], uV[0])) {
                    vector.setInvalidDisplacement(false);
                }
            }
            
        } else {
            float accumV = 0.0f;
            float accumU = 0.0f;
            
            //Compute best estimate
            /*int countU = 0;
            int countV = 0;
            int bestClassificationU = 0;
            int bestClassificationV = 0;
            for (int i = 0; i < candidatePoints; i++) {
                if (classificationV[i] > bestClassificationV) {
                    bestClassificationV = classificationV[i];
                    countV = 1;
                    accumV = uV[i];
                } else if (classificationV[i] == bestClassificationV) {
                    countV++;
                    accumV += uV[i];
                }                
                
                if (classificationU[i] > bestClassificationU) {
                    bestClassificationU = classificationU[i];
                    countU = 1;
                    accumU = uU[i];
                } else if (classificationU[i] == bestClassificationU) {
                    countU++;
                    accumU += uU[i];
                }
            }
            accumU /= countU;
            accumV /= countV;

            */
            
            int weightU = 0;
            int weightV = 0;
            for (int i = 0; i < candidatePoints; i++) {
                accumV += classificationV[i] * uV[i];
                weightV += classificationV[i];
                
                accumU += classificationU[i] * uU[i];
                weightU += classificationU[i];
            }
            
            accumV /= weightV;
            accumU /= weightU;
            
            /*if (countU > 2) {
                //We have enough points to do some voting
                float distanceU[] = new float[countU];
                int indexU[] = new int[countU];
                int k = 0;
                for (int i = 1; i < candidatePoints; i++) {
                    if (bestClassificationU == classificationU[i]) {
                        distanceU[k] = FastMath.abs(uU[i] - accumU);
                        indexU[k] = i;
                        k++;
                    }
                }
                
                float minDistance = Float.MAX_VALUE;
                for (k = 0; k < countU; k++) {
                    //Select smallest distance
                    if (distanceU[k] < minDistance) {
                        minDistance = distanceU[k];
                    }
                }
                
                float ratios[] = new float[countU];
                for (k = 0; k < countU; k++) {
                    ratios[k] = distanceU[k] / minDistance;
                }
                
                accumU = 0.0f;
                countU = 0;
                for (k = 0; k < countU; k++) {
                    if (ratios[k] < 2.00f) {
                        accumU += uU[indexU[k]];
                        countU ++;
                    }
                }
                
                accumU /= countU;
            }
            
            if (countV > 2) {
                //We have enough points to do some voting
                float distanceV[] = new float[countV];
                int indexV[] = new int[countV];
                int k = 0;
                for (int i = 1; i < candidatePoints; i++) {
                    if (bestClassificationV == classificationV[i]) {
                        distanceV[k] = FastMath.abs(uV[i] - accumV);
                        indexV[k] = i;
                        k++;
                    }
                }
                
                float minDistance = Float.MAX_VALUE;
                for (k = 0; k < countV; k++) {
                    //Select smallest distance
                    if (distanceV[k] < minDistance) {
                        minDistance = distanceV[k];
                    }
                }
                
                float ratios[] = new float[countV];
                for (k = 0; k < countV; k++) {
                    ratios[k] = distanceV[k] / minDistance;
                }
                
                accumV = 0.0f;
                countV = 0;
                for (k = 0; k < countV; k++) {
                    if (ratios[k] < 2.00f) {
                        accumV += uV[indexV[k]];
                        countV ++;
                    }
                }
                
                accumV /= countV;
            }*/
            
            vector.replaceDisplacement(accumU, accumV);
            vector.setInvalidDisplacement(false);
        }
    }

}
