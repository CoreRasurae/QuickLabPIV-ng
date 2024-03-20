// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

public class NullStringConverter extends ConverterWithForwardValidator<String, String> {

    @Override
    public String convertForwardAfterValidation(String value) {
        return value;
    }

    @Override
    public String convertReverse(String value) {
        return value;
    }

}
