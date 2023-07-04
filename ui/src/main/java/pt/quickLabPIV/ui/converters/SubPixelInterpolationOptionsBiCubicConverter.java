package pt.quickLabPIV.ui.converters;

import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsBiCubicModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsModel;

public class SubPixelInterpolationOptionsBiCubicConverter extends ConverterWithForwardValidator<SubPixelInterpolationOptionsModel, SubPixelInterpolationOptionsBiCubicModel> {

    @Override
    public SubPixelInterpolationOptionsBiCubicModel convertForwardAfterValidation(SubPixelInterpolationOptionsModel value) {
        return (SubPixelInterpolationOptionsBiCubicModel)value;
    }

    @Override
    public SubPixelInterpolationOptionsModel convertReverse(SubPixelInterpolationOptionsBiCubicModel value) {
        return value;
    }

}
