// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.beansbinding.Converter;

import pt.quickLabPIV.exceptions.UIException;

public class TotalImagesConverter extends Converter<Integer, String> implements PropertyChangeListener {

    private int availableImages;
    
    public void setAvailableImages(int _availableImages) {
        availableImages = _availableImages;
    }
    
    @Override
    public String convertForward(Integer totalImages) {
        StringBuilder sb = new StringBuilder(50);
        sb.append("from ");
        sb.append(availableImages);
        sb.append(" available and a total of ");
        sb.append(totalImages);
        sb.append(" that match the image pattern");
        
        return sb.toString();
    }

    @Override
    public Integer convertReverse(String value) {
        throw new UIException("PIV Image selection", "Unsupported conversion");
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == "availableImageFiles") {
            availableImages = (Integer)evt.getNewValue();
        }
    }

}
