package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.WarpingModeEnum;

public class NullWarpingModeConverter extends ConverterWithForwardValidator<WarpingModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(WarpingModeEnum value) {
        return value;
    }

    @Override
    public WarpingModeEnum convertReverse(Object value) {
        return (WarpingModeEnum)value;
    }

}
