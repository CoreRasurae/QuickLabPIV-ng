package pt.quickLabPIV.images;

import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.images.ImageFactoryEnum;

public class ImageCrossCorrelationFFTParStdJob16BitDepthTests extends ImageCrossCorrelationFFTParStdJobTests {
	private static ComputationDevice gpuDevice;
	private static ComputationDevice cpuDevice;
	private static ComputationDevice device;

    private static class CLKernelManager extends KernelManager {
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.ACC, Device.TYPE.GPU, Device.TYPE.CPU);
    	}
    }
	
	@BeforeClass
	public static void setup() {
	    PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image16Bit);

		gpuDevice = DeviceManager.getSingleton().getGPU();
		cpuDevice = DeviceManager.getSingleton().getCPU();
		
		if (gpuDevice != null) {
			device = gpuDevice;
		} else {
			device = cpuDevice;
		}
		
		KernelManager.setKernelManager(new CLKernelManager());
		
		assumeTrue("No OpenCL device available", device != null);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x64GPUPass() {
	    assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, gpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopLeft128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
	    testSyntheticImageCrossCorrelationTopLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomLeft128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomLeftPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationTopRight128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationTopRightPass(128, 128, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight64x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(64, 64, cpuDevice);
	}

	@Test
	public void testSyntheticImageCrossCorrelationBottomRight128x64CPUPass() {
	    assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
		testSyntheticImageCrossCorrelationBottomRightPass(128, 128, cpuDevice);
	}
}
