package pt.quickLabPIV.iareas;

public interface IAdaptiveInterVelocityInheritanceLogger {
	public boolean isToBeLogged(IterationStepTiles currentStepTiles, Tile currenTile, Tile parentTile);
	
	public void logTileContribution(IterationStepTiles currentStepTiles, Tile currenTile, Tile parentTile, 
			float weightU, float weightV);

	public void logCompletedForTile(IterationStepTiles currentStepTiles, Tile currentTile);
}
