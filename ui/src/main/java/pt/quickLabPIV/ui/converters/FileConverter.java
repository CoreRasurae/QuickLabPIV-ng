// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import java.io.File;

public class FileConverter extends ConverterWithForwardValidator<File, String> {

    @Override
    public File convertReverse(String path) {
        File f = new File(path);
        return f;
    }

    @Override
    public String convertForwardAfterValidation(File file) {
        return file.getAbsolutePath();
    }

}
