// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.validator;

public class VectorValidatorConfiguration {

    public static final String IDENTIFIER = "VectorValidator";
    
    private final int maxCorrectionIterations;
    private final boolean iterateUntilNoMoreCorrections;
    
    public VectorValidatorConfiguration(int _maxCorrectionIterations, boolean _iterateUntilNoMoreCorrections) {
        maxCorrectionIterations = _maxCorrectionIterations;
        iterateUntilNoMoreCorrections = _iterateUntilNoMoreCorrections;
    }
    
    public int getMaxCorrectionIterations() {
        return maxCorrectionIterations;
    }
    
    public boolean isIterateUntilNoMoreCorrections() {
        return iterateUntilNoMoreCorrections;
    }
}
