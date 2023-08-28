// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

public class NullGenericFloatConverter extends ConverterWithForwardValidator<Float, Object> {

    @Override
    public Object convertForwardAfterValidation(Float value) {       
        return value;
    }

    @Override
    public Float convertReverse(Object value) {
        return (Float)value;
    }

}
