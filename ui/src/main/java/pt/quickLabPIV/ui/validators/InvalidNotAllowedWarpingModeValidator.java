// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.WarpingModeEnum;

public class InvalidNotAllowedWarpingModeValidator extends Validator<WarpingModeEnum> {

    @Override
    public Validator<WarpingModeEnum>.Result validate(WarpingModeEnum value) {
        Validator<WarpingModeEnum>.Result result = null;
        
        if (value == WarpingModeEnum.Invalid) {
            result = new InvalidNotAllowedWarpingModeValidator.Result(null, "Selected warping strategy is invalid.");
        }
        
        return result;
    }

}
