package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVInputParameters;

public enum InterAreaVelocityStrategiesFactoryEnum {
	Direct,
	Area,
	Distance,
	BiCubicSpline;
	
	public static IInterAreaVelocityInheritanceStrategy create(PIVInputParameters parameters) {
		switch (parameters.getVelocityInheritanceStrategy()) {
		case Direct:
			return new AdaptiveInterVelocityInheritanceStrategyDirect();
		case Area:
			return new AdaptiveInterVelocityInheritanceStrategyArea();
		case Distance:
			return new AdaptiveInterVelocityInheritanceStrategyDistanceGeneric();
		case BiCubicSpline:
		    return new AdaptiveInterVelocityInheritanceStrategyBiCubicSpline();
		}
		
		return null;
	}

}
