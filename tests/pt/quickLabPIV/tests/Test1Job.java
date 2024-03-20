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

public class Test1Job {

	public Test1Job() {

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
        
        
        int count = 1;
        int[] matrixIn = new int[256*256];
        for (int j = 0; j < 256; j++) {
        	for (int i = 0; i < 256; i++) {
        		matrixIn[j * 256 + i] = count++;
        	}
        }
        
        int[] matrixOut = new int[256*256];
        
        final Test1Kernel kernel = new Test1Kernel(matrixIn, matrixOut);
        kernel.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
        
        
        final Range range = Range.create2D(chosen, 256, 256, 32, 32); //This will fill four GPU units 
        kernel.execute(range);
        
        for (int j = 0; j < 256; j++) {
        	for (int i = 0; i < 256; i++) {
        		System.out.println("i: " + i + "j: " + j + ", in: " + matrixIn[j * 256 + i] + ", out: " + matrixOut[j * 256 + i]);
        	}
        }
	}

}
