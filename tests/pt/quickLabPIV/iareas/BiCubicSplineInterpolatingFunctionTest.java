package pt.quickLabPIV.iareas;

import static org.junit.Assert.*;
import org.junit.Test;

import pt.quickLabPIV.iareas.BiCubicSplineInterpolatorWithBiLinearBackup;

import org.junit.Before;

public class BiCubicSplineInterpolatingFunctionTest {
    private double[] xs = new double[] {1.0, 3.0, 5.0, 6.0};
    private double[] ys = new double[] {2.0, 4.0, 6.0, 7.0};
    private double[][] uyx = new double[][] {{5.0, 8.0, 3.0, 1.5}, {6.0, 10.0, 3.5, 3.0}, {4.5, 7.5, 4.0, 3.0}, {4.5, 7.5, 4.0, 3.0} };

    private BiCubicSplineInterpolatorWithBiLinearBackup interp;
   
    @Before
    public void setup() {        
        interp = new BiCubicSplineInterpolatorWithBiLinearBackup(ys, xs, uyx, uyx, 2.0f, 7.0f, 1.0f, 6.0f);
    }
    
    @Test
    public void testInterpolatingValue1() {
        double x = 1.3;
        double y = 2.4;
        
        double weightX = 1.0 - (x - 1.0)/(3.0 - 1.0);
        double weightY = 1.0 - (y - 2.0)/(4.0 - 2.0);
        
        double testValue =      weightY    * (weightX * uyx[0][0] + (1.0 - weightX) * uyx[0][1]) +
                       (1.0 - weightY) * (weightX * uyx[1][0] + (1.0 - weightX) * uyx[1][1]);
        
        assertTrue(testValue < 6.0);
        
        double value = interp.interpolateLocationForU(y, x);
        assertEquals(testValue, value, 5e-1);
    }

    @Test
    public void testInterpolatingValue2() {
        double x = 3.3;
        double y = 4.4;
        
        double weightX = 1.0 - (x - 3.0)/(5.0 - 3.0);
        double weightY = 1.0 - (y - 4.0)/(6.0 - 4.0);
        
        double testValue =      weightY    * (weightX * uyx[1][1] + (1.0 - weightX) * uyx[1][2]) +
                       (1.0 - weightY) * (weightX * uyx[2][1] + (1.0 - weightX) * uyx[2][2]);
        
        assertTrue(testValue > 6.0);
        assertTrue(testValue < 9.0);
        
        double value = interp.interpolateLocationForU(y, x);
        assertEquals(testValue, value, 5e-1);
    }

}
