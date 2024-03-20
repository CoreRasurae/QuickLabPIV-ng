// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeSupport;

import javax.swing.DefaultComboBoxModel;

public class VelocityValidationComboBoxModel extends DefaultComboBoxModel<VelocityValidationModeEnum> {
    /**
     * 
     */
    private static final long serialVersionUID = 1114753338313309552L;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    @Override
    public void setSelectedItem(Object object) {
        VelocityValidationModeEnum oldValue = getSelectedItem();
        super.setSelectedItem(object);
        pcs.firePropertyChange("selectedItem", oldValue, (VelocityValidationModeEnum)object);
    }
    
    public VelocityValidationComboBoxModel(VelocityValidationModeEnum[] values) {
        super(values);
    }
    
    public VelocityValidationComboBoxModel() {
        super(VelocityValidationModeEnum.values());
    }

    @Override
    public VelocityValidationModeEnum getElementAt(int index) {
        return super.getElementAt(index);
    }
    
    @Override
    public VelocityValidationModeEnum getSelectedItem() {
        return (VelocityValidationModeEnum)super.getSelectedItem();
    }
}
