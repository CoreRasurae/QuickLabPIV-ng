// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public enum WarpingModeEnum {
    Invalid("Invalid warping mode"),
    NoWarping("No warping"), 
    BiLinearMicroWarping1stImageWithBiCubicSplineInterpolation("Bi-linear image micro-warping on the first image with BiCubic spline velocity interpolation"),
    BiLinearMicroWarping2ndImageWithBiCubicSplineInterpolation("Bi-linear image micro-warping on the second image with BiCubic spline velocity interpolation"),
    BiLinearMicroWarpingBothImagesWithBiCubicSplineInterpolation("Bi-linear image micro-warping on both images with BiCubic spline velocity interpolation"),
    BiLinearMiniWarping1stImageWithBiCubicSplineInterpolation("Bi-linear image mini-warping on the first image with BiCubic spline velocity interpolation"),
    BiLinearMiniWarping2ndImageWithBiCubicSplineInterpolation("Bi-linear image mini-warping on the second image with BiCubic spline velocity interpolation"),
    BiLinearMiniWarpingBothImagesWithBiCubicSplineInterpolation("Bi-linear image mini-warping on both images with BiCubic spline velocity interpolation"),
    BiLinearWarping1stImageWithBiCubicSplineInterpolation("Bi-linear image warping on the first image with BiCubic spline velocity interpolation"),
    BiLinearWarping2ndImageWithBiCubicSplineInterpolation("Bi-linear image warping on the second image with BiCubic spline velocity interpolation"),
    BiLinearWarpingBothImagesWithBiCubicSplineInterpolation("Bi-linear image warping on both images with BiCubic spline velocity interpolation");    
    
    private String description;
    
    private WarpingModeEnum(String desc) {
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
