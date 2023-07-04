package pt.quickLabPIV.ui.models;

import javax.swing.DefaultComboBoxModel;

public class ClippingModeComboBoxModel extends DefaultComboBoxModel<ClippingModeEnum> {

    /**
     * 
     */
    private static final long serialVersionUID = 6124466124757330493L;

    public ClippingModeComboBoxModel(ClippingModeEnum[] values) {
        super(values);
    }
    
    public ClippingModeComboBoxModel() {
        super(ClippingModeEnum.values());
    }

    public void updateAvailableResolutions(ClippingModeEnum[] values) {
        ClippingModeEnum selectedMode = getSelectedItem();
        boolean selectedResolutionExists = false;
        super.removeAllElements();        
        for (ClippingModeEnum value : values) {
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
    public ClippingModeEnum getSelectedItem() {
        return (ClippingModeEnum)super.getSelectedItem();
    }
    
}
