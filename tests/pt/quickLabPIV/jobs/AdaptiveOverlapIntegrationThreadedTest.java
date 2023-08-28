// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;


import org.junit.Test;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.InputFiles;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVResults;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.IVelocityExporterVisitor;
import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.iareas.AdaptiveInterVelocityInheritanceFileLogger;
import pt.quickLabPIV.iareas.InterAreaDisplacementStableConfiguration;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaUnstableLogEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian1DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.Gaussian2DSubTypeFactoryEnum;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.managers.OpenClGpuManager;
import pt.quickLabPIV.maximum.MaximumFinderFactoryEnum;

public class AdaptiveOverlapIntegrationThreadedTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    
	public List<File> findFiles(String path, final String filenamePattern) {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (!pathname.isFile()) {
					return false;
				}
				
				if (!pathname.getName().matches(filenamePattern)) {
					return false;
				}
				
				return true;
			}
			
		};
		List<File> files = Collections.emptyList();
		
		File dir = new File(path);
		File[] allFiles = dir.listFiles(filter);
		
		Arrays.sort(allFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
			
		});
		
		files = Arrays.asList(allFiles);
		
		return files;
	}
	
	@Test
	public void completeAdaptiveTest() throws InterruptedException, ExecutionException {
	    assumeTrue("No OpenCL GPU device available", gpuDevice == null);

		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		
		PIVInputParameters pivParameters = singleton.getPIVParameters();
		
		/***
		 * Velocity inheritance configuration
		 */
		//pivParameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Distance);
		pivParameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		pivParameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		pivParameters.setImageFilteringMode(ImageFilteringModeFactoryEnum.NoImageFiltering);
		pivParameters.setImageFilterMode(ImageFilterFactoryEnum.NoFiltering);
		
		/***
		 * Sub-pixel interpolation configuration
		 */
		//pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.None);
		pivParameters.setInterpolatorStartStep(2);

		//BiCubica
		/*pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.BiCubic);
		BiCubicInterpolatorConfiguration bicubicConfig = new BiCubicInterpolatorConfiguration();
		int numberOfDecimalPoints=100;
		int numberOfPixels=5;
		bicubicConfig.setProperties(numberOfDecimalPoints, numberOfPixels);
		pivParameters.setSpecificConfiguration(BiCubicInterpolatorConfiguration.IDENTIFIER, bicubicConfig);*/
		
		//Gaussiana
		pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian1D);
		Gaussian1DInterpolatorConfiguration gaussianConfig = new Gaussian1DInterpolatorConfiguration();
		int numberOfPixels=7; //Not recommended to use 5 point interpolation may take long time 
		gaussianConfig.setInterpolationPixels(numberOfPixels);
		pivParameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, gaussianConfig);

		//Gaussiana 2D
		/*pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		Gaussian2DInterpolatorConfiguration gaussianConfig = new Gaussian2DInterpolatorConfiguration();
		int numberOfPixelsX=7;
		int numberOfPixelsY=7;
		//Gaussian2DSubTypeFactoryEnum subType = Gaussian2DSubTypeFactoryEnum.Symmetric;
		//Gaussian2DSubTypeFactoryEnum subType = Gaussian2DSubTypeFactoryEnum.Assymmetric;
		Gaussian2DSubTypeFactoryEnum subType = Gaussian2DSubTypeFactoryEnum.AssymmetricWithRotation;
		//Gaussian2DSubTypeFactoryEnum subType = Gaussian2DSubTypeFactoryEnum.EllipticalWithRotation;
		gaussianConfig.setProperties(numberOfPixelsX, numberOfPixelsY, subType);
		//gaussianConfig.setLogResults(true);
		pivParameters.setSpecificConfiguration(Gaussian2DInterpolatorConfiguration.IDENTIFIER, gaussianConfig);*/

		/***
		 * Super-position configuration
		 */
		pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.MixedSuperPositionStrategy);
		pivParameters.setSuperPositionIterationStepStart(1);
		//pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.SuperPositionStrategy);
		
		
		/***
		 * Area stabilization criteria configuration
		 */ 
		pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Ignore);
		//pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Log);
		//pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.LogAndDump);
		pivParameters.setAreaUnstableDumpFilenamePrefix("unstable_dump");
		
		//Simple strategy will stabilize always after first iteration
		pivParameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		
		//Max. Displacement strategy - Interrogation Area stabilizes when displacement norm is below the specified value,
		//or when the maximum number of repetitions is reached.
		/*InterAreaDisplacementStableConfiguration stableConfig = new InterAreaDisplacementStableConfiguration(1.0f, 5);
		pivParameters.setSpecificConfiguration(InterAreaDisplacementStableConfiguration.IDENTIFIER, stableConfig);
		pivParameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.MaxDisplacementStrategy);*/
		
		/***
		 * Out of bound clipping configuration
		 */
		pivParameters.setClippingMode(ClippingModeEnum.AllowedOutOfBoundClipping);
		//pivParameters.setClippingMode(ClippingModeEnum.LoggedOutOfBoundClipping);
		//pivParameters.setClippingMode(ClippingModeEnum.NoOutOfBoundClipping);
		
		/***
		 * Margins configuration
		 */
		pivParameters.setMarginPixelsITop(0);
		pivParameters.setMarginPixelsIBottom(0);
		pivParameters.setMarginPixelsJLeft(0);
		pivParameters.setMarginPixelsJRight(0);
		/*
		pivParameters.setMarginPixelsITop(16);
		pivParameters.setMarginPixelsIBottom(16);
		pivParameters.setMarginPixelsJLeft(16);
		pivParameters.setMarginPixelsJRight(16);*/
		pivParameters.setImageHeightPixels(1200);
		pivParameters.setImageWidthPixels(1600);
		pivParameters.setOverlapFactor(1.0f/2.0f);
		pivParameters.setInterrogationAreaStartIPixels(128);
		pivParameters.setInterrogationAreaEndIPixels(32);
		pivParameters.setInterrogationAreaStartJPixels(128);
		pivParameters.setInterrogationAreaEndJPixels(32);
		pivParameters.setNumberOfVelocityFrames(1); //Total number of velocity maps to be generated at end of processing
		
		/*****
		 * Pixel depth
		 */
		pivParameters.setPixelDepth(ImageFactoryEnum.Image8Bit);
		
        /*****
         * Warping and clipping configuration
         */
        pivParameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);

        /*****
         * Validation configuration
         */
        pivParameters.setVectorValidationStrategy(VectorValidatorFactoryEnum.None);
		
        /****
         * Maximum finder configuration
         */
        pivParameters.setMaximumFinderStrategy(MaximumFinderFactoryEnum.MaximumFinderSimple);
        
		PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
		runParameters.setTotalNumberOfThreads(3);
		DeviceRuntimeConfiguration config = new DeviceRuntimeConfiguration();
		config.setDevice(gpuDevice);
		config.setCpuThreadAssignments(new int[] {0, 1, 2});
		runParameters.clearThreadMappings();
		
		//Log velocity inheritance for the specified tiles at the specified adaptive step.
		AdaptiveInterVelocityInheritanceFileLogger velocityLogger = null;
		/*TileMatcher matcher = new TileMatcher(9,4,1,-1,-1);
		velocityLogger = new AdaptiveInterVelocityInheritanceFileLogger("inheritance_log.txt");
		velocityLogger.addRelevantTile(matcher);
		matcher = new TileMatcher(9,3,1,-1,-1);
		velocityLogger.addRelevantTile(matcher);
		matcher = new TileMatcher(8,4,1,-1,-1);
		velocityLogger.addRelevantTile(matcher);

		runParameters.setVelocityInheritanceLogger(velocityLogger);*/
				
		String patternA = "image_.*a.jpg";
		String patternB = "image_.*b.jpg";
		//List<File> filenamesA = findFiles(".." + File.separator + "testFiles", patternA);
		//List<File> filenamesB = findFiles(".." + File.separator + "testFiles", patternB);
        List<File> filenamesA = findFiles("testFiles", patternA);
        List<File> filenamesB = findFiles("testFiles", patternB);
		
		InputFiles inputFiles = new InputFiles(0, 0, filenamesA.subList(0, 3), filenamesB.subList(0, 3));
		
		
		OpenClGpuManager managerJob = new OpenClGpuManager(inputFiles);
		managerJob.analyze();
		managerJob.compute();
		
		PIVResults results = managerJob.getJobResult(JobResultEnum.JOB_RESULT_PIV);
		
		StructMultiFrameFloatVelocityExporter exporter = new StructMultiFrameFloatVelocityExporter();
		exporter.openFile("velocities.mat");
		exporter.exportDataToFile(results);
		exporter.closeFile();
	}

	public static void main(String[] args) {
		AdaptiveOverlapIntegrationThreadedTest clz = new AdaptiveOverlapIntegrationThreadedTest();
		try {
			clz.completeAdaptiveTest();
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e);
			System.exit(1);
		}
	}
}
