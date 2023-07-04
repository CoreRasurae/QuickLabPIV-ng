package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

public class NotNullValidator extends Validator<Object> {
    private String message = "Value cannot be null";
    
    
    @Override
    public Validator<Object>.Result validate(Object value) {
        Validator<Object>.Result result = null;
        if (value == null) {
            result = new NotNullValidator.Result(null, message);
        }
        
        return result;
    }

}
