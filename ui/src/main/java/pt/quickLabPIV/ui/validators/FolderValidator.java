package pt.quickLabPIV.ui.validators;

import java.io.File;

import org.jdesktop.beansbinding.Validator;

public class FolderValidator extends Validator<File> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Validator<File>.Result validate(File myFile) {
        Validator<File>.Result result = null;
        try {
            if (!myFile.isDirectory() || !myFile.exists()) {
                result = new Validator.Result(null, "Selected element is not a valid folder");
            }
        } catch (SecurityException e) {
            result = new Validator.Result(null, "No permissions to read folder");
        }
        return result;
    }

}
