// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.iareas;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.ClippingModeEnum;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.exporter.IAdditionalStructExporter;
import pt.quickLabPIV.exporter.SingleFrameFloatMatlabExporter;


public final class Tile {
	private static Logger logger = LoggerFactory.getLogger(Tile.class);
	
	private final PIVContextSingleton pivContext = PIVContextSingleton.getSingleton();
	private IterationStepTiles parentStep;
	private short tileIndexI;
	private short tileIndexJ;
	
	private short topPixel;
	private short leftPixel;
	
	private float displacementU;
	private float displacementV;

    private float backupDisplacementU;
    private float backupDisplacementV;
	
	private boolean invalidDisplacement;
	private boolean lockedInvalidationState;
	private boolean maskedDisplacement;	
	
	/**
	 * Indicates if velocity components U and V are stable
	 * <ul><li>Unknown, tile needs still to be recomputed</li>
	 * <li>Stable, if last displacement increment was too small according to the current strategy.</li>
	 * <li>Unstable, tile did not stabilize within max retries.</li>
	 */
	private TileStableStateEnum stable;
	
	//Image matrix containing the Tile pixel values
	private Matrix matrix;
	
	public Tile(IterationStepTiles parentStep) {
		this.parentStep = parentStep;
		displacementU = 0.0f;
		displacementV = 0.0f;
		invalidDisplacement = true;
		maskedDisplacement = false;
		stable = TileStableStateEnum.EVALUATING;
	}
	
	public Tile(IterationStepTiles parentStep, Matrix m) {
		matrix = m;
		this.parentStep = parentStep;
		displacementU = 0.0f;
		displacementV = 0.0f;
	    invalidDisplacement = true;
	    maskedDisplacement = false;
		stable = TileStableStateEnum.EVALUATING;
	}
	
	/**
	 * Resets the accumulated tile displacement
	 */
	void resetDisplacements() {
		displacementU = 0.0f;
		displacementV = 0.0f;
		stable = TileStableStateEnum.EVALUATING;
	    invalidDisplacement = true;
	    maskedDisplacement = false;
	    lockedInvalidationState = false;
	}
	
	void setStableState(TileStableStateEnum newStableState) {
		stable = newStableState;
	}
	
	void setTileIndexI(short newIndexI) {
		tileIndexI = newIndexI;
	}
	
	void setTileIndexJ(short newIndexJ) {
		tileIndexJ = newIndexJ;
	}
	
	void setTopPixel(short newTopPixel) {
		topPixel = newTopPixel;
	}
	
	void setLeftPixel(short newLeftPixel) {
		leftPixel = newLeftPixel;
	}
	
	void accumulateDisplacement(float displacementIncrementU, float displacementIncrementV) {
		float roundedDisplacementU = (float)FastMath.round(displacementU + displacementIncrementU);
		float roundedDisplacementV = (float)FastMath.round(displacementV + displacementIncrementV);

		int imageHeight = pivContext.getPIVParameters().getImageHeightPixels();
		int imageWidth = pivContext.getPIVParameters().getImageWidthPixels();
	
		ClippingModeEnum clippingMode = pivContext.getPIVParameters().getClippingMode();
		
		boolean allowed = false;
		if (clippingMode != ClippingModeEnum.AllowedOutOfBoundClipping && (topPixel + roundedDisplacementU < 0 ||
			topPixel + parentStep.getTileHeight() + roundedDisplacementU >= imageHeight ||
			leftPixel + roundedDisplacementV < 0 ||
			leftPixel + parentStep.getTileWidth() + roundedDisplacementV >= imageWidth)) {
			
			if (clippingMode != ClippingModeEnum.NoOutOfBoundClipping) {
				allowed = true;
			} else {
				logger.error("Ignoring displacement increment for tile I: {}, J: {} of U: {}, V: {}",
				        tileIndexI, tileIndexJ, displacementIncrementU, displacementIncrementV);
				logger.error("Accumulated tile I: {}, J: {} of U: {}, V: {}",
				        tileIndexI, tileIndexJ, (displacementU + displacementIncrementU), (displacementV + displacementIncrementV));
				displacementIncrementU = 0.0f;
				displacementIncrementV = 0.0f;
			}
		} else {
			allowed = true;
		}
		
		if (allowed) {
			displacementU += displacementIncrementU;
			displacementV += displacementIncrementV;
		}
	}
	
	/**
	 * Backup current accumulated displacement, so that it can be restored at a later step.
	 * Useful for validation purposes, where vector displacements can be updated and reverted multiple times.
	 */
	public void backupDisplacement() {
	    backupDisplacementU = displacementU;
	    backupDisplacementV = displacementV;
	}

   /**
     * Restore previously, backed-up displacement.
     * Useful for validation purposes, where vector displacements can be updated and reverted multiple times.
     */
	public void restoreDisplacement() {
	    displacementU = backupDisplacementU;
	    displacementV = backupDisplacementV;
	}
	
    public boolean replaceDisplacement(float uU, float uV) {
        float roundedDisplacementU = (float)FastMath.round(uU);
        float roundedDisplacementV = (float)FastMath.round(uV);        
        
        int imageHeight = pivContext.getPIVParameters().getImageHeightPixels();
        int imageWidth  = pivContext.getPIVParameters().getImageWidthPixels();
    
        ClippingModeEnum clippingMode = pivContext.getPIVParameters().getClippingMode();
        
        boolean allowed = false;
        if (clippingMode != ClippingModeEnum.AllowedOutOfBoundClipping && (topPixel + roundedDisplacementU < 0 ||
            topPixel + parentStep.getTileHeight() + roundedDisplacementU >= imageHeight ||
            leftPixel + roundedDisplacementV < 0 ||
            leftPixel + parentStep.getTileWidth() + roundedDisplacementV >= imageWidth)) {
            
            if (clippingMode != ClippingModeEnum.NoOutOfBoundClipping) {
                allowed = true;
            } else {
                logger.error("Clipping will occur with displacement for tile I: {}, J: {} of U: {}, V: {}",
                        tileIndexI, tileIndexJ, uU, uV);
                logger.error("Tile I: {}, J: {} of U: {}, V: {}",
                        tileIndexI, tileIndexJ, uU, uV);
            }
        } else {
            allowed = true;
        }
        
        if (allowed) {
            displacementU = uU;
            displacementV = uV;
            return true;
        }
        return false;
    }
	
	/**
	 * Indicates if the vector displacement in this tile was deemed invalid by a validation method
	 * @return <ul><li>true, if vector is considered invalid</li>
	 *             <li>false, if vector is considered Valid</li></ul> 
	 */
	public boolean isInvalidDisplacement() {
	    return invalidDisplacement;
	}
	
	/**
	 * Updates the validation state of the vector in this tile. To be used by the validation methods.
	 * @param state the new vector validation state
	 */
	public void setInvalidDisplacement(boolean state) {
	    if (lockedInvalidationState) {
	        return;
	    }
	    invalidDisplacement = state;
	}
	
	public void setLockedValidationState(boolean state) {
	    lockedInvalidationState = state;
	}
	
	public boolean isLockedValidationState() {
	    return lockedInvalidationState;
	}
	
	/**
	 * Indicates if the vector displacement in this tile was deemed as masked, according to the image mask used.
	 * Rejected vectors are not eligible for vector substitution, and should be set to 0.
	 * @return <ul><li>true, if vector is considered rejected</li>
	 *             <li>false, if vector is considered Not rejected</li></ul>
	 */
	public boolean isMaskedDisplacement() {
	    return maskedDisplacement;
	}
	
	/**
	 * Updates the masked state of the vector in this tile, according to the image mask used. To be used by the validation methods. 
	 * @param state the new vector rejected state
	 */
	public void setMaskedDisplacement(boolean state) {
	    maskedDisplacement = state;
	}
	
	public Matrix getMatrix() {
		return matrix;
	}
	
	public void setMatrix(Matrix m) {
	    matrix = m;
	}
	
	public TileStableStateEnum getStableState() {
		return stable;
	}
	
	public short getTopPixel() {
		return topPixel;
	}
	
	public short getLeftPixel() {
		return leftPixel;
	}
	
	public float getDisplacementU() {
		return displacementU;
	}
	
	public float getDisplacementV() {
		return displacementV;
	}
	
	public short getTileIndexI() {
		return tileIndexI;
	}
	
	public short getTileIndexJ() {
		return tileIndexJ;
	}
	
	public float getDisplacedTileTop() {
		return topPixel + displacementU;
	}
	
	public float getDisplacedTileLeft() {
		return leftPixel + displacementV;
	}
	
	public float getDisplacedTileCenterV() {
	    return getDisplacedTileTop() + parentStep.getTileHeight()/2 - 0.5f;
	}
	
	public float getDisplacedTileCenterH() {
	    return getDisplacedTileLeft() + parentStep.getTileWidth()/2 - 0.5f;
	}
	
	public void dumpTileToExistingExporter(IAdditionalStructExporter exporter, String tileStructPathName) {
		exporter.createStruct(tileStructPathName);
		exporter.addStructField(tileStructPathName, "AdaptiveLevel", parentStep.getCurrentStep());
		exporter.addStructField(tileStructPathName, "NumberOfAdaptiveLevels", parentStep.getMaxAdaptiveSteps());
		exporter.addStructField(tileStructPathName, "Retries", parentStep.getCurrentStepRetries());
		exporter.addStructField(tileStructPathName, "I", tileIndexI);
		exporter.addStructField(tileStructPathName, "J", tileIndexJ);
		exporter.addStructField(tileStructPathName, "State", stable.toString());
		exporter.addStructField(tileStructPathName, "topPixel", topPixel);
		exporter.addStructField(tileStructPathName, "leftPixel", leftPixel);
		exporter.addStructField(tileStructPathName, "displacementU", displacementU);
		exporter.addStructField(tileStructPathName, "displacementV", displacementV);
		exporter.addStructField(tileStructPathName, "matrix", matrix);
	}
	
	public void dumpTile(String filenamePrefix) {
		SingleFrameFloatMatlabExporter exporter = new SingleFrameFloatMatlabExporter();
		String filename = filenamePrefix + "_AdpStep" + parentStep.getCurrentStep() + "_I" + (int)tileIndexI + "_J" + (int)tileIndexJ + ".mat";
		exporter.openFile(filename);
		exporter.setPIVContext();
		exporter.createStruct("Info");
		exporter.addStructField("Info", "AdaptiveLevel", parentStep.getCurrentStep());
		exporter.addStructField("Info", "NumberOfAdaptiveLevels", parentStep.getMaxAdaptiveSteps());
		exporter.addStructField("Info", "Retries", parentStep.getCurrentStepRetries());
		exporter.addStructField("Info", "I", tileIndexI);
		exporter.addStructField("Info", "J", tileIndexJ);
		exporter.addStructField("Info", "State", stable.toString());
		exporter.addStructField("Info", "topPixel", topPixel);
		exporter.addStructField("Info", "leftPixel", leftPixel);
		exporter.addStructField("Info", "displacementU", displacementU);
		exporter.addStructField("Info", "displacementV", displacementV);
		exporter.exportDataToFile(matrix);
		exporter.closeFile();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		
		sb.append("Tile indices (I=");
		sb.append(tileIndexI);
		sb.append(", J=");
		sb.append(tileIndexJ);
		sb.append("), at pixel (Top=");
		sb.append(topPixel);
		sb.append(", Left=");
		sb.append(leftPixel);
		sb.append(")");
		if (parentStep != null) {
			sb.append(", Center (U=");
			sb.append(topPixel + parentStep.getTileHeight()/2.0f - 0.5f);
			sb.append(", V=");
			sb.append(leftPixel + parentStep.getTileWidth()/2.0f - 0.5f);
			sb.append(")");
		};
		sb.append(", velocities (U=");
		sb.append(displacementU);
		sb.append(",V=");
		sb.append(displacementV);
		sb.append(")");
		
		return sb.toString();
	}

	public IterationStepTiles getParentIterationStepTiles() {
		return parentStep;
	}
}
