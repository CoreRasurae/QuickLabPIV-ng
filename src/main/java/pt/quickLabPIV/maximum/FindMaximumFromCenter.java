// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import java.util.Iterator;
import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class FindMaximumFromCenter implements IMaximumFinder {
	private float maximum;
	private MaxCrossResult test = new MaxCrossResult();
	private MaxCrossResult maxResult = new MaxCrossResult();
		
	public FindMaximumFromCenter() {
	}
	
	private void reset() {
		maximum = 0;
		maxResult.reset();
	}
	
	private MaxCrossResult getUnusedMaxResult() {
		return test;
	}
	
	private void updateMaxResults(MaxCrossResult newMaximum, short centerI, short centerJ) {
		if (newMaximum.getMainPeakValue() > maximum) {
			maxResult.importOtherMax(newMaximum);
			maximum = newMaximum.getMainPeakValue();
		} else if (newMaximum.getMainPeakValue() == maximum) {
			float distanceMax = maxResult.computeDistance(centerI, centerJ);
			float distanceNew = newMaximum.computeDistance(centerI, centerJ);
			if (distanceNew < distanceMax) {
				maxResult.importOtherMax(newMaximum);
			}
		}
	}
	
	@Override
	public MaxCrossResult findMaximum(Matrix m) { 
		int dimI = m.getHeight();
		int dimJ = m.getWidth();
		
		reset();
	
		short centerI = (short)(dimI/2 + 1);
		short centerJ = (short)(dimJ/2 + 1);
		
		float value = m.getElement((short)(dimI/2 + 1), (short)(dimJ/2 + 1));
		MaxCrossResult newMax = getUnusedMaxResult();
		newMax.setMainPeakI((short)(dimI/2 + 1));
		newMax.setMainPeakJ((short)(dimJ/2+1));
		newMax.setMainPeakValue(value);
		
		updateMaxResults(newMax, centerI, centerJ);

		float minValue = Float.MAX_VALUE;
		
		for (short i = 0; i < dimI/2; i++) {
			float value1 = m.getElement((short)(dimI/2 - i), (short)(dimJ/2+1));
			float value2 = m.getElement((short)(dimI/2 + 1 + i), (short)(dimJ/2+1));
			if (value1 < minValue) {
			    minValue = value1;
			}
            if (value2 < minValue) {
                minValue = value2;
            }

			if (value1 >= maximum) {
				newMax = getUnusedMaxResult();
				newMax.setMainPeakI((short)(dimI/2 - i));
				newMax.setMainPeakJ((short)(dimJ/2+1));
				newMax.setMainPeakValue(value1);
				
				updateMaxResults(newMax, centerI, centerJ);
			}
			if (value2 >= maximum) {
				newMax = getUnusedMaxResult();
				newMax.setMainPeakI((short)(dimI/2 + 1 + i));
				newMax.setMainPeakJ((short)(dimJ/2+1));
				newMax.setMainPeakValue(value2);
				
				updateMaxResults(newMax, centerI, centerJ);
			}
		}
		
		for (short j = 0; j < dimJ/2; j++) {
			float value1 = m.getElement((short)(dimI/2 + 1), (short)(dimJ/2 - j));
			float value2 = m.getElement((short)(dimI/2 + 1), (short)(dimJ/2 + 1 + j));
            if (value1 < minValue) {
                minValue = value1;
            }
            if (value2 < minValue) {
                minValue = value2;
            }

			if (value1 >= maximum) {
				newMax = getUnusedMaxResult();
				newMax.setMainPeakI((short)(dimI/2+1));
				newMax.setMainPeakJ((short)(dimJ/2 - j));
				newMax.setMainPeakValue(value1);
				
				updateMaxResults(newMax, centerI, centerJ);
			}
			if (value2 >= maximum) {
				newMax = getUnusedMaxResult();
				newMax.setMainPeakI((short)(dimI/2+1));
				newMax.setMainPeakJ((short)(dimJ/2 + 1 + j));
				newMax.setMainPeakValue(value2);
				
				updateMaxResults(newMax, centerI, centerJ);
			}
		}
		
		for (int i = 0; i < dimI/2; i++) {
			for (int j = 0; j < dimJ/2; j++) {
				float value1 = m.getElement((short)(dimI/2 - i), (short)(dimJ/2 - j));
				float value2 = m.getElement((short)(dimI/2 + 1 + i), (short)(dimJ/2 - j));
				float value3 = m.getElement((short)(dimI/2 - i), (short)(dimJ/2 + 1 + j));
				float value4 = m.getElement((short)(dimI/2 + 1 + i), (short)(dimJ/2 + 1 + j));
	            if (value1 < minValue) {
	                minValue = value1;
	            }
	            if (value2 < minValue) {
	                minValue = value2;
	            }
	            if (value3 < minValue) {
	                minValue = value3;
	            }
	            if (value4 < minValue) {
	                minValue = value4;
	            }

				if (value1 >= maximum) {
					newMax = getUnusedMaxResult();
					newMax.setMainPeakI((short)(dimI/2 - i));
					newMax.setMainPeakJ((short)(dimJ/2 - j));
					newMax.setMainPeakValue(value1);
					
					updateMaxResults(newMax, centerI, centerJ);
				}
				if (value2 >= maximum) {
					newMax = getUnusedMaxResult();
					newMax.setMainPeakI((short)(dimI/2 + 1 + i));
					newMax.setMainPeakJ((short)(dimJ/2 - j));
					newMax.setMainPeakValue(value2);
					
					updateMaxResults(newMax, centerI, centerJ);
				}
				if (value3 >= maximum) {
					newMax = getUnusedMaxResult();
					newMax.setMainPeakI((short)(dimI/2 - i));
					newMax.setMainPeakJ((short)(dimJ/2 + 1 + j));
					newMax.setMainPeakValue(value3);
					
					updateMaxResults(newMax, centerI, centerJ);
				}				
				if (value4 >= maximum) {
					newMax = getUnusedMaxResult();
					newMax.setMainPeakI((short)(dimI/2 + 1 + i));
					newMax.setMainPeakJ((short)(dimJ/2 + 1 + j));
					newMax.setMainPeakValue(value4);
					
					updateMaxResults(newMax, centerI, centerJ);
				}				
			}
		}
		
		MaxCrossResult finalResult = new MaxCrossResult();
		finalResult.setCrossMatrix(m);
		finalResult.setMainPeakI(maxResult.getMainPeakI());
		finalResult.setMainPeakJ(maxResult.getMainPeakJ());
		finalResult.setMainPeakValue(maxResult.getMainPeakValue());
		finalResult.setMinFloor(minValue);
		return finalResult;
	}

	@Override
	public void dispose() {
		//Intentionally empty
	}

	@Override
	public List<MaxCrossResult> findAllPeaks(List<XCorrelationResults> xCorrResults, Iterator<Tile> tileAIterator,
			Iterator<Tile> tileBIterator) {
		// TODO Auto-generated method stub
		return null;
	}

}
