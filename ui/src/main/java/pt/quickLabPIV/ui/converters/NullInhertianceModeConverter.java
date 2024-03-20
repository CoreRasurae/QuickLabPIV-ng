// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.InheritanceModeEnum;

public class NullInhertianceModeConverter extends ConverterWithForwardValidator<InheritanceModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(InheritanceModeEnum value) {
        return value;
    }

    @Override
    public InheritanceModeEnum convertReverse(Object value) {
        return (InheritanceModeEnum)value;
    }

}
