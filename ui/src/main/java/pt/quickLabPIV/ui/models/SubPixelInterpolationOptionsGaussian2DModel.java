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

@XmlRootElement(name="interpolation-Gaussian2D")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsGaussian2DModel extends SubPixelInterpolationOptionsModel {
    private Gaussian2DStrategiesEnum algorithm = Gaussian2DStrategiesEnum.Invalid;
    private int numberOfPixelsInX;
    private int numberOfPixelsInY;
    
    public SubPixelInterpolationOptionsGaussian2DModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Gaussian2D);
    }
    
    public Gaussian2DStrategiesEnum getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(Gaussian2DStrategiesEnum _algorithm) {
        Gaussian2DStrategiesEnum oldValue = algorithm;
        algorithm = _algorithm;
        pcs.firePropertyChange("algorithm", oldValue, algorithm);
    }
    
    public int getNumberOfPixelsInX() {
        return numberOfPixelsInX;
    }
    
    public void setNumberOfPixelsInX(int _numberOfPixels) {
        int oldValue = numberOfPixelsInX;
        numberOfPixelsInX = _numberOfPixels;
        pcs.firePropertyChange("numberOfPixelsInX", oldValue, numberOfPixelsInX);
    }

    public int getNumberOfPixelsInY() {
        return numberOfPixelsInY;
    }
    
    public void setNumberOfPixelsInY(int _numberOfPixels) {
        int oldValue = numberOfPixelsInY;
        numberOfPixelsInY = _numberOfPixels;
        pcs.firePropertyChange("numberOfPixelsInY", oldValue, numberOfPixelsInY);
    }

    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsGaussian2DModel model = new SubPixelInterpolationOptionsGaussian2DModel();
        
        model.algorithm = algorithm;
        model.numberOfPixelsInX = numberOfPixelsInX;
        model.numberOfPixelsInY = numberOfPixelsInY;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsGaussian2DModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsGaussian2DModel anotherGaussian2D = (SubPixelInterpolationOptionsGaussian2DModel)another;
        
        if (algorithm != anotherGaussian2D.algorithm) {
            changed = true;
        }
        
        if (numberOfPixelsInX != anotherGaussian2D.numberOfPixelsInX) {
            changed = true;
        }

        if (numberOfPixelsInY != anotherGaussian2D.numberOfPixelsInY) {
            changed = true;
        }

        return changed;
    }
}
