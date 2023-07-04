package pt.quickLabPIV.jobs;

import java.util.Collections;
import java.util.List;

import pt.quickLabPIV.ImageFilteringModeFactoryEnum;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.images.filters.ImageFilterFactoryEnum;

public class JavaTilesFilterJob extends Job<List<Tile>, List<Tile>> {
    final IFilter filter;
    final ImageFilteringModeFactoryEnum imageFilteringMode;
    private List<Tile> inputTiles = Collections.emptyList();

    public JavaTilesFilterJob() {
        //When implementing GPU accelerated filtered job instances they will also be created from the same PIV input parameters
        PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
        imageFilteringMode = parameters.getImageFilteringMode();
        filter = ImageFilterFactoryEnum.create(parameters);
    }
    
    @Override
    public void analyze() {
        inputTiles = getInputParameters(JobResultEnum.JOB_RESULT_TILES);
        if (inputTiles.size() == 0) {
            throw new JobAnalyzeException("No tiles to filter");
        }
        if (filter == null) {
            throw new JobAnalyzeException("No filter available for image filtering");
        }
        if (imageFilteringMode == ImageFilteringModeFactoryEnum.ImageFilteringBeforeWarping) {
            throw new JobAnalyzeException("Image filter mode (" + imageFilteringMode + ") is inadequate for JavaTilesFilterJob");
        }
    }

    @Override
    public void compute() {
        for (Tile tile : inputTiles) {
            tile.setMatrix(filter.applyFilter(tile.getMatrix(), tile.getMatrix()));
        }
        setJobResult(JobResultEnum.JOB_RESULT_FILTERED_TILES, inputTiles);
    }

    @Override
    public void dispose() {
        //Nothing to do
    }

}
