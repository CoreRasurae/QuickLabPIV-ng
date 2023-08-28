// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import javax.swing.filechooser.FileSystemView;

public interface IFileSystemViewFactory {
    public FileSystemView createView();
}
