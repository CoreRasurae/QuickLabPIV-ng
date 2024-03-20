// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.images.IImage;

public interface ILiuShenOpticalFlowHelper {
    
    /**
     * Obtains the Lucas-Kanade velocities matrix
     * @param centerLocI the IA vertical center location
     * @param centerLocJ the IA horizontal center location
     * @param finalLocI the IA vertical center final position
     * @param finalLocJ the IA horizontal center final position
     * @param us the horizontal velocities (Lucas-Kanade OpF orientation)
     * @param vs the vertical velocities (Lucas-Kanade OpF orientation)
     */
    public void getVelocitiesMatrix(float centerLocI, float centerLocJ, float finalLocI, float finalLocJ, float us[], float vs[]);
    
    public void receiveImageA(IImage img);
    
    public void receiveImageB(IImage img);
}
