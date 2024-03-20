// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;

public class SubPixelInterpolationComboBoxModel extends DefaultComboBoxModel<SubPixelInterpolationModeEnum> {

    /**
     * 
     */
    private static final long serialVersionUID = 81251150591717637L;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    @Override
    public void setSelectedItem(Object object) {
        SubPixelInterpolationModeEnum oldValue = getSelectedItem();
        super.setSelectedItem(object);
        pcs.firePropertyChange("selectedItem", oldValue, object);
    }
    
    public SubPixelInterpolationComboBoxModel(SubPixelInterpolationModeEnum[] values) {
        super(values);
    }

    public SubPixelInterpolationComboBoxModel(boolean excludeComposite, boolean excludeNonOpticalFlow) {
        super(Arrays.stream(SubPixelInterpolationModeEnum.values())
                    .filter(x -> !(excludeComposite && x.isCompositeMode()) && 
                                 !(excludeNonOpticalFlow && !x.isOpticalFlow())).toArray(x -> new SubPixelInterpolationModeEnum[x]));
    }
    
    public SubPixelInterpolationComboBoxModel() {
        super(SubPixelInterpolationModeEnum.values());
    }

    @Override
    public SubPixelInterpolationModeEnum getElementAt(int index) {
        return super.getElementAt(index);
    }
    
    @Override
    public SubPixelInterpolationModeEnum getSelectedItem() {
        return (SubPixelInterpolationModeEnum)super.getSelectedItem();
    }
}
