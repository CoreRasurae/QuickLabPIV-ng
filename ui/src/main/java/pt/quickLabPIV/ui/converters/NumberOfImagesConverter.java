// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

public class NumberOfImagesConverter extends ConverterWithForwardValidator<Integer, String> {

    @Override
    public String convertForwardAfterValidation(Integer value) {
        return String.valueOf(value);
    }

    @Override
    public Integer convertReverse(String value) {
        int result = 0;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            //Ignore... return as 0 which is already invalid
        }
        return result;
    }

}
