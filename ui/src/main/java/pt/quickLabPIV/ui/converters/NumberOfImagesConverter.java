package pt.quickLabPIV.ui.converters;

public class NumberOfImagesConverter extends ConverterWithForwardValidator<Integer, String> {

    @Override
    public String convertForwardAfterValidation(Integer value) {
        return String.valueOf(value);
    }

    @Override
    public Integer convertReverse(String value) {
        int result = 0;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            //Ignore... return as 0 which is already invalid
        }
        return result;
    }

}
