package pt.quickLabPIV.ui.models;

public enum InheritanceModeEnum {
        Invalid("Invalid inheritance mode"),
        Distance("Distance inheritance mode"),
        Area("Area inheritance mode"),
        BiCubicSpline("Bi-cubic spline inheritance mode");
    
    
    private String description;
    
    private InheritanceModeEnum(String desc) {
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
