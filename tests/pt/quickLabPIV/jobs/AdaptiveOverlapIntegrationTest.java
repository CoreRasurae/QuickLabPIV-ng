// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs;	

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import com.aparapi.device.Device;
import com.aparapi.device.Device.TYPE;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.opencl.OpenCLPlatform;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVReusableObjects;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.exporter.IVelocityExporterVisitor;
import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.iareas.AdaptiveInterVelocityInheritanceFileLogger;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaUnstableLogEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.IterationStepTilesFactory;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.Gaussian1DInterpolatorConfiguration;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.jobs.ImageWarpingAndClippingJob;
import pt.quickLabPIV.jobs.ImageWarpingInputData;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.FindMaximumSimple;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class AdaptiveOverlapIntegrationTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();

	@Test
	public void completeAdaptiveTest() {
	    assumeTrue("No OpenCL GPU device is available", gpuDevice != null);
	    		
		IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		IImage imgB = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000b.jpg");
		
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
		pivParameters.setInterpolatorStartStep(0);

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
		int numberOfPixels=5;
		gaussianConfig.setInterpolationPixels(numberOfPixels);
		pivParameters.setSpecificConfiguration(Gaussian1DInterpolatorConfiguration.IDENTIFIER, gaussianConfig);

		//Gaussiana 2D
		/*pivParameters.setInterpolatorStrategy(CrossCorrelationInterpolatorFactoryEnum.Gaussian2D);
		Gaussian2DInterpolatorConfiguration gaussianConfig = new Gaussian2DInterpolatorConfiguration();
		int numberOfPixelsX=5;
		int numberOfPixelsY=5;
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
		pivParameters.setSuperPositionIterationStepStart(2);
		//pivParameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.SuperPositionStrategy);
		
		
		/***
		 * Area stabilization criteria configuration
		 */
		//pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Ignore);
		//pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.Log);
		pivParameters.setAreaUnstableLoggingMode(InterAreaUnstableLogEnum.LogAndDump);
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
		pivParameters.setImageHeightPixels(imgA.getHeight());
		pivParameters.setImageWidthPixels(imgA.getWidth());
		pivParameters.setOverlapFactor(1.0f/2.0f);
		pivParameters.setInterrogationAreaStartIPixels(32);
		pivParameters.setInterrogationAreaEndIPixels(8);
		pivParameters.setInterrogationAreaStartJPixels(32);
		pivParameters.setInterrogationAreaEndJPixels(8);
		pivParameters.setNumberOfVelocityFrames(1); //Total number of velocity maps to be generated at end of processing
		
		/*****
		 * Warping and clipping configuration
		 */
		pivParameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
		
		PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
		
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
		
        final int adaptiveSteps = (int)FastMath.log(2, pivParameters.getInterrogationAreaStartIPixels() / pivParameters.getInterrogationAreaEndIPixels()) + 1;  
        
        IterationStepTiles[] stepTilesA = new IterationStepTiles[adaptiveSteps];
        IterationStepTiles[] stepTilesB = new IterationStepTiles[adaptiveSteps];
        
        stepTilesA[0] = IterationStepTilesFactory.create(TilesOrderEnum.FirstImage, pivParameters);
        stepTilesB[0] = IterationStepTilesFactory.create(TilesOrderEnum.SecondImage, pivParameters);
		
		List<OpenCLPlatform> platforms = OpenCLPlatform.getUncachedOpenCLPlatforms();
        
		List<Device> candidateDevices = new ArrayList<Device>();
        for (OpenCLPlatform platform: platforms) {
	        for (Device device : platform.getOpenCLDevices()){
	        	System.out.println("Description: " + device.getShortDescription());
	        	if (device.getType() == TYPE.GPU){
	        		candidateDevices.add(device);
	           }
	        }
        }
        OpenCLDevice aparapiDevice = (OpenCLDevice)gpuDevice.getAparapiDevice();
        aparapiDevice.setSharedMemory(false);
        ComputationDevice computeDevice = new ComputationDevice(aparapiDevice);
        ComputationDevice computeDeviceB = new ComputationDevice((OpenCLDevice)Device.first(TYPE.GPU));
        
        if (candidateDevices.size() >= 4) {
        	computeDevice = new ComputationDevice((OpenCLDevice)candidateDevices.get(2));
        	computeDeviceB = new ComputationDevice((OpenCLDevice)candidateDevices.get(3));
        }
		
    	PIVReusableObjects reusableObjects = PIVContextSingleton.getSingleton().getPIVReusableObjects();
    	
    	long groupTilesSize = 10;
    	long maxTilesSize = groupTilesSize;
		//groupTilesSize = 0;
    	
        int currentAdaptiveStep = 0;
        while (currentAdaptiveStep < adaptiveSteps) {
            if (currentAdaptiveStep > 0) {
                stepTilesA[currentAdaptiveStep] = stepTilesA[currentAdaptiveStep - 1].createTilesForNextIterationStep(); 
                stepTilesB[currentAdaptiveStep] = stepTilesB[currentAdaptiveStep - 1].createTilesForNextIterationStep();
            }
            
            ImageWarpingAndClippingJob warpingAndClippingJob = new ImageWarpingAndClippingJob();
            ImageWarpingInputData warpingAndClippingInput = new ImageWarpingInputData();
            warpingAndClippingInput.imageA = imgA;
            warpingAndClippingInput.imageB = imgB;
            warpingAndClippingInput.stepTilesA = stepTilesA[currentAdaptiveStep];
            warpingAndClippingInput.stepTilesB = stepTilesB[currentAdaptiveStep];
            warpingAndClippingJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING, warpingAndClippingInput);

            System.out.println("Clipping analyze...");
            warpingAndClippingJob.analyze();

            boolean unstableTiles = true;
            while (unstableTiles) {
                System.out.println("Clipping compute...");
                warpingAndClippingJob.compute();
                System.out.println("Clipping computed...");
                IterationStepTiles[] stepTiles = warpingAndClippingJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING);
                
                IterationStepTiles currentStepTilesA = stepTiles[0];
                IterationStepTiles currentStepTilesB = stepTiles[1];

                List<Tile> tilesB = currentStepTilesB.getUnstableVelocityTiles();
                List<Tile> tilesA = currentStepTilesA.getRelatedTilesInSameOrder(tilesB);
			
				//groupTilesSize = maxTilesSize = tilesA.size();
				
				if (tilesB.isEmpty()) {
					unstableTiles = false;
					currentAdaptiveStep++;
					continue;
				}
				
				if (tilesA.size() > maxTilesSize) {
					maxTilesSize = tilesA.size();
				}
				
				System.out.println("GroupTilesSize=" + groupTilesSize);
				if (groupTilesSize > maxTilesSize) {
					groupTilesSize = maxTilesSize;
				}
				
				List<Tile> tilesSimpleA = new ArrayList<Tile>((int)groupTilesSize);
				List<Tile> tilesSimpleB = new ArrayList<Tile>((int)groupTilesSize);
				Iterator<Tile> tileBIterator = tilesB.iterator();
				Iterator<Tile> tileAIterator = tilesA.iterator();
				List<Matrix> outputCross = new ArrayList<Matrix>(tilesA.size());
				Job<List<Tile>, XCorrelationResults> crossJobA = null;
				Job<List<Tile>, XCorrelationResults> crossJobB = null;
				int index = 0;
				try {
					if (groupTilesSize > 0) {
						while (tileAIterator.hasNext()) {
							tilesSimpleA.clear();
							tilesSimpleB.clear();
			
							int numberOfTiles = 0;
							while (numberOfTiles < groupTilesSize && tileAIterator.hasNext()) {
								Tile tileA = tileAIterator.next();
								Tile tileB = tileBIterator.next();									
								
								tilesSimpleA.add(tileA);
								tilesSimpleB.add(tileB);
								numberOfTiles++;
							}
							/*if (crossJobA != null) {
								crossJobA.dispose();
								crossJobA = null;
							}*/
								
							if (crossJobA != null && numberOfTiles < groupTilesSize) {
								crossJobA.dispose();
								crossJobA = null;
							}
							
							if (crossJobA == null) {
								//crossJobA = new CrossCorrelationJob(false, computeDevice, new int[] {128,64,128});
								//crossJobA = new CrossCorrelationJob(false, computeDevice, null);
								crossJobA = new CrossCorrelationFFTParStdJob(false, computeDevice, null);
							}
							crossJobA.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesSimpleA);
							crossJobA.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesSimpleB);
							crossJobA.analyze();
							crossJobA.compute();
							XCorrelationResults outputCrossLocal = crossJobA.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
							outputCross.addAll(outputCrossLocal.getCrossMatrices());
							
							index+=numberOfTiles;
							System.out.println("Computing tile index: " + index + " of " + tilesA.size() + " tiles.");						
						}
						
						groupTilesSize*=groupTilesSize/4;
						if (groupTilesSize == 0) {
							groupTilesSize = 1;
						}
					} else {
						int tilesN=tilesA.size()/2;
						final List<Tile> tilesA1 = tilesA.subList(0, tilesN);
						final List<Tile> tilesB1 = tilesB.subList(0, tilesN);
						final List<Tile> tilesA2 = tilesA.subList(tilesN, tilesA.size());
						final List<Tile> tilesB2 = tilesB.subList(tilesN, tilesA.size());
						
						final class Result {
							private int order;
							private List<Matrix> crossMatrices;
						};
						
						List<Callable<Result>> tasks = new ArrayList<Callable<Result>>(2);
						Callable<Result> callableOpenCL1 = new Callable<Result>() {
							private int order;
							private ComputationDevice computeDevice;
							private List<Tile> tilesA;
							private List<Tile> tilesB;
							
							public Callable<Result> setCrossJobParams(int order, ComputationDevice computeDevice, List<Tile> tilesA, List<Tile> tilesB) {
									this.computeDevice = computeDevice;
									this.tilesA = tilesA;
									this.tilesB = tilesB;
									this.order  = order;
									return this;
							}
							
							@Override
							public Result call() throws Exception {
								Job<List<Tile>, XCorrelationResults> crossJob = null;
								try {
									crossJob = new CrossCorrelationFFTParStdJob(false, computeDevice, null);
									crossJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesA);
									crossJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesB);
									crossJob.analyze();
									crossJob.compute();
								} finally {
									crossJob.dispose();
								}
								Result r = new Result();
								r.order = order;
								r.crossMatrices = crossJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES).getCrossMatrices();
								
								return r;
							}
							
						}.setCrossJobParams(1, computeDevice, tilesA1, tilesB1);
						
						Callable<Result> callableOpenCL2 = new Callable<Result>() {
							private int order;
							private ComputationDevice computeDevice;
							private List<Tile> tilesA;
							private List<Tile> tilesB;
							
							public Callable<Result> setCrossJobParams(int order, ComputationDevice computeDevice, List<Tile> tilesA, List<Tile> tilesB) {
									this.computeDevice = computeDevice;
									this.tilesA = tilesA;
									this.tilesB = tilesB;
									this.order = order;
									return this;
							}
							
							@Override
							public Result call() throws Exception {
								Job<List<Tile>, XCorrelationResults> crossJob = null;
								try {
									crossJob = new CrossCorrelationFFTParStdJob(false, computeDevice, null);
									crossJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesA);
									crossJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesB);
									crossJob.analyze();
									crossJob.compute();
								} finally {
									crossJob.dispose();
								}
								Result r = new Result();
								r.order = order;
								r.crossMatrices = crossJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES).getCrossMatrices();
								
								return r;
							}
							
						}.setCrossJobParams(2, computeDeviceB, tilesA2, tilesB2);
	
	
						tasks.add(callableOpenCL1);
						tasks.add(callableOpenCL2);
						
						Result[] results = new Result[2];
						ExecutorService service = Executors.newFixedThreadPool(2);
						List<Future<Result>> futures;
						try {
							futures = service.invokeAll(tasks);
							for (Future<Result> future : futures) {
								try {
									Result r = future.get();
									results[r.order-1] = r;
								} catch (ExecutionException e) {
								}
							}
	
						} catch (InterruptedException e) {
							service.shutdown();
							try {
								service.awaitTermination(1, TimeUnit.MINUTES);
							} catch (InterruptedException e1) {
							}
						}
						
						outputCross.addAll(results[0].crossMatrices);
						outputCross.addAll(results[1].crossMatrices);
						/*crossJobA = new CrossCorrelationJob(false, computeDevice, EXECUTION_MODE.GPU, null);
						crossJobA.analyze(tilesA, tilesB);
						crossJobA.compute();
						outputCross = crossJobA.getOutputMatrices();*/
					}
				} finally {
					if (crossJobA != null) {
						crossJobA.dispose();
						crossJobA = null;
					}
					
					if (crossJobB != null) {
						crossJobB.dispose();
						crossJobB = null;
					}
				}
				
				//IMaximumFinder findPeak = new FindMaximumFromCenter();
				IMaximumFinder findPeak = new FindMaximumSimple();
				
				List<MaxCrossResult> maxResults = new ArrayList<MaxCrossResult>();
				tileAIterator = tilesA.iterator();
				tileBIterator = tilesB.iterator();
				for (Matrix m : outputCross) {
					MaxCrossResult result = findPeak.findMaximum(m);
					//System.out.println(result);
					//associate MaxCrossResults with the corresponding tiles... they are in same order...
					result.setAssociatedTileA(tileAIterator.next());
					result.setAssociatedTileB(tileBIterator.next());
										
					maxResults.add(result);
				}
				
                ICrossCorrelationInterpolator interpolator = reusableObjects.getOrCreateInterpolator(); 
                if (interpolator != null && currentStepTilesB.getCurrentStep() >= pivParameters.getInterpolatorStartStep()) {
                    maxResults = interpolator.interpolate(maxResults);
                }

				//Check bounds... when clipping...
				currentStepTilesB.updateDisplacementsFromMaxCrossResults(0, maxResults);
			}			
        }
    
        IVelocityExporterVisitor exporter = new StructMultiFrameFloatVelocityExporter();
        exporter.openFile("velocities.mat");
        IterationStepTiles lastStepTilesB = stepTilesB[adaptiveSteps - 1];
        for (IterationStepTiles stepTilesToExport : stepTilesB) {
            exporter.exportDataToFile(0, stepTilesToExport);
        }
        exporter.closeFile();
        if (velocityLogger != null) {
            velocityLogger.close();
        }
    }
}
