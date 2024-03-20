// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
@XmlSeeAlso({SubPixelInterpolationOptionsBiCubicModel.class, 
        SubPixelInterpolationOptionsGaussian1DModel.class,
        SubPixelInterpolationOptionsGaussian2DModel.class})
public abstract class SubPixelInterpolationOptionsModel {
    @XmlTransient
    private PIVConfigurationModel parent;
    
    @XmlTransient
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private SubPixelInterpolationModeEnum interpolationMode = SubPixelInterpolationModeEnum.Disabled;
    
    private SubPixelInterpolationModeEnum subInterpolationModes[] = null;
        
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public PIVConfigurationModel getParent() {
        return parent;
    }

    public void setParent(PIVConfigurationModel model) {
        parent = model;
    }
    
    public SubPixelInterpolationModeEnum getInterpolationMode() {
        return interpolationMode;
    }
    
    protected void setInterpolationMode(SubPixelInterpolationModeEnum mode) {
        interpolationMode = mode;
    }

    public SubPixelInterpolationModeEnum[] getSubInterpolationModes() {
        return subInterpolationModes; 
    }
    
    protected void setSubInterpolationModes(SubPixelInterpolationModeEnum[] subModes) {
        subInterpolationModes = subModes;
    }
    
    public boolean isCombinedModel() {
        return subInterpolationModes != null;
    }
        
    public abstract SubPixelInterpolationOptionsModel copy();

    public abstract void accept(IPIVConfigurationVisitor visitor);

    public abstract boolean isChanged(SubPixelInterpolationOptionsModel another);

    public boolean isDenseExport() {
        return false;
    }
}
