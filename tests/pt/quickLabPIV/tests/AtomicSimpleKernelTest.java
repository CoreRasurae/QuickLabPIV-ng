package pt.quickLabPIV.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.aparapi.Kernel;

public class AtomicSimpleKernelTest extends Kernel {
	public AtomicSimpleKernelTest(final AtomicInteger[] max) {
		val = max;
		valLocal = new AtomicInteger[2];
		valLocal[0] = new AtomicInteger(0);
		valLocal[1] = new AtomicInteger(1);
	}
	
	private final AtomicInteger val[];
	
	@Local
	private AtomicInteger valLocal[];;
	
	public void doIt(float me, AtomicInteger[] valLocal) {	    
		atomicSet(valLocal[0], 150);
		valLocal[0] = val[0];
		//Implementar Aparapi para suportar atomic_max, atomic_cmpxchg, atomic_xchg
		me = round(atan(3.0f));
		atomicAdd(valLocal[1], 50);//50.1f);
		atomicMax(valLocal[0], 100);
		int test = atomicGet(valLocal[0]);
		val[0] = valLocal[0];
		atomicSet(val[0], test+1);
	}
	
	@Override
	public void run() {
		float me = 0.0f;
		doIt(me, val);	    
	}

}
