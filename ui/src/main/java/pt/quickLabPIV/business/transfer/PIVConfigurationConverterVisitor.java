package pt.quickLabPIV.business.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.IgnorePIVBaseDisplacementsModeEnum;
import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVMapOptionalConfiguration;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.exporter.MemoryCachedMATLABExporterOptionalConfiguration;
import pt.quickLabPIV.iareas.InterAreaDisplacementStableConfiguration;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaUnstableLogEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.replacement.SecondaryPeakReplacementConfiguration;
import pt.quickLabPIV.iareas.replacement.VectorReplacementFactoryEnum;
import pt.quickLabPIV.iareas.validation.CombinedValidatorAndReplacementConfiguration;
import pt.quickLabPIV.iareas.validation.DifferenceValidatorConfiguration;
import pt.quickLabPIV.iareas.validation.NormalizedMedianValidatorConfiguration;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.images.filters.GaussianFilter2DConfiguration;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;
import pt.quickLabPIV.interpolators.BiCubicInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Centroid2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.CompositeLastLevelInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian1DHongweiGuoInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian1DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DLinearRegressionInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DPolynomialInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DSubTypeFactoryEnum;
import pt.quickLabPIV.interpolators.LiuShenInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.LucasKanadeInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.OpticalFlowAfterPIVInterpolatorFactoryEnum;
import pt.quickLabPIV.jobs.validator.VectorValidatorConfiguration;
import pt.quickLabPIV.maximum.FindMaximumMultiPeaksConfiguration;
import pt.quickLabPIV.maximum.MaximumFinderFactoryEnum;
import pt.quickLabPIV.ui.models.DataExportConfigurationModel;
import pt.quickLabPIV.ui.models.Gaussian2DStrategiesEnum;
import pt.quickLabPIV.ui.models.IPIVConfigurationVisitor;
import pt.quickLabPIV.ui.models.ImageFilterOptionsGaussian2DModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;
import pt.quickLabPIV.ui.models.InheritanceModeEnum;
import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsBiCubicModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsCentroid2DModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian1DHongweiGuoModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian1DModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian1DPolynomialModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian2DLinearRegressionModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian2DModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian2DPolynomialModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLucasKanadeModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLucasKanadeOpenCLModel;
import pt.quickLabPIV.ui.models.VelocityStabilizationOptionsMaxDisplacementModel;
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsDifferenceModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsDifferenceOnlyModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsMultiPeakNormalizedMedianModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsNormalizedMedianModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsNormalizedMedianOnlyModel;
import pt.quickLabPIV.ui.models.WarpingModeEnum;

public class PIVConfigurationConverterVisitor implements IPIVConfigurationVisitor {
    private static Logger logger = LoggerFactory.getLogger(PIVConfigurationConverterVisitor.class);
    private PIVInputParameters pivParameters;
    private int subPixelInterpolationStartStep;
    
    public void setTargetForConversion(PIVInputParameters _pivParameters) {
        pivParameters = _pivParameters;
        pivParameters.clearSpecificConfigurations();
    }

    @Override
    public void visit(PIVConfigurationModel pivConfigurationModel) {
        if (pivConfigurationModel.isMaskEnabled()) {
            pivParameters.setMaskFilename(pivConfigurationModel.getMaskFile().getAbsolutePath());
            pivParameters.setMaskOnlyAtExport(pivConfigurationModel.isMaskOnlyAtExport());
        } else {
            pivParameters.setMaskFilename(null);
        }
        
        if (pivConfigurationModel.getInitialResolution() == InterrogationAreaResolutionEnum.IA0) {
            throw new UIException("Failed to process PIV", "Invalid PIV initial resolution");
        }

        if (pivConfigurationModel.getEndResolution() == InterrogationAreaResolutionEnum.IA0) {
            throw new UIException("Failed to process PIV", "Invalid PIV end resolution");
        }

        final int initialStepOrdinal = pivConfigurationModel.getInitialResolution().ordinal();
        final int numberOfSteps = initialStepOrdinal - pivConfigurationModel.getEndResolution().ordinal() + 1;                   
        
        /***
         * Velocity inheritance configuration
         */       
        pivParameters.setVelocityInheritanceStrategy(convertVelocityInheritanceToBusiness(pivConfigurationModel.getInheritanceMode()));
        
        /***
         * Super-position configuration
         */
        int superPositionStartStep = initialStepOrdinal - pivConfigurationModel.getSuperpositionStartStep().ordinal();
        if (pivConfigurationModel.getSuperpositionOverlapPercentage() == 0.0f || superPositionStartStep >= numberOfSteps) {
            pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.NoSuperPositionStrategy);
        } else if (superPositionStartStep == 0) {
            pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.SuperPositionStrategy);
            pivParameters.setSuperPositionIterationStepStart(0);
        } else {
            pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.MixedSuperPositionStrategy);
            pivParameters.setSuperPositionIterationStepStart(superPositionStartStep);
        } 
             
        /***
         * Sub-pixel interpolation configuration
         */
        pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.None);
        pivParameters.setInterpolatorStartStep(numberOfSteps);
        InterrogationAreaResolutionEnum subPixelStartStep = pivConfigurationModel.getInterpolationStartStep();
        subPixelInterpolationStartStep = initialStepOrdinal - subPixelStartStep.ordinal();

        /***
         * Margins configuration
         */
        pivParameters.setMarginPixelsITop(pivConfigurationModel.getTopMargin());
        pivParameters.setMarginPixelsIBottom(pivConfigurationModel.getBottomMargin());
        pivParameters.setMarginPixelsJLeft(pivConfigurationModel.getLeftMargin());
        pivParameters.setMarginPixelsJRight(pivConfigurationModel.getRightMargin());
        pivParameters.setImageHeightPixels(pivConfigurationModel.getImageHeight());
        pivParameters.setImageWidthPixels(pivConfigurationModel.getImageWidth());
        pivParameters.setOverlapFactor(1.0f - pivConfigurationModel.getSuperpositionOverlapPercentage()/100.0f);
        pivParameters.setInterrogationAreaStartIPixels(pivConfigurationModel.getInitialResolution().getSizeH());
        pivParameters.setInterrogationAreaEndIPixels(pivConfigurationModel.getEndResolution().getSizeH());
        pivParameters.setInterrogationAreaStartJPixels(pivConfigurationModel.getInitialResolution().getSizeW());
        pivParameters.setInterrogationAreaEndJPixels(pivConfigurationModel.getEndResolution().getSizeW());
        pivParameters.setNumberOfVelocityFrames(pivConfigurationModel.getNumberOfImages()); //Total number of velocity maps to be generated at end of processing

        if (pivConfigurationModel.getImageFilteringMode() != ImageFilteringModeEnum.DoNotApplyImageFiltering) {
            if (pivConfigurationModel.isImageFilteringAfterWarpingOnly()) {
                pivParameters.setImageFilteringMode(ImageFilteringModeFactoryEnum.ImageFilteringAfterWarping);
            } else {
                pivParameters.setImageFilteringMode(ImageFilteringModeFactoryEnum.ImageFilteringBeforeWarping);
            }
        } else {
            pivParameters.setImageFilteringMode(ImageFilteringModeFactoryEnum.NoImageFiltering);
        }
        if (pivConfigurationModel.getImageFilteringMode() == ImageFilteringModeEnum.ApplyImageFilteringGaussian2D) {
            pivParameters.setImageFilterMode(ImageFilterFactoryEnum.GaussianFiltering);
        } else {
            pivParameters.setImageFilterMode(ImageFilterFactoryEnum.NoFiltering);
        }
        
        /***
         * Out of bound clipping configuration
         */
        pivParameters.setClippingMode(convertClippingModeToBusiness(pivConfigurationModel.getClippingMode()));
        
        pivParameters.setWarpingMode(convertWarpingModeToBusiness(pivConfigurationModel.getWarpingMode()));
        
        /***
         * Area stabilization criteria configuration
         */ 
        pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Ignore);
        //pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Log);
        //pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.LogAndDump);
        pivParameters.setAreaUnstableDumpFilenamePrefix("unstable_dump");
        
        //Simple strategy will stabilize always after first iteration
        pivParameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
        
        int validatorMaxNumberOfIterations = pivConfigurationModel.getValidationNumberOfValidatorIterations();
        boolean validatorIterateUntilNoMoreReplaced = pivConfigurationModel.isValidationIterateUntilNoMoreReplaced();
        VectorValidatorConfiguration validatorConfig = new VectorValidatorConfiguration(validatorMaxNumberOfIterations, validatorIterateUntilNoMoreReplaced);
        pivParameters.setSpecificConfiguration(VectorValidatorConfiguration.IDENTIFIER, validatorConfig);
        
        boolean validatorMarkInvalidAsNaNs = false;
        if (pivConfigurationModel.getValidationMode() != VelocityValidationModeEnum.Disabled) { 
            validatorMarkInvalidAsNaNs = pivConfigurationModel.isValidationReplaceInvalidByNaNs();
        }
        //TODO Temporary solution, however refactorization is expected as DataExportConfiguration grows with future releases -> move to own Visitor
        DataExportConfigurationModel dataExportModel = pivConfigurationModel.getParent().getExportConfiguration();
        boolean swapUVOrder = dataExportModel.isSwapUVOrder();
        int mapsPerFile = 0;
        if (dataExportModel.isSplitExports()) {
            mapsPerFile = dataExportModel.getNumberOfPIVMapsPerExportedFile();
        }
        PIVMapOptionalConfiguration pivMapConfig = new PIVMapOptionalConfiguration(validatorMarkInvalidAsNaNs, swapUVOrder, mapsPerFile);
        pivParameters.setSpecificConfiguration(PIVMapOptionalConfiguration.IDENTIFIER, pivMapConfig);
        MemoryCachedMATLABExporterOptionalConfiguration memExporterConfig = new MemoryCachedMATLABExporterOptionalConfiguration(validatorMarkInvalidAsNaNs);
        pivParameters.setSpecificConfiguration(MemoryCachedMATLABExporterOptionalConfiguration.IDENTIFIER, memExporterConfig);
        
        //Default PIV velocity vector validation and replacement strategies... will be overridden if required.
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.None);
        pivParameters.setVectorReplacementStrategy(VectorReplacementFactoryEnum.None);

        //Default Optical Flow after PIV is completed configuration... will be overridden if required.
        pivParameters.setOpticalFlowAfterPIVStrategy(OpticalFlowAfterPIVInterpolatorFactoryEnum.None);
        pivParameters.setDenseExport(false);
    }

    @Override
    public void visit(ImageFilterOptionsGaussian2DModel imageFilterOptionsGaussian2DModel) {
        pivParameters.setImageFilterMode(ImageFilterFactoryEnum.GaussianFiltering);
        final float sigma = imageFilterOptionsGaussian2DModel.getSigma();
        final int widthPx = imageFilterOptionsGaussian2DModel.getWidthPx();
        GaussianFilter2DConfiguration config = new GaussianFilter2DConfiguration(sigma, widthPx);
        pivParameters.setSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER, config);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsBiCubicModel subPixelInterpolationOptionsBiCubicModel) {
        //BiCubica
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.BiCubic);
        }
        BiCubicInterpolatorConfiguration bicubicConfig = new BiCubicInterpolatorConfiguration();
        int numberOfDecimalPoints=subPixelInterpolationOptionsBiCubicModel.getNumberOfDecimalPoints(); //100
        int numberOfPixels=subPixelInterpolationOptionsBiCubicModel.getNumberOfPixels(); //5
        bicubicConfig.setProperties(numberOfDecimalPoints, numberOfPixels);
        pivParameters.setSpecificConfiguration(BiCubicInterpolatorConfiguration.IDENTIFIER, bicubicConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsGaussian1DModel subPixelInterpolationOptionsGaussian1DModel) {        
        //Gaussiana
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);
        }
        Gaussian1DInterpolatorConfiguration gaussianConfig = new Gaussian1DInterpolatorConfiguration();
        //Default should be like 7 - Not recommended to use 5 point interpolation may take long time
        int numberOfPixels=subPixelInterpolationOptionsGaussian1DModel.getNumberOfPixels(); 
        gaussianConfig.setInterpolationPixels(numberOfPixels);
        pivParameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, gaussianConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }
    
    @Override
    public void visit(SubPixelInterpolationOptionsGaussian1DHongweiGuoModel model) {        
        //Robust Gaussian iterative LSM method - Hongwei Guo
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1DHongweiGuo);
        }
        Gaussian1DHongweiGuoInterpolatorConfiguration gaussianConfig = new Gaussian1DHongweiGuoInterpolatorConfiguration();
        int numberOfPixels=model.getNumberOfPixels();
        int numberOfIterations=model.getNumberOfIterations();
        gaussianConfig.setInterpolationPixels(numberOfPixels);
        gaussianConfig.setInteporlationIterations(numberOfIterations);
        pivParameters.setSpecificConfiguration(Gaussian1DHongweiGuoInterpolatorConfiguration.IDENTIFIER, gaussianConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsGaussian1DPolynomialModel subPixelInterpolationOptionsGaussian1DPolynomialModel) {
        //Gaussiana Raffel 1D-1D
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1DPolynomial);
        }
                
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsCentroid2DModel subPixelInterpolationOptionsCentroid2DModel) {        
        //Centroid 2D n-point
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Centroid2D);
        }
        Centroid2DInterpolatorConfiguration centroidConfig = new Centroid2DInterpolatorConfiguration();
        int numberOfPixels = subPixelInterpolationOptionsCentroid2DModel.getNumberOfPixels(); 
        centroidConfig.setInterpolationPixels(numberOfPixels);
        pivParameters.setSpecificConfiguration(Centroid2DInterpolatorConfiguration.IDENTIFIER, centroidConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }
    
    @Override
    public void visit(SubPixelInterpolationOptionsGaussian2DModel subPixelInterpolationOptionsGaussian2DModel) {
        //Gaussiana 2D
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
        }
        Gaussian2DInterpolatorConfiguration gaussianConfig = new Gaussian2DInterpolatorConfiguration();
        int numberOfPixelsX = subPixelInterpolationOptionsGaussian2DModel.getNumberOfPixelsInX();
        int numberOfPixelsY = subPixelInterpolationOptionsGaussian2DModel.getNumberOfPixelsInY();
        Gaussian2DSubTypeFactoryEnum subType = convertSubTypeToBusiness(subPixelInterpolationOptionsGaussian2DModel.getAlgorithm());
        //Gaussian2DSubTypeFactoryEnum subType = Gaussian2DSubTypeFactoryEnum.EllipticalWithRotation;
        gaussianConfig.setProperties(numberOfPixelsX, numberOfPixelsY, subType);
        //gaussianConfig.setLogResults(true);
        pivParameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, gaussianConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsGaussian2DPolynomialModel subPixelInterpolationOptionsGaussian2DPolynomialModel) {        
        //Gaussian 2D polynomial with n-point 2D Centroid back up sub-pixel interpolation
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2DPolynomial);
        }
        Gaussian2DPolynomialInterpolatorConfiguration gaussianConfig = new Gaussian2DPolynomialInterpolatorConfiguration();
        int numberOfPixels = subPixelInterpolationOptionsGaussian2DPolynomialModel.getNumberOfPixels(); 
        gaussianConfig.setInterpolationPixelsForCentroid2D(numberOfPixels);
        pivParameters.setSpecificConfiguration(Gaussian2DPolynomialInterpolatorConfiguration.IDENTIFIER, gaussianConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(SubPixelInterpolationOptionsGaussian2DLinearRegressionModel subPixelInterpolationOptionsGaussian2DLinearRegressionModel) {        
        //Gaussian 2D polynomial with n-point 2D Centroid back up sub-pixel interpolation
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2DLinearRegression);
        }
        Gaussian2DLinearRegressionInterpolatorConfiguration gaussianConfig = new Gaussian2DLinearRegressionInterpolatorConfiguration();
        int numberOfPixels = subPixelInterpolationOptionsGaussian2DLinearRegressionModel.getNumberOfPixels(); 
        gaussianConfig.setInterpolationPixels(numberOfPixels);
        pivParameters.setSpecificConfiguration(Gaussian2DLinearRegressionInterpolatorConfiguration.IDENTIFIER, gaussianConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }
    
    @Override
    public void visit(SubPixelInterpolationOptionsLucasKanadeModel subPixelInterpolationOptionsLucasKanadeModel) {
        //Lucas-Kanade sparse optical-flow sub-pixel interpolation
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.LucasKanade);
        }
        
        boolean ignorePIVBaseDisplacements = subPixelInterpolationOptionsLucasKanadeModel.isIgnorePIVBaseDisplacements();        
        
        float filterSigma = subPixelInterpolationOptionsLucasKanadeModel.getFilterSigma();
        int filterWidthPx = subPixelInterpolationOptionsLucasKanadeModel.getFilterWidthPx();
        boolean averageMode = subPixelInterpolationOptionsLucasKanadeModel.isAverageOfFourPixels();
        int windowSize = subPixelInterpolationOptionsLucasKanadeModel.getWindowSize();
        int iterations = subPixelInterpolationOptionsLucasKanadeModel.getNumberOfIterations();
        
        IgnorePIVBaseDisplacementsModeEnum businessIgnorePIVMode = 
                convertIgnorePIVModeEnumToBussinessEnum(subPixelInterpolationOptionsLucasKanadeModel.getIgnorePIVBaseDisplacementsMode());;
        
        LucasKanadeInterpolatorConfiguration lkConfig = new LucasKanadeInterpolatorConfiguration();
        lkConfig.setIgnorePIVBaseDisplacements(ignorePIVBaseDisplacements);
        lkConfig.setAverageOfFourPixels(averageMode);
        lkConfig.setFilterSigma(filterSigma);
        lkConfig.setFilterWidthPx(filterWidthPx);
        lkConfig.setWindowSize(windowSize);
        lkConfig.setNumberOfIterations(iterations);
        lkConfig.setIgnorePIVBaseDisplacementsMode(businessIgnorePIVMode);
        pivParameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER, lkConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(
            SubPixelInterpolationOptionsLucasKanadeOpenCLModel subPixelInterpolationOptionsLucasKanadeOpenCLModel) {
        //Lucas-Kanade sparse optical-flow sub-pixel interpolation
        boolean strategyNotSetAtStart = false;
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.LucasKanadeAparapi);
            strategyNotSetAtStart = true;
        }
        
        boolean ignorePIVBaseDisplacements = subPixelInterpolationOptionsLucasKanadeOpenCLModel.isIgnorePIVBaseDisplacements();
        
        float filterSigma = subPixelInterpolationOptionsLucasKanadeOpenCLModel.getFilterSigma();
        int filterWidthPx = subPixelInterpolationOptionsLucasKanadeOpenCLModel.getFilterWidthPx();
        boolean averageMode = subPixelInterpolationOptionsLucasKanadeOpenCLModel.isAverageOfFourPixels();
        int windowSize = subPixelInterpolationOptionsLucasKanadeOpenCLModel.getWindowSize();
        int iterations = subPixelInterpolationOptionsLucasKanadeOpenCLModel.getNumberOfIterations();
        boolean denseExport = subPixelInterpolationOptionsLucasKanadeOpenCLModel.isDenseExport();

        IgnorePIVBaseDisplacementsModeEnum businessIgnorePIVMode = 
                convertIgnorePIVModeEnumToBussinessEnum(subPixelInterpolationOptionsLucasKanadeOpenCLModel.getIgnorePIVBaseDisplacementsMode());

        LucasKanadeInterpolatorConfiguration lkConfig = new LucasKanadeInterpolatorConfiguration();
        lkConfig.setIgnorePIVBaseDisplacements(ignorePIVBaseDisplacements);
        lkConfig.setIgnorePIVBaseDisplacementsMode(businessIgnorePIVMode);
        lkConfig.setAverageOfFourPixels(averageMode);
        lkConfig.setFilterSigma(filterSigma);
        lkConfig.setFilterWidthPx(filterWidthPx);
        lkConfig.setWindowSize(windowSize);
        lkConfig.setNumberOfIterations(iterations);
        lkConfig.setDenseExport(denseExport);
        
        pivParameters.setSpecificConfiguration(LucasKanadeInterpolatorConfiguration.IDENTIFIER_APARAPI, lkConfig);
        pivParameters.setDenseExport(denseExport);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
        if (strategyNotSetAtStart && denseExport) {
            pivParameters.setOpticalFlowAfterPIVStrategy(OpticalFlowAfterPIVInterpolatorFactoryEnum.LucasKanadeAparapi);
        }
    }
    
    @Override
    public void visit(
            SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel subPixelInterpolationOptionsModel) {
        SubPixelInterpolationModeEnum[] subInterpolationBusinessLogicModes = subPixelInterpolationOptionsModel.getSubInterpolationModes();
        
        if (subPixelInterpolationOptionsModel.isApplyFinalInterpolationAsLastPIVProcessingStep()) {
            pivParameters.setOpticalFlowAfterPIVStrategy(SubPixelInterpolatorConverter.convertToBusinessLogicOF(subInterpolationBusinessLogicModes[1]));   
        } else {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.CompositeLastLevelInterpolator);
            boolean applyAtLastStep = subPixelInterpolationOptionsModel.isAlsoApplyMainInterpolationOnLastStep();
            CompositeLastLevelInterpolatorConfiguration config = new CompositeLastLevelInterpolatorConfiguration();
            config.setFirstLevelInterpolator(SubPixelInterpolatorConverter.convertToBusinessLogic(subInterpolationBusinessLogicModes[0]));
            config.setLastLevelInterpolator(SubPixelInterpolatorConverter.convertToBusinessLogic(subInterpolationBusinessLogicModes[1]));
            config.setApplyMainInterpolatorAtLastStep(applyAtLastStep);
            pivParameters.setSpecificConfiguration(CompositeLastLevelInterpolatorConfiguration.IDENTIFIER, config);
        }
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(
            SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel subPixelInterpolationOptionsLiuShenWithLucasKanadeModel) {
        //Liu-Shen combined with Lucas-Kanade sparse optical-flow sub-pixel interpolation
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.LiuShenWithLucasKanade);
        }
        
        boolean ignorePIVBaseDisplacements = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.isIgnorePIVBaseDisplacements();
        
        float filterSigmaLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getFilterSigmaLK();
        int filterWidthPxLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getFilterWidthPxLK();
        int windowSizeLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getWindowSizeLK();
        int iterationsLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getNumberOfIterationsLK();

        float filterSigmaLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getFilterSigmaLS();
        int filterWidthPxLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getFilterWidthPxLS();
        float multiplierLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getMultiplierLS();
        int vectorsWindowSizeLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getVectorsWindowSizeLS();
        int iterationsLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getNumberOfIterationsLS();
        
        IgnorePIVBaseDisplacementsModeEnum businessIgnorePIVMode = 
                convertIgnorePIVModeEnumToBussinessEnum(subPixelInterpolationOptionsLiuShenWithLucasKanadeModel.getIgnorePIVBaseDisplacementsMode());
        
        LiuShenInterpolatorConfiguration lsLkConfig = new LiuShenInterpolatorConfiguration();
        lsLkConfig.setFilterSigmaLK(filterSigmaLK);
        lsLkConfig.setFilterWidthPxLK(filterWidthPxLK);
        lsLkConfig.setWindowSizeLK(windowSizeLK);
        lsLkConfig.setNumberOfIterationsLK(iterationsLK);
        //----------------------------------------------
        lsLkConfig.setFilterSigmaLS(filterSigmaLS);
        lsLkConfig.setFilterWidthPxLS(filterWidthPxLS);
        lsLkConfig.setMultiplierLagrangeLS(multiplierLS);
        lsLkConfig.setVectorsWindowSizeLS(vectorsWindowSizeLS);
        lsLkConfig.setNumberOfIterationsLS(iterationsLS);
        //----------------------------------------------
        lsLkConfig.setIgnorePIVBaseDisplacements(ignorePIVBaseDisplacements);
        lsLkConfig.setIgnorePIVBaseDisplacementsMode(businessIgnorePIVMode);
        
        pivParameters.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsLkConfig);
        
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
    }

    @Override
    public void visit(
            SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel) {
        //Liu-Shen combined with Lucas-Kanade sparse/dense optical-flow sub-pixel interpolation
        boolean strategyNotSetAtStart = false;
        if (pivParameters.getInterpolationStrategy() == CrossCorrelationInterpolatorFactoryEnum.None) {
            strategyNotSetAtStart = true;
            pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.LiuShenWithLucasKanadeAparapi);
        }

        boolean ignorePIVBaseDisplacements = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.isIgnorePIVBaseDisplacements();
        
        float filterSigmaLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getFilterSigmaLK();
        int filterWidthPxLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getFilterWidthPxLK();
        int windowSizeLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getWindowSizeLK();
        int iterationsLK = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getNumberOfIterationsLK();

        float filterSigmaLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getFilterSigmaLS();
        int filterWidthPxLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getFilterWidthPxLS();
        float multiplierLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getMultiplierLS();
        int vectorsWindowSizeLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getVectorsWindowSizeLS();
        int iterationsLS = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getNumberOfIterationsLS();
        boolean denseExport = subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.isDenseExport();
        
        IgnorePIVBaseDisplacementsModeEnum businessIgnorePIVMode;
        businessIgnorePIVMode = 
                convertIgnorePIVModeEnumToBussinessEnum(subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel.getIgnorePIVBaseDisplacementsMode());
        
        LiuShenInterpolatorConfiguration lsLkConfig = new LiuShenInterpolatorConfiguration();
        
        lsLkConfig.setFilterSigmaLK(filterSigmaLK);
        lsLkConfig.setFilterWidthPxLK(filterWidthPxLK);
        lsLkConfig.setWindowSizeLK(windowSizeLK);
        lsLkConfig.setNumberOfIterationsLK(iterationsLK);
        //----------------------------------------------
        lsLkConfig.setFilterSigmaLS(filterSigmaLS);
        lsLkConfig.setFilterWidthPxLS(filterWidthPxLS);
        lsLkConfig.setMultiplierLagrangeLS(multiplierLS);
        lsLkConfig.setVectorsWindowSizeLS(vectorsWindowSizeLS);
        lsLkConfig.setNumberOfIterationsLS(iterationsLS);
        //
        lsLkConfig.setDenseVectors(denseExport);
        lsLkConfig.setIgnorePIVBaseDisplacements(ignorePIVBaseDisplacements);
        lsLkConfig.setIgnorePIVBaseDisplacementsMode(businessIgnorePIVMode);
        
        pivParameters.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER_APARAPI, lsLkConfig);
        
        pivParameters.setDenseExport(denseExport);
        pivParameters.setInterpolatorStartStep(subPixelInterpolationStartStep);
        
        if (strategyNotSetAtStart && denseExport) {
            pivParameters.setOpticalFlowAfterPIVStrategy(OpticalFlowAfterPIVInterpolatorFactoryEnum.LiuShenAparapi);
        }
    }

    @Override
    public void visit(VelocityStabilizationOptionsMaxDisplacementModel velocityStabilizationOptionsMaxDisplacementModel) {
        float maxDisplacementPixels = velocityStabilizationOptionsMaxDisplacementModel.getMaxDisplacementPixels();
        int maxIterationSteps = velocityStabilizationOptionsMaxDisplacementModel.getMaxIterations();
        
        //Max. Displacement strategy - Interrogation Area stabilizes when displacement norm is below the specified value,
        //or when the maximum number of repetitions is reached.
        InterAreaDisplacementStableConfiguration stableConfig = 
                new InterAreaDisplacementStableConfiguration(maxDisplacementPixels, maxIterationSteps);
        pivParameters.setSpecificConfiguration(InterAreaDisplacementStableConfiguration.IDENTIFIER, stableConfig);
        pivParameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.MaxDisplacementStrategy);        
    }

    @Override
    public void visit(VelocityValidationOptionsDifferenceOnlyModel model) {
        float distanceThresholdPixels = model.getDistanceThresholdPixels();
        
        CombinedValidatorAndReplacementConfiguration[] config = new CombinedValidatorAndReplacementConfiguration[1];
        DifferenceValidatorConfiguration differenceConfig =
                new DifferenceValidatorConfiguration(distanceThresholdPixels);
        config[0] = new CombinedValidatorAndReplacementConfiguration(
                VectorValidatorFactoryEnum.DifferenceValidator, differenceConfig, VectorReplacementFactoryEnum.None, null);
        
        pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, config);
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.CombinedValidator);
        MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderFromCenter;
        pivParameters.setMaximumFinderStrategy(maxStrategy);
    }
    
    @Override
    public void visit(VelocityValidationOptionsDifferenceModel velocityValidationOptionsDifferenceModel) {
        float distanceThresholdPixels = velocityValidationOptionsDifferenceModel.getDistanceThresholdPixels();
        
        CombinedValidatorAndReplacementConfiguration[] config = new CombinedValidatorAndReplacementConfiguration[1];
        DifferenceValidatorConfiguration differenceConfig =
                new DifferenceValidatorConfiguration(distanceThresholdPixels);
        config[0] = new CombinedValidatorAndReplacementConfiguration(
                VectorValidatorFactoryEnum.DifferenceValidator, differenceConfig, VectorReplacementFactoryEnum.Bilinear, null);
        
        pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, config);
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.CombinedValidator);
        MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderFromCenter;
        pivParameters.setMaximumFinderStrategy(maxStrategy);
    }

    @Override
    public void visit(VelocityValidationOptionsNormalizedMedianOnlyModel model) {
        float distanceThresholdPixels = model.getDistanceThresholdPixels();
        float epsilon0 = model.getEpsilon0();
        
        CombinedValidatorAndReplacementConfiguration[] config = new CombinedValidatorAndReplacementConfiguration[1];
        NormalizedMedianValidatorConfiguration normConfig =
                new NormalizedMedianValidatorConfiguration(distanceThresholdPixels, epsilon0);
        config[0] = new CombinedValidatorAndReplacementConfiguration(
                VectorValidatorFactoryEnum.MedianValidator, normConfig, VectorReplacementFactoryEnum.None, null);

        MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderFromCenter;
        pivParameters.setMaximumFinderStrategy(maxStrategy);
        pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, config);
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.CombinedValidator);        
    }
    
    @Override
    public void visit(VelocityValidationOptionsNormalizedMedianModel velocityValidationOptionsNormalizedMedianModel) {
        float distanceThresholdPixels = velocityValidationOptionsNormalizedMedianModel.getDistanceThresholdPixels();
        float epsilon0 = velocityValidationOptionsNormalizedMedianModel.getEpsilon0();
        
        CombinedValidatorAndReplacementConfiguration[] config = new CombinedValidatorAndReplacementConfiguration[1];
        NormalizedMedianValidatorConfiguration normConfig =
                new NormalizedMedianValidatorConfiguration(distanceThresholdPixels, epsilon0);
        config[0] = new CombinedValidatorAndReplacementConfiguration(
                VectorValidatorFactoryEnum.MedianValidator, normConfig, VectorReplacementFactoryEnum.Bilinear, null);

        MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderFromCenter;
        pivParameters.setMaximumFinderStrategy(maxStrategy);
        pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, config);
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.CombinedValidator);
    }

    @Override
    public void visit(VelocityValidationOptionsMultiPeakNormalizedMedianModel velocityValidationOptionsMultiPeakNormalizedMedianModel) {
        int numberOfPeaks = velocityValidationOptionsMultiPeakNormalizedMedianModel.getNumberOfPeaks(); 
        int kernelSize = velocityValidationOptionsMultiPeakNormalizedMedianModel.getKernelSize();
        float distanceThresholdPixels = velocityValidationOptionsMultiPeakNormalizedMedianModel.getDistanceThresholdPixels();
        float epsilon0 = velocityValidationOptionsMultiPeakNormalizedMedianModel.getEpsilon0();        
        
        CombinedValidatorAndReplacementConfiguration[] configs = new CombinedValidatorAndReplacementConfiguration[numberOfPeaks];
        int peakIndex = 0;

        for (; peakIndex < numberOfPeaks-1; peakIndex++) { 
            SecondaryPeakReplacementConfiguration secondaryPeakConfig = 
                    new SecondaryPeakReplacementConfiguration(peakIndex + 1);
            NormalizedMedianValidatorConfiguration normConfig =
                    new NormalizedMedianValidatorConfiguration(distanceThresholdPixels, epsilon0);
            configs[peakIndex] = new CombinedValidatorAndReplacementConfiguration(
                    VectorValidatorFactoryEnum.MedianValidator, normConfig, VectorReplacementFactoryEnum.SecondaryPeak, secondaryPeakConfig);
        }

        NormalizedMedianValidatorConfiguration normConfig =
                new NormalizedMedianValidatorConfiguration(distanceThresholdPixels, epsilon0);
        configs[peakIndex] = new CombinedValidatorAndReplacementConfiguration(
                VectorValidatorFactoryEnum.MedianValidator, normConfig, VectorReplacementFactoryEnum.Bilinear, null);
        
        FindMaximumMultiPeaksConfiguration multiPeakConfig =
        		new FindMaximumMultiPeaksConfiguration(numberOfPeaks, kernelSize);
        
        MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderMultiPeaks;
        pivParameters.setMaximumFinderStrategy(maxStrategy);
        pivParameters.setSpecificConfiguration(FindMaximumMultiPeaksConfiguration.IDENTIFIER, multiPeakConfig);
        pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, configs);
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.CombinedValidator);
    }
    
    private ClippingModeEnum convertClippingModeToBusiness(
            pt.quickLabPIV.ui.models.ClippingModeEnum clippingMode) {
        ClippingModeEnum result = null;
        
        if (clippingMode == null) {
            //Ensure a sane default when no option was set by the user, or in the loaded configuration file
            logger.warn("Enforcing default option of \"Allowed out of bound clipping\" for the Clipping mode");
            clippingMode = pt.quickLabPIV.ui.models.ClippingModeEnum.AllowedOutOfBoundClipping; 
        }
        
        switch (clippingMode) {
        case AllowedOutOfBoundClipping:
            result = ClippingModeEnum.AllowedOutOfBoundClipping;
            break;
        case LoggedOutOfBoundClipping:
            result = ClippingModeEnum.LoggedOutOfBoundClipping;
            break;
        case NoOutOfBoundClipping:
            result = ClippingModeEnum.NoOutOfBoundClipping;
            break;
        default:
            throw new UIException("Failed to convert UI configuration", "Unknown clipping mode");        
        }

        return result;
    }
    
    private WarpingModeFactoryEnum convertWarpingModeToBusiness(WarpingModeEnum warpingMode) {
        WarpingModeFactoryEnum result = null;
        
        if (warpingMode == null) {
            logger.error("Warping mode should not be null");
            throw new UIException("Failed to convert UI configuration", "Warping mode should not be empty");
        }
        
        switch (warpingMode) {
        case NoWarping:
            result = WarpingModeFactoryEnum.NoImageWarping;
            break;
        case BiLinearMicroWarping1stImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.FirstImageBiLinearMicroWarping;
            break;
        case BiLinearMicroWarping2ndImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.SecondImageBiLinearMicroWarping;
            break;
        case BiLinearMicroWarpingBothImagesWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.BothImagesBiLinearMicroWarping;
            break;
        case BiLinearMiniWarping1stImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.FirstImageBiLinearMiniWarping;
            break;
        case BiLinearMiniWarping2ndImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.SecondImageBiLinearMiniWarping;
            break;
        case BiLinearMiniWarpingBothImagesWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.BothImagesBiLinearMiniWarping;
            break;
        case BiLinearWarping1stImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.FirstImageBiLinearWarping;
            break;
        case BiLinearWarping2ndImageWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.SecondImageBiLinearWarping;
            break;
        case BiLinearWarpingBothImagesWithBiCubicSplineInterpolation:
            result = WarpingModeFactoryEnum.BothImagesBiLinearWarping;
            break;
        default:
            throw new UIException("Failed to convert UI configuration", "Unknown warping mode");
        }
        
        return result;
    }
    
    private InterAreaVelocityStrategiesFactoryEnum convertVelocityInheritanceToBusiness(
            InheritanceModeEnum inheritanceMode) {
        InterAreaVelocityStrategiesFactoryEnum result = null;
        
        switch (inheritanceMode) {
        case Area:
            result = InterAreaVelocityStrategiesFactoryEnum.Area;
            break;
        case Distance:
            result = InterAreaVelocityStrategiesFactoryEnum.Distance;
            break;
        case BiCubicSpline:
            result = InterAreaVelocityStrategiesFactoryEnum.BiCubicSpline;
            break;
        case Invalid:
            throw new UIException("Failed to process PIV", "Invalid PIV parameters - Invalid velocity inheritance algorithm");
        default:
            throw new UIException("Failed to process PIV", 
                    "Invalid PIV parameters - Unknonwn velocity inheritance algorithm: " + inheritanceMode);
        };
        
        return result;
    }
        
    private Gaussian2DSubTypeFactoryEnum convertSubTypeToBusiness(Gaussian2DStrategiesEnum algorithm) {
        Gaussian2DSubTypeFactoryEnum result = null;
        
        switch (algorithm) {
        case Assymetric:
            result = Gaussian2DSubTypeFactoryEnum.Assymmetric;
            break;
        case AssymetricWithRotation:
            result = Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation;
            break;
        case Invalid:
            throw new UIException("Failed to process PIV", "Invalid PIV parameters - Invalid sub pixel interpolation algorithm");
        case Symmetric:
            result = Gaussian2DSubTypeFactoryEnum.Symmetric; 
            break;
        default:
            throw new UIException("Failed to process PIV", "Invalid PIV parameters - Unknonwn sub pixel interpolation algorithm: " + algorithm);
        }

        return result;
    }

    private IgnorePIVBaseDisplacementsModeEnum convertIgnorePIVModeEnumToBussinessEnum(pt.quickLabPIV.ui.models.IgnorePIVBaseDisplacementsModeEnum mode) {
        IgnorePIVBaseDisplacementsModeEnum businessIgnorePIVMode;
        switch (mode) {
        case IgnoreU:
            businessIgnorePIVMode = IgnorePIVBaseDisplacementsModeEnum.IgnoreU;
            break;
        case IgnoreV:
            businessIgnorePIVMode = IgnorePIVBaseDisplacementsModeEnum.IgnoreV;
            break;
        case IgnoreUV:
            businessIgnorePIVMode = IgnorePIVBaseDisplacementsModeEnum.IgnoreUV;
            break;
        case IgnoreAuto:
            businessIgnorePIVMode = IgnorePIVBaseDisplacementsModeEnum.Auto;
            break;
        case IgnoreAutoSmall:
            businessIgnorePIVMode = IgnorePIVBaseDisplacementsModeEnum.AutoSmall;
            break;
        default:
            throw new UIException("PIV Conviguration converter", "Unknown PIV ignore displacements mode: " + mode);
        }
        return businessIgnorePIVMode;
    }
}
