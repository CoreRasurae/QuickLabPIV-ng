package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;

public class NullImageFilteringModeConverter extends ConverterWithForwardValidator<ImageFilteringModeEnum, Object> {

    @Override
    public Object convertForwardAfterValidation(ImageFilteringModeEnum value) {
        return value;
    }

    @Override
    public ImageFilteringModeEnum convertReverse(Object value) {
        return (ImageFilteringModeEnum)value;
    }

}
