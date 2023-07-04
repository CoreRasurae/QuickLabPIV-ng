package pt.quickLabPIV.maximum;

//TODO Why does Maven fails during test compilation when this is enabled:
//import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aparapi.Kernel.EXECUTION_MODE;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.exception.QueryFailedException;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.JobComputeException;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class FindMaximumSimpleOpenCL implements IMaximumFinder  {
	private FindMaximumSimpleKernel kernel;
	private Range range = null; 
	
	private int[] max = new int[4]; 
	
	public FindMaximumSimpleOpenCL() {
	}
	
	@Override
	public MaxCrossResult findMaximum(Matrix m) {
	    //assumeTrue("No OpenCL GPU device available", Device.firstGPU() != null);
	    
		final float[] matrix = m.getFloatArray();
		if (range == null) {		
			int dimI = m.getHeight();
			int dimJ = m.getWidth();
			int localDimI = dimI+1;
			int localDimJ = dimJ+1;
			
			kernel = new FindMaximumSimpleKernel(max, dimI, dimJ);
	        int maxLocalSize = 1024;
            try {
                maxLocalSize = kernel.getKernelMaxWorkGroupSize(Device.firstGPU());
            } catch (QueryFailedException e) {
                throw new JobComputeException(e);
            }
            if ((dimI+1) * (dimJ+1) > maxLocalSize) {
                localDimJ = maxLocalSize/(dimI+1);
                if (localDimJ == 0) {
                    int excessFactor = (dimJ+1)/maxLocalSize;
                    localDimI = (dimI+1)/excessFactor;;
                    localDimJ = maxLocalSize/localDimI;
                }
            }
            

			//range = Range.create2D(localDimJ, localDimI, localDimJ, localDimI);
			range = Range.create2D(Device.firstGPU(), localDimJ, localDimI, localDimJ, localDimI);
			kernel.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
			kernel.setExplicit(true);
		}
		kernel.setNumberOfTiles(1);
		kernel.setMatrix(matrix);
		kernel.put(matrix);
		kernel.execute(range);
		if (max[3] != 0) {
			throw new JobComputeException("Integer was overloaded");
		}
		kernel.get(max);
		MaxCrossResult result = new MaxCrossResult();
		result.setCrossDims(m.getHeight(), m.getWidth());
		result.setMainPeakI(max[0]);
		result.setMainPeakJ(max[1]);
		result.setMainPeakValue(max[2]);
		return result;
	}
	
	@Override
	public void dispose() {
		if (kernel != null) {
			kernel.dispose();
			kernel = null;
		}
		
		if (range != null) {
			range = null;
		}
	}

	@Override
	public List<MaxCrossResult> findAllPeaks(List<XCorrelationResults> xCorrResults, Iterator<Tile> tileAIterator,
			Iterator<Tile> tileBIterator) {
		int totalNumberOfMatrices = 0;
		for (XCorrelationResults xCorrResult : xCorrResults) {
			totalNumberOfMatrices += xCorrResult.getNumberOfMatrices();
		}
		
		List<MaxCrossResult> results = new ArrayList<MaxCrossResult>(totalNumberOfMatrices);
		for (XCorrelationResults xCorrResult : xCorrResults) {
			int maxLocalSize = 1024;
			int localDimI = xCorrResult.getDimI();
			int localDimJ = xCorrResult.getDimJ();
			if (localDimI * localDimJ > maxLocalSize) {
				localDimJ = maxLocalSize/localDimI;
				if (localDimJ == 0) {
					int excessFactor = (xCorrResult.getDimJ())/maxLocalSize;
					localDimI = (xCorrResult.getDimI())/excessFactor;;
					localDimJ = maxLocalSize/localDimI;
				}
			}
			
			int tilesInJ = totalNumberOfMatrices / localDimJ;
		    int tilesInI = totalNumberOfMatrices / tilesInJ;
		    if (tilesInI * tilesInJ < totalNumberOfMatrices) {
		    	tilesInI++;
		    }
			
			/*int localDimI = xCorrResult.getDimI()/8;
			int localDimJ = 1;*/
		    
			max = new int[4 * xCorrResult.getNumberOfMatrices()];
			kernel = new FindMaximumSimpleKernel(max, xCorrResult.getDimI()-1, xCorrResult.getDimJ()-1);
			//range = Range.create2D(localDimJ, localDimI, localDimJ, localDimI);
			range = Range.create2D(Device.firstGPU(), localDimJ*tilesInJ, localDimI*tilesInI, localDimJ, localDimI);
			kernel.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
			kernel.setMatrix(xCorrResult.getArray());
			kernel.setNumberOfTiles(xCorrResult.getNumberOfMatrices());
			//kernel.execute(range, xCorrResult.getNumberOfMatrices());
			kernel.execute(range);
			
			for (int i = 0; i < xCorrResult.getNumberOfMatrices(); i++) {
				MaxCrossResult result = new MaxCrossResult();
				result.setMainPeakI(max[i * 4 + 0]);
				result.setMainPeakJ(max[i * 4 + 1]);
				result.setMainPeakValue(max[i * 4 +2]);
				result.tileA = tileAIterator.next();
				result.tileB = tileBIterator.next();
				results.add(result);
			}
		}
		
		return results;
	}

}
