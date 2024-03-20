// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

import pt.quickLabPIV.PIVInputParameters;

public enum ImageFilterFactoryEnum {
    NoFiltering,
    GaussianFiltering;
    
    public static IFilter create(PIVInputParameters parameters) {
        ImageFilterFactoryEnum mode = parameters.getImageFilterMode();
        
        switch (mode) {
        case NoFiltering:
            return new NullImageFilter();
        case GaussianFiltering:
            return new GaussianFilter2D();
        default:
            throw new ImageFilterException("Unknown image filter: " + mode.toString());
        }
    }

}
