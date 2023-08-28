// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import javax.swing.filechooser.FileSystemView;

public class LocalFileSystemViewFactory implements IFileSystemViewFactory {

    @Override
    public FileSystemView createView() {
        FileSystemView view = FileSystemView.getFileSystemView();
        return view;
    }

}
