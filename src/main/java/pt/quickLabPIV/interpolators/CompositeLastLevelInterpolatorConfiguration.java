// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

public class CompositeLastLevelInterpolatorConfiguration {
    public static final String IDENTIFIER = "interpCompositeLastLevel";
    
    private boolean applyFirstLevelsInterpolatorAtLastStep;
    private CrossCorrelationInterpolatorFactoryEnum firstLevelsInterpolator;
    private CrossCorrelationInterpolatorFactoryEnum lastLevelInterpolator;    
    
    public void setFirstLevelInterpolator(CrossCorrelationInterpolatorFactoryEnum _firstLevelsInterpolator) {
        firstLevelsInterpolator = _firstLevelsInterpolator;
    }
    
    public CrossCorrelationInterpolatorFactoryEnum getFirstLevelInterpolator() {
        if (firstLevelsInterpolator == null) {
            throw new InterpolatorStateException("CompositeLastLEvelInterpolatorConfiguration has a null interpolator");
        }
        return firstLevelsInterpolator;
    }
    
    public void setLastLevelInterpolator(CrossCorrelationInterpolatorFactoryEnum _lastLevelInterpolator) {
        lastLevelInterpolator = _lastLevelInterpolator;
    }
    
    public CrossCorrelationInterpolatorFactoryEnum getLastLevelInterpolator() {
        if (lastLevelInterpolator == null) {
            throw new InterpolatorStateException("CompositeLastLEvelInterpolatorConfiguration has a null last interpolator");
        }

        return lastLevelInterpolator;
    }

    public void setApplyMainInterpolatorAtLastStep(boolean applyAtLastStep) {
        applyFirstLevelsInterpolatorAtLastStep = applyAtLastStep;
    }
    
    public boolean isApplyMainInterpolatorAtLastStep() {
        return applyFirstLevelsInterpolatorAtLastStep;
    }
}
