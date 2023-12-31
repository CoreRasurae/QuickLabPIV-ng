// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.ClippingModeEnum;

public class NullClippingModeConverter extends ConverterWithForwardValidator<ClippingModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(ClippingModeEnum value) {
        return value;
    }

    @Override
    public ClippingModeEnum convertReverse(Object value) {
        return (ClippingModeEnum)value;
    }

}
