package pt.quickLabPIV.ui.converters;

import org.jdesktop.beansbinding.Converter;

import pt.quickLabPIV.exceptions.UIException;

public class IntegerToStringConverter extends Converter<Integer, String> {

    @Override
    public String convertForward(Integer value) {
        return String.valueOf(value);
    }

    @Override
    public Integer convertReverse(String value) {
        throw new UIException("Not supported conversion");
    }

}
