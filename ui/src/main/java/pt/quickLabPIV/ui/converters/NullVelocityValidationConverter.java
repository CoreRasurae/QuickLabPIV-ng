package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;

public class NullVelocityValidationConverter
        extends ConverterWithForwardValidator<VelocityValidationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(VelocityValidationModeEnum value) {
        return value;
    }

    @Override
    public VelocityValidationModeEnum convertReverse(Object value) {
        return (VelocityValidationModeEnum)value;
    }

}
