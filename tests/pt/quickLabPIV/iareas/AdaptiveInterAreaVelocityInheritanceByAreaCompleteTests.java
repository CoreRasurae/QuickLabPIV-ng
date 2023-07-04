package pt.quickLabPIV.iareas;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategySuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;

public class AdaptiveInterAreaVelocityInheritanceByAreaCompleteTests {

	@Before
	public void setup() {
		
	}
	
	@Test
	public void testSuperPositionNextIAs_1ThirdPass() {
		final PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
		parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Area);
		parameters.setWarpingMode(WarpingModeFactoryEnum.FirstImageBiLinearWarping);
		parameters.setImageHeightPixels(107+64);
		parameters.setImageWidthPixels(107+64);
		parameters.setOverlapFactor(1.0f/3.0f);
		parameters.setMarginPixelsITop(32);
		parameters.setMarginPixelsIBottom(32);
		parameters.setMarginPixelsJLeft(32);
		parameters.setMarginPixelsJRight(32);
		parameters.setInterrogationAreaStartIPixels(64);
		parameters.setInterrogationAreaEndIPixels(32);
		parameters.setInterrogationAreaStartJPixels(64);
		parameters.setInterrogationAreaEndJPixels(32);
		
		//First tile at (16,16), second tile at (37,16), third tile at (59,16)
		//(37,16) , (37,37) , (37,59)
		//(59,16) , (59,37) , (59,59)
		

		//Top-left matrix with 
		//( 16, 16) - ( 16, 37) - ( 16, 59)
		//    |           |           |
		//( 37, 16) - ( 37, 37) - ( 37, 59)
		//    |           |           |
		//( 59, 16) - ( 59, 37) - ( 59, 59)
		
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
		
		
		//Check tile (I:0, J:0)...
		float[] displacements00 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[1][0], stepTiles1Array[1][1]}, 
													new float[] { 32*32, 32*11, 11*32, 11*11 });
		assertEquals("Displacement in U doesn't match", displacements00[0], stepTiles2Array[0][0].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements00[1], stepTiles2Array[0][0].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:1)...
		float[] displacements01 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[1][0], stepTiles1Array[1][1]}, 
													new float[] { 32*32, 22*32, 11*32, 11*22 });
		assertEquals("Displacement in U doesn't match", displacements01[0], stepTiles2Array[0][1].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements01[1], stepTiles2Array[0][1].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:2)...
		float[] displacements02 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][0], stepTiles1Array[1][1], stepTiles1Array[1][2]}, 
													new float[] { 32*32, 32*32, 32*10, 11*32, 11*32, 11*10 });
		assertEquals("Displacement in U doesn't match", displacements02[0], stepTiles2Array[0][2].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements02[1], stepTiles2Array[0][2].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:3)...
		float[] displacements03 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][0], stepTiles1Array[1][1], stepTiles1Array[1][2]}, 
													new float[] { 32*32, 32*32, 32*21, 11*32, 11*32, 11*21 });
		assertEquals("Displacement in U doesn't match", displacements03[0], stepTiles2Array[0][3].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements03[1], stepTiles2Array[0][3].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:4)...
		float[] displacements04 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][0], stepTiles1Array[1][1], stepTiles1Array[1][2] }, 
													new float[] { 32*21, 32*32, 32*32, 11*21, 11*32, 11*32 });
		assertEquals("Displacement in U doesn't match", displacements04[0], stepTiles2Array[0][4].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements04[1], stepTiles2Array[0][4].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:5)...
		float[] displacements05 = computeWeights(new Tile[] {stepTiles1Array[0][0], stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][0], stepTiles1Array[1][1], stepTiles1Array[1][2]}, 
													new float[] { 32*11, 32*32, 32*32, 11*11, 11*32, 11*32 });
		assertEquals("Displacement in U doesn't match", displacements05[0], stepTiles2Array[0][5].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements05[1], stepTiles2Array[0][5].getDisplacementV(), 0.0001f);

		//Check tile (I:0, J:6)...
		float[] displacements06 = computeWeights(new Tile[] {stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][1], stepTiles1Array[1][2]}, 
													new float[] { 32*21, 32*32, 11*21, 11*32 });
		assertEquals("Displacement in U doesn't match", displacements06[0], stepTiles2Array[0][6].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements06[1], stepTiles2Array[0][6].getDisplacementV(), 0.0001f);
		
		//Check tile (I:0, J:7)...
		float[] displacements07 = computeWeights(new Tile[] {stepTiles1Array[0][1], stepTiles1Array[0][2], 
				stepTiles1Array[1][1], stepTiles1Array[1][2]}, 
													new float[] { 32*10, 32*32, 11*10, 11*32 });
		assertEquals("Displacement in U doesn't match", displacements07[0], stepTiles2Array[0][7].getDisplacementU(), 0.0001f);
		assertEquals("Displacement in V doesn't match", displacements07[1], stepTiles2Array[0][7].getDisplacementV(), 0.0001f);
		
		
		int[][] contributingTilesIndicesMap = {
				{0, 1},
				{0, 1},
				{0, 1, 2},
				{0, 1, 2},
				{0, 1, 2},
				{0, 1, 2},
				{1, 2},
				{1, 2},
			};
		
		float[][] contributingAreasMap = {
				{32, 11},
				{32, 22},
				{32, 32, 10},
				{32, 32, 21},
				{21, 32, 32},
				{11, 32, 32},
				{21, 32},
				{10, 32}
			};
		
		for (int indexI = 0; indexI < stepTiles2.getNumberOfTilesInI(); indexI++) {
			for (int indexJ = 0; indexJ < stepTiles2.getNumberOfTilesInJ(); indexJ++) {
				Tile currentTile = stepTiles2Array[indexI][indexJ];
				
				int[] tileIIndices = contributingTilesIndicesMap[indexI];
				int[] tileJIndices = contributingTilesIndicesMap[indexJ];
			
				float[] tileIAreas = contributingAreasMap[indexI];
				float[] tileJAreas = contributingAreasMap[indexJ];
				
				Tile[] contributingTiles = new Tile[tileIIndices.length * tileJIndices.length]; 
				float[] contributingAreas = new float[tileIAreas.length * tileJAreas.length];
				
				int index = 0;
				for (int i = 0; i < tileIIndices.length; i++) {
					for (int j = 0; j < tileJIndices.length; j++) {
						contributingTiles[index] = stepTiles1Array[tileIIndices[i]][tileJIndices[j]];
						contributingAreas[index++] = tileIAreas[i]*tileJAreas[j];
					}
				}
				
				float[] displacements = computeWeights(contributingTiles, contributingAreas);
				
				assertEquals("Displacement in U doesn't match for tile at I: " + indexI + ", J: " + indexJ, displacements[0], currentTile.getDisplacementU(), 0.0001f);
				assertEquals("Displacement in V doesn't match for tile at I: " + indexI + ", J: " + indexJ, displacements[1], currentTile.getDisplacementV(), 0.0001f);
			}
		}
	}
	
	private float[] computeWeights(Tile[] contributingTiles, float[] contributingAreas) {
		float[] result = new float[2];
		float totalArea = 0.0f;
		for (float area : contributingAreas) {
			totalArea += area;
		}
		
		int index = 0;
		for (Tile tile : contributingTiles) {
			result[0] += contributingAreas[index] / totalArea * tile.getDisplacementU();
			result[1] += contributingAreas[index++] / totalArea * tile.getDisplacementV();
		}
		
		return result;
	}
}
