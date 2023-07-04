package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.images.IImage;

public interface ILiuShenOpticalFlowHelper {
    
    public void getVelocitiesMatrix(float centerLocI, float centerLocJ, float finalLocI, float finalLocJ, float us[], float vs[]);
    
    public void receiveImageA(IImage img);
    
    public void receiveImageB(IImage img);
}
