// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

import java.util.Arrays;

public class DirectConvolution implements IConvolution1D {

    @Override
    public float[] convolve1D(float[] a, int sizeA, float[] b, int sizeB, float result[]) {
        int minSize = sizeA;
        int otherSize = sizeB;
        float[] minVec = a;
        float[] otherVec = b;
        
        //The vectors can be switched if needed, because the convolution is a commutative operation
        if (sizeB < minSize) {
            minSize = sizeB;
            otherSize = sizeA;
            minVec = b;
            otherVec = a;
        }
       
        if (result == null || result.length < otherSize) {
            result = new float[otherSize];
        } else {
            Arrays.fill(result, 0);
        }
        
        int halfSize = minSize / 2;
        
        for (int i = 0; i < sizeA + sizeB - 1; i++) {
            for (int j = 0; j < minSize; j++) {
                if (i - j >= 0 && i - j < otherSize) {
                    if (halfSize * 2 == minSize) {
                       if (i >= halfSize - 1 && i < otherSize + halfSize - 1) {
                          result[i - (halfSize - 1)] += otherVec[i - j] * minVec[j];
                       }
                    } else {
                       if (i >= halfSize && i < otherSize + halfSize) {
                          result[i - halfSize] += otherVec[i - j] * minVec[j];
                       }
                    }
                }
            }
        }
        
        return result;
    }

}
