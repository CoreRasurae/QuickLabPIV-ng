// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-LiuShenWithLucasKanade")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel extends SubPixelInterpolationOptionsModel {
    private boolean ignorePIVBaseDisplacements = false;
    private IgnorePIVBaseDisplacementsModeEnum ignorePIVBaseMode = IgnorePIVBaseDisplacementsModeEnum.IgnoreUV;
    
    //-------------------------------
    private float filterSigmaLK = 2.0f;
    private int filterWidthPxLK = 3;

    private int numberOfIterationsLK = 5;
    private int windowSizeLK         = 27;

    //-------------------------------
    private float filterSigmaLS = 0.48f;
    private int filterWidthPxLS = 5;

    private float multiplierLS = 4.0f;
    private int numberOfIterationsLS = 60;
    private int vectorsWindowSizeLS  = 13;

    public SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.LiuShenWithLucasKanade);
    }
    
    public float getFilterSigmaLK() {
        return filterSigmaLK;
    }
    
    public void setFilterSigmaLK(float sigma) {
        float oldValue = filterSigmaLK;
        filterSigmaLK = sigma;
        pcs.firePropertyChange("filterSigmaLK", oldValue, filterSigmaLK);
    }
    
    public int getFilterWidthPxLK() {
        return filterWidthPxLK;
    }
    
    public void setFilterWidthPxLK(int widthPx) {
        int oldValue = filterWidthPxLK;
        filterWidthPxLK = widthPx;
        pcs.firePropertyChange("filterWidthPxLK", oldValue, filterWidthPxLK);
    }
    
    public int getNumberOfIterationsLK() {
        return numberOfIterationsLK;
    }
    
    public void setNumberOfIterationsLK(int iterations) {
        int oldValue = numberOfIterationsLK;
        numberOfIterationsLK = iterations;
        pcs.firePropertyChange("numberOfIterationsLK", oldValue, numberOfIterationsLK);
    }

    public int getWindowSizeLK() {
        return windowSizeLK;
    }
    
    public void setWindowSizeLK(int size) {
        int oldValue = windowSizeLK;
        windowSizeLK = size;
        pcs.firePropertyChange("windowSizeLK", oldValue, windowSizeLK);
    }

    public float getFilterSigmaLS() {
        return filterSigmaLS;
    }
    
    public void setFilterSigmaLS(float sigma) {
        float oldValue = filterSigmaLS;
        filterSigmaLS = sigma;
        pcs.firePropertyChange("filterSigmaLS", oldValue, filterSigmaLS);
    }
    
    public int getFilterWidthPxLS() {
        return filterWidthPxLS;
    }
    
    public void setFilterWidthPxLS(int widthPx) {
        int oldValue = filterWidthPxLS;
        filterWidthPxLS = widthPx;
        pcs.firePropertyChange("filterWidthPxLS", oldValue, filterWidthPxLS);
    }
    
    public int getNumberOfIterationsLS() {
        return numberOfIterationsLS;
    }
    
    public void setNumberOfIterationsLS(int iterations) {
        int oldValue = numberOfIterationsLS;
        numberOfIterationsLS = iterations;
        pcs.firePropertyChange("numberOfIterationsLS", oldValue, numberOfIterationsLS);
    }

    public int getVectorsWindowSizeLS() {
        return vectorsWindowSizeLS;
    }
    
    public void setVectorsWindowSizeLS(int size) {
        int oldValue = vectorsWindowSizeLS;
        vectorsWindowSizeLS = size;
        pcs.firePropertyChange("vectorsWindowSizeLS", oldValue, vectorsWindowSizeLS);
    }

    public float getMultiplierLS() {
        return multiplierLS;
    }
    
    public void setMultiplierLS(float _multiplierLS) {
        float oldValue = multiplierLS;
        multiplierLS = _multiplierLS;
        pcs.firePropertyChange("multiplierLS", oldValue, multiplierLS);
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
        SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel model = new SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel();
        
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
        
        if (!(another instanceof SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel anotherLS = (SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel)another;

        if (ignorePIVBaseDisplacements != anotherLS.ignorePIVBaseDisplacements) {
            changed = true;
        }
        
        if (ignorePIVBaseMode != anotherLS.ignorePIVBaseMode) {
            changed = true;
        }
        
        if (filterSigmaLK != anotherLS.filterSigmaLK) {
            changed = true;
        }

        if (filterWidthPxLK != anotherLS.filterWidthPxLK) {
            changed = true;
        }

        if (numberOfIterationsLK != anotherLS.numberOfIterationsLK) {
            changed = true;
        }
        
        if (windowSizeLK != anotherLS.windowSizeLK) {
            changed = true;
        }

        if (filterSigmaLS != anotherLS.filterSigmaLS) {
            changed = true;
        }

        if (filterWidthPxLS != anotherLS.filterWidthPxLS) {
            changed = true;
        }

        if (multiplierLS != anotherLS.multiplierLS) {
            changed = true;
        }
        
        if (numberOfIterationsLS != anotherLS.numberOfIterationsLS) {
            changed = true;
        }
        
        if (vectorsWindowSizeLS != anotherLS.vectorsWindowSizeLS) {
            changed = true;
        }
        
        return changed;
    }

    public SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel copy(SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel model) {
        model.ignorePIVBaseDisplacements = ignorePIVBaseDisplacements;
        model.ignorePIVBaseMode = ignorePIVBaseMode;
        
        model.filterSigmaLK = filterSigmaLK;
        model.filterWidthPxLK = filterWidthPxLK;
        model.numberOfIterationsLK = numberOfIterationsLK;
        model.windowSizeLK = windowSizeLK;

        model.filterSigmaLS = filterSigmaLS;
        model.filterWidthPxLS = filterWidthPxLS;
        model.multiplierLS = multiplierLS;
        model.numberOfIterationsLS = numberOfIterationsLS;
        model.vectorsWindowSizeLS = vectorsWindowSizeLS;

        return model;
    }

}
