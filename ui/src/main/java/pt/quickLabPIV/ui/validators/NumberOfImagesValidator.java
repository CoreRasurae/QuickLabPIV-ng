package pt.quickLabPIV.ui.validators;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.beansbinding.Validator;

public class NumberOfImagesValidator extends Validator<Integer> implements PropertyChangeListener {
    private int totalNumberOfImagesAccepted;
    
    public void setTotalNumberOfImagesAccepted(int number) {
        totalNumberOfImagesAccepted = number;
    }
    
    @Override
    public Validator<Integer>.Result validate(Integer value) {
        Validator<Integer>.Result result = null;
        if (value == 0) {
            result = new NumberOfImagesValidator.Result(null, "Number of images to process must be greater than 0");
        } else if (value > totalNumberOfImagesAccepted) {
            result = new NumberOfImagesValidator.Result(null, "Number of images to process must be less or equal to available images");
        }
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == "availableImageFiles") {
            totalNumberOfImagesAccepted = (Integer)evt.getNewValue();
        }
    }
}
