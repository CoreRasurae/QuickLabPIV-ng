// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
