package pt.quickLabPIV.iareas.replacement;

import java.util.List;

import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IVectorReplacement {

    public void replaceVector(boolean firstPass, int frameNumber, Tile vector, Tile[][] adjacents, List<MaxCrossResult> maxCrosses);
}
