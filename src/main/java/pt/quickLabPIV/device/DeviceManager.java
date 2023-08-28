// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.opencl.OpenCLPlatform;

import net.openhft.affinity.CpuLayout;
import net.openhft.affinity.impl.VanillaCpuLayout;

public class DeviceManager {
	private final static AtomicReference<DeviceManager> singleton = new AtomicReference<DeviceManager>(null);
	private static DeviceConfigurator configurator = new DeviceConfigurator();
	private final AtomicBoolean configurationPerformed = new AtomicBoolean(false);
	private final List<ComputationDevice> availableGPUs = new ArrayList<ComputationDevice>(4);
	
	private final List<ComputationDevice> availableCPUs = new ArrayList<ComputationDevice>(4);
	
	private final List<ComputationDevice> javaDevices = new ArrayList<ComputationDevice>(4);
	
	private final ComputationDevice JTP;
	
	private boolean linuxOS = false;
	private CpuLayout layout;
	
	public static DeviceManager getSingleton() {
		DeviceManager manager = singleton.get();
		if (manager == null) {
			manager = new DeviceManager();
			if (!singleton.compareAndSet(null, manager)) {
				manager = singleton.get();
			}
		}
		
		return manager;
	}
	
	protected DeviceManager() {
		JavaDevice.THREAD_POOL.setMaxWorkGroupSize(1024);
		JavaDevice.THREAD_POOL.setMaxWorkItemDimensions(3);
		JavaDevice.THREAD_POOL.setMaxWorkItemSize(new int[] {1024,1024,1024});
		JTP = new ComputationDevice(JavaDevice.THREAD_POOL);
		JTP.setLocalMemoryBytes(49152);
		if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).indexOf("nux") >= 0) {
		    linuxOS = true;
    		try {
                layout = VanillaCpuLayout.fromCpuInfo();
            } catch (IOException e) {         
                throw new DeviceManagerException("Failed to obtain the CPU Info from the machine", e);
            }
		} else {
		    linuxOS = false;		    
		}
	}
	
	public synchronized void detectDevices() {
		OpenCLDevice.setConfigurator(configurator);
		javaDevices.clear();
		availableGPUs.clear();
		availableCPUs.clear();
		//
		for (OpenCLPlatform platform : OpenCLPlatform.getUncachedOpenCLPlatforms()) {
			for (OpenCLDevice device : platform.getOpenCLDevices()) {
				ComputationDevice computeDevice = new ComputationDevice(device);
				configurator.configure(computeDevice);
				if (Device.TYPE.GPU.equals(device.getType())) {
					availableGPUs.add(computeDevice);
				} else if (Device.TYPE.CPU.equals(device.getType())) {
					availableCPUs.add(computeDevice);
				}
			}
		}
		javaDevices.add(new ComputationDevice(JavaDevice.THREAD_POOL));
		configurationPerformed.set(true);
	}
	
	private void detectDevicesIfNotDetected() {
		if (!configurationPerformed.get()) {
			synchronized (this) {
				if (!configurationPerformed.get()) {
					detectDevices();
				}
			}
		}
	}
	
	public List<ComputationDevice> getGPUs() {
		detectDevicesIfNotDetected();
		return Collections.unmodifiableList(availableGPUs);
	}
	
	public List<ComputationDevice> getCPUs() {
		detectDevicesIfNotDetected();
		return Collections.unmodifiableList(availableCPUs);
	}
	
	public List<ComputationDevice> getJavaDevices() {
		return Collections.unmodifiableList(javaDevices);
	}
	
	public ComputationDevice getGPU() {
		detectDevicesIfNotDetected();
		return availableGPUs.size() == 0 ? null : availableGPUs.get(0);
	}
	
	public ComputationDevice getCPU() {
		detectDevicesIfNotDetected();
		return availableCPUs.size() == 0 ? null : availableCPUs.get(0);
	}

	public ComputationDevice getJTP() {
		return JTP;
	}

    public int getNumberOfRealCores() {
        if (linuxOS) {
            return layout.sockets() * layout.coresPerSocket();
        } else {
            return Runtime.getRuntime().availableProcessors();
        }
    }
}
