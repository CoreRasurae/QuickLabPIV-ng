// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.maximum;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.exporter.IAdditionalStructExporter;
import pt.quickLabPIV.iareas.Tile;

final public class MaxCrossResult {
    private final Logger logger = LoggerFactory.getLogger(MaxCrossResult.class); 
    
	public Tile tileA;
	public Tile tileB;
	private Matrix crossMatrix;
	private int dimCrossI;
	private int dimCrossJ;
	private float i;
	private float j;
	private float value;
	private boolean displacementsSet = false;
	private float u;
	private float v;
	private boolean absoluteVector;
	private float minFloorValue;

	private boolean peaksLocked = false;
	private float secondaryIs[];
	private float secondaryJs[];
	private float secondaryValues[];
	private float secondaryUs[];
	private float secondaryVs[];
	private boolean secondaryAbsoluteVectors[];
	private boolean secondaryDisplacementsSet[];
	
	public MaxCrossResult() {
		
	}
	
	public float computeDistance(short centerI, short centerJ) {
		float result = (float)Math.sqrt((getMainPeakI() - centerI)*(getMainPeakI() - centerI)+(getMainPeakJ() - centerJ)*(getMainPeakJ() - centerJ));
		return result;
	}

	public void setCrossMatrix(Matrix _crossMatrix) {
	    dimCrossI = _crossMatrix.getHeight();
	    dimCrossJ = _crossMatrix.getWidth();
	    crossMatrix = _crossMatrix;
	}
	
	public void setCrossDims(int _dimCrossI, int _dimCrossJ) {
	    dimCrossI = _dimCrossI;
	    dimCrossJ = _dimCrossJ;
	}
	
	/**
	 * Reserve space for the desired total number of peaks.
	 * @param peaks the total number of peaks to support (including main peak).
	 * @param lock <ul><li>true, disallow dynamic array resize</li>
	 *                 <li>false, otherwise</li></ul>
	 */
	public void setTotalPeaks(int peaks, boolean lock) {
	    peaksLocked = lock;
	    displacementsSet = false;
	    if (peaks < 1) {
	        throw new MaximumFinderException("Invalid number of peaks specified for reservation: " + peaks);
	    }
	    if (peaks == 1) {
	        secondaryIs = null;
	        secondaryJs = null;
	        secondaryValues = null;
	        secondaryUs = null;
	        secondaryVs = null;
	        secondaryAbsoluteVectors = null;
	        secondaryDisplacementsSet = null;
	        return;
	    }
	    
	    if (secondaryIs == null || secondaryJs == null || secondaryValues == null) {
	        secondaryIs = new float[peaks-1];
	        secondaryJs = new float[peaks-1];
	        secondaryValues = new float[peaks-1];
	        secondaryUs = new float[peaks-1];
	        secondaryVs = new float[peaks-1];
	        secondaryAbsoluteVectors = new boolean[peaks-1];
	        secondaryDisplacementsSet = new boolean[peaks-1];
	        return;
	    }
	    
	    if (secondaryIs.length != peaks - 1 || secondaryJs.length != peaks - 1 || secondaryValues.length != peaks - 1) {
	        secondaryIs = Arrays.copyOf(secondaryIs, peaks - 1);
	        secondaryJs = Arrays.copyOf(secondaryJs, peaks - 1);
	        secondaryValues = Arrays.copyOf(secondaryValues, peaks - 1);
	        secondaryUs = Arrays.copyOf(secondaryUs, peaks - 1);
	        secondaryVs = Arrays.copyOf(secondaryVs, peaks - 1);
	        secondaryAbsoluteVectors = Arrays.copyOf(secondaryAbsoluteVectors, peaks - 1);
	        secondaryDisplacementsSet = Arrays.copyOf(secondaryDisplacementsSet, peaks - 1);
	    }
	}
	
	public int getTotalPeaks() {
	    if (secondaryIs == null) {
	        return 1;
	    }
	    
	    return secondaryIs.length + 1;
	}
	
	public float getMainPeakI() {
        return i;
    }

    public void setMainPeakI(float i) {
        this.i = i;
    }

    public float getMainPeakJ() {
        return j;
    }

    public void setMainPeakJ(float j) {
        this.j = j;
    }

    public float getMainPeakValue() {
        return value;
    }

    public void setMainPeakValue(float value) {
        this.value = value;
    }
    
    public float getDisplacementFromNthPeakU(int peakIndex) {
        float peakI = getNthPeakI(peakIndex);
        return peakI - (int)(dimCrossI / 2);
    }

    public float getDisplacementFromNthPeakV(int peakIndex) {
        float peakJ = getNthPeakJ(peakIndex);
        return peakJ - (int)(dimCrossJ / 2);
    }

    public void setNthPeakI(int index, float _i) {
        if (index == 0) {
            i = _i;
            return;
        }
        
        if (secondaryIs == null || index > secondaryIs.length) {
            resizePeaksTo(index + 1);
        }
        secondaryIs[index-1] = _i;
    }
    
    public float getNthPeakI(int index) {
        if (index == 0) {
            return i;
        }
        
        return secondaryIs[index-1]; 
    }

    public void setNthPeakJ(int index, float _j) {
        if (index == 0) {
            j = _j;
            return;
        }
        
        if (secondaryJs == null || index > secondaryJs.length) {
            resizePeaksTo(index + 1);
        }
        secondaryJs[index-1] = _j;
    }

    public float getNthPeakJ(int index) {
        if (index == 0) {
            return j;
        }
        
        return secondaryJs[index-1]; 
    }

    public void setNthPeakValue(int index, float _value) {
        if (index == 0) {
            value = _value;
            return;
        }
        
        if (secondaryValues == null || index > secondaryValues.length) {
            resizePeaksTo(index + 1);
        }
        secondaryValues[index-1] = _value;
    }

    public float getNthPeakValue(int index) {
        if (index == 0) {
            return value;
        }
        
        return secondaryValues[index-1]; 
    }
    
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		
		sb.append("Max is at: (I=");
		sb.append(getMainPeakI());
		sb.append(",J=");
		sb.append(getMainPeakJ());
		sb.append(") with value: ");
		sb.append(getMainPeakValue());
		if (displacementsSet) {
		    sb.append(", having u=");
		    sb.append(u);
		    sb.append(", v=");
		    sb.append(v);
		}
		
		return sb.toString();
	}

	public void setAssociatedTileA(Tile tileA) {
		this.tileA = tileA;		
	}

	public void setAssociatedTileB(Tile tileB) {
		this.tileB = tileB;		
	}
	
	public void dumpMaxCrossResultToExistingExporter(IAdditionalStructExporter exporter, String tileStructPathName) {
		exporter.createStruct(tileStructPathName);
		exporter.addStructField(tileStructPathName, "maxI", getMainPeakI());
		exporter.addStructField(tileStructPathName, "maxJ", getMainPeakJ());
		exporter.addStructField(tileStructPathName, "maxVal", getMainPeakValue());
		exporter.addStructField(tileStructPathName, "u", u);
		exporter.addStructField(tileStructPathName, "v", v);
		exporter.addStructField(tileStructPathName, "absoluteVector", absoluteVector ? 1 : 0);
		exporter.addStructField(tileStructPathName, "crossMatrix", crossMatrix);
	}

	public void importOtherMax(MaxCrossResult other) {
		tileA = other.tileA;
		tileB = other.tileB;
		crossMatrix = other.crossMatrix;
		i = other.i;
		j = other.j;
		value = other.value;
		absoluteVector = other.absoluteVector;
		u = other.u;
		v = other.v;
		if (other.secondaryIs != null) {
		    secondaryIs = Arrays.copyOf(other.secondaryIs, other.secondaryIs.length);
		}
		if (other.secondaryJs != null) {
		    secondaryJs = Arrays.copyOf(other.secondaryJs, other.secondaryJs.length);
		}
		if (other.secondaryValues != null) {
		    secondaryValues = Arrays.copyOf(other.secondaryValues, other.secondaryValues.length);;
		}
		if (other.secondaryUs != null) {
		    secondaryUs = Arrays.copyOf(other.secondaryUs, other.secondaryUs.length);
		}
		if (other.secondaryVs != null) {
		    secondaryVs = Arrays.copyOf(other.secondaryVs, other.secondaryVs.length);
		}
		if (other.secondaryAbsoluteVectors != null) {
		    secondaryAbsoluteVectors = Arrays.copyOf(other.secondaryAbsoluteVectors, other.secondaryAbsoluteVectors.length);
		}
		if (other.secondaryDisplacementsSet != null) {
		    secondaryDisplacementsSet = Arrays.copyOf(other.secondaryDisplacementsSet,other.secondaryDisplacementsSet.length);
		}
		
	}

    public void setMinFloor(float _minFloorValue) {
        minFloorValue = _minFloorValue;
    }
    
    public float getMinFloor() {
        return minFloorValue;
    }
    
    public Tile getTileA() {
        return tileA;
    }
    
    public Tile getTileB() {
        return tileB;
    }

    public void setNthRelativeDisplacementFromVelocities(int peakIndex, float u, float v) {
        setNthRelativeDisplacementFromPeak(peakIndex, u + dimCrossI/2, v + dimCrossJ/2);
    }
    
    public void setNthRelativeDisplacementFromPeak(int peakIndex, float newMaxI, float newMaxJ) {
        if (peakIndex == 0) {
            i = newMaxI;
            j = newMaxJ;
            u = newMaxI - dimCrossI / 2;
            v = newMaxJ - dimCrossJ / 2;
            absoluteVector = false;
            displacementsSet = true;
        } else {
            if (secondaryIs == null || peakIndex - 1 >= secondaryIs.length ||
                secondaryUs == null || peakIndex - 1 >= secondaryUs.length) {
                resizePeaksTo(peakIndex + 1);
            }

            secondaryIs[peakIndex - 1] = newMaxI;
            secondaryJs[peakIndex - 1] = newMaxJ;
            secondaryUs[peakIndex - 1] = newMaxI - dimCrossI / 2;
            secondaryVs[peakIndex - 1] = newMaxJ - dimCrossJ / 2;
            secondaryAbsoluteVectors[peakIndex - 1] = false;
            secondaryDisplacementsSet[peakIndex - 1] = true;
        }
        
    }

    private void resizePeaksTo(int peaks) {
        if (peaks == 1) {
            return;
        }
        
        peaks--;
        if (secondaryValues != null && secondaryValues.length < peaks) {
            secondaryValues = Arrays.copyOf(secondaryValues, peaks);;
        } else if (secondaryValues == null) {
            secondaryValues = new float[peaks];
        }

        if (secondaryIs != null && secondaryIs.length < peaks) {
            secondaryIs = Arrays.copyOf(secondaryIs, peaks);
        } else if (secondaryIs == null) {
            secondaryIs = new float[peaks];
        }
        
        if (secondaryJs != null && secondaryJs.length < peaks) {
            secondaryJs = Arrays.copyOf(secondaryJs, peaks);
        } else if (secondaryJs == null) {
            secondaryJs = new float[peaks];
        }
        
        if (secondaryUs != null && secondaryUs.length < peaks) {
            secondaryUs = Arrays.copyOf(secondaryUs, peaks);
        } else if (secondaryUs == null) {
            secondaryUs = new float[peaks];
        }
        
        if (secondaryVs != null && secondaryVs.length < peaks) {
            secondaryVs = Arrays.copyOf(secondaryVs, peaks);
        } else if (secondaryVs == null) {
            secondaryVs = new float[peaks];
        }
        
        if (secondaryAbsoluteVectors != null && secondaryAbsoluteVectors.length < peaks) {
            secondaryAbsoluteVectors = Arrays.copyOf(secondaryAbsoluteVectors, peaks);
        } else if (secondaryAbsoluteVectors == null) {
            secondaryAbsoluteVectors = new boolean[peaks];
        }
        
        if (secondaryDisplacementsSet != null && secondaryDisplacementsSet.length < peaks) {
            secondaryDisplacementsSet = Arrays.copyOf(secondaryDisplacementsSet, peaks);
        } else if (secondaryDisplacementsSet == null) {
            secondaryDisplacementsSet = new boolean[peaks];
        }
    }
    
    public void setNthAbsoluteDisplacement(int peakIndex, float newU, float newV) {
        if (peakIndex == 0) {
            u = newU;
            v = newV;
            absoluteVector = true;
            displacementsSet = true;
        } else { 
            if (secondaryIs == null || peakIndex - 1 >= secondaryIs.length ||
                secondaryUs == null || peakIndex - 1 >= secondaryUs.length) {
                resizePeaksTo(peakIndex + 1);
            }
            secondaryUs[peakIndex - 1] = newU;
            secondaryVs[peakIndex - 1] = newV;
            secondaryAbsoluteVectors[peakIndex - 1] = true;
            secondaryDisplacementsSet[peakIndex - 1] = true;
        }        
    }

    private void computeDefaultDisplacements(int peakIndex) {
        logger.warn("Peak index: {} - No displacement set: Late computing default displacement. Tile I:{}, J:{}, Matrix dimI:{}, dimJ{}", 
                peakIndex, tileA != null ? tileA.getTileIndexI() : -1 , tileA != null ? tileA.getTileIndexJ() : -1, dimCrossI, dimCrossJ);

        if (peakIndex == 0) {
            u = i - dimCrossI / 2;
            v = j - dimCrossJ / 2;
            absoluteVector = false;
            displacementsSet = true;
        } else {
            secondaryUs[peakIndex - 1] = getNthPeakI(peakIndex) - dimCrossI / 2;
            secondaryVs[peakIndex - 1] = getNthPeakJ(peakIndex) - dimCrossJ / 2;
            secondaryAbsoluteVectors[peakIndex - 1] = false;
            secondaryDisplacementsSet[peakIndex - 1] = true;
        }
    }

    public float getNthDisplacementU(int peakIndex) {
        if (!displacementsSet && peakIndex == 0) {
            computeDefaultDisplacements(0);
        } else if (peakIndex > 0 && !secondaryDisplacementsSet[peakIndex - 1]){
            computeDefaultDisplacements(peakIndex);
        }
        if (peakIndex == 0) {
            return u;
        }
        
        return secondaryUs[peakIndex - 1];
    }

    public float getNthDisplacementV(int peakIndex) {
        if (!displacementsSet && peakIndex == 0) {
            computeDefaultDisplacements(0);
        } else if (peakIndex > 0 && !secondaryDisplacementsSet[peakIndex - 1]){
            computeDefaultDisplacements(peakIndex);
        }

        if (peakIndex == 0) {
            return v;
        }
        
        return secondaryVs[peakIndex - 1];
    }

    public boolean isAbsoluteNthDisplacement(int peakIndex) {
        if (!displacementsSet) {
            throw new MaximumFinderException("Trying to retrieve a displacement when displacements were not set");
        }
        
        if (peakIndex == 0) {
            return absoluteVector;
        } else {
            return secondaryAbsoluteVectors[peakIndex - 1];
        }
    }

    public Matrix getCrossMatrix() {
        return crossMatrix;
    }

    public void reset() {
        crossMatrix = null;
        tileA = null;
        tileB = null;
        i = 0;
        j = 0;
        value = 0;
        displacementsSet = false;
        if (secondaryIs != null) {
            Arrays.fill(secondaryIs, 0.0f);
        }
        if (secondaryJs != null) {
            Arrays.fill(secondaryJs, 0.0f);
        }
        if (secondaryValues != null) {
            Arrays.fill(secondaryValues, 0.0f);
        }
        if (secondaryDisplacementsSet != null) {
            Arrays.fill(secondaryDisplacementsSet, false);
        }
    }
}
