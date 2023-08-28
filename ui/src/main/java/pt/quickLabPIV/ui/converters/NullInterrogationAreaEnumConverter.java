// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;

public class NullInterrogationAreaEnumConverter
        extends ConverterWithForwardValidator<InterrogationAreaResolutionEnum, Object> {

    @Override
    public InterrogationAreaResolutionEnum convertForwardAfterValidation(InterrogationAreaResolutionEnum value) {
        return value;
    }

    @Override
    public InterrogationAreaResolutionEnum convertReverse(Object value) {
        return (InterrogationAreaResolutionEnum)value;
    }

}
