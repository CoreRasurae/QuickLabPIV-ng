// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.xcorr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;

public class FastRealFFTXCorrTests {
    private boolean dumpArray = false;
    
    @Test
    public void fastRealXCorrTestPass() {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();
    
        float[][] matrixF = new float[4][4];
        float[][] matrixG = new float[4][4];
        matrixF[0][0] = 6.0f;
        matrixF[0][1] = 6.0f;
        matrixF[0][2] = 6.0f;
        matrixF[1][0] = 7.0f;
        matrixF[1][1] = 6.0f;
        matrixF[1][2] = 6.0f;
        matrixF[2][0] = 7.0f;
        matrixF[2][1] = 7.0f;
        matrixF[2][2] = 6.0f;
        
        matrixG[0][0] = 5.0f;
        matrixG[0][1] = 5.0f;
        matrixG[0][2] = 6.0f;
        matrixG[1][0] = 6.0f;
        matrixG[1][1] = 6.0f;
        matrixG[1][2] = 7.0f;
        matrixG[2][0] = 7.0f;
        matrixG[2][1] = 7.0f;
        matrixG[2][2] = 7.0f;
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixF, 0, 0);
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixG, 0, 0);
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        float gr[][] = new float[8][8];
        float gi[][] = new float[8][8];
        float fr[][] = new float[8][8];
        float fi[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    gr[i][j] = matrixG[i][j];
                    fr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                } else {
                    gr[i][j] = 0.0f;
                    fr[i][j] = 0.0f;
                }
                gi[i][j] = 0.0f;
                fi[i][j] = 0.0f;                
            }
        }
        
        SimpleFFT fft = new SimpleFFT(8, 8);
        fft.computeFFT2D(fr, fi);
        if (dumpArray) {
            SimpleFFT.dump2DArray("FFT F Real", fr);
            SimpleFFT.dump2DArray("FFT F Imag", fi);
        }
        fft.computeFFT2D(gr, gi);
        if (dumpArray) {
            SimpleFFT.dump2DArray("FFT G Real", gr);
            SimpleFFT.dump2DArray("FFT G Imag", gi);
        }
            

        float tr[][] = new float[8][8];
        float ti[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    tr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                    ti[i][j] = matrixG[i][j];
                } else {
                    tr[i][j] = 0.0f;
                    ti[i][j] = 0.0f;
                }
            }
        }
        
        float Fr[][] = new float[8][8];
        float Fi[][] = new float[8][8];
        float Gr[][] = new float[8][8];
        float Gi[][] = new float[8][8];
        SimpleFFT fftReal = new SimpleFFT(8, 8);
        fftReal.computeFFT2D(tr, ti);
        
        if (dumpArray) {
            SimpleFFT.dump2DArray("Combined FFT Real", tr);
            SimpleFFT.dump2DArray("Combined FFT Imag", ti);
        }
        for (int i = 0; i <= gr.length/2; i++) {
            for (int j = 0; j <= gr[0].length/2; j++) {
                int k1 = i;
                int Nk1 = (gr.length - k1) % gr.length;
                int k2 = j;
                int Nk2 = (gr.length - k2) % gr.length;

                Gr[k1][k2] = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                Gi[k1][k2] = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                
                Gr[Nk1][Nk2] = Gr[k1][k2];
                Gi[Nk1][Nk2] = -Gi[k1][k2];

                Fr[k1][k2] = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                Fi[k1][k2] = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                
                Fr[Nk1][Nk2] = Fr[k1][k2];
                Fi[Nk1][Nk2] = -Fi[k1][k2];

                
                if (k1 < gr.length/2 && k2 < gr.length/2) {
                    k1 = i + gr.length/2;
                    Nk1 = (gr.length - k1) % gr.length;
                    
                    Gr[k1][k2] = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                    Gi[k1][k2] = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                    
                    Gr[Nk1][Nk2] = Gr[k1][k2];
                    Gi[Nk1][Nk2] = -Gi[k1][k2];
                    
                    Fr[k1][k2] = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                    Fi[k1][k2] = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                    
                    Fr[Nk1][Nk2] = Fr[k1][k2];
                    Fi[Nk1][Nk2] = -Fi[k1][k2];
                }
            }
        }
        if (dumpArray) {
            SimpleFFT.dump2DArray("Reconstructed F Real", Fr);
            SimpleFFT.dump2DArray("Reconstructed F Imag", Fi);
    
            SimpleFFT.dump2DArray("Reconstructed G Real", Gr);
            SimpleFFT.dump2DArray("Reconstructed G Imag", Gi);
        }
            
        for (short i = 0; i < fr.length; i++) {
            for (short j = 0; j < fr[0].length; j++) {
                assertEquals("Computed FFT value for Fr" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", fr[i][j], Fr[i][j], 1e-5);
                assertEquals("Computed FFT value for Fi" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", fi[i][j], Fi[i][j], 1e-5);
                assertEquals("Computed FFT value for Gr" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", gr[i][j], Gr[i][j], 1e-5);
                assertEquals("Computed FFT value for Gi" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", gi[i][j], Gi[i][j], 1e-5);
            }
        }

        for (short i = 0; i < fr.length; i++) {
            for (short j = 0; j < fr[0].length; j++) {
                //Complex product
                //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
                float tempRe = Fr[i][j] * Gr[i][j] - Fi[i][j] * Gi[i][j]; 
                float tempIm = Fr[i][j] * Gi[i][j] + Fi[i][j] * Gr[i][j];
                Fr[i][j] = tempRe;
                Fi[i][j] = tempIm;
            }
        }

        if (dumpArray) {
            SimpleFFT.dump2DArray("Matrix real for XCorr IFFT", Fr);
            SimpleFFT.dump2DArray("Matrix real for XCorr IFFT", Fi);
        }
           
        fftReal.computeIFFT2D(Fr, Fi);

        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < Fr.length - 1; i++) {
            for (short j = 0; j < Fr[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), Fr[i][j], 1e-3);
            }
        }
    }

    
    @Test
    public void smallFootprintFastRealXCorrPass() {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();
    
        float[][] matrixF = new float[4][4];
        float[][] matrixG = new float[4][4];
        matrixF[0][0] = 6.0f;
        matrixF[0][1] = 6.0f;
        matrixF[0][2] = 6.0f;
        matrixF[1][0] = 7.0f;
        matrixF[1][1] = 6.0f;
        matrixF[1][2] = 6.0f;
        matrixF[2][0] = 7.0f;
        matrixF[2][1] = 7.0f;
        matrixF[2][2] = 6.0f;
        
        matrixG[0][0] = 5.0f;
        matrixG[0][1] = 5.0f;
        matrixG[0][2] = 6.0f;
        matrixG[1][0] = 6.0f;
        matrixG[1][1] = 6.0f;
        matrixG[1][2] = 7.0f;
        matrixG[2][0] = 7.0f;
        matrixG[2][1] = 7.0f;
        matrixG[2][2] = 7.0f;
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixF, 0, 0);
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixG, 0, 0);
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        float gr[][] = new float[8][8];
        float gi[][] = new float[8][8];
        float fr[][] = new float[8][8];
        float fi[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    gr[i][j] = matrixG[i][j];
                    fr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                } else {
                    gr[i][j] = 0.0f;
                    fr[i][j] = 0.0f;
                }
                gi[i][j] = 0.0f;
                fi[i][j] = 0.0f;                
            }
        }
        
        SimpleFFT fft = new SimpleFFT(8, 8);
        fft.computeFFT2D(fr, fi);
        if (dumpArray) {
            SimpleFFT.dump2DArray("FFT F Real", fr);
            SimpleFFT.dump2DArray("FFT F Imag", fi);
        }
        fft.computeFFT2D(gr, gi);
        if (dumpArray) {
            SimpleFFT.dump2DArray("FFT G Real", gr);
            SimpleFFT.dump2DArray("FFT G Imag", gi);
        }

        float tr[][] = new float[8][8];
        float ti[][] = new float[8][8];
        for (int i = 0; i < gr.length; i++) {
            for (int j = 0; j < gr[0].length; j++) {
                if (i < matrixG.length && j < matrixG[0].length) {
                    tr[i][j] = matrixF[matrixF.length-1 - i][matrixF[0].length-1 - j];
                    ti[i][j] = matrixG[i][j];
                } else {
                    tr[i][j] = 0.0f;
                    ti[i][j] = 0.0f;
                }
            }
        }
        
        SimpleFFT fftReal = new SimpleFFT(8, 8);
        fftReal.computeFFT2D(tr, ti);
        for (int i = 0; i <= gr.length/2; i++) {
            for (int j = 0; j <= gr[0].length/2; j++) {
                int k1 = i;
                int Nk1 = (gr.length - k1) % gr.length;
                int k2 = j;
                int Nk2 = (gr.length - k2) % gr.length;

                final float Gr = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                final float Gi = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
                
                /*final float NGr = Gr;
                final float NGi = -Gi;*/

                final float Fr = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]);
                final float Fi = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);                               
                
                /*final float NFr = Fr;
                final float NFi = -Fi;*/
                
                //Complex product
                //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)                
                tr[k1][k2] = Fr * Gr - Fi * Gi;
                ti[k1][k2] = Fr * Gi + Fi * Gr;
                tr[Nk1][Nk2] = tr[k1][k2];
                ti[Nk1][Nk2] = -ti[k1][k2];
                
                if (k1 != 0 && k2 != 0 && k1 < gr.length/2 && k2 < gr.length/2) {
                    k1 = i + gr.length/2;
                    Nk1 = (gr.length - k1) % gr.length;
                    
                    final float Gr2 = 0.5f * (ti[k1][k2] + ti[Nk1][Nk2]);
                    final float Gi2 = -0.5f * (tr[k1][k2] - tr[Nk1][Nk2]);
           
                    /*final float NGr = Gr2;
                    final float NGi = -Gi2;*/
                    
                    final float Fr2 = 0.5f * (tr[k1][k2] + tr[Nk1][Nk2]); 
                    final float Fi2 = 0.5f * (ti[k1][k2] - ti[Nk1][Nk2]);
                    
                    /*final float NFr2 = Fr2;
                    final float NFi2 = -Fi2;*/
                    
                    //Complex product
                    //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)                
                    tr[k1][k2] = Fr2 * Gr2 - Fi2 * Gi2;
                    ti[k1][k2] = Fr2 * Gi2 + Fi2 * Gr2;
                    tr[Nk1][Nk2] = tr[k1][k2];
                    ti[Nk1][Nk2] = -ti[k1][k2];
                }
            }
        }
        
        if (dumpArray) {
            SimpleFFT.dump2DArray("Matrix real for XCorr IFFT", tr);
            SimpleFFT.dump2DArray("Matrix real for XCorr IFFT", ti);
        }
        
        fftReal.computeIFFT2D(tr, ti);

        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < tr.length - 1; i++) {
            for (short j = 0; j < tr[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), tr[i][j], 1e-3);
            }
        }        
    }   
}
