package pt.quickLabPIV.jobs.validator;

public class VectorValidatorConfiguration {

    public static final String IDENTIFIER = "VectorValidator";
    
    private final int maxCorrectionIterations;
    private final boolean iterateUntilNoMoreCorrections;
    
    public VectorValidatorConfiguration(int _maxCorrectionIterations, boolean _iterateUntilNoMoreCorrections) {
        maxCorrectionIterations = _maxCorrectionIterations;
        iterateUntilNoMoreCorrections = _iterateUntilNoMoreCorrections;
    }
    
    public int getMaxCorrectionIterations() {
        return maxCorrectionIterations;
    }
    
    public boolean isIterateUntilNoMoreCorrections() {
        return iterateUntilNoMoreCorrections;
    }
}
