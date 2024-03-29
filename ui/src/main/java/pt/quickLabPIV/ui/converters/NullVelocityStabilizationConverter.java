// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;

public class NullVelocityStabilizationConverter
        extends ConverterWithForwardValidator<VelocityStabilizationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(VelocityStabilizationModeEnum value) {
        return value;
    }

    @Override
    public VelocityStabilizationModeEnum convertReverse(Object value) {
        return (VelocityStabilizationModeEnum)value;
    }

}
