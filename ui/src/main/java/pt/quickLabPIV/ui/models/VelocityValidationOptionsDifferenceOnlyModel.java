// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class VelocityValidationOptionsDifferenceOnlyModel extends VelocityValidationOptionsDifferenceModel {  
    public VelocityValidationOptionsDifferenceOnlyModel() {
        setValidationMode(VelocityValidationModeEnum.DifferenceOnly);
    }
    
    //This method must be implemented here to ensure proper class preservation and specialized behavior
    @Override
    public VelocityValidationOptionsModel copy() {
        VelocityValidationOptionsDifferenceOnlyModel model = new VelocityValidationOptionsDifferenceOnlyModel();
        
        model.setDistanceThresholdPixels(getDistanceThresholdPixels());
        
        return model;
    }
    
    //This method must be implemented here to ensure proper class preservation and specialized behavior
    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    //This method must be implemented here to ensure proper class preservation and specialized behavior
    @Override
    public boolean isChanged(VelocityValidationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof VelocityValidationOptionsDifferenceOnlyModel)) {
            return true;            
        }
        
        VelocityValidationOptionsDifferenceOnlyModel anotherDifference = (VelocityValidationOptionsDifferenceOnlyModel)another;
                
        if (super.getDistanceThresholdPixels() != anotherDifference.getDistanceThresholdPixels()) {
            changed = true;
        }
                
        return changed;
    }

}
