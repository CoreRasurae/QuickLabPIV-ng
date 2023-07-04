package pt.quickLabPIV.ui.converters;

import org.jdesktop.beansbinding.Converter;

import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;

public class IAResolutionToStringConverter extends Converter<InterrogationAreaResolutionEnum, String> {

    @Override
    public String convertForward(InterrogationAreaResolutionEnum value) {
        return value.getDescription();
    }

    @Override
    public InterrogationAreaResolutionEnum convertReverse(String value) {
        throw new UIException("Unsupported conversion");
    }

}
