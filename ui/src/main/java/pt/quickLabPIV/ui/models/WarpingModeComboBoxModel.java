// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.swing.DefaultComboBoxModel;

public class WarpingModeComboBoxModel extends DefaultComboBoxModel<WarpingModeEnum> {


    /**
     * 
     */
    private static final long serialVersionUID = -731765312343700152L;

    public WarpingModeComboBoxModel(WarpingModeEnum[] values) {
        super(values);
    }
    
    public WarpingModeComboBoxModel() {
        super(WarpingModeEnum.values());
    }

    public void updateAvailableResolutions(WarpingModeEnum[] values) {
        WarpingModeEnum selectedMode = getSelectedItem();
        boolean selectedResolutionExists = false;
        super.removeAllElements();        
        for (WarpingModeEnum value : values) {
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
    public WarpingModeEnum getSelectedItem() {
        return (WarpingModeEnum)super.getSelectedItem();
    }
    
}
