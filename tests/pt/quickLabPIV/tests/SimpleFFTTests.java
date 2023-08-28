// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.jobs.xcorr.SimpleFFT;

public class SimpleFFTTests {		
    /*void swapInput(float[] inputArray) {
    //Arr => Ek, Ok
    //OptionA) Difficult....
    //exchange the odd positions in Ek to the right half side
    //   exchange odd entries with even entries at the right half side
    //   sort entries to main relative positions for even values
    //exchange the even positions in Ok to the left half side
    //swap Right half side of Ek with left half side of Ok
    //OptionB)
    //Fill array with reordering target indices...
    int inputSize = inputArray.length;
    int offset = 0;
    for (int i = 1; i < inputSize/2; i+=2) {
        float temp = inputArray[offset + i + 1];
        inputArray[offset + i + 1] = inputArray[offset + inputSize/2 + i];
        inputArray[offset + inputSize/2 + i] = temp;
    }
}*/	
	
	@Test
	public void computeCrossCorrelationFFT2DSimple() {
		float xr[][] = new float[16][16];
		float xi[][] = new float[16][16];
		float yr[][] = new float[16][16];
		float yi[][] = new float[16][16];

		SimpleFFT fft = new SimpleFFT(xr.length, xr[0].length);
		
		Matrix mx = new MatrixFloat(new float[8*8],(short)8,(short)8);
		Matrix my = new MatrixFloat(new float[8*8],(short)8,(short)8);

		List<Matrix> mxs = new ArrayList<Matrix>(1);
		List<Matrix> mys = new ArrayList<Matrix>(1);
		mxs.add(mx);
		mys.add(my);
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				mx.setElement(i*8+j, i, j);
				my.setElement(i*8+j+2, i, j);
				xr[i][j] = i*8+j;
				xi[i][j] = 0.0f;
				yr[i][j] = i*8+j+2;
				yi[i][j] = 0.0f;
			}
		}
	
		fft.computeCrossCorrelationFFT2DSerial(xr, xi, yr, yi);
		
		for (int i = 0; i < xr.length; i++) {
			SimpleFFT.printArray(xr[i]);
			SimpleFFT.printArray(xi[i]);
		}
		
		List<Matrix> mXs = CrossCorrelationTestHelper.localCrossCorrelation(mxs, mys);
		Matrix mX = mXs.get(0);
		assertEquals("mX Height is wrong", 15, mX.getHeight());
		assertEquals("mX Width is wrong", 15, mX.getWidth());
		for (int i = 0; i < mX.getHeight(); i++) {
			for (int j = 0; j < mX.getWidth(); j++) {
				float mxVal = mX.getElement((short)i, (short)j);
				float xrVal = xr[i][j];
				float xiVal = xi[i][j];
				
				assertEquals("Cross-correlation imaginary value is too large", 0.0f, xiVal, 0.003f);
				assertEquals("Cross-correlation values at: [i: " + i + ",j: " + j + "]", mxVal, xrVal, 0.1f);
			}
		}
		
	}
	
	//@Test
	/*public void computeFFT() {
		float []inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
		//float []inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		//		              16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
		//float []inputArray = {0, 1, 2, 3, 4, 5, 6, 7};
		
		initEulerTable(inputArray.length, 0);
		initEulerTable(inputArray.length, 1);
		float xr[] = new float[inputArray.length];
		float xi[] = new float[inputArray.length];

		System.arraycopy(inputArray, 0, xr, 0, inputArray.length);
		System.arraycopy(inputArray, 0, xi, 0, inputArray.length);
		//Arrays.fill(xi, 0.0f);
		
		if (DUMP_INFO) {
		    System.out.println("FFT");
		}
		perfectShuffleFFTInput(xr);
		perfectShuffleFFTInput(xi);
		if (DUMP_INFO) {
		    printArray(xr);
		}

		computeFFTSerial(xr, xi);
		
		if (DUMP_INFO) {
    		printArray(xr);
    		printArray(xi);
		}
		
		if (DUMP_INFO) {
		    System.out.println("IFFT");
		}
		perfectShuffleFFTInput(xr);
		perfectShuffleFFTInput(xi);
		computeIFFTSerial(xr, xi);
		if (DUMP_INFO) {
    		printArray(xr);
    		printArray(xi);
		}
	}*/
	
}