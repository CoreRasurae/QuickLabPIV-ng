// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.business.facade;

import java.io.File;

public interface IFileFactory {
    public File createFile(String path);
    
    public File createFile(File parent, String path);
}
