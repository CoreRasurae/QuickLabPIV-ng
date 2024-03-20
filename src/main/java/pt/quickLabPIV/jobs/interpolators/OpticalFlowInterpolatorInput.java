// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

import pt.quickLabPIV.images.IImage;

public class OpticalFlowInterpolatorInput {
    public IImage imageA;
    public IImage imageB;
    public float[] us;
    public float[] vs;
    public boolean halfPixelOffset;
    public Object options;
}
