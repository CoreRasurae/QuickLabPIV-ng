package pt.quickLabPIV.ui.models;

public enum VelocityValidationReplacementModeEnum {
    Invalid("Invalid vector replacement mode"),
    Bilinear("Bi-Linear vector replacement mode");
    
    private String description;
    
    private VelocityValidationReplacementModeEnum(String desc) {
        description = desc;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
