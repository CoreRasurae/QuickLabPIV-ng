package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;

public class NullVelocityStabilizationConverter
        extends ConverterWithForwardValidator<VelocityStabilizationModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(VelocityStabilizationModeEnum value) {
        return value;
    }

    @Override
    public VelocityStabilizationModeEnum convertReverse(Object value) {
        return (VelocityStabilizationModeEnum)value;
    }

}
