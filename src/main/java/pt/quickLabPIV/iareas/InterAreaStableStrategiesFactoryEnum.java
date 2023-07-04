package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public enum InterAreaStableStrategiesFactoryEnum {
	MaxDisplacementStrategy,
	SimpleStrategy;
	
	static public IInterAreaStableStrategy create(PIVInputParameters parameters) {
		switch (parameters.getAreaStableStrategy()) {
		case SimpleStrategy:
			return new InterAreaSimpleStableStrategy();
		case MaxDisplacementStrategy:
			InterAreaDisplacementStableConfiguration config = 
				(InterAreaDisplacementStableConfiguration)parameters.getSpecificConfiguration(InterAreaDisplacementStableConfiguration.IDENTIFIER);
			return new InterAreaDisplacementStableStrategy(config);
		}
		
		return null;
	}
}
