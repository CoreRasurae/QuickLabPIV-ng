package pt.quickLabPIV.ui.models;

public enum ImageFilteringModeEnum {
    DoNotApplyImageFiltering("Do not apply image filtering"),
    ApplyImageFilteringGaussian2D("Apply image filtering with a 2D Gaussian filter");
 
    private String description;
    
    private ImageFilteringModeEnum(String _description) {
        description = _description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return description;
    }
}
