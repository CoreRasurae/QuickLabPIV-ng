package pt.quickLabPIV.iareas;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.maximum.FindMaximumSimple;
import pt.quickLabPIV.maximum.IMaximumFinder;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class TileCrossCorrelationTest {
	private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
	
	@Test
	public void testSimpleTileCrossCorrelationFromTestImage1Pass() {
		String filenameA = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_A.tif";
		String filenameB = "testFiles" + File.separator + "Rota_Iso_D4_C4_N0_OUT0_LT0_0010_B.tif";
		
		int top = 16;
		int left = 27;
		int width = 32;
		int height = 32;
	
		Tile tileA = new Tile(null);
		Tile tileB = new Tile(null);
		
		tileA.setTopPixel((short)top);
		tileA.setLeftPixel((short)left);
		tileA.resetDisplacements();

		tileB.setTopPixel((short)top);
		tileB.setLeftPixel((short)left);
		tileB.resetDisplacements();

		
		IImage imgA = ImageTestHelper.getImage(filenameA);
		IImage imgB = ImageTestHelper.getImage(filenameB);

		tileA.setMatrix(imgA.clipImageMatrix(0, 0, height, width, false, null));
		tileB.setMatrix(imgB.clipImageMatrix(0, 0, height, width, false, null));
		
		List<Tile> inputTilesF = new ArrayList<Tile>(1);
		List<Tile> inputTilesG = new ArrayList<Tile>(1);
		inputTilesF.add(tileA);
		inputTilesG.add(tileB);
		
		CrossCorrelationJob job = new CrossCorrelationJob(false, gpuDevice, null, false);
		job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, inputTilesF);
		job.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, inputTilesG);
		try {
	        job.analyze();
	        job.compute();
		} finally {
			job.dispose();
		}
        
        List<Matrix> inputMatricesF = new ArrayList<Matrix>(1);
        List<Matrix> inputMatricesG = new ArrayList<Matrix>(1);
        inputMatricesF.add(tileA.getMatrix());
        inputMatricesG.add(tileB.getMatrix());
        
        List<Matrix> outputMatricesLocal = CrossCorrelationTestHelper.localCrossCorrelation(inputMatricesF, inputMatricesG);
        Matrix resultLocal = outputMatricesLocal.get(0);
        
        XCorrelationResults results = job.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> outputMatrices = results.getCrossMatrices();
        Matrix result = outputMatrices.get(0);
                
        assertEquals("Cross correlation doesn't match expected dimI dimensions", resultLocal.getHeight(), result.getHeight());
        assertEquals("Cross correlation doesn't match expected dimJ dimensions", resultLocal.getWidth(), result.getWidth());
        
        for (int i = 0; i < resultLocal.getHeight(); i++) {
        	for (int j = 0; j < resultLocal.getWidth(); j++) {
        		assertEquals("Cross-correlation matrix at I=" + i + ", J=" + j + " is wrong", resultLocal.getElement(i, j), result.getElement(i, j), 1e-10f);
        	}
        }
        
        IMaximumFinder finder = new FindMaximumSimple();
        MaxCrossResult maxResult = finder.findMaximum(result);
        System.out.println(maxResult);
	}
	
	@Test
	public void testIterationStepTilesCrossCorrelation() {
		
	}
}
