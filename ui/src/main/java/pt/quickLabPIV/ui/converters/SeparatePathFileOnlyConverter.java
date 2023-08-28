// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.converters;

import java.io.File;

import pt.quickLabPIV.business.facade.IFileFactory;

public class SeparatePathFileOnlyConverter extends ConverterWithForwardValidator<File, String> {
    private File basePath;
    private IFileFactory factory;
        
    public void setFileFactory(IFileFactory _factory) {
        factory = _factory;
    }
        
    @Override
    public File convertReverse(String name) {
        File f = factory.createFile(basePath, name);
        return f;
    }

    @Override
    public String convertForwardAfterValidation(File file) {
        basePath = file.getParentFile();
        return file.getName();
    }
}
