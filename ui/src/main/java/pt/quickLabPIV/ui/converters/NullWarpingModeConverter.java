// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.WarpingModeEnum;

public class NullWarpingModeConverter extends ConverterWithForwardValidator<WarpingModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(WarpingModeEnum value) {
        return value;
    }

    @Override
    public WarpingModeEnum convertReverse(Object value) {
        return (WarpingModeEnum)value;
    }

}
