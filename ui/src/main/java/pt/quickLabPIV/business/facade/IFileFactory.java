// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import java.io.File;

public interface IFileFactory {
    public File createFile(String path);
    
    public File createFile(File parent, String path);
}
