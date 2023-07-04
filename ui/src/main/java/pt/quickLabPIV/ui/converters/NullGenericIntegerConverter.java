package pt.quickLabPIV.ui.converters;

public class NullGenericIntegerConverter extends ConverterWithForwardValidator<Integer, Object> {

    @Override
    public Object convertForwardAfterValidation(Integer value) {       
        return value;
    }

    @Override
    public Integer convertReverse(Object value) {
        return (Integer)value;
    }

}
