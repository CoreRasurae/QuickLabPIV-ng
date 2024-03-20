// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeSupport;

import javax.swing.DefaultComboBoxModel;

public class Gaussian2DStrategiesComboBoxModel extends DefaultComboBoxModel<Gaussian2DStrategiesEnum> {


    /**
     * 
     */
    private static final long serialVersionUID = -576924144784148614L;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    @Override
    public void setSelectedItem(Object object) {
        Gaussian2DStrategiesEnum oldValue = getSelectedItem();
        super.setSelectedItem(object);
        pcs.firePropertyChange("selectedItem", oldValue, object);
    }
    
    public Gaussian2DStrategiesComboBoxModel(Gaussian2DStrategiesEnum[] values) {
        super(values);
    }
    
    public Gaussian2DStrategiesComboBoxModel() {
        super(Gaussian2DStrategiesEnum.values());
    }

    @Override
    public Gaussian2DStrategiesEnum getElementAt(int index) {
        return super.getElementAt(index);
    }
    
    @Override
    public Gaussian2DStrategiesEnum getSelectedItem() {
        return (Gaussian2DStrategiesEnum)super.getSelectedItem();
    }
}
