// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.device;

import java.util.ArrayList;
import java.util.List;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.device.Device.TYPE;

/**
 * DeviceConfiguration allows QuickLabPIV-ng to extend its information about the OpenCL devices and provide better tuning/performance for those units.
 * If device is not found in the listed configurations, a generic default configuration is considered instead.
 * <br/><b>NOTE:</b> Current configuration and their usage in code is still far from the desired, as it requires harder tuning work, but the environment has been prepared.
 * @author lpnm
 *
 */
public class DeviceConfiguration {
    /**
     * Specific configurations with tuned parameters for known OpenCL devices.
     */
	public final static List<DeviceConfiguration> SPECIFIC_CONFIGURATIONS = new ArrayList<DeviceConfiguration>(10);
	
	/**
	 * Default configurations to be employed when no specific configuration is available.
	 */
	public final static List<DeviceConfiguration> DEFAULT_CONFIGURATIONS = new ArrayList<DeviceConfiguration>(2);
	
	static {
		//Device type - Device Id - Partial name for matching - Shared Memory - Threads per compute unit - Thread multiple - Largest thread multiple - Memory data width
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "NVIDIA<GPU> - NVS 4200M", false, 48, 16, 16, 8, new IAdditionalDeviceProperties() {
			@Override
			public void apply(OpenCLDevice device) {
				device.setMaxWorkGroupSize(512);
			}
		}));
		
        //defaultConfigurations.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "NVIDIA<GPU> - NVS 5200M", false, 1024, 48, 1024, 8, null));
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "NVIDIA<GPU> - NVS 5200M", false, 48, 16, 16, 8, null));

		//FIXME Ugly Hack: Max threads per SM is 128, but setting to 1024, makes it much faster... find why and properly fix  
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 1050 Ti", false, 1024, 32, 1024, 16, null)); //128 cuda cores*6 compute units=768 cuda cores
		
        //FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RX 550", false, 256, 64, 256, 16, null)); //640 stream processors/10 active compute units
		
		//RX550
		//defaultConfigurations.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "OLAND", false, 64, 64, 64, 16, null)); //320 stream processors/5 active compute units, 128-bit memory width
		
		//FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RX 560", false, 256, 64, 256, 16, null)); //1024 stream processors/16 active compute units, 128-bit memory width

        //FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RX 5700", false, 2304, 64, 256, 32, null)); //1024 stream processors/16 active compute units, 128-bit memory width
		
        //FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GFX1010", false, 2304, 64, 256, 32, null)); //1024 stream processors/16 active compute units, 128-bit memory width

		//HD8790M
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "Oland", false, 64, 64, 1024, 16, null)); //384 stream processors/6 active compute units, 128-bit memory width
		
		//FIXME
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "UHD Graphics 630", true, 256, 64, 256, 64, null)); //192 stream processors/24 active compute units?
		
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "HD Graphics 4000", true, 512, 16, 512, 64, null)); //192 stream processors/24 active compute units?

        //FIXME Ugly Hack: Max threads per SM is 128, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RX 460", false, 256, 64, 256, 16, null)); //896 stream processors/14 active compute units, 128-bit memory width

        //FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "Baffin", false, 256, 64, 256, 16, null)); //896 stream processors/14 active compute units, 128-bit memory width
		
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GeForce GT 1030", false, 128, 32, 256, 8, null)); //384 cuda cores/3 active compute units, 64-bit memory width

        //Correct configuration would be:
        //defaultConfigurations.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 750 Ti", false, 640, 32, 640, 16, null)); //640 cuda cores/5 active compute units (each SMM has 4 separate processing blocks each with 32 threads and scheduler), 128-bit memory width
		//defaultConfigurations.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 750 Ti", false, 4*32, 32, 640, 16, null)); //640 cuda cores/5 active compute units (each SMM has 4 separate processing blocks each with 32 threads and scheduler), 128-bit memory width
        //FIXME Ugly Hack: Max threads per SM is 192, but setting to 768, makes it much faster... find why and properly fix		
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 750 Ti", false, 640, 32, 640, 16, null)); //640 cuda cores/5 active compute units (each SMM has 4 separate processing blocks each with 32 threads and scheduler), 128-bit memory width
		
        //FIXME Ugly Hack: Max threads per SM is 192, but setting to 768, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GeForce GTX 760", false, 768, 32, 768, 32, null)); //1152 cuda cores/6 active compute units, 256-bit memory width
		
		//FIXME Ugly Hack: Max threads per SM is 192, but setting to 768, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GT 730", false, 768, 32, 768, 8, null)); //384 cuda cores/2 active compute units, 64-bit memory width
		
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 950M", false, 128, 32, 128, 16, null)); //128 cuda cores/5 active compute units, 128-bit memory width
		
                //FIXME Ugly Hack: Max threads per SM is 192, but setting to 768, makes it much faster... find why and properly fix
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "Tesla K20c", false, 768, 32, 768, 40, null)); //2496 cuda cores/13 active compute units, 320-bit memory width
		//Ex with K20c: Number of work groups should be > 2496/64=39 with max thread size or
		//Work items could be 128x2 groups, or 256x4 groups, or 512x8 groups - 2496/128*2=39/13=3 (is multiple) makes full use of CUs with a group size of at least to 2*3, 4*3 or 8*3 respectively
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 1080 Ti", false, 192, 32, 384, 44, null)); //3584 cuda cores/28 active compute units, 352-bit memory width (true max multiple of 192 is 64)
		
		//FIXME Ugly Hack: Max threads per SM is 128, but setting to 1024, makes it much faster...   
		SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "GTX 1080", false, 128, 32, 1024, 32, null)); //2560 cuda cores/20 active compute units, 256-bit memory width (true max multiple is 1024)
                
        //FIXME Ugly Hack: Max threads per SM is 64, but setting to 1024, makes it much faster...   
        SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RTX 2070", false, 64, 32, 1024, 32, null)); //2304 cuda cores/36 active compute units, 256-bit memory width

		//FIXME Ugly Hack: Max threads per SM is 64, but setting to 2048, makes it much faster...   
        SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RTX 2080", false, 64, 32, 128, 32, null)); //2944 cuda cores/46 active compute units, 256-bit memory width
        
        SPECIFIC_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "RTX 3050 Ti", false, 128, 32, 512, 16, null)); //2560 cuda cores/20 active compute units = 128 threads/SM = 4 warp scheduler x 32 thread wave, 128-bit memory width
        
        DEFAULT_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "AMD", false, 1024, 32, 1024, 16, null));
        
        DEFAULT_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "NVIDIA", false, 1024, 32, 1024, 16, null));
        
        DEFAULT_CONFIGURATIONS.add(new DeviceConfiguration(Device.TYPE.GPU, -1L, "ANY", false, 1024, 32, 1024, 16, null));
	}
	
	private TYPE type;
	private long id;
	private String name;
	private boolean sharedMemory;
	private int threadsPerComputeUnit;
	private int threadMultiple;
	private int maxThreadMultiple;
	private int memoryWidthBytes;
	private IAdditionalDeviceProperties additionalProperties;
	
	public DeviceConfiguration(Device.TYPE _type, long _id, String _name, 
			boolean _sharedMemory, int _threadsPerComputeUnit, int _threadMultiple, int _maxThreadMultiple, int _memoryWidthBytes,
			IAdditionalDeviceProperties _additionalProperties) {
		type = _type;
		id = _id;
		name = _name.toUpperCase();
		sharedMemory = _sharedMemory;
		threadsPerComputeUnit = _threadsPerComputeUnit;
		threadMultiple = _threadMultiple;
		maxThreadMultiple = _maxThreadMultiple;
		memoryWidthBytes = _memoryWidthBytes;
		additionalProperties = _additionalProperties;
	}
	
	public TYPE getType() {
		return type;
	}
	
	public long getDeviceId() {
		return id;
	}
	
	public String getDeviceName() {
		return name;
	}
	
	public int getThreadsPerComputeUnit() {
		return threadsPerComputeUnit;
	}
	
	public boolean isSharedMemory() {
		return sharedMemory;
	}

	/**
	 * The smallest recommended exact sub-multiple of the overall simultaneous threads across all compute units.
	 * @return the number of threads
	 */
	public int getMinimumThreadMultiple() {
		return threadMultiple;
	}
	
	/**
	 * The maximum number of threads that is an exact sub-multiple of the overall simultaneous threads across all compute units.
	 * @return the number of threads
	 */
	public int getMaxIntegerThreadMultiple() {
	    return maxThreadMultiple;
	}
	
	/**
	 * Retrieves the memory width in bytes
	 * @return memory width
	 */
	public int getMemoryWidth() {
		return memoryWidthBytes;
	}
	
	public IAdditionalDeviceProperties getAdditionalProperties() {
		return additionalProperties;
	}
}
