// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

public class PercentageOverlapValidator extends Validator<Float> {

    @Override
    public Validator<Float>.Result validate(Float value) {
        Validator<Float>.Result result = null;
        
        if (value < 25.00f || value > 90.00f) {
            result = new PercentageOverlapValidator.Result(null, "Valid overlap percentage values are between 25.00% and 90.00%, including the limit values");
        }
        
        return result;
    }

}
