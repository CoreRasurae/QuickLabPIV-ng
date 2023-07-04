package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;

public class InvalidNotAllowedVelocityStabilizationValidator extends Validator<VelocityStabilizationModeEnum> {

    @Override
    public Validator<VelocityStabilizationModeEnum>.Result validate(VelocityStabilizationModeEnum value) {
        Validator<VelocityStabilizationModeEnum>.Result result = null;
        if (value == VelocityStabilizationModeEnum.Disabled) {
            result = new InvalidNotAllowedVelocityStabilizationValidator.Result(null, "Selected stabilization strategy is invalid.\nConsider disabling stabilization.");
        }
        return result;
    }

}
