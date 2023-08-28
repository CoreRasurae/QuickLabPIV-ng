// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

public enum VelocityValidationReplacementModeEnum {
    Invalid("Invalid vector replacement mode"),
    Bilinear("Bi-Linear vector replacement mode");
    
    private String description;
    
    private VelocityValidationReplacementModeEnum(String desc) {
        description = desc;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
