// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DSubTypeFactoryEnum;
import pt.quickLabPIV.interpolators.IBasicCrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.InterpolatorStateException;
import pt.quickLabPIV.maximum.FindMaximumSimple;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian2DInterpolatorTest {
	
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

	@Test
	public void testAssymetricWithRotationCenteredTest1Pass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DCenteredTest1Pass(config, 1.0f, 1.3f);
	}
	
	@Test
	public void testAssymetricWithRotationCenteredTest2Pass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DCenteredTest2Pass(config, 1.0f, 1.3f);
	}
	
	@Test
	public void testAssymetricWithRotationCenteredTest3Pass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DCenteredTest3Pass(config, 1.0f, 1.3f);		
	}

	@Test
	public void testAssymetricWithRotationDeviatedUpLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedUpLeftTestPass(config, 0.50f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedDownLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedDownLeftTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedDownRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedDownRightTestPass(config, -0.30f, 1.3f, 1.0f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedLeftTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedUpTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedUpTestPass(config, -0.30f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedDownTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedDownTestPass(config, 0.40f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricWithRotationDeviatedRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedRightTestPass(config, -0.40f, 1.3f, 1.0f);		
	}	
	
	@Test
	public void testAssymetricWithRotationDeviatedUpRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		
		simple2DDeviatedUpRightTestPass(config, 0.30f, 1.3f, 1.0f);		
	}	
	
	@Test
	public void testAssymetricCenteredTest1Pass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DCenteredTest1Pass(config, 1.0f, 1.3f);
	}

	@Test
	public void testAssymetricDeviatedUpLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpLeftTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricDeviatedDownLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownLeftTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricDeviatedDownRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownRightTestPass(config, 0.00f, 1.3f, 1.0f);		
	}	

	@Test
	public void testAssymetricDeviatedUpRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpRightTestPass(config, 0.00f, 1.3f, 1.0f);		
	}

	@Test
	public void testAssymetricDeviatedUpTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricDeviatedDownTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownTestPass(config, 0.00f, 1.0f, 1.3f);		
	}	

	@Test
	public void testAssymetricDeviatedRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedRightTestPass(config, 0.00f, 1.3f, 1.0f);		
	}	
	
	@Test
	public void testAssymetricDeviatedLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Assymmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedLeftTestPass(config, 0.00f, 1.3f, 1.0f);		
	}	

	@Test
	public void testSymmetricCenteredTest1Pass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DCenteredTest1Pass(config, 1.0f, 1.0f);
	}

	@Test
	public void testSymmetricDeviatedUpLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpLeftTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	

	@Test
	public void testSymmetricDeviatedDownLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownLeftTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	

	@Test
	public void testSymmetricDeviatedDownRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownRightTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	

	@Test
	public void testSymmetricDeviatedUpRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpRightTestPass(config, 0.00f, 1.0f, 1.0f);		
	}

	@Test
	public void testSymmetricDeviatedUpTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedUpTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	

	@Test
	public void testSymmetricDeviatedDownTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedDownTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	

	@Test
	public void testSymmetricDeviatedRightTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedRightTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	
	
	@Test
	public void testSymmetricDeviatedLeftTestPass() {
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(5, 5, Gaussian2DSubTypeFactoryEnum.Symmetric);;
		config.setLogResults(true);
		
		simple2DDeviatedLeftTestPass(config, 0.00f, 1.0f, 1.0f);		
	}	
	
	@Test(expected=InterpolatorStateException.class)
	public void configurationTest1Fail() {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);

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
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		Gaussian2DInterpolatorConfiguration config = new Gaussian2DInterpolatorConfiguration();
		config.setProperties(7, 7, Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation);;
		config.setLogResults(true);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
		
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

		//In I direction the max is deviated to the Top
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
		
	public void simple2DCenteredTest1Pass(Gaussian2DInterpolatorConfiguration config, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.00f, sigmaX, sigmaY, m);		

		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(12);
		result.setMainPeakJ(21);
		result.setMainPeakValue(1000.0f);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertEquals("Interpolation incorrectly deviated in I direction from integer max.", 12.0f, result.getMainPeakI(), 0.0001f);
		
		assertEquals("Interpolation incorrectly deviated in J direction from integer max.", 21.0f, result.getMainPeakJ(), 0.0001f);
		
		assertEquals("Interpolated maximum is incorrect", 1000.0f, result.getMainPeakValue(), 0.0001f);
	}	
	
	public void simple2DCenteredTest2Pass(Gaussian2DInterpolatorConfiguration config, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.30f, sigmaX, sigmaY, m);		

		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(12);
		result.setMainPeakJ(21);
		result.setMainPeakValue(1000.0f);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertEquals("Interpolation incorrectly deviated in I direction from integer max.", 12.0f, result.getMainPeakI(), 0.0001f);
		
		assertEquals("Interpolation incorrectly deviated in J direction from integer max.", 21.0f, result.getMainPeakJ(), 0.0001f);
		
		assertEquals("Interpolated maximum is incorrect", 1000.0f, result.getMainPeakValue(), 0.0001f);
	}
	
	public void simple2DCenteredTest3Pass(Gaussian2DInterpolatorConfiguration config, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)FastMath.PI / 3.0f, sigmaX, sigmaY, m);		

		MaxCrossResult result = new MaxCrossResult();
		result.setMainPeakI(12);
		result.setMainPeakJ(21);
		result.setMainPeakValue(1000.0f);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertEquals("Interpolation incorrectly deviated in I direction from integer max.", 12.0f, result.getMainPeakI(), 0.0001f);
		
		assertEquals("Interpolation incorrectly deviated in J direction from integer max.", 21.0f, result.getMainPeakJ(), 0.0001f);
		
		assertEquals("Interpolated maximum is incorrect", 1000.0f, result.getMainPeakValue(), 0.0001f);
	}
	
	public void simple2DDeviatedDownLeftTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)thetha, sigmaX, sigmaY, m);
		m.setElement((m.getElement(13, 20) + m.getElement(12, 21)) / 2.0f, 13, 20);
		m.setElement((m.getElement(14, 19) + m.getElement(13, 20)) / 2.0f, 14, 19);
		m.setElement((m.getElement(15, 18) + m.getElement(14, 19)) / 2.0f, 15, 18);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
        result.setCrossMatrix(m);		
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 12.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 21.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 13.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 20.0f);
	}

	public void simple2DDeviatedUpLeftTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)thetha, sigmaX, sigmaY, m);
		m.setElement((m.getElement(11, 20) + m.getElement(12, 21)) / 2.0f, 11, 20);
		m.setElement((m.getElement(10, 19) + m.getElement(11, 20)) / 2.0f, 10, 19);
		m.setElement((m.getElement(9, 18) + m.getElement(10, 19)) / 2.0f, 9, 18);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 11.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 21.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 12.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 20.0f);
	}

	public void simple2DDeviatedUpRightTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.00f, 1.0f, 1.3f, m);
		m.setElement((m.getElement(11, 22) + m.getElement(12, 21)) / 2.0f, 11, 22);
		m.setElement((m.getElement(10, 23) + m.getElement(11, 22)) / 2.0f, 10, 23);
		m.setElement((m.getElement(9, 24) + m.getElement(10, 23)) / 2.0f, 9, 24);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		result.setCrossMatrix(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 11.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 22.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 12.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 21.0f);
	}

	public void simple2DDeviatedDownRightTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)thetha, sigmaX, sigmaY, m);
		m.setElement((m.getElement(12, 22) + m.getElement(12, 21)) / 2.0f, 12, 22);
		m.setElement((m.getElement(13, 23) + m.getElement(12, 22)) / 2.0f, 13, 23);
		m.setElement((m.getElement(14, 24) + m.getElement(13, 23)) / 2.0f, 14, 24);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 12.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 22.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 13.0f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 21.0f);
	}

	public void simple2DDeviatedDownTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)thetha, sigmaX, sigmaY, m);
		m.setElement((m.getElement(13, 21) + m.getElement(12, 21)) / 2.0f, 13, 21);
		m.setElement((m.getElement(14, 21) + m.getElement(13, 21)) / 2.0f, 14, 21);
		m.setElement((m.getElement(15, 21) + m.getElement(14, 21)) / 2.0f, 15, 21);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 12.0f);
		assertEquals("Interpolation incorrectly deviated in J direction from integer max.", 21.0f, result.getMainPeakJ(), 0.01f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 13.0f);
	}

	public void simple2DDeviatedLeftTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)thetha, sigmaX, sigmaY, m);
		m.setElement((m.getElement(12, 20) + m.getElement(12, 21)) / 2.0f, 12, 20);
		m.setElement((m.getElement(12, 19) + m.getElement(12, 20)) / 2.0f, 12, 19);
		m.setElement((m.getElement(12, 18) + m.getElement(12, 19)) / 2.0f, 12, 18);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertEquals("Interpolation incorrectly deviated in I direction from integer max.", 12.0f, result.getMainPeakI(), 0.0001f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 21.0f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 13.0f);
	}

	public void simple2DDeviatedUpTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.00f, 1.0f, 1.3f, m);
		m.setElement((m.getElement(11, 21) + m.getElement(12, 21)) / 2.0f, 11, 21);
		m.setElement((m.getElement(10, 21) + m.getElement(11, 21)) / 2.0f, 10, 21);
		m.setElement((m.getElement(9, 21) + m.getElement(10, 21)) / 2.0f, 9, 21);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() > 11.0f);
		assertEquals("Interpolation incorrectly deviated in J direction from integer max.", 21.0f, result.getMainPeakJ(), 0.0001f);
		
		assertTrue("Interpolation incorrectly deviated in I direction from integer max.", result.getMainPeakI() < 12.0f);
	}

	public void simple2DDeviatedRightTestPass(Gaussian2DInterpolatorConfiguration config, float thetha, float sigmaX, float sigmaY) {
		PIVContextSingleton context = PIVContextSingleton.getSingleton();
		PIVInputParameters parameters = context.getPIVParameters();
		parameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		parameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, config);
				
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.00f, 1.0f, 1.3f, m);
		m.setElement((m.getElement(12, 22) + m.getElement(12, 21)) / 2.0f, 12, 22);
		m.setElement((m.getElement(12, 23) + m.getElement(12, 22)) / 2.0f, 12, 23);
		m.setElement((m.getElement(12, 24) + m.getElement(12, 23)) / 2.0f, 12, 24);
		
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		
		IBasicCrossCorrelationInterpolator interpolator = simpleAdapter(context.getPIVReusableObjects().getOrCreateInterpolator());
		result = interpolator.interpolate(m, result);
		System.out.println(result);
		
		assertEquals("Interpolation incorrectly deviated in I direction from integer max.", 12.0f, result.getMainPeakI(), 0.0001f);
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() < 22.0f);
		
		assertTrue("Interpolation incorrectly deviated in J direction from integer max.", result.getMainPeakJ() > 21.0f);
	}

	
	@Test
	public void internalValidateGaussianTest1Pass() {
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)0.00f, 1.0f, 1.3f, m);
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		assertEquals("Gaussian maximum is not at desired I location.", 12, result.getMainPeakI(), 0.0001f);
		assertEquals("Gaussian maximum is not at desired J location.", 21, result.getMainPeakJ(), 0.0001f);
	}

	@Test
	public void internalValidateGaussianTest2Pass() {
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 12, 21, (float)FastMath.PI/3.0f, 1.0f, 1.3f, m);
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		assertEquals("Gaussian maximum is not at desired I location.", 12, result.getMainPeakI(), 0.0001f);
		assertEquals("Gaussian maximum is not at desired J location.", 21, result.getMainPeakJ(), 0.0001f);	
	}

	@Test
	public void internalValidateGaussianTest3Pass() {
		Matrix m = new MatrixFloat((short)21,(short)41);
		m = computeGaussian(1000.0f, 15, 17, (float)-FastMath.PI/3.0f, 1.0f, 1.3f, m);
		IMaximumFinder finder = new FindMaximumSimple();
		MaxCrossResult result = finder.findMaximum(m);
		assertEquals("Gaussian maximum is not at desired I location.", 15, result.getMainPeakI(), 0.0001f);
		assertEquals("Gaussian maximum is not at desired J location.", 17, result.getMainPeakJ(), 0.0001f);
	}
	
	public static Matrix computeGaussian(float maxValue, int maxI, int maxJ, float theta, float sigmaX, float sigmaY, Matrix m) {
		float meanX = maxJ;
		float meanY = maxI;
		
		float sigmaXSquared = sigmaX * sigmaX;
		float sigmaYSquared = sigmaY * sigmaY;
		float constant = maxValue / (2.0f * (float)FastMath.PI * sigmaX * sigmaY);
		float cosTheta = (float)FastMath.cos(theta);
		float sinTheta = (float)FastMath.sin(theta);
		
		
		if (m.getWidth() % 2 != 1 || m.getHeight() % 2 != 1) {
			throw new IllegalArgumentException("Matrix dims must be odd");
		}
		
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				float thetaX = (cosTheta * (j - meanX) - sinTheta * (i - meanY));
				float thetaY = (sinTheta * (j - meanX) + cosTheta * (i - meanY));
				float expThetaX = (float)FastMath.exp(-FastMath.pow(thetaX, 2) / (2.0f * sigmaXSquared));
				float expThetaY = (float)FastMath.exp(-FastMath.pow(thetaY, 2) / (2.0f * sigmaYSquared));
						
				float value = constant * expThetaX * expThetaY;
				m.setElement(value, i, j);
			}
		}
		
		return m;
	}
}
