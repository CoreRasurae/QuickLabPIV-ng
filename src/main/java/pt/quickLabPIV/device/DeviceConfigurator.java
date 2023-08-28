// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.device;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.device.Device;
import com.aparapi.device.IOpenCLDeviceConfigurator;
import com.aparapi.device.OpenCLDevice;

public class DeviceConfigurator implements IOpenCLDeviceConfigurator {
	private static Logger logger = LoggerFactory.getLogger(DeviceConfigurator.class); 
	
	private List<DeviceConfiguration> configurations = DeviceConfiguration.SPECIFIC_CONFIGURATIONS;
	private List<DeviceConfiguration> defaultConfigurations = DeviceConfiguration.DEFAULT_CONFIGURATIONS;
	
	public void setConfigurations(List<DeviceConfiguration> configs) {
		configurations = configs;
	}

	protected DeviceConfiguration getConfiguration(Device.TYPE type, long deviceId, String name) {
		DeviceConfiguration config = null; 
		
		for (DeviceConfiguration configuration : configurations) {
			if (configuration.getType() != type) {
				continue;
			}
			if (configuration.getDeviceId() == deviceId ||
				name.contains(configuration.getDeviceName())) {
				config = configuration;
				break;
			}
		}
		
		return config;
	}
	
    protected DeviceConfiguration getDefaultConfiguration(Device.TYPE type, long deviceId, String name) {
        DeviceConfiguration config = null; 
        
        for (DeviceConfiguration configuration : defaultConfigurations) {
            if (configuration.getType() != type && !name.equals("ANY")) {
                continue;
            }
            if (configuration.getDeviceId() == deviceId ||
                name.contains(configuration.getDeviceName()) || configuration.getDeviceName().equals("ANY")) {
                config = configuration;
                break;
            }
        }
        
        return config;
    }
	
	@Override
	public void configure(OpenCLDevice device) {
		//This ensures that Aparapi will always have their devices configured, even if devices are discovered 
		//independently by Aparapi without passing through ViPIVIST-NG. 
		String name = ComputationDevice.buildDeviceName(device).toUpperCase();
		DeviceConfiguration configuration = getConfiguration(device.getType(), device.getDeviceId(), name);
		if (configuration == null) {
			logger.warn("Cannot find specific configuration for Aparapi device - (Id: " + device.getDeviceId() + ", Name: " + name + ")" + " - Applying default configuration");
			return;
		}
		
		logger.info("Configuring Aparapi device - (Id: " + device.getDeviceId() + ", Name: " + name + ")");
		device.setSharedMemory(configuration.isSharedMemory());
		if (configuration.getAdditionalProperties() != null) {
			configuration.getAdditionalProperties().apply(device);
		}
	}

	public void configure(ComputationDevice computeDevice) {
		Device.TYPE type = computeDevice.getAparapiDevice().getType();
		
		if (type != Device.TYPE.GPU ) {
			logger.warn("All non-GPU OpenCL devices are currently unsupported, ignoring configurarion for: " + computeDevice.getDeviceName());
			return;
		}
		
		DeviceConfiguration configuration = getConfiguration(type, computeDevice.getDeviceId(), computeDevice.getDeviceName().toUpperCase()); 
        boolean usingDefaultConfiguration = false;
        if (configuration == null) {
            logger.warn("Cannot find specific configuration for Aparapi device - (Id: " + computeDevice.getDeviceId() + ", Name: " + computeDevice.getDeviceName() + ")" + " - Applying default configuration");
            configuration = getDefaultConfiguration(type, computeDevice.getDeviceId(), computeDevice.getDeviceName().toUpperCase());
            usingDefaultConfiguration = true;
        }
		      
		logger.info("Configuring Aparapi device - (Id: " + computeDevice.getDeviceId() + ", Name: " + computeDevice.getDeviceName() + ")");
		computeDevice.setMinGroupThreads(configuration.getMinimumThreadMultiple());
		computeDevice.setGreatestThreadGroupCommonDivisor(configuration.getMaxIntegerThreadMultiple());
		computeDevice.setThreadsPerComputeUnit(configuration.getThreadsPerComputeUnit());
		computeDevice.setMemoryWidth(configuration.getMemoryWidth());
		computeDevice.setConfiguredFromDefaults(usingDefaultConfiguration);
	}

}
