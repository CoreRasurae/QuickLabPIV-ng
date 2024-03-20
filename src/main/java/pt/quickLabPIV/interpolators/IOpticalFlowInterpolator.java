// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.iareas.IterationStepTiles;

public interface IOpticalFlowInterpolator extends ICrossCorrelationInterpolator {
    @Override 
    public default boolean isImagesRequired() {
        return true;
    }

    void interpolate(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB);
}
