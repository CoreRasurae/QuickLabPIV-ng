// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import com.aparapi.Kernel;

public class CrossCorrelationFFTBasicKernel extends Kernel{
	int numberOfUsedTiles;
	int dimI;
	int dimJ;
	float[] xr;
	float[] xi;
	float[] yr;
	float[] yi;
	
	@Constant
	float[] w;
	
	@Constant
	int[] shuffleOrderConst;

	public void setKernelParams(int[] shuffleOrder, float[] w, float[] xr, float xi[], float yr[], float yi[], 
			int[] inputGeometry, int[] outputGeometry, int numberOfUsedTiles) {
		this.w = w;
		this.shuffleOrderConst = shuffleOrder;
		this.xr = xr;
		this.xi = xi;
		this.yr = yr;
		this.yi = yi;
		this.dimI = outputGeometry[0];
		this.dimJ = outputGeometry[1];
		this.numberOfUsedTiles = numberOfUsedTiles;
	}
	
	/**
	 * Shuffles the input array data to mimic the FFT expansion phase, without needing to allocate additional memory (perfect shuffle).
	 * @param inputArray the input array to be perfect shuffled into initial FFT order
	 */
	public void perfectShuffleFFTInput(float[] array, int startJ) {
		for (int jl = 0; jl < dimJ; jl++) {
			int src = jl;
			int dst = shuffleOrderConst[src];
			if (dst > src) { 
				float temp = array[src + startJ];
				array[src + startJ] = array[dst + startJ];
				array[dst + startJ] = temp;
			}
		}
	}
	
	/**
	 * Compute a FFT parallel step... this is the FFT kernel. 
	 * @param xr the real      input vector component
	 * @param xi the imaginary input vector component
	 * @param step the current FFT block size (in the expansion phase)
	 * @param level the current FFT size at the current depth level
	 * @param level2 the FFT size at next depth level
	 * @param tidx the index within the current FFT size
	 * @param N the original FFT size
	 */
	public void computeFFTStep(final float[] xr, final float[] xi, final int step, int level, int level2, int tidx, final int N, int startJ) {
		//step * (tid % level) - splits the fft into the fft blocks index expected at a given fft depth level.
		//
		//example A: depth Level 1 - two mixed blocks of N/2 
		//step=N/2
		//level=1
		//k is always zero for every tidx - the second N/2 half is automatically handled by the code below.
		//                                  simple signal changes to the symmetric of the intermixed second half.
		//
		//example B: depth Level 2 - four blocks of N/4, that means two intermixed blocks in the first N/2 mixed half that must be handled
		//step=N/4
		//level=2
		//k is N/4 * (tidx % 2)
		//tidx even goes to k=0  ( 0 degrees = 1)
		//tidx odd goes to k=N/4 (90 degrees = j)
		//
		int k = step * (tidx % level);           //twiddle index in sine table, also matches Euler index
		
		//Example A: depth Level 1 - two mixed blocks of N/2 (with N=8)
		//tidx=0, level=1, level2=2 -> i=0/1*2+0 = 0, j=1
		//tidx=1, level=1, level2=2 -> i=1/1*2+0 = 2, j=3
		//tidx=2, level=1, level2=2 -> i=2/1*2+0 = 4, j=5
		//tidx=3, level=1, level2=2 -> i=3/1*2+0 = 6, j=7
		//
		//Example B: depth Level 2 - four mixed blocks of N/4 (with N=8)
		//tidx=0, level=2, level2=4 -> i=0/2*4+(0%2) = 0, j=2
		//tidx=1, level=2, level2=4 -> i=1/2*4+(1%2) = 1, j=3
		//tidx=2, level=2, level2=4 -> i=2/2*4+(2%2) = 4, j=6
		//tidx=3, level=2, level2=4 -> i=3/2*4+(3%2) = 5, j=7
		//
		int i = (tidx/level)*level2+(tidx%level); //source index (even index)
		int j = i + level;                        //destination index (odd index)
		
		//NOTES A)
		//Cos(x) = Sin(Pi/2 - x)
		//
		//n=N/4 in table (w) is -> 2*Pi*n/N = 2*Pi*N/4/N/1 = 2*Pi*1/4 = Pi/2
		//
		//Since Euler function is exp(-2j*Pi*n/N) = cos(-2*Pi*n/N) + j sin(-2*Pi*n/N)
		//and cos(k) = sin(Pi/2 - k) -> cos(-2*Pi*k/N) = sin(Pi/2 - (- 2*Pi*k/N)) = sin(Pi/2 + 2*Pi*k/N)
		//and j sin(-2*Pi*k/N) = - j sin(2*Pi*k/N)
		//Thus Euler table (w) can have only the values of sin(2*Pi*n/N) and
		// w(k + N/4) = sin(2*Pi*k/N + Pi/2)
		// and
		//-w(k) = - j sin(2*Pi*k/N)
		float wr = w[k + N/4]; //twiddle value, real part
		float wi = -w[k];      //twiddle value, imaginary part
		
		//FFT Exploit N/2 Euler symmetries of complex products
		
		/*//Not working well... due to likely compiler instruction reordering, although prints don't evidence such...
		System.out.println("A: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);
		//Handle the lower half = Ek + Wk Ok
		float tempLowerHalfReal = xr[i] + (wr*xr[j]) - (wi*xi[j]);
		float tempLowerHalfImg  = xi[i] + (wr*xi[j]) + (wi*xr[j]);
		
		System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);
		//Handle the upper half = Ek - Wk Ok
		xr[j] = xr[i] - (wr*xr[j]) + (wi*xi[j]);
		xi[j] = xi[i] - (wr*xi[j]) - (wi*xr[j]);
		System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);

		xr[i] = tempLowerHalfReal;
		xi[i] = tempLowerHalfImg;
		System.out.println("D: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);*/
		
		
		//Option A: That is working properly... Apparently saving both results to temporary variables allows proper instruction ordering. 
		//System.out.println("A: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
		float tempLowerHalfReal = xr[i + startJ] + (wr*xr[j + startJ]) - (wi*xi[j + startJ]);
		float tempLowerHalfImg  = xi[i + startJ] + (wr*xi[j + startJ]) + (wi*xr[j + startJ]);
		
		//Handle the upper half = Ek - Wk Ok
		float tempUpperHalfReal = xr[i + startJ] - (wr*xr[j + startJ]) + (wi*xi[j + startJ]);
		float tempUpperHalfImg = xi[i + startJ] - (wr*xi[j + startJ]) - (wi*xr[j + startJ]);
		//System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);

		xr[i + startJ] = tempLowerHalfReal;
		xi[i + startJ] = tempLowerHalfImg;
		//System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
		xr[j + startJ] = tempUpperHalfReal;
		xi[j + startJ] = tempUpperHalfImg;
		
		//Option B: This one is working with the compiler.... by switching the order of the temporary results
		/*float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
		float tempUpperHalfImg  = xi[i] - (wr*xi[j]) - (wi*xr[j]);
		
		//Handle the lower half = Ek - Wk Ok
		xr[i] = xr[i] + (wr*xr[j]) - (wi*xi[j]);
		xi[i] = xi[i] + (wr*xi[j]) + (wi*xr[j]);

		xr[j] = tempUpperHalfReal;
		xi[j] = tempUpperHalfImg;*/
	}

	
	/**
	 * <b>NOTE:</b> it is required that the FFT reduction phase reordering has already been performed before calling this method.
	 * Otherwise the FFT will be computed wrongly.
	 * @param xr
	 * @param xi
	 */
	public void computeFFTSerial(final float xr[], final float xi[], final int offset) {
		final int N = dimJ;
		int step = N;
		for (int level=1; level < N; level <<= 1) {
			int level2 = level << 1;
			step >>>= 1;
			for (int tidx = 0; tidx < N/2; tidx++) {
				computeFFTStep(xr, xi, step, level, level2, tidx, N, offset);
			}			
		}
	}

	void computeFFT2DSerialX(int offset) {
		for (int i = 0; i < dimI/2; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(xr, lineOffset);
			computeFFTSerial(xr, xi, lineOffset);
		}
		
		//Transpose matrix....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = xr[lineOffset + j];
				float tempAIm = xi[lineOffset + j];
				float tempBRe=xr[offset + j * dimI + i];
				float tempBIm=xi[offset + j * dimI + i];
				xr[offset + j * dimI + i]=tempARe;
				xi[offset + j * dimI + i]=tempAIm;
				xr[lineOffset + j]=tempBRe;
				xi[lineOffset + j]=tempBIm;
			}
		}
		
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(xr, lineOffset);
			perfectShuffleFFTInput(xi, lineOffset);
			computeFFTSerial(xr, xi, lineOffset);
		}

		//Transpose matrix....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = xr[lineOffset + j];
				float tempAIm = xi[lineOffset + j];
				float tempBRe=xr[offset + j * dimI + i];
				float tempBIm=xi[offset + j * dimI + i];
				xr[offset + j * dimI + i]=tempARe;
				xi[offset + j * dimI + i]=tempAIm;
				xr[lineOffset + j]=tempBRe;
				xi[lineOffset + j]=tempBIm;
			}
		}
	}

	void computeFFT2DSerialY(final int offset) {
		for (int i = 0; i < dimI/2; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(yr, lineOffset);
			computeFFTSerial(yr, yi, lineOffset);
		}
		
		//Transpose matrix....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = yr[lineOffset + j];
				float tempAIm = yi[lineOffset + j];
				float tempBRe=yr[offset + j * dimI + i];
				float tempBIm=yi[offset + j * dimI + i];
				yr[offset + j * dimI + i]=tempARe;
				yi[offset + j * dimI + i]=tempAIm;
				yr[lineOffset + j]=tempBRe;
				yi[lineOffset + j]=tempBIm;
			}
		}
		
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(yr, lineOffset);
			perfectShuffleFFTInput(yi, lineOffset);
			computeFFTSerial(yr, yi, lineOffset);
		}

		//Transpose matrix....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = yr[lineOffset + j];
				float tempAIm = yi[lineOffset + j];
				float tempBRe=yr[offset + j * dimI + i];
				float tempBIm=yi[offset + j * dimI + i];
				yr[offset + j * dimI + i]=tempARe;
				yi[offset + j * dimI + i]=tempAIm;
				yr[lineOffset + j]=tempBRe;
				yi[lineOffset + j]=tempBIm;
			}
		}
	}
	
	public void divideArray(float[] inputArray, int offset, float value) {
		for (int index = 0; index < dimJ; index++) {
			inputArray[offset + index] /= value;
		}
	}
	
	public void computeIFFTSerial(float[] xr, float[] xi, int offset) {
		//divideArray(xr, offset, dimJ);
		//divideArray(xi, offset, dimJ);
		computeFFTSerial(xi, xr, offset);
	}

	void computeIFFT2DSerial(final int offset) {
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(xr, lineOffset);
			perfectShuffleFFTInput(xi, lineOffset);
			computeIFFTSerial(xr, xi, lineOffset);
		}
		
		//Transpose matrix....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = xr[lineOffset + j];
				float tempAIm = xi[lineOffset + j];
				float tempBRe=xr[offset + j * dimI + i];
				float tempBIm=xi[offset + j * dimI + i];
				xr[offset + j * dimI + i]=tempARe;
				xi[offset + j * dimI + i]=tempAIm;
				xr[lineOffset + j]=tempBRe;
				xi[lineOffset + j]=tempBIm;
			}
		}
		
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			perfectShuffleFFTInput(xr, lineOffset);
			perfectShuffleFFTInput(xi, lineOffset);
			computeIFFTSerial(xr, xi, lineOffset);
		}

		//Transpose again....
		for (int i = 0; i < dimI; i++) {
			int lineOffset = offset + i * dimJ;
			for (int j = i; j < dimJ; j++) {
				float tempARe = xr[lineOffset + j];
				float tempAIm = xi[lineOffset + j];
				float tempBRe=xr[offset + j * dimI + i];
				float tempBIm=xi[offset + j * dimI + i];
				xr[offset + j * dimI + i]=tempARe;
				xi[offset + j * dimI + i]=tempAIm;
				xr[lineOffset + j]=tempBRe;
				xi[lineOffset + j]=tempBIm;
			}
		}
	}
	
	@Override
	public void run() {
		final int jGlobal = getGlobalId(0);
		final int iGlobal = getGlobalId(1);
		final int k = getGlobalId(2);
		

		final int tileIdx = k * getGlobalSize(1) * getGlobalSize(0) + 
				iGlobal * getGlobalSize(0) + 
				jGlobal;

		final int offset = tileIdx * dimI * dimJ;
		
		if (tileIdx < numberOfUsedTiles) {
			computeFFT2DSerialX(offset);
			computeFFT2DSerialY(offset);
					
			//By now xr, xi and yr, yi have the FFT transform with real and imaginary values
			//NOTE: That cross-correlation is being computed by a convolution, thus the product must be made like x[-n, -m]*y[n, m],
			//only the non-zero valued data is reversed before padding with zeros.
			for (int i = 0; i < dimI; i++) {
				int lineOffset = offset + i * dimJ;
				for (int j = 0; j < dimJ; j++) {
					//(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
					float tempRe = xr[lineOffset + j]*yr[lineOffset + j]-xi[lineOffset + j]*yi[lineOffset + j];
					float tempIm = xr[lineOffset + j]*yi[lineOffset + j]+xi[lineOffset + j]*yr[lineOffset + j];
					xr[lineOffset + j] = tempRe/dimJ/dimJ; //Optimization which replaces divideArray loops in the next computeIFFT2D
					xi[lineOffset + j] = tempIm/dimJ/dimJ;
				}
			}
			
			computeIFFT2DSerial(offset);
		}
	}
}
