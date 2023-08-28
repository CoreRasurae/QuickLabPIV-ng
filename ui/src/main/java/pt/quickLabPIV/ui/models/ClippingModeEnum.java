// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

public enum ClippingModeEnum {
    AllowedOutOfBoundClipping("Out of bound clipping is allowed"),
    LoggedOutOfBoundClipping("Out of bound clipping is logged"),
    NoOutOfBoundClipping("Out of bound clipping is not allowed");
 
    private String description;
    
    private ClippingModeEnum(String _description) {
        description = _description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return description;
    }
}
