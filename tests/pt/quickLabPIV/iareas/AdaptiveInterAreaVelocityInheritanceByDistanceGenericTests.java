// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import static org.junit.Assert.*;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategySuperPosition;
import pt.quickLabPIV.iareas.InterAreaDivisionStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;

/**
 * These tests are intended to be run after 
 * @author lpnm
 *
 */
public class AdaptiveInterAreaVelocityInheritanceByDistanceGenericTests {

	@Test
	public void simpleIASuperPositionDistanceVelocityInheritanceTest1() {		
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaDivisionStrategy(InterAreaDivisionStrategiesFactoryEnum.MixedSuperPositionStrategy);
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Distance);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setImageHeightPixels(107+32);
		parameters.setImageWidthPixels(107+32);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(16);
		parameters.setMarginPixelsIBottom(16);
		parameters.setMarginPixelsJLeft(16);
		parameters.setMarginPixelsJRight(16);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);

		//Compute margin:
		//107 - (64 - 64/3) / (64/3) =  3,015625
		//0,015625 * 64/3 = 0,33333 additional pixels left = 0
		final int marginComputedTop = 16;
		final int marginComputedBottom = 16;
		final int marginComputedLeft = 16;
		final int marginComputedRight = 16;			
		
		//Current iteration step tile geometry
		final int tileWidth = 64;
		final int tileHeight = 64;

		//First tile at (16,16), second tile at (37,16), third tile at (59,16)
		//(37,16) , (37,37) , (37,59)
		//(59,16) , (59,37) , (59,59)
		
		//Parent centers are:
		//(48, 48) - (48,69) - (48,91)
		//(69, 48) - (69,69) - (69,91)
		//(91, 48) - (91,69) - (91,91)
		
		AdaptiveInterAreaStrategySuperPosition strategy = new AdaptiveInterAreaStrategySuperPosition();
		IterationStepTiles stepTiles1 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
		IterationStepTiles stepTiles2 = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, stepTiles1);

		Tile[][] stepTiles1Array = stepTiles1.getTilesArray();
		stepTiles1Array[0][0].accumulateDisplacement(-11, -11);
		stepTiles1Array[0][1].accumulateDisplacement(-11, 1);
		stepTiles1Array[0][2].accumulateDisplacement(-11, 11);
		stepTiles1Array[1][0].accumulateDisplacement(1,  -11);
		stepTiles1Array[1][1].accumulateDisplacement(1, 1);
		stepTiles1Array[1][2].accumulateDisplacement(1, 11);
		stepTiles1Array[2][0].accumulateDisplacement(11,  -11);
		stepTiles1Array[2][1].accumulateDisplacement(11, 1);
		stepTiles1Array[2][2].accumulateDisplacement(11, 11);

		stepTiles2.reuseTiles();
		
		//Number of tiles in step2 is: 107-(32-32/3) / (32/3) = 8,03125
		//8x8 tiles...

		//Grid is      16 - 27 - 37 - 48 - 59 - 69 - 80 - 91
		//Centers are: 32 - 43 - 53 - 64 - 75 - 85 - 96 -107  
		assertEquals("Number of tiles in I doesn't match", 8,  stepTiles2.getNumberOfTilesInI());
		assertEquals("Number of tiles in J doesn't match", 8,  stepTiles2.getNumberOfTilesInJ());
		
		Tile[][] stepTiles2Array = stepTiles2.getTilesArray();
		float[] displacement00 = computeDisplacements(stepTiles2Array[0][0], 32, 32, stepTiles1Array, new int[][]{{0,0}});
		assertEquals("Displacement U is incorrect", displacement00[0], stepTiles2Array[0][0].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement00[1], stepTiles2Array[0][0].getDisplacementV(), 0.000001f);
		
		float[] displacement01 = computeDisplacements(stepTiles2Array[0][1], 32, 32, stepTiles1Array, new int[][]{{0,0}});
		assertEquals("Displacement U is incorrect", displacement01[0], stepTiles2Array[0][1].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement01[1], stepTiles2Array[0][1].getDisplacementV(), 0.000001f);

		float[] displacement02 = computeDisplacements(stepTiles2Array[0][2], 32, 32, stepTiles1Array, new int[][]{{0,0},{0,1}});
		assertEquals("Displacement U is incorrect", displacement02[0], stepTiles2Array[0][2].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement02[1], stepTiles2Array[0][2].getDisplacementV(), 0.000001f);

		float[] displacement03 = computeDisplacements(stepTiles2Array[0][3], 32, 32, stepTiles1Array, new int[][]{{0,0},{0,1}});
		assertEquals("Displacement U is incorrect", displacement03[0], stepTiles2Array[0][3].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement03[1], stepTiles2Array[0][3].getDisplacementV(), 0.000001f);

		float[] displacement04 = computeDisplacements(stepTiles2Array[0][4], 32, 32, stepTiles1Array, new int[][]{{0,1},{0,2}});
		assertEquals("Displacement U is incorrect", displacement04[0], stepTiles2Array[0][4].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement04[1], stepTiles2Array[0][4].getDisplacementV(), 0.000001f);

		float[] displacement05 = computeDisplacements(stepTiles2Array[0][5], 32, 32, stepTiles1Array, new int[][]{{0,1},{0,2}});
		assertEquals("Displacement U is incorrect", displacement05[0], stepTiles2Array[0][5].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement05[1], stepTiles2Array[0][5].getDisplacementV(), 0.000001f);

		float[] displacement06 = computeDisplacements(stepTiles2Array[0][6], 32, 32, stepTiles1Array, new int[][]{{0,2}});
		assertEquals("Displacement U is incorrect", displacement06[0], stepTiles2Array[0][6].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement06[1], stepTiles2Array[0][6].getDisplacementV(), 0.000001f);

		float[] displacement07 = computeDisplacements(stepTiles2Array[0][7], 32, 32, stepTiles1Array, new int[][]{{0,2}});
		assertEquals("Displacement U is incorrect", displacement07[0], stepTiles2Array[0][7].getDisplacementU(), 0.000001f);
		assertEquals("Displacement V is incorrect", displacement07[1], stepTiles2Array[0][7].getDisplacementV(), 0.000001f);

		//Solution table
		int[][] solutionsIndexTable = new int[8][2];
		solutionsIndexTable[0][0] = 0;
		solutionsIndexTable[0][1] = -1;

		solutionsIndexTable[1][0] = 0;
		solutionsIndexTable[1][1] = -1;

		solutionsIndexTable[2][0] = 0;
		solutionsIndexTable[2][1] = 1;

		solutionsIndexTable[3][0] = 0;
		solutionsIndexTable[3][1] = 1;

		solutionsIndexTable[4][0] = 1;
		solutionsIndexTable[4][1] = 2;

		solutionsIndexTable[5][0] = 1;
		solutionsIndexTable[5][1] = 2;

		solutionsIndexTable[6][0] = 2;
		solutionsIndexTable[6][1] = -1;

		solutionsIndexTable[7][0] = 2;
		solutionsIndexTable[7][1] = -1;

		for (int indexI = 0; indexI < 8; indexI++) {
			for (int indexJ = 0; indexJ < 8; indexJ++) {
				int[] solutionsI = solutionsIndexTable[indexI];
				int[] solutionsJ = solutionsIndexTable[indexJ];
				
				int numberOfVectors = 4;
				if (solutionsI[1] < 0 && solutionsJ[1] < 0) {
					numberOfVectors=1;
				} else if (solutionsI[1] < 0) {
					numberOfVectors=2;
				} else if (solutionsJ[1] < 0) {
					numberOfVectors=2;
				}
				
				int[][] validationArray = new int[numberOfVectors][2];
				int vectorIndex = 0;
				for (int vectorI : solutionsI) {
					if (vectorI < 0) {
						continue;
					}
					for (int vectorJ : solutionsJ) {
						if (vectorJ < 0) {
							continue;
						}
						
						validationArray[vectorIndex][0] = vectorI;
						validationArray[vectorIndex++][1] = vectorJ;
					}
				}
				
				float[] displacement = computeDisplacements(stepTiles2Array[indexI][indexJ], 32, 32, stepTiles1Array, validationArray);
				assertEquals("Tile indices [I: " + indexI + ", J: " + indexJ + "] Displacement U is incorrect", displacement[0], stepTiles2Array[indexI][indexJ].getDisplacementU(), 0.000001f);
				assertEquals("Tile indices [I: " + indexI + ", J: " + indexJ + "] Displacement V is incorrect", displacement[1], stepTiles2Array[indexI][indexJ].getDisplacementV(), 0.000001f);				
				
			}
		}
		
	}

	private float computeDistance(float[] vector1, float[] vector2) {
		float result = (float)FastMath.sqrt(FastMath.pow(vector1[0] - vector2[0], 2) + FastMath.pow(vector1[1] - vector2[1], 2));
		
		return result;
	}
	
	private float[] computeDisplacements(Tile tile, int tileHeight, int tileWidth, Tile[][] parentTilesArray, int[][] parentSurroundingTiles) {
		float[] tileCenter = {tile.getTopPixel() + tileHeight / 2.0f, tile.getLeftPixel() + tileWidth / 2.0f};
		float[][] parentTilesCenter = new float[parentSurroundingTiles.length][2];
		float[] distances = new float[parentSurroundingTiles.length];
		float totalInverseDistance = 0.0f;
		int zeroDistanceIndex = -1;
		for (int index = 0; index < parentSurroundingTiles.length; index++) {
			parentTilesCenter[index][0] = parentTilesArray[parentSurroundingTiles[index][0]][parentSurroundingTiles[index][1]].getTopPixel() + tileHeight;
		 	parentTilesCenter[index][1] = parentTilesArray[parentSurroundingTiles[index][0]][parentSurroundingTiles[index][1]].getLeftPixel() + tileWidth;
		 	distances[index] = computeDistance(parentTilesCenter[index], tileCenter);
		 	if (distances[index] > 0.0f) {
		 		totalInverseDistance += 1.0f/distances[index];
		 	} else {
		 		zeroDistanceIndex = index;
		 	}
		}
		
		float[] weights = new float[parentSurroundingTiles.length];
		for (int index = 0; index < parentSurroundingTiles.length; index++) {
			if (zeroDistanceIndex < 0) {
				weights[index] = (1.0f/distances[index])/totalInverseDistance;
			} else {
				if (zeroDistanceIndex != index) {
					weights[index] = 0.0f;
				} else {
					weights[index] = 1.0f;
				}
			}
		}
				
		float[] displacement = new float[2];
		for (int index = 0; index < parentSurroundingTiles.length; index++) {
			displacement[0] += weights[index] * parentTilesArray[parentSurroundingTiles[index][0]][parentSurroundingTiles[index][1]].getDisplacementU();
			displacement[1] += weights[index] * parentTilesArray[parentSurroundingTiles[index][0]][parentSurroundingTiles[index][1]].getDisplacementV();
		}
		
		return displacement; 
	}
}
