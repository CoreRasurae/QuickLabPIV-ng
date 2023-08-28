// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.device;

import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;

public class ComputationDevice {
	private Device device;
	private long deviceId;
	private String deviceName;

	private long localMemoryBytes;
	private long globalMemoryBytes;
	private int  memoryWidthBytes;

	private int maxThreadsPerWorkGroup;
	private int minimumThreadMultiple;
	private int greatestThreadGroupCommonDivisor;
	
	private int maxCores;
	private int threadsPerComputeUnit;

	private int maxWorkItemDimensions;
	private int maxWorkItems[];
	
	private boolean configuredFromDefaults;
	
	public ComputationDevice(OpenCLDevice _device) {
		device = _device;
		deviceId = _device.getDeviceId();
		deviceName = buildDeviceName(_device);
		globalMemoryBytes = _device.getGlobalMemSize();
		localMemoryBytes = _device.getLocalMemSize();
		maxThreadsPerWorkGroup = _device.getMaxWorkGroupSize();
		maxCores = _device.getMaxComputeUnits();
		maxWorkItemDimensions = _device.getMaxWorkItemDimensions();
		maxWorkItems = _device.getMaxWorkItemSize();
	}

	public ComputationDevice(JavaDevice _device) {
		device = _device;
		deviceId = _device.getDeviceId();
		deviceName = _device.getShortDescription();
		globalMemoryBytes = Runtime.getRuntime().freeMemory();
		localMemoryBytes = globalMemoryBytes;
		maxThreadsPerWorkGroup = _device.getMaxWorkGroupSize();
		maxCores = Runtime.getRuntime().availableProcessors();
		threadsPerComputeUnit = 1;
		maxWorkItemDimensions = _device.getMaxWorkItemDimensions();
		maxWorkItems = _device.getMaxWorkItemSize();
	}

	public static String buildDeviceName(OpenCLDevice device) {
		return device.getShortDescription() + " - " + device.getName();
	}

	public long getDeviceId() {
		return deviceId;
	}	
	
	public Device getAparapiDevice() {
		return device;
	}
	
	/**
	 * Retrieves the device name
	 * @return the name
	 */
	public String getDeviceName() {
		return deviceName;
	}
	
	
	
	/**
	 * Retrieves the device local memory size
	 * @return the memory size in bytes
	 */
	public long getLocalMemoryBytes() {
		return localMemoryBytes;
	}
	
	public void setLocalMemoryBytes(long value) {
		localMemoryBytes = value;
	}
	
	/**
	 * Retrieves the device global memory size
	 * @return the memory size in bytes
	 */
	public long getGlobalMemoryBytes() {
		return globalMemoryBytes;
	}
	
	/**
	 * Gets the device memory width in bytes
	 * @return total bytes that can be transfered in one time
	 */
	public int getMemoryWidthBytes() {
		return memoryWidthBytes;
	}
	
	/**
	 * Retrieves the absolute maximum total number of threads per workgroup
	 * @return the max. number of threads per work group
	 */
	public int getMaxThreadsPerThreadGroup() {
		return maxThreadsPerWorkGroup;
	}
	
	/**
	 * Retrieves the minimum recommended number of threads per thread group
	 * @return the number of threads multiple
	 */
	public int getMinGroupThreads() {
		return minimumThreadMultiple;
	}

	/**
     * Retrieves the maximum recommended number of threads that is an exact sub-multiple of overall device simultaneous threads
     * @return the number of threads multiple
     */
	public int getGreatestThreadGroupCommonDivisor() {
	    return greatestThreadGroupCommonDivisor;
	}
	
	/**
	 * Retrieves the number of compute units available in the chip (either GPU, CPU, or ACC)
	 * @return the maximum number of compute units
	 */
	public int getMaxComputeUnits() {
		return maxCores;
	}
	
	/**
	 * The absolute number of threads per compute core.
	 * @return the number of threads
	 */
	public int getThreadsPerComputeUnit() {
		return threadsPerComputeUnit;
	}

	public int getMemoryWidth() {
		return memoryWidthBytes;
	}
	
	/**
	 * Sets the minimum recommended group threads.
	 * @param multipleSize the group thread size
	 */
	public void setMinGroupThreads(int multipleSize) {
		minimumThreadMultiple = multipleSize;
	}
	

	/**
     * Retrieves the number of compute units available in the chip (either GPU, CPU, or ACC)
     * @param _maxCores the maximum number of compute units
     */
    public void setMaxComputeUnits(int _maxCores) {
        maxCores = _maxCores;
    }

	/**
	 * Sets the maximum number of group threads that is an exact sub-multiple of overall device simultaneous threads.
     * @param multipleSize the group thread size
	 */
	public void setGreatestThreadGroupCommonDivisor(int multipleSize) {
	    greatestThreadGroupCommonDivisor = multipleSize;
	}
	
	public void setThreadsPerComputeUnit(int threads) {
		threadsPerComputeUnit = threads;
	}

	/**
	 * Sets the memory width of the device in bytes.
	 * @param memoryWidth the memory width
	 */
	public void setMemoryWidth(int memoryWidth) {
		memoryWidthBytes = memoryWidth;
	}
	
	public int getMaxWorkItemDimensions() {
		return maxWorkItemDimensions;
	}
	
	public int[] getMaxWorkItems() {
		return maxWorkItems;
	}
	
	public void setConfiguredFromDefaults(boolean _configuredFromDefaults) {
	    configuredFromDefaults = _configuredFromDefaults;
	}
	
	public boolean isConfiguredFromDefaults() {
	    return configuredFromDefaults;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(deviceId);
		sb.append(" - ");
		sb.append(deviceName);
		return sb.toString();
	}
}
