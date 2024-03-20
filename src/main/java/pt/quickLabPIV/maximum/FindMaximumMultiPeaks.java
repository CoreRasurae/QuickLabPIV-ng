// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.maximum;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.util.Entry;
import pt.quickLabPIV.util.SimpleFixedLengthFloatLinkedList;

public class FindMaximumMultiPeaks implements IMaximumFinder {
    private final static Logger logger = LoggerFactory.getLogger(FindMaximumMultiPeaks.class);
            
    private final short kernelWidth;
    private final short numberOfPeaks;
	private final SimpleFixedLengthFloatLinkedList maxPeaks;
	
	public FindMaximumMultiPeaks() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(FindMaximumMultiPeaksConfiguration.IDENTIFIER);
		if (configurationObject == null) {
			throw new MaximumFinderException("Couldn't retrieve MultiPeaks maximum finder configuration");
		}
		FindMaximumMultiPeaksConfiguration configuration = (FindMaximumMultiPeaksConfiguration)configurationObject;
		kernelWidth = (short)configuration.getKernelWidth();
		numberOfPeaks = (short)configuration.getNumberOfPeaks();
		maxPeaks = new SimpleFixedLengthFloatLinkedList(numberOfPeaks, kernelWidth);
	}
		
	@Override
	public MaxCrossResult findMaximum(Matrix m) { 
		final short dimI = m.getHeight();
		final short dimJ = m.getWidth();
				
		short iSlices = (short)(dimI / kernelWidth);
		if (iSlices * kernelWidth != dimI) {
		    iSlices += 1;
		}
		final short iSlicesBottom = (short)(iSlices / 2);
		final short iSlicesTop = (short)(iSlices - iSlicesBottom);
		
		short jSlices = (short)(dimJ / kernelWidth);
		if (jSlices * kernelWidth != dimJ) {
		    jSlices += 1;
		}
		final short jSlicesRight = (short)(jSlices / 2);
        final short jSlicesLeft = (short)(jSlices - jSlicesRight);
		
        maxPeaks.clear();
        
        float maxs[] = new float[4];
        short maxsI[] = new short[4];
        short maxsJ[] = new short[4];
        float min = Float.MAX_VALUE;
		for (short sliceIdxI = 0; sliceIdxI < iSlicesTop; sliceIdxI++) {
		    for (short sliceIdxJ = 0; sliceIdxJ < jSlicesLeft; sliceIdxJ++) {
		        final short idxTopI = (short)(sliceIdxI * kernelWidth);
		        final short idxLeftJ = (short)(sliceIdxJ * kernelWidth);
		        final short idxBottomI = (short)(sliceIdxI * kernelWidth + iSlicesTop * kernelWidth);
		        final short idxRightJ = (short)(sliceIdxJ * kernelWidth + jSlicesLeft * kernelWidth);
		        
		        for (int index = 0; index < maxs.length; index++) {
		        	maxs[index] = 0.0f;
		        }
		        
		        for (short offsetI = 0; offsetI < kernelWidth; offsetI++) {
		            for (short offsetJ = 0; offsetJ < kernelWidth; offsetJ++) {
		                 short posI1 = (short)(idxTopI + offsetI);
		                 short posJ1 = (short)(idxLeftJ + offsetJ);
		                 float value1 = m.getElement(posI1, posJ1);
		                 if (value1 > maxs[0]) {
		                     maxs[0] = value1;
		                     maxsI[0] = posI1;
		                     maxsJ[0] = posJ1;
		                 }
		                 if (value1 < min) {
		                     min = value1;
		                 }
		                 
                         short posI2 = (short)(idxTopI + offsetI);
                         short posJ2 = (short)(idxRightJ + offsetJ);
		                 if (posJ2 < dimJ) {
		                     float value2 = m.getElement(posI2, posJ2);
		                     if (value2 > maxs[1]) {
	                             maxs[1] = value2;
	                             maxsI[1] = posI2;
	                             maxsJ[1] = posJ2;
		                     }
		                     if (value2 < min) {
		                         min = value2;
		                     }
		                 }
		                 
		                 short posI3 = (short)(idxBottomI + offsetI);
                         short posJ3 = (short)(idxLeftJ + offsetJ);
		                 if (posI3 < dimI) {
		                     float value3 = m.getElement(posI3, posJ3);
		                     if (value3 > maxs[2]) {
	                             maxs[2] = value3;
	                             maxsI[2] = posI3;
	                             maxsJ[2] = posJ3;
		                     }
		                     if (value3 < min) {
		                         min = value3;
		                     }
		                 }

	                     short posI4 = (short)(idxBottomI + offsetI);
	                     short posJ4 = (short)(idxRightJ + offsetJ);
		                 if (posI4 < dimI && posJ4 < dimJ) {
		                     float value4 = m.getElement(posI4, posJ4);
		                     if (value4 > maxs[3]) {
	                             maxs[3] = value4;
	                             maxsI[3] = posI4;
	                             maxsJ[3] = posJ4;
		                     }
                             if (value4 < min) {
                                 min = value4;
                             }
		                 }
		            }
		        }
		   
		        for (int index = 0; index < 4; index++) {
		            maxPeaks.addInAscendOrder(maxs[index], maxsI[index], maxsJ[index]);
		        }
		    }
		}
		
        if (maxPeaks.size() != numberOfPeaks) {
            //FIXME See why this exception is thrown sometimes
            //throw new MaximumFinderException("Inconsistency in number of peaks found vs requested peaks amount");
            logger.warn("Expected {} peaks, but only found {} peaks", numberOfPeaks, maxPeaks.size());
        }
        
		MaxCrossResult result = new MaxCrossResult();
		result.setCrossMatrix(m);
		int peaksFound = maxPeaks.size();
		Iterator<Entry> maxPeaksIterator = maxPeaks.iterator();
		int index = peaksFound - 1;
		while (maxPeaksIterator.hasNext()) {
			Entry e = maxPeaksIterator.next();
		    result.setNthPeakI(index, e.getI());
		    result.setNthPeakJ(index, e.getJ());
		    result.setNthPeakValue(index, e.getValue());
		    index--;
		}		
		
		result.setMinFloor(min);
		return result;
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
