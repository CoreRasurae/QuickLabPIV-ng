// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.jobs.NotImplementedException;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class SimpleLucasKanadeImpl implements IOpticalFlowInterpolator {
    private float sigma;
    private int widthPx;
    private boolean avgOf4;
    private int windowSize;
    private int iterations;
    private IFilter filter;
    
    private IImage imgA;
    private IImage imgB;
    private double[][][] patchI;
    private double[][][] patchJ;
    private double[][][] dIs;
    private double[][][] dJs;
    private double[][][] A00s;
    private double[][][] A01s;
    private double[][][] A11s;
    private double[] detAs;
    private double[][] AInv;
    private double[][][] b0s;
    private double[][][] b1s;
    private boolean[] status;
    private double[] Us;
    private double[] Vs;
    
    public SimpleLucasKanadeImpl() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Lucas-Kanade interpolator configuration");
        }
        
        LucasKanadeInterpolatorConfiguration configuration = (LucasKanadeInterpolatorConfiguration)configurationObject;
        avgOf4 = configuration.getAverageOfFourPixels();
        iterations = configuration.getNumberOfIterations();
        windowSize = configuration.geWindowSize();
        sigma = configuration.getFilterSigma();
        widthPx = configuration.getFilterWidthPx();        
        
        int reps = 1;
        if (avgOf4) {
            reps = 4;
        }
        
        filter = new GaussianFilter2D(sigma, widthPx);
        
        patchI = new double[reps][windowSize][windowSize];
        patchJ = new double[reps][windowSize][windowSize];
        dIs = new double[reps][windowSize][windowSize];
        dJs = new double[reps][windowSize][windowSize];
        A00s = new double[reps][windowSize][windowSize];
        A01s = new double[reps][windowSize][windowSize];
        A11s = new double[reps][windowSize][windowSize];
        detAs = new double[reps];
        AInv = new double[reps][3];        
        b0s = new double[reps][windowSize][windowSize];
        b1s = new double[reps][windowSize][windowSize];
        status = new boolean[reps];
        Us = new double[reps];
        Vs = new double[reps];
    }
    
    public SimpleLucasKanadeImpl(float filterSigma, int filterWidthPx, boolean _avgOf4, int _windowSize, int _iterations) {
        sigma = filterSigma;
        widthPx = filterWidthPx;
        avgOf4 = _avgOf4;
        windowSize = _windowSize;
        iterations = _iterations;
        int reps = 1;
        if (avgOf4) {
            reps = 4;
        }
        
        filter = new GaussianFilter2D(filterSigma, filterWidthPx);
        
        patchI = new double[reps][windowSize][windowSize];
        patchJ = new double[reps][windowSize][windowSize];
        dIs = new double[reps][windowSize][windowSize];
        dJs = new double[reps][windowSize][windowSize];
        A00s = new double[reps][windowSize][windowSize];
        A01s = new double[reps][windowSize][windowSize];
        A11s = new double[reps][windowSize][windowSize];
        detAs = new double[reps];
        AInv = new double[reps][3];        
        b0s = new double[reps][windowSize][windowSize];
        b1s = new double[reps][windowSize][windowSize];
        status = new boolean[reps];
        Us = new double[reps];
        Vs = new double[reps];        
    }
    
    public double getNearestPixel(IImage img, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= img.getHeight()) {
            i = img.getHeight() - 1;
        }
        
        if (j < 0) {
            j = 0;
        }
        if (j >= img.getWidth()) {
            j = img.getWidth() - 1;
        }
        
        return img.readPixel(i, j);
    }
    
    public double getNearestPixelWithWarp(IImage img, double locI, double locJ) {
        int i = (int) locI;
        int j = (int) locJ;
        
        double deltaI = locI - i;
        double deltaJ = locJ - j;
        
        if (deltaI < 0) {
            i--;
            deltaI += 1.0;
        }
        
        if (deltaJ < 0) {
            j--;
            deltaJ += 1.0;
        }
        
        double value = (1.0 - deltaI) * ((1.0 - deltaJ) * getNearestPixel(img, i  ,j) + deltaJ * getNearestPixel(img, i  ,j+1)) + 
                              deltaI  * ((1.0 - deltaJ) * getNearestPixel(img, i+1,j) + deltaJ * getNearestPixel(img, i+1,j+1));
        
        return value;
    }
    
    void getImagePatchA(double[][] patch, double[][] dIs, double dJs[][], double A00s[][], double A01s[][], double A11s[][], IImage img, double locI, double locJ) {
        for (int i = -windowSize/2; i <= windowSize/2; i++) {
            for (int j = -windowSize/2; j <= windowSize/2; j++) {
                int localI = i + windowSize/2;
                int localJ = j + windowSize/2;
                patch[localI][localJ] = getNearestPixelWithWarp(img, i + locI, j + locJ);
                if (dIs != null && dJs != null && A00s != null && A01s != null && A11s != null) {
                    double dI = 3.0 * (getNearestPixelWithWarp(img, locI + i - 1, locJ + j - 1) + getNearestPixelWithWarp(img, locI + i - 1, locJ + j + 1) -
                                       getNearestPixelWithWarp(img, locI + i + 1, locJ + j - 1) - getNearestPixelWithWarp(img, locI + i + 1, locJ + j + 1)) +
                               10.0 * (getNearestPixelWithWarp(img, locI + i - 1, locJ + j) - getNearestPixelWithWarp(img, locI + i + 1, locJ + j));
                    dI *= 1.0 / 32.0;
                    
                    double dJ = 3.0 * (getNearestPixelWithWarp(img, locI + i - 1, locJ + j - 1) + getNearestPixelWithWarp(img, locI + i + 1, locJ + j - 1) -
                                       getNearestPixelWithWarp(img, locI + i - 1, locJ + j + 1) - getNearestPixelWithWarp(img, locI + i + 1, locJ + j + 1)) +
                                10.0 * (getNearestPixelWithWarp(img, locI + i, locJ + j - 1) - getNearestPixelWithWarp(img, locI + i, locJ + j + 1));
                    dJ *= 1.0 / 32.0;
                    
                    dIs[localI][localJ] = dI;
                    dJs[localI][localJ] = dJ;
                            
                    A00s[localI][localJ] = dJ * dJ;
                    A01s[localI][localJ] = dI * dJ;
                    A11s[localI][localJ] = dI * dI;                    
                }
            }
        }
    }
    
    void getImagePatchB(double[][] patchI, double[][] patchJ, double[][] dIs, double[][] dJs, double[][] b0s, double[][] b1s, IImage img, double locI, double locJ) {
        for (int i = -windowSize/2; i <= windowSize/2; i++) {
            for (int j = -windowSize/2; j <= windowSize/2; j++) {
                int localI = i + windowSize/2;
                int localJ = j + windowSize/2;
                patchJ[localI][localJ] = getNearestPixelWithWarp(img, i + locI, j + locJ);
                
                if (b0s != null && b1s != null) {
                    double dT = patchJ[localI][localJ] - patchI[localI][localJ];
                    b0s[localI][localJ] = dT * dJs[localI][localJ];
                    b1s[localI][localJ] = dT * dIs[localI][localJ];
                }
            }
        }
    }
    
    public double reduceArray(double a[][]) {
        double accum = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                accum += a[i][j];
            }
        }
        
        return accum;
    }
    
    @Override
    public List<MaxCrossResult> interpolate(List<MaxCrossResult> results) {
        for (MaxCrossResult result : results) {
            Tile tileA = result.getTileA();
            Tile tileB = result.getTileB();
            
            IterationStepTiles stepTilesB = tileB.getParentIterationStepTiles();
            
            double vCenter = tileA.getTopPixel() + stepTilesB.getTileHeight() / 2;
            double hCenter = tileA.getLeftPixel() + stepTilesB.getTileWidth() / 2;

            if (avgOf4) {
                hCenter -= 1.0;
                vCenter -= 1.0;
            } else {
                hCenter -= 0.5;
                vCenter -= 0.5;
            }
            
            computeMatrixAInv(vCenter, hCenter);
            
            double vLocInitial = result.getMainPeakI() - (stepTilesB.getTileHeight() - 1) + tileB.getDisplacedTileTop() + stepTilesB.getTileHeight() / 2;
            double hLocInitial = result.getMainPeakJ() - (stepTilesB.getTileWidth() - 1) + tileB.getDisplacedTileLeft() + stepTilesB.getTileWidth() / 2;
            
            if (avgOf4) {
                vLocInitial -= 1.0;
                hLocInitial -= 1.0;
            } else {
                vLocInitial -= 0.5;
                hLocInitial -= 0.5;
            }
            
            double Us[] = new double[2];
            Us[0] = 0.0; //U
            Us[1] = 0.0; //V
            
            computeVs(vLocInitial, hLocInitial, Us);

            if (avgOf4) {
                Us[0] /= 4.0;
                Us[1] /= 4.0;
            }
            
            double u = Us[0] - tileB.getDisplacedTileTop() - (stepTilesB.getTileHeight() / 2 - 0.5);
            double v = Us[1] - tileB.getDisplacedTileLeft() - (stepTilesB.getTileWidth() / 2 - 0.5);
            
            result.setNthRelativeDisplacementFromVelocities(0, (float)u, (float)v);
        }
        
        return results;
    }

    private void computeVs(double vLocInitial, double hLocInitial, double[] results) {
        for (int idx = 0; idx < (avgOf4 ? 4 : 1); idx++) {
            double vLoc = vLocInitial;
            double hLoc = hLocInitial;
            
            if (idx == 1) {
                hLoc += 1.0;
            }
            
            if (idx == 2) {
                vLoc += 1.0;
            }
            
            if (idx == 3) {
                vLoc += 1.0;
                hLoc += 1.0;
            }
      
            double patchILocal[][] = patchI[idx];
            double patchJLocal[][] = patchJ[idx];
            double dIsLocal[][] = dIs[idx];
            double dJsLocal[][] = dJs[idx];
            double b0sLocal[][] = b0s[idx];
            double b1sLocal[][] = b1s[idx];
            
            for (int iter = 0; iter < iterations; iter++) {
                if (!status[idx]) {
                    break;
                }
                
                if (vLoc < 0 || vLoc >= imgA.getHeight() + windowSize/2) {
                    status[idx] = false;
                    break;
                }
                
                if (hLoc < 0 || hLoc >= imgA.getWidth() + windowSize/2) {
                    status[idx] = false;
                    break;
                }
        
                getImagePatchB(patchILocal, patchJLocal, dIsLocal, dJsLocal, b0sLocal, b1sLocal, imgB, vLoc, hLoc);
                
                double b0 = reduceArray(b0sLocal);
                double b1 = reduceArray(b1sLocal);
                
                double incU = b0 * AInv[idx][0] + b1 * AInv[idx][1];
                double incV = b0 * AInv[idx][1] + b1 * AInv[idx][2];
                if (FastMath.abs(incU) < 1e-2 && FastMath.abs(incV) < 1e-2) {
                    status[idx] = false;
                }
      
                vLoc += incV;
                hLoc += incU;
            }
            
            Vs[idx] = vLoc;
            Us[idx] = hLoc;
            
            results[0] += vLoc;
            results[1] += hLoc;
        }
    }

    private void computeMatrixAInv(double vCenter, double hCenter) {
        for (int idx = 0; idx < (avgOf4 ? 4 : 1); idx++) {
            double locI = vCenter;
            double locJ = hCenter;
            
            if (idx == 1) {
                locJ += 1.0;
            }
            if (idx == 2) {
                locI += 1.0;
            }
            if (idx == 3) {
                locI += 1;
                locJ += 1;
            }
                
            double patch[][] = patchI[idx];
            double dIsLocal[][] = dIs[idx];
            double dJsLocal[][] = dJs[idx];
            double A00sLocal[][] = A00s[idx];
            double A01sLocal[][] = A01s[idx];
            double A11sLocal[][] = A11s[idx];
            getImagePatchA(patch, dIsLocal, dJsLocal, A00sLocal, A01sLocal, A11sLocal, imgA, locI, locJ);
            
            AInv[idx][0] = reduceArray(A00sLocal);
            AInv[idx][1] = reduceArray(A01sLocal);
            AInv[idx][2] = reduceArray(A11sLocal);
            
            double detA = AInv[idx][0] * AInv[idx][2] - AInv[idx][1] * AInv[idx][1];
            if (detA < 1.192092896e-7) {
                status[idx] = false;
            } else {
                status[idx] = true;
            }
            
            detAs[idx] = detA;
            
            for (int idx2 = 0; idx2 < 3; idx2++) {
                AInv[idx][idx2] /= detA; 
            }
            
            double temp = AInv[idx][0];
            AInv[idx][0] = AInv[idx][2];
            AInv[idx][2] = temp;
            AInv[idx][1] = -AInv[idx][1];
        }
    }

    @Override
    public void updateImageA(IImage img) {
        imgA = ImageFloat.sizeFrom(img);
        imgA = filter.applyFilter(img, imgA);
    }
    
    @Override
    public void updateImageB(IImage img) {
        imgB = ImageFloat.sizeFrom(img);
        imgB = filter.applyFilter(img, imgB);
    }

    public double[][][] getImgPatchAResult() {
        return patchI;
    }
    
    public double[][][] getImgPatchBResult() {
        return patchJ;
    }

    public double[][] getA() {
        double A[][] = new double[avgOf4 ? 4 : 1][3];
        for (int idx = 0; idx < (avgOf4 ? 4 : 1); idx++) {
            A[idx][0] = reduceArray(A00s[idx]);
            A[idx][1] = reduceArray(A01s[idx]);
            A[idx][2] = reduceArray(A11s[idx]);
        }
        
        return A;
    }

    public double[] getDetA() {
        return detAs;
    }

    public double[][] getInvA() {
        return AInv;
    }

    public double[][][] getDIs() {
        return dIs;
    }

    public double[][][] getDJs() {
        return dJs;
    }
    
    public double[] getB0s() {
        double b0sRes[] = new double[b0s.length];
        
        for (int i = 0; i < b0s.length; i++) {
            b0sRes[i] = reduceArray(b0s[i]);
        }
        
        return b0sRes;
    }
    
    public double[] getB1s() {
        double b1sRes[] = new double[b1s.length];
        
        for (int i = 0; i < b1s.length; i++) {
            b1sRes[i] = reduceArray(b1s[i]);
        }
        
        return b1sRes;
    }
    
    public double[][][] getPatchI() {
        return patchI;
    }

    public double[][][] getPatchJ() {
        return patchJ;
    }

    @Override
    public void interpolate(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB) {
        throw new NotImplementedException("Method not implemented");
    }

    public double[] interpolate(double vCenter, double hCenter, double v, double u, boolean halfPixelOffset) {
        if (!avgOf4 && halfPixelOffset) {
            vCenter += 0.5;
            hCenter += 0.5;
        }

        computeMatrixAInv(vCenter, hCenter);

        double vLocInitial = vCenter + v;
        double hLocInitial = hCenter + u;        
        
        double velocities[] = new double[2];
        velocities[0] = 0.0; //V
        velocities[1] = 0.0; //U
        
        computeVs(vLocInitial, hLocInitial, velocities);

        //Discard velocities accumulation in AvgOf4 mode
        velocities[0] = velocities[0] - vCenter;
        velocities[1] = velocities[1] - hCenter;
                
        return velocities;
    }

}
