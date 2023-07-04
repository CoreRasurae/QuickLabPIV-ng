package pt.quickLabPIV.iareas.validation;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;

public class DifferenceValidator implements IVectorValidator {
    private final float threshold;
    
    public DifferenceValidator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(DifferenceValidatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new VectorValidatorException("Couldn't retrieve Difference validator configuration");
        }
        DifferenceValidatorConfiguration configuration = (DifferenceValidatorConfiguration)configurationObject;
        threshold = configuration.getDistanceThreshold() * configuration.getDistanceThreshold();
    }

    public DifferenceValidator(DifferenceValidatorConfiguration configuration) {
        threshold = configuration.getDistanceThreshold() * configuration.getDistanceThreshold();
    }

    @Override
    public void validateVector(Tile tile, Tile[][] adjacents, IterationStepTiles stepTiles) {
        if (tile.isMaskedDisplacement()) {
            return;
        }

        for (int indexI = 0; indexI < 2; indexI++) {
            for (int indexJ = 0; indexJ < 2; indexJ++) {
                if (indexI == 1 && indexJ == 1) {
                    continue;
                }
                
                Tile adjacent = adjacents[indexI][indexJ];
                if (adjacent == null || adjacent.isMaskedDisplacement() || adjacent.isInvalidDisplacement()) {
                    continue;
                }
                
                float uSelf = tile.getDisplacementU();
                float vSelf = tile.getDisplacementV();
                
                float uNeighbor = adjacent.getDisplacementU();
                float vNeighbor = adjacent.getDisplacementV();
                
                float du = uNeighbor - uSelf;
                float dv = vNeighbor - vSelf;
                
                float distance = du*du + dv*dv;

                if (distance < threshold) {
                    tile.setInvalidDisplacement(false);
                } else {
                    tile.setInvalidDisplacement(true);
                }
            }
        }
    }

}
