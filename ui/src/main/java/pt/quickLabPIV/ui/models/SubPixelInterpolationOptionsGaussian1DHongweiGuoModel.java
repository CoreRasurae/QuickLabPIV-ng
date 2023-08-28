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
public class SubPixelInterpolationOptionsGaussian1DHongweiGuoModel extends SubPixelInterpolationOptionsModel {
    private int numberOfPixels = 3;
    private int numberOfIterations = 20;
    
    public SubPixelInterpolationOptionsGaussian1DHongweiGuoModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Gaussian1DHongweiGuo);
    }
    
    public int getNumberOfPixels() {
        return numberOfPixels;
    }
    
    public void setNumberOfPixels(int _numberOfPixels) {
        int oldValue = numberOfPixels;
        numberOfPixels = _numberOfPixels;
        pcs.firePropertyChange("numberOfPixels", oldValue, numberOfPixels);
    }
    
    public int getNumberOfIterations() {
        return numberOfIterations;
    }
    
    public void setNumberOfIterations(int _iterations) {
        int oldValue = numberOfIterations;
        numberOfIterations = _iterations;
        pcs.firePropertyChange("numberOfIterations", oldValue, numberOfIterations);
    }

    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsGaussian1DHongweiGuoModel model = new SubPixelInterpolationOptionsGaussian1DHongweiGuoModel();
        
        model.numberOfPixels = numberOfPixels;
        model.numberOfIterations = numberOfIterations;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
       visitor.visit(this);        
    }
    
    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsGaussian1DHongweiGuoModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsGaussian1DHongweiGuoModel anotherGaussian1D = (SubPixelInterpolationOptionsGaussian1DHongweiGuoModel)another;
                
        if (numberOfPixels != anotherGaussian1D.numberOfPixels) {
            changed = true;
        }
        
        if (numberOfIterations != anotherGaussian1D.numberOfIterations) {
            changed = true;
        }
        
        return changed;
    }
}
