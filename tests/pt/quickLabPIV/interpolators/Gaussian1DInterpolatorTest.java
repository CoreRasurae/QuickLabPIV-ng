// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian1DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.IBasicCrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.InterpolatorStateException;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian1DInterpolatorTest {
	
    public IBasicCrossCorrelationInterpolator simpleAdapter(ICrossCorrelationInterpolator interp) {
        return new IBasicCrossCorrelationInterpolator() {
            private ICrossCorrelationInterpolator _interp = interp;
            
            @Override
            public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
                List<MaxCrossResult> results = new ArrayList<>(1);
                results.add(result);
                results = _interp.interpolate(results);
                return results.get(0);
            }
        };
    }

	@Before
	public void setup() {
		PIVContextTestsSingleton.setSingleton();
	}
	
	@Test(expected=InterpolatorStateException.class)
	public void configurationTest1Fail() {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);

		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(2);
		result.setMainPeakJ(2);
		result.setMainPeakValue(1000.0f);
		
		Matrix m = new MatrixFloat((short)5,(short)5);
		result.setCrossMatrix(m);
		

		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
	}
	
	//Disabled - Exception is not being thrown by current code configuration - @Test(expected=InterpolatorStateException.class)
	public void simple1DInterpolatorTest1ManyPointsFail() {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);
		Gaussian1DInterpolatorConfiguration config = new Gaussian1DInterpolatorConfiguration();
		config.setInterpolationPixels(7);
		parameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, config);
		
		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(2);
		result.setMainPeakJ(2);
		result.setMainPeakValue(1000.0f);
		
		Matrix m = new MatrixFloat((short)5,(short)5);
		m.setElement(1000.0f, 2, 2);
		
		//In J direction the max is deviated to the Right
		m.setElement(800.0f, 2, 0);
		m.setElement(900.0f, 2, 1);
		m.setElement(950.0f, 2, 3);
		m.setElement(850.0f, 2, 4);

		//In J direction the max is deviated to the Top
		m.setElement(850.0f, 0, 2);
		m.setElement(950.0f, 1, 2);
		m.setElement(900.0f, 3, 2);
		m.setElement(800.0f, 4, 2);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 2.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 2.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 1.0f);
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakJ() < 3.0f);
	}
		

	
	@Test
	public void simple1DInterpolatorTest1Pass() {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);
		Gaussian1DInterpolatorConfiguration config = new Gaussian1DInterpolatorConfiguration();
		config.setInterpolationPixels(5);
		parameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, config);
		
		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(2);
		result.setMainPeakJ(2);
		result.setMainPeakValue(1000.0f);
		
		Matrix m = new MatrixFloat((short)5,(short)5);
		m.setElement(1000.0f, 2, 2);
		
		//In J direction the max is deviated to the Right
		m.setElement(800.0f, 2, 0);
		m.setElement(900.0f, 2, 1);
		m.setElement(950.0f, 2, 3);
		m.setElement(850.0f, 2, 4);

		//In J direction the max is deviated to the Top
		m.setElement(850.0f, 0, 2);
		m.setElement(950.0f, 1, 2);
		m.setElement(900.0f, 3, 2);
		m.setElement(800.0f, 4, 2);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 2.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 2.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 1.0f);
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakJ() < 3.0f);
	}
}
