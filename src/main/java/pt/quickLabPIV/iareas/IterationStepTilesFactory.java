package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public class IterationStepTilesFactory {
	public static IterationStepTiles create(TilesOrderEnum order, PIVInputParameters parameters) {
		IInterAreaDivisionStrategy areaDivisionStrategy = InterAreaDivisionStrategiesFactoryEnum.create(parameters);

		IterationStepTiles stepTiles = areaDivisionStrategy.createIterationStepTilesParameters(order, null);
		
		return stepTiles;
	}
}
