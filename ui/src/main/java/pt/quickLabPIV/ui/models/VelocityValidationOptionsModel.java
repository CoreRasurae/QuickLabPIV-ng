// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
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
@XmlSeeAlso({VelocityValidationOptionsNormalizedMedianModel.class, 
             VelocityValidationOptionsDifferenceModel.class})
public abstract class VelocityValidationOptionsModel {
    @XmlTransient
    private PIVConfigurationModel parent;
    
    @XmlTransient
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private VelocityValidationModeEnum validationMode = VelocityValidationModeEnum.Disabled;
    
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
    
    public VelocityValidationModeEnum getValidationMode() {
        return validationMode;
    }
    
    protected void setValidationMode(VelocityValidationModeEnum mode) {
        validationMode = mode;
    }

    public abstract VelocityValidationOptionsModel copy();

    public abstract void accept(IPIVConfigurationVisitor visitor);

    public abstract boolean isChanged(VelocityValidationOptionsModel another);
}
