package pt.quickLabPIV;

import pt.quickLabPIV.device.ComputationDevice;

public class DeviceRuntimeConfiguration {
    private ComputationDevice device;
    private int cpuThreadAssignments[];
    private float score;
    
    public void setDevice(ComputationDevice _device) {
        device = _device;
    }
    
    public ComputationDevice getDevice() {
        return device;
    }
    
    public void setCpuThreadAssignments(int threadsIdxs[]) {
        cpuThreadAssignments = threadsIdxs;
    }
    
    public int getNrOfCpuThreadsForDevice() {
        return cpuThreadAssignments == null ? 0 : cpuThreadAssignments.length;
    }
    
    public void setScore(float _score) {
        score = _score;
    }
    
    public float getScore() {
        return score;
    }

    public int[] getCpuThreadAssignments() {
        return cpuThreadAssignments;
    }
}
