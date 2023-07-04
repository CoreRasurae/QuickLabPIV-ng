package pt.quickLabPIV.business.facade;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageFilterOptionsGaussian2DModel;
import pt.quickLabPIV.ui.models.ImageFilterOptionsModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;
import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.PIVImageTypeEnum;
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
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsModel;
import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;
import pt.quickLabPIV.ui.models.VelocityStabilizationOptionsMaxDisplacementModel;
import pt.quickLabPIV.ui.models.VelocityStabilizationOptionsModel;
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsDifferenceModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsDifferenceOnlyModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsMultiPeakNormalizedMedianModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsNormalizedMedianModel;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsNormalizedMedianOnlyModel;
import pt.quickLabPIV.ui.views.panels.SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel;

public class PIVConfigurationFacade {
    public static final long BYTES_4GB = 4294967296L;
    
    public static List<List<String>> getFileList(PIVConfigurationModel model) {
        final String patternA = model.getImagePatternA();
        final String patternB = model.getImagePatternB();
        final PIVImageTypeEnum imageType = model.getImageType();
        final File folder = model.getSourceImageFolder();
        final File firstFile = model.getSourceImageFile();
        
        if (patternA == null) {
            return Collections.emptyList();
        }
        
        Pattern compiledPatternA = null;
        try {
            compiledPatternA = Pattern.compile(patternA);
        } catch (PatternSyntaxException ex) {
            return Collections.emptyList();
        }
        
        String[] filesA = folder.list(new PatternFilenameFilter(compiledPatternA));
        String[] filesB = null;
        if (imageType == PIVImageTypeEnum.PIVImagePair) {
            Pattern compiledPatternB = null;
            
            if (patternB == null) {
                return Collections.emptyList();
            }

            try {
                compiledPatternB = Pattern.compile(patternB);
            } catch (PatternSyntaxException ex) {
                return Collections.emptyList();
            }

            filesB = folder.list(new PatternFilenameFilter(compiledPatternB));
        }
             
        if (filesA == null) {
            return Collections.emptyList();
        }
        
        Arrays.sort(filesA);
        List<String> candidateFilesListA = Arrays.asList(filesA);
        int index = candidateFilesListA.indexOf(firstFile.getName());        
        if (index < 0) {
            return Collections.emptyList();
        }
        List<String> selectedFilesListA = candidateFilesListA.subList(index, candidateFilesListA.size());
        
        if (filesB != null) {
            Arrays.sort(filesB);
        }

        List<List<String>> result = null;
        if (imageType == PIVImageTypeEnum.PIVImagePair) {
            if (filesA.length != filesB.length) {
                return Collections.emptyList();
            }
            
            List<String> candidateFilesListB = Arrays.asList(filesB);
            List<String> selectedFilesListB = candidateFilesListB.subList(index, filesB.length);
            
            result = new ArrayList<List<String>>(2);
            result.add(selectedFilesListA);
            result.add(selectedFilesListB);
        } else {
            result = new ArrayList<List<String>>(1);
            result.add(selectedFilesListA);            
        }
        
        return result;
    }

    public static List<List<File>> getFileListForProcessing(PIVConfigurationModel model) {
        IFileFactory factory = model.getParent().getParent().getFileFactory();
        List<List<String>> filesByName = getFileList(model);      
        
        List<List<File>> files = new ArrayList<>(2);
        if (model.getImageType() == PIVImageTypeEnum.PIVImageSequence) {
            if (filesByName.size() != 1) {
                throw new UIException("PIV configuration error", 
                        "Please check input image files.\nPIV Image sequence should receive a single file list only.");
            }
            
            List<String> sequenceStringFiles = filesByName.get(0);
            if (sequenceStringFiles.size() <= model.getNumberOfImages()) {
                throw new UIException("PIV configuration error", 
                        "Please check input image files.\nNumber of PIV Images is insufficient for configured total images to process.");                
            }
            
            List<String> sequenceStringFilesA = sequenceStringFiles.subList(0, model.getNumberOfImages());
            List<String> sequenceStringFilesB = sequenceStringFiles.subList(1, model.getNumberOfImages() + 1);

            List<File> sequenceFilesA = new ArrayList<File>(model.getNumberOfImages());
            List<File> sequenceFilesB = new ArrayList<File>(model.getNumberOfImages());

            for (String filename : sequenceStringFilesA) {
                sequenceFilesA.add(factory.createFile(model.getSourceImageFolder(), filename));
            }
            
            for (String filename : sequenceStringFilesB) {
                sequenceFilesB.add(factory.createFile(model.getSourceImageFolder(), filename));
            }
            
            files.add(sequenceFilesA);
            files.add(sequenceFilesB);            
        } else {
            if (filesByName.size() != 2) {
                throw new UIException("PIV configuration error", 
                        "Please check input image files.\nPIV Image pair should receive a double file list only");
            }
            
            List<String> sequenceStringFilesA = filesByName.get(0);
            List<String> sequenceStringFilesB = filesByName.get(1);
            
            if (sequenceStringFilesA.size() < model.getNumberOfImages()) {
                throw new UIException("PIV configuration error", 
                        "Please check input image files.\nNumber of PIV Images A is insufficient for configured total images to process.");                
            }
            
            if (sequenceStringFilesB.size() < model.getNumberOfImages()) {
                throw new UIException("PIV configuration error", 
                        "Please check input image files.\nNumber of PIV Images B is insufficient for configured total images to process.");                
            }

            sequenceStringFilesA = sequenceStringFilesA.subList(0, model.getNumberOfImages());
            sequenceStringFilesB = sequenceStringFilesB.subList(0, model.getNumberOfImages());

            List<File> sequenceFilesA = new ArrayList<File>(model.getNumberOfImages());
            List<File> sequenceFilesB = new ArrayList<File>(model.getNumberOfImages());
            for (String filename : sequenceStringFilesA) {
                sequenceFilesA.add(factory.createFile(model.getSourceImageFolder(), filename)); 
            }
            for (String filename : sequenceStringFilesB) {
                sequenceFilesB.add(factory.createFile(model.getSourceImageFolder(), filename));
            }
            
            files.add(sequenceFilesA);
            files.add(sequenceFilesB);
        }
        
        return files;
    }
    
    public static AppContextModel createSubPixelInterpolationOptionsModelForOption(AppContextModel model, SubPixelInterpolationModeEnum option) {
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(model);
        SubPixelInterpolationOptionsModel interpModel = pivModel.getInterpolationOption(option);
                
        switch (option) {
        case BiCubic:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsBiCubicModel();
            }
            break;
        case Gaussian1D:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian1DModel();
            }
            break;
        case Gaussian1DHongweiGuo:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian1DHongweiGuoModel();
            }
            break;
        case Gaussian1DPolynomial:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian1DPolynomialModel();
            }
            break;
        case Centroid2D:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsCentroid2DModel();
            }
            break;        
        case Gaussian2D:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian2DModel();
            }
            break;
        case Gaussian2DPolynomial:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian2DPolynomialModel();
            }
            break;            
        case Gaussian2DLinearRegression:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {           
                interpModel = new SubPixelInterpolationOptionsGaussian2DLinearRegressionModel();
            }
            break;
        case LucasKanade:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {
                interpModel = new SubPixelInterpolationOptionsLucasKanadeModel();
            }
            break;
        case LucasKanadeOpenCL:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {
                interpModel = new SubPixelInterpolationOptionsLucasKanadeOpenCLModel();
            }
            break;
        case CombinedBaseAndFinalInterpolator:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {
                interpModel = new SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel();
            }
            break;
        case LiuShenWithLucasKanade:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {
                interpModel = new SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel();
            }
            break;
        case LiuShenWithLucasKanadeOpenCL:
            if (interpModel == null || interpModel.getInterpolationMode() != option) {
                interpModel = new SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel();
            }
            break;
        case Disabled:
            //Make no change on invalid model
            break;
        default:
            throw new UIException("Sub Pixel Interpolation option","Cannot create model for sub pixel interpolation mode: "  + option);        
        }        
        
        pivModel.setInterpolationOption(interpModel);
        
        return model;
    }

    public static AppContextModel createVelocityStabilizationOptionsModelForOption(AppContextModel model,
            VelocityStabilizationModeEnum option) {
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(model);
        VelocityStabilizationOptionsModel stabilizationModel = pivModel.getStabilizationOption(option);     
        
        switch (option) {
        case Disabled:
            //Make no change on invalid model
            break;
        case MaxDisplacement:
            if (stabilizationModel == null || stabilizationModel.getStabilizationMode() != option) {
                stabilizationModel = new VelocityStabilizationOptionsMaxDisplacementModel();
            }
            break;
        default:
            throw new UIException("Velocity Stabilization option","Cannot create model for velocity stabilization mode: "  + option);
        
        }
        
        pivModel.setStabilizationOption(stabilizationModel);
        
        return model;
    }

    public static AppContextModel createVelocityValidationOptionsModelForOption(AppContextModel model,
            VelocityValidationModeEnum option) {
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(model);
        VelocityValidationOptionsModel validationModel = pivModel.getValidationOption(option);     
        
        switch (option) {
        case Disabled:
            //Make no change on invalid model
            break;
        case DifferenceOnly:
            if (validationModel == null || validationModel.getValidationMode() != option) {
                validationModel = new VelocityValidationOptionsDifferenceOnlyModel();
            }
            break;
        case Difference:
            if (validationModel == null || validationModel.getValidationMode() != option) {
                validationModel = new VelocityValidationOptionsDifferenceModel();
            }
            break;
        case NormalizedMedianOnly:
            if (validationModel == null || validationModel.getValidationMode() != option) {
                validationModel = new VelocityValidationOptionsNormalizedMedianOnlyModel();
            }
            break;            
        case NormalizedMedian:
            if (validationModel == null || validationModel.getValidationMode() != option) {
                validationModel = new VelocityValidationOptionsNormalizedMedianModel();
            }
            break;
        case MultiPeakNormalizedMedian:
            if (validationModel == null || validationModel.getValidationMode() != option) {
                validationModel = new VelocityValidationOptionsMultiPeakNormalizedMedianModel();
            }
            break;
        default:
            throw new UIException("Velocity Validation option","Cannot create model for velocity validation mode: "  + option);
        
        }
        
        pivModel.setValidationOption(validationModel);
        
        return model;
    }

    public static AppContextModel createImageFilterOptionsModelForOption(AppContextModel model,
            ImageFilteringModeEnum option) {
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(model);
        ImageFilterOptionsModel filterModel = pivModel.getImageFilterOption(option);
        switch (option) {
        case DoNotApplyImageFiltering:
            //Make no change to the model
            break;
        case ApplyImageFilteringGaussian2D:
            if (filterModel == null || filterModel.getFilterMode() != option) {
                filterModel = new ImageFilterOptionsGaussian2DModel();
            }
            break;
        default:
            throw new UIException("Image Filter option","Cannot create model for image filter mode: "  + option);
        }
        
        pivModel.setImageFilterOption(filterModel);
        
        return model;
    }

    public static boolean validateCoherencyWithDataProcessingEnv(AppContextModel appContextModel) {
        if (appContextModel.getExecutionEnvironment() == null) {
            return false;
        }
        
        if (!appContextModel.getExecutionEnvironment().isEnableOpenCL()) {
            SubPixelInterpolationModeEnum interpMode = appContextModel.getProject().getPIVConfiguration().getInterpolationMode();
            if (interpMode == SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL ||
                interpMode == SubPixelInterpolationModeEnum.LucasKanadeOpenCL) {
                return false;
            } else if (interpMode == SubPixelInterpolationModeEnum.CombinedBaseAndFinalInterpolator) {
                SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel options = 
                        (SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel)appContextModel.getProject().getPIVConfiguration().
                                                                                           getInterpolationOption(SubPixelInterpolationModeEnum.CombinedBaseAndFinalInterpolator);
                for (SubPixelInterpolationModeEnum subInterp : options.getSubInterpolationModes()) {
                    if (subInterp == SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL ||
                        subInterp == SubPixelInterpolationModeEnum.LucasKanadeOpenCL) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    public static boolean isDenseExport(AppContextModel appContext) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        
        SubPixelInterpolationModeEnum subPixelMode = model.getInterpolationMode();
        SubPixelInterpolationOptionsModel subPixelOptions = model.getInterpolationOption(subPixelMode);
        if (subPixelMode.isCompositeMode()) {
            SubPixelInterpolationModeEnum subModes[] = subPixelOptions.getSubInterpolationModes();
            SubPixelInterpolationModeEnum lastMode = subModes[subModes.length - 1];
            SubPixelInterpolationOptionsModel lastSubPixelOptions = model.getInterpolationOption(lastMode);
            if (lastSubPixelOptions.isDenseExport()) {
                return true;
            }
        } else {            
            if (subPixelOptions.isDenseExport()) {
               return true;
            }
        }
        
        return false;
    }

    public static int computeTilesInI(AppContextModel appContext) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        final int height = model.getImageHeight();
                
        final int marginITop = model.getTopMargin();
        final int marginIBottom = model.getBottomMargin(); 
        
        final int tileHeight = model.getEndResolution().getSizeH();
        
        int numberOfTilesInI;
        if (model.getSuperpositionStartStep() != InterrogationAreaResolutionEnum.IA0) {
            final float overlapFactor = model.getSuperpositionOverlapPercentage();
            
            final float heightIncrement = overlapFactor * tileHeight;

            numberOfTilesInI = (int)(0.01f+((float)(height - (marginITop + marginIBottom)) - (tileHeight  - heightIncrement)) / heightIncrement);
        } else {
            numberOfTilesInI = (height - (marginITop + marginIBottom)) / tileHeight;
        }
        

        return numberOfTilesInI;
    }
    
    public static int computeTilesInJ(AppContextModel appContext) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        final int width = model.getImageWidth();

        final int marginJLeft = model.getLeftMargin();
        final int marginJRight = model.getRightMargin();

        final int tileWidth = model.getEndResolution().getSizeW();
        
        int numberOfTilesInJ;
        if (model.getSuperpositionStartStep() != InterrogationAreaResolutionEnum.IA0) {
            final float overlapFactor = model.getSuperpositionOverlapPercentage();
            
            float widthIncrement = overlapFactor * tileWidth;
            
            numberOfTilesInJ = (int)(0.01f+((float)(width - (marginJLeft + marginJRight)) - (tileWidth  - widthIncrement)) / widthIncrement);
        } else {
            numberOfTilesInJ = (width - (marginJLeft + marginJRight)) / tileWidth;
        }
        
        return numberOfTilesInJ;
    }
    
    public static int computePixelsInI(AppContextModel appContext) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        final int height = model.getImageHeight();
                
        final int marginITop = model.getTopMargin();
        final int marginIBottom = model.getBottomMargin(); 

        return height - marginITop - marginIBottom;
    }

    public static int computePixelsInJ(AppContextModel appContext) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        final int width = model.getImageWidth();
                
        final int marginJLeft = model.getLeftMargin();
        final int marginJRight = model.getRightMargin(); 

        return width - marginJLeft - marginJRight;
    }

    public static boolean validateExportFileStorageLimits(AppContextModel appContextModel) {
        PIVConfigurationModel model = appContextModel.getProject().getPIVConfiguration();
        long numberOfImages = model.getNumberOfImages();
        
        if (DataExportEnvFacade.isMultiVolumeExport(appContextModel)) {
            numberOfImages = DataExportEnvFacade.getNumberOfImagerPerMultiVolumeFile(appContextModel);
        }
        
        long memory = 0L;
        
        if (isDenseExport(appContextModel)) {
            final long pixelsInI = computePixelsInI(appContextModel);
            final long pixelsInJ = computePixelsInJ(appContextModel);
            memory = pixelsInI * pixelsInJ * numberOfImages;
        } else {
            final long tilesInI = computeTilesInI(appContextModel);
            final long tilesInJ = computeTilesInJ(appContextModel);
            memory = tilesInI * tilesInJ * numberOfImages;
        }

        //Assuming MATLAB exporter - Currently is the only available exporter
        //With header and U,V matrices only and reserving 1024 bytes for info structs. 
        memory = memory * 8L + 128L + 16L + 1024L;
        
        if (memory < BYTES_4GB) {
            return true;
        }
        
        return false;
    }
    
    public static int getMaximumNumberOfMapsPerVolume(AppContextModel appContextModel) {
        int maxMapsPerVolumeFile;

        final long reserved = 128L + 16L + 1024L;
        
        PIVConfigurationModel model = appContextModel.getProject().getPIVConfiguration();
        final int numberOfImages = model.getNumberOfImages();

        long memory;
        if (isDenseExport(appContextModel)) {
            final long pixelsInI = computePixelsInI(appContextModel);
            final long pixelsInJ = computePixelsInJ(appContextModel);
            memory = pixelsInI * pixelsInJ;
        } else {
            final long tilesInI = computeTilesInI(appContextModel);
            final long tilesInJ = computeTilesInJ(appContextModel);
            memory = tilesInI * tilesInJ;
        }

        //Assuming MATLAB exporter - Currently is the only available exporter
        //With header and U,V matrices only and reserving 1024 bytes for info structs. 
        memory = memory * 8L;

        if (memory * numberOfImages + reserved > BYTES_4GB) {
            maxMapsPerVolumeFile = (int)((BYTES_4GB - reserved) / memory);
        } else {
            maxMapsPerVolumeFile = numberOfImages;
        }
        
        return maxMapsPerVolumeFile;
    }
}
