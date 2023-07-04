package pt.quickLabPIV.ui.validators;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.PIVImageTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class DifferentStringValidator extends Validator<String> implements PropertyChangeListener {
    private final String otherPropertyName;
    private String errorMessage = "Patterns must not be equal";
    private String otherValue;
    private String lastValidatedString;
    private boolean enabled;
    private boolean isErrored = false;
    private ErrorBorderForComponent errorBorder;
    
    public DifferentStringValidator(String _otherPropertyName) {
        otherPropertyName = _otherPropertyName;
    }
    
    public void setInitialOtherValue(String value, boolean _enabled) {
        otherValue = value;
        enabled = _enabled;
    }
    
    public void setErrorBorder(ErrorBorderForComponent border) {
        errorBorder = border;
    }
    
    @Override
    public Validator<String>.Result validate(String value) {
        DifferentStringValidator.Result r = null;
        
        lastValidatedString = value;
        
        if (otherValue != null && !otherValue.isEmpty() && otherValue.equals(value)) {
            if (enabled) {
                r = new DifferentStringValidator.Result(null, errorMessage);
            }
            //Error state is always recorded, so that error can be triggered if it becomes enabled,
            //by changes on other components, e.g. imageType.
            isErrored = true;
        } else {
            isErrored = false;
        }
        
        return r;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("imageType".equals(evt.getPropertyName())) {
           PIVImageTypeEnum imageType = (PIVImageTypeEnum)evt.getNewValue();
           if (imageType == PIVImageTypeEnum.PIVImagePair) {
               enabled = true;
               if (isErrored && errorBorder != null) {
                   errorBorder.updateStatus(new DifferentStringValidator.Result(null, errorMessage));
               }
           } else {
               enabled = false;
               if (isErrored && errorBorder != null) {
                   errorBorder.updateStatus(null);
               }
           }
        }
        if (otherPropertyName.equals(evt.getPropertyName())) {
            otherValue = (String)evt.getNewValue();
            if (errorBorder != null) {
                //When error border is set, error border can be update from value changes of other property too,
                //instead of waiting for the own text field to be changed by the user.
                DifferentStringValidator.Result r = validate(lastValidatedString);
                errorBorder.updateStatus(r);
            }
        }
    }
}
