// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas.validation;

public class NormalizedMedianValidatorConfiguration {

    public static final String IDENTIFIER = "ValidatorNormMedian";
    
    private float distanceThreshold;
    private float epsilon0;
    
    public NormalizedMedianValidatorConfiguration(float _distanceThreshold, float _epsilon0) {
        distanceThreshold = _distanceThreshold;
        epsilon0 = _epsilon0;
    }
    
    public float getDistanceThreshold() {
        return distanceThreshold;
    }
    
    public float getEpsilon0() {
        return epsilon0;
    }
 
}
