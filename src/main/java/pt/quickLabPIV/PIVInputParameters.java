// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.HashMap;
import java.util.Map;

import pt.quickLabPIV.iareas.ICrossCorrelationDumpMatcher;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaUnstableLogEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.replacement.VectorReplacementFactoryEnum;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.OpticalFlowAfterPIVInterpolatorFactoryEnum;
import pt.quickLabPIV.maximum.MaximumFinderFactoryEnum;

/**
 * This class is designed to work like a Bean which only carries input parameters from the UI/User to the 
 * business logic code. No intermediate, computed or derived parameters are to be included in this bean.
 * @author lpnm
 *
 */
public class PIVInputParameters {
	private InterAreaDivisionStrategiesFactoryEnum areaDivisionStrategy;
	private int superPositionIterationStepStart;
	private InterAreaStableStrategiesFactoryEnum areaStableStrategy;
	private InterAreaUnstableLogEnum areaUnstableLogMode = InterAreaUnstableLogEnum.Ignore;
	private String areaUnstableLogFilenamePrefix = "";
	private ICrossCorrelationDumpMatcher crossCorrelationDumpMatcher = null;
	private InterAreaVelocityStrategiesFactoryEnum velocityInheritanceStrategy;
	private CrossCorrelationInterpolatorFactoryEnum interpolatorStrategy;
	private OpticalFlowAfterPIVInterpolatorFactoryEnum opticalFlowAfterPIVStrategy;
	private VectorValidatorFactoryEnum vectorValidatorStrategy = VectorValidatorFactoryEnum.None;
	private VectorReplacementFactoryEnum vectorReplacementStrategy = VectorReplacementFactoryEnum.None;	
	private MaximumFinderFactoryEnum maximumFinderStrategy;
	private ImageFactoryEnum pixelDepth;
	
	private ClippingModeEnum clippingMode;
	private WarpingModeFactoryEnum warpingModeStrategy;
	private ImageFilterFactoryEnum imageFilterModeStrategy;
	private ImageFilteringModeFactoryEnum imageFilteringMode;
	
	private Map<String, Object> specificConfigurations = new HashMap<String, Object>();
	
	//The vertical pixels size for the interrogation area for the first adaptive step 
	private int interrogationAreaStartIPixels;
	//The vertical pixels size for the interrogation area for the final adaptive step
	private int interrogationAreaEndIPixels;
	//The horizontal pixels size for the interrogation area for the first adaptive step
	private int interrogationAreaStartJPixels;
	//The horizontal pixels size fot the interrogation area fot the last adaptive step;
	private int interrogationAreaEndJPixels;
	
	//
	private float overlapFactor;
	private int maxReIterativeSteps;
	
	//Total margin pixels in I vertical direction, from the Top side
	private int marginPixelsITop;
	private int marginPixelsIBottom;
	//Total margin pixels in J vertical direction, from the Left side
	private int marginPixelsJLeft;
	private int marginPixelsJRight;
	private int imageHeightPixels;
	private int imageWidthPixels;
	
	private int numberOfVelocityFrames;
	private int interpolatorStartStep;
	
	private String outputPath;
	private String outputFilename;
	private String nextFilename;
	private String maskFilename;
    private boolean maskOnlyAtExport;
    private boolean denseExport;
	
		
	public int getInterrogationAreaStartIPixels() {
		return interrogationAreaStartIPixels;
	}

	public void setInterrogationAreaStartIPixels(int _interrogationAreaStartIPixels) {
		interrogationAreaStartIPixels = _interrogationAreaStartIPixels;
	}

	public int getInterrogationAreaEndIPixels() {
		return interrogationAreaEndIPixels;
	}

	public void setInterrogationAreaEndIPixels(int _interrogationAreaEndIPixels) {
		interrogationAreaEndIPixels = _interrogationAreaEndIPixels;
	}

	public int getInterrogationAreaStartJPixels() {
		return interrogationAreaStartJPixels;
	}

	public void setInterrogationAreaStartJPixels(int _interrogationAreaStartJPixels) {
		interrogationAreaStartJPixels = _interrogationAreaStartJPixels;
	}

	public int getInterrogationAreaEndJPixels() {
		return interrogationAreaEndJPixels;
	}

	public void setInterrogationAreaEndJPixels(int _interrogationAreaEndJPixels) {
		interrogationAreaEndJPixels = _interrogationAreaEndJPixels;
	}

	public float getOverlapFactor() {
		return overlapFactor;
	}

	public void setOverlapFactor(float _overlapFactor) {
		overlapFactor = _overlapFactor;
	}

	public int getMaxReIterativeSteps() {
		return maxReIterativeSteps;
	}

	public void setMaxReIterativeSteps(int _maxReIterativeSteps) {
		maxReIterativeSteps = _maxReIterativeSteps;
	}

	public int getMarginPixelsITop() {
		return marginPixelsITop;
	}

	public void setMarginPixelsITop(int _marginPixelsI) {
		marginPixelsITop = _marginPixelsI;
	}

	public int getMarginPixelsJLeft() {
		return marginPixelsJLeft;
	}

	public void setMarginPixelsJLeft(int _marginPixelsJ) {
		marginPixelsJLeft = _marginPixelsJ;
	}

	public int getMarginPixelsIBottom() {
		return marginPixelsIBottom;
	}

	public void setMarginPixelsIBottom(int _marginPixelsI) {
		marginPixelsIBottom = _marginPixelsI;
	}

	public int getMarginPixelsJRight() {
		return marginPixelsJRight;
	}

	public void setMarginPixelsJRight(int _marginPixelsJ) {
		marginPixelsJRight = _marginPixelsJ;
	}
	
	public int getImageHeightPixels() {
		return imageHeightPixels;
	}

	public void setImageHeightPixels(int _imageHeightPixels) {
		imageHeightPixels = _imageHeightPixels;
	}

	public int getImageWidthPixels() {
		return imageWidthPixels;
	}

	public void setImageWidthPixels(int _imageWidthPixels) {
		imageWidthPixels = _imageWidthPixels;
	}
	
	public InterAreaDivisionStrategiesFactoryEnum getAreaDivisionStrategy() {
		return areaDivisionStrategy;
	}
	
	public void setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum _areaDivisionStrategy) {
		areaDivisionStrategy = _areaDivisionStrategy;
	}

	public InterAreaStableStrategiesFactoryEnum getAreaStableStrategy() {
		return areaStableStrategy;
	}
	
	public void setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum _areaStableStrategy) {
		areaStableStrategy = _areaStableStrategy;
	}
	
	public int getNumberOfVelocityFrames() {
		return numberOfVelocityFrames;
	}
	
	public void setNumberOfVelocityFrames(int _numberOfVelocityFrames) {
		numberOfVelocityFrames = _numberOfVelocityFrames;
	}
	
	public InterAreaVelocityStrategiesFactoryEnum getVelocityInheritanceStrategy() {
		return velocityInheritanceStrategy;
	}
	
	public void setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum strategy) {
		velocityInheritanceStrategy = strategy;
	}

	public void setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum _interpolatorStrategy) {
		interpolatorStrategy = _interpolatorStrategy;
	}
	
	public CrossCorrelationInterpolatorFactoryEnum getInterpolationStrategy() {
		return interpolatorStrategy;
	}
	
	public void setOpticalFlowAfterPIVStrategy(OpticalFlowAfterPIVInterpolatorFactoryEnum _opticalFlowAfterPIVStrategy) {
	    opticalFlowAfterPIVStrategy = _opticalFlowAfterPIVStrategy;
	}
	
	public OpticalFlowAfterPIVInterpolatorFactoryEnum getOpticalFlowAfterPIVStrategy() {
	    return opticalFlowAfterPIVStrategy;
	}
	
	public void setVectorValidationStrategy(VectorValidatorFactoryEnum _vectorValidatorStrategy) {
	    vectorValidatorStrategy = _vectorValidatorStrategy;
	}
	
	public VectorValidatorFactoryEnum getVectorValidatorStrategy() {
	    return vectorValidatorStrategy;
	}
	
	public void setVectorReplacementStrategy(VectorReplacementFactoryEnum _vectorReplacementStrategy) {
	    vectorReplacementStrategy = _vectorReplacementStrategy;
	}
	
	public VectorReplacementFactoryEnum getVectorReplacementStrategy() {
	    return vectorReplacementStrategy;
	}
	
	public void setSpecificConfiguration(String identifier, Object configuration) {
		if (specificConfigurations.containsKey(identifier)) {
			throw new InvalidPIVParametersException("Attempt to overwrite specific configuration with id: " + identifier);
		}
		
		specificConfigurations.put(identifier, configuration);
	}
	
	public void setMaximumFinderStrategy(MaximumFinderFactoryEnum _maximumFinderStrategy) {
		maximumFinderStrategy = _maximumFinderStrategy;	
	}    

	public MaximumFinderFactoryEnum getMaximumFinderStrategy() {
		return maximumFinderStrategy;
	}
	
	public void setPixelDepth(ImageFactoryEnum imageDepth) {
	    pixelDepth = imageDepth;
	}
	
    public ImageFactoryEnum getPixelDepth() {
        return pixelDepth;
    }
	
    public Object getSpecificConfiguration(String identifier) {
		return specificConfigurations.get(identifier);
	}
	
	public void clearSpecificConfigurations() {
		specificConfigurations.clear();
	}

	public void setInterpolatorStartStep(int startStep) {
		interpolatorStartStep = startStep;
	}
	
	public int getInterpolatorStartStep() {
		return interpolatorStartStep;
	}
	
	public void setClippingMode(ClippingModeEnum mode) {
		clippingMode = mode;
	}
	
	public ClippingModeEnum getClippingMode() {
		return clippingMode;
	}
	
	public void setWarpingMode(WarpingModeFactoryEnum mode) {
	    warpingModeStrategy = mode;
	}
	
	public WarpingModeFactoryEnum getWarpingMode() {
	    return warpingModeStrategy;
	}
	
	public void setImageFilterMode(ImageFilterFactoryEnum filterMode) {
	    imageFilterModeStrategy = filterMode;
	}
	
	public ImageFilterFactoryEnum getImageFilterMode() {
	    return imageFilterModeStrategy;
	}

	public void setImageFilteringMode(ImageFilteringModeFactoryEnum _imageFilteringMode) {
	    imageFilteringMode = _imageFilteringMode;
	}
	
    public ImageFilteringModeFactoryEnum getImageFilteringMode() {
        return imageFilteringMode;
    }
    
	public void setSuperPositionIterationStepStart(int step) {
		superPositionIterationStepStart = step;
	}
	
	public int getSuperPositionIterationStepStart() {
		return superPositionIterationStepStart;
	}
	
	public void setAreaUnstableLoggingMode(InterAreaUnstableLogEnum logMode) {
		areaUnstableLogMode = logMode;
	}
	
	public InterAreaUnstableLogEnum getAreaUnstableLoggingMode() {
		return areaUnstableLogMode;
	}
	
	public void setAreaUnstableDumpFilenamePrefix(String filenamePrefix) {
		areaUnstableLogFilenamePrefix = filenamePrefix;
	}
	
	public String getAreaUnstableDumpFilenamePrefix() {
		return areaUnstableLogFilenamePrefix;
	}
	
    public void setCrossCorrelationDumpMatcher(ICrossCorrelationDumpMatcher matcher) {
        crossCorrelationDumpMatcher = matcher;
    }

    public ICrossCorrelationDumpMatcher getCrossCorrelationDumpMatcher() {
        return crossCorrelationDumpMatcher;
    }

    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String path) {
        outputPath = path;
    }

    public String getOutputFilename() {       
        return outputFilename;
    }
    
    public void setOutputFilename(String filename) {
        outputFilename = filename;
    }

    public void setNextFilename(String filename) {
        nextFilename = filename;
    }
    
    public String getNextFilename() {
        return nextFilename;
    }    
    
    public void setMaskFilename(String filename) {
        maskFilename = filename;
    }
    
    public String getMaskFilename() {
        return maskFilename;
    }

    public void setMaskOnlyAtExport(boolean _maskOnlyAtExport) {
        maskOnlyAtExport = _maskOnlyAtExport;
    }
    
    public boolean isMaskOnlyAtExport() {
        return maskOnlyAtExport;
    }

    public void setDenseExport(boolean dense) {
        denseExport = dense;
    }
    
    public boolean isDenseExport() {
        return denseExport;
    }
}
