// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import org.jdesktop.beansbinding.Converter;

import pt.quickLabPIV.exceptions.UIException;

public class IntegerToStringConverter extends Converter<Integer, String> {

    @Override
    public String convertForward(Integer value) {
        return String.valueOf(value);
    }

    @Override
    public Integer convertReverse(String value) {
        throw new UIException("Not supported conversion");
    }

}
