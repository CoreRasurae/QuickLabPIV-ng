// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

public class NullMarginsConverter extends ConverterWithForwardValidator<Integer, Object> {
    @Override
    public Integer convertForwardAfterValidation(Integer value) {        
        //return String.valueOf(value);
        return value;
    }

    @Override
    public Integer convertReverse(Object value) {
        /*int result = Integer.MAX_VALUE;
        try {
            result = Integer.parseInt((String)value);
        } catch (NumberFormatException e) {
            //Ignored - this will Integer.MAX_VALUE to be returned
        }
        return result;*/
        return (Integer)value;
    }
}
