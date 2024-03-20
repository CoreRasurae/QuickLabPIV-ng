// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public class VelocityValidationOptionsNormalizedMedianModel extends VelocityValidationOptionsModel {
    private float distanceThresholdPixels;
    private float epsilon0;
    
    public VelocityValidationOptionsNormalizedMedianModel() {
        setValidationMode(VelocityValidationModeEnum.NormalizedMedian);
    }
    
    public float getDistanceThresholdPixels() {
        return distanceThresholdPixels;
    }
    
    public void setDistanceThresholdPixels(float _distanceThresholdPixels) {
        float oldValue = distanceThresholdPixels;
        distanceThresholdPixels = _distanceThresholdPixels;
        pcs.firePropertyChange("distanceThresholdPixels", oldValue, distanceThresholdPixels);
    }

    public float getEpsilon0() {
        return epsilon0;
    }
    
    public void setEpsilon0(float _epsilon0) {
        float oldValue = epsilon0;
        epsilon0 = _epsilon0;
        pcs.firePropertyChange("epsilon0", oldValue, epsilon0);
    }

    @Override
    public VelocityValidationOptionsModel copy() {
        VelocityValidationOptionsNormalizedMedianModel model = new VelocityValidationOptionsNormalizedMedianModel();
                
        return copy(model);
    }
    
    protected VelocityValidationOptionsNormalizedMedianModel copy(VelocityValidationOptionsNormalizedMedianModel model) {
        model.distanceThresholdPixels = distanceThresholdPixels;
        model.epsilon0 = epsilon0;
        
        return model;
    }


    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(VelocityValidationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof VelocityValidationOptionsNormalizedMedianModel)) {
            return true;            
        }
        
        VelocityValidationOptionsNormalizedMedianModel anotherNormMedian = (VelocityValidationOptionsNormalizedMedianModel)another;
                
        if (distanceThresholdPixels != anotherNormMedian.distanceThresholdPixels) {
            changed = true;
        }
        
        if (epsilon0 != anotherNormMedian.epsilon0) {
            changed = true;
        }
                
        return changed;
    }
}
