package pt.quickLabPIV;

import pt.quickLabPIV.images.BiLinearImageMicroAndMiniWarpingStrategy;
import pt.quickLabPIV.images.BiLinearImageWarpingModeEnum;
import pt.quickLabPIV.images.BiLinearImageWarpingStrategy;
import pt.quickLabPIV.images.IImageWarpingStrategy;
import pt.quickLabPIV.images.InvalidWarpingModeException;
import pt.quickLabPIV.images.NoImageWarpingStrategy;

public enum WarpingModeFactoryEnum {
	NoImageWarping(true, false, false),
	FirstImageBiLinearMiniWarping(false, true, false),
	SecondImageBiLinearMiniWarping(false, false, true),
	BothImagesBiLinearMiniWarping(false, true, true),
    FirstImageBiLinearMicroWarping(false, true, false),
    SecondImageBiLinearMicroWarping(false, false, true),
    BothImagesBiLinearMicroWarping(false, true, true),
	FirstImageBiLinearWarping(false, true, false),
	SecondImageBiLinearWarping(false, false, true),
	BothImagesBiLinearWarping(false, true, true);
    
    private boolean requiresRounding;
    private boolean warpsFirstImage;
    private boolean warpsSecondImage;
    
    private WarpingModeFactoryEnum(boolean _requiresRounding, boolean _warpsFirstImage, boolean _warpsSecondImage) {
        requiresRounding = _requiresRounding;
        warpsFirstImage = _warpsFirstImage;
        warpsSecondImage = _warpsSecondImage;
    }
    
    public static IImageWarpingStrategy create(PIVInputParameters parameters) {
        WarpingModeFactoryEnum mode = parameters.getWarpingMode();
        
        IImageWarpingStrategy strategy = null;
        switch (mode) {
            case NoImageWarping:
                strategy = new NoImageWarpingStrategy();
                break;
            case FirstImageBiLinearMiniWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.FirstImage, true);
                break;                
            case SecondImageBiLinearMiniWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.SecondImage, true);
                break;                
            case BothImagesBiLinearMiniWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.BothImages, true);
                break;                
            case FirstImageBiLinearMicroWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.FirstImage, false);
                break;                
            case SecondImageBiLinearMicroWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.SecondImage, false);
                break;                
            case BothImagesBiLinearMicroWarping:
                strategy = new BiLinearImageMicroAndMiniWarpingStrategy(BiLinearImageWarpingModeEnum.BothImages, false);
                break;                
            case FirstImageBiLinearWarping:
                strategy = new BiLinearImageWarpingStrategy(BiLinearImageWarpingModeEnum.FirstImage);
                break;
            case SecondImageBiLinearWarping:
                strategy = new BiLinearImageWarpingStrategy(BiLinearImageWarpingModeEnum.SecondImage);
                break;
            case BothImagesBiLinearWarping:
                strategy = new BiLinearImageWarpingStrategy(BiLinearImageWarpingModeEnum.BothImages);
                break;
            default:
                throw new InvalidWarpingModeException("Unkown or unsupported warping mode");
        }
        
        return strategy;
    }

    public boolean isRequiresRounding() {
        return requiresRounding;
    }
    
    public boolean isWarpsFirstImage() {
        return warpsFirstImage;
    }
    
    public boolean isWarpsSecondImage() {
        return warpsSecondImage;
    }
}
