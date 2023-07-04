package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.iareas.IterationStepTiles;

public interface IOpticalFlowInterpolator extends ICrossCorrelationInterpolator {
    @Override 
    public default boolean isImagesRequired() {
        return true;
    }

    void interpolate(IterationStepTiles stepTilesA, IterationStepTiles stepTilesB);
}
