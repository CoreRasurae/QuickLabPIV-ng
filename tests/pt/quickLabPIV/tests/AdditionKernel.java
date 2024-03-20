// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.tests;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;

public class AdditionKernel extends Kernel {

double[] result;
final int chunkSize;
final int initialArraySize;

public AdditionKernel(double[] result, int chunkSize) {
    this.chunkSize = chunkSize;
    this.result = result;
    initialArraySize = result.length;
}

@Override
public void run() {
    int numPasses = 0;
    int gap = 1;
    int workArraySize = initialArraySize;
    boolean end = false;
    for (;!end; ) {

        if(workArraySize == 1) {
            return;
        }

        int i = getGlobalId() * gap * chunkSize;

        for (int a = 1; a < chunkSize && !end; a++) {
            if (i + a * gap < workArraySize) {
                result[i] += result[i + a * gap];
            } else {
                end = true;
            }
        }

        globalBarrier();
        
        workArraySize = (int) Math.ceil((double) workArraySize / (double) chunkSize);
        gap = (int) Math.pow(chunkSize, numPasses);
    }
}

public static void main(String[] args) {
    final int chunkSize = 2;
    double result[] = new double[] {21,21,21,21,21};
    Kernel additionKernel = new AdditionKernel(result, chunkSize); 
    //Range additionRange = JavaDevice.THREAD_POOL.createRange(result.length,result.length);
    Range additionRange = Range.create(result.length,result.length);
    OpenCLDevice dev = (OpenCLDevice)KernelManager.instance().bestDevice();
    dev.setSharedMemory(false);
    additionKernel.execute(additionRange);
    System.out.println(result[0]);
}

}
