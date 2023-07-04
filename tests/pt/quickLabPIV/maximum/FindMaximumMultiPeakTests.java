package pt.quickLabPIV.maximum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.maximum.FindMaximumMultiPeaks;
import pt.quickLabPIV.maximum.FindMaximumMultiPeaksConfiguration;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class FindMaximumMultiPeakTests {

    @Test
    public void testMaximumSimple5x5MultipleMatrixFixedResult() {
        PIVContextTestsSingleton.setSingleton();
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        FindMaximumMultiPeaksConfiguration config = new FindMaximumMultiPeaksConfiguration(4, 3);
        singleton.getPIVParameters().setSpecificConfiguration(FindMaximumMultiPeaksConfiguration.IDENTIFIER, config);
        
        Matrix m = new MatrixFloat(10,10);
        m.setElement(12.0f, 0, 0);
        m.setElement(12.0f, 0, 1);
        m.setElement(12.0f, 0, 2);
        m.setElement(12.0f, 0, 3);
        m.setElement(12.0f, 0, 4);
        m.setElement(12.0f, 0, 5);
        m.setElement(12.0f, 0, 6);
        m.setElement(12.0f, 0, 7);
        m.setElement(12.0f, 0, 8);
        m.setElement(12.0f, 0, 9);
        //--------------------------
        m.setElement(12.0f, 1, 0);
        m.setElement(12.0f, 1, 1);
        m.setElement(12.0f, 1, 2);
        m.setElement(12.0f, 1, 3);
        m.setElement(12.0f, 1, 4);
        m.setElement(12.0f, 1, 5);
        m.setElement(12.0f, 1, 6);
        m.setElement(12.0f, 1, 7);
        m.setElement(12.0f, 1, 8);
        m.setElement(12.0f, 1, 9);
        //--------------------------
        m.setElement(12.0f, 2, 0);
        m.setElement(12.0f, 2, 1);
        m.setElement(12.0f, 2, 2);
        m.setElement(12.0f, 2, 3);
        m.setElement(12.0f, 2, 4);
        m.setElement(12.0f, 2, 5);
        m.setElement(12.0f, 2, 6);
        m.setElement(12.0f, 2, 7);
        m.setElement(12.0f, 2, 8);
        m.setElement(12.0f, 2, 9);
        //--------------------------
        m.setElement(12.0f, 3, 0);
        m.setElement(12.0f, 3, 1);
        m.setElement(12.0f, 3, 2);
        m.setElement(12.0f, 3, 3);
        m.setElement(12.0f, 3, 4);
        m.setElement(12.0f, 3, 5);
        m.setElement(15.0f, 3, 6);
        m.setElement(12.0f, 3, 7);
        m.setElement(12.0f, 3, 8);
        m.setElement(12.0f, 3, 9);
        //--------------------------
        m.setElement(12.0f, 4, 0);
        m.setElement(12.0f, 4, 1);
        m.setElement(12.0f, 4, 2);
        m.setElement(12.0f, 4, 3);
        m.setElement(12.0f, 4, 4);
        m.setElement(16.0f, 4, 5);
        m.setElement(12.0f, 4, 6);
        m.setElement(12.0f, 4, 7);
        m.setElement(12.0f, 4, 8);
        m.setElement(12.0f, 4, 9);
        //--------------------------
        m.setElement(12.0f, 5, 0);
        m.setElement(12.0f, 5, 1);
        m.setElement(12.0f, 5, 2);
        m.setElement(12.0f, 5, 3);
        m.setElement(12.0f, 5, 4);
        m.setElement(12.0f, 5, 5);
        m.setElement(12.0f, 5, 6);
        m.setElement(12.0f, 5, 7);
        m.setElement(12.0f, 5, 8);
        m.setElement(12.0f, 5, 9);        
        //--------------------------
        m.setElement(12.0f, 6, 0);
        m.setElement(12.0f, 6, 1);
        m.setElement(12.0f, 6, 2);
        m.setElement(12.0f, 6, 3);
        m.setElement(12.0f, 6, 4);
        m.setElement(12.0f, 6, 5);
        m.setElement(12.0f, 6, 6);
        m.setElement(12.0f, 6, 7);
        m.setElement(12.0f, 6, 8);
        m.setElement(12.0f, 6, 9);        
        //--------------------------
        m.setElement(12.0f, 7, 0);
        m.setElement(12.0f, 7, 1);
        m.setElement(12.0f, 7, 2);
        m.setElement(12.0f, 7, 3);
        m.setElement(12.0f, 7, 4);
        m.setElement(12.0f, 7, 5);
        m.setElement(12.0f, 7, 6);
        m.setElement(12.0f, 7, 7);
        m.setElement(12.0f, 7, 8);
        m.setElement(12.0f, 7, 9);        
        //--------------------------
        m.setElement(12.0f, 8, 0);
        m.setElement(12.0f, 8, 1);
        m.setElement(12.0f, 8, 2);
        m.setElement(12.0f, 8, 3);
        m.setElement(12.0f, 8, 4);
        m.setElement(12.0f, 8, 5);
        m.setElement(12.0f, 8, 6);
        m.setElement(12.0f, 8, 7);
        m.setElement(12.0f, 8, 8);
        m.setElement(12.0f, 8, 9);        
        //--------------------------
        m.setElement(12.0f, 9, 0);
        m.setElement(12.0f, 9, 1);
        m.setElement(12.0f, 9, 2);
        m.setElement(12.0f, 9, 3);
        m.setElement(13.0f, 9, 4);
        m.setElement(12.0f, 9, 5);
        m.setElement(12.0f, 9, 6);
        m.setElement(12.0f, 9, 7);
        m.setElement(12.0f, 9, 8);
        m.setElement(12.0f, 9, 9);
        
        IMaximumFinder fMax = new FindMaximumMultiPeaks();
        MaxCrossResult maxCross = fMax.findMaximum(m);
        
        assertEquals("Max cross value is not correct", 16.0f, maxCross.getMainPeakValue(), 1e-6f);
        assertEquals("Max cross value is not at correct I location", 4, maxCross.getMainPeakI(), 1e-6f);
        assertEquals("Max cross value is not at correct J location", 5, maxCross.getMainPeakJ(), 1e-6f);

        assertEquals("Max cross value is not correct", 16.0f, maxCross.getNthPeakValue(0), 1e-6f);
        assertEquals("Max cross value is not at correct I location", 4, maxCross.getNthPeakI(0), 1e-6f);
        assertEquals("Max cross value is not at correct J location", 5, maxCross.getNthPeakJ(0), 1e-6f);
        
        assertEquals("Max cross value is not correct", 13.0f, maxCross.getNthPeakValue(1), 1e-6f);
        assertEquals("Max cross value is not at correct I location", 9, maxCross.getNthPeakI(1), 1e-6f);
        assertEquals("Max cross value is not at correct J location", 4, maxCross.getNthPeakJ(1), 1e-6f);

        assertEquals("Max cross value is not correct", 12.0f, maxCross.getNthPeakValue(2), 1e-6f);
    }

}
