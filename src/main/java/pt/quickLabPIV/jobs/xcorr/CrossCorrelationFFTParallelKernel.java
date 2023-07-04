package pt.quickLabPIV.jobs.xcorr;

import pt.quickLabPIV.maximum.MaximumOpenCLKernel;

public class CrossCorrelationFFTParallelKernel extends MaximumOpenCLKernel {
	final boolean computeMax = false;
	
	int numberOfUsedTiles;
	int dimIBlockSize;
	int dimI;
	int dimJ;
	float[] xr;
	float[] xi;
	float[] yr;
	float[] yi;
	float[] maxs;

	//Can only allocate 49K of shared local memory - 64*64*4*4 would be 64K, thus this would limit maximum input matrix dimensions to 16*16
	//Solution: kernel needs to be aware of local memory size, and problem splitting needs to take this into account
	//Large matrices of 256x256 have to be supported this would require 512*512*4*4 bytes = 4MB.
	@Local 
	float[] xrLocal;
	@Local
	float[] xiLocal;

	@Local 
	float[] yrLocal;
	@Local
	float[] yiLocal;
	
	@Constant
	float[] wConst; 
	@Constant
	int[] inputOrderConst;
	@Constant
	int[] shuffleOrderConst;

	@NoCL
	public void setKernelParams(int[] inputOrder, int[] shuffleOrder, float[] w, 
			float[] xr, float xi[], float yr[], float yi[], float maxs[],
			int dimIBlockSize, int[] inputGeometry, int[] outputGeometry, int numberOfUsedTiles) {
		inputOrderConst = inputOrder;
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
		
		xrLocal = new float[dimIBlockSize * dimJ];
		xiLocal = new float[dimIBlockSize * dimJ];
		yrLocal = new float[dimIBlockSize * dimJ];
		yiLocal = new float[dimIBlockSize * dimJ];
		this.numberOfUsedTiles = numberOfUsedTiles;
	}
	
	/**
	 * Shuffles the input array data to mimic the FFT expansion phase, without needing to allocate additional memory (perfect shuffle).
	 * @param inputArray the input array to be perfect shuffled into initial FFT order
	 */
	public void perfectShuffleFFTInput(@Local float[] array, int jlOffset, int colBlockSize) {
		final int il = getLocalId(1);
		
		/*for (int jl = 0; jl < colBlockSize; jl++) {
			int idx = jlOffset + jl;
			int src = inputOrderConst[idx];
			int dst = shuffleOrderConst[idx];
	 
			float temp = array[il * dimJ + src];
			array[il * dimJ + src] = array[il * dimJ + dst];
			array[il * dimJ + dst] = temp;
		}*/
		
		for (int jl = 0; jl < colBlockSize; jl++) {
			int src = jlOffset+jl;
			int dst = shuffleOrderConst[src];
			if (dst > src) { 
				float tempA = array[il * dimJ + src];
				float tempB = array[il * dimJ + dst];
				array[il * dimJ + src] = tempB;
				array[il * dimJ + dst] = tempA;
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
	public void computeFFTStep(@Local float[] xr, @Local float[] xi, 
			                   final int step,  int level, int level2, int tidx, final int N, int lineOffset) {
		final int jl = getLocalId(0);
		final int il = getLocalId(1) + lineOffset;

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
		float tempLowerHalfReal = xr[il * dimJ + i] + (wr*xr[il * dimJ + j]) - (wi*xi[il * dimJ + j]);
		float tempLowerHalfImg  = xi[il * dimJ + i] + (wr*xi[il * dimJ + j]) + (wi*xr[il * dimJ + j]);
		
		//Handle the upper half = Ek - Wk Ok
		float tempUpperHalfReal = xr[il * dimJ + i] - (wr*xr[il * dimJ + j]) + (wi*xi[il * dimJ + j]);
		float tempUpperHalfImg = xi[il * dimJ + i] - (wr*xi[il * dimJ + j]) - (wi*xr[il * dimJ + j]);
		//System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);

		xr[il * dimJ + i] = tempLowerHalfReal;
		xi[il * dimJ + i] = tempLowerHalfImg;
		//System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
		xr[il * dimJ + j] = tempUpperHalfReal;
		xi[il * dimJ + j] = tempUpperHalfImg;
		
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
		final int threadsI = getLocalSize(1);
		final int threadsJ = getLocalSize(0);
		final int threadNrJ = getLocalId(0);
		final int threadNrI = getLocalId(1);
		
		int lineBlockSize = dimI / threadsI;	
		if (lineBlockSize * threadsI != dimI)  {
			lineBlockSize++;
		}
		final int tileNumberI = getGroupId(1);		
		final int ilOffset = threadNrI; 
 
		final int colBlockSize = dimJ / threadsJ;
		final int tileNumberJ = getGroupId(0);
		final int jlOffset = threadNrJ * (colBlockSize >>> 1);
		
		int tileIdx = getPassId() * getNumGroups(0) * getNumGroups(1) * getNumGroups(2) +
				k * getNumGroups(0) * getNumGroups(1) +
				tileNumberI * getNumGroups(0) + 
				tileNumberJ;

		final int offset = tileIdx * dimI * dimJ;
		
		final int N = dimJ;
		
		final int jlAdj = jlOffset << 1;
		
		float maxVal = 0.0f;
		int maxI = 0;
		int maxJ = 0;
		
		if (tileIdx >= numberOfUsedTiles) {
			//All threads in the tile work-group will exit here, so no issues with the barrriers.
			return;
		}
		
		if (threadNrJ == 0 && threadNrI == 0) {
			//Workaround for aparapi to consider maxs as an output variable
			maxs[tileIdx*4 + 0] = 1;
		}
		
		if (lineBlockSize > 1) {
			for (int lineBlockIndex=0; lineBlockIndex < lineBlockSize; lineBlockIndex++) {
				final int lineOffsetGlobal = offset + (ilOffset + lineBlockIndex*threadsI) * dimJ;
				final int lineOffsetLocal = threadNrI * dimJ;
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					//Copy from source array to local buffer...
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						final int colOffsetGlobal = lineOffsetGlobal + threadNrJ + indexJ*threadsJ;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;
						xrLocal[colOffsetLocal] = xr[colOffsetGlobal];
						xiLocal[colOffsetLocal] = 0.0f;
						yrLocal[colOffsetLocal] = yr[colOffsetGlobal];
						yiLocal[colOffsetLocal] = 0.0f;
					}
				}
				localBarrier(); // - Not needed - currently only one thread per matrix per row
				
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					//This will compute the FFT for all the row entries of the matrix
					//An improvement can be tried... Half the work items compute top half of xr while the other half work items
					//compute the top half of yr.
					perfectShuffleFFTInput(xrLocal, jlAdj, colBlockSize);
					perfectShuffleFFTInput(yrLocal, jlAdj, colBlockSize);
				}
				localBarrier();
				
				for (int levelA=1, stepA=N; levelA < N; levelA <<= 1) {
					int level2 = levelA << 1;
					stepA >>>= 1;
					
					if (ilOffset + lineBlockIndex*threadsI < dimI) {
						for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
							computeFFTStep(xrLocal, xiLocal, stepA, levelA, level2, jlOffset + tidx, N, 0);
							computeFFTStep(yrLocal, yiLocal, stepA, levelA, level2, jlOffset + tidx, N, 0);
						}
					}
		
					localBarrier(); //Ensure all work-items of work-group have written data to local memory
				}
			
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					if (lineBlockSize > 1) {
						//Write local values to global memory
						for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
							final int colOffsetGlobal = lineOffsetGlobal + threadNrJ + indexJ*threadsJ;
							final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;
							xr[colOffsetGlobal] = xrLocal[colOffsetLocal];
							xi[colOffsetGlobal] = xiLocal[colOffsetLocal];
							yr[colOffsetGlobal] = yrLocal[colOffsetLocal];
							yi[colOffsetGlobal] = yiLocal[colOffsetLocal];
						}		
					}
				}
				globalBarrier();
			}
		} else {
			final int lineOffsetGlobal = offset + threadNrI * dimJ;
			final int lineOffsetLocal = threadNrI * dimJ;
			if (threadNrI < dimI) {
				//Copy from source array to local buffer...
				for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
					final int colOffsetGlobal = lineOffsetGlobal + threadNrJ + indexJ*threadsJ;
					final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;

					xrLocal[colOffsetLocal] = xr[colOffsetGlobal];
					xiLocal[colOffsetLocal] = xi[colOffsetGlobal];
					yrLocal[colOffsetLocal] = yr[colOffsetGlobal];
					yiLocal[colOffsetLocal] = yi[colOffsetGlobal];
				}
			}
			localBarrier();
			
			if (threadNrI < dimI) {
				//This will compute the FFT for all the row entries of the matrix
				//An improvement can be tried... Half the work items compute top half of xr while the other half work items
				//compute the top half of yr.
				perfectShuffleFFTInput(xrLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(yrLocal, jlAdj, colBlockSize);
			}
			localBarrier();
			
			for (int levelA=1, stepA=N; levelA < N; levelA <<= 1) {
				int level2 = levelA << 1;
				stepA >>>= 1;
			
				if (threadNrI < dimI) {
					for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
						if (ilOffset < dimI / 2) {
							computeFFTStep(xrLocal, xiLocal, stepA, levelA, level2, jlOffset + tidx, N, 0);
						} else {
							computeFFTStep(yrLocal, yiLocal, stepA, levelA, level2, jlOffset + tidx, N, -dimI/2);
						}
					}
				}
				localBarrier(); //Ensure all work-items of work-group have written data to local memory
			}		
		}
		
		for (int lineBlockIndex = 0; lineBlockIndex < lineBlockSize; lineBlockIndex++) {
			final int lineOffsetLocal = threadNrI * dimJ;
			if (ilOffset + lineBlockIndex*threadsI < dimI) {				
				if (lineBlockSize == 1) {
					//Problem we are concurrently reading and writing to the same buffer...
					//So we must ensure each thread exchanges independent values only.
					//No problem to be inside loop, since loop will run only once.
					//In this conditional block there will be one thread per each matrix line.
				
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						if (jlAdj + indexJ > threadNrI) {
							float tempARe = xrLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempAIm = xiLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempBRe = xrLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							float tempBIm = xiLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							xrLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempARe;
							xiLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempAIm;
							xrLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBRe;
							xiLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBIm;
							tempARe = yrLocal[threadNrI * dimJ + jlAdj + indexJ];
							tempAIm = yiLocal[threadNrI * dimJ + jlAdj + indexJ];
							tempBRe = yrLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							tempBIm = yiLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							yrLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempARe;
							yiLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempAIm;
							yrLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBRe;
							yiLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBIm;
						}
					}
				} else {
					//If local memory can not handle the whole matrix, then it cannot be transposed in
					//local memory. Instead read-back transposed data from global memory.
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						final int colOffsetGlobal = offset + (threadNrJ + indexJ*threadsJ) * dimI + ilOffset + lineBlockIndex*threadsI;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;
						//Since global array has multiple matrices, one needs to translate only the offset within the matrix, while
						//keeping the global matrix start index in both dimensions.
						xrLocal[colOffsetLocal] = xr[colOffsetGlobal];
						xiLocal[colOffsetLocal] = xi[colOffsetGlobal];
						yrLocal[colOffsetLocal] = yr[colOffsetGlobal];
						yiLocal[colOffsetLocal] = yi[colOffsetGlobal];
					}		
				}
			}
			
			localBarrier();
			
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				perfectShuffleFFTInput(xrLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(xiLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(yrLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(yiLocal, jlAdj, colBlockSize);
			}
			localBarrier();
	
			//This will compute FFT for the top half entries of the matrix
			for (int levelB=1, stepB=N; levelB < N; levelB <<= 1) {
				int level2B = levelB << 1;
				stepB >>>= 1;
			
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
						computeFFTStep(xrLocal, xiLocal, stepB, levelB, level2B, jlOffset + tidx, N, 0);
						computeFFTStep(yrLocal, yiLocal, stepB, levelB, level2B, jlOffset + tidx, N, 0);
					}
				}
				localBarrier(); //Ensure data from all local work-items is written to the local memory
			}
			
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				if (lineBlockSize > 1) {
					//Write local values to global memory, transposing the data. Since local memory is not big enough to handle the full matrix,
					//we must copy data to global memory, so that it can be transposed.			
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						final int colOffsetGlobal = offset + (threadNrJ + indexJ*threadsJ) * dimI + ilOffset + lineBlockIndex*threadsI;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;

						xr[colOffsetGlobal] = xrLocal[colOffsetLocal];
						xi[colOffsetGlobal] = xiLocal[colOffsetLocal];
						yr[colOffsetGlobal] = yrLocal[colOffsetLocal];
						yi[colOffsetGlobal] = yiLocal[colOffsetLocal];
					}
				}
			}
			globalBarrier();
		}
		
		for (int lineBlockIndex = 0; lineBlockIndex < lineBlockSize; lineBlockIndex++) {
			final int lineOffsetGlobal = offset + (ilOffset + lineBlockIndex*threadsI) * dimJ;
			final int lineOffsetLocal = threadNrI * dimJ;
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				if (lineBlockSize == 1) {
					//Transpose back the matrix
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						if (jlAdj + indexJ > threadNrI) { 
							float tempARe = xrLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempAIm = xiLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempBRe = xrLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							float tempBIm = xiLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							xrLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempARe;
							xiLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempAIm;
							xrLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBRe;
							xiLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBIm;
							tempARe = yrLocal[threadNrI * dimJ + jlAdj + indexJ];
							tempAIm = yiLocal[threadNrI * dimJ + jlAdj + indexJ];
							tempBRe = yrLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							tempBIm = yiLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							yrLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempARe;
							yiLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempAIm;
							yrLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBRe;
							yiLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBIm;
						}
					}
				} else {
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						//Read data back to local memory...
						final int colOffsetGlobal = lineOffsetGlobal + threadNrJ + indexJ*threadsJ;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;
						xrLocal[colOffsetLocal] = xr[colOffsetGlobal];
						xiLocal[colOffsetLocal] = xi[colOffsetGlobal];
						yrLocal[colOffsetLocal] = yr[colOffsetGlobal];
						yiLocal[colOffsetLocal] = yi[colOffsetGlobal];
					}				
				}
			}
			localBarrier();
			//dumpArray("xr", xrLocal[il]);
			//dumpArray("xi", xiLocal[il]);
	
			
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				//By now xr, xi and yr, yi have the FFT transform with real and imaginary values
				//NOTE: That cross-correlation is being computed by a convolution, thus the product must be made like x[-n, -m]*y[n, m],
				//only the non-zero valued data is reversed before padding with zeros.
				
				//Note: this can also be done in parallel
				for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
					//(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
					float tempRe = xrLocal[threadNrI * dimJ + jlAdj + indexJ]*yrLocal[threadNrI * dimJ + jlAdj + indexJ]-xiLocal[threadNrI * dimJ + jlAdj + indexJ]*yiLocal[threadNrI * dimJ + jlAdj + indexJ];
					float tempIm = xrLocal[threadNrI * dimJ + jlAdj + indexJ]*yiLocal[threadNrI * dimJ + jlAdj + indexJ]+xiLocal[threadNrI * dimJ + jlAdj + indexJ]*yrLocal[threadNrI * dimJ + jlAdj + indexJ];
					xrLocal[threadNrI * dimJ + jlAdj + indexJ] = tempRe / (float)(dimJ * dimJ); //Must divide once to compensate IFFT to FFT in X and another for IFFT to FFT in Y
					xiLocal[threadNrI * dimJ + jlAdj + indexJ] = tempIm / (float)(dimJ * dimJ); 
				}
			}
			localBarrier();
	
			
			/*dumpArray("xrProd", xrLocal[il]);
			dumpArray("xiProd", xiLocal[il]);*/
	
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				perfectShuffleFFTInput(xrLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(xiLocal, jlAdj, colBlockSize);
			}
			localBarrier();
						
			//Compute the IFFT...
			for (int levelC=1, stepC=N; levelC < N; levelC <<= 1) {
				int level2C = levelC << 1;
				stepC >>>= 1;
			
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
						computeFFTStep(xiLocal, xrLocal, stepC, levelC, level2C, jlOffset + tidx, N, 0);
					}
				}
				localBarrier();
			}
				
			/*dumpArray("xrFFT_1Half", xrLocal[il]);
			dumpArray("xiFFT_1Half", xiLocal[il]);*/
			
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				if (lineBlockSize > 1) {
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						final int colOffsetGlobal = lineOffsetGlobal + threadNrJ + indexJ*threadsJ;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;

						xr[colOffsetGlobal] = xrLocal[colOffsetLocal];
						xi[colOffsetGlobal] = xiLocal[colOffsetLocal];
					}						
				}			
			}
			globalBarrier();
		}
		
		for (int lineBlockIndex = 0; lineBlockIndex < lineBlockSize; lineBlockIndex++) {
			final int lineOffsetLocal = threadNrI * dimJ;
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				if (lineBlockSize == 1) {			
					//Transpose matrix again...
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						if (jlAdj + indexJ > threadNrI) {
							float tempARe = xrLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempAIm = xiLocal[threadNrI * dimJ + jlAdj + indexJ];
							float tempBRe = xrLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							float tempBIm = xiLocal[(jlAdj + indexJ) * dimJ + threadNrI];
							xrLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempARe;
							xiLocal[(jlAdj + indexJ) * dimJ + threadNrI]=tempAIm;
							xrLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBRe;
							xiLocal[threadNrI * dimJ + jlAdj + indexJ]=tempBIm;
						}
					}
				} else {
					for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
						//Read transposed data back to local memory
						final int colOffsetGlobal = offset + (threadNrJ + indexJ*threadsJ) * dimI + ilOffset + lineBlockIndex*threadsI;
						final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;

						xrLocal[colOffsetLocal] = xr[colOffsetGlobal];
						xiLocal[colOffsetLocal] = xi[colOffsetGlobal];
					}
				}
			}
			localBarrier();
			
			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				perfectShuffleFFTInput(xrLocal, jlAdj, colBlockSize);
				perfectShuffleFFTInput(xiLocal, jlAdj, colBlockSize);
			}
			localBarrier();
	
			for (int levelD=1, stepD=N; levelD < N; levelD <<= 1) {
				int level2D = levelD << 1;
				stepD >>>= 1;
			
				if (ilOffset + lineBlockIndex*threadsI < dimI) {
					for (int tidx = 0; tidx < (colBlockSize >>> 1); tidx++) {
						computeFFTStep(xiLocal, xrLocal, stepD, levelD, level2D, jlOffset + tidx, N, 0);
					}
				}
				localBarrier();
			}
	
			/*dumpArray("xrFFT_2Half", xrLocal[il]);
			dumpArray("xiFFT_2Half", xiLocal[il]);*/

			if (ilOffset + lineBlockIndex*threadsI < dimI) {
				//Put transposed data back into xr only
				for (int indexJ = 0; indexJ < colBlockSize; indexJ++) {
					final int colOffsetGlobal = offset + (threadNrJ + indexJ*threadsJ) * dimI + ilOffset + lineBlockIndex*threadsI;
					final int colOffsetLocal = lineOffsetLocal + threadNrJ + indexJ*threadsJ;
					
					float value = xrLocal[colOffsetLocal];
					xr[colOffsetGlobal] = value;
					if (computeMax && max(maxVal, value) == value) {
						maxVal = value;
						maxI = threadNrJ + indexJ*threadsJ;
						maxJ = ilOffset + lineBlockIndex*threadsI;
					}					
				}
			}
		}
		
		if (computeMax) {
			computeMaximum(maxs, tileIdx, numberOfUsedTiles, maxVal, maxI, maxJ);
		}
	}
}
