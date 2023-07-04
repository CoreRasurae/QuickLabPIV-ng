package pt.quickLabPIV.iareas;

public class InterAreaSimpleStableStrategy implements IInterAreaStableStrategy {

	@Override
	public TileStableStateEnum computeStableState(IterationStepTiles iterTiles, Tile tile, float displacementDeltaU,
			float displacementDeltaV) {
		if (iterTiles.getCurrentStepRetries() == 0) {
			return TileStableStateEnum.EVALUATING;
		} else {
			return TileStableStateEnum.STABLE;
		}
	}

}
