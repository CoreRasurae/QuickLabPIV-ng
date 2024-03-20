// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

public class NotNullValidator extends Validator<Object> {
    private String message = "Value cannot be null";
    
    
    @Override
    public Validator<Object>.Result validate(Object value) {
        Validator<Object>.Result result = null;
        if (value == null) {
            result = new NotNullValidator.Result(null, message);
        }
        
        return result;
    }

}
