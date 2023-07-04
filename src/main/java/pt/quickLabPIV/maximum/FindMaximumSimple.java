package pt.quickLabPIV.maximum;

import java.util.Iterator;
import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class FindMaximumSimple implements IMaximumFinder {

	@Override
	public MaxCrossResult findMaximum(Matrix m) {
		MaxCrossResult result = new MaxCrossResult();
		
		result.setMainPeakValue(0.0f);
		result.setCrossMatrix(m);
		
		float minValue = Float.MAX_VALUE;
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				float value = m.getElement(i, j);
				if (value > result.getMainPeakValue()) {
					result.setMainPeakValue(value);
					result.setMainPeakI(i);
					result.setMainPeakJ(j);
				}
				if (value < minValue) {
				    minValue = value;
				}
			}
		}
		
		if (result.getMainPeakValue() == 0.0f) {
			result.setMainPeakI(m.getHeight() / 2);
			result.setMainPeakJ(m.getWidth() / 2);
		}
		
		result.setMinFloor(minValue);
		
		return result;
	}

	@Override
	public void dispose() {
		//Intentionally empty
	}

	@Override
	public List<MaxCrossResult> findAllPeaks(List<XCorrelationResults> xCorrResults, Iterator<Tile> tileAIterator,
			Iterator<Tile> tileBIterator) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
