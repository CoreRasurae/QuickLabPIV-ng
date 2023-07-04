package pt.quickLabPIV.jobs.xcorr;

import com.aparapi.Kernel;

public class CrossCorrelationFFTCpuKernel extends Kernel{
	int totalTiles;
	int dimIBlockSize;
	int dimI;
	int dimJ;
	float[] xr;
	float[] xi;
	float[] yr;
	float[] yi;
	

	@Constant
	float[] wConst;
	
	@Constant
	int[] shuffleOrderConst;

	public void setKernelParams(int[] shuffleOrder, float[] w, float[] xr, float xi[], float yr[], float yi[],
			int dimIBlockSize, int[] inputGeometry, int[] outputGeometry, int numberOfUsedTiles) {
		this.shuffleOrderConst = shuffleOrder;
		this.wConst = w;
		this.xr = xr;
		this.xi = xi;
		this.yr = yr;
		this.yi = yi;
		this.dimIBlockSize = dimIBlockSize;
		this.dimI = outputGeometry[0];
		this.dimJ = outputGeometry[1];
		totalTiles = numberOfUsedTiles;
	}
	
	/**
	 * Shuffles the input array data to mimic the FFT expansion phase, without needing to allocate additional memory (perfect shuffle).
	 * @param inputArray the input array to be perfect shuffled into initial FFT order
	 */
	public void perfectShuffleFFTInput(float[] array, int offset) {
		for (int jl = 0; jl < dimJ; jl++) {
			int src = jl;
			int dst = shuffleOrderConst[src];
			if (dst > src) { 
				float temp = array[offset + src];
				array[offset + src] = array[offset + dst];
				array[offset + dst] = temp;
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
	public void computeFFTStep(float[] xr, float[] xi, final int offset, final int step,  int level, int level2, int tidx, final int N) {
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
		float tempLowerHalfReal = xr[offset + i] + (wr*xr[offset + j]) - (wi*xi[offset + j]);
		float tempLowerHalfImg  = xi[offset + i] + (wr*xi[offset + j]) + (wi*xr[offset + j]);
		
		//Handle the upper half = Ek - Wk Ok
		float tempUpperHalfReal = xr[offset + i] - (wr*xr[offset + j]) + (wi*xi[offset + j]);
		float tempUpperHalfImg  = xi[offset + i] - (wr*xi[offset + j]) - (wi*xr[offset + j]);
		//System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);

		xr[offset + i] = tempLowerHalfReal;
		xi[offset + i] = tempLowerHalfImg;
		//System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
		xr[offset + j] = tempUpperHalfReal;
		xi[offset + j] = tempUpperHalfImg;
		
		//Option B: This one is working with the compiler.... by switching the order of the temporary results
		/*float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
		float tempUpperHalfImg  = xi[i] - (wr*xi[j]) - (wi*xr[j]);
		
		//Handle the lower half = Ek - Wk Ok
		xr[i] = xr[i] + (wr*xr[j]) - (wi*xi[j]);
		xi[i] = xi[i] + (wr*xi[j]) + (wi*xr[j]);

		xr[j] = tempUpperHalfReal;
		xi[j] = tempUpperHalfImg;*/
	}
	
	/*public void dumpArray(String name, int offset, float[] xr) {
		StringBuilder sb = new StringBuilder(100);
		int il = getLocalId(1);
		sb.append("[ ");
		sb.append(name);
		sb.append(" - il=");
		sb.append(il);
		sb.append(" - ");
		for (int indexJ = 0; indexJ < dimJ; indexJ++) {
			sb.append(xr[offset + indexJ]);
			sb.append(", ");
		}
		sb.append("]");
		System.out.println(sb.toString());
	}*/
	
	@Override
	public void run() {
		final int tileNumberJ = getGlobalId(0);
		final int i = getGlobalId(1);
		final int k = getGlobalId(2);
		final int jl = getLocalId(0);
		final int threadNr = getLocalId(1); //Thread number
		
		final int lineBlockSize = dimI / getLocalSize(1);
		final int tileNumberI = i / getLocalSize(1);		
		final int ilOffset = threadNr * lineBlockSize; 
		
		final int N = dimJ;
		
		int tileIdx = getPassId() * getNumGroups(0) * getNumGroups(1) * getNumGroups(2)
				+  k * getNumGroups(0) * getNumGroups(1)
				+  tileNumberI * getNumGroups(0) + 
				tileNumberJ;
		
		final int tileDimensions = (dimI * dimJ);
		final int tileOffset = tileIdx * tileDimensions;
		final int rowOffset = tileOffset + threadNr*dimJ;
		
		if (tileIdx < totalTiles) {
			//This will compute the FFT for all the row entries of the matrix
			//An improvement can be tried... Half the work items compute top half of xr while the other half work items
			//compute the top half of yr.
			perfectShuffleFFTInput(xr, rowOffset);
			perfectShuffleFFTInput(yr, rowOffset);
			
			/*dumpArray("xr_1Shuffle", threadNr * dimJ, xr[tileIdx]);
			dumpArray("xi_1Shuffle", threadNr * dimJ, xi[tileIdx]);*/
			
			for (int levelA=1, stepA=N; levelA < N; levelA <<= 1) {
				int level2 = levelA << 1;
				stepA >>>= 1;
				
				for (int tidx = 0; tidx < N/2; tidx++) {
					computeFFTStep(xr, xi, rowOffset, stepA, levelA, level2, tidx, N);
					computeFFTStep(yr, yi, rowOffset, stepA, levelA, level2, tidx, N);
				}
	
				//globalBarrier(); - Not needed - currently only one thread per matrix per row
			}
		}
		//Ensure all work-items of work-group have written data to local memory
		globalBarrier();		

		//dumpArray("xr_1Half", threadNr * dimJ, xr[tileIdx]);
		//dumpArray("xi_1Half", threadNr * dimJ, xi[tileIdx]);

		if (tileIdx < totalTiles) {
			//Problem we are concurrently reading and writing to the same buffer...
			//So we must ensure each thread exchanges independent values only.
			//No problem to be inside loop, since loop will run only once.
			//In this conditional block there will be one thread per each matrix line.
			for (int indexJ = threadNr; indexJ < dimJ; indexJ++) {
				float tempARe = xr[rowOffset + indexJ];
				float tempAIm = xi[rowOffset + indexJ];
				float tempBRe = xr[tileOffset + indexJ*dimI + threadNr];
				float tempBIm = xi[tileOffset + indexJ*dimI + threadNr];
				xr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				xi[tileOffset + indexJ*dimI + threadNr]=tempAIm;
				xr[rowOffset + indexJ]=tempBRe;
				xi[rowOffset + indexJ]=tempBIm;
				tempARe = yr[rowOffset + indexJ];
				tempAIm = yi[rowOffset + indexJ];
				tempBRe = yr[tileOffset + indexJ*dimI + threadNr];
				tempBIm = yi[tileOffset + indexJ*dimI + threadNr];
				yr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				yi[tileOffset + indexJ*dimI + threadNr]=tempAIm;
				yr[rowOffset + indexJ]=tempBRe;
				yi[rowOffset + indexJ]=tempBIm;
			}
		}
		globalBarrier();

		if (tileIdx < totalTiles) {
			perfectShuffleFFTInput(xr, rowOffset);
			perfectShuffleFFTInput(xi, rowOffset);
			perfectShuffleFFTInput(yr, rowOffset);
			perfectShuffleFFTInput(yi, rowOffset);
	
			//This will compute FFT for the top half entries of the matrix
			for (int levelB=1, stepB=N; levelB < N; levelB <<= 1) {
				int level2B = levelB << 1;
				stepB >>>= 1;
				
				for (int tidx = 0; tidx < N/2; tidx++) {
					computeFFTStep(xr, xi, rowOffset, stepB, levelB, level2B, tidx, N);
					computeFFTStep(yr, yi, rowOffset, stepB, levelB, level2B, tidx, N);
				}
				//globalBarrier(); - Not needed - currently only one thread per matrix per row
			}
		}
		globalBarrier(); //Ensure data from all local work-items is written to the local memory

		if (tileIdx < totalTiles) {
			//Transpose back the matrix
			for (int indexJ = threadNr; indexJ < dimJ; indexJ++) {
				float tempARe = xr[rowOffset + indexJ];
				float tempAIm = xi[rowOffset + indexJ];
				float tempBRe = xr[tileOffset + indexJ*dimI + threadNr];
				float tempBIm = xi[tileOffset + indexJ*dimI + threadNr];
				xr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				xi[tileOffset + indexJ*dimI + threadNr]=tempAIm;
				xr[rowOffset + indexJ]=tempBRe;
				xi[rowOffset + indexJ]=tempBIm;
				tempARe = yr[rowOffset + indexJ];
				tempAIm = yi[rowOffset + indexJ];
				tempBRe = yr[tileOffset + indexJ*dimI + threadNr];
				tempBIm = yi[tileOffset + indexJ*dimI + threadNr];
				yr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				yi[tileOffset + indexJ*dimI + threadNr]=tempAIm;
				yr[rowOffset + indexJ]=tempBRe;
				yi[rowOffset + indexJ]=tempBIm;
			}
		}
		globalBarrier();
		
		//dumpArray("xr", threadNr*dimJ, xr[tileIdx]);
		//dumpArray("xi", threadNr*dimJ, xi[tileIdx]);

		if (tileIdx < totalTiles) {
			//By now xr, xi and yr, yi have the FFT transform with real and imaginary values
			//NOTE: That cross-correlation is being computed by a convolution, thus the product must be made like x[-n, -m]*y[n, m],
			//only the non-zero valued data is reversed before padding with zeros.
			for (int indexJ = 0; indexJ < dimJ; indexJ++) {
				//(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
				float tempRe = xr[rowOffset + indexJ]*yr[rowOffset + indexJ]-xi[rowOffset + indexJ]*yi[rowOffset + indexJ];
				float tempIm = xr[rowOffset + indexJ]*yi[rowOffset + indexJ]+xi[rowOffset + indexJ]*yr[rowOffset + indexJ];
				xr[rowOffset + indexJ] = tempRe / (float)(dimJ * dimJ); //Must divide once to compensate IFFT to FFT in X and another for IFFT to FFT in Y
				xi[rowOffset + indexJ] = tempIm / (float)(dimJ * dimJ); 
			}
	
			//dumpArray("xrProd", threadNr*dimJ, xr[tileIdx]);
			//dumpArray("xiProd", threadNr*dimJ, xi[tileIdx]);
	
			perfectShuffleFFTInput(xr, rowOffset);
			perfectShuffleFFTInput(xi, rowOffset);
			
			//Compute the IFFT...
			for (int levelC=1, stepC=N; levelC < N; levelC <<= 1) {
				int level2C = levelC << 1;
				stepC >>>= 1;
				
				for (int tidx = 0; tidx < N/2; tidx++) {
					computeFFTStep(xi, xr, rowOffset, stepC, levelC, level2C, tidx, N);
				}
				//globalBarrier(); - Not needed - currently only one thread per matrix per row
			}
		}
		globalBarrier();
			
		//dumpArray("xrFFT_1Half", threadNr*dimJ, xr[tileIdx]);
		//dumpArray("xiFFT_1Half", threadNr*dimJ, xi[tileIdx]);

		if (tileIdx < totalTiles) {
			//Transpose matrix again...
			for (int indexJ = threadNr; indexJ < dimJ; indexJ++) {
				float tempARe = xr[rowOffset + indexJ];
				float tempAIm = xi[rowOffset + indexJ];
				float tempBRe = xr[tileOffset + indexJ*dimI + threadNr];
				float tempBIm = xi[tileOffset + indexJ*dimI + threadNr];
				xr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				xi[tileOffset + indexJ*dimI + threadNr]=tempAIm;
				xr[rowOffset + indexJ]=tempBRe;
				xi[rowOffset + indexJ]=tempBIm;
			}
		}
		globalBarrier();
		
		if (tileIdx < totalTiles) {
			perfectShuffleFFTInput(xr, rowOffset);	
			perfectShuffleFFTInput(xi, rowOffset);
	
			for (int levelD=1, stepD=N; levelD < N; levelD <<= 1) {
				int level2D = levelD << 1;
				stepD >>>= 1;
				
				for (int tidx = 0; tidx < N/2; tidx++) {
					computeFFTStep(xi, xr, rowOffset, stepD, levelD, level2D, tidx, N);
				}
			}
		}
		globalBarrier();
		
		if (tileIdx < totalTiles) {
			//Transpose matrix again...
			for (int indexJ = threadNr; indexJ < dimJ; indexJ++) {
				float tempARe = xr[rowOffset + indexJ];
				float tempBRe = xr[tileOffset + indexJ*dimI + threadNr];
				xr[tileOffset + indexJ*dimI + threadNr]=tempARe;
				xr[rowOffset + indexJ]=tempBRe;
			}
		}
		
	/*	dumpArray("xrFFT_2Half", threadNr*dimJ, xr[tileIdx]);
		dumpArray("xiFFT_2Half", threadNr*dimJ, xi[tileIdx]);*/
	}
}
