// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class CompositeLastLevelInterpolator implements ICrossCorrelationInterpolator {
    private boolean applyFirstLevelsInterpolatorAtLastStep;
    private ICrossCorrelationInterpolator firstLevelsInterpolator;
    private ICrossCorrelationInterpolator lastLevelInterpolator;
        
    public CompositeLastLevelInterpolator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(CompositeLastLevelInterpolatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new InterpolatorStateException("Couldn't retrieve Composite Last Level interpolator configuration");
        }
        
        CompositeLastLevelInterpolatorConfiguration configuration = (CompositeLastLevelInterpolatorConfiguration)configurationObject;

        applyFirstLevelsInterpolatorAtLastStep = configuration.isApplyMainInterpolatorAtLastStep();
        firstLevelsInterpolator = CrossCorrelationInterpolatorFactoryEnum.createInterpolator(configuration.getFirstLevelInterpolator());
        lastLevelInterpolator = CrossCorrelationInterpolatorFactoryEnum.createInterpolator(configuration.getLastLevelInterpolator());
    }
    
    @Override
    public List<MaxCrossResult> interpolate(List<MaxCrossResult> results) {
        IterationStepTiles stepTiles = results.get(0).getTileB().getParentIterationStepTiles();
        boolean isLastStep = stepTiles.getCurrentStep() != stepTiles.getMaxAdaptiveSteps() - 1;
        
        if (!isLastStep || applyFirstLevelsInterpolatorAtLastStep) {
            if (firstLevelsInterpolator != null) {
                results = firstLevelsInterpolator.interpolate(results);
            }
        }
        
        if (isLastStep) {
            if (lastLevelInterpolator != null) {
                results = lastLevelInterpolator.interpolate(results);
            }
        }
        
        return results;
    }
    
    @Override
    public boolean isImagesRequired() {
        return firstLevelsInterpolator != null &&
               firstLevelsInterpolator.isImagesRequired() || 
               lastLevelInterpolator != null &&
               lastLevelInterpolator.isImagesRequired();
    }

    @Override
    public void updateImageA(IImage img) {
       if (firstLevelsInterpolator != null && firstLevelsInterpolator.isImagesRequired()) {
           firstLevelsInterpolator.updateImageA(img);
       }
       
       if (lastLevelInterpolator != null && lastLevelInterpolator.isImagesRequired()) {
           lastLevelInterpolator.updateImageA(img);
       }
    }
    
    @Override
    public void updateImageB(IImage img) {
        if (firstLevelsInterpolator != null && firstLevelsInterpolator.isImagesRequired()) {
            firstLevelsInterpolator.updateImageB(img);
        }
        
        if (lastLevelInterpolator != null && lastLevelInterpolator.isImagesRequired()) {
            lastLevelInterpolator.updateImageB(img);
        }        
    }
}
