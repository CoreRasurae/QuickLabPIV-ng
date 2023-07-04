package pt.quickLabPIV.jobs.validator;

import java.util.List;

import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class VectorValidatorJobParameters {
    public IterationStepTiles stepTiles;
    public List<MaxCrossResult> maxResults;
    public int currentFrame;
}
