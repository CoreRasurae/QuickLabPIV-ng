// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import javax.swing.filechooser.FileSystemView;

import pt.quickLabPIV.exceptions.UIException;

public class RemoteFileSystemViewFactory implements IFileSystemViewFactory {

    @Override
    public FileSystemView createView() {
        throw new UIException("Remote projects are unsupported yet", "Cannot create remote file system view");
    }

}
