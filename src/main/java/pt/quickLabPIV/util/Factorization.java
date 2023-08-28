// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

public class Factorization {
    public static List<Integer> factorizeNumbers(int n) {
        List<Integer> result = new ArrayList<Integer>(7);
        
        while (n % 2 == 0) {
            result.add(2);
            n /= 2;
        }
        
        //Only odd numbers are possible at this stage
        int f = 3;
        while (f * f <= n) {
            if (n % f == 0) {
                result.add(f);
                n /= f;
            } else {
                f += 2;
            }
        }
        
        if (n != 1) {
            result.add(n);
        }
        
        return result;
    }
    
    /**
     * Finds a suggestion for the local sizes, based on maxWorkGroup that is a power of 2
     * @param globalX
     * @param globalY
     * @param maxWorkGroup
     * @return
     */
    public static int[] getSuggestedLocalWorkgroup2D(int globalX, int globalY, int maxWorkGroup) {
        int localX, localY;
        List<Integer> resultX = factorizeNumbers(globalX);
        List<Integer> resultY = factorizeNumbers(globalY);
        int localGroupPerDimension = (int)FastMath.sqrt(maxWorkGroup);
        if (globalX <= localGroupPerDimension) {
            localX = globalX;
            int usefulLeftOver = localGroupPerDimension / localX;
            if (usefulLeftOver > 0) {
                localGroupPerDimension *= usefulLeftOver;
            }
        } else {
            localX = 1;
            for (int index = resultX.size() - 1; index >= 0; index --) {
                int value = resultX.get(index); 
                localX *= value;
                if (localX > localGroupPerDimension) {
                    localX /= value;
                }
            }
            int usefulLeftOver = localGroupPerDimension / localX;
            if (usefulLeftOver > 0) {
                localGroupPerDimension *= usefulLeftOver;
            }
        }
        
        if (globalY <= localGroupPerDimension) {
            localY = globalY;
        } else {
            localY = 1;
            for (int index = resultY.size() - 1; index >= 0; index --) {
                int value = resultY.get(index);
                localY *= value;
                if (localY > localGroupPerDimension) {
                    localY /= value;
                }
            }
        }
        
        return new int[] {localX, localY};
    }
}
