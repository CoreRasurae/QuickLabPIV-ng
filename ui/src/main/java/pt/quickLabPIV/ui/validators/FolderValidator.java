// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
