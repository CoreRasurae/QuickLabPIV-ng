// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import java.util.concurrent.atomic.AtomicInteger;

import com.aparapi.Kernel;

public class FindMaximumSimpleKernel extends Kernel {
	public FindMaximumSimpleKernel(final int[] maxResult, final int dimI, final int dimJ) {
		this.maxResult = maxResult;
		this.dimI = dimI;
		this.dimJ = dimJ;
		lockAndMaxLocal = new AtomicInteger[2];
		lockAndMaxLocal[0] = new AtomicInteger(0);
		lockAndMaxLocal[1] = new AtomicInteger(0);
	}
	
	@NoCL
	public void setMatrix(final float[] inputMatrix) {
		matrix = inputMatrix;
	}
	
	@NoCL
	public void setNumberOfTiles(final int _numberOfTiles) {
		numberOfTiles = _numberOfTiles;
	}
	
	private final int dimI;
	private final int dimJ;
	private final int lockIndex = 0;
	private final int maxIndex = 1;
	
	private float matrix[];
	private final int maxResult[];
	private int numberOfTiles;
	
	@Local
	private AtomicInteger lockAndMaxLocal[];;
		
	@Override
	public void run() {
		int localJ = getLocalId(0);
		int localI = getLocalId(1);
		int jIterations = (dimJ+1) / getLocalSize(0);
		int iIterations = (dimI+1) / getLocalSize(1);

		//int startOffset = getPassId() * (dimI+1) * (dimJ+1);
		//int maxOffset = getPassId() * 4;
		
		int tileIdx = getGroupId(1) * getNumGroups(0) + getGroupId(0);
		
		int startOffset = tileIdx * (dimI+1) * (dimJ+1);
		int maxOffset = tileIdx * 4;
		
		if (tileIdx < numberOfTiles) {
			maxResult[3] = 0;
			if (localI == 0 && localJ == 0) {
				atomicSet(lockAndMaxLocal[lockIndex], 0);
				atomicSet(lockAndMaxLocal[maxIndex], 0);
			}
			localBarrier();
			
			int maxI = 0;
			int maxJ = 0;
			int val = 0;
			for (int i = localI * iIterations; i < localI * iIterations + iIterations; i++) {
				for (int j = localJ * jIterations; j < localJ * jIterations + jIterations ; j++) {
					int accessedI = i;
					int accessedJ = j;
					if (accessedI >= dimI) {
						accessedI = dimI - 1;
					}
					if (accessedJ >= dimJ) {
						accessedJ = dimJ - 1;
					}
				
					float floatVal = matrix[startOffset + accessedI * (dimJ+1) + accessedJ];
					int intVal = round(floatVal);
					val = max(intVal, val);
					if (val == intVal) {
						maxI = accessedI;
						maxJ = accessedJ;
					}
					/*if (floatVal >= 2147483647.0f) { //Integer.MAX_VALUE) {
						maxResult[3] = 0xff;
					}*/
				}
			}
			atomicMax(lockAndMaxLocal[maxIndex], val);
			localBarrier();
			
			int maxVal = atomicGet(lockAndMaxLocal[maxIndex]);
			if (maxVal == val) {
				if (atomicXchg(lockAndMaxLocal[lockIndex], 0xff) == 0) {
					maxResult[maxOffset + 0] = maxI;
					maxResult[maxOffset + 1] = maxJ;
					maxResult[maxOffset + 2] = maxVal;
					atomicSet(lockAndMaxLocal[lockIndex], 0x00);
				}
			}
		}
	}

}