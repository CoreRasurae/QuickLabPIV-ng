// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

public class GaussianFilter2DConfiguration {
    private float sigma;
    private int kernelPx;
    
    public final static String IDENTIFER = "ImgFilterGaussian2D";
    
    /**
     * Creates a 2D Gaussian filter configuration
     * @param _sigma the standard deviation
     * @param _kernelPx the number of pixels that define the filter size
     */
    public GaussianFilter2DConfiguration(float _sigma, int _kernelPx) {
        sigma = _sigma;
        kernelPx = _kernelPx;
    }
    
    public float getSigma() {
        return sigma;
    }
    
    public int getKernelPx() {
        return kernelPx;
    }

}
