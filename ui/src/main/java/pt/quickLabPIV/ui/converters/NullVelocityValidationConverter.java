// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;

public class NullVelocityValidationConverter
        extends ConverterWithForwardValidator<VelocityValidationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(VelocityValidationModeEnum value) {
        return value;
    }

    @Override
    public VelocityValidationModeEnum convertReverse(Object value) {
        return (VelocityValidationModeEnum)value;
    }

}
