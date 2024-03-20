// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import java.util.LinkedList;

import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.Validator;
import org.jdesktop.beansbinding.Validator.Result;

import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public abstract class ConverterWithForwardValidator<S, T> extends Converter<S, T> {
    private Validator<? super S> validatorOnForward;
    private LinkedList<ErrorBorderForComponent> listeners = new LinkedList<>();
    
    public void setValidatorOnConvertForward(Validator<? super S> validator) {
        validatorOnForward = validator;
    }
    
    public void addStatusListener(ErrorBorderForComponent listener) {
        listeners.add(listener);
    }
    
    public abstract T convertForwardAfterValidation(S value);
    
    @Override
    public T convertForward(S value) {
        //Force validation on set too
        if (validatorOnForward != null) {
            @SuppressWarnings("rawtypes")
            Result r = validatorOnForward.validate(value);
            for (ErrorBorderForComponent listener : listeners) {
                listener.updateStatus(r);
            }
        }

        return convertForwardAfterValidation(value);
    }

}
