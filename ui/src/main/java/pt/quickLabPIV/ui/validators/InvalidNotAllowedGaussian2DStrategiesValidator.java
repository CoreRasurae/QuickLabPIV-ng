package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.Gaussian2DStrategiesEnum;

public class InvalidNotAllowedGaussian2DStrategiesValidator extends Validator<Gaussian2DStrategiesEnum> {

    @Override
    public Validator<Gaussian2DStrategiesEnum>.Result validate(Gaussian2DStrategiesEnum value) {
        Validator<Gaussian2DStrategiesEnum>.Result result = null;
        
        if (value == Gaussian2DStrategiesEnum.Invalid) {
            result = new InvalidNotAllowedGaussian2DStrategiesValidator.Result(null, "Invalid Gaussian algorithm is selected.");
        }
        
        return result;
    }

}
