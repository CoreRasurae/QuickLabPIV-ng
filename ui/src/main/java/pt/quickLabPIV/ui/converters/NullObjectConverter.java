// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

public class NullObjectConverter extends ConverterWithForwardValidator<Object, Object> {

    @Override
    public Object convertForwardAfterValidation(Object value) {
        return value;
    }

    @Override
    public Object convertReverse(Object value) {
        return value;
    }

}
