// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

public enum VelocityValidationModeEnum {
    Disabled("Velocity validation mode disabled"),
    DifferenceOnly("Difference validation only mode, no vector replacement"),
    Difference("Difference validation mode with Bilinear replacement"),    
    NormalizedMedianOnly("Normalized median only validation, no vector replacement"),
    NormalizedMedian("Normalized median validation mode with Bilinear replacement"),   
    MultiPeakNormalizedMedian("Normalized median validation mode with Multi-peak replacement");
    
    private String description;
    
    private VelocityValidationModeEnum(String desc) {
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
