package pt.quickLabPIV.ui.converters;

public class NullObjectConverter extends ConverterWithForwardValidator<Object, Object> {

    @Override
    public Object convertForwardAfterValidation(Object value) {
        return value;
    }

    @Override
    public Object convertReverse(Object value) {
        return value;
    }

}
