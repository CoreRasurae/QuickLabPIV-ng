// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel;

public class InvalidNotAllowedSubPixelInterpolationValidator extends Validator<SubPixelInterpolationModeEnum> {
    private AppContextModel appContext;
    
    public void setAppContext(AppContextModel _appContext) {
        appContext = _appContext;
    }
    
    @Override
    public Validator<SubPixelInterpolationModeEnum>.Result validate(SubPixelInterpolationModeEnum value) {
        Validator<SubPixelInterpolationModeEnum>.Result result = null;
        
        if (value == SubPixelInterpolationModeEnum.Disabled) {
            result = new InvalidNotAllowedSubPixelInterpolationValidator.Result(null, "Selected interpolation strategy is invalid.\\nConsider disabling validation.");
        } else if (value == SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL ||
                   value == SubPixelInterpolationModeEnum.LucasKanadeOpenCL) {
            if (appContext.getExecutionEnvironment() != null && !appContext.getExecutionEnvironment().isEnableOpenCL()) {
                result = new InvalidNotAllowedSubPixelInterpolationValidator.Result(null, "Selected interpolation strategy cannot be employed with OpenCL disabled.");
            }
        }
        
        return result;
    }

}
