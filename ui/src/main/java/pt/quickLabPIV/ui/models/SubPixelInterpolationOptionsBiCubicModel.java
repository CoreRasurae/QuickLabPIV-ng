// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-BiCubic")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsBiCubicModel extends SubPixelInterpolationOptionsModel {
    private int numberOfDecimalPoints;
    private int numberOfPixels;

    public SubPixelInterpolationOptionsBiCubicModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.BiCubic);
    }
    
    public int getNumberOfDecimalPoints() {
        return numberOfDecimalPoints;
    }
    
    public void setNumberOfDecimalPoints(int _numberOfDecimalPoints) {
        int oldValue = numberOfDecimalPoints;
        numberOfDecimalPoints = _numberOfDecimalPoints;
        pcs.firePropertyChange("numberOfDecimalPoints", oldValue, numberOfDecimalPoints);
    }
    
    public int getNumberOfPixels() {
        return numberOfPixels;
    }
    
    public void setNumberOfPixels(int _numberOfPixels) {
        int oldValue = numberOfPixels;
        numberOfPixels = _numberOfPixels;
        pcs.firePropertyChange("numberOfPixels", oldValue, numberOfPixels);
    }

    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsBiCubicModel model = new SubPixelInterpolationOptionsBiCubicModel();
        
        model.numberOfDecimalPoints = numberOfDecimalPoints;
        model.numberOfPixels = numberOfPixels;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsBiCubicModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsBiCubicModel anotherBiCubic = (SubPixelInterpolationOptionsBiCubicModel)another;
        
        if (numberOfDecimalPoints != anotherBiCubic.numberOfDecimalPoints) {
            changed = true;
        }
        
        if (numberOfPixels != anotherBiCubic.numberOfPixels) {
            changed = true;
        }
        
        return changed;
    }
    
}
