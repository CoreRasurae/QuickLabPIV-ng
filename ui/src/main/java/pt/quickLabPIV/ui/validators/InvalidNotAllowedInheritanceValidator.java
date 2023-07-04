package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.InheritanceModeEnum;

public class InvalidNotAllowedInheritanceValidator extends Validator<InheritanceModeEnum> {

    @Override
    public Validator<InheritanceModeEnum>.Result validate(InheritanceModeEnum value) {
        Validator<InheritanceModeEnum>.Result result = null;
        
        if (value == InheritanceModeEnum.Invalid) {
            result = new InvalidNotAllowedInheritanceValidator.Result(null, "Selected inheritance strategy is invalid.");
        }
        
        return result;
    }

}
