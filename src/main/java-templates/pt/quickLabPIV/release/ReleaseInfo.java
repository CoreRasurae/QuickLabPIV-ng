// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.release;

public class ReleaseInfo {
    public final static String BUILD_VERSION   = "${project.version}";
    public final static String BUILD_NUMBER = "${buildNumber.value}";
    public final static String BUILD_TIMESTAMP = "${buildNumber.timestamp}";
}
