// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.maximum.FindMaximumSimpleOpenCL;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class FindMaximumSimpleOpenCLTest {

	//@Test - Implementation if currently buggy, and is currently not used by the software application 
	public void testKernelSimple1x1() {
		float[] values = { 100 };
		Matrix m = new MatrixFloat(values, 1, 1);
		IMaximumFinder finder = new FindMaximumSimpleOpenCL();
		MaxCrossResult result = finder.findMaximum(m);
		assertEquals("Result max value doesn't match the expected", 100, result.getMainPeakValue(), 1e-7f);
		assertEquals("Result maxI doesn't match the expected", 0, result.getMainPeakI(), 1e-7f);
		assertEquals("Result maxJ doesn't match the expected", 0, result.getMainPeakJ(), 1e-7f);
		
		
		System.out.println(result.getMainPeakValue());
	}
	
	//@Test - Implementation if currently buggy, and is currently not used by the software application 
	public void testKernelSimple3x3() {
		float[] values = { 100, 80,  50,
				            90, 130, 150,
				            10, 40,  80   };
		Matrix m = new MatrixFloat(values, 3, 3);
		IMaximumFinder finder = new FindMaximumSimpleOpenCL();
		MaxCrossResult result = finder.findMaximum(m);
		assertEquals("Result max value doesn't match the expected", 150, result.getMainPeakValue(), 1e-7f);
		assertEquals("Result maxI doesn't match the expected", 1, result.getMainPeakI(), 1e-7f);
		assertEquals("Result maxJ doesn't match the expected", 2, result.getMainPeakJ(), 1e-7f);
		
		
		System.out.println(result.getMainPeakValue());
	}
}
