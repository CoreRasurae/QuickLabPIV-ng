package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;

public class NullInterrogationAreaEnumConverter
        extends ConverterWithForwardValidator<InterrogationAreaResolutionEnum, Object> {

    @Override
    public InterrogationAreaResolutionEnum convertForwardAfterValidation(InterrogationAreaResolutionEnum value) {
        return value;
    }

    @Override
    public InterrogationAreaResolutionEnum convertReverse(Object value) {
        return (InterrogationAreaResolutionEnum)value;
    }

}
