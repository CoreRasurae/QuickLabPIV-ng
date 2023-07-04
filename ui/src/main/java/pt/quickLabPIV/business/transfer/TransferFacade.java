package pt.quickLabPIV.business.transfer;

import java.util.List;

import com.aparapi.device.Device;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.ui.models.OpenCLDeviceModel;
import pt.quickLabPIV.ui.models.OpenCLDeviceTypeEnum;

public class TransferFacade {
    public static List<ComputationDevice> getAvailableGpuDevices() {
        DeviceManager manager = DeviceManager.getSingleton();
        manager.detectDevices();
        return manager.getGPUs();
    }
    
    public static OpenCLDeviceModel convertToModel(ComputationDevice device) {
        OpenCLDeviceModel model = new OpenCLDeviceModel();
        
        Device.TYPE businessType = device.getAparapiDevice().getType();
        OpenCLDeviceTypeEnum type;
        switch (businessType) {
        case GPU:
            type = OpenCLDeviceTypeEnum.GPU;
            break;
        case CPU:
            type = OpenCLDeviceTypeEnum.CPU;
            break;
        default:
            type = OpenCLDeviceTypeEnum.Unknown;
        }
        
        model.setDeviceType(type);
        model.setId(device.getDeviceId());
        model.setName(device.getDeviceName());
        model.setDefaultConfig(device.isConfiguredFromDefaults());
        model.setNumberOfComputeUnits(device.getMaxComputeUnits());
        model.setThreadsPerComputeUnit(device.getThreadsPerComputeUnit());
        model.setMinimumRecommendedThreadMultiple(device.getMinGroupThreads());
        model.setGreatestThreadCommonDivisor(device.getGreatestThreadGroupCommonDivisor());        
        
        return model;
    }

    public static int getNumberOfRealCores() {
        return DeviceManager.getSingleton().getNumberOfRealCores();
    }

    public static ComputationDevice convertToComputeDevice(OpenCLDeviceModel model) {
        DeviceManager manager = DeviceManager.getSingleton();
        List<ComputationDevice> devices = manager.getGPUs();
        for (ComputationDevice device : devices) {
            if (device.getAparapiDevice().getDeviceId() == model.getId()) {
                device.setMaxComputeUnits(model.getNumberOfComputeUnits());
                device.setThreadsPerComputeUnit(model.getThreadsPerComputeUnit());
                device.setMinGroupThreads(model.getMinimumRecommendedThreadMultiple());
                device.setGreatestThreadGroupCommonDivisor(model.getGreatestThreadCommonDivisor());
                return device;
            }
        }
        return null;
    }
}
