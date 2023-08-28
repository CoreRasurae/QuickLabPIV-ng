// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.exceptions.UIException;

public class IntegerRangeValidator extends Validator<Integer> {
    public enum RangeTypeEnum {
        ANY,
        EVEN,
        ODD
    }

    private int min = 0;
    private int max = Integer.MAX_VALUE;
    private RangeTypeEnum rangeType = RangeTypeEnum.ANY;
    
    public void setMinAndMax(int newMin, int newMax) {
        if (newMin > newMax) {
            throw new UIException("Range validation error", "Minimum value must be less or equal to maximum value.");
        }
        
        min = newMin;
        max = newMax;
    }
    
    public void setRangeType(RangeTypeEnum newRangeType) {
        rangeType = newRangeType;
    }
    
    @Override
    public Validator<Integer>.Result validate(Integer value) {
        Validator<Integer>.Result result = null;
        if (value < min || value > max) {
            result = new IntegerRangeValidator.Result(null, "Value must be between " + min + " and " + max + " including range limit values.");
        }
        
        if (rangeType == RangeTypeEnum.ODD) {
            boolean isOdd = value % 2 == 1;
            if (!isOdd) {
                result = new IntegerRangeValidator.Result(null, "Value must be an odd value.");
            }
        } else if (rangeType == RangeTypeEnum.EVEN) {
            boolean isEven = value % 2 == 0;
            if (!isEven) {
                result = new IntegerRangeValidator.Result(null, "Value must be an even value.");
            }
        }
        
        return result;
    }

}
