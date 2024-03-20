// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.images.ImageNotFoundException;
import pt.quickLabPIV.images.ImageReaderException;

/**
 * ImageReaderJob is a job intended to run under a dedicated thread, managed by a JobManager.
 * It reads a set of images.
 * @author lpnm
 *
 */
public class ImageReaderJob extends Job<String, List<IImage>> {
    private static Logger logger = LoggerFactory.getLogger(ImageReaderJob.class);
	private List<File> files;
	private PIVInputParameters parameters;
	
	public ImageReaderJob() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		parameters = singleton.getPIVParameters();
	}
	
	public ImageReaderJob(final List<String> _filenames) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        parameters = singleton.getPIVParameters();
	    files = new ArrayList<File>(_filenames.size());
		for (String filename : _filenames) {
			File f = new File(filename);
			files.add(f);
		}
	}
	
	public ImageReaderJob(final String filename) {
	    PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
	    parameters = singleton.getPIVParameters();
		files = new ArrayList<File>(1);
		files.add(new File(filename));
	}

	public void setFilenamesToRead(final File[] _filenames) {
		if (files == null) {
			files = new ArrayList<File>(2);
		}
		files.clear();
		Collections.addAll(files, _filenames);
	}
	
	public void analyze() {
		for (File f : files) {			
			if (!f.exists() || f.isDirectory()) {
				throw new ImageNotFoundException("Cannot find image file"); 
			}
			
			if (!f.canRead()) {
				throw new ImageReaderException("Insuficient permissions to read file");
			}
		}
	}
	
	public void compute() {
		BufferedImage bi = null;
		List<IImage> images = getJobResult(JobResultEnum.JOB_RESULT_IMAGES);
		if (images == null) {
			images = new ArrayList<IImage>(files.size());
		}
		
		images.clear();
		
		for (File file : files) {
			try {
				bi = ImageIO.read(file);
			} catch (IOException e) {
				throw new ImageReaderException("Failed to read file: " + file.getAbsolutePath(), e);
			}
			
			if (bi == null) {
			    //Invalid image file, library should have given an error, but sometimes it returns a null buffer when it can't read
			    logger.error("Couldn't read image from file: " + file.getAbsolutePath());
			    throw new ImageReaderException("Couldn't read image from file: " + file.getAbsolutePath());
			}
			
			IImage img = ImageFactoryEnum.create(parameters, bi, file.getAbsolutePath());
			images.add(img);
		}
		
		clearResults();
		setJobResult(JobResultEnum.JOB_RESULT_IMAGES, images);
	}

	@Override
	public void dispose() {
		
	}	
}
