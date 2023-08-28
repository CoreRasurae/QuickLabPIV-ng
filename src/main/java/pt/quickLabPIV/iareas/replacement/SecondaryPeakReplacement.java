// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas.replacement;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class SecondaryPeakReplacement implements IVectorReplacement {
    private static Logger logger = LoggerFactory.getLogger(SecondaryPeakReplacement.class);
    
    private int peakIndex;

    public SecondaryPeakReplacement() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(SecondaryPeakReplacementConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new VectorReplacementException("Couldn't retrieve Secondary Peak configuration");
        }
        SecondaryPeakReplacementConfiguration configuration = (SecondaryPeakReplacementConfiguration)configurationObject;
        peakIndex = configuration.getPeakIndex();
        if (peakIndex < 2) {
            throw new VectorReplacementException("Peak index must be equal or greater than 2 (secondary peak)");
        }
    }
    
    public SecondaryPeakReplacement(SecondaryPeakReplacementConfiguration configuration) {
        peakIndex = configuration.getPeakIndex();
    }
  
    @Override
    public void replaceVector(boolean firstPass, int frameNumber, Tile vector, Tile[][] adjacents, List<MaxCrossResult> maxCrosses) {      
        if (!vector.isInvalidDisplacement() || vector.isMaskedDisplacement()) {
            return;
        }
        
        final MaxCrossResult maxCross = maxCrosses.get(0);
        if (maxCross.getTotalPeaks() <= peakIndex) {
            logger.warn("Secondary peak: {} is not available for tile: {}.", peakIndex, vector);
            return;
        }
        
        //This will not work... because this is the displacement increment not the complete displacement in an adaptive PIV...
        //One needs to undo last increment contribution...
        //So, undo main peak displacement contribution, before updating. 
        //Note that VectorValidatorJob always resets invalid vectors to the original value of the main peak.
        float u = vector.getDisplacementU();
        float v = vector.getDisplacementV();

        float uOld = maxCross.getNthDisplacementU(0);
        float vOld = maxCross.getNthDisplacementV(0);
                       
        //Undo main peak displacement contribution
        u -= uOld;
        v -= vOld;
                
        float uNew = maxCross.getNthDisplacementU(peakIndex);
        float vNew = maxCross.getNthDisplacementV(peakIndex);
        
        u += uNew;
        v += vNew;
        
        if (vector.replaceDisplacement(u, v)) {
            vector.setInvalidDisplacement(false);
        }
    }

}
