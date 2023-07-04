package pt.quickLabPIV.jobs.xcorr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class CrossCorrelationSoftRealFFTJob extends Job<List<Tile>, XCorrelationResults>  {
    List<Matrix> inputMatricesF = null;
    List<Matrix> inputMatricesG = null;
    List<Tile> inputTilesF = null;
    List<Tile> inputTilesG = null;
    int inputGeometry[];
    int outputGeometry[];
    int numberOfUsedTiles;

    private void analyzeTilesHelper(List<Tile> tilesF, List<Tile> tilesG) {
        if (tilesF.size() < 1 || tilesG.size() < 1) {
            return;
        }
        
        Matrix refMatrix = tilesF.get(0).getMatrix();
        int dimI = refMatrix.getHeight();
        int dimJ = refMatrix.getWidth();
                
        for (Tile tile : tilesF) {
            if (dimI != tile.getMatrix().getHeight()) {
                throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along I)");
            }
            
            if (dimJ != tile.getMatrix().getWidth()) {
                throw new RuntimeException("Matrices in matricesF don't have the same dimensions (along J)");
            }
        }
        
        for (Tile tile : tilesG) {
            if (dimI != tile.getMatrix().getHeight()) {
                throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along I)");
            }
            
            if (dimJ != tile.getMatrix().getWidth()) {
                throw new RuntimeException("Matrices in matricesG don't have the same dimensions (along J)");
            }
        }

        if (dimI != dimJ) {
            throw new RuntimeException("This FFT standard job can only handle square matrices.");
        }

        
        if (tilesF.size() != tilesG.size()) {
            throw new RuntimeException("The number of matrices in F must be matched with the number of matrices G");
        }
    
        inputGeometry = new int[2];
        inputGeometry[0] = dimI;
        inputGeometry[1] = dimJ;
        
        outputGeometry = new int[2];
        outputGeometry[0] = dimI * 2;
        outputGeometry[1] = dimJ * 2;
        
        inputMatricesF = null;
        inputMatricesG = null;      
    }

    
    @Override
    public void analyze() {
        inputTilesF = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A);
        inputTilesG = getInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B);
        
        if (inputMatricesF == null || inputMatricesG == null || 
                (inputTilesF != null && inputTilesG != null)) {
            analyzeTilesHelper(inputTilesF, inputTilesG);
            numberOfUsedTiles = inputTilesF.size();
        } else {
            numberOfUsedTiles = inputMatricesF.size();
        }
    }

    @Override
    public void compute() {
        List<Matrix> workMatricesF = null;
        List<Matrix> workMatricesG = null;

        if (inputMatricesF != null || inputMatricesG != null) {
            workMatricesF = inputMatricesF;
            workMatricesG = inputMatricesG;
        } else {
            workMatricesF = new ArrayList<Matrix>(numberOfUsedTiles);
            workMatricesG = new ArrayList<Matrix>(numberOfUsedTiles);
            for (Tile inputTile : inputTilesF) {
                workMatricesF.add(inputTile.getMatrix());
            }
            for (Tile inputTile : inputTilesG) {
                workMatricesG.add(inputTile.getMatrix());
            }
        }
        
        List<Matrix> outputMatrices = new ArrayList<Matrix>(numberOfUsedTiles);
        for (int index = 0; index < numberOfUsedTiles; index++) {
            Matrix result = FastRealFFTXCorr.computeXCorr(workMatricesF.get(index), workMatricesG.get(index));
            outputMatrices.add(result);
        }
        
        List<MaxCrossResult> crossResults = Collections.emptyList();
        XCorrelationResults results = new XCorrelationResults(outputMatrices, crossResults, null, outputGeometry[0], outputGeometry[1], numberOfUsedTiles);
        setJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES, results);
    }

    @Override
    public void dispose() {
        //Not required
    }

}
