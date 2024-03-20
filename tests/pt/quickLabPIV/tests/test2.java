// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.tests;

import com.aparapi.Range;
import java.util.concurrent.atomic.AtomicInteger;

public class test2 {
    public static void main( String[] args )
    {
        final int size = 10000000;

        final float[] a = new float[size];
        final float[] b = new float[size];

        for (int i = 0; i < size; i++) {
            a[i] = (float) (Math.random() * 100);
            b[i] = (float) (Math.random() * 100);
        }

        final float[] sum = new float[size];

        Test2KernelA kernel = new Test2KernelA(size, a, b);
        Range range = Range.create(size);

        kernel.setExplicit(true);
        kernel.put(a);
        kernel.put(b);

        for (int i = 0; i < 10; i++) {
            long t1 = System.currentTimeMillis();
            kernel.execute(range);
            long t2 = System.currentTimeMillis();

            System.out.println(t2-t1 + " : " + kernel.getExecutionTime());
        }

//        kernel.get(sum);
        //AtomicInteger[] counters = kernel.getAtomics();

        //System.out.println("Counter = " + String.valueOf(counters[0]));

        kernel.dispose();

    }
}