// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas.replacement;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.validation.CombinedValidatorAndReplacementConfiguration;

public enum VectorReplacementFactoryEnum {
    None,
    Bilinear,
    SecondaryPeak,
    CombinedReplacement;
    
    public static IVectorReplacement[] createReplacer(VectorReplacementFactoryEnum replacementType) {
        IVectorReplacement[] method;
        
        switch(replacementType) {
        case Bilinear:
            method = new IVectorReplacement[1];
            method[0] = new BilinearReplacement();
            break;
        case SecondaryPeak:
            method = new IVectorReplacement[1];
            method[0] = new SecondaryPeakReplacement();
            break;
        case CombinedReplacement:
            final PIVContextSingleton singleton = PIVContextSingleton.getSingleton();        
            final PIVInputParameters pivParameters = singleton.getPIVParameters();
            final CombinedValidatorAndReplacementConfiguration[] configs =  
                (CombinedValidatorAndReplacementConfiguration[])pivParameters.getSpecificConfiguration(
                        CombinedValidatorAndReplacementConfiguration.IDENTIFIER);
            method = new IVectorReplacement[configs.length];
            int index = 0;
            for (CombinedValidatorAndReplacementConfiguration config : configs) {
                method[index++] = createReplacer(config.getReplacementType(), config.getReplacementConfiguration());
            }
            break;
        case None:
            method = new IVectorReplacement[1];
            method[0] = null;
            break;
        default:
            throw new VectorReplacementException("Unknown vector replacement method");
        }
        
        return method;
    }

    private static IVectorReplacement createReplacer(VectorReplacementFactoryEnum replacementType,
            Object replacementConfiguration) {
        IVectorReplacement method;
        
        switch(replacementType) {
        case Bilinear:
            method = new BilinearReplacement();
            break;
        case SecondaryPeak:
            method = new SecondaryPeakReplacement((SecondaryPeakReplacementConfiguration)replacementConfiguration);
            break;
        case None:
            method = null;
            break;
        default:
            throw new VectorReplacementException("Unknown vector replacement method");
        }
        
        return method;
    }
}
