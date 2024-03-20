// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.tests;

import java.util.List;

import com.aparapi.Kernel.EXECUTION_MODE;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.Device.TYPE;
import com.aparapi.internal.opencl.OpenCLPlatform;

public class Test2Job {

	public Test2Job() {

		List<OpenCLPlatform> platforms = OpenCLPlatform.getUncachedOpenCLPlatforms();
        Device chosen=null;
        for (OpenCLPlatform platform: platforms) {
	        for (Device device : platform.getOpenCLDevices()){
	        	System.out.println("Description: " + device.getShortDescription());
	           if (device.getShortDescription().contains("NVIDIA") && device.getType() == TYPE.GPU){
	              chosen = device;
	              break;
	           }
	        }
        }
        
        System.out.println("Chosen: " + chosen.getShortDescription());
        System.out.println(chosen.getMaxWorkGroupSize());  // 32 * 32 = 1024 (MaxGroupSize)
        System.out.println(chosen.getMaxWorkItemDimensions());
        System.out.println(chosen.getMaxWorkItemSize()[0]);
        System.out.println(chosen.getMaxWorkItemSize()[1]);
        System.out.println(chosen.getMaxWorkItemSize()[2]);
        
        
        //static com.aparapi.Config.enableShowGeneratedOpenCL=true;
        
        int count = 1;
        int[] matrixIn = new int[256*256];
        for (int j = 0; j < 256; j++) {
        	for (int i = 0; i < 256; i++) {
        		matrixIn[j * 256 + i] = count++;
        	}
        }
        
        final int sideGlobal = 256;
        final int sideLocal = 32;
        
        long[] matrixOut = new long[sideGlobal/sideLocal * sideGlobal/sideLocal];
        int[] selOut = new int[sideLocal*sideLocal];
        int[] originJ = new int[sideLocal*sideLocal];
        int[] originI = new int[sideLocal*sideLocal];
        
        
        final Test2Kernel kernel = new Test2Kernel(matrixIn, matrixOut, selOut, originJ, originI);
        kernel.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
                
        final Range range = Range.create2D(chosen, sideGlobal, sideGlobal, sideLocal, sideLocal); //This will fill four GPU units
        kernel.execute(range);
        
        long[][] cpuMatrixOut = new long[sideGlobal/sideLocal][sideGlobal/sideLocal];
        for (int j = 0; j < sideGlobal; j++) {
        	for (int i = 0; i < sideGlobal; i++) {
        		cpuMatrixOut[j/sideLocal][i/sideLocal] += matrixIn[j * sideGlobal + i] + matrixIn[j * sideGlobal + i];
        	}
        }
        
        for (int j = 0; j < sideGlobal/sideLocal; j++) {
        	for (int i = 0; i < sideGlobal/sideLocal; i++) {
        		System.out.println("i: " + i + "j: " + j + ", in: " + cpuMatrixOut[j][i] + ", out: " + matrixOut[j * sideGlobal/sideLocal + i]);
        	}
        }
        
        int value = 0;
        int valueGPU = 0;
        for (int j = 0; j < sideLocal; j++) {
	        for (int i = 0; i < sideLocal; i ++) {
	        	value += matrixIn[j * sideGlobal + i] + matrixIn[j * sideGlobal + i];
	        	System.out.println("source(i=" + originI[j * sideLocal + i] + ",j=" + originJ[j * sideLocal + i] + ") - [value=" + selOut[j * sideLocal + i] + "]");
	        	valueGPU += selOut[j * sideLocal + i];
	        }
        }
        System.out.println(value + ", " + valueGPU);
	}

}
