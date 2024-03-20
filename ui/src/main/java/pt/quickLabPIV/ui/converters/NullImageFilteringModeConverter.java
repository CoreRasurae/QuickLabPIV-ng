// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;

public class NullImageFilteringModeConverter extends ConverterWithForwardValidator<ImageFilteringModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(ImageFilteringModeEnum value) {
        return value;
    }

    @Override
    public ImageFilteringModeEnum convertReverse(Object value) {
        return (ImageFilteringModeEnum)value;
    }

}
