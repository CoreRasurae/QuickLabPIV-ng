// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeSupport;

import javax.swing.DefaultComboBoxModel;

public class VelocityStabilizationComboBoxModel extends DefaultComboBoxModel<VelocityStabilizationModeEnum> {


    /**
     * 
     */
    private static final long serialVersionUID = -6960963380902320888L;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    @Override
    public void setSelectedItem(Object object) {
        VelocityStabilizationModeEnum oldValue = getSelectedItem();
        super.setSelectedItem(object);
        pcs.firePropertyChange("selectedItem", oldValue, (VelocityStabilizationModeEnum)object);
    }
    
    public VelocityStabilizationComboBoxModel(VelocityStabilizationModeEnum[] values) {
        super(values);
    }
    
    public VelocityStabilizationComboBoxModel() {
        super(VelocityStabilizationModeEnum.values());
    }

    @Override
    public VelocityStabilizationModeEnum getElementAt(int index) {
        return super.getElementAt(index);
    }
    
    @Override
    public VelocityStabilizationModeEnum getSelectedItem() {
        return (VelocityStabilizationModeEnum)super.getSelectedItem();
    }
}
