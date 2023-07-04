package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;

public class InvalidNotAllowedVelocityValidationValidator extends Validator<VelocityValidationModeEnum> {

    @Override
    public Validator<VelocityValidationModeEnum>.Result validate(VelocityValidationModeEnum value) {
        Validator<VelocityValidationModeEnum>.Result result = null;
        if (value == VelocityValidationModeEnum.Disabled) {
            result = new InvalidNotAllowedVelocityValidationValidator.Result(null, "Selected validation strategy is invalid.\nConsider disabling validation.");
        }
        return result;
    }

}
