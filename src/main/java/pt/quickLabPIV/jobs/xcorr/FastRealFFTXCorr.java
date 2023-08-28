// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import java.util.Arrays;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;

public class FastRealFFTXCorr {
    static public float[][] computeXCorr(float[][] matA, float[][] matB) {
        final int dimI = 2*matA.length;
        final int dimJ = 2*matA[0].length;
        
        float tr[][] = new float[dimI][dimJ];
        float ti[][] = new float[dimI][dimJ];
        for (int i = 0; i < dimI; i++) {
            for (int j = 0; j < dimJ; j++) {
                if (i < matA.length && j < matA[0].length) {
                    tr[i][j] = matA[matA.length - 1 - i][matA[0].length - 1 - j];
                    ti[i][j] = matB[i][j];
                } else {
                    tr[i][j] = 0.0f;
                    ti[i][j] = 0.0f;
                }
            }
        }
        
        commonRealFFTXCorr(dimI, dimJ, tr, ti);
        
        float[][] result = new float[dimI-1][dimJ-1]; 
        
        for (int i = 0; i < dimI-1; i++) {
            result[i] = Arrays.copyOf(tr[i], dimJ-1);
        }

        return result;        
    }
    
    static public Matrix computeXCorr(Matrix matA, Matrix matB) {
        final int dimI = 2*matA.getHeight();
        final int dimJ = 2*matA.getWidth();
        
        float tr[][] = new float[dimI][dimJ];
        float ti[][] = new float[dimI][dimJ];
        for (int i = 0; i < dimI; i++) {
            for (int j = 0; j < dimJ; j++) {
                if (i < matA.getHeight() && j < matA.getWidth()) {
                    tr[i][j] = matA.getElement(matA.getHeight() - 1 - i, matA.getWidth() - 1 - j) / matA.getMaxValue() * 16.0f + 1.0f;
                    ti[i][j] = matB.getElement(i, j) / matA.getMaxValue() * 16.0f + 1.0f;
                } else {
                    tr[i][j] = 0.0f;
                    ti[i][j] = 0.0f;
                }
            }
        }
        
        tr = commonRealFFTXCorr(dimI, dimJ, tr, ti);
        
        Matrix matResult = new MatrixFloat(dimI - 1, dimJ - 1);
        
        matResult.copyMatrixFrom2DArray(tr, 0, 0);
        return matResult;
    }
    
    private static float[][] commonRealFFTXCorr(final int dimI, final int dimJ, float[][] tr, float[][] ti) {
        SimpleFFT fftReal = new SimpleFFT(dimI, dimJ);
        fftReal.computeFFT2D(tr, ti);
        for (int i = 0; i <= dimI/2; i++) {
            for (int j = 0; j <= dimJ/2; j++) {
                int k1 = i;
                int Nk1 = (dimI - k1) % dimI;
                int k2 = j;
                int Nk2 = (dimJ - k2) % dimJ;

                final float Gr = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                final float Gi = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                
                final float Fr = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                final float Fi = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);                               
                
                //Complex product
                //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)                
                tr[k1][k2] = Fr * Gr - Fi * Gi;
                ti[k1][k2] = Fr * Gi + Fi * Gr;
                tr[Nk1][Nk2] = tr[k1][k2];
                ti[Nk1][Nk2] = -ti[k1][k2];
                
                if (k1 != 0 && k2 != 0 && k1 < dimI/2 && k2 < dimJ/2) {
                    k1 = i + dimI/2;
                    Nk1 = (dimI - k1) % dimI;
                    
                    final float Gr2 = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                    final float Gi2 = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                               
                    final float Fr2 = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]); 
                    final float Fi2 = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                                        
                    //Complex product
                    //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)                
                    tr[k1][k2] = Fr2 * Gr2 - Fi2 * Gi2;
                    ti[k1][k2] = Fr2 * Gi2 + Fi2 * Gr2;
                    tr[Nk1][Nk2] = tr[k1][k2];
                    ti[Nk1][Nk2] = -ti[k1][k2];
                }
            }
            
        }
        
        fftReal.computeIFFT2D(tr, ti);
        
        return tr;
    }   

}
