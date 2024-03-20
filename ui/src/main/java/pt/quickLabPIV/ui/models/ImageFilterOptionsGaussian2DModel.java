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

@XmlRootElement(name="imageFilter-Gaussian2D")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageFilterOptionsGaussian2DModel extends ImageFilterOptionsModel {
    private float sigma;
    private int widthPx = 3;
    
    
    public ImageFilterOptionsGaussian2DModel() {
        super.setFilterMode(ImageFilteringModeEnum.ApplyImageFilteringGaussian2D);
    }
    
    public float getSigma() {
        return sigma;
    }
    
    public void setSigma(float _sigma) {
        float oldValue = sigma;
        sigma = _sigma;
        pcs.firePropertyChange("sigma", oldValue, sigma);
    }
    
    public int getWidthPx() {
        return widthPx;
    }
    
    public void setWidthPx(int _numberOfPixels) {
        int oldValue = widthPx;
        widthPx = _numberOfPixels;
        pcs.firePropertyChange("widthPx", oldValue, widthPx);
    }

    @Override
    public ImageFilterOptionsModel copy() {
        ImageFilterOptionsGaussian2DModel model = new ImageFilterOptionsGaussian2DModel();
        
        model.sigma = sigma;
        model.widthPx = widthPx;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(ImageFilterOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof ImageFilterOptionsGaussian2DModel)) {
            return true;            
        }
        
        ImageFilterOptionsGaussian2DModel anotherGaussian = (ImageFilterOptionsGaussian2DModel)another;
        
        if (sigma != anotherGaussian.sigma) {
            changed = true;
        }
        
        if (widthPx != anotherGaussian.widthPx) {
            changed = true;
        }
        
        return changed;
    }
}
