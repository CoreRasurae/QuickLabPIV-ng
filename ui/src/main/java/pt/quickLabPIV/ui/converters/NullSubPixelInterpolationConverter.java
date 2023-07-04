package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;

public class NullSubPixelInterpolationConverter
        extends ConverterWithForwardValidator<SubPixelInterpolationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(SubPixelInterpolationModeEnum value) {
        return value;
    }

    @Override
    public SubPixelInterpolationModeEnum convertReverse(Object value) {
        return (SubPixelInterpolationModeEnum)value;
    }

}
