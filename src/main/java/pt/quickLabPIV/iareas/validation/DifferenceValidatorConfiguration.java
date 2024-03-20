// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas.validation;

public class DifferenceValidatorConfiguration {
    public static final String IDENTIFIER = "ValidatorDifference"; 
    private float distanceThreshold;
    
    public DifferenceValidatorConfiguration(float _distanceThreshold) {
        distanceThreshold = _distanceThreshold;
    }
    
    public float getDistanceThreshold() {
        return distanceThreshold;
    }
}
