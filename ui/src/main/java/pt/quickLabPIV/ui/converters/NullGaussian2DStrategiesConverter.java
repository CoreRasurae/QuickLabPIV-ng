// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.Gaussian2DStrategiesEnum;

public class NullGaussian2DStrategiesConverter extends ConverterWithForwardValidator<Gaussian2DStrategiesEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(Gaussian2DStrategiesEnum value) {
        return value;
    }

    @Override
    public Gaussian2DStrategiesEnum convertReverse(Object value) {
        return (Gaussian2DStrategiesEnum)value;
    }

}
