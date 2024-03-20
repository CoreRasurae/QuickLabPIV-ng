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
public class VelocityValidationOptionsNormalizedMedianOnlyModel extends VelocityValidationOptionsNormalizedMedianModel {  
    public VelocityValidationOptionsNormalizedMedianOnlyModel() {
        setValidationMode(VelocityValidationModeEnum.NormalizedMedianOnly);
    }

    //This method must be implemented here to ensure proper class preservation and specialized behavior
    @Override
    public VelocityValidationOptionsModel copy() {
        VelocityValidationOptionsNormalizedMedianOnlyModel model = new VelocityValidationOptionsNormalizedMedianOnlyModel();
                
        model.setDistanceThresholdPixels(getDistanceThresholdPixels());
        model.setEpsilon0(getEpsilon0());
        
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
        
        if (!(another instanceof VelocityValidationOptionsNormalizedMedianOnlyModel)) {
            return true;            
        }
        
        VelocityValidationOptionsNormalizedMedianOnlyModel anotherNormMedian = (VelocityValidationOptionsNormalizedMedianOnlyModel)another;
                
        if (super.getDistanceThresholdPixels() != anotherNormMedian.getDistanceThresholdPixels()) {
            changed = true;
        }
        
        if (super.getEpsilon0() != anotherNormMedian.getEpsilon0()) {
            changed = true;
        }
                
        return changed;
    }
}
