// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
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
