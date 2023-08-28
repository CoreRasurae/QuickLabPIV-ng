// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.tests;

import com.aparapi.Kernel;
import java.util.concurrent.atomic.AtomicInteger;

public class Test2KernelA extends Kernel {
    final int size;
    final float[] a;
    final float[] b;
    float[] sum;

    AtomicInteger[] atomics = new AtomicInteger[2000000];

    public Test2KernelA(int _size, float[] _a, float[] _b) {
        size = _size;
        a = _a;
        b = _b;
        sum = new float[size];

        for (int i = 0; i < atomics.length; i++) {
            atomics[i] = new AtomicInteger(0);
        }
    }

//    public AtomicInteger[] getAtomics() {
//        return atomics;
//    }

    @Override public void run() {
        int gid = getGlobalId();
        sum[gid] = a[gid] + b[gid];

        atomicUpdate(1,0,0,atomics);
    }

    public int atomicUpdate(int index, int indexB, int indexC, AtomicInteger[] arr) {
        //Other logic could be included to avoid having to call atomicUpdate, just to update an atomic
        return atomicInc(arr[index + indexB + indexC]);    
    }
    
    public int sumUpdate(int[] arr, int index) {
        return arr[index] + 1;
    }
}