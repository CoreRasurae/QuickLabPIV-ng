// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

public class VelocityValidationOptionsMultiPeakNormalizedMedianModel extends VelocityValidationOptionsNormalizedMedianModel {
    private int numberOfPeaks;
    private int kernelSize;
    
    public VelocityValidationOptionsMultiPeakNormalizedMedianModel() {
        setValidationMode(VelocityValidationModeEnum.MultiPeakNormalizedMedian);
    }
    
    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }
        
    public void setNumberOfPeaks(int _numberOfPeaks) {
        float oldValue = numberOfPeaks;
        numberOfPeaks = _numberOfPeaks;
        pcs.firePropertyChange("numberOfPeaks", oldValue, numberOfPeaks);
    }

    public int getKernelSize() {
        return kernelSize;
    }
    
    public void setKernelSize(int _kernelSize) {
        float oldValue = kernelSize;
        kernelSize = _kernelSize;
        pcs.firePropertyChange("kernelSize", oldValue, kernelSize);
    }

    @Override
    public VelocityValidationOptionsModel copy() {
        VelocityValidationOptionsMultiPeakNormalizedMedianModel model = new VelocityValidationOptionsMultiPeakNormalizedMedianModel();
        
        super.copy(model);
        model.numberOfPeaks = numberOfPeaks;
        model.kernelSize = kernelSize;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(VelocityValidationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof VelocityValidationOptionsMultiPeakNormalizedMedianModel)) {
            return true;            
        }
        
        VelocityValidationOptionsMultiPeakNormalizedMedianModel anotherNormMedian = (VelocityValidationOptionsMultiPeakNormalizedMedianModel)another;
        
        if (super.isChanged(anotherNormMedian)) {
            return true;
        }
        
        if (numberOfPeaks != anotherNormMedian.numberOfPeaks) {
            changed = true;
        }
        
        if (kernelSize != anotherNormMedian.kernelSize) {
            changed = true;
        }
                
        return changed;
    }
}
