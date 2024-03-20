// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.images.IImage;

/**
 * JavaNullImageFilterJob implements a Java CPU null image filtering.
 * @author lpnm
 *
 */
public class JavaNullImageFilterJob extends Job<IImage, IImage> {
    private final ImageFilteringModeFactoryEnum imageFilteringMode;
    private IImage inputImage = null;
    
    public JavaNullImageFilterJob() {
       //When implementing GPU accelerated filtered job instances they will also be created from the same PIV input parameters
       PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
       imageFilteringMode = parameters.getImageFilteringMode();
    }
    
    @Override
    public void analyze() {
        inputImage = getInputParameters(JobResultEnum.JOB_RESULT_IMAGES);
        if (inputImage == null) {
            throw new JobAnalyzeException("No images to filter");
        }
        if (imageFilteringMode != ImageFilteringModeFactoryEnum.NoImageFiltering && imageFilteringMode != ImageFilteringModeFactoryEnum.ImageFilteringAfterWarping) {
            throw new JobAnalyzeException("Image filter mode (" + imageFilteringMode + ") is inadequate for JavaNullImageFilterJob");
        }
        
    }

    @Override
    public void compute() {
        setJobResult(JobResultEnum.JOB_RESULT_FILTERED_IMAGES, inputImage);
    }

    @Override
    public void dispose() {
    }

}
