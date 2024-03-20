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
import java.util.List;

import javax.imageio.ImageIO;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageNotFoundException;
import pt.quickLabPIV.images.ImageReaderException;

public class VectorMaskJob extends Job<IterationStepTiles, IterationStepTiles> {
    private boolean shouldMask;
    private String maskFilename;
    private IImage maskImage;
    private IterationStepTiles stepTiles;
    private boolean[][] maskMatrix;
    
    public VectorMaskJob(IterationStepTiles _stepTiles) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters inputParameters = singleton.getPIVParameters();
        shouldMask = !inputParameters.isMaskOnlyAtExport() || (!inputParameters.isDenseExport() && _stepTiles.getCurrentStep() == _stepTiles.getMaxAdaptiveSteps() - 1);
        stepTiles = _stepTiles; 
        maskFilename = inputParameters.getMaskFilename();
    }
    
    @Override
    public void analyze() {
        if (maskMatrix == null && shouldMask) {
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
            
            maskImage = new Image(bi, f.getAbsolutePath());
            
            maskMatrix = new boolean[stepTiles.getNumberOfTilesInI()][stepTiles.getNumberOfTilesInJ()];
            for (int i = 0; i < stepTiles.getNumberOfTilesInI(); i++) {
                for (int j = 0; j < stepTiles.getNumberOfTilesInJ(); j++) {
                    Tile tile = stepTiles.getTile(i, j);
                    int count = maskImage.getSpecificIntensityValueCountForRegion(tile.getTopPixel(), tile.getLeftPixel(), 
                            stepTiles.getTileHeight(), stepTiles.getTileWidth(), 0);
                    if ((float)count/(float)(stepTiles.getTileHeight() * stepTiles.getTileWidth()) > 0.35f) {
                        maskMatrix[i][j] = true;
                    }
                }
            }
        }
    }

    @Override
    public void compute() {
        if (!shouldMask) {
            return;
        }
        
        IterationStepTiles stepTiles = getInputParameters(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM);
        for (int i = 0; i < stepTiles.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTiles.getNumberOfTilesInJ(); j++) {
                if (maskMatrix[i][j]) {
                    Tile tile = stepTiles.getTile(i, j);
                    tile.replaceDisplacement(0.0f, 0.0f);
                    tile.setMaskedDisplacement(true);
                }                
            }
        }
        setJobResult(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM_MASKED, stepTiles);
    }

    @Override
    public void dispose() {
        
    }

}
