package pt.quickLabPIV.iareas.validation;

public class DifferenceValidatorConfiguration {
    public static final String IDENTIFIER = "ValidatorDifference"; 
    private float distanceThreshold;
    
    public DifferenceValidatorConfiguration(float _distanceThreshold) {
        distanceThreshold = _distanceThreshold;
    }
    
    public float getDistanceThreshold() {
        return distanceThreshold;
    }
}
