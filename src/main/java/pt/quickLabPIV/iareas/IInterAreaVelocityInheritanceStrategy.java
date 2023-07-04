package pt.quickLabPIV.iareas;

public interface IInterAreaVelocityInheritanceStrategy {
	/**
	 * Resets the state for the iteration step tiles for instance re-use purposes where the base PIV Parameters remains unchanged. 
	 * @param currentStepTiles the iteration step tiles instance to be reset for reuse
	 */
	public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTiles);
}
