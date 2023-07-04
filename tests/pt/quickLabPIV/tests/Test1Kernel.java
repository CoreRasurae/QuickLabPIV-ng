package pt.quickLabPIV.tests;

import com.aparapi.Kernel;

public class Test1Kernel extends Kernel {

	public Test1Kernel(final int[] matrixIn, final int[] matrixOut) {
		this.matrixIn = matrixIn;
		this.matrixOut = matrixOut;
	}
	
	final int[] matrixIn;
	int[] matrixOut;
	final int[] bufferLocal_$local$  = new int[32 * 32];
	
	@Override
	public void run() {
		final int i;
		final int j;
		final int il;
		final int jl;
		i = this.getGlobalId(0);
		j = this.getGlobalId(1);
		matrixOut[j * getGlobalSize(0) + i] = matrixIn[j * getGlobalSize(0) + i] + matrixIn[j * getGlobalSize(0) + i];
		//System.out.println(j + ", " + i);
		il = this.getLocalId(0);
		jl = this.getLocalId(1);
		//this.getPassId()
		
		//matrix[i*j]
	}

}
