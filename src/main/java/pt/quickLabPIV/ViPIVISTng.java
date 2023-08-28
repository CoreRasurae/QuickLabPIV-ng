// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.ArrayList;
import java.util.List;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class ViPIVISTng {

	private int signX(int x) {
		return (x + x + 1)/(Math.abs(x + x + 1)); // if (x >= 0) return 1, else return -1
	}

	private int relocateX(final int x, final int dimX) {
		int result;
		
		result = (signX(x) + 1) * (x + 1) / 2; // if (x >= 0) then result=x+1, else result = 0 
		result = (signX(dimX - result) + 1) * result / 2; //if (result > dimX) then result = 0
		
		return result;
	}
	
	/*private int relocateXa(final int x, final int dimX) {
		int result;
		
		result = (signX(x) + 1) * x / 2 + (signX(x) - 1) / 2; // if (x >= 0) then result=x, else result = -1 
		result = (signX(dimX - 1 - result) + 1) * result / 2 + (signX(dimX - 1 - result) - 1) / 2; //if (result >= dimX) then result = -1
		
		return result;
	}*/
	
	public static void main(String[] args) {
		ViPIVISTng ng = new ViPIVISTng();
		
		List<Matrix> inputMatricesF = new ArrayList<Matrix>();
		List<Matrix> inputMatricesG = new ArrayList<Matrix>();
	
		float[][] matrixF = new float[2][2];
		float[][] matrixG = new float[2][2];
		matrixF[0][0] = 1.0f;
		matrixF[0][1] = 2.0f;
		matrixF[1][0] = 3.0f;
		matrixF[1][1] = 4.0f;
		
		matrixG[0][0] = 0.5f;
		matrixG[0][1] = 1.0f;
		matrixG[1][0] = 1.5f;
		matrixG[1][1] = 2.0f;
		
		Matrix f = new MatrixFloat((short)2,(short)2);
		f.copyMatrixFrom2DArray(matrixF, 0, 0);
		Matrix g = new MatrixFloat((short)2,(short)2);
		g.copyMatrixFrom2DArray(matrixG, 0, 0);
		inputMatricesF.add(f);
		inputMatricesG.add(g);
		
		System.out.println(ng.signX(10));
		System.out.println(ng.signX(1));
		System.out.println(ng.signX(0));
		System.out.println(ng.signX(-1));
		System.out.println(ng.signX(-10));
		
		System.out.println("---- Relocate results below - A ----");
		System.out.println(ng.relocateX(11, 20));
		System.out.println(ng.relocateX(10, 20));
		System.out.println(ng.relocateX(1, 20));
		System.out.println(ng.relocateX(0, 20));
		System.out.println(ng.relocateX(-1, 20));
		System.out.println(ng.relocateX(-10, 20));
		System.out.println("---- Relocate results below - B ----");
		System.out.println(ng.relocateX(11, 10));
		System.out.println(ng.relocateX(10, 10));
		System.out.println(ng.relocateX(9, 10));
		System.out.println(ng.relocateX(1, 10));
		System.out.println(ng.relocateX(0, 10));
		System.out.println(ng.relocateX(-1, 10));
		System.out.println(ng.relocateX(-10, 10));
		

		ComputationDevice chosen = DeviceManager.getSingleton().getCPU();
		
        CrossCorrelationJob job = new CrossCorrelationJob(chosen, false, inputMatricesF, inputMatricesG);
        job.analyze();
        job.compute();
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        for (Matrix result : outputMatrices) {
        	for (int i = 0;  i < result.getHeight(); i++) {
        		for (int j = 0; j < result.getWidth(); j++) {
        			System.out.println("i: " + i + ", j: " + j + " - " + result.getElement((short)i, (short)j));
        		}
        	}
        }
	}

}
