// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.interpolators;

public interface IDenseLucasKanadeKernel {
    public void setKernelArgs(final float _imageA[], final float[] _imageB, final float[] _us, final float[] _vs, boolean halfPixelOffset);
}
