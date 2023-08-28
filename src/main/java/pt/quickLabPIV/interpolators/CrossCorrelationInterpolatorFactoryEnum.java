// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVRunParameters;

public enum CrossCorrelationInterpolatorFactoryEnum {
	None,
	BiCubic,
	Gaussian1D,
	Gaussian1DHongweiGuo,
	Gaussian1DPolynomial,
	Centroid2D,
	Gaussian2D,
	Gaussian2DPolynomial,
	Gaussian2DLinearRegression,
	LucasKanade,
	LucasKanadeAparapi,
	LiuShenWithLucasKanade,
	LiuShenWithLucasKanadeAparapi,
	CompositeLastLevelInterpolator; 
	
	public static ICrossCorrelationInterpolator createInterpolator(CrossCorrelationInterpolatorFactoryEnum interpolatorType) {
	    PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
	    PIVRunParameters runParameters = singleton.getPIVRunParameters();
		ICrossCorrelationInterpolator interpolator;
		
		boolean useOpenCL = runParameters.isUseOpenCL();
		
		switch (interpolatorType) {
		case None:
		    interpolator = null;
		    break;
		case BiCubic:
		    interpolator = new BasicInterpolatorAdapter(new BiCubicInterpolator());
		    break;
		case Gaussian1D:
		    interpolator = new BasicInterpolatorAdapter(new Gaussian1DInterpolator());
		    break;
		case Gaussian1DHongweiGuo:
		    interpolator = new BasicInterpolatorAdapter(new Gaussian1DHongweiGuoInterpolator());
            break;		    
		case Gaussian1DPolynomial:
		    interpolator = new BasicInterpolatorAdapter(new Gaussian1DPolynomialInterpolator());
		    break;
		case Centroid2D:
		    interpolator = new BasicInterpolatorAdapter(new Centroid2DInterpolator());
		    break;
		case Gaussian2D:
		    interpolator = new BasicInterpolatorAdapter(new Gaussian2DInterpolator());
		    break;
		case Gaussian2DPolynomial:
		    interpolator = new BasicInterpolatorAdapter(new Gaussian2DPolynomialInterpolator());
		    break;
        case Gaussian2DLinearRegression:
            interpolator = new BasicInterpolatorAdapter(new Gaussian2DLinearRegressionInterpolator());
            break;
        case LucasKanade:
            interpolator = new LucasKanadeFloat();
            break;
        case LucasKanadeAparapi:
            if (!useOpenCL) {
                throw new InterpolatorStateException("Cannot use Lucas-Kanade Aparapi implemenation as OpenCL acceleration is not allowed");
            }
            interpolator = new DenseLucasKanadeAparapiJobInterpolator();
            break;
        case LiuShenWithLucasKanade:
            interpolator = new LiuShenFloat();
            break;
        case LiuShenWithLucasKanadeAparapi:
            if (!useOpenCL) {
                throw new InterpolatorStateException("Cannot use Lucas-Kanade combined with Liu-Shen Aparapi implemenation as OpenCL acceleration is not allowed");
            }
            interpolator = new DenseLiuShenAparapiJobInterpolator();
            break;
        case CompositeLastLevelInterpolator:
            interpolator = new CompositeLastLevelInterpolator();
            break;
		default:
		    throw new InterpolatorStateException("Unknown interpolator");
		}
		
		return interpolator;
	}
}
