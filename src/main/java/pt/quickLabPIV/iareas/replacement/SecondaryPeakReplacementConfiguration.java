// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas.replacement;

public class SecondaryPeakReplacementConfiguration {
    public static final String IDENTIFIER = "SECONDARY_PEAK";
    
    private int peakIndex;
    
    public SecondaryPeakReplacementConfiguration(int _peakIndex) {
        peakIndex = _peakIndex;
    }
    
    public int getPeakIndex() {
        return peakIndex;
    }

}
