// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.IgnorePIVBaseDisplacementsModeEnum;

public class LucasKanadeInterpolatorConfiguration {
    public final static String IDENTIFIER = "InterpLucasKanade";
    public final static String IDENTIFIER_APARAPI = "InterpLucasKanadeAparapi";

    private boolean averageOfFourPixels;
    private int iterations;
    private int windowSize;
    
    private float filterSigma;
    private int filterWidthPx;

    private boolean denseExport = false;
    private boolean ignorePIVBaseDisplacements;
    private IgnorePIVBaseDisplacementsModeEnum ignorePIVMode;
    
    public void setAverageOfFourPixels(boolean average) {
        averageOfFourPixels = average;
    }
    
    public boolean getAverageOfFourPixels() {
        return averageOfFourPixels;
    }

    public void setNumberOfIterations(int iters) {
        if (iters < 1) {
            throw new InterpolatorStateException("Lucas-Kanade number of iterations must be at least 1");
        }
        
        iterations = iters;
    }
    
    public int getNumberOfIterations() {
        if (iterations < 1) {
            throw new InterpolatorStateException("Lucas-Kanade number of iterations must be at least 1");
        }
        
        return iterations;
    }
	
    public void setWindowSize(int size) {
        if (size % 2 != 1) {
            throw new InterpolatorStateException("Lucas-Kanade window size must be an odd number");
        }
        
        if (size < 3) {
            throw new InterpolatorStateException("Lucas-Kanade window size must be at least 3");
        }
        
        windowSize = size;
    }
        
    public int geWindowSize() {
        if (windowSize % 2 != 1) {
            throw new InterpolatorStateException("Lucas-Kanade window size must be an odd number");
        }
    
        if (windowSize < 3) {
            throw new InterpolatorStateException("Lucas-Kanade window size must be at least 3");
        }
        
        return windowSize;
    }
    
    
    public void setFilterSigma(float sigma) {
        if (sigma < 0.0f || sigma > 6.0f) {
            throw new InterpolatorStateException("Filter sigma value must be non-negative and less or equal to 6.0");
        }
        
        filterSigma = sigma;
    }
    
    public float getFilterSigma() {
        if (filterSigma < 0.0f || filterSigma > 6.0f) {
            throw new InterpolatorStateException("Filter sigma value must be non-negative and less or equal to 6.0");
        }
    
        return filterSigma;
    }
    
    public void setFilterWidthPx(int widthPx) {
    	if (widthPx % 2 != 1) {
    		throw new InterpolatorStateException("Lucas-Kanade filter width (in pixels) must be an odd number");
    	}
    	
    	if (widthPx < 3) {
    	    throw new InterpolatorStateException("Lucas-Kanade filter width (in pixels) must be greater or equal to 3");
    	}
    	
    	filterWidthPx = widthPx;
    }
    	
    public int getFilterWidthPx() {
        if (filterWidthPx % 2 != 1) {
            throw new InterpolatorStateException("Lucas-Kanade filter width (in pixels) must be an odd number");
        }
        
        if (filterWidthPx < 3) {
            throw new InterpolatorStateException("Lucas-Kanade filter width (in pixels) must be greater or equal to 3");
        }
    
        return filterWidthPx;
    }

    public void setDenseExport(boolean _denseExport) {
        denseExport  = _denseExport;
    }

    public boolean isDenseExport() {
        return denseExport;
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
