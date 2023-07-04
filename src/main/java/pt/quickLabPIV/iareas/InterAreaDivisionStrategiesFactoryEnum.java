package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public enum InterAreaDivisionStrategiesFactoryEnum {
	NoSuperPositionStrategy,
	SuperPositionStrategy,
	MixedSuperPositionStrategy;

	public static IInterAreaDivisionStrategy create(PIVInputParameters parameters) {
		switch (parameters.getAreaDivisionStrategy()) {
		case NoSuperPositionStrategy:
			return new AdaptiveInterAreaStrategyNoSuperPosition();
		case SuperPositionStrategy:
			return new AdaptiveInterAreaStrategySuperPosition();
		case MixedSuperPositionStrategy:
			return new AdaptiveInterAreaStrategyMixedSuperPosition();
		}
		
		return null;
	}
	
}
