// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-Gaussian2DPolynomial")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsGaussian2DPolynomialModel extends SubPixelInterpolationOptionsModel {
    private int numberOfPixels;
    
    public SubPixelInterpolationOptionsGaussian2DPolynomialModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Gaussian2DPolynomial);
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
        SubPixelInterpolationOptionsGaussian2DPolynomialModel model = new SubPixelInterpolationOptionsGaussian2DPolynomialModel();
        
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
        
        if (!(another instanceof SubPixelInterpolationOptionsGaussian2DPolynomialModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsGaussian2DPolynomialModel anotherGaussian1D = (SubPixelInterpolationOptionsGaussian2DPolynomialModel)another;
                
        if (numberOfPixels != anotherGaussian1D.numberOfPixels) {
            changed = true;
        }
        
        return changed;
    }
}
