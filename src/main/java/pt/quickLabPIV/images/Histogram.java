// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

public final class Histogram {
    private int intensityValue;
    private int count;
    
    public Histogram(int _intensityValue) {
        if (intensityValue > 255 || intensityValue < 0) {
            throw new HistogramException("Invalid intensity value");
        }
        intensityValue = _intensityValue;
        count = 0;
    }
    
    public void incrementCount() {
        count++;
    }
    
    public int getIntensityValue() {
        return intensityValue;
    }
    
    public int getCount() {
        return count;
    }

}
