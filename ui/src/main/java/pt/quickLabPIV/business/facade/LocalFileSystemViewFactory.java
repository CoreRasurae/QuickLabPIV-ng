// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
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
