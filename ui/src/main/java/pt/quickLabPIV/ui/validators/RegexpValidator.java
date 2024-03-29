// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jdesktop.beansbinding.Validator;

public class RegexpValidator extends Validator<String> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Validator<String>.Result validate(String regex) {
        Validator<String>.Result result = null;
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException ex) {
            result = new Validator.Result(null, "Regular expression is not valid");
        }
        
        return result;
    }

}
