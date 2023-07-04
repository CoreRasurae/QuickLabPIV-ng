package pt.quickLabPIV.ui.converters;

public class NullGenericFloatConverter extends ConverterWithForwardValidator<Float, Object> {

    @Override
    public Object convertForwardAfterValidation(Float value) {       
        return value;
    }

    @Override
    public Float convertReverse(Object value) {
        return (Float)value;
    }

}
