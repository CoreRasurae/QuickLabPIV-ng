// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import com.aparapi.Kernel;

import pt.quickLabPIV.maximum.MaximumOpenCLKernel;

public class CrossCorrelationFFTCpuParallelKernel extends MaximumOpenCLKernel {
	int numberOfUsedTiles;
	int dimIBlockSize;
	int dimI;
	int dimJ;
	float[] xr;
	float[] xi;
	float[] yr;
	float[] yi;
	float[] maxs;

	@Constant
	float[] wConst; 

	@Constant
	int[] shuffleOrderConst;

	public void setKernelParams(int[] shuffleOrder, float[] w, 
			float[] xr, float xi[], float yr[], float yi[], float[] maxs,
			int dimIBlockSize, int[] inputGeometry, int[] outputGeometry, int numberOfUsedTiles) {
		shuffleOrderConst = shuffleOrder;
		wConst = w;
		this.xr = xr;
		this.xi = xi;
		this.yr = yr;
		this.yi = yi;
		this.maxs = maxs;
		this.dimIBlockSize = dimIBlockSize;
		this.dimI = outputGeometry[0];
		this.dimJ = outputGeometry[1];
		this.numberOfUsedTiles = numberOfUsedTiles;		
	}
	
	/**
	 * Shuffles the input array data to mimic the FFT expansion phase, without needing to allocate additional memory (perfect shuffle).
	 * @param inputArray the input array to be perfect shuffled into initial FFT order
	 */
	public void perfectShuffleFFTInput(float[] array, int lineOffset, int jlOffset, int colBlockSize) {		
		for (int jl = 0; jl < colBlockSize; jl++) {
			int src = jlOffset+jl;
			int dst = shuffleOrderConst[src];
			if (dst > src) { 
				float tempA = array[lineOffset + src];
				float tempB = array[lineOffset + dst];
				array[lineOffset + src] = tempB;
				array[lineOffset + dst] = tempA;
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
	public void computeFFTStep(float[] xr, float[] xi, 
			                   final int step,  int level, int level2, int tidx, final int N, int lineOffset) {

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
		
		//System.out.println("level: " + level + ", tidx: " + tidx + ", k: " + k + ", i: " + i + ", j: " + j);
		
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
		float wr = wConst[k + N/4]; //twiddle value, real part
		float wi = -wConst[k];      //twiddle value, imaginary part
		
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
		float tempLowerHalfReal = xr[lineOffset + i] + (wr*xr[lineOffset + j]) - (wi*xi[lineOffset + j]);
		float tempLowerHalfImg  = xi[lineOffset + i] + (wr*xi[lineOffset + j]) + (wi*xr[lineOffset + j]);
		
		//Handle the upper half = Ek - Wk Ok
		float tempUpperHalfReal = xr[lineOffset + i] - (wr*xr[lineOffset + j]) + (wi*xi[lineOffset + j]);
		float tempUpperHalfImg = xi[lineOffset + i] - (wr*xi[lineOffset + j]) - (wi*xr[lineOffset + j]);
		//System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);

		xr[lineOffset + i] = tempLowerHalfReal;
		xi[lineOffset + i] = tempLowerHalfImg;
		//System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
		xr[lineOffset + j] = tempUpperHalfReal;
		xi[lineOffset + j] = tempUpperHalfImg;
		
		//Option B: This one is working with the compiler.... by switching the order of the temporary results
		/*float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
		float tempUpperHalfImg  = xi[i] - (wr*xi[j]) - (wi*xr[j]);
		
		//Handle the lower half = Ek - Wk Ok
		xr[i] = xr[i] + (wr*xr[j]) - (wi*xi[j]);
		xi[i] = xi[i] + (wr*xi[j]) + (wi*xr[j]);

		xr[j] = tempUpperHalfReal;
		xi[j] = tempUpperHalfImg;*/
	}
	
	/*public void dumpArray(String name, float[] xr) {
		StringBuilder sb = new StringBuilder(100);
		int il = getLocalId(1);
		sb.append("[ ");
		sb.append(name);
		sb.append(" - il=");
		sb.append(il);
		sb.append(" - ");
		for (int indexJ = 0; indexJ < dimJ; indexJ++) {
			sb.append(xr[indexJ]);
			sb.append(", ");
		}
		sb.append("]");
		System.out.println(sb.toString());
	}*/
	
	@Override
	public void run() {
		final int j = getGlobalId(0);
		final int i = getGlobalId(1);
		final int k = getGlobalId(2);
		final int threadNrJ = getLocalId(0);
		final int threadNrI = getLocalId(1);
		
		final int lineBlockSize = dimI / getLocalSize(1);
		final int tileNumberI = getGroupId(1);		
		final int ilOffset = threadNrI * lineBlockSize; 
 
		final int colBlockSize = dimJ / getLocalSize(0);
		final int tileNumberJ = getGroupId(0);
		final int jlOffset = threadNrJ * (colBlockSize >>> 1); //jlOffset goes up to dimJ/2
		
		//final int tileIdx = getPassId(); //Performance is much lower 
		int tileIdx = getPassId() * getNumGroups(0) * getNumGroups(1) * getNumGroups(2) +
				k * getNumGroups(0) * getNumGroups(1) +
				tileNumberI * getNumGroups(0) + 
				tileNumberJ;


		final int offset = tileIdx * dimI * dimJ;
		
		final int N = dimJ;
		
		final int jlAdj = jlOffset << 1; //jlAdj goes up to dimJ
		
		final int lineOffset = offset + threadNrI * dimJ;
		
		if (tileIdx >= numberOfUsedTiles) {
			return;
		}
		
		//This will compute the FFT for all the row entries of the matrix
		//An improvement can be tried... Half the work items compute top half of xr while the other half work items
		//compute the top half of yr.
		perfectShuffleFFTInput(xr, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(yr, lineOffset, jlAdj, colBlockSize);
		globalBarrier();
		
		for (int levelA=1, stepA=N; levelA < N; levelA <<= 1) {
			int level2 = levelA << 1;
			stepA >>>= 1;
		
			for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
				if (ilOffset < dimI / 2) {
					computeFFTStep(xr, xi, stepA, levelA, level2, jlOffset + tidx, N, lineOffset);
				} else {
					computeFFTStep(yr, yi, stepA, levelA, level2, jlOffset + tidx, N, lineOffset-(dimI/2*dimJ));
				}
			}
			globalBarrier(); //Ensure all work-items of work-group have written data to local memory
		}		
		

		//Problem we are concurrently reading and writing to the same buffer...
		//So we must ensure each thread exchanges independent values only.
		//No problem to be inside loop, since loop will run only once.
		//In this conditional block there will be one thread per each matrix line.
	
		for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
			if (jlAdj + indexJ > threadNrI) {
				int columnOffset = lineOffset + jlAdj + indexJ;
				int transposedOffset = offset + (jlAdj + indexJ) * dimI + threadNrI;
				float tempARe = xr[columnOffset];
				float tempAIm = xi[columnOffset];
				float tempBRe = xr[transposedOffset];
				float tempBIm = xi[transposedOffset];
				xr[transposedOffset]=tempARe;
				xi[transposedOffset]=tempAIm;
				xr[columnOffset]=tempBRe;
				xi[columnOffset]=tempBIm;
				tempARe = yr[columnOffset];
				tempAIm = yi[columnOffset];
				tempBRe = yr[transposedOffset];
				tempBIm = yi[transposedOffset];
				yr[transposedOffset]=tempARe;
				yi[transposedOffset]=tempAIm;
				yr[columnOffset]=tempBRe;
				yi[columnOffset]=tempBIm;
			}
		}
		globalBarrier();
		
		perfectShuffleFFTInput(xr, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(xi, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(yr, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(yi, lineOffset, jlAdj, colBlockSize);
		globalBarrier();

		//This will compute FFT for the top half entries of the matrix
		for (int levelB=1, stepB=N; levelB < N; levelB <<= 1) {
			int level2B = levelB << 1;
			stepB >>>= 1;
		
			for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
				computeFFTStep(xr, xi, stepB, levelB, level2B, jlOffset + tidx, N, lineOffset);
				computeFFTStep(yr, yi, stepB, levelB, level2B, jlOffset + tidx, N, lineOffset);
			}
			globalBarrier(); //Ensure data from all local work-items is written to the local memory
		}

		//Transpose back the matrix
		for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
			if (jlAdj + indexJ > threadNrI) {
				int columnOffset = lineOffset + jlAdj + indexJ;
				int transposedOffset = offset + (jlAdj + indexJ) * dimI + threadNrI;
				float tempARe = xr[columnOffset];
				float tempAIm = xi[columnOffset];
				float tempBRe = xr[transposedOffset];
				float tempBIm = xi[transposedOffset];
				xr[transposedOffset]=tempARe;
				xi[transposedOffset]=tempAIm;
				xr[columnOffset]=tempBRe;
				xi[columnOffset]=tempBIm;
				tempARe = yr[columnOffset];
				tempAIm = yi[columnOffset];
				tempBRe = yr[transposedOffset];
				tempBIm = yi[transposedOffset];
				yr[transposedOffset]=tempARe;
				yi[transposedOffset]=tempAIm;
				yr[columnOffset]=tempBRe;
				yi[columnOffset]=tempBIm;
			}
		}
		globalBarrier();
		//dumpArray("xr", xrLocal[il]);
		//dumpArray("xi", xiLocal[il]);

		
		//By now xr, xi and yr, yi have the FFT transform with real and imaginary values
		//NOTE: That cross-correlation is being computed by a convolution, thus the product must be made like x[-n, -m]*y[n, m],
		//only the non-zero valued data is reversed before padding with zeros.
		
		//Note: this can also be done in parallel
		for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
			final int colOffset = lineOffset + jlAdj + indexJ;
			//(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
			float tempRe = xr[colOffset]*yr[colOffset]-xi[colOffset]*yi[colOffset];
			float tempIm = xr[colOffset]*yi[colOffset]+xi[colOffset]*yr[colOffset];
			xr[colOffset] = tempRe / (float)(dimJ * dimJ); //Must divide once to compensate IFFT to FFT in X and another for IFFT to FFT in Y
			xi[colOffset] = tempIm / (float)(dimJ * dimJ); 
		}
		globalBarrier();

		
		/*dumpArray("xrProd", xrLocal[il]);
		dumpArray("xiProd", xiLocal[il]);*/

		perfectShuffleFFTInput(xr, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(xi, lineOffset, jlAdj, colBlockSize);
		globalBarrier();
					
		//Compute the IFFT...
		for (int levelC=1, stepC=N; levelC < N; levelC <<= 1) {
			int level2C = levelC << 1;
			stepC >>>= 1;
		
			for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
				computeFFTStep(xi, xr, stepC, levelC, level2C, jlOffset + tidx, N, lineOffset);
			}
			globalBarrier();
		}
			
		/*dumpArray("xrFFT_1Half", xrLocal[il]);
		dumpArray("xiFFT_1Half", xiLocal[il]);*/			

		//Transpose matrix again...
		for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
			if (jlAdj + indexJ > threadNrI) {
				int columnOffset = lineOffset + jlAdj + indexJ;
				int transposedOffset = offset + (jlAdj + indexJ) * dimI + threadNrI;
				float tempARe = xr[columnOffset];
				float tempAIm = xi[columnOffset];
				float tempBRe = xr[transposedOffset];
				float tempBIm = xi[transposedOffset];
				xr[transposedOffset]=tempARe;
				xi[transposedOffset]=tempAIm;
				xr[columnOffset]=tempBRe;
				xi[columnOffset]=tempBIm;
			}
		}
		globalBarrier();
		
		perfectShuffleFFTInput(xr, lineOffset, jlAdj, colBlockSize);
		perfectShuffleFFTInput(xi, lineOffset, jlAdj, colBlockSize);
		globalBarrier();

		for (int levelD=1, stepD=N; levelD < N; levelD <<= 1) {
			int level2D = levelD << 1;
			stepD >>>= 1;
		
			for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
				computeFFTStep(xi, xr, stepD, levelD, level2D, jlOffset + tidx, N, lineOffset);
			}
			globalBarrier();
		}

		/*dumpArray("xrFFT_2Half", xrLocal[il]);
		dumpArray("xiFFT_2Half", xiLocal[il]);*/
		
		float maxVal = 0.0f;
		int maxI = 0;
		int maxJ = 0;
		//Transpose matrix again...
		for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
			if (jlAdj + indexJ >= threadNrI) {
				int columnOffset = lineOffset + jlAdj + indexJ;
				int transposedOffset = offset + (jlAdj + indexJ) * dimI + threadNrI;
				float tempARe = xr[columnOffset];
				float tempBRe = xr[transposedOffset];
				xr[transposedOffset]=tempARe;
				xr[columnOffset]=tempBRe;
				
				maxVal = max(maxVal, tempARe);
				if (maxVal == tempARe) {
					maxI = jlAdj + indexJ;
					maxJ = threadNrI;
				}
				
				maxVal = max(maxVal, tempBRe);
				if (maxVal == tempBRe) {
					maxI = threadNrI;
					maxJ = jlAdj + indexJ;
				}
			}
		}
		
		computeMaximum(maxs, tileIdx, numberOfUsedTiles, maxVal, maxI, maxJ);
	}
}
