// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import org.jdesktop.beansbinding.Converter;

import pt.quickLabPIV.exceptions.UIException;

public class ImageResolutionConverter extends Converter<int[], String> {

    @Override
    public String convertForward(int[] value) {
        StringBuilder sb = new StringBuilder(30);
        sb.append(value[0]);
        sb.append("x");
        sb.append(value[1]);
        
        return sb.toString();
    }

    @Override
    public int[] convertReverse(String value) {
        throw new UIException("Image resolution conversion", "Unsupported reverse convertion operation");
    }

}
