// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeSupport;

import javax.swing.DefaultComboBoxModel;

public class InterrogationAreaResolutionComboBoxModel extends DefaultComboBoxModel<InterrogationAreaResolutionEnum> {

    /**
     * 
     */
    private static final long serialVersionUID = -6975329498665501113L;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    boolean fireChangesDisabled = false;
    
    @Override
    public void setSelectedItem(Object object) {
        InterrogationAreaResolutionEnum oldValue = (InterrogationAreaResolutionEnum)getSelectedItem();
        super.setSelectedItem(object);
        pcs.firePropertyChange("selectedItem", oldValue, (InterrogationAreaResolutionEnum)object);
    }
    
    public InterrogationAreaResolutionComboBoxModel(InterrogationAreaResolutionEnum[] values) {
        super(values);
    }
    
    public InterrogationAreaResolutionComboBoxModel() {
        super(InterrogationAreaResolutionEnum.values());
        //This disallows IA0/Invalid to be shown at all...
        //super(Arrays.copyOfRange(InterrogationAreaResolutionEnum.values(), 1, InterrogationAreaResolutionEnum.values().length));
        //Solution would be to remove Invalid after first valid selection
    }

    public void updateAvailableResolutions(InterrogationAreaResolutionEnum[] values) {
        InterrogationAreaResolutionEnum selectedResolution = getSelectedItem();
        //System.out.println(this + " - Previous res.: " + selectedResolution);
        boolean selectedResolutionExists = false;
        //Disable firechanged events
        fireChangesDisabled = true;
        removeAllElements();        
        for (InterrogationAreaResolutionEnum value : values) {
            if (value == selectedResolution) {
                selectedResolutionExists = true;
            }
            addElement(value);
        }
        
        if (selectedResolutionExists) {
            setSelectedItem(selectedResolution);
            //System.out.println(this + " - Found: " + selectedResolution);
        } else {
            //System.out.println(this + " - Not Found: " + selectedResolution);
            setSelectedItem(InterrogationAreaResolutionEnum.IA0);
        }
        
        //Re-enable events
        fireChangesDisabled = false;
        fireContentsChanged(this, 0, values.length-1);
    }
    
    @Override
    public InterrogationAreaResolutionEnum getElementAt(int index) {
        return super.getElementAt(index);
    }
    
    @Override
    public InterrogationAreaResolutionEnum getSelectedItem() {
        return (InterrogationAreaResolutionEnum)super.getSelectedItem();
    }
    
    @Override
    public void removeAllElements() {
        super.removeAllElements();
    }
    
    @Override
    public void addElement(InterrogationAreaResolutionEnum anObject) {    
        super.addElement(anObject);
    }
    
    @Override
    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        //System.out.println("Firing1");
        if (!fireChangesDisabled) {
            //System.out.println("Fire interval1");
            super.fireIntervalRemoved(source, index0, index1);
        }
    }
    
    @Override
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        //System.out.println("Firing2");
        if (!fireChangesDisabled) {
            //System.out.println("Fire interval2");
            super.fireIntervalAdded(source, index0, index1);
        }
    }
    
    @Override
    protected void fireContentsChanged(Object source, int index0, int index1) {
        //System.out.println("Firing3");
        if (!fireChangesDisabled) {
            //System.out.println("Fire interval3");
            super.fireContentsChanged(source, index0, index1);
        }
    }
}
