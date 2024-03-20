// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.exceptions.UIException;

public class FloatRangeValidator extends Validator<Float> implements PropertyChangeListener {
    private float min = 0;
    private float max = Integer.MAX_VALUE;
    
    public void setMinAndMax(float newMin, float newMax) {
        if (newMin > newMax) {
            throw new UIException("Range validation error", "Minimum value must be less or equal to maximum value.");
        }
        
        min = newMin;
        max = newMax;
    }
        
    @Override
    public Validator<Float>.Result validate(Float value) {
        Validator<Float>.Result result = null;
        if (value < min || value > max) {
            result = new FloatRangeValidator.Result(null, "Value must be between " + min + " and " + max + " including range limit values.");
        }
                
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //FIXME LM how to properly detect value changes and validate along typing...
        /*if (evt.getPropertyName() == "distanceThresholdPixels") {
            distanceThresholdPixels = (Float)evt.getNewValue();
        }*/
    }

}
