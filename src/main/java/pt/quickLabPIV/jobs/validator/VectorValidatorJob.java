// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.replacement.IVectorReplacement;
import pt.quickLabPIV.iareas.replacement.VectorReplacementFactoryEnum;
import pt.quickLabPIV.iareas.validation.IVectorValidator;
import pt.quickLabPIV.iareas.validation.VectorValidatorException;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobComputeException;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.maximum.MaxCrossResult;

interface VisitorInterface {
    MaxCrossResult visit(MaxCrossResult r, Object status);
}


public class VectorValidatorJob extends Job<VectorValidatorJobParameters,IterationStepTiles> {
    private static final Logger logger = LoggerFactory.getLogger(VectorValidatorJob.class);
    
    private IVectorValidator[] validatorStrategies;
    private IVectorReplacement[] replacementStrategies;
    private VectorValidatorJobParameters parameters;
    private VectorValidatorConfiguration configuration;
    private int totalCorrectedVectors = 0;
    private boolean validationDisabled;
    
    public VectorValidatorJob() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        if (singleton.getPIVParameters().getVectorValidatorStrategy() != VectorValidatorFactoryEnum.None) {
            Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(VectorValidatorConfiguration.IDENTIFIER);
            if (configurationObject == null) {
                throw new VectorValidatorException("Couldn't retrieve vector validator job configuration");
            }
            configuration = (VectorValidatorConfiguration)configurationObject;
            validatorStrategies = VectorValidatorFactoryEnum.createValidator(VectorValidatorFactoryEnum.CombinedValidator);
            replacementStrategies = VectorReplacementFactoryEnum.createReplacer(VectorReplacementFactoryEnum.CombinedReplacement);
        } else {
            validationDisabled = true;
        }
    }
    
    @Override
    public void analyze() {
        if (validationDisabled) {
            return;
        }
        
        if (validatorStrategies == null || replacementStrategies == null) {
            throw new JobAnalyzeException("Cannot have a null vector validator or null vector replacement strategy defined");
        }
        
        if (validatorStrategies.length != replacementStrategies.length) {
            throw new JobAnalyzeException("Number of validator strategies doesn't match number of replacement strategies");
        }
        
        for (IVectorValidator validator : validatorStrategies) {
            if (validator == null) {
                throw new JobAnalyzeException("Validator strategy cannot be null");
            }
        }
             
        parameters = getInputParameters(JobResultEnum.JOB_RESULT_VALIDATOR_DATA);
    }
    
    private boolean shouldDoAnotherAttempt(int currentAttempt, int correctedVectors) {
        int maxCorrectionIterations = configuration.getMaxCorrectionIterations();
        boolean iterateUntilNoMoreCorrections = configuration.isIterateUntilNoMoreCorrections();

        totalCorrectedVectors += correctedVectors;
        
        if (correctedVectors == 0) {
            logger.info("Stopped at iteration {} due to no more corrected vectors. Total corrected vectors {}.", 
                    currentAttempt, totalCorrectedVectors);
            return false;
        }
        
        if (!iterateUntilNoMoreCorrections && currentAttempt >= maxCorrectionIterations) {
            logger.info("Stopped at iteration {} due to max. correction iterations limit. Total corrected vectors {}.", 
                    currentAttempt, totalCorrectedVectors);
            return false;
        }
        
        return true;
    }

    protected MaxCrossResult[][] circularVisitor(int maxResultsCount, IterationStepTiles stepTiles, MaxCrossResult[][] arrayedCrossResults, VisitorInterface visitor, Object status) {
        int startI = stepTiles.getNumberOfTilesInI() / 2;
        int startJ = stepTiles.getNumberOfTilesInJ() / 2;
        int sideI = 2;
        if (stepTiles.getNumberOfTilesInI() != startI*2) {
            //If the array has an odd number of entries in I, just start by considering the array center location in I
            sideI = 1;
        } else {
            startI--;
        }
        
        int sideJ = 2;
        if (stepTiles.getNumberOfTilesInJ() != startJ*2) {
            //If the array has an odd number of entries in J, just start by considering the array center location in J
            sideJ = 1;
        } else {
            startJ--;
        }
        
        int visitedTiles = 0;
        boolean checkMatrix[][] = new boolean[stepTiles.getNumberOfTilesInI()][stepTiles.getNumberOfTilesInJ()];
        
        int moves[] = new int[4]; //Right, Down, Left, Up
        
        while (visitedTiles < maxResultsCount) {
            //Move right
            if (sideJ > 1) {
                moves[0] = sideJ - 1;
            } else {
                moves[0] = 0;
            }
            
            //Move down
            if (sideI > 1) {
                moves[1] = sideI - 1;
            } else {
                moves[1] = 0;
            }
            
            //Move left
            if (sideJ > 1) {
                moves[2] = sideJ - 1;
            } else {
                moves[2] = 0;
            }
            
            //Move up
            if (sideI > 2) {
                moves[3] = sideI - 2;
            } else {
                moves[3] = 0;
            }
            
            int j = startJ;
            int i = startI;
            visitedTiles = visitTileHelper(stepTiles, arrayedCrossResults, visitor, status, visitedTiles, checkMatrix, j, i);
    
            for (int offsetJ = 0; offsetJ < moves[0]; offsetJ++) {
                visitedTiles = visitTileHelper(stepTiles, arrayedCrossResults, visitor, status, visitedTiles, checkMatrix, ++j, i);
            }
            
            for (int offsetI = 0; offsetI < moves[1]; offsetI++) {
                visitedTiles = visitTileHelper(stepTiles, arrayedCrossResults, visitor, status, visitedTiles, checkMatrix, j, ++i);
            }
    
            if (moves[1] > 0) {
                for (int offsetJ = 0; offsetJ < moves[2]; offsetJ++) {
                    visitedTiles = visitTileHelper(stepTiles, arrayedCrossResults, visitor, status, visitedTiles, checkMatrix, --j, i);
                }
        
                for (int offsetI = 0; offsetI < moves[3]; offsetI++) {
                    visitedTiles = visitTileHelper(stepTiles, arrayedCrossResults, visitor, status, visitedTiles, checkMatrix, j, --i);
                }
            }
            
            sideI += 2;
            sideJ += 2;
            startJ--;
            startI--;
        }
        
        return arrayedCrossResults;
    }

    private int visitTileHelper(IterationStepTiles stepTiles, MaxCrossResult[][] arrayedCrossResults,
            VisitorInterface visitor, Object status, int visitedTiles, boolean[][] checkMatrix, int j, int i) {
        if (i >= 0 && i < stepTiles.getNumberOfTilesInI() &&
            j >= 0 && j < stepTiles.getNumberOfTilesInJ() &&
            arrayedCrossResults[i][j] != null) {
            if (checkMatrix[i][j]) {
                throw new VectorValidatorException("Already visited i: " + i + ", j:" + j);
            }
            arrayedCrossResults[i][j] = visitor.visit(arrayedCrossResults[i][j], status);
            //System.out.println("Visiting " + i + ", " + j);
            checkMatrix[i][j]=true;
            visitedTiles++;                        
        }
        
        return visitedTiles;
    }
    
    protected MaxCrossResult[][] sweepVisitor(int maxResultsCount, IterationStepTiles stepTiles, MaxCrossResult[][] arrayedCrossResults, VisitorInterface visitor, Object status) {
        for (int i = 0; i < stepTiles.getNumberOfTilesInI(); i++) {
            for (int j = 0; j < stepTiles.getNumberOfTilesInJ(); j++) {
                if (arrayedCrossResults[i][j] != null) {
                    arrayedCrossResults[i][j] = visitor.visit(arrayedCrossResults[i][j], status);
                }
            }
        }
        
        return arrayedCrossResults;
    }

    
    private Tile[][] getNeighbors(Tile[][] neighbors, Tile tile, int tileI, int tileJ) {
        IterationStepTiles stepTiles = parameters.stepTiles;
        
        //Fill in neighbors for this tile
        for (int neighborI = 0; neighborI < 3; neighborI++) {
            for (int neighborJ = 0; neighborJ < 3; neighborJ++) {
                if (tileI + (neighborI - 1) < 0) {                            
                    neighbors[neighborI][neighborJ] = null;
                } else if (tileI + (neighborI - 1) >= stepTiles.getNumberOfTilesInI()){
                    neighbors[neighborI][neighborJ] = null;
                } else if (tileJ + (neighborJ - 1) < 0){
                    neighbors[neighborI][neighborJ] = null;
                } else if (tileJ + (neighborJ - 1) >= stepTiles.getNumberOfTilesInJ()) {
                    neighbors[neighborI][neighborJ] = null;
                } else {
                    neighbors[neighborI][neighborJ] = stepTiles.getTile(tileI + (neighborI - 1), tileJ + (neighborJ - 1));
                }                        
            }
        }
        
        return neighbors;
    }
    
    @Override
    public void compute() {
        if (validationDisabled || validatorStrategies.length == 0) {
            setJobResult(JobResultEnum.JOB_RESULT_VALIDATED_VECTORS, parameters.stepTiles);
            return;
        }
        
        List<MaxCrossResult> tileMaxResults = new ArrayList<MaxCrossResult>(1);
        IterationStepTiles stepTiles = parameters.stepTiles;
        List<MaxCrossResult> maxResults = parameters.maxResults;
        
        MaxCrossResult[][] arrayedCrossResults = new MaxCrossResult[stepTiles.getNumberOfTilesInI()][stepTiles.getNumberOfTilesInJ()];
        //Backup original displacement vector with main peak value and previously accumulated displacement,
        //while also initializing the validation state.
        final Tile[][] neighbors = new Tile[3][3];
        for (MaxCrossResult maxResult : maxResults) {
            //TODO map Max Cross Results to an array
            //And validate from the center outwards
            final int i = maxResult.tileA.getTileIndexI();
            final int j = maxResult.tileA.getTileIndexJ();
            Tile tile = stepTiles.getTile(i, j);
            tile.backupDisplacement();

            arrayedCrossResults[i][j] = maxResult;
        }

        VisitorInterface initializerVisitor = (MaxCrossResult r, Object s) -> {
            int i = r.tileA.getTileIndexI();
            int j = r.tileA.getTileIndexJ();
            
            Tile tile = stepTiles.getTile(i, j);
            
            getNeighbors(neighbors, tile, i, j);

            validatorStrategies[0].validateVector(tile, neighbors, stepTiles);
            if (!tile.isInvalidDisplacement()) {
                tile.setLockedValidationState(true);
            }
            return r;
        };
        
        arrayedCrossResults = circularVisitor(maxResults.size(), stepTiles, arrayedCrossResults, initializerVisitor, null);
        //arrayedCrossResults = sweepVisitor(maxResults.size(), stepTiles, arrayedCrossResults, initializerVisitor, null);

        VisitorInterface validatorVisitor = (MaxCrossResult r, Object statusObj) -> {
            VectorValidatorStatus status = (VectorValidatorStatus)statusObj;
            
            final int i = r.tileA.getTileIndexI();
            final int j = r.tileA.getTileIndexJ();
            Tile tile = stepTiles.getTile(i, j);
                          
            getNeighbors(neighbors, tile, i, j);
            
            tileMaxResults.clear();
            tileMaxResults.add(r);            
            int lastIndex = -1;
            validatorStrategies[0].validateVector(tile, neighbors, stepTiles);
            for (int index = 0; (index < validatorStrategies.length) && tile.isInvalidDisplacement(); index++) {
                if (!tile.isInvalidDisplacement() || tile.isMaskedDisplacement()) {
                    lastIndex = index;
                    break;
                }

                if (replacementStrategies[index] != null) {
                    //FIXME This may have null implication, but one has to check what is done after the validation Job with
                    //invalidated vectors during intermediate processing steps, since currently only the tiles with correspondent
                    //MaxResults are considered, ignoring the remaining ones, which can be neighbors of some invalidated tile, 
                    //also becoming invalid when doing above local neighbor validation. In the the end those tiles will be invalid,
                    //but will be ignored because they have no corresponding MaxResults and will exit the validation Job with
                    //invalid state, without any replacement attempt. This can only happen when doing Stabilization phases.
                    //restore original neighbors validation state is required when doing validation steps, because in
                    //validation steps, not all Tiles are consulted and may become permanently invalid, when they could be substituted...
                    //OR Those Tiles must be revalidated later after this loop, also note they can't be used with SecondaryPeaks,
                    //because they will not be in MaxResults state, other option is to load MaxResults into a matrix similar to
                    //IterationStepTiles.

                    //Ensure that all neighbor tiles are validated too, because some substitution methods rely on 
                    //neighbors vector validation state. 
                    /*for (int localI = 0; localI < 3; localI++) {
                        for (int localJ = 0; localJ < 3; localJ++) {
                            Tile localTile = neighbors[localI][localJ];
                            if (localTile != null && localI != 1 && localJ != 1) {
                                localNeighbors = getNeighbors(localNeighbors, tile, i + (localI - 1), j + (localJ - 1));
                                validatorStrategies[0].validateVector(localTile, localNeighbors, stepTiles);
                            }
                        }
                    }*/

                    //Restore original vector displacement at the main peak displacement value, to ensure that
                    //peak replacement and other updates dependent on main peak displacement are not corrupted,
                    //when validating over multiple iterations.
                    tile.restoreDisplacement();
                    replacementStrategies[index].replaceVector(index == 0, parameters.currentFrame, 
                                                               tile, neighbors, tileMaxResults);
                }
                
                validatorStrategies[index].validateVector(tile, neighbors, stepTiles);
                if (!tile.isInvalidDisplacement()) {
                    //A vector that was invalid is now corrected
                    status.correctedVectors++;
                }
                if (!tile.isInvalidDisplacement() || tile.isMaskedDisplacement()) {
                    lastIndex = index;
                    break;
                }
            }
            
            if (tile.isInvalidDisplacement()) {
                logger.info("Tile: {} could not be validated at validation step: {}", tile, validatorStrategies.length);
                status.invalidVectors++;
            }
            
            if (tile.isMaskedDisplacement()) {
                logger.warn("Tile: {} was masked at validation step: {}", tile, lastIndex+1);
            } else if (!tile.isInvalidDisplacement() && lastIndex >= 0) {
                logger.info("Tile: {} was only validated at validation step: {}", tile, lastIndex+1);
            }
            
            return r;
        };
        
        final VectorValidatorStatus status = new VectorValidatorStatus();
        
        int currentAttempt = 0;
        do {
            status.invalidVectors = 0;
            status.correctedVectors = 0;
            
            arrayedCrossResults = circularVisitor(maxResults.size(), stepTiles, arrayedCrossResults, validatorVisitor, status);             
            //arrayedCrossResults = sweepVisitor(maxResults.size(), stepTiles, arrayedCrossResults, validatorVisitor, status);
            
            currentAttempt++;
            logger.debug("End of vector validation iteration {} with iteration corrected vectors {} and invalids {}",
                          currentAttempt, status.correctedVectors, status.invalidVectors); 
        } while (shouldDoAnotherAttempt(currentAttempt, status.correctedVectors));
        
        //Only now set all still invalid vectors to 0.0 displacements
        int invalids = 0;
        for (MaxCrossResult maxResult : maxResults) {
            final int i = maxResult.tileA.getTileIndexI();
            final int j = maxResult.tileA.getTileIndexJ();
            Tile tile = stepTiles.getTile(i, j);
            if (tile.isInvalidDisplacement()) {
                invalids++;
                tile.replaceDisplacement(0.0f, 0.0f);
            }
        }
        logger.warn("Left validation in {} iterations with {} invalid vectors and {} vector corrections", 
                currentAttempt, invalids, totalCorrectedVectors);
        
        final int totalTiles = stepTiles.getNumberOfTilesInI() * stepTiles.getNumberOfTilesInJ();
        if (maxResults.size() != totalTiles && replacementStrategies[0] != null) {
            //See above FIXME
            //This may have null implication, but one has to check what is done after the validation Job with
            //invalidated vectors during intermediate processing steps, since currently only the tiles with correspondent
            //MaxResults are considered, ignoring the remaining ones, which can be neighbors of some invalidated tile, 
            //also becoming invalid when doing above local neighbor validation. In the the end those tiles will be invalid,
            //but will be ignored because they have no corresponding MaxResults and will exit the validation Job with
            //invalid state, without any replacement attempt. This can only happen when doing Stabilization phases.
            throw new JobComputeException("Interrogation area stabilization is currently not supported with validation and replacement");            
        }
        
        setJobResult(JobResultEnum.JOB_RESULT_VALIDATED_VECTORS, parameters.stepTiles);
    }

    @Override
    public void dispose() {
        //Nothing to do
    }
}
