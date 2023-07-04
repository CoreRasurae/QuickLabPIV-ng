package pt.quickLabPIV.ui.validators;

import java.io.File;

import org.jdesktop.beansbinding.Validator;

public class FileValidator extends Validator<File> {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Validator<File>.Result validate(File myFile) {
        Validator<File>.Result result = null;
        if (!myFile.isFile()) {
            result = new Validator.Result(null, "Selected element is not a valid file");
        }
        
        return result;
    }

}
