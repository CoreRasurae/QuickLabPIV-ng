// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas.validation;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;

public enum VectorValidatorFactoryEnum {
    None,
    DifferenceValidator,
    MedianValidator, 
    CombinedValidator;
    
    public static IVectorValidator[] createValidator(VectorValidatorFactoryEnum validatorType) {
        IVectorValidator[] validator;
        
        switch (validatorType) {
        case DifferenceValidator:
            validator = new IVectorValidator[1];
            validator[0] = new DifferenceValidator();
            break;
        case MedianValidator:
            validator = new IVectorValidator[1];
            validator[0] = new NormalizedMedianValidator(); 
            break;
        case CombinedValidator:
            final PIVContextSingleton singleton = PIVContextSingleton.getSingleton();        
            final PIVInputParameters pivParameters = singleton.getPIVParameters();
            final CombinedValidatorAndReplacementConfiguration[] configs =  
                (CombinedValidatorAndReplacementConfiguration[])pivParameters.getSpecificConfiguration(
                        CombinedValidatorAndReplacementConfiguration.IDENTIFIER);
            validator = new IVectorValidator[configs.length];
            int index = 0;
            for (CombinedValidatorAndReplacementConfiguration config : configs) {
                validator[index++] = createValidator(config.getValidatorType(), config.getValidatorConfiguration());
            }
            break;
        case None:
            validator = null;
            break;
        default:
            throw new VectorValidatorException("Unknown vector validator");
        }
        
        return validator;
    }
    
    private static IVectorValidator createValidator(VectorValidatorFactoryEnum validatorType, Object validatorConfiguration) {
        IVectorValidator validator;
        
        switch (validatorType) {
        case DifferenceValidator:
            validator = new DifferenceValidator((DifferenceValidatorConfiguration)validatorConfiguration);
            break;
        case MedianValidator:
            validator = new NormalizedMedianValidator((NormalizedMedianValidatorConfiguration)validatorConfiguration); 
            break;
        case None:
            validator = null;
            break;
        default:
            throw new VectorValidatorException("Unknown vector validator");
        }
        
        return validator;
    }
}
