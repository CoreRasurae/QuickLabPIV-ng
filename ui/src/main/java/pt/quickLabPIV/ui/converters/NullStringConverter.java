package pt.quickLabPIV.ui.converters;

public class NullStringConverter extends ConverterWithForwardValidator<String, String> {

    @Override
    public String convertForwardAfterValidation(String value) {
        return value;
    }

    @Override
    public String convertReverse(String value) {
        return value;
    }

}
