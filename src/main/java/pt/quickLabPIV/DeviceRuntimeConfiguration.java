// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
