// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
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
