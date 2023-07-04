package pt.quickLabPIV.tests;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.aparapi.Kernel;
import com.aparapi.Kernel.EXECUTION_MODE;
import com.aparapi.Range;
import com.aparapi.device.Device;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;

public class AtomicSimpleOpenCLTest {
    private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

	private Kernel kernel;
	private Range range = null; 
	
	private AtomicInteger[] max = new AtomicInteger[1]; 
	
	public AtomicSimpleOpenCLTest() {
		max[0] = new AtomicInteger(0);
	}
	
	public int findMaximum() {
		if (range == null) {
			kernel = new AtomicSimpleKernelTest(max);
			range = Range.create2D(gpuDevice.getAparapiDevice(), 1, 1);
		}
		kernel.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
		kernel.execute(range);
	
		int val = max[0].get();
		return val;
	}

	@Test
	public void simpleAparapiModeTest() {
		AtomicSimpleOpenCLTest test = new AtomicSimpleOpenCLTest();
		int result = test.findMaximum();
	}
}
