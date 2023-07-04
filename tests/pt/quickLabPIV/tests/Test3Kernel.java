package pt.quickLabPIV.tests;

import com.aparapi.Kernel;

public class Test3Kernel extends Kernel {

	public Test3Kernel(final int[] matrixIn, final long[] matrixOut, final int[] selOut, final int[] originJ, final int[] originI, final float[][][] test3D) {
		this.matrixIn = matrixIn;
		this.matrixOut = matrixOut;
		this.selOut = selOut;
		this.originJ = originJ;
		this.originI = originI;
		this.test3D = test3D;
	}
	
	final int[] matrixIn;
	long[] matrixOut;
	int[] selOut;
	int[] originJ;
	int[] originI;
	float[][][] test3D;
	
	@Local 
	final long[] bufferLocal  = new long[32 * 32];
	
	@Override
	public void run() {
		final int i;
		final int j;
		final int il;
		final int jl;

		i = this.getGlobalId(0);
		j = this.getGlobalId(1);
		il = this.getLocalId(0);
		jl = this.getLocalId(1);
		//this.getPassId() - Used for loops indices....
		bufferLocal[jl * getLocalSize(0) + il] = matrixIn[j * getGlobalSize(0) + i] + matrixIn[j * getGlobalSize(0) + i];
		
		localBarrier(); //Make sure all local data from all work items in the work group is computed and written to
		
		
		if (i < getLocalSize(0) && j < getLocalSize(0)) {
			selOut[jl * getLocalSize(0) + il] = matrixIn[j * getGlobalSize(0) + i] + matrixIn[j * getGlobalSize(0) + i];
			originJ[jl * getLocalSize(0) + il] = j;
			originI[jl * getLocalSize(0) + il] = i;
		}

		//NOTE: This will not work because += is not atomic in openCL, one would have to use atomic_add
		//matrixOut[j/getLocalSize(0) * getGlobalSize(0)/getLocalSize(0) + i/getLocalSize(0)] += bufferLocal[jl * getLocalSize(0) + il];
		
		//This is a reduce implementation where just one work item is working
		if (jl == 0 && il == 0) {
			for (int indexI = 0; indexI < getLocalSize(0)*getLocalSize(0); indexI++) {
				matrixOut[j/getLocalSize(0) * getGlobalSize(0)/getLocalSize(0) + i/getLocalSize(0)] += bufferLocal[indexI];
			}
		} 
	}

}
