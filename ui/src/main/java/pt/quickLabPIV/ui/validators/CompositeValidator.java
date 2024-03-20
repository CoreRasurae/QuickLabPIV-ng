// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

public class CompositeValidator<T> extends Validator<T> {
    private Validator<T> validatorA;
    private Validator<T> validatorB;
        
    
    public CompositeValidator(Validator<T> _validatorA, Validator<T> _validatorB) {
        validatorA = _validatorA;
        validatorB = _validatorB;
    }
    
    @Override
    public Validator<T>.Result validate(T value) {
        Validator<T>.Result r = validatorA.validate(value);
        if (r == null) {
            r = validatorB.validate(value);
        }
        
        return r;
    }

    public Validator<T> getValidatorA() {
        return validatorA;
    }
    
    public Validator<T> getValidatorB() {
        return validatorB;
    }
}
