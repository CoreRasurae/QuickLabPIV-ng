// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

import pt.quickLabPIV.images.IImage;

public class LiuShenOptions {
    public int windowSizeLK;
    public int iterationsLK;
    public IImage imageLSA;
    public IImage imageLSB;
    public float lambdaLS;
    public int iterationsLS;
}
