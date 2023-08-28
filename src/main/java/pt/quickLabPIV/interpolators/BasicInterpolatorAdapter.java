// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class BasicInterpolatorAdapter implements ICrossCorrelationInterpolator {
    private static final Logger logger = LoggerFactory.getLogger(BasicInterpolatorAdapter.class);
    private IBasicCrossCorrelationInterpolator basicInterpolator;

    public BasicInterpolatorAdapter(IBasicCrossCorrelationInterpolator _interpolator) {
        basicInterpolator = _interpolator;
    }
    
    @Override
    public List<MaxCrossResult> interpolate(List<MaxCrossResult> results) {
        for (MaxCrossResult result : results) {
            try {
                basicInterpolator.interpolate(result.getCrossMatrix(), result);
            } catch (InterpolatorFailedException e) {
                Tile tile = result.getTileA();
                //Just because the interpolator couldn't interpolate an individual MaxCrossResult is not sufficient to fail the processing
                //and is expected to happen, since sometimes the correlation peak location is located too near the margins. 
                logger.warn("Faile to interpolate MaxCrossResult for Tile at I:{}, J:{} at step: {}", tile.getTileIndexI(), tile.getTileIndexJ(), tile.getParentIterationStepTiles().getCurrentStep());
            }
        }
        
        return results;
    }

}
