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

@XmlRootElement(name="interpolation-Gaussian1DPolynomial")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsGaussian1DPolynomialModel extends SubPixelInterpolationOptionsModel {    
    public SubPixelInterpolationOptionsGaussian1DPolynomialModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Gaussian1DPolynomial);
    }
    
    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsGaussian1DPolynomialModel model = new SubPixelInterpolationOptionsGaussian1DPolynomialModel();
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
       visitor.visit(this);        
    }
    
    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsGaussian1DPolynomialModel)) {
            return true;            
        }
        
        //SubPixelInterpolationOptionsGaussian1DPolynomialModel anotherGaussian1D = (SubPixelInterpolationOptionsGaussian1DPolynomialModel)another;               
        
        return changed;
    }
}
