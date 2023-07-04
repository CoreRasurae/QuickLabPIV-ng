package pt.quickLabPIV.iareas;

public interface IInterAreaStableStrategy {
	public TileStableStateEnum computeStableState(IterationStepTiles iterTiles, Tile tile, float displacementDeltaU, float displacementDeltaV);
}
