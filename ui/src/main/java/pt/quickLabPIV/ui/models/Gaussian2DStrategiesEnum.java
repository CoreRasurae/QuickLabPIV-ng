package pt.quickLabPIV.ui.models;

public enum Gaussian2DStrategiesEnum {
    Invalid("Invalid method"),
    Symmetric("Symmetric 2D-Gaussian method"),
    Assymetric("Assymetric 2D-Gaussian method"),
    AssymetricWithRotation("Assymetric with rotation 2D-Gaussian method");
    
    private String description;
    
    private Gaussian2DStrategiesEnum(String _description) {
        description = _description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }

}
