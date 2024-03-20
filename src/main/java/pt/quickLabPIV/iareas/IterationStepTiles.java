// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.exporter.SingleFrameFloatMatlabExporter;
import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.maximum.MaxCrossResult;

//Part of the iteration tile will have no displacement. They are the first image/frame. It is the reference image for cross-correlation.
//Since no displacement will exist, their tile position is fixed across all adaptive steps.
//The second image/frame will have displacement which the tiles will follow.

//When recomputing unstable tiles, there will be two instances of IterationStepTiles one that suffers no displacement (first-frame), and
//one that suffers displacements (second frame). However both can be related by their I and J indices, so that the second-frame IterationStepTiles, one
//can do getUnstableVelocityTiles() to obtain all tiles that still need further computations, then corresponding first-frames can be obtained from 
//getRelatedTiles(refTiles).

/**
 * IterationStepTiles class is responsible for keeping track of the image tiles and their positions, 
 * as well as, storing PIV displacements results, clipping images and also for determining when a tile displacement has become stable.  
 * @author lpnm
 */
public class IterationStepTiles {
	private static Logger logger = LoggerFactory.getLogger(IterationStepTiles.class);
	
	private PIVInputParameters pivParameters = PIVContextSingleton.getSingleton().getPIVParameters();
	private IterationStepTiles parentStepTiles;
	
	private final TilesOrderEnum tilesOrder;
	private final byte currentStep;
	private final byte maxAdaptiveSteps;
	private short currentStepRetries;
	
	private short marginTop;
	private short marginBottom;
	private short marginLeft;
	private short marginRight;
	
	private final short tileWidth;
	private final short tileHeight;
	
	private final short numberOfTilesInI;
	private final short numberOfTilesInJ;
	
	private boolean denseTiles = false;
    private short denseWidth;
    private short denseHeight;
    
	private Tile[][] tiles;	
	
	//Strategy to decide which tiles need to be recomputed or are already stable
	private IInterAreaStableStrategy stabilizationStrategy;
	private IInterAreaDivisionStrategy iaDivisionStrategy;
	private IInterAreaVelocityInheritanceStrategy velocityInheritanceStrategy;

	private float uBuffer[], vBuffer[];
	
	public IterationStepTiles(IInterAreaDivisionStrategy newDivisionStrategy, 
				IInterAreaStableStrategy newStabilizationStrategy,
				IInterAreaVelocityInheritanceStrategy newVelocityInheritanceStrategy,
				final TilesOrderEnum _tilesOrder,
				final int _currentStep, final int _maxAdaptiveSteps,
				final short newTileWidth, final short newTileHeight, final short tilesInI, final short tilesInJ,
				final short marginTop, final short marginLeft, final short marginBottom, final short marginRight) {
		iaDivisionStrategy = newDivisionStrategy;
		stabilizationStrategy = newStabilizationStrategy;
		velocityInheritanceStrategy = newVelocityInheritanceStrategy;
		
		tilesOrder = _tilesOrder;
		
		currentStep = (byte)_currentStep;
		maxAdaptiveSteps = (byte)_maxAdaptiveSteps;
		
		currentStepRetries = 0;
		
		tileHeight = newTileHeight;
		tileWidth = newTileWidth;
		
		numberOfTilesInI = tilesInI;
		numberOfTilesInJ = tilesInJ;
		
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		
		tiles = new Tile[numberOfTilesInI][numberOfTilesInJ];
		for (int i = 0; i < numberOfTilesInI; i++) {
			for (int j = 0; j < numberOfTilesInJ; j++) {
				tiles[i][j] = new Tile(this);
			}
		}
	}
	
	Tile[][] getTilesArray() {
		return tiles;
	}
	
	void setParentStepTiles(IterationStepTiles parent) {
		if (parentStepTiles == null) {
			parentStepTiles = parent;
		}
	}
	
	short getCurrentStepRetries() {
		return currentStepRetries;
	}
	
	/**
	 * Retrieves the current step/iteration of the tiles division steps as assigned by the IInterrogationAreaDivisionStrategy strategy.
	 * @return the current iteration step
	 */
	public short getCurrentStep() {
		return currentStep;
	}
		
	/**
	 * Obtains the top margin pixels value.
	 * @return the top margin in pixels
	 */
	public short getMarginTop() {
		return marginTop;
	}
	
	/**
	 * Obtains the bottom margin pixels value.
	 * @return the bottom margin in pixels
	 */
	public short getMarginBottom() {
		return marginBottom;
	}
	
	/**
	 * Obtains the left margin pixels value.
	 * @return the left margin in pixels
	 */
	public short getMarginLeft() {
		return marginLeft;
	}
	
	/**
	 * Obtains the right margin pixels value.
	 * @return the right margin in pixels
	 */
	public short getMarginRight() {
		return marginRight;
	}

	/**
	 * Obtains the width of a single tile.
	 * @return the tile width in pixels
	 */
	public short getTileWidth() {
		return tileWidth;
	}
	
	/**
	 * Obtains the height of a single tile.
	 * @return the tile height in pixels
	 */
	public short getTileHeight() {
		return tileHeight;
	}
	
	/**
	 * The number of tiles I-wise (row wise).
	 * @return the number of tiles in I direction
	 */
	public short getNumberOfTilesInI() {
		return numberOfTilesInI;
	}

	/**
	 * The number of tiles J-wise (column wise).
	 * @return the number of tiles in J direction.
	 */
	public short getNumberOfTilesInJ() {
		return numberOfTilesInJ;
	}

	/**
	 * Get previous step tiles, from which the this instance inherits its structure.
	 * @return the previous step tiles instance
	 */
	public IterationStepTiles getParentStepTiles() {
		return parentStepTiles;
	}
	
	/**
	 * Sets the top-left pixel for a given tile.
	 * @param tileIndexI the I index for the tile to be set
	 * @param tileIndexJ the J index for the tile to be set
	 * @param topPixel the top pixel where the tile begins
	 * @param leftPixel the left pixel where the tile begins
	 */
	protected void setTileTopLeft(short tileIndexI, short tileIndexJ, short topPixel, short leftPixel) {
		Tile tile = tiles[tileIndexI][tileIndexJ];
		tile.setTopPixel(topPixel);
		tile.setLeftPixel(leftPixel);
	}
	
	/**
	 * Retrieves a given tile, if available.
	 * @param tileIndexI the index I of the tile to retrieve (row-wise)
	 * @param tileIndexJ the index J of the tile to retrieve (column-wise)
	 * @return the tile if found, or null otherwise
	 */
	public Tile getTile(int tileIndexI, int tileIndexJ) {
		return tiles[tileIndexI][tileIndexJ];
	}
	
	/**
	 * Retrieves all tiles for which the velocities components have been determined not to be stable,
	 * thus requiring further repetition of computations to retrieve the actual velocity.
	 * @return the tiles that are still unstable (the ones for which the velocity hasn't converged yet)
	 */
	public List<Tile> getUnstableVelocityTiles() {
		List<Tile> result = new ArrayList<Tile>(numberOfTilesInJ * numberOfTilesInJ);
		//FIXME It would be better to have a custom List or Collection of elements that can move efficiently from one Collection/List to another
		for (short indexI = 0; indexI < numberOfTilesInI; indexI++) {
			for (short indexJ = 0; indexJ < numberOfTilesInJ; indexJ++) {
				Tile tile = tiles[indexI][indexJ];
				if (tile.getStableState() == TileStableStateEnum.EVALUATING) {
					result.add(tile);
				}
			}
		}
		
		return result;
	}
	
	//TODO Method is not being called... may remove or refactor... since now tile has access to iteration step tiles.
	/**
	 * Updates the accumulated tile displacement, while also checking for displacement stabilization.
	 * @param tile the source tile that produced the respective displacements
	 * @param displacementDeltaU the displacement in U direction that was observed for the tile 
	 * @param displacementDeltaV the displacement in V direction that was observer for the tile
	 */
	public void updateDisplacement(Tile tile, float displacementDeltaU, float displacementDeltaV) {
		Tile localTile = tiles[tile.getTileIndexI()][tile.getTileIndexJ()];
		localTile.accumulateDisplacement(displacementDeltaU, displacementDeltaV);
		localTile.setStableState(stabilizationStrategy.computeStableState(this, tile, displacementDeltaU, displacementDeltaV));
	}

	/**
	 * This method will split the current tiles for the next iteration step format and new tiles will inherit displacements from the
	 * respective existing tiles.
	 * @return the new instance of iteration step tiles 
	 */
	public IterationStepTiles createTilesForNextIterationStep() {
		IterationStepTiles result = iaDivisionStrategy.createIterationStepTilesParameters(tilesOrder, this);
		
		return result;
	}
	
	/**
	 * Prepares the tiles for reuse with different images, while preserving the PIV tiling parameters across images.
	 * Current accumulated displacements are reset and displacements from previous adaptive step are inherited.
	 * Actual displacement inheritance algorithm is dependent on current Iteration Area Division Strategy in use.  
	 */
	public void reuseTiles() {
		currentStepRetries = 0;
		resetDisplacements();
		velocityInheritanceStrategy.reuseIterationStepTilesParameters(this);
		denseTiles = false;
	}

	/**
	 * Retrieves the tile that has the same indices as the reference tile. It is intended to be used
	 * when retrieving the two related image tiles for cross-correlation purposes.   
	 * @param refTile the reference tile from the which the related tile will be retrieved
	 * @return the related tile
	 */
	public Tile getRelatedTile(Tile refTile) {
		return tiles[refTile.getTileIndexI()][refTile.getTileIndexJ()];
	}
	
	/**
	 * Retrieves the tiles that have the same indices as the reference tiles and returns them in the same order.
	 * @param refTiles the reference tiles from which the related tiles will be retrieved
	 * @return the related tiles in the same order as the reference tiles
	 */
	public List<Tile> getRelatedTilesInSameOrder(List<Tile> refTiles) {
		List<Tile> result = new ArrayList<Tile>(refTiles.size());
		
		for (Tile refTile : refTiles) {
			result.add(tiles[refTile.getTileIndexI()][refTile.getTileIndexJ()]);
		}
		
		return result;
	}
	
	/**
	 * Increment the number of step retries counter.
	 */
	public void incrementCurrentStepRetries() {
		currentStepRetries++;
	}

	/**
	 * Updates displacement information for the associated tiles. 
	 * @param maxResults the list of maximum cross results with associated tiles
	 */
	public void updateDisplacementsFromMaxCrossResults(int frameNumber, List<MaxCrossResult> maxResults) {
		SingleFrameFloatMatlabExporter exporter = null;
		if (pivParameters.getAreaUnstableLoggingMode() == InterAreaUnstableLogEnum.LogAndDump) {
			//This can be done, because all tiles get Unstable at exactly the same iteration, thus only one file write is needed at the last repetition
			String filename = pivParameters.getAreaUnstableDumpFilenamePrefix() + "_AdpStep" + getCurrentStep() + ".mat";
			exporter = new SingleFrameFloatMatlabExporter();
			exporter.openFile(filename);
			exporter.setPIVContext();
		}
			
		for (MaxCrossResult maxResult : maxResults) {
			Tile refTile = maxResult.tileB;
			
			//Each tile can only displace at most getTileHeight - 1 pixels from center,
			//given that cross correlation for identical matrices pair is 2*getTileHeight() - 1.
			//Or for a 32x32 tile -> 63x63 cross-correlation with center at (31,31) and limits
			//(0,0) - (62,63).
			float displacementIncrementU = maxResult.getNthDisplacementU(0);
			float displacementIncrementV = maxResult.getNthDisplacementV(0);
			
			int tileIndexI = refTile.getTileIndexI();
			int tileIndexJ = refTile.getTileIndexJ();
			
			
			Tile tileB = tiles[tileIndexI][tileIndexJ];
			
			TileStableStateEnum newState = stabilizationStrategy.computeStableState(this, tileB, displacementIncrementU, displacementIncrementV);
			tileB.setStableState(newState);
			if (newState == TileStableStateEnum.UNSTABLE && pivParameters.getAreaUnstableLoggingMode() != InterAreaUnstableLogEnum.Ignore) {
				logger.warn("WARNING: Tile did not stabilizie within max. number of retries. Tile: {}", tileB.toString());
				if (pivParameters.getAreaUnstableLoggingMode() == InterAreaUnstableLogEnum.LogAndDump) {
					String structName = "tileAtI" + String.format("%05d", tileIndexI) + "J" + String.format("%05d", tileIndexJ);
					exporter.createStruct(structName);
					maxResult.tileA.dumpTileToExistingExporter(exporter, structName + ".tileA");
					tileB.dumpTileToExistingExporter(exporter, structName + ".tileB");
					maxResult.dumpMaxCrossResultToExistingExporter(exporter, structName + ".crossResult");
				}
			}
			if (maxResult.isAbsoluteNthDisplacement(0)) {
			    tileB.replaceDisplacement(displacementIncrementU, displacementIncrementV);
			} else {
			    tileB.accumulateDisplacement(displacementIncrementU, displacementIncrementV);
			} 
		}
		
		if (exporter != null) {
			exporter.closeFile();
		}
		
        if (pivParameters.getCrossCorrelationDumpMatcher() != null && pivParameters.getCrossCorrelationDumpMatcher().matches(this)) {
            PIVRunParameters pivRunParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
            StructMultiFrameFloatVelocityExporter crossExporter = pivRunParameters.getExporter();
            crossExporter.exportCrossDataToFile(frameNumber, maxResults, this, pivParameters.getCrossCorrelationDumpMatcher());
        }
	}	
	
	/**
	 * Resets the accumulated displacement of all tiles.
	 * Helper method for internal API.
	 */
	void resetDisplacements() {
		for (short indexI = 0; indexI < numberOfTilesInI; indexI++) {
			for (short indexJ = 0; indexJ < numberOfTilesInJ; indexJ++) {
				tiles[indexI][indexJ].resetDisplacements();
			}
		}
	}

	public int getMaxAdaptiveSteps() {
		return maxAdaptiveSteps;
	}

	void replaceAreadDivisionStrategy(AdaptiveInterAreaStrategyMixedSuperPosition adaptiveInterAreaStrategyMixedSuperPosition) {
		this.iaDivisionStrategy = adaptiveInterAreaStrategyMixedSuperPosition;
	}

	/**
	 * Get PIV image pair tile order.
	 * @return whether the step tiles refer to the first or the second image in a PIV image pair
	 */
    public TilesOrderEnum getTilesOrder() {
        return tilesOrder;
    }

    public boolean isDenseTiles() {
        return denseTiles;
    }

    public void setDenseTiles(boolean b) {
        denseTiles = true;
    }

    public void setUpdateDenseVectors(final int height, final int width, final float[] us, final float[] vs) {
        setDenseTiles(true);
        //
        denseWidth = (short)(width - marginLeft - marginRight);
        denseHeight = (short)(height - marginTop - marginBottom);
        //
        if (uBuffer == null) {
            uBuffer = new float[denseWidth * denseHeight];
        }
        //
        if (vBuffer == null) {
            vBuffer = new float[denseWidth * denseHeight];
        }
        //
        for (int subI = marginTop; subI < height - marginBottom; subI++) {
            for (int subJ = marginLeft; subJ < width - marginRight; subJ++) {
                int idxLocal = (subI - marginTop) * denseWidth + (subJ - marginLeft);
                int idxImg = subI * width + subJ;
                        
                uBuffer[idxLocal] = us[idxImg];
                vBuffer[idxLocal] = vs[idxImg];
            }
        }        
    }
    
    public float[] getUBuffer() {
        return uBuffer;
    }
    
    public float[] getVBuffer() {
        return vBuffer;
    }

    public short getDenseHeight() {
        return denseHeight;
    }
    
    public short getDenseWidth() {
        return denseWidth;
    }
}
