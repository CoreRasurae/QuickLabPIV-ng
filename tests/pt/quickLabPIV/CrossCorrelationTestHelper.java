// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.ArrayList;
import java.util.List;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.iareas.Tile;

public class CrossCorrelationTestHelper {
	public static List<Matrix> localCrossCorrelationTile(List<Tile> inputTilesF, List<Tile> inputTilesG) {
		List<Matrix> matricesF = new ArrayList<Matrix>(inputTilesF.size());
		List<Matrix> matricesG = new ArrayList<Matrix>(inputTilesG.size());
	
		for (Tile tile : inputTilesF) {
			matricesF.add(tile.getMatrix());
		}
		
		for (Tile tile : inputTilesG) {
			matricesG.add(tile.getMatrix());
		}
		
		return localCrossCorrelation(matricesF, matricesG);
	}

	public static List<Matrix> localCrossCorrelation(final List<Matrix> matricesF, final List<Matrix> matricesG) {
		Matrix matrixF = matricesF.get(0);
		Matrix matrixG;
		
		final int dimCrossI = 2*matrixF.getHeight()-1;
		final int dimCrossJ = 2*matrixF.getWidth()-1;
		
		final int dimI = matrixF.getHeight();
		final int dimJ = matrixF.getWidth();
		
		List<Matrix> results = new ArrayList<Matrix>();
		for (int matrixIndex = 0; matrixIndex < matricesF.size(); matrixIndex++) {
			Matrix result = new MatrixFloat((short)dimCrossI,(short)dimCrossJ);
		
			matrixF = matricesF.get(matrixIndex);
			matrixG = matricesG.get(matrixIndex);
			for (short i = (short)(-dimCrossI/2); i <= dimCrossI/2; i++) {
				for (short j = (short)(-dimCrossJ/2); j <= dimCrossJ/2; j++) {
					float accum = 0;
					
					for (short n = (short)(-dimCrossI/2); n <= dimCrossI/2; n++) {
						for (short m = (short)(-dimCrossJ/2); m <= dimCrossJ/2; m++) {
							if (n < 0 || m < 0 || i + n < 0 || j + m < 0) {
								continue;
							}
							if (i + n >= dimI || j + m >= dimJ) {
								continue;
							}
							
							accum += matrixF.getElement(n,m)*matrixG.getElement((short)(i + n), (short)(j + m));
						}
					}
					
					result.setElement(accum, (short)(i+dimCrossI/2), (short)(j+dimCrossJ/2));
				}
			}
			
			results.add(result);
		}
		
		return results;
	}
}
