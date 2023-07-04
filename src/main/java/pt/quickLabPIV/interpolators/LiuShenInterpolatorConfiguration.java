package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.IgnorePIVBaseDisplacementsModeEnum;

public class LiuShenInterpolatorConfiguration {
    public final static String IDENTIFIER = "interpLiuShen";
    public final static String IDENTIFIER_APARAPI = "interpLiuShenAparapi";
    
    private float filterSigmaLK;
    private int filterWidthPxLK;
    
    private int windowSizeLK;
    private int iterationsLK;
    
    private int vectorsWindowSizeLS;
    private float filterSigmaLS;
    private int filterWidthPxLS;
    
    private float multLagrangeLS;
    private int iterationsLS;
    
    private boolean denseVectors = false;
    private boolean ignorePIVBaseDisplacements;
    private IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;

    public void setFilterSigmaLK(float sigmaLK) {
        filterSigmaLK = sigmaLK;
    }
    
    public float getFilterSigmaLK() {
        return filterSigmaLK;
    }
    
    public void setFilterWidthPxLK(int widthPxLK) {
        filterWidthPxLK = widthPxLK;
    }
    
    public int getFilterWidthPxLK() {
        return filterWidthPxLK;
    }
    
    public void setNumberOfIterationsLK(int iters) {
        iterationsLK = iters;
    }
    
    public int getNumberOfIterationsLK() {
        return iterationsLK;
    }

    public void setWindowSizeLK(int windowSize) {
        windowSizeLK = windowSize;
    }
    
    public int getWindowSizeLK() {
        return windowSizeLK;
    }
    
    public void setFilterSigmaLS(float sigma) {
        filterSigmaLS = sigma;
    }
    
    public float getFilterSigmaLS() {
        return filterSigmaLS;
    }
    
    public void setFilterWidthPxLS(int widthPx) {
        filterWidthPxLS = widthPx;
    }
    
    public int getFilterWidthPxLS() {
        return filterWidthPxLS;
    }
    
    public void setVectorsWindowSizeLS(int size) {
        vectorsWindowSizeLS = size;
    }
    
    public int getVectorsWindowSizeLS() {
        return vectorsWindowSizeLS;
    }
    
    public void setMultiplierLagrangeLS(float multiplier) {
        multLagrangeLS = multiplier;
    }
    
    public float getMultiplierLagrangeLS() {
        return multLagrangeLS;
    }

    public void setNumberOfIterationsLS(int iters) {
        iterationsLS = iters;
    }
    
    public int getNumberOfIterationsLS() {
        return iterationsLS;
    }
    
    public void setDenseVectors(boolean _dense) {
        denseVectors = _dense;
    }
    
    public boolean isDenseVectors() {
        return denseVectors;
    }

    public void setIgnorePIVBaseDisplacements(boolean _ignorePIVBaseDisplacements) {
        ignorePIVBaseDisplacements = _ignorePIVBaseDisplacements;
    }
    
    public boolean isIgnorePIVBaseDisplacements() {
        return ignorePIVBaseDisplacements;
    }

    public void setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum _ignorePIVMode) {
        ignorePIVMode = _ignorePIVMode;
    }
    
    public IgnorePIVBaseDisplacementsModeEnum getIgnorePIVBaseDisplacementsMode() {
        return ignorePIVMode;
    }
}
