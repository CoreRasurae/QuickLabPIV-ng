// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import pt.quickLabPIV.ExecutionStatus;
import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.InputFiles;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVMap;
import pt.quickLabPIV.PIVResults;
import pt.quickLabPIV.PIVReusableObjects;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.IterationStepTilesFactory;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.IOpticalFlowInterpolator;
import pt.quickLabPIV.interpolators.OpticalFlowAfterPIVInterpolatorFactoryEnum;
import pt.quickLabPIV.jobs.managers.ManagerParameters;
import pt.quickLabPIV.jobs.validator.VectorValidatorJob;
import pt.quickLabPIV.jobs.validator.VectorValidatorJobParameters;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;
import pt.quickLabPIV.maximum.MaximumFinderFactoryEnum;

public class LocalPIVOpenCLGpuJob extends Job<ManagerParameters, Future<PIVResults>> {
    private Logger logger = LoggerFactory.getLogger(LocalPIVOpenCLGpuJob.class);
	private InputFiles inputFiles;
	private AffinityThreadFactory atf;
	private ExecutorService service = Executors.newFixedThreadPool(1);//, atf);
	private ManagerParameters parameters;
	
    private final PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
    private final ExecutionStatus execStatus = runParameters.getExecutionStatus();
	
	public void setInputImages(InputFiles inputFiles) {
		this.inputFiles = inputFiles;
	}
		
	public void setParameters(final ManagerParameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public void analyze() {
		//Check if all images are accessible, can be done serially
		//Check that GPU is available
		if (inputFiles.getFilesA().size() != inputFiles.getFilesB().size()) {
			throw new JobAnalyzeException("Number of files A must match the number of files B");
		}
	}

	@Override
	public void compute() {
		atf = new AffinityThreadFactory("PIVOpenCLGpuJob-" + parameters.getThreadIdx(), false, AffinityStrategies.SAME_SOCKET, AffinityStrategies.DIFFERENT_SOCKET, AffinityStrategies.DIFFERENT_CORE);
		
		Callable<PIVResults> callable = new Callable<PIVResults>() {
			final PIVInputParameters inputParameters = PIVContextSingleton.getSingleton().getPIVParameters();
			final PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
			ManagerParameters managerParameters;
			
			int iaStartPixelsI = inputParameters.getInterrogationAreaStartIPixels();
			int iaEndPixelsI = inputParameters.getInterrogationAreaEndIPixels();
			
			final int adaptiveLevels = (int)(FastMath.log(2, iaStartPixelsI) - FastMath.log(2, iaEndPixelsI)) + 1;

			final ImageReaderJob imagesReaderJob = new ImageReaderJob();
			final Job<IImage, IImage> imagesAFilteringJob = ImageFilteringModeFactoryEnum.createMainImageAFilterJob(inputParameters);
			final Job<IImage, IImage> imagesBFilteringJob = ImageFilteringModeFactoryEnum.createMainImageBFilterJob(inputParameters);			
			final Job<List<Tile>, List<Tile>> warpedTilesAFilteringJob = ImageFilteringModeFactoryEnum.createWarpedTilesAFilterJob(inputParameters);
			final Job<List<Tile>, List<Tile>> warpedTilesBFilteringJob = ImageFilteringModeFactoryEnum.createWarpedTilesBFilterJob(inputParameters);
			final ImageMaskJob imageMaskJob = inputParameters.getMaskFilename() != null && !inputParameters.isMaskOnlyAtExport() ? new ImageMaskJob(inputParameters.getMaskFilename()) : null;
			final VectorMaskJob[] vectorMaskJobs = new VectorMaskJob[adaptiveLevels];
			final DenseVectorMaskJob denseVectorMaskJob = new DenseVectorMaskJob();
			final ImageWarpingAndClippingJob warpingAndClippingJob = new ImageWarpingAndClippingJob();
			final ImageWarpingInputData warpingAndClippingInput = new ImageWarpingInputData();
			final IterationStepTiles[] stepTilesAByLevel = new IterationStepTiles[adaptiveLevels];
			final IterationStepTiles[] stepTilesBByLevel = new IterationStepTiles[adaptiveLevels];
			final VectorValidatorJob validatorJob = new VectorValidatorJob();
			final IOpticalFlowInterpolator opticalFlowAfterPIVInterpolator = OpticalFlowAfterPIVInterpolatorFactoryEnum.createInterpolator(inputParameters);
			
			public Callable<PIVResults> setParameters(final ManagerParameters parameters) {
				managerParameters = parameters;
				return this;
			}
			
			@Override
			public PIVResults call() throws Exception {
				runParameters.mapThreadToThreadIndex(managerParameters.getThreadIdx());
				
				final File[] filenames = new File[2];
				Iterator<File> filesAIter = inputFiles.getFilesA().iterator();
				Iterator<File> filesBIter = inputFiles.getFilesB().iterator();
												
				PIVResults partialResults = new PIVResults();
				VectorValidatorJobParameters validatorParameters = new VectorValidatorJobParameters();
				
				int groupTilesSize = 50;
				int currentRelativeFrame = inputFiles.getRelativeStartFrame();
				
				final IMaximumFinder findPeak = MaximumFinderFactoryEnum.create();
				//final IMaximumFinder checkPeak = MaximumFinderFactoryEnum.create(MaximumFinderFactoryEnum.MaximumFinderSimple);

				Job<List<Tile>, XCorrelationResults>[] openCLJobs = managerParameters.getOpenCLJobs();
				try {
					PIVReusableObjects reusableObjects = PIVContextSingleton.getSingleton().getPIVReusableObjects();
					List<IImage> filteredImages = new ArrayList<IImage>(2);
					ICrossCorrelationInterpolator interpolator = reusableObjects.getOrCreateInterpolator();
	
					while (filesAIter.hasNext() && filesBIter.hasNext()) {
					    if (runParameters.isCancelRequested()) {
					        return partialResults;
					    }
						File filenameA = filesAIter.next();
						File filenameB = filesBIter.next();
						
						filenames[0] = filenameA;
						filenames[1] = filenameB;
						
						imagesReaderJob.setFilenamesToRead(filenames);
						imagesReaderJob.analyze();
						imagesReaderJob.compute();
						List<IImage> images = imagesReaderJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES);
						
	                    filteredImages.clear();

	                    if (interpolator.isImagesRequired()) {
	                        interpolator.updateImageA(images.get(0));
	                        interpolator.updateImageB(images.get(1));
	                    }
	            
	                    if (opticalFlowAfterPIVInterpolator != null) {
	                        opticalFlowAfterPIVInterpolator.updateImageA(images.get(0));
	                        opticalFlowAfterPIVInterpolator.updateImageB(images.get(1));
	                    }
	                    
						imagesAFilteringJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES, images.get(0));
						imagesAFilteringJob.analyze();
						imagesAFilteringJob.compute();
						filteredImages.add(0, imagesAFilteringJob.getJobResult(JobResultEnum.JOB_RESULT_FILTERED_IMAGES));

						imagesBFilteringJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES, images.get(1));
                        imagesBFilteringJob.analyze();
                        imagesBFilteringJob.compute();
                        filteredImages.add(1, imagesBFilteringJob.getJobResult(JobResultEnum.JOB_RESULT_FILTERED_IMAGES));
                        
						if (imageMaskJob != null) {
						    imageMaskJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES_TO_MASK, filteredImages);
						    imageMaskJob.analyze();
						    imageMaskJob.compute();
						    filteredImages = imageMaskJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES);
						}
												
						IImage imgA = filteredImages.get(0);
						IImage imgB = filteredImages.get(1);
						logger.warn("Comparing image: " + images.get(0) + ", with image: " + images.get(1));
	
						List<Matrix> outputCross;
						List<MaxCrossResult> maxResults;
						
						int currentAdaptiveLevel = 0;
						while (currentAdaptiveLevel < adaptiveLevels) {
						    warpingAndClippingInput.imageA = imgA;
						    warpingAndClippingInput.imageB = imgB;
						    
						    warpingAndClippingInput.stepTilesA = stepTilesAByLevel[currentAdaptiveLevel];
						    if (warpingAndClippingInput.stepTilesA == null) {
	                            if (currentAdaptiveLevel == 0) {
	                                stepTilesAByLevel[currentAdaptiveLevel] = IterationStepTilesFactory.create(TilesOrderEnum.FirstImage, inputParameters);
	                            } else {
	                                stepTilesAByLevel[currentAdaptiveLevel] = stepTilesAByLevel[currentAdaptiveLevel - 1].createTilesForNextIterationStep();
	                            }
	                            warpingAndClippingInput.stepTilesA = stepTilesAByLevel[currentAdaptiveLevel];
						    } else {
						        //warpingAndClippingInput.stepTilesA.reuseTiles();
						    }
						    
	                        warpingAndClippingInput.stepTilesB = stepTilesBByLevel[currentAdaptiveLevel];
                            if (warpingAndClippingInput.stepTilesB == null) {
                                if (currentAdaptiveLevel == 0) {
                                    stepTilesBByLevel[currentAdaptiveLevel] = IterationStepTilesFactory.create(TilesOrderEnum.SecondImage, inputParameters);
                                } else {
                                    stepTilesBByLevel[currentAdaptiveLevel] = stepTilesBByLevel[currentAdaptiveLevel - 1].createTilesForNextIterationStep();
                                }
                                warpingAndClippingInput.stepTilesB = stepTilesBByLevel[currentAdaptiveLevel];
                            }
                            if (currentAdaptiveLevel > 0 || warpingAndClippingInput.stepTilesB != null) {
                                //By always running when currentAdaptiveLevel is greater than 0, we ensure that velocity inheritance will always take place
                                warpingAndClippingInput.stepTilesB.reuseTiles();
                            }
						    warpingAndClippingJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING, warpingAndClippingInput);
							
							logger.info("Warping and Clipping analyze...");
							warpingAndClippingJob.analyze();
	
							Job<List<Tile>, XCorrelationResults> openClLevelJob = null;
							if (managerParameters.isSameXCorrJobForAllAdaptiveLevels()) {
								openClLevelJob = openCLJobs[0];
							} else {
								openClLevelJob = openCLJobs[currentAdaptiveLevel];
							}

                            logger.info("Warping and Clipping compute...");
                            warpingAndClippingJob.compute();
                            logger.info("Clipping computed...");
                            IterationStepTiles[] stepTiles = warpingAndClippingJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING);
							
							boolean unstableTiles = true;
							int unstableIteration = 0;
							while (unstableTiles) {								
								IterationStepTiles stepTilesA = stepTiles[0];
								IterationStepTiles stepTilesB = stepTiles[1]; 
								
								List<Tile> tilesB = stepTilesB.getUnstableVelocityTiles();
								List<Tile> tilesA = stepTilesA.getRelatedTilesInSameOrder(tilesB);
							
								maxResults = new ArrayList<MaxCrossResult>(tilesA.size());
								outputCross = new ArrayList<Matrix>(tilesA.size());
								groupTilesSize = tilesA.size();

								if (tilesB.isEmpty()) {
								    //This can happen at the end of first iteration if stabilization is disabled, 
								    //or at a later iteration, if stabilization is enabled
									unstableTiles = false;
									currentAdaptiveLevel++;
									continue;
								}
								
								if (unstableIteration > 0) {
								    //NOTE: The warping and clipping job does not affect the stabilization state of the tiles, it only affects the clipped 
								    //image region.
		                            logger.info("Warping and Clipping compute...");
		                            warpingAndClippingJob.compute();
		                            logger.info("Clipping computed...");
		                            //NOTE: This is also redundant, because all job compute calls are working with same input tiles instances, as provided in the
		                            //initialization block outside of the loop, and they are always called from the same thread.
		                            stepTiles = warpingAndClippingJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING);
		                            stepTilesA = stepTiles[0];
		                            stepTilesB = stepTiles[1];
								}
								
								//If doing warping on the first or second image only, only the corresponding filter needs to be performed here,
								//while filtering the other non-warped image as a whole at the start (first iteration only)
								warpedTilesAFilteringJob.setInputParameters(JobResultEnum.JOB_RESULT_TILES, tilesA);
								warpedTilesAFilteringJob.analyze();
								warpedTilesAFilteringJob.compute();
								tilesA = warpedTilesAFilteringJob.getJobResult(JobResultEnum.JOB_RESULT_FILTERED_TILES);

    						    warpedTilesBFilteringJob.setInputParameters(JobResultEnum.JOB_RESULT_TILES, tilesB);
                                warpedTilesBFilteringJob.analyze();
                                warpedTilesBFilteringJob.compute();
                                tilesB = warpedTilesBFilteringJob.getJobResult(JobResultEnum.JOB_RESULT_FILTERED_TILES);
								
								int fromIndex = 0;
								int toIndex = 0;
	
								List<Tile> tilesAChunk = null;
								List<Tile> tilesBChunk = null;
								
								int dataLength = tilesA.size();
								boolean disposeLocally = false;
								do {
									toIndex += groupTilesSize;
									
									List<Matrix> outputCrossLocal = null;
									List<MaxCrossResult> maxCrossResultsLocal = null;
									try {
										if (groupTilesSize >= dataLength) {
											tilesAChunk = tilesA;
											tilesBChunk = tilesB;
										} else {
											if (toIndex > dataLength) {
												tilesAChunk = tilesA.subList(fromIndex, dataLength);
												tilesBChunk = tilesB.subList(fromIndex, dataLength);
											} else {
												tilesAChunk = tilesA.subList(fromIndex, toIndex);
												tilesBChunk = tilesB.subList(fromIndex, toIndex);
											}
										}
										
										openClLevelJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesAChunk);
										openClLevelJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesBChunk);
										openClLevelJob.analyze();
										openClLevelJob.compute();
										
										XCorrelationResults results = openClLevelJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
										outputCrossLocal = results.getCrossMatrices();										
										maxCrossResultsLocal = results.getMaxCrossResults();
										//xCorrResults.add(results);
									} finally {
										if (disposeLocally) {
											openClLevelJob.dispose();
											openClLevelJob = null;
										}
									}
									outputCross.addAll(outputCrossLocal);
									maxResults.addAll(maxCrossResultsLocal);
									
									fromIndex += groupTilesSize;
								}  while (toIndex < dataLength);
								
								if (outputCross.size() != dataLength) {
									throw new JobComputeException("Number of computed cross-correlations doesn't match the expected");
								}														
								
								//Note array list can be reused by adaptive level, even if less tiles are needed due to less unstable tiles
								maxResults = new ArrayList<MaxCrossResult>(dataLength);
								Iterator<Tile> tileAIterator = tilesA.iterator();
								Iterator<Tile> tileBIterator = tilesB.iterator();
								
								//Test code only... not relevant for real usage 
								/*for (int index = 0; index < maxResults.size(); index++) {
									MaxCrossResult maxResult = maxResults.get(index);
									Matrix m = outputCross.get(index);
									//MaxCrossResult result = findPeak.findMaximum(m);
									//if (maxResult.i != result.i || maxResult.j != result.j) {
									//	System.out.println("Index = " + index + " - Max is at (i:" + result.i + ", j:" + result.j + ") - but was found instead at: (i: " + maxResult.i + ", j: " + maxResult.j + ")");
									//}
									maxResult.setAssociatedTileA(tileAIterator.next());
									maxResult.setAssociatedTileB(tileBIterator.next());
									ICrossCorrelationInterpolator interpolator = reusableObjects.getOrCreateInterpolator(); 
									if (interpolator != null && stepTilesB.getCurrentStep() >= inputParameters.getInterpolatorStartStep()) {
										try {
											maxResult = interpolator.interpolate(m, maxResult);
										} catch (InterpolatorStateException ex) {
											System.out.println("Failed to interpolate tile " + maxResult.tileB  + " reason: " + ex.getMessage());
										}
									}
								}*/
								
				                if (runParameters.isCancelRequested()) {
				                    return partialResults;
				                }

								for (Matrix m : outputCross) {
									Tile tileA = tileAIterator.next();
									Tile tileB = tileBIterator.next();
									MaxCrossResult result = findPeak.findMaximum(m);

									//Associate MaxCrossResults with the corresponding tiles... they are in same order...
									result.setAssociatedTileA(tileA);
									result.setAssociatedTileB(tileB);
																		
									maxResults.add(result);
								}
								
                                if (interpolator != null && stepTilesB.getCurrentStep() >= inputParameters.getInterpolatorStartStep()) {
                                   maxResults = interpolator.interpolate(maxResults);
                                }

								//findPeak.dispose();

				                if (runParameters.isCancelRequested()) {
				                    return partialResults;
				                }

								//Check bounds... when clipping...
								stepTilesB.updateDisplacementsFromMaxCrossResults(currentRelativeFrame, maxResults);								
                                if (inputParameters.getMaskFilename() != null) {
                                    if (vectorMaskJobs[currentAdaptiveLevel] == null) {
                                        vectorMaskJobs[currentAdaptiveLevel] = new VectorMaskJob(stepTilesA);
                                    }
                                    VectorMaskJob vectorMaskJob = vectorMaskJobs[currentAdaptiveLevel]; 
                                    vectorMaskJob.setInputParameters(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM, stepTilesB);
                                    vectorMaskJob.analyze();
                                    vectorMaskJob.compute();
                                }
                                
                                if (inputParameters.getVectorValidatorStrategy() != VectorValidatorFactoryEnum.None) { 
    								validatorParameters.stepTiles = stepTilesB;
    								validatorParameters.maxResults = maxResults;
    								validatorParameters.currentFrame = currentRelativeFrame;
    								validatorJob.setInputParameters(JobResultEnum.JOB_RESULT_VALIDATOR_DATA, validatorParameters);
    								validatorJob.analyze();
    								validatorJob.compute();
    								stepTilesB = validatorJob.getJobResult(JobResultEnum.JOB_RESULT_VALIDATED_VECTORS);
                                }
                        
                                //This is redundant, however it may help the compiler to identify target dependencies
                                stepTilesBByLevel[currentAdaptiveLevel] = stepTilesB;
								unstableIteration++;
							}
						}

						IterationStepTiles stepTilesA = stepTilesAByLevel[adaptiveLevels - 1];
	                    IterationStepTiles stepTilesB = stepTilesBByLevel[adaptiveLevels - 1];
                        if (opticalFlowAfterPIVInterpolator != null) {
                            opticalFlowAfterPIVInterpolator.interpolate(stepTilesA, stepTilesB);
                        }

                        //If the last step was a dense optical flow, whether within the adaptive steps or after the last step,
                        //we may need to apply a mask...
                        denseVectorMaskJob.setInputParameters(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM, stepTilesB);
                        denseVectorMaskJob.analyze();
                        denseVectorMaskJob.compute();
                        stepTilesB = denseVectorMaskJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM_MASKED);
						
						//This will just store the last adaptive step PIV results
						//Add partial export to PIV results, since IterationStepTiles will be reused for the next image too...
						PIVMap map = partialResults.getOrCreateMap(0);
						if (!map.isPrepared()) {
						    IImage image = images.get(0);
							map.prepare(inputFiles.getAbsoluteStartFrame(), inputFiles.getRelativeStartFrame(), inputFiles.getFilesA().size(), image.getHeight(), image.getWidth(), stepTilesB);
						}
						map.importFromIterationStepTiles(currentRelativeFrame, stepTilesB);
						
						//Advance frame
						currentRelativeFrame++;
						execStatus.incrementProcessedImages();
					}
				} finally {
					for (int level = 0; level < adaptiveLevels; level++) {
						if (openCLJobs[level] != null) {
							openCLJobs[level].dispose();
							openCLJobs[level] = null;
						}
					}					
				}
				
				return partialResults;
			}
		}.setParameters(parameters);
		
		//Need to create own thread pool extension for CPU affinity
		Future<PIVResults> future = service.submit(callable);
		setJobResult(JobResultEnum.JOB_RESULT_PIV, future);
	}

	public void dispose() {
		if (service != null) {
			service.shutdown();
			try {
				service.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				service.shutdownNow();
			}
			service = null;
		}
	}
}
