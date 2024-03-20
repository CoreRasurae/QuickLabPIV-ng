// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

public class AdaptiveInterVelocityInheritanceFileLogger implements IAdaptiveInterVelocityInheritanceLogger {
	private File file;
	FileOutputStream fos;
	private BufferedOutputStream bos;
	private List<TileMatcher> matchers = new LinkedList<TileMatcher>();
	
	
	public AdaptiveInterVelocityInheritanceFileLogger(String filename) {
		file = new File(filename);
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
		} catch (FileNotFoundException e) {
			throw new InvalidStateException("Couldn't open file for writing", e);
		}
	}
	
	public void addRelevantTile(TileMatcher relevantTile) {
		matchers.add(relevantTile);
	}
	
	@Override
	public boolean isToBeLogged(IterationStepTiles currentStepTiles, Tile currentTile, Tile parentTile) {
		for (TileMatcher matcher : matchers) {
			if (matcher.matches(currentTile, parentTile)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void logTileContribution(IterationStepTiles currentStepTiles, Tile currentTile, Tile parentTile,
			float weightU, float weightV) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("Tile that will inherit the vector: ");
		sb.append(currentTile);
		sb.append("\n");
		sb.append("Current tile displacement U: ");
		sb.append(currentTile.getDisplacementU());
		sb.append(", V: ");
		sb.append(currentTile.getDisplacementV());
		sb.append("\nTile that will contribute to the displacement: ");
		sb.append(parentTile);
		sb.append("\n");
		sb.append("Weight for the vector in U direction is: ");
		sb.append(weightU);
		sb.append(", weight for the vector in V direction is: ");
		sb.append(weightV);
		sb.append("\n");
		sb.append("Contribution U: ");
		sb.append(weightU * parentTile.getDisplacementU());
		sb.append(", V: ");
		sb.append(weightV * parentTile.getDisplacementV());
		sb.append("\n");
		try {
			fos.write(sb.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InvalidStateException("Couldn't log tile contibution to final displacement", e);
		} catch (IOException e) {
			throw new InvalidStateException("Couldn't log tile contibution to final displacement", e);
		}

	}

	@Override
	public void logCompletedForTile(IterationStepTiles currentStepTiles, Tile currentTile) {
		try {
			fos.write("FinalTile: ".getBytes("UTF-8"));
			fos.write(currentTile.toString().getBytes("UTF-8"));
			fos.write("\n----------------------------------------------------\n".getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new InvalidStateException("Couldn't log tile separator", e);
		} catch (IOException e) {
			throw new InvalidStateException("Couldn't log tile separator", e);
		}
	}
	
	public void close() {
		try {
			bos.flush();
			fos.flush();
			bos.close();
			fos.close();
		} catch (IOException e) {
			throw new InvalidStateException("Couldn't close log file: " + file.getName(), e);
		}
	}

}
