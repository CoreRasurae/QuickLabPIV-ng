// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import java.io.File;

import pt.quickLabPIV.exceptions.UIException;

public class RemoteFileFactory implements IFileFactory {

    @Override
    public File createFile(String path) {
        throw new UIException("Remote files are not supported yet");
    }

    @Override
    public File createFile(File parent, String path) {
        throw new UIException("Remote files are not supported yet");
    }

}
