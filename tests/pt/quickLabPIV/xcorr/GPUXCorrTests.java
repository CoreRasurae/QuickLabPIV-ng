// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.xcorr;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;
import pt.quickLabPIV.tests.SimpleFFTTests;

public class GPUXCorrTests {
    private boolean dumpArray = false;
    
    @Test
    public void compute4x4XCorr() {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();

        int[][] matrixF = new int[4][4];
        int[][] matrixG = new int[4][4];
        float[][] matrixFf = new float[4][4];
        float[][] matrixGf = new float[4][4];
        matrixF[0][0] = 6;
        matrixF[0][1] = 6;
        matrixF[0][2] = 6;
        matrixF[1][0] = 7;
        matrixF[1][1] = 6;
        matrixF[1][2] = 6;
        matrixF[2][0] = 7;
        matrixF[2][1] = 7;
        matrixF[2][2] = 6;
        
        matrixG[0][0] = 5;
        matrixG[0][1] = 5;
        matrixG[0][2] = 6;
        matrixG[1][0] = 6;
        matrixG[1][1] = 6;
        matrixG[1][2] = 7;
        matrixG[2][0] = 7;
        matrixG[2][1] = 7;
        matrixG[2][2] = 7;

        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                matrixFf[i][j] = matrixF[i][j];
                matrixGf[i][j] = matrixG[i][j];
            }
        }
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixFf, 0, 0);
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixGf, 0, 0);
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        
        int[][] result = computeSingleThreadedXCorr(matrixF, matrixG);
        if (dumpArray) {
            SimpleFFT.dump2DArray("Result", result);
        }
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result[i][j], 1e-3);
            }
        }

    }

    @Test
    public void compute4x4XCorrSubMat2x2MethodA() {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();

        int[][] matrixF = new int[4][4];
        int[][] matrixG = new int[4][4];
        float[][] matrixFf = new float[4][4];
        float[][] matrixGf = new float[4][4];
        matrixF[0][0] = 6;
        matrixF[0][1] = 6;
        matrixF[0][2] = 6;
        matrixF[1][0] = 7;
        matrixF[1][1] = 6;
        matrixF[1][2] = 6;
        matrixF[2][0] = 7;
        matrixF[2][1] = 7;
        matrixF[2][2] = 6;
        
        matrixG[0][0] = 5;
        matrixG[0][1] = 5;
        matrixG[0][2] = 6;
        matrixG[1][0] = 6;
        matrixG[1][1] = 6;
        matrixG[1][2] = 7;
        matrixG[2][0] = 7;
        matrixG[2][1] = 7;
        matrixG[2][2] = 7;

        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                matrixFf[i][j] = matrixF[i][j];
                matrixGf[i][j] = matrixG[i][j];
            }
        }
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixFf, 0, 0);
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixGf, 0, 0);
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        
        int[][] result = computeSingleThreadedXCorrInSubMatricesMethodA(matrixF, matrixG, 2, 2);
        if (dumpArray) {
            SimpleFFT.dump2DArray("Result", result);
        }
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result[i][j], 1e-3);
            }
        }

    }

    @Test
    public void compute4x4XCorrSubMat2x2MethodB() {
        List<Matrix> inputMatricesF = new ArrayList<Matrix>();
        List<Matrix> inputMatricesG = new ArrayList<Matrix>();

        int[][] matrixF = new int[4][4];
        int[][] matrixG = new int[4][4];
        float[][] matrixFf = new float[4][4];
        float[][] matrixGf = new float[4][4];
        matrixF[0][0] = 6;
        matrixF[0][1] = 6;
        matrixF[0][2] = 6;
        matrixF[1][0] = 7;
        matrixF[1][1] = 6;
        matrixF[1][2] = 6;
        matrixF[2][0] = 7;
        matrixF[2][1] = 7;
        matrixF[2][2] = 6;
        
        matrixG[0][0] = 5;
        matrixG[0][1] = 5;
        matrixG[0][2] = 6;
        matrixG[1][0] = 6;
        matrixG[1][1] = 6;
        matrixG[1][2] = 7;
        matrixG[2][0] = 7;
        matrixG[2][1] = 7;
        matrixG[2][2] = 7;

        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                matrixFf[i][j] = matrixF[i][j];
                matrixGf[i][j] = matrixG[i][j];
            }
        }
        
        Matrix f = new MatrixFloat((short)4,(short)4);
        f.copyMatrixFrom2DArray(matrixFf, 0, 0);
        Matrix g = new MatrixFloat((short)4,(short)4);
        g.copyMatrixFrom2DArray(matrixGf, 0, 0);
        inputMatricesF.add(f);
        inputMatricesG.add(g);

        
        int[][] result = computeSingleThreadedXCorrInSubMatricesMethodB(matrixF, matrixG);
        if (dumpArray) {
            SimpleFFT.dump2DArray("Result", result);
        }
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix refMatrix = outputMatricesLocal.get(0);
        for (short i = 0;  i < matrixF.length - 1; i++) {
            for (short j = 0; j < matrixF[0].length - 1; j++) {
                assertEquals("Computed cross-correlation value" +
                        ", i=" + (int)i + ", j=" + (int)j + " is wrong", refMatrix.getElement(i, j), result[i][j], 1e-3);
            }
        }

    }

    
    public static void copyMat(int startI, int startJ, int[][] mat, int[][] subMat) {
        for (int copyI = 0; copyI < subMat.length; copyI++) {
            for (int copyJ = 0; copyJ < subMat[0].length; copyJ++) {
                if (startI + copyI < mat.length && startJ + copyJ < mat[0].length) {
                    subMat[copyI][copyJ] = mat[startI + copyI][startJ + copyJ];
                } else {
                    subMat[copyI][copyJ] = 0;
                }
            }
        }
    }
    
    public static int[][] computeSingleThreadedXCorrInSubMatricesMethodA(int[][] matA, int[][] matB, int dimSubM, int dimSubN) {
        int dimM = matA.length;
        int dimN =  matA[0].length;
        int dimResultM = 2 * dimM - 1;
        int dimResultN = 2 * dimN - 1;
        int offsetResultM = dimResultM / 2;
        int offsetResultN = dimResultN / 2;
        
        int dimResultSubM = 2 * dimSubM - 1;
        int dimResultSubN = 2 * dimSubN - 1;
        int offsetResultSubM = dimResultSubM / 2;
        int offsetResultSubN = dimResultSubN / 2;
        
        int[][] result = new int[dimResultM][dimResultN];

        int[][] subMatA = new int[dimSubM][dimSubN];
        int[][] subMatB = new int[dimSubM][dimSubN];
        
        for (int ai = 0; ai < dimM; ai+=dimSubM) {
            for (int aj = 0; aj < dimN; aj+=dimSubN) {
                copyMat(ai, aj, matA, subMatA);
                
                for (int bi = 0; bi < dimM; bi+=dimSubM)     {
                    for (int bj = 0; bj < dimN; bj+=dimSubN) {
                        copyMat(bi, bj, matB, subMatB);
                        
                        int[][] subResult = computeSingleThreadedXCorr(subMatA, subMatB);

                        for (int copyI = 0; copyI < dimResultSubM; copyI++) {
                            for (int copyJ = 0; copyJ < dimResultSubN; copyJ++) {
                                result[offsetResultM + (bi - ai) - offsetResultSubM + copyI]
                                        [offsetResultN + (bj - aj) - offsetResultSubN + copyJ] += subResult[copyI][copyJ];
                            }
                        }
                    }
                }                
            }
        }
        
        return result;
    }
    
    public static int[][] computeSingleThreadedXCorrInSubMatricesMethodB(int[][] matA, int[][] matB) {
        int dimM = matA.length;
        int dimN =  matA[0].length;
        int dimResultM = 2 * dimM - 1;
        int dimResultN = 2 * dimN - 1;

        int[][] result = new int[2*dimM-1][2*dimN-1];
        
        Function<Integer, Integer> oddEvenIf;
        Function<Integer, Integer> oddEvenJf;
        Function<Integer, Integer> oddEvenMf;
        Function<Integer, Integer> oddEvenNf;
        for (int oddEvenI = 0; oddEvenI <= 1; oddEvenI++) {
            for (int oddEvenJ = 0; oddEvenJ <= 1; oddEvenJ++) {
                for (int oddEvenM = 0; oddEvenM <= 1; oddEvenM++) {
                    for (int oddEvenN = 0; oddEvenN <= 1; oddEvenN++) {
                        if (oddEvenI == 0) {
                            oddEvenIf = val -> 2*val; 
                        } else {
                            oddEvenIf = val -> 2*val + 1;
                        }
                        
                        if (oddEvenJ == 0) {
                            oddEvenJf = val -> 2*val;
                        } else {
                            oddEvenJf = val -> 2*val + 1;
                        }
                        
                        if (oddEvenM == 0) {
                            oddEvenMf = val -> 2*val;
                        } else {
                            oddEvenMf = val -> 2*val + 1;
                        }
                        
                        if (oddEvenN == 0) {
                            oddEvenNf = val -> 2*val;
                        } else {
                            oddEvenNf = val -> 2*val + 1;
                        }

                        //CrossCorrelation of subMatricesA and subMatricesB with dimensions [dimM/2, dimN/2]
                        //Requires 16 combinations (2*2*2*2)
                        
                        //Total operations for matrix are 2*dimM*2*dimN*2*dimM*2*dimN=(2*2*2*2)*(dimM*dimN*dimM*dimN) that means that computing the 
                        //cross-correlation by definition or using cross-correlation of sub-matrices results in the same total number of operations.
                        for (int m = -dimM/2 + 1; m <= dimM/2 - 1; m++) {
                            for (int n = -dimN/2 + 1; n <= dimN/2 - 1; n++) {
                                for (int i = -dimM/2 + 1; i <= dimM/2 - 1; i++) {
                                    for (int j = -dimN/2 + 1; j <= dimN/2 - 1; j++) {
                                        int resultIndexI = oddEvenIf.apply(i);
                                        int resultIndexJ = oddEvenJf.apply(j);
                                        int resultIndexM = oddEvenMf.apply(m);
                                        int resultIndexN = oddEvenNf.apply(n);
                                        if (resultIndexM >= 0 && resultIndexN >= 0 && 
                                            resultIndexM + resultIndexI >= 0 && resultIndexM + resultIndexI < dimM &&
                                            resultIndexN + resultIndexJ >= 0 && resultIndexN + resultIndexJ < dimN) {
                                            result[dimResultM/2 + resultIndexI][dimResultN/2 + resultIndexJ] += matA[resultIndexM][resultIndexN]*matB[resultIndexI + resultIndexM][resultIndexJ+resultIndexN];
                                        }                                         
                                    }
                                }
                            }
                        }                        
                    }
                }
            }
        }
        
        return result;
    }
    
    public static int[][] computeSingleThreadedXCorr(int[][] matA, int[][]matB) {
        int dimM = matA.length;
        int dimN =  matA[0].length;
        int[][] result = new int[2 * dimM - 1][2 * dimN - 1];
        
        for (int threadI = 0; threadI < dimM; threadI++) {
            for (int threadJ = 0; threadJ < dimN; threadJ++) {
                //Actual GPU thread work
                for (int m = 0; m < dimM; m++) {
                    for (int n = 0; n < dimN; n++) {
                        //This will have to be an atomicAdd(...)
                        result[dimM-1 + (m - dimM+1) + (dimM - 1 - threadI)][dimN-1 + (n - dimN + 1) + (- threadJ + dimN - 1)] += matB[m][n]*matA[threadI][threadJ];
                    }
                }
            }
        }
        
        return result;
    }
}
