package pt.quickLabPIV.iareas;

public interface ICrossCorrelationDumpMatcher {
    public boolean matches(IterationStepTiles stepTiles);
    
    public boolean matches(Tile tile);
}
