// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;

public class NullSubPixelInterpolationConverter
        extends ConverterWithForwardValidator<SubPixelInterpolationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(SubPixelInterpolationModeEnum value) {
        return value;
    }

    @Override
    public SubPixelInterpolationModeEnum convertReverse(Object value) {
        return (SubPixelInterpolationModeEnum)value;
    }

}
