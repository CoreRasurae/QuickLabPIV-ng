package pt.quickLabPIV.business.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;
import pt.quickLabPIV.ui.models.IExecutionEnvVisitor;
import pt.quickLabPIV.ui.models.OpenCLDeviceModel;

public class ExecutionEnvConverterVisitor implements IExecutionEnvVisitor {
    private PIVRunParameters runtimeConfig;
    
    public ExecutionEnvConverterVisitor(PIVRunParameters _runtimeConfig) {
        runtimeConfig = _runtimeConfig;
    }
    
    public void visit(ExecutionEnvModel execEnv) {
        Map<Long, DeviceRuntimeConfiguration> map = new HashMap<>(execEnv.getOpenClDevices().size());
        Map<Long, List<Integer>> mapThreadIdxs = new HashMap<>();
        for (OpenCLDeviceModel model : execEnv.getOpenClDevices()) {
            if (model.isSelected()) {
                DeviceRuntimeConfiguration devConfig = new DeviceRuntimeConfiguration();
                devConfig.setDevice(TransferFacade.convertToComputeDevice(model));
                devConfig.setScore(model.getPerformanceScore());
                map.put(model.getId(), devConfig);
                List<Integer> list = new ArrayList<>();
                mapThreadIdxs.put(model.getId(), list);
            }
        }
        for (int i = 0; i < execEnv.getValidAssignments().size(); i++) {
            boolean isValid = execEnv.getValidAssignments().get(i);
            long deviceId = execEnv.getOpenClAssignments().get(i);
            if (isValid) {
                List<Integer> list = mapThreadIdxs.get(deviceId);
                list.add(i);
            }
        }
        
        for (Entry<Long, List<Integer>> entry : mapThreadIdxs.entrySet()) {
            DeviceRuntimeConfiguration config = map.get(entry.getKey());
            int arr[] = new int[entry.getValue().size()];
            for (int i = 0; i < entry.getValue().size(); i++) {
                arr[i] = entry.getValue().get(i);
            }
            config.setCpuThreadAssignments(arr);
        }
        
        runtimeConfig.setUseOpenCL(execEnv.isEnableOpenCL());;
        runtimeConfig.setTotalNumberOfThreads(execEnv.getCpuThreads());
        runtimeConfig.setDeviceRuntimeConfigurationMap(map);
    }

}
