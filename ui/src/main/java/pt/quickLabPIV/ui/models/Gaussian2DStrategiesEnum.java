// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public enum Gaussian2DStrategiesEnum {
    Invalid("Invalid method"),
    Symmetric("Symmetric 2D-Gaussian method"),
    Assymetric("Assymetric 2D-Gaussian method"),
    AssymetricWithRotation("Assymetric with rotation 2D-Gaussian method");
    
    private String description;
    
    private Gaussian2DStrategiesEnum(String _description) {
        description = _description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }

}
