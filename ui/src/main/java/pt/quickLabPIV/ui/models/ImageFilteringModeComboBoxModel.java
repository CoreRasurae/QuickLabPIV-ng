// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import javax.swing.DefaultComboBoxModel;

public class ImageFilteringModeComboBoxModel extends DefaultComboBoxModel<ImageFilteringModeEnum> {

    /**
     * 
     */
    private static final long serialVersionUID = 7143108430452852568L;

    public ImageFilteringModeComboBoxModel(ImageFilteringModeEnum[] values) {
        super(values);
    }
    
    public ImageFilteringModeComboBoxModel() {
        super(ImageFilteringModeEnum.values());
    }

    public void updateAvailableFilteringModes(ImageFilteringModeEnum[] values) {
        ImageFilteringModeEnum selectedMode = getSelectedItem();
        boolean selectedResolutionExists = false;
        super.removeAllElements();        
        for (ImageFilteringModeEnum value : values) {
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
    public ImageFilteringModeEnum getSelectedItem() {
        return (ImageFilteringModeEnum)super.getSelectedItem();
    }

}
