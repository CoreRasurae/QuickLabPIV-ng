package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import pt.quickLabPIV.business.facade.IFileFactory;
import pt.quickLabPIV.business.facade.PIVConfigurationFacade;

@XmlAccessorType(XmlAccessType.FIELD)
public class PIVConfigurationModel implements PropertyChangeListener {
    @XmlTransient
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    @XmlTransient
    private ProjectModel project;
        
    private int imageWidth;
    private int imageHeight;
    
    private boolean maskEnabled;
    private boolean maskOnlyAtExport;
    @XmlElement(nillable=true)
    private File maskFile;
    private File sourceImageFolder;
    private File sourceImageFile;
    private PIVImageTypeEnum imageType;
    private String imagePatternA;
    private String imagePatternB;
    @XmlTransient
    private int availableImageFiles;
    @XmlTransient
    private int totalImageFiles;
    private int numberOfImages;
    
    private ImageFilteringModeEnum  imageFilteringMode = ImageFilteringModeEnum.DoNotApplyImageFiltering;
    @XmlElementWrapper(name = "imageFilterOptions")
    @XmlElements (
        { 
            @XmlElement(name="imageFilter-Gaussian2D", type=ImageFilterOptionsGaussian2DModel.class),
        }
    )
    private List<ImageFilterOptionsModel> imageFilterOptions = new ArrayList<ImageFilterOptionsModel>();
    private boolean imageFilteringAfterWarpingOnly = false;
    
    private InterrogationAreaResolutionEnum initialResolution = InterrogationAreaResolutionEnum.IA0;
    private InterrogationAreaResolutionEnum endResolution = InterrogationAreaResolutionEnum.IA0;
    private ClippingModeEnum clippingMode;
    private WarpingModeEnum warpingMode = WarpingModeEnum.Invalid;
    private int topMargin;
    private int bottomMargin;
    private int leftMargin;
    private int rightMargin;
    
    private InheritanceModeEnum inheritanceMode = InheritanceModeEnum.Invalid;
    
    private float superpositionOverlapPercentage = 0.0f;
    private InterrogationAreaResolutionEnum superpositionStartStep = InterrogationAreaResolutionEnum.IA0;
    
    private SubPixelInterpolationModeEnum interpolationMode = SubPixelInterpolationModeEnum.Disabled;
    private InterrogationAreaResolutionEnum interpolationStartStep = InterrogationAreaResolutionEnum.IA0;
    
    @XmlElementWrapper(name = "interpolationOptions")
    @XmlElements (
        { 
            @XmlElement(name="interpolation-BiCubic", type=SubPixelInterpolationOptionsBiCubicModel.class),
            @XmlElement(name="interpolation-Gaussian1D", type=SubPixelInterpolationOptionsGaussian1DModel.class),
            @XmlElement(name="interpolation-Gaussian1D-HongweiGuo", type=SubPixelInterpolationOptionsGaussian1DHongweiGuoModel.class),
            @XmlElement(name="interpolation-Gaussian1DPolynomial", type=SubPixelInterpolationOptionsGaussian1DPolynomialModel.class),
            @XmlElement(name="interpolation-Centroid2D", type=SubPixelInterpolationOptionsCentroid2DModel.class),
            @XmlElement(name="interpolation-Gaussian2D", type=SubPixelInterpolationOptionsGaussian2DModel.class),
            @XmlElement(name="interpolation-Gaussian2DPolynomial", type=SubPixelInterpolationOptionsGaussian2DPolynomialModel.class),
            @XmlElement(name="interpolation-Gaussian2DLinearRegression", type=SubPixelInterpolationOptionsGaussian2DLinearRegressionModel.class),
            @XmlElement(name="interpolation-LucasKanade", type=SubPixelInterpolationOptionsLucasKanadeModel.class),
            @XmlElement(name="interpolation-LucasKanadeOpenCL", type=SubPixelInterpolationOptionsLucasKanadeOpenCLModel.class),
            @XmlElement(name="interpolation-CombinedBaseAndFinal", type=SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel.class),
            @XmlElement(name="interpolation-LiuShenWithLucasKanade", type=SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel.class),
            @XmlElement(name="interpolation-LiuShenWithLucasKanadeOpenCL", type=SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.class)
        }
    )
    //This also works, but uses XML name-spaces
    //@XmlElement(name="interpolation")
    private List<SubPixelInterpolationOptionsModel> interpolationOptions = new ArrayList<SubPixelInterpolationOptionsModel>();
    
    private VelocityStabilizationModeEnum stabilizationMode = VelocityStabilizationModeEnum.Disabled;
    @XmlElementWrapper(name = "stabilizationOptions")
    @XmlElements (
        { 
            @XmlElement(name="stabilization-MaxDisplacement", type=VelocityStabilizationOptionsMaxDisplacementModel.class)
        }
    )
    private List<VelocityStabilizationOptionsModel> stabilizationOptions = new ArrayList<VelocityStabilizationOptionsModel>();

    
    private boolean validationReplaceInvalidByNaNs;
    
    private int validationNumberOfValidatorIterations;
    
    private boolean validationIterateUntilNoMoreReplaced;
    
    private VelocityValidationModeEnum validationMode = VelocityValidationModeEnum.Disabled;
    @XmlElementWrapper(name = "validationOptions")
    @XmlElements (
        { 
            @XmlElement(name="validation-Difference", type=VelocityValidationOptionsDifferenceModel.class),
            @XmlElement(name="validation-DifferenceOnly", type=VelocityValidationOptionsDifferenceOnlyModel.class),
            @XmlElement(name="validation-NormalizedMedian", type=VelocityValidationOptionsNormalizedMedianModel.class),
            @XmlElement(name="validation-NormalizedMedianOnly", type=VelocityValidationOptionsNormalizedMedianOnlyModel.class),
            @XmlElement(name="validation-MultiPeakNormalizedMedian", type=VelocityValidationOptionsMultiPeakNormalizedMedianModel.class)
        }
    )
    private List<VelocityValidationOptionsModel> validationOptions = new ArrayList<VelocityValidationOptionsModel>();
    
    private VelocityValidationReplacementModeEnum validationReplacementMode = VelocityValidationReplacementModeEnum.Invalid;

    public PIVConfigurationModel() {
        pcs.addPropertyChangeListener(this);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int _imageWidth) {
        int[] oldResolution = getImageResolution();
        int oldValue = _imageWidth;
        imageWidth = _imageWidth;
        pcs.firePropertyChange("imageWidth", oldValue, imageWidth);
        pcs.firePropertyChange("imageResolution", oldResolution, getImageResolution());
    }
    
    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int _imageHeight) {
        int[] oldResolution = getImageResolution();
        int oldValue = _imageHeight;
        imageHeight = _imageHeight;
        pcs.firePropertyChange("imageHeight", oldValue, imageHeight);
        pcs.firePropertyChange("imageResolution", oldResolution, getImageResolution());
    }
    
    public int[] getImageResolution() {
        return new int[] {imageWidth, imageHeight};
    }
    
    public File getSourceImageFolder() {
        return sourceImageFolder;
    }
    
    public void setSourceImageFolder(File folder) {
        File oldValue = sourceImageFolder;
        sourceImageFolder = folder;
        pcs.firePropertyChange("sourceImageFolder", oldValue, folder);        
    }

    public File getSourceImageFile() {
        return sourceImageFile;
    }
    
    public void setSourceImageFile(File selectedSource) {
        File oldValue = sourceImageFile;
        sourceImageFile = selectedSource;
        pcs.firePropertyChange("sourceImageFile", oldValue, sourceImageFile);
    }
    
    public void setImageType(PIVImageTypeEnum _imageType) {
        PIVImageTypeEnum oldType = imageType;
        imageType = _imageType;
        pcs.firePropertyChange("imageType", oldType, imageType);
    }
    
    public PIVImageTypeEnum getImageType() {
        return imageType;
    }
    
    public void setImagePatternA(String patternA) {
        String oldValue = imagePatternA; 
        imagePatternA = patternA;
        pcs.firePropertyChange("imagePatternA", oldValue, patternA);
    }
    
    public String getImagePatternA() {
        return imagePatternA;
    }
    
    public void setImagePatternB(String patternB) {
        String oldValue = imagePatternB;
        imagePatternB = patternB;
        pcs.firePropertyChange("imagePatternB", oldValue, patternB);
    }
    
    public String getImagePatternB() {
        return imagePatternB;
    }

    public int getAvailableImageFiles() {
        return availableImageFiles;
    }    
    
    public int getTotalImageFiles() {
        return totalImageFiles;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }
    
    public void setNumberOfImages(int number) {
        int oldValue = numberOfImages;
        numberOfImages = number;
        pcs.firePropertyChange("numberOfImages", oldValue, number);
    }
    
    public boolean isMaskEnabled() {
        return maskEnabled;
    }
    
    public void setMaskEnabled(boolean enabled) {
        boolean oldValue = maskEnabled;
        maskEnabled = enabled;
        pcs.firePropertyChange("maskEnabled", oldValue, maskEnabled);
    }
    
    public boolean isMaskOnlyAtExport() {
        return maskOnlyAtExport;
    }
    
    public void setMaskOnlyAtExport(boolean mask) {
        boolean oldValue = maskOnlyAtExport;
        maskOnlyAtExport = mask;
        pcs.firePropertyChange("maskOnlyAtExport", oldValue, maskOnlyAtExport);
    }
    
    public File getMaskFile() {
        return maskFile;
    }

    public void setMaskFile(File newMask) {
        File oldValue = maskFile;
        maskFile = newMask;
        pcs.firePropertyChange("maskFile", oldValue, maskFile);
    }
    
    public InterrogationAreaResolutionEnum getInitialResolution() {
        return initialResolution;
    }
    
    public void setInitialResolution(InterrogationAreaResolutionEnum resolution) {
        InterrogationAreaResolutionEnum oldValue = initialResolution;
        initialResolution = resolution;
        pcs.firePropertyChange("initialResolution", oldValue, initialResolution);
    }
    
    public InterrogationAreaResolutionEnum getEndResolution() {
        return endResolution;
    }
    
    public void setEndResolution(InterrogationAreaResolutionEnum resolution) {
        InterrogationAreaResolutionEnum oldValue = endResolution;
        endResolution = resolution;
        pcs.firePropertyChange("endResolution", oldValue, endResolution);
    }

    public ImageFilteringModeEnum getImageFilteringMode() {
        return imageFilteringMode;
    }
    
    public void setImageFilteringMode(ImageFilteringModeEnum filteringMode) {
        ImageFilteringModeEnum oldValue = imageFilteringMode;
        imageFilteringMode = filteringMode;
        pcs.firePropertyChange("imageFilteringMode", oldValue, imageFilteringMode);
    }
   
    public boolean isImageFilteringAfterWarpingOnly() {
        return imageFilteringAfterWarpingOnly;
    }
    
    public void setImageFilteringAfterWarpingOnly(boolean afterWarpingOnly) {
        boolean oldValue = imageFilteringAfterWarpingOnly;
        imageFilteringAfterWarpingOnly = afterWarpingOnly;
        pcs.firePropertyChange("imageFilteringAfterWarpingOnly", oldValue, imageFilteringAfterWarpingOnly);
    }
    
    public ImageFilterOptionsModel getImageFilterOption(ImageFilteringModeEnum optionType) {
        Optional<ImageFilterOptionsModel> result = 
                imageFilterOptions.stream().filter(option -> option.getFilterMode() == optionType).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
    
    public void setImageFilterOption(ImageFilterOptionsModel model) {
        if (model == null) {
            return;
        }
        
        Iterator<ImageFilterOptionsModel> iter = imageFilterOptions.iterator();
        ImageFilterOptionsModel oldValue = null;
        while (iter.hasNext()) {
            ImageFilterOptionsModel oldModel = iter.next();
            if (oldModel.getFilterMode() == model.getFilterMode()) {
                if (model == oldModel) {
                    return;
                } else {
                    oldValue = oldModel;
                    iter.remove();
                    break;
                }
            }
        }
        imageFilterOptions.add(model);
        pcs.firePropertyChange("imageFilterOption", oldValue, model);
    }

    
    public ClippingModeEnum getClippingMode() {
        return clippingMode;
    }

    public void setClippingMode(ClippingModeEnum clippingMode) {
        ClippingModeEnum oldValue = this.clippingMode;
        this.clippingMode = clippingMode;
        pcs.firePropertyChange("clippingMode", oldValue, this.clippingMode);
    }

    public WarpingModeEnum getWarpingMode() {
        return warpingMode;
    }
    
    public void setWarpingMode(WarpingModeEnum mode) {
        WarpingModeEnum oldValue = warpingMode;
        warpingMode = mode;
        pcs.firePropertyChange("warpingMode", oldValue, warpingMode);
    }
    
    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int _topMargin) {
        int oldValue = topMargin;
        topMargin = _topMargin;
        pcs.firePropertyChange("topMargin", oldValue, topMargin);
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int _bottomMargin) {
        int oldValue = bottomMargin;
        bottomMargin = _bottomMargin;
        pcs.firePropertyChange("bottomMargin", oldValue, bottomMargin);
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int _leftMargin) {
        int oldValue = leftMargin;
        leftMargin = _leftMargin;
        pcs.firePropertyChange("leftMargin", oldValue, leftMargin);
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int _rightMargin) {
        int oldValue = rightMargin;
        rightMargin = _rightMargin;
        pcs.firePropertyChange("rightMargin", oldValue, rightMargin);
    }
    
    public InheritanceModeEnum getInheritanceMode() {
        return inheritanceMode;
    }

    public void setInheritanceMode(InheritanceModeEnum _inheritanceMode) {
        InheritanceModeEnum oldValue = inheritanceMode;
        inheritanceMode = _inheritanceMode;
        pcs.firePropertyChange("inheritanceMode", oldValue, inheritanceMode);
    }

    public float getSuperpositionOverlapPercentage() {
        return superpositionOverlapPercentage;
    }

    public void setSuperpositionOverlapPercentage(float _overlapFactor) {
        float oldValue = superpositionOverlapPercentage;
        superpositionOverlapPercentage = _overlapFactor;
        pcs.firePropertyChange("superpositionOverlapPercentage", oldValue, superpositionOverlapPercentage);
    }

    public InterrogationAreaResolutionEnum getSuperpositionStartStep() {
        return superpositionStartStep;
    }

    public void setSuperpositionStartStep(InterrogationAreaResolutionEnum _startStep) {
        InterrogationAreaResolutionEnum oldValue = superpositionStartStep;
        superpositionStartStep = _startStep;
        pcs.firePropertyChange("superpositionStartStep", oldValue, superpositionStartStep);
    }

    public SubPixelInterpolationModeEnum getInterpolationMode() {
        return interpolationMode;
    }

    public void setInterpolationMode(SubPixelInterpolationModeEnum _interpolationMode) {
        SubPixelInterpolationModeEnum oldValue = interpolationMode;
        interpolationMode = _interpolationMode;
        pcs.firePropertyChange("interpolationMode", oldValue, interpolationMode);
    }

    public InterrogationAreaResolutionEnum getInterpolationStartStep() {
        return interpolationStartStep;
    }

    public void setInterpolationStartStep(InterrogationAreaResolutionEnum _interpolationStartStep) {
        InterrogationAreaResolutionEnum oldValue = interpolationStartStep;
        interpolationStartStep = _interpolationStartStep;
        pcs.firePropertyChange("interpolationStartStep", oldValue, interpolationStartStep);
    }

    public List<SubPixelInterpolationOptionsModel> getInterpolationOptions() {
        return interpolationOptions;
    }
    
    public void setInterpolationOptions(List<SubPixelInterpolationOptionsModel> newOptions) {
        interpolationOptions = newOptions;
    }
    
    public SubPixelInterpolationOptionsModel getInterpolationOption(SubPixelInterpolationModeEnum optionType) {
        Optional<SubPixelInterpolationOptionsModel> result = 
                interpolationOptions.stream().filter(option -> option.getInterpolationMode() == optionType).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
    
    public void setInterpolationOption(SubPixelInterpolationOptionsModel model) {
        if (model == null) {
            return;
        }
        
        Iterator<SubPixelInterpolationOptionsModel> iter = interpolationOptions.iterator();
        SubPixelInterpolationOptionsModel oldValue = null;
        while (iter.hasNext()) {
            SubPixelInterpolationOptionsModel oldModel = iter.next();
            if (oldModel.getInterpolationMode() == model.getInterpolationMode()) {
                if (model == oldModel) {
                    return;
                } else {
                    oldValue = oldModel;
                    iter.remove();
                    break;
                }
            }
        }
        interpolationOptions.add(model);
        pcs.firePropertyChange("interpolationOption", oldValue, model);
    }
    
    public VelocityStabilizationModeEnum getStabilizationMode() {
        return stabilizationMode;
    }

    public void setStabilizationMode(VelocityStabilizationModeEnum _stabilizationMode) {
        VelocityStabilizationModeEnum oldValue = stabilizationMode; 
        stabilizationMode = _stabilizationMode;
        pcs.firePropertyChange("stabilizationMode", oldValue, stabilizationMode);
    }
    
    public List<VelocityStabilizationOptionsModel> getStabilizationOptions() {
        return stabilizationOptions;
    }
    
    public void setStabilizationOptions(List<VelocityStabilizationOptionsModel> newOptions) {
        stabilizationOptions = newOptions;
    }
    
    public VelocityStabilizationOptionsModel getStabilizationOption(VelocityStabilizationModeEnum optionType) {
        Optional<VelocityStabilizationOptionsModel> result = 
                stabilizationOptions.stream().filter(option -> option.getStabilizationMode() == optionType).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
    
    public void setStabilizationOption(VelocityStabilizationOptionsModel model) {
        if (model == null) {
            return;
        }
        
        Iterator<VelocityStabilizationOptionsModel> iter = stabilizationOptions.iterator();
        VelocityStabilizationOptionsModel oldValue = null;
        while (iter.hasNext()) {
            VelocityStabilizationOptionsModel oldModel = iter.next();
            if (oldModel.getStabilizationMode() == model.getStabilizationMode()) {
                if (model == oldModel) {
                    return;
                } else {
                    oldValue = oldModel;
                    iter.remove();
                    break;
                }
            }
        }
        stabilizationOptions.add(model);
        pcs.firePropertyChange("stabilizationOption", oldValue, model);
    }
    
    public boolean isValidationReplaceInvalidByNaNs() {
        return validationReplaceInvalidByNaNs;
    }

    public void setValidationReplaceInvalidByNaNs(boolean _validatorReplaceInvalidByNaNs) {
        validationReplaceInvalidByNaNs = _validatorReplaceInvalidByNaNs;
    }

    public int getValidationNumberOfValidatorIterations() {
        return validationNumberOfValidatorIterations;
    }

    public void setValidationNumberOfValidatorIterations(int _numberOfValidatorIterations) {
        validationNumberOfValidatorIterations = _numberOfValidatorIterations;
    }

    public boolean isValidationIterateUntilNoMoreReplaced() {
        return validationIterateUntilNoMoreReplaced;
    }

    public void setValidationIterateUntilNoMoreReplaced(boolean _validatorIterateUntilNoMoreReplaced) {
        validationIterateUntilNoMoreReplaced = _validatorIterateUntilNoMoreReplaced;
    }
    
    public VelocityValidationModeEnum getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(VelocityValidationModeEnum _validationMode) {
        VelocityValidationModeEnum oldValue = validationMode; 
        validationMode = _validationMode;
        pcs.firePropertyChange("validationMode", oldValue, validationMode);
    }
    
    public VelocityValidationOptionsModel getValidationOption(VelocityValidationModeEnum optionType) {
        Optional<VelocityValidationOptionsModel> result = 
                validationOptions.stream().filter(option -> option.getValidationMode() == optionType).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    public void setValidationOption(VelocityValidationOptionsModel model) {
        if (model == null) {
            return;
        }
        
        Iterator<VelocityValidationOptionsModel> iter = validationOptions.iterator();
        VelocityValidationOptionsModel oldValue = null;
        while (iter.hasNext()) {
            VelocityValidationOptionsModel oldModel = iter.next();
            if (oldModel.getValidationMode() == model.getValidationMode()) {
                if (model == oldModel) {
                    return;
                } else {
                    oldValue = oldModel;
                    iter.remove();
                    break;
                }
            }
        }
        validationOptions.add(model);
        pcs.firePropertyChange("validationOption", oldValue, model);
    }
    
    public VelocityValidationReplacementModeEnum getValidationReplacementMode() {
        return validationReplacementMode;
    }
    
    public void setValidationReplacementMode(VelocityValidationReplacementModeEnum _validationReplacementMode) {
        VelocityValidationReplacementModeEnum oldValue = validationReplacementMode; 
        validationReplacementMode = _validationReplacementMode;
        pcs.firePropertyChange("validationReplacementMode", oldValue, validationReplacementMode);
    }
    
    public PIVConfigurationModel copy() {
        PIVConfigurationModel model = new PIVConfigurationModel();
     
        model.maskEnabled = maskEnabled;
        model.maskOnlyAtExport = maskOnlyAtExport;
        //File is assumed to be immutable
        model.maskFile = maskFile;
        model.sourceImageFile = sourceImageFile;
        model.sourceImageFolder = sourceImageFolder;
        model.imageType = imageType;
        model.imagePatternA = imagePatternA;
        model.setImagePatternB(imagePatternB); //Trigger a total image files update
        model.numberOfImages = numberOfImages;
        model.imageFilteringMode = imageFilteringMode;
        model.imageFilterOptions = copyImageFilterOptions(imageFilterOptions);
        model.imageFilteringAfterWarpingOnly = imageFilteringAfterWarpingOnly;
        //model.totalImageFiles = totalImageFiles; //If this value is copied... total image files won't be computed
        model.clippingMode = clippingMode;
        model.warpingMode = warpingMode;
        model.initialResolution = initialResolution;
        model.endResolution = endResolution;
        model.imageHeight = imageHeight;
        model.imageWidth = imageWidth;
        model.topMargin = topMargin;
        model.bottomMargin = bottomMargin;
        model.leftMargin = leftMargin;
        model.rightMargin = rightMargin;
        
        model.inheritanceMode = inheritanceMode;
        
        model.superpositionOverlapPercentage = superpositionOverlapPercentage;
        model.superpositionStartStep = superpositionStartStep;
        
        model.interpolationMode = interpolationMode;
        model.interpolationStartStep = interpolationStartStep;
        model.interpolationOptions = copyInterpolationOptions(interpolationOptions);
        
        model.stabilizationMode = stabilizationMode;
        model.stabilizationOptions = copyStabilizationOptions(stabilizationOptions);
        
        model.validationReplaceInvalidByNaNs = validationReplaceInvalidByNaNs;
        model.validationNumberOfValidatorIterations = validationNumberOfValidatorIterations;
        model.validationIterateUntilNoMoreReplaced = validationIterateUntilNoMoreReplaced;
        model.validationMode = validationMode;
        model.validationOptions = copyValidationOptions(validationOptions);
        
        model.validationReplacementMode = validationReplacementMode;
        
        return model;
    }

    private static List<ImageFilterOptionsModel> copyImageFilterOptions(List<ImageFilterOptionsModel> imageFilterOptions) {
        List<ImageFilterOptionsModel> results = new ArrayList<>(imageFilterOptions.size());
        
        for (ImageFilterOptionsModel model : imageFilterOptions) {
            results.add(model.copy());
        }
        
        return results;
    }

    private static List<SubPixelInterpolationOptionsModel> copyInterpolationOptions(List<SubPixelInterpolationOptionsModel> interpolationOptions) {
        List<SubPixelInterpolationOptionsModel> results = new ArrayList<>(interpolationOptions.size());
        
        for (SubPixelInterpolationOptionsModel model : interpolationOptions) {
            results.add(model.copy());
        }
        
        return results;
    }

    private static List<VelocityStabilizationOptionsModel> copyStabilizationOptions(List<VelocityStabilizationOptionsModel> stabilizationOptions) {
        List<VelocityStabilizationOptionsModel> results = new ArrayList<>(stabilizationOptions.size());
        
        for (VelocityStabilizationOptionsModel model : stabilizationOptions) {
            results.add(model.copy());
        }
        
        return results;
    }
    
    private static List<VelocityValidationOptionsModel> copyValidationOptions(List<VelocityValidationOptionsModel> validationOptions) {
        List<VelocityValidationOptionsModel> results = new ArrayList<>(validationOptions.size());
        
        for (VelocityValidationOptionsModel model : validationOptions) {
            results.add(model.copy());
        }
        
        return results;
    }
    
    private void computeTotalImageFiles() {
        int oldTotalValue = totalImageFiles;
        int oldAvailableValue = availableImageFiles;
        totalImageFiles = 0;
        availableImageFiles = 0;
        if (imagePatternA != null) {
            Pattern pattern = Pattern.compile(imagePatternA);
            Matcher matcher = pattern.matcher(sourceImageFile.getName());
            if (matcher.matches()) {
                List<List<String>> files = PIVConfigurationFacade.getFileList(this);
                if (files.size() != 0) {
                    totalImageFiles = files.get(0).size();
                    availableImageFiles = totalImageFiles;
                }
            }
        }
        
        if (availableImageFiles > 0 && imageType == PIVImageTypeEnum.PIVImageSequence) {
            //Image sequence have always one image less available, to make a pair
            availableImageFiles--;
        }
        
        if (oldAvailableValue != availableImageFiles) {
            pcs.firePropertyChange("availableImageFiles", oldAvailableValue, availableImageFiles);
        }
        if (oldTotalValue != totalImageFiles) {
            pcs.firePropertyChange("totalImageFiles", oldTotalValue, totalImageFiles);
        }
        
        if (availableImageFiles < numberOfImages) {
            setNumberOfImages(0);
        }
    }

    private void checkForUpdateOfTotalImageFiles(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("imageType") ||
            evt.getPropertyName().equals("sourceImageFolder") ||
            evt.getPropertyName().equals("sourceImageFile") ||
            evt.getPropertyName().equals("imagePatternA") ||
            evt.getPropertyName().equals("imagePatternB")) {
            computeTotalImageFiles();
        }
    }

    private void checkForUpdateOfImageFolder(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("sourceImageFolder") && sourceImageFile != null) {
            IFileFactory factory = getParent().getParent().getFileFactory();
            File newSourceImageFile = factory.createFile((File)evt.getNewValue(), sourceImageFile.getName());
            setSourceImageFile(newSourceImageFile);    
        }
    }
    
    public void setParent(ProjectModel model) {
        project = model;
    }
    
    public ProjectModel getParent() {
        return project;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {        
        checkForUpdateOfTotalImageFiles(evt);
        checkForUpdateOfImageFolder(evt);
    }
    
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
        ImageFilterOptionsModel imageFilterModel = getImageFilterOption(imageFilteringMode);
        if (imageFilterModel != null) {
            imageFilterModel.accept(visitor);
        }
        
        SubPixelInterpolationOptionsModel subPixelModel = getInterpolationOption(interpolationMode);
        if (subPixelModel != null) {
            subPixelModel.accept(visitor);
            if (subPixelModel.isCombinedModel()) {
                for (SubPixelInterpolationModeEnum subPixelInnerMode : subPixelModel.getSubInterpolationModes()) {
                    SubPixelInterpolationOptionsModel subPixelInnerModel = getInterpolationOption(subPixelInnerMode);
                    if (subPixelInnerModel != null) {
                        subPixelInnerModel.accept(visitor);
                    }
                }
            }
        }        
        
        VelocityStabilizationOptionsModel stabilizationModel = getStabilizationOption(stabilizationMode);
        if (stabilizationModel != null) {
            stabilizationModel.accept(visitor);
        }
        
        VelocityValidationOptionsModel validationModel = getValidationOption(validationMode);
        if (validationModel != null) {
            validationModel.accept(visitor);
        }
    }
    
    public boolean isChanged(PIVConfigurationModel another) {
        boolean changed = false;
        
        if (this == another) {
            return false;
        }
        
        if (imageWidth != another.imageWidth) {
            changed = true;
        }
        
        if (imageHeight != another.imageHeight) {
            changed = true;
        }
        
        if (maskEnabled != another.maskEnabled) {
            changed = true;
        } else if (maskEnabled && !maskFile.equals(another.maskFile)) {
            changed = true;
        } else if (maskEnabled && maskOnlyAtExport != another.maskOnlyAtExport) {
            changed = true;
        }
        
        if (sourceImageFolder != another.sourceImageFolder) {
            if (sourceImageFolder == null && another.sourceImageFolder != null) {
                changed = true;
            } else if (!sourceImageFolder.equals(another.sourceImageFolder)) {
                //sourceImageFolder is not null for sure, at this point, because both are different,
                //and sourceImageFolder was not null.
                changed = true; 
            }
        }
        
        if (sourceImageFile != another.sourceImageFile) {
            if (sourceImageFile == null && another.sourceImageFile != null) {
                changed = true;
            } else if (!sourceImageFile.equals(another.sourceImageFile)) {
                //sourceImageFile is not null for sure, at this point, because both are different,
                //and sourceImageFile was not null.
                changed = true;
            }
        }
        
        if (imageType != another.imageType) {
            changed = true;
        }
        
        if (imagePatternA == null && another.imagePatternA != null) {
            changed = true;
        } else if (imagePatternA != null && !imagePatternA.equals(another.imagePatternA)) {
            changed = true;
        }

        if (imagePatternB == null && another.imagePatternB != null) {
            changed = true;
        } else if (imagePatternB != null && !imagePatternB.equals(another.imagePatternB)) {
            changed = true;
        }

        if (numberOfImages != another.numberOfImages) {
            changed = true;
        }

        if (initialResolution != another.initialResolution) {
            changed = true;
        }
        
        if (endResolution != another.endResolution) {
            changed = true;
        }
        
        if (imageFilteringMode != another.imageFilteringMode) {
            changed = true;
        }
        
        if (imageFilteringAfterWarpingOnly != another.imageFilteringAfterWarpingOnly) {
            changed = true;
        }
        
        if (imageFilteringMode != another.imageFilteringMode) {
            changed = true;
        }
        
        if (clippingMode != another.clippingMode) {
            changed = true;
        }
        if (warpingMode != another.warpingMode) {
            changed = true;
            
        }
        
        if (topMargin != another.topMargin) {
            changed = true;
        }
        
        if (bottomMargin != another.bottomMargin) {
            changed = true;
        }
        
        if (leftMargin != another.leftMargin) {
            changed = true;
        }
        
        if (rightMargin != another.rightMargin) {
            changed = true;
        }

        if (inheritanceMode != another.inheritanceMode) {
            changed = true;
        }
    
        if (superpositionOverlapPercentage != another.superpositionOverlapPercentage) {
            changed = true;
        }
     
        if (superpositionStartStep != another.superpositionStartStep) {
            changed = true;
        }
    
        if (interpolationMode != another.interpolationMode) {
            changed = true;
        }
        
        if (!changed) {
            ImageFilterOptionsModel model = getImageFilterOption(imageFilteringMode);
            ImageFilterOptionsModel modelAnother = another.getImageFilterOption(another.imageFilteringMode);
            
            if (model == null && modelAnother != null) {
                changed = true;
            } else if (model != null && model.isChanged(modelAnother)) {
                changed = true;
            }            
        }

        if (!changed) {
            SubPixelInterpolationOptionsModel model = getInterpolationOption(interpolationMode);
            SubPixelInterpolationOptionsModel modelAnother = another.getInterpolationOption(another.interpolationMode);
            
            if (model == null && modelAnother != null) {
                changed = true;
            } else if (model != null && model.isChanged(modelAnother)) {
                changed = true;
            }
            
            if (!changed && another != null && interpolationMode == another.interpolationMode && model.isCombinedModel()) {
                SubPixelInterpolationModeEnum[] modelSubModes = model.getSubInterpolationModes();
                SubPixelInterpolationModeEnum[] modelAnotherSubModes = model.getSubInterpolationModes();
                for (int i = 0; i < modelSubModes.length; i++) {
                    SubPixelInterpolationOptionsModel subModel = getInterpolationOption(modelSubModes[i]);
                    SubPixelInterpolationOptionsModel subModelAnother = another.getInterpolationOption(modelAnotherSubModes[i]);
                    
                    if (subModel == null && subModelAnother != null) {
                        changed = true;
                    } else if (subModel != null && subModel.isChanged(subModelAnother)) {
                        changed = true;
                    }

                    if (changed) {
                        break;
                    }
                }
            }
        }
        
        if (interpolationStartStep != another.interpolationStartStep) {
            changed = true;
        }
                
        if (stabilizationMode != another.stabilizationMode) {
            changed = true;
        }
        
        if (!changed) {
            VelocityStabilizationOptionsModel model = getStabilizationOption(stabilizationMode);;
            VelocityStabilizationOptionsModel modelAnother = another.getStabilizationOption(another.stabilizationMode);;
            
            if (model == null && modelAnother != null) {
                changed = true;
            } else if (model != null && model.isChanged(modelAnother)) {
                changed = true;
            }
        }

        if (validationReplaceInvalidByNaNs != another.validationReplaceInvalidByNaNs) {
            changed = true;
        }
        if (validationNumberOfValidatorIterations != another.validationNumberOfValidatorIterations) {
            changed = true;
        }
        if (validationIterateUntilNoMoreReplaced != another.validationIterateUntilNoMoreReplaced) {
            changed = true;
        }
        
        if (validationMode != another.validationMode) {
            changed = true;
        }
        
        if (!changed) {
            VelocityValidationOptionsModel model = getValidationOption(validationMode);;
            VelocityValidationOptionsModel modelAnother = another.getValidationOption(another.validationMode);;
            
            if (model == null && modelAnother != null) {
                changed = true;
            } else if (model != null && model.isChanged(modelAnother)) {
                changed = true;
            }
        }
        
        if (validationReplacementMode != another.validationReplacementMode) {
            changed = true;
        }

        return changed;
    }
}
