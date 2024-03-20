// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.converters;

import java.io.File;

import org.jdesktop.beansbinding.Converter;

public class SimpleFileOnlyConverter extends Converter<File, String> {
    
    @Override
    public File convertReverse(String name) {
        File f = new File(name);
        return f;
    }

    @Override
    public String convertForward(File file) {
        return file.getName();
    }

}
