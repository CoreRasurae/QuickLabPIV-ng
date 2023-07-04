package pt.quickLabPIV.ui.models;

public enum SubPixelInterpolationModeEnum {
    Disabled("Sub-pixel interpolation mode disabled", false, true, false),
    BiCubic("Bi-Cubic sub-pixel interpolation mode", false, false, false),
    Gaussian1D("1D-1D Gaussian sub-pixel interpolation mode", false, false, false),
    Gaussian1DHongweiGuo("1D-1D Gaussian Hongwei Guo sub-pixel interpolation mode", false, false, false),
    Gaussian1DPolynomial("1D-1D Gaussian 3 point polynomial sub-pixel interpolation mode", false, false, false),
    Centroid2D("2D n-point Centroid sub-pixel interpolation mode", false, false, false),
    Gaussian2D("2D Gaussian sub-pixel interpolation mode", false, false, false),
    Gaussian2DPolynomial("2D Gaussian 3 point polynomial sub-pixel with 2D n-point Centroid backup interpolation mode", false, false, false),
    Gaussian2DLinearRegression("Linear Regression based 2D Gaussian sub-pixel interpolation with Polynomial 1D-1D Gaussian backup mode", false, false, false),
    LucasKanade("Java only Lucas-Kanade optical flow sub-pixel estimator", false, true, false),
    LucasKanadeOpenCL("OpenCL accelerated Lucas-Kanade optical flow sub-pixel estimator", false, true, true),
    LiuShenWithLucasKanade("Java only Liu-Shen with Lucas-Kanade combination optical flow sub-pixel estimator", false, true, false),
    LiuShenWithLucasKanadeOpenCL("OpenCL accelerated Liu-Shen with Lucas-Kanade combination optical flow sub-pixel estimator", false, true, true),
    CombinedBaseAndFinalInterpolator("Selectable combinable base interpolator and a final level interpolator sub-pixel", true, false, false);
    
    private String description;
    private boolean compositeMethod;
    private boolean opticalFlowMethod;
    private boolean supportsDenseVectors;
    
    private SubPixelInterpolationModeEnum(String desc, boolean _compositeMethod, boolean _opticalFlowMethod, boolean _supportsDenseVectors) {
        description = desc;
        compositeMethod = _compositeMethod;
        opticalFlowMethod = _opticalFlowMethod;
        supportsDenseVectors = _supportsDenseVectors;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompositeMode() {
        return compositeMethod;
    }
    
    public boolean isOpticalFlow() {
        return opticalFlowMethod;
    }
    
    public boolean isSupportsDenseVectors() {
        return supportsDenseVectors;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
