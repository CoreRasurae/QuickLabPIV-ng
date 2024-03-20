// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public interface IPIVConfigurationVisitor {

    void visit(PIVConfigurationModel pivConfigurationModel);

    //Image filter options
    void visit(ImageFilterOptionsGaussian2DModel imageFilterOptionsGaussian2DModel);

    //Sub-pixel options
    void visit(SubPixelInterpolationOptionsBiCubicModel subPixelInterpolationOptionsBiCubicModel);

    void visit(SubPixelInterpolationOptionsGaussian1DModel subPixelInterpolationOptionsGaussian1DModel);
    
    void visit(SubPixelInterpolationOptionsGaussian1DHongweiGuoModel subPixelInterpolationOptionsGaussian1DHongweiGuoModel);

    void visit(SubPixelInterpolationOptionsGaussian1DPolynomialModel subPixelInterpolationOptionsGaussian1DPolynomialModel);
    
    void visit(SubPixelInterpolationOptionsCentroid2DModel subPixelInterpolationOptionsCentroid2DModel);
    
    void visit(SubPixelInterpolationOptionsGaussian2DModel subPixelInterpolationOptionsGaussian2DModel);
    
    void visit(SubPixelInterpolationOptionsGaussian2DPolynomialModel subPixelInterpolationOptionsGaussian2DPolynomialModel);
    
    void visit(SubPixelInterpolationOptionsGaussian2DLinearRegressionModel subPixelInterpolationOptionsGaussian2DLinearRegressionModel);
    
    void visit(SubPixelInterpolationOptionsLucasKanadeModel subPixelInterpolationOptionsLucasKanadeModel);

    void visit(
            SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel subPixelInterpolationOptionsGaussian1DHongweiGuoWithLucasKanadeFinalModel);

    void visit(
            SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel subPixelInterpolationOptionsLiuShenWithLucasKanadeModel);

    //Stabilization options
    void visit(VelocityStabilizationOptionsMaxDisplacementModel velocityStabilizationOptionsMaxDisplacementModel);
    
    //Validation options
    void visit(VelocityValidationOptionsDifferenceOnlyModel model);
    
    void visit(VelocityValidationOptionsDifferenceModel velocityValidationOptionsDifferenceModel);

    void visit(VelocityValidationOptionsNormalizedMedianOnlyModel model);
    
    void visit(VelocityValidationOptionsNormalizedMedianModel velocityValidationOptionsNormalizedMedianModel);

    void visit(VelocityValidationOptionsMultiPeakNormalizedMedianModel velocityValidationOptionsMultiPeakNormalizedMedianModel);

    void visit(
            SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel);

    void visit(SubPixelInterpolationOptionsLucasKanadeOpenCLModel subPixelInterpolationOptionsLucasKanadeOpenCLModel);

}
