// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public enum InheritanceModeEnum {
        Invalid("Invalid inheritance mode"),
        Distance("Distance inheritance mode"),
        Area("Area inheritance mode"),
        BiCubicSpline("Bi-cubic spline inheritance mode");
    
    
    private String description;
    
    private InheritanceModeEnum(String desc) {
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
