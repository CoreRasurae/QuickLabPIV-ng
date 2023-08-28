// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-LucasKanade")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsLucasKanadeModel extends SubPixelInterpolationOptionsModel {
    private boolean ignorePIVBaseDisplacements = false;
    private IgnorePIVBaseDisplacementsModeEnum ignorePIVBaseMode = IgnorePIVBaseDisplacementsModeEnum.IgnoreUV;
    
    private float filterSigma = 2.0f;
    private int filterWidthPx = 3;

    private boolean averageOfFourPixels = false;
    private int numberOfIterations = 5;
    private int windowSize         = 27;
    
    
    public SubPixelInterpolationOptionsLucasKanadeModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.LucasKanade);
    }
    
    public float getFilterSigma() {
        return filterSigma;
    }
    
    public void setFilterSigma(float sigma) {
        float oldValue = filterSigma;
        filterSigma = sigma;
        pcs.firePropertyChange("filterSigmaLK", oldValue, filterSigma);
    }
    
    public int getFilterWidthPx() {
        return filterWidthPx;
    }
    
    public void setFilterWidthPx(int widthPx) {
        int oldValue = filterWidthPx;
        filterWidthPx = widthPx;
        pcs.firePropertyChange("filterWidthPxLK", oldValue, filterWidthPx);
    }
    
    public boolean isAverageOfFourPixels() {
        return averageOfFourPixels;
    }
    
    public void setAverageOfFourPixels(boolean average) {
        boolean oldValue = averageOfFourPixels;
        averageOfFourPixels = average;
        pcs.firePropertyChange("averageOfFourPixelsLK", oldValue, averageOfFourPixels);
    }
    
    public int getNumberOfIterations() {
        return numberOfIterations;
    }
    
    public void setNumberOfIterations(int iterations) {
        int oldValue = numberOfIterations;
        numberOfIterations = iterations;
        pcs.firePropertyChange("numberOfIterationsLK", oldValue, numberOfIterations);
    }

    public int getWindowSize() {
        return windowSize;
    }
    
    public void setWindowSize(int size) {
        int oldValue = windowSize;
        windowSize = size;
        pcs.firePropertyChange("windowSizeLK", oldValue, windowSize);
    }
    
    public boolean isIgnorePIVBaseDisplacements() {
        return ignorePIVBaseDisplacements;
    }
    
    public void setIgnorePIVBaseDisplacements(boolean state) {
        boolean oldValue = ignorePIVBaseDisplacements;
        ignorePIVBaseDisplacements = state;
        pcs.firePropertyChange("ignorePIVBaseDisplacements", oldValue, ignorePIVBaseDisplacements);
    }
    
    public IgnorePIVBaseDisplacementsModeEnum getIgnorePIVBaseDisplacementsMode() {
        return ignorePIVBaseMode;
    }
    
    public void setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum mode) {
        IgnorePIVBaseDisplacementsModeEnum oldValue = ignorePIVBaseMode;
        ignorePIVBaseMode = mode;
        pcs.firePropertyChange("ignorePIVBaseMode", oldValue, ignorePIVBaseMode);
    }
    
    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsLucasKanadeModel model = new SubPixelInterpolationOptionsLucasKanadeModel();
        
        model = copy(model);
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
       visitor.visit(this);        
    }
    
    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsLucasKanadeModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsLucasKanadeModel anotherLK = (SubPixelInterpolationOptionsLucasKanadeModel)another;
        
        if (ignorePIVBaseDisplacements != anotherLK.ignorePIVBaseDisplacements) {
            changed = true;
        }
        
        if (ignorePIVBaseMode != anotherLK.ignorePIVBaseMode) {
            changed = true;
        }
        
        if (filterSigma != anotherLK.filterSigma) {
            changed = true;
        }

        if (filterWidthPx != anotherLK.filterWidthPx) {
            changed = true;
        }

        if (averageOfFourPixels != anotherLK.averageOfFourPixels) {
            changed = true;
        }

        if (numberOfIterations != anotherLK.numberOfIterations) {
            changed = true;
        }
        
        if (windowSize != anotherLK.windowSize) {
            changed = true;
        }
                
        return changed;
    }

    public SubPixelInterpolationOptionsLucasKanadeModel copy(SubPixelInterpolationOptionsLucasKanadeModel model) {
        model.ignorePIVBaseDisplacements = ignorePIVBaseDisplacements;
        model.ignorePIVBaseMode = ignorePIVBaseMode;
        model.filterSigma = filterSigma;
        model.filterWidthPx = filterWidthPx;
        model.averageOfFourPixels = averageOfFourPixels;
        model.numberOfIterations = numberOfIterations;
        model.windowSize = windowSize;
        
        return model;
    }
}
