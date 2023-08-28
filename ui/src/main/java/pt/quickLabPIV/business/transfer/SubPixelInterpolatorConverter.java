// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.transfer;

import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.OpticalFlowAfterPIVInterpolatorFactoryEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;

public class SubPixelInterpolatorConverter {
    public static CrossCorrelationInterpolatorFactoryEnum convertToBusinessLogic(SubPixelInterpolationModeEnum subPixelMode) {
        CrossCorrelationInterpolatorFactoryEnum targetMode = null;
        
        switch (subPixelMode) {
        case BiCubic:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.BiCubic;
            break;
        case Centroid2D:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Centroid2D;
            break;
        case Disabled:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.None;
            break;
        case Gaussian1D:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian1D;
            break;
        case Gaussian1DHongweiGuo:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian1DHongweiGuo;
            break;
        case CombinedBaseAndFinalInterpolator:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.CompositeLastLevelInterpolator;
            break;
        case Gaussian1DPolynomial:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian1DPolynomial;
            break;
        case Gaussian2D:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian2D;
            break;
        case Gaussian2DLinearRegression:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian2DLinearRegression;
            break;
        case Gaussian2DPolynomial:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.Gaussian1DPolynomial;
            break;
        case LucasKanade:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.LucasKanade;
            break;
        case LucasKanadeOpenCL:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.LucasKanadeAparapi;
            break;
        case LiuShenWithLucasKanade:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.LiuShenWithLucasKanade;
            break;
        case LiuShenWithLucasKanadeOpenCL:
            targetMode = CrossCorrelationInterpolatorFactoryEnum.LiuShenWithLucasKanadeAparapi;
            break;
        default:
            break;
        
        }
        
        return targetMode;
    }

    public static OpticalFlowAfterPIVInterpolatorFactoryEnum convertToBusinessLogicOF(
            SubPixelInterpolationModeEnum subPixelInterpolationModeEnum) {
        OpticalFlowAfterPIVInterpolatorFactoryEnum targetMode = null;
        
        switch (subPixelInterpolationModeEnum) {
        case LiuShenWithLucasKanade:
            targetMode = OpticalFlowAfterPIVInterpolatorFactoryEnum.LiuShen;
            break;
        case LiuShenWithLucasKanadeOpenCL:
            targetMode = OpticalFlowAfterPIVInterpolatorFactoryEnum.LiuShenAparapi;
            break;
        case LucasKanade:
            targetMode = OpticalFlowAfterPIVInterpolatorFactoryEnum.LucasKanade;
            break;
        case LucasKanadeOpenCL:
            targetMode = OpticalFlowAfterPIVInterpolatorFactoryEnum.LucasKanadeAparapi;
            break;
        default:
            targetMode = OpticalFlowAfterPIVInterpolatorFactoryEnum.None;
        }
        
        return targetMode;
    }

}
