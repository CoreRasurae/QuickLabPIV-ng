// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.images.IImage;

public interface ILiuShenOpticalFlowHelper {
    
    public void getVelocitiesMatrix(float centerLocI, float centerLocJ, float finalLocI, float finalLocJ, float us[], float vs[]);
    
    public void receiveImageA(IImage img);
    
    public void receiveImageB(IImage img);
}
