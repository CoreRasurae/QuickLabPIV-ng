package pt.quickLabPIV.interpolators;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.maximum.MaxCrossResult;

public interface IBasicCrossCorrelationInterpolator {
	public MaxCrossResult interpolate(final Matrix m, MaxCrossResult result);
}
