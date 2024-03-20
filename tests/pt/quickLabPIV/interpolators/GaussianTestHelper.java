// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.interpolators.Centroid2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian1DHongweiGuoInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian1DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DPolynomialInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DSubTypeFactoryEnum;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class GaussianTestHelper {
    private static final float GAIN = 1000.0f;
    
    public static float gaussian2DAssymmetric(float gain, float uX, float sigmaX, float uY, float sigmaY, float x, float y) {
        return (float)(gain * 1.0f/(2.0f * FastMath.PI * sigmaX * sigmaY) *
                FastMath.exp(-(FastMath.pow(x - uX, 2)/(2.0f * FastMath.pow(sigmaX, 2)) + FastMath.pow(y - uY, 2)/(2.0f * FastMath.pow(sigmaY, 2)))));
    }

    public static float gaussian2DAssymmetricRotated(float gain, float uX, float sigmaX, float uY, float sigmaY, float theta, float x, float y) {
        double cosTheta = FastMath.cos(theta);
        double sinTheta = FastMath.sin(theta);
        
        double thetaX = FastMath.exp(-FastMath.pow(cosTheta *(x - uX) - sinTheta * (y -uY), 2)/(2.0f * FastMath.pow(sigmaX, 2)));
        double thetaY = FastMath.exp(-FastMath.pow(sinTheta *(x - uX) + cosTheta * (y -uY), 2)/(2.0f * FastMath.pow(sigmaY, 2)));
        return (float)(gain * 1.0f/(2.0f * FastMath.PI * sigmaX * sigmaY) * thetaX * thetaY);
    }

    public static Matrix createMatrixWithGaussian2DAssymmetric(int dimX, int dimY, float uX, float sigmaX, float uY, float sigmaY) {
        Matrix result = new MatrixFloat(dimY, dimX);
        
        for (int i = 0; i < dimY; i++) {
            for (int j = 0; j < dimX; j++) {
                float value = gaussian2DAssymmetric(GAIN, uX, sigmaX, uY, sigmaY, j-dimX/2, i-dimY/2);
                result.setElement(value, i, j);
            }
        }
        
        return result;
    }

    public static Matrix createMatrixWithGaussian2DAssymmetricRotated(int dimX, int dimY, float uX, float sigmaX, float uY, float sigmaY, float theta) {
        Matrix result = new MatrixFloat(dimY, dimX);
        
        for (int i = 0; i < dimY; i++) {
            for (int j = 0; j < dimX; j++) {
                float value = gaussian2DAssymmetricRotated(GAIN, uX, sigmaX, uY, sigmaY, theta, j-dimX/2, i-dimY/2);
                result.setElement(value, i, j);
            }
        }
        
        return result;
    }

    public static MaxCrossResult setupPIVContext(PIVContextSingleton context, float maxValue, int dimX, int dimY, Gaussian2DSubTypeFactoryEnum subType) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
        Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
        config.setProperties(dimX, dimY, subType);
        parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dimX/2);
        result.setMainPeakJ(dimY/2);
        result.setMainPeakValue(maxValue);

        return result;
    }
    
    public static MaxCrossResult setupPIVContext(PIVContextSingleton context, int dimX, int dimY, Gaussian2DSubTypeFactoryEnum subType) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
        Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
        config.setProperties(dimX, dimY, subType);
        parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dimX/2);
        result.setMainPeakJ(dimY/2);
        result.setMainPeakValue(GAIN);

        return result;
    }
    
    public static MaxCrossResult setupPIVContext1D(PIVContextSingleton context, int dim) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);
        Gaussian1DInterpolatorConfiguration config = new Gaussian1DInterpolatorConfiguration();
        config.setInterpolationPixels(dim);
        parameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dim/2);
        result.setMainPeakJ(dim/2);
        result.setMainPeakValue(GAIN);

        return result;        
    }

    public static MaxCrossResult setupPIVContextHongWeiGuo1D(PIVContextSingleton context, int dim) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1DHongweiGuo);
        Gaussian1DHongweiGuoInterpolatorConfiguration config = new Gaussian1DHongweiGuoInterpolatorConfiguration();
        config.setInterpolationPixels(dim);
        config.setInteporlationIterations(20);
        parameters.setSpecificConfiguration(Gaussian1DHongweiGuoInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dim/2);
        result.setMainPeakJ(dim/2);
        result.setMainPeakValue(GAIN);

        return result;        
    }
    
    public static MaxCrossResult setupPIVContextGaussian2DPoly(PIVContextSingleton context, int dim) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2DPolynomial);
        Gaussian2DPolynomialInterpolatorConfiguration config = new Gaussian2DPolynomialInterpolatorConfiguration();
        config.setInterpolationPixelsForCentroid2D(dim);
        parameters.setSpecificConfiguration(Gaussian2DPolynomialInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dim/2);
        result.setMainPeakJ(dim/2);
        result.setMainPeakValue(GAIN);

        return result;        
    }

    public static MaxCrossResult setupPIVContextCentroid2D(PIVContextSingleton context, int dim) {
        PIVInputParameters parameters = context.getPIVParameters();
        parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Centroid2D);
        Centroid2DInterpolatorConfiguration config = new Centroid2DInterpolatorConfiguration();
        config.setInterpolationPixels(dim);
        parameters.setSpecificConfiguration(Centroid2DInterpolatorConfiguration.IDENTIFIER, config);
        
        MaxCrossResult result = new MaxCrossResult();
        result.setMainPeakI(dim/2);
        result.setMainPeakJ(dim/2);
        result.setMainPeakValue(GAIN);

        return result;        
    }

}
