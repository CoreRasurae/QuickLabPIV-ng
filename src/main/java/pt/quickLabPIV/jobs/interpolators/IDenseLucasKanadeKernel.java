// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.interpolators;

public interface IDenseLucasKanadeKernel {
    public void setKernelArgs(final float _imageA[], final float[] _imageB, final float[] _us, final float[] _vs, boolean halfPixelOffset);
}
