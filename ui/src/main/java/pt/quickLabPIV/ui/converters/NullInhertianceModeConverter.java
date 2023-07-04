package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.InheritanceModeEnum;

public class NullInhertianceModeConverter extends ConverterWithForwardValidator<InheritanceModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(InheritanceModeEnum value) {
        return value;
    }

    @Override
    public InheritanceModeEnum convertReverse(Object value) {
        return (InheritanceModeEnum)value;
    }

}
