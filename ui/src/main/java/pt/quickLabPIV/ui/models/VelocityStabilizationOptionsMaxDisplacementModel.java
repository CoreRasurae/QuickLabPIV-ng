// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

public class VelocityStabilizationOptionsMaxDisplacementModel extends VelocityStabilizationOptionsModel {
    private int maxPixels;
    private int maxIterations;
    
    public VelocityStabilizationOptionsMaxDisplacementModel() {
        setStabilizationMode(VelocityStabilizationModeEnum.MaxDisplacement);
    }
    
    public int getMaxDisplacementPixels() {
        return maxPixels;
    }
    
    public void setMaxDisplacementPixels(int _maxPixels) {
        int oldValue = maxPixels;
        maxPixels = _maxPixels;
        pcs.firePropertyChange("maxDisplacementPixels", oldValue, maxPixels);
    }
    
    public int getMaxIterations() {
        return maxIterations;
    }
    
    public void setMaxIterations(int _maxIterations) {
        int oldValue = maxIterations;
        maxIterations = _maxIterations;
        pcs.firePropertyChange("maxIterations", oldValue, maxIterations);
    }
    
    @Override
    public VelocityStabilizationOptionsModel copy() {
        VelocityStabilizationOptionsMaxDisplacementModel model = new VelocityStabilizationOptionsMaxDisplacementModel();
        
        model.maxPixels = maxPixels;
        model.maxIterations = maxIterations;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(VelocityStabilizationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof VelocityStabilizationOptionsMaxDisplacementModel)) {
            return true;            
        }
        
        VelocityStabilizationOptionsMaxDisplacementModel anotherMaxDisp = (VelocityStabilizationOptionsMaxDisplacementModel)another;
                
        if (maxPixels != anotherMaxDisp.maxPixels) {
            changed = true;
        }
        
        if (maxIterations != anotherMaxDisp.maxIterations) {
            changed = true;
        }
        
        return changed;
    }

}
