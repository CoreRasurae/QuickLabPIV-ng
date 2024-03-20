// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum InterrogationAreaResolutionEnum {
    IA0(0, 0, "Invalid"),
    IA8(8, 8, "8x8"),
    IA16(16, 16, "16x16"),
    IA32(32, 32, "32x32"),
    IA64(64, 64, "64x64"),
    IA128(128, 128, "128x128"),
    IA256(256, 256, "256x256");
    
    private int sizeW;
    private int sizeH;
    private String description;
    
    private InterrogationAreaResolutionEnum(int _sizeW, int _sizeH, String _description) {
        sizeW = _sizeW;
        sizeH = _sizeH;
        description = _description;
    }
    
    public int getSizeW() {
        return sizeW;
    }
    
    public int getSizeH() {
        return sizeH;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    public static InterrogationAreaResolutionEnum[] getResolutionsAboveOrEqual(InterrogationAreaResolutionEnum endResolution) {
        InterrogationAreaResolutionEnum[] validValues = Arrays.copyOfRange(values(), 1, values().length);
        if (endResolution == null) {
            return validValues;
        }
        
        List<InterrogationAreaResolutionEnum> aboveOrEquals = new ArrayList<>(validValues.length);
        for (InterrogationAreaResolutionEnum iaRes : validValues) {
            if (iaRes.compareTo(endResolution) >= 0) {
                aboveOrEquals.add(iaRes);
            }
        }
        return aboveOrEquals.toArray(new InterrogationAreaResolutionEnum[aboveOrEquals.size()]);
    }
    
    public static InterrogationAreaResolutionEnum[] getResolutionsBetweenOrEqual(InterrogationAreaResolutionEnum endResolution,
                                                                                InterrogationAreaResolutionEnum initialResolution) {
        if (endResolution == IA0 || initialResolution == IA0 || initialResolution == null || endResolution == null) {
            InterrogationAreaResolutionEnum[] result = new InterrogationAreaResolutionEnum[] { IA0 }; 
            return result;
        }
        
        System.out.println("Called with end Res: " + endResolution.getDescription() + ", initial Res: " + initialResolution.getDescription());
        InterrogationAreaResolutionEnum[] validValues = Arrays.copyOfRange(values(), endResolution.ordinal(), values().length);
        List<InterrogationAreaResolutionEnum> result = new ArrayList<>(validValues.length);
        
        for (InterrogationAreaResolutionEnum value : validValues) {
            if (value.compareTo(initialResolution) <= 0) {
                System.out.println("Adding resolution: " + value.getDescription());
                result.add(value);
            } else {
                break;
            }
        }
        
        System.out.println("Returning " + result.size() + " resolutions");
        
        return result.toArray(new InterrogationAreaResolutionEnum[result.size()]);
    }
}
