package pt.quickLabPIV.iareas.validation;

import pt.quickLabPIV.iareas.replacement.VectorReplacementFactoryEnum;

public class CombinedValidatorAndReplacementConfiguration {
    public static final String IDENTIFIER = "COMBINED_VALIDATOR_CONFIG"; 
    
    private VectorValidatorFactoryEnum validatorStrategy;
    private Object validatorConfiguration;
    private VectorReplacementFactoryEnum replacementStrategy;
    private Object replacementConfiguration;
    
    public CombinedValidatorAndReplacementConfiguration(VectorValidatorFactoryEnum _validatorStrategy, Object _validatorConfiguration,
            VectorReplacementFactoryEnum _replacementStrategy, Object _replacementConfiguration) {
        validatorStrategy = _validatorStrategy;
        validatorConfiguration = _validatorConfiguration;
        replacementStrategy = _replacementStrategy;
        replacementConfiguration = _replacementConfiguration;
    }

    public VectorValidatorFactoryEnum getValidatorType() {
        return validatorStrategy;
    }
    
    public Object getValidatorConfiguration() {
        return validatorConfiguration;
    }
    
    public VectorReplacementFactoryEnum getReplacementType() {
        return replacementStrategy;
    }
    
    public Object getReplacementConfiguration() {
        return replacementConfiguration;
    }
}
