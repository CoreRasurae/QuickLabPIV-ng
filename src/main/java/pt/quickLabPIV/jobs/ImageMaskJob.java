// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageNotFoundException;
import pt.quickLabPIV.images.ImageReaderException;

public class ImageMaskJob extends Job<List<IImage>, List<IImage>> {
    private String maskFilename;
    private Image mask;

    public ImageMaskJob(String maskName) {
        maskFilename = maskName;
    }
    
    @Override
    public void analyze() {
        if (mask == null) {
            File f = new File(maskFilename);
            if (!f.exists() || f.isDirectory()) {
                throw new ImageNotFoundException("Cannot find image mask file"); 
            }
            
            if (!f.canRead()) {
                throw new ImageReaderException("Insuficient permissions to read mask file");
            }
            
            BufferedImage bi;
            try {
                bi = ImageIO.read(f);
            } catch (IOException e) {
                throw new ImageReaderException("Failed to read mask file: " + f.getAbsolutePath(), e);
            }
            
            mask = new Image(bi, f.getAbsolutePath());
            List<IImage> masks = new ArrayList<IImage>();
            masks.add(mask);
            setJobResult(JobResultEnum.JOB_RESULT_MASK, masks);
        }        
    }

    @Override
    public void compute() {
        List<IImage> images = getInputParameters(JobResultEnum.JOB_RESULT_IMAGES_TO_MASK);
        for (IImage image : images) {
            image.applyMask(mask);
        }
        setJobResult(JobResultEnum.JOB_RESULT_IMAGES, images);
    }

    @Override
    public void dispose() {
    }

}
