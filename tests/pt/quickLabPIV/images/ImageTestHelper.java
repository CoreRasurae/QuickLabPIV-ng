// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixByte;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.images.ImageReaderException;
import pt.quickLabPIV.jobs.ImageReaderJob;
import pt.quickLabPIV.jobs.JobResultEnum;

public class ImageTestHelper {
	public static byte[] getBuffer(Image img) {
		return img.internalBuffer;
	}

	
	public static BufferedImage getImageBI(String filename) {
	    File file = new File(filename);
	    
        BufferedImage result = null;
	    try {
            result = ImageIO.read(file);
        } catch (IOException e) {
            throw new ImageReaderException("Failed to read file: " + file.getAbsolutePath(), e);
        }
	    
	    return result;
	}

	public static IImage getImage(String filename) {
        PIVContextTestsSingleton.setSingletonIfNotSetAlready();
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        singleton.getPIVParameters().setPixelDepth(ImageFactoryEnum.Image8Bit);
	    ImageReaderJob job1 = new ImageReaderJob(filename);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		
		return img1;
	}
	
	public static Matrix getMatrixFromClippedImage(String filename, int top, int left, int height, int width) {
	    PIVContextTestsSingleton.setSingletonIfNotSetAlready();;
	    PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        singleton.getPIVParameters().setPixelDepth(ImageFactoryEnum.Image8Bit);
		ImageReaderJob job1 = new ImageReaderJob(filename);
		job1.analyze();
		job1.compute();
		
		IImage img1 = job1.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		
		return img1.clipImageMatrix(top, left, height, width, false, null);
	}
	
	public static void writeMatrixToImage(MatrixByte m, String filename) {
		IImage img = new Image(m, m.getWidth(), m.getHeight(), filename);
		img.writeToFile(true);
	}
}
