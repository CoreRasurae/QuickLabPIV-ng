// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public enum OpenCLDeviceTypeEnum {
    GPU("GPU"),
    CPU("CPU"), 
    Unknown("Unkown");
    
    private String description;
    
    private OpenCLDeviceTypeEnum(String _description) {
        description = _description;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    public String getDescription() {
        return description;
    }
}
