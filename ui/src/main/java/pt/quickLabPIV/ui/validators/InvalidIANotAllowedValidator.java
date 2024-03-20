// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;

public class InvalidIANotAllowedValidator extends Validator<Object> {

    @Override
    public Validator<Object>.Result validate(Object valueObj) {
        Validator<Object>.Result result = null;
        
        InterrogationAreaResolutionEnum value = (InterrogationAreaResolutionEnum)valueObj;
        if (value == InterrogationAreaResolutionEnum.IA0) {
            result = new InvalidIANotAllowedValidator.Result(null, "Please select a valid Interrogation Area size");
        }
        
        return result;
    }

}
