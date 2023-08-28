// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImageOnlyFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        String filename = f.getName().toLowerCase();
        if (f.isFile() && (filename.endsWith(".png") ||
                filename.endsWith(".tif") || filename.endsWith(".tiff") ||
                filename.endsWith(".bmp") || 
                filename.endsWith(".jpg") || filename.endsWith(".jpeg"))) {
            return true;
        } else if (f.isDirectory()) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Image files (*.png, *.tif, *.bmp, *.jpg)";
    }
};

