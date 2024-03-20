// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
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
