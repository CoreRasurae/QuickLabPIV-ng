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

@XmlRootElement(name="interpolation-Gaussian1D")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsGaussian1DModel extends SubPixelInterpolationOptionsModel {
    private int numberOfPixels;
    
    public SubPixelInterpolationOptionsGaussian1DModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Gaussian1D);
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
        SubPixelInterpolationOptionsGaussian1DModel model = new SubPixelInterpolationOptionsGaussian1DModel();
        
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
        
        if (!(another instanceof SubPixelInterpolationOptionsGaussian1DModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsGaussian1DModel anotherGaussian1D = (SubPixelInterpolationOptionsGaussian1DModel)another;
                
        if (numberOfPixels != anotherGaussian1D.numberOfPixels) {
            changed = true;
        }
        
        return changed;
    }
}
