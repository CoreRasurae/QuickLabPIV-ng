package pt.quickLabPIV;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.iareas.IAdaptiveInterVelocityInheritanceLogger;
import pt.quickLabPIV.jobs.Job;

public class PIVRunParameters {
    private final ExecutionStatus executionStatus = new ExecutionStatus();
    private Map<Long, DeviceRuntimeConfiguration> deviceConfigurations = new HashMap<Long, DeviceRuntimeConfiguration>();
	private IAdaptiveInterVelocityInheritanceLogger velocityLogger = null;
	private AtomicBoolean  cancelRequested = new AtomicBoolean(false);
	private Job<?,?> executionJob;
	private StructMultiFrameFloatVelocityExporter exporter;
	private boolean useOpenCL = false;
	private int totalCpuThreads;
	private ConcurrentHashMap<Long, Integer> threadIdToIndexMap = new ConcurrentHashMap<>();
	
	public void setVelocityInheritanceLogger(IAdaptiveInterVelocityInheritanceLogger velocityLogger) {
		this.velocityLogger = velocityLogger;
	}
	
	public IAdaptiveInterVelocityInheritanceLogger getVelocityInheritanceLogger() {
		return velocityLogger;
	}

	public ExecutionStatus getExecutionStatus() {
	    return executionStatus;
	}

    public void setCancelRequested(boolean state) {
        cancelRequested.set(state);
    }
    
    public boolean isCancelRequested() {
        return cancelRequested.get();
    }
    
    public void setExecutionJob(Job<?,?> job) {
        executionJob = job;
    }
    
    public Job<?,?> getExecutionJob() {
        return executionJob;
    }
    
    public void cancelExecutionJob() {
        if (executionJob != null) {
            executionJob.cancel();
        }
    }
    
    public void setUseOpenCL(boolean _useOpenCL) {
        useOpenCL = _useOpenCL;
    }
    
    public boolean isUseOpenCL() {
        return useOpenCL;
    }

    public StructMultiFrameFloatVelocityExporter getExporter() {        
        return exporter;
    }
    
    public void setExporter(StructMultiFrameFloatVelocityExporter _exporter) {
        exporter = _exporter;
    }
    
    public void putDeviceConfiguration(DeviceRuntimeConfiguration config) {
        deviceConfigurations.put(config.getDevice().getDeviceId(), config);
    }
    
    public DeviceRuntimeConfiguration getDeviceConfiguration(long id) {
        return deviceConfigurations.get(id);
    }
    
    public Collection<DeviceRuntimeConfiguration> getDeviceConfigurations() {
        return Collections.unmodifiableCollection(deviceConfigurations.values());
    }

    public void setTotalNumberOfThreads(int _totalCpuThreads) {
        totalCpuThreads = _totalCpuThreads;
    }
    
    public int getTotalNumberOfThreads() {
        return totalCpuThreads;
    }

    public void setDeviceRuntimeConfigurationMap(Map<Long, DeviceRuntimeConfiguration> map) {
        deviceConfigurations = map;
    }
    
    public void clearThreadMappings() {
    	threadIdToIndexMap.clear();
    }
    
    public void mapThreadToThreadIndex(int idx) {
    	threadIdToIndexMap.putIfAbsent(Thread.currentThread().getId(), idx);
    }
    
    public int getThreadIndex() {
    	return threadIdToIndexMap.get(Thread.currentThread().getId());
    }
    
    public ComputationDevice getComputationDeviceForThread() {
    	ComputationDevice result = null;
    	
    	int threadIdx = getThreadIndex();
    	for (DeviceRuntimeConfiguration config : deviceConfigurations.values()) {
    		int threadsIdxs[] = config.getCpuThreadAssignments();
    		if (Arrays.stream(threadsIdxs).anyMatch(id -> id == threadIdx)) {
    			result = config.getDevice();
    			break;
    		}
    	}
    	
    	return result;
    }    
}
 