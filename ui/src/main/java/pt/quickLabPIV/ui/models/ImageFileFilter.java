// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        String filename = f.getName().toLowerCase();
        if (f.isDirectory() || filename.endsWith(".png") ||
                filename.endsWith(".tif") || filename.endsWith(".tiff") ||
                filename.endsWith(".bmp") || 
                filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Image files (*.png, *.tif, *.bmp, *.jpg)";
    }
};

