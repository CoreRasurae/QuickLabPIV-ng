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

import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class MarginsValidator extends Validator<Integer> implements PropertyChangeListener {
    private String errorMessage = "Invalid margins size. Please correct the margins to allow for at least a full interrogarion area.";
    private int lastValidatedValue = 0;
    private int imagePixels;
    private int otherMargin = 0;
    private int initialResolution = 0;
    private String propertyName;
    private ErrorBorderForComponent errorBorder;
    
    public MarginsValidator(String _propertyName) {
        propertyName = _propertyName;
    }

    /**
     * Provides a way to update the error state of the validated component, based on the last validated value and
     * new values updates. Since updates can be external to the property under evaluation, it is possible that either
     * the error condition is not signaled immediately, but also the error condition could not be cleared immediately either.
     * 
     * @param _errorBorder the error border associated with the validated component
     */
    public void setAssociatedErrorBorder(ErrorBorderForComponent _errorBorder) {
        errorBorder = _errorBorder;
    }
    
    public void setInitialValues(int _imagePixels, int _otherMargin, InterrogationAreaResolutionEnum _initialResolution) {
        imagePixels = _imagePixels;
        otherMargin = _otherMargin;
        if (_initialResolution != null) {
            initialResolution = _initialResolution.getSizeH();            
        }
    }
    
    public void setImagePixels(int _imagePixels) {
        imagePixels = _imagePixels;
    }
    
    @Override
    public Validator<Integer>.Result validate(Integer value) {
        Validator<Integer>.Result result = null;
        
        lastValidatedValue = value;
        
        if (imagePixels == 0 || initialResolution == 0) {
            result = new MarginsValidator.Result(null, "Please fill in previous tabs, before attempting to setup this one.");
        } else if (imagePixels - value - otherMargin < initialResolution) {
            result = new MarginsValidator.Result(null, errorMessage); 
        }
        
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean updated = false;
        //System.out.println(propertyName + " - Updating for prop: " + evt.getPropertyName());
        if (evt.getPropertyName().equals(propertyName)) {
            otherMargin = (Integer)evt.getNewValue();
            //System.out.println(propertyName + " - Updating initial resolution to: " + otherMargin);
            updated = true;
        } else if (evt.getPropertyName().equals("initialResolution")) {
            //Currently IAs are squares
            if (evt.getNewValue() != null) {
                initialResolution = ((InterrogationAreaResolutionEnum)evt.getNewValue()).getSizeH();
                //System.out.println(propertyName + " - Updating initial resolution to: " + initialResolution);
                updated = true;
            }
        }
        
        if (updated && errorBorder != null) {
            if (imagePixels == 0 || initialResolution == 0) {
                errorBorder.updateStatus(new MarginsValidator.Result(null, "Please fill in previous tabs, before attempting to setup this one."));
            } else if (imagePixels - lastValidatedValue - otherMargin < initialResolution) {
                errorBorder.updateStatus(new MarginsValidator.Result(null, errorMessage)); 
            } else {
                errorBorder.updateStatus(null);
            }
        }
    }
}
