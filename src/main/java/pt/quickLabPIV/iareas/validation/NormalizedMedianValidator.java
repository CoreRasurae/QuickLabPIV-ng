package pt.quickLabPIV.iareas.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;

public class NormalizedMedianValidator implements IVectorValidator {
    private final float threshold;
    private final float epsilon0;
    private ArrayList<Tile> sortedTiles = new ArrayList<Tile>(8);
    private float[] rIs = new float[8];
    private Comparator<Tile> distanceComparator = new Comparator<Tile>() {
        @Override
        public int compare(Tile t1, Tile t2) {
            float u1 = t1.getDisplacementU();
            float v1 = t1.getDisplacementV();
            float u2 = t2.getDisplacementU();
            float v2 = t2.getDisplacementV();

            float norm1 = u1*u1 + v1*v1;
            float norm2 = u2*u2 + v2*v2;
            
            if (norm1 > norm2) {
                return 1;
            } else if (norm1 < norm2) {
                return -1;
            } else {
                return 0;
            }
        }        
    };
    
    public NormalizedMedianValidator() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(NormalizedMedianValidatorConfiguration.IDENTIFIER);
        if (configurationObject == null) {
            throw new VectorValidatorException("Couldn't retrieve Normalized Median validator configuration");
        }
        NormalizedMedianValidatorConfiguration configuration = (NormalizedMedianValidatorConfiguration)configurationObject;
        threshold = configuration.getDistanceThreshold();
        epsilon0 = configuration.getEpsilon0();
    }
    
    public NormalizedMedianValidator(NormalizedMedianValidatorConfiguration configuration) {
        threshold = configuration.getDistanceThreshold();
        epsilon0 = configuration.getEpsilon0();        
    }

    @Override
    public void validateVector(Tile tile, Tile[][] adjacents, IterationStepTiles stepTiles) {
        if (tile.isMaskedDisplacement()) {
            return;
        }
        sortedTiles.clear();        
        for (int indexI = 0; indexI < 3; indexI++) {
            for (int indexJ = 0; indexJ < 3; indexJ++) {
                if (indexI == 1 && indexJ == 1) {
                    continue;
                }
                
                Tile adjacent = adjacents[indexI][indexJ];
                if (adjacent == null) {
                    continue;
                }
                
                if (adjacent.isMaskedDisplacement()) {
                    continue;
                }

                sortedTiles.add(adjacent);
            }
        }
        Collections.sort(sortedTiles, distanceComparator);
        
        float medianU = 0.0f;
        float medianV = 0.0f;
        int neighborsCount = sortedTiles.size();
        if (neighborsCount > 0) {
            if (neighborsCount % 2 == 0) {
                //Even
                medianU = (sortedTiles.get(neighborsCount/2-1).getDisplacementU() + sortedTiles.get(neighborsCount/2).getDisplacementU())/2.0f;
                medianV = (sortedTiles.get(neighborsCount/2-1).getDisplacementV() + sortedTiles.get(neighborsCount/2).getDisplacementV())/2.0f;
            } else {
                //Odd
                medianU = sortedTiles.get(neighborsCount/2).getDisplacementU();
                medianV = sortedTiles.get(neighborsCount/2).getDisplacementV();
            }
        }
        
        for (int indexI = 0; indexI < rIs.length; indexI++) {
            rIs[indexI] = Float.MAX_VALUE;
        }
        
        int rIndex = 0;
        for (Tile sortedTile : sortedTiles) {
            float dU = medianU - sortedTile.getDisplacementU(); 
            float dV = medianV - sortedTile.getDisplacementV();
            float rI = dU*dU + dV*dV;
            rIs[rIndex++] = rI;
        }
        Arrays.sort(rIs);
        
        float rMed;
        if (rIndex - 1 == 0) {
            rMed = 1.0f;
        } else {
            if ((rIndex - 1) % 2 == 0) {
                rMed = (float)FastMath.sqrt((rIs[(rIndex-1)/2] + rIs[(rIndex-1)/2 + 1])/2.0f);   
            } else {
                rMed = (float)FastMath.sqrt(rIs[(rIndex-1)/2 + 1]);
            }
        }
        
        float dU = medianU - tile.getDisplacementU();
        float dV = medianV - tile.getDisplacementV();
        
        float distance = (float)FastMath.sqrt(dU*dU + dV*dV)/(rMed + epsilon0);
        if (distance < threshold) {
            tile.setInvalidDisplacement(false);
        } else {
            tile.setInvalidDisplacement(true);
        }        
    }
}
