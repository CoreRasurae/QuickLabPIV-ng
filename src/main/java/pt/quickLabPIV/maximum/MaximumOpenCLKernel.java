// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.maximum;

import java.util.concurrent.atomic.AtomicInteger;

import com.aparapi.Kernel;

public abstract class MaximumOpenCLKernel extends Kernel {

	public MaximumOpenCLKernel() {
		lockAndMaxLocal = new AtomicInteger[2];
		lockAndMaxLocal[0] = new AtomicInteger(0);
		lockAndMaxLocal[1] = new AtomicInteger(0);
	}
	
	@Local
	protected AtomicInteger lockAndMaxLocal[];;

	protected final int lockIndex = 0;
	protected final int maxIndex = 1;
	
	public void computeMaximum(float[] maxs, int tileIdx, int numberOfTiles, float localMaxVal, int maxI, int maxJ) {
		final int maxOffset = tileIdx * 4;
				
		int localMax = round(localMaxVal);
		if (tileIdx < numberOfTiles) {
			if (getLocalId(0) == 0 && getLocalId(1) == 0) {
				atomicSet(lockAndMaxLocal[lockIndex], 0);
				atomicSet(lockAndMaxLocal[maxIndex], 0);
			}
			localBarrier();

			atomicMax(lockAndMaxLocal[maxIndex], localMax);
			localBarrier();
			
			int maxVal = atomicGet(lockAndMaxLocal[maxIndex]);
			if (maxVal == localMax) {
				if (atomicXchg(lockAndMaxLocal[lockIndex], 0xff) == 0) {
					maxs[maxOffset + 0] = maxI;
					maxs[maxOffset + 1] = maxJ;
					maxs[maxOffset + 2] = localMaxVal;
				}
			}
		}
	}
}
