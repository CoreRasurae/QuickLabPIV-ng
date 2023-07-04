package pt.quickLabPIV.iareas;

import org.apache.commons.math3.util.FastMath;

public class InterAreaDisplacementStableStrategy implements IInterAreaStableStrategy {
	private final InterAreaDisplacementStableConfiguration config;
	
	
	public InterAreaDisplacementStableStrategy(InterAreaDisplacementStableConfiguration config) {
		this.config = config;
	}
	
	@Override
	public TileStableStateEnum computeStableState(IterationStepTiles iterTiles, Tile tile, float displacementDeltaU,
			float displacementDeltaV) {
		if (iterTiles.getCurrentStepRetries() == 0) {
			return TileStableStateEnum.EVALUATING;
		} else {
			float norm = (float)FastMath.sqrt(FastMath.pow(displacementDeltaU, 2) + FastMath.pow(displacementDeltaV, 2));
			if (norm > config.getMaxDisplacement()) {
				if (iterTiles.getCurrentStepRetries() < config.getMaxRetries()) {
					return TileStableStateEnum.EVALUATING;
				} else {
					return TileStableStateEnum.UNSTABLE;
				}
			}
		}
		
		return TileStableStateEnum.STABLE;
	}

}
