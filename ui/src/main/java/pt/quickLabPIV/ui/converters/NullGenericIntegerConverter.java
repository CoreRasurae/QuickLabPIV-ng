// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

public class NullGenericIntegerConverter extends ConverterWithForwardValidator<Integer, Object> {

    @Override
    public Object convertForwardAfterValidation(Integer value) {       
        return value;
    }

    @Override
    public Integer convertReverse(Object value) {
        return (Integer)value;
    }

}
