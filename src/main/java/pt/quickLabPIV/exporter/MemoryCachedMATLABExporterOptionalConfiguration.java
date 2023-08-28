// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.exporter;

public class MemoryCachedMATLABExporterOptionalConfiguration extends GenericExporterOptionalConfiguration {

    public static final String IDENTIFIER = "MemCachedMATLABExporterOptional";
    
    public MemoryCachedMATLABExporterOptionalConfiguration(boolean _markInvalidAsNaN) {
        super(_markInvalidAsNaN);
    }

}
