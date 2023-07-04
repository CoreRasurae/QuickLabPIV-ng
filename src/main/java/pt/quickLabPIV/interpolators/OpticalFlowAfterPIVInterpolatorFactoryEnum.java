package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;

public enum OpticalFlowAfterPIVInterpolatorFactoryEnum {
    None,
    LucasKanade,
    LucasKanadeAparapi,
    LiuShen,
    LiuShenAparapi;
    
    public static IOpticalFlowInterpolator createInterpolator(PIVInputParameters parameters) {
        OpticalFlowAfterPIVInterpolatorFactoryEnum option = parameters.getOpticalFlowAfterPIVStrategy();
        return createInterpolator(option, parameters);
    }

    public static IOpticalFlowInterpolator createInterpolator(OpticalFlowAfterPIVInterpolatorFactoryEnum option, PIVInputParameters parameters) {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();

        boolean useOpenCL = runParameters.isUseOpenCL();
        
        switch (option) {
        case None:
            return null;
        case LucasKanade:
            return new LucasKanadeFloat();
        case LucasKanadeAparapi:
            if (!useOpenCL) {
                throw new InterpolatorStateException("Cannot use Lucas-Kanade Aparapi implemenation as OpenCL acceleration is not allowed");
            }
            return new DenseLucasKanadeAparapiJobInterpolator(); 
        case LiuShen:
            return new LiuShenFloat();
        case LiuShenAparapi:
            if (!useOpenCL) {
                throw new InterpolatorStateException("Cannot use Lucas-Kanade combined with Liu-Shen Aparapi implemenation as OpenCL acceleration is not allowed");
            }
            return new DenseLiuShenAparapiJobInterpolator();

        default:
            throw new InterpolatorStateException("Unknown or unsupported Optical flow after PIV strategy: " + option);
        }
    }

}
