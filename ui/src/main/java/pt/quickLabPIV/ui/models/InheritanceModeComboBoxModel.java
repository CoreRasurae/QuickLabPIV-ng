// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import javax.swing.DefaultComboBoxModel;

public class InheritanceModeComboBoxModel extends DefaultComboBoxModel<InheritanceModeEnum> {

    /**
     * 
     */
    private static final long serialVersionUID = -6820887139661695167L;

    public InheritanceModeComboBoxModel(InheritanceModeEnum[] values) {
        super(values);
    }
    
    public InheritanceModeComboBoxModel() {
        super(InheritanceModeEnum.values());
    }

    public void updateAvailableResolutions(InheritanceModeEnum[] values) {
        InheritanceModeEnum selectedMode = getSelectedItem();
        boolean selectedResolutionExists = false;
        super.removeAllElements();        
        for (InheritanceModeEnum value : values) {
            if (value == selectedMode) {
                selectedResolutionExists = true;
            }
            super.addElement(value);
        }
        
        if (selectedResolutionExists) {
            setSelectedItem(selectedMode);
        } else {
            setSelectedItem(null);
        }
    }
    
    @Override
    public InheritanceModeEnum getSelectedItem() {
        return (InheritanceModeEnum)super.getSelectedItem();
    }
    
}
