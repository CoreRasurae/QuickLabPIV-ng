// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.xcorr;

import com.aparapi.Kernel;

public class NormCrossCorrelationKernel extends Kernel {

	public void setParameters(final float[] matrixInF, final float[] matrixInG, final float[] matrixOut,
            final int[] threadOutputStart,  final int[] threadOffsetI, final int[] threadOffsetJ,
            final int[] inputGeometry, final int[] outputGeometry, final int[] tilesGeometry) {
        this.matrixInF = matrixInF;
        this.matrixInG = matrixInG;
        this.matrixOut = matrixOut;
        this.threadOutputStart = threadOutputStart;
        this.threadOffsetI = threadOffsetI;
        this.threadOffsetJ = threadOffsetJ;
        this.inputGeometry = inputGeometry;
        this.outputGeometry = outputGeometry;
        this.tilesGeometry = tilesGeometry;     
    }
	
	//final float[] avgsF;
	float[] matrixInF;          //Input matrix F (Possibly matrix of sub-matrices) - All sub-matrices must have the same geometry
	float[] matrixInG;          //Input matrix G (Possibly matrix of sub-matrices) - Both matrices (F,G) must have the same geometry
	int[] threadOutputStart;    //Contains the start I coordinate for the input matrix (that contains the sub-matrix to process)
	int[] threadOffsetI;        //Contains the result I offset within the output subMatrix that the work-item is to compute 
	int[] threadOffsetJ;        //Contains the result J offset within the output subMatrix that the work-item is to compute
	int[] inputGeometry;        //Geometry of the input sub-Matrix (I,J)
	int[] outputGeometry;       //Geometry of the output sub-Matrix (I,J)
	int[] tilesGeometry;		  //I, J, K - tiles geometry
	float[] matrixOut;
		
	private final short signX(short x) {
		//When called with value -2 fails with error -9999
		//return (short)((x+x+1)/(Math.abs(x+x+1))); // if (x >= 0) return 1, else return -1
		//However the following code, has no such problem
		short value = (short)(x + x + 1);
		return (short)(value/(Math.abs(value))); // if (x >= 0) return 1, else return -1
	}

	private final short relocateX(final short x, final short dimX) {
		short result;
		
		result = (short)((signX(x) + 1) * (x + 1) / 2); // if (x >= 0) then result=x+1, else result = 0 
		result = (short)((signX((short)(dimX - result)) + 1) * result / 2); //if (result >= dimX) then result = 0
		
		return result;
	}
		
	@Override
	public void run() {
		final int i;
		final int j;
		final int il;
		final int jl;

		i = getGlobalId(0);
		j = getGlobalId(1);
		il = getLocalId(0);
		jl = getLocalId(1);
		//this.getPassId() - Used for loops indices....
		final short avgIndex=(short)(threadOutputStart[i * getGlobalSize(1) + j]/outputGeometry[0]);
		final int matrixInputStart = (threadOutputStart[i * getGlobalSize(1) + j]/outputGeometry[0]*(inputGeometry[0]+1));
		final int matrixOutputStart = threadOutputStart[i * getGlobalSize(1) + j];
		final short subMatrixI = (short)threadOffsetI[i];
		final short subMatrixJ = (short)threadOffsetJ[j];
		
		float accum = 0.0f;
		float stdDevF = 0.0f;
		float stdDevG = 0.0f;
		short fj, fi;
		short gj, gi;
		float avgF = 0.0f;
		float avgG = 0.0f;
		for (short indexN = (short)(-inputGeometry[0]/2); indexN <= inputGeometry[0]/2; indexN++) {
			for (short indexM = (short)(-inputGeometry[1]/2); indexM <= inputGeometry[1]/2; indexM++) {
				fi = relocateX(indexN, (short)inputGeometry[0]);
				fj = relocateX(indexM, (short)inputGeometry[1]);

				gi = relocateX((short)(subMatrixI + indexN), (short)inputGeometry[0]);
				gj = relocateX((short)(subMatrixJ + indexM), (short)inputGeometry[1]);

				avgF += matrixInF[matrixInputStart + fi * (inputGeometry[0]+1) + fj];
				avgG += matrixInG[matrixInputStart + gi * (inputGeometry[0]+1) + gj];
			}
		}
		avgF/=(float)(inputGeometry[0]*inputGeometry[1]);
		avgG/=(float)(inputGeometry[0]*inputGeometry[1]);
		
		for (short indexN = (short)(-inputGeometry[0]/2); indexN <= inputGeometry[0]/2; indexN++) {
			for (short indexM = (short)(-inputGeometry[1]/2); indexM <= inputGeometry[1]/2; indexM++) {
				//gjBeforeRelocate = (short)(-1 + -1);
				
				fi = relocateX(indexN, (short)inputGeometry[0]);
				fj = relocateX(indexM, (short)inputGeometry[1]);
				
				gi = relocateX((short)(subMatrixI + indexN), (short)inputGeometry[0]);
				gj = relocateX((short)(subMatrixJ + indexM), (short)inputGeometry[1]);
				
				//Option A)
				//Use wrap around principle to avoid having to add additional 0 padding, but use a weight mask with proper subMatrixI and subMatrixJ
				//For instance a 2x2 matrix will have 
				//Fweight and Gweight matrices
				//           0    0    0 (i = -1, j= -1, 0, 1)
				//Fweight =  0    1    1 (i = 0, j= -1, 0 ,1)
				//           0    1    1 (i = 1, j= -1, 0, 1)
				//
				//Gweight(m,n) = many maps, but logic is if i + m < 0 || > 1 weight is zero... total number of maps is: (2 * n - 1)^2 * (2 * n - 1)^2  = (2*n-1)^4 
				//Will take 27MB for 256 matrix size
				//
				//Option B)
				//Pad each input sub-matrix with 000s around it so that it gets (2 + 2) * (2 + 2) * 4bytes = 64bytes per each F matrix
				//and only needs padding for each F matrix. Overall (dimI + 2*(dimI-1))*(dimJ + 2*(dimJ-1))*4 bytes per F matrix.
				//For a 2x2 G matrix, do          
				// 0 0 0 0(i = -1, j= -1, 0, 1, 2)
				// 0 V V 0(i = 0,  j= -1, 0, 1, 2)
				// 0 V V 0(i = 1,  j= -1, 0, 1, 2)
				// 0 0 0 0(i = 2,  j= -1, 0, 1, 2)
				//For a 3x3 G matrix, do
				// 0 0 0 0 0 0 0
				// 0 0 0 0 0 0 0 (dimI + 4) * (dimJ + 4) -> dimI + 2*(dimI-1)
				// 0 0 V V V 0 0
				// 0 0 V V V 0 0
				// 0 0 V V V 0 0
				// 0 0 0 0 0 0 0
				// 0 0 0 0 0 0 0
				//
				//Option C)
				//Use math to compress the zero padding to size 1 -> (dimI + 1) * (dimJ + 1)
				//0 0 0 0
				//0 V V V
				//0 V V V
				//0 V V V
		        //
				//Like so:
				//sign(x) = value / abs(value)
                //
			    //indexX = (signX(x) + 1) * x / 2 + (signX(x) - 1) / 2; // if (x >= 0) then result=x, else result = -1
                //
                //Solves left and above zero padding, however right padding is still required, which also can be solved...
				//
				//indexX = (signX(dimX - 1 - result) + 1) * result / 2 + (signX(dimX - 1 - result) - 1) / 2; //if (result >= dimX) then result = -1
				
				float valueF = matrixInF[matrixInputStart + fi * (inputGeometry[0]+1) + fj]-avgF;//avgsF[avgIndex];
				float valueG = matrixInG[matrixInputStart + gi * (inputGeometry[0]+1) + gj]-avgG;
				
				stdDevF += valueF*valueF;
				stdDevG += valueG*valueG;
				
				accum += (valueF * valueG); 
			}
		}
		
		accum /= Math.sqrt(stdDevF + stdDevG);
		
		int outIndex = matrixOutputStart + 
				(subMatrixI+outputGeometry[0]/2)*outputGeometry[1] + 
				(subMatrixJ+outputGeometry[1]/2);
		
		matrixOut[outIndex] = accum;
	}
}



