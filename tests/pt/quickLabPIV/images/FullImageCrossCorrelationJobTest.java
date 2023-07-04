package pt.quickLabPIV.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.CrossCorrelationTestHelper;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.IInterAreaDivisionStrategy;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationSoftRealFFTJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class FullImageCrossCorrelationJobTest {
    private final static ComputationDevice cpuDevice = DeviceManager.getSingleton().getCPU();
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    
    @Before
    public void setup() {
        PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image8Bit);
    }

    @Test
    public void testSyntheticImageCrossCorrelation64x64GPUPass() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        testSyntheticImageCrossCorrelation(64, 64, gpuDevice);
    }

    @Test
    public void testSyntheticImageCrossCorrelation32x32GPUPass() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        testSyntheticImageCrossCorrelation(32, 32, gpuDevice);
    }
    
    @Test
    public void testSyntheticImageCrossCorrelation16x16GPUPass() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        testSyntheticImageCrossCorrelation(16, 16, gpuDevice);
    }

    @Test
    public void testSyntheticImageCrossCorrelation8x8GPUPass() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        testSyntheticImageCrossCorrelation(8, 8, gpuDevice);
    }

    @Test
    public void testSyntheticImageCrossCorrelation4x4GPUPass() {
        assumeTrue("No OpenCL GPU Device available", gpuDevice != null);
        testSyntheticImageCrossCorrelation(4, 4, gpuDevice);
    }
    
    @Test
    public void testSyntheticImageCrossCorrelation64x64CPUPass() {
        assumeTrue("No OpenCL CPU Device available", cpuDevice != null);
        testSyntheticImageCrossCorrelation(64, 64, cpuDevice);
    }

    private void testSyntheticImageCrossCorrelation(int tileHeight, int tileWidth, ComputationDevice device) {
        IImage imgA = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage imgB = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Create tiles for images
        int tilesY = imgA.getHeight() / tileHeight;
        int tilesX = imgA.getWidth() / tileWidth;
        
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);        
        params.setImageHeightPixels(imgA.getHeight());
        params.setImageWidthPixels(imgB.getWidth());
        
        params.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.NoSuperPositionStrategy);
        params.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
        params.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
        params.setClippingMode(ClippingModeEnum.NoOutOfBoundClipping);
        
        params.setInterrogationAreaStartIPixels(tileHeight);
        params.setInterrogationAreaStartJPixels(tileWidth);
        params.setInterrogationAreaEndIPixels(tileHeight);
        params.setInterrogationAreaEndJPixels(tileWidth);
        
        params.setMarginPixelsITop((short)0);
        params.setMarginPixelsIBottom(0);
        params.setMarginPixelsJLeft(0);
        params.setMarginPixelsJRight(0);

        IInterAreaDivisionStrategy noSuperStrat = new AdaptiveInterAreaStrategyNoSuperPosition();
        IterationStepTiles stepTilesA = noSuperStrat.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);        
        IterationStepTiles stepTilesB = noSuperStrat.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
        List<Tile> tilesForCrossA = new ArrayList<Tile>(tilesY*tilesX);
        List<Tile> tilesForCrossB = new ArrayList<Tile>(tilesY*tilesX);
        List<Matrix> matricesA = new ArrayList<Matrix>(tilesY*tilesX);
        List<Matrix> matricesB = new ArrayList<Matrix>(tilesY*tilesX);
        for (int i = 0; i < tilesY; i++) {
            for (int j = 0; j < tilesX; j++) {
                Tile tileA = stepTilesA.getTile(i, j);
                Tile tileB = stepTilesB.getTile(i, j);
                tileA.setMatrix(imgA.clipImageMatrix(tileA.getTopPixel(), tileA.getLeftPixel(), tileHeight, tileWidth, false, tileA.getMatrix()));
                tileB.setMatrix(imgB.clipImageMatrix(tileB.getTopPixel(), tileB.getLeftPixel(), tileHeight, tileWidth, false, tileB.getMatrix()));
                tilesForCrossA.add(tileA);
                tilesForCrossB.add(tileB);
                matricesA.add(tileA.getMatrix());
                matricesB.add(tileB.getMatrix());
            }
        }
        
        CrossCorrelationJob testJob = new CrossCorrelationJob(false, device, null);
        testJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesForCrossA);
        testJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesForCrossB);
        testJob.analyze();
        testJob.compute();        
        XCorrelationResults results = testJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        System.out.println("Now computing validation values");
        List<Matrix> resultMatrices = results.getCrossMatrices();
        List<Matrix> validationMatrices = CrossCorrelationTestHelper.localCrossCorrelation(matricesA, matricesB);
        System.out.println("Validation values computed");
        for (int tileIndex = 0; tileIndex < tilesY*tilesX; tileIndex++) {
           //System.out.println("Checking matrix: " + tileIndex);
           Matrix result = resultMatrices.get(tileIndex);
           Matrix validation = validationMatrices.get(tileIndex);
           assertEquals("Matrices at index: " + tileIndex + " - Height do not match", result.getHeight(), validation.getHeight());
           assertEquals("Matrices at index: " + tileIndex + " - Width do not match", result.getWidth(), validation.getWidth());
           double maxValueValidation = 0.0f;
           double maxValueComputed = 0.0f;
           for (int i = 0; i < tileHeight; i++) {
               for (int j = 0; j < tileWidth; j++) {
                   if (validation.getElement(i,j) > maxValueValidation) {
                       maxValueValidation = validation.getElement(i,j); 
                   }
                   if (result.getElement(i, j) > maxValueComputed) {
                       maxValueComputed = result.getElement(i, j); 
                   }
               }
           }
           for (int i = 0; i < tileHeight; i++) {
               for (int j = 0; j < tileWidth; j++) {
                  if (maxValueValidation == 0) {
                      maxValueValidation = 1;
                  }
                  assertEquals("Matrices at index: " + tileIndex + ", row=" + i + ", column= " + j + " - Do not match correlation result", validation.getElement(i, j)/maxValueValidation*maxValueComputed, result.getElement(i, j), 1e-7f);   
               }
           }
        }
        
        System.out.println("Now comparing with FFTs");
        CrossCorrelationSoftRealFFTJob validationJob2 = new CrossCorrelationSoftRealFFTJob();
        validationJob2.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesForCrossA);
        validationJob2.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesForCrossB);
        validationJob2.analyze();
        validationJob2.compute();        
        XCorrelationResults validationResults2 = testJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> validationMatrices2 = validationResults2.getCrossMatrices();
        for (int tileIndex = 0; tileIndex < tilesY*tilesX; tileIndex++) {
            //System.out.println("Checking matrix: " + tileIndex);
            Matrix result = resultMatrices.get(tileIndex);
            Matrix validation = validationMatrices2.get(tileIndex);
            assertEquals("Matrices at index: " + tileIndex + " - Height do not match", result.getHeight(), validation.getHeight());
            assertEquals("Matrices at index: " + tileIndex + " - Width do not match", result.getWidth(), validation.getWidth());
            for (int i = 0; i < tileHeight; i++) {
                for (int j = 0; j < tileWidth; j++) {
                   assertEquals("Matrices at index: " + tileIndex + ", row=" + i + ", column= " + j + " - Do not match correlation result", validation.getElement(i, j), result.getElement(i, j), 1e-7f);   
                }
            }
         }        
    }
}
