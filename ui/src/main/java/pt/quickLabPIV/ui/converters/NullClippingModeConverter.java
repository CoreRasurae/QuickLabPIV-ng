package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.ClippingModeEnum;

public class NullClippingModeConverter extends ConverterWithForwardValidator<ClippingModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(ClippingModeEnum value) {
        return value;
    }

    @Override
    public ClippingModeEnum convertReverse(Object value) {
        return (ClippingModeEnum)value;
    }

}
