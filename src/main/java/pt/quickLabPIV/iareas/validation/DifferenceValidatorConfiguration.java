// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
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
