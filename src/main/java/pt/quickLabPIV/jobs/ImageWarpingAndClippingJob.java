// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.IImageWarpingStrategy;

public class ImageWarpingAndClippingJob extends Job<ImageWarpingInputData, IterationStepTiles[]> {

    private final IImageWarpingStrategy warpingStrategy;
    private IImage imgA;
    private IImage imgB;
    private IterationStepTiles stepTilesA;
    private IterationStepTiles stepTilesB;
    private IterationStepTiles[] result = new IterationStepTiles[2];
    
    public ImageWarpingAndClippingJob() {
        //When implementing GPU accelerated warping job instances they will also be created from the same PIV input parameters
        PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
        warpingStrategy = WarpingModeFactoryEnum.create(parameters);
    }
    
    @Override
    public void analyze() {
        ImageWarpingInputData inputData = getInputParameters(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING);
        imgA = inputData.imageA;
        imgB = inputData.imageB;
        stepTilesA = inputData.stepTilesA;
        stepTilesB = inputData.stepTilesB;
        
        //TODO Do validation checks
    }

    @Override
    public void compute() {
        stepTilesA.incrementCurrentStepRetries();
        stepTilesB.incrementCurrentStepRetries();
        warpingStrategy.warpAndClipImage(imgA, imgB, stepTilesA, stepTilesB);
        result[0] = stepTilesA;
        result[1] = stepTilesB;
        setJobResult(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING, result);
    }

    @Override
    public void dispose() {
        
    }
}
