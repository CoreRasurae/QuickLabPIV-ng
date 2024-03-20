// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

/**
 * Common optional configuration properties for all exporters.
 * 
 * @author lpnm
 */
public class GenericExporterOptionalConfiguration {
    private final boolean markInvalidAsNaN;
    
    public GenericExporterOptionalConfiguration(boolean _markInvalidAsNaN) {
        markInvalidAsNaN = _markInvalidAsNaN;
    }
    
    public boolean isMarkInvalidAsNaN() {
        return markInvalidAsNaN;
    }
}
