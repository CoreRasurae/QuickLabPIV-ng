// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.validators;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.jdesktop.beansbinding.Validator;

public class SeparateFileValidator extends Validator<File> implements PropertyChangeListener {
    private File basePath;
    private final String boundPropertyName;
   
    public SeparateFileValidator(String propertyName) {
        boundPropertyName = propertyName;
    }

    public SeparateFileValidator(String propertyName, File startBasePath) {
        boundPropertyName = propertyName;
        basePath = startBasePath;
    }
    
    public void setBasePath(File _basePath) {
        basePath = _basePath;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Validator<File>.Result validate(File myFile) {
        //FIXME new File should not be used... A factory is needed.. either File of FileProxy instances are created based on project type...
        File completePathFile = new File(basePath, myFile.getName());
        Validator<File>.Result result = null;
        if (!completePathFile.isFile()) {
            result = new Validator.Result(null, "Selected element is not a valid file");
        }
        
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(boundPropertyName)) {
            basePath = (File)evt.getNewValue();
        }
    }

}
