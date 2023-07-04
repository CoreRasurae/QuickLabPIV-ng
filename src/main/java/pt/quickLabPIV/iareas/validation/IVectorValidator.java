package pt.quickLabPIV.iareas.validation;

import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;

public interface IVectorValidator {
    public void validateVector(Tile tile, Tile[][] adjacents, IterationStepTiles stepTiles);
}
