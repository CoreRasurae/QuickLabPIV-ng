package pt.quickLabPIV.iareas;

import static org.junit.Assert.*;
import org.junit.Test;

import pt.quickLabPIV.iareas.BiLinearInterpolatingFunction;

import org.junit.Before;

public class BiLinearInterpolatingFunctionTest {
    private double[] xs = new double[] {1.0, 3.0, 5.0};
    private double[] ys = new double[] {2.0, 4.0, 6.0};
    private double[][] fyx = new double[][] {{5.0, 8.0, 3.0}, {6.0, 10.0, 3.5}, {4.5, 7.5, 4.0}}; 

    private BiLinearInterpolatingFunction interp;
   
    @Before
    public void setup() {        
        interp = new BiLinearInterpolatingFunction(ys, xs, fyx);
    }
    
    @Test
    public void testInterpolatingValue1() {
        double x = 1.3;
        double y = 2.4;
        
        double weightX = 1.0 - (x - 1.0)/(3.0 - 1.0);
        double weightY = 1.0 - (y - 2.0)/(4.0 - 2.0);
        
        double testValue =      weightY    * (weightX * fyx[0][0] + (1.0 - weightX) * fyx[0][1]) +
                       (1.0 - weightY) * (weightX * fyx[1][0] + (1.0 - weightX) * fyx[1][1]);
        
        assertTrue(testValue < 6.0);
        
        double value = interp.value(y, x);
        assertEquals(testValue, value, 1e-8);
    }

    @Test
    public void testInterpolatingValue2() {
        double x = 3.3;
        double y = 4.4;
        
        double weightX = 1.0 - (x - 3.0)/(5.0 - 3.0);
        double weightY = 1.0 - (y - 4.0)/(6.0 - 4.0);
        
        double testValue =      weightY    * (weightX * fyx[1][1] + (1.0 - weightX) * fyx[1][2]) +
                       (1.0 - weightY) * (weightX * fyx[2][1] + (1.0 - weightX) * fyx[2][2]);
        
        assertTrue(testValue > 6.0);
        assertTrue(testValue < 9.0);
        
        double value = interp.value(y, x);
        assertEquals(testValue, value, 1e-8);
    }

}
