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
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;

/**
 * JavaImageFilterJob implements a Java CPU image filtering, targeting same resolution input images at all times.
 * @author lpnm
 *
 */
public class JavaImageFilterJob extends Job<IImage, IImage> {
    final IFilter filter;
    final ImageFilteringModeFactoryEnum imageFilteringMode;
    private IImage inputImage = null;
    private IImage outputImage = null;
    
    public JavaImageFilterJob() {
       //When implementing GPU accelerated filtered job instances they will also be created from the same PIV input parameters
       PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
       imageFilteringMode = parameters.getImageFilteringMode();
       filter = ImageFilterFactoryEnum.create(parameters);
    }
    
    @Override
    public void analyze() {
        inputImage = getInputParameters(JobResultEnum.JOB_RESULT_IMAGES);
        if (inputImage == null) {
            throw new JobAnalyzeException("No images to filter");
        }
        if (filter == null) {
            throw new JobAnalyzeException("No filter available for image filtering");
        }

        if (outputImage == null) {
            outputImage = ImageFloat.sizeFrom(inputImage);
        }
    }

    @Override
    public void compute() {
        outputImage = filter.applyFilter(inputImage, outputImage);
        setJobResult(JobResultEnum.JOB_RESULT_FILTERED_IMAGES, outputImage);
    }

    @Override
    public void dispose() {
    }

}
