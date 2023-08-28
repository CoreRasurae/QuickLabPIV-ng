// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import java.io.File;

public class LocalFileFactory implements IFileFactory {

    @Override
    public File createFile(String path) {
        return new File(path);
    }

    @Override
    public File createFile(File parent, String path) {
        return new File(parent, path);
    }

}
