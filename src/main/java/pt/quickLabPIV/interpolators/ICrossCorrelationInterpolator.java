// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import java.util.List;

import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface ICrossCorrelationInterpolator {
	public List<MaxCrossResult> interpolate(final List<MaxCrossResult> results);

	public default boolean isImagesRequired() {
        return false;
    }
	
    public default void updateImageA(IImage img) {
        
    }
    
    public default void updateImageB(IImage img) {
        
    }

}
