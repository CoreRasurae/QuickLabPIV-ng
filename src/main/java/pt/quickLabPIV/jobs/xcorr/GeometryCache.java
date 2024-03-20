// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs.xcorr;

import java.util.concurrent.ConcurrentHashMap;

final class GeometryCache {
	private ConcurrentHashMap<Integer, LocalGeometry> geometries = new ConcurrentHashMap<Integer, LocalGeometry>();
	
	public LocalGeometry getGeometry(int inputSizeDimension) {
		return geometries.get(inputSizeDimension);
	}
	
	public void setGeometry(int inputSizeDimension, LocalGeometry geometry) {
		geometries.put(inputSizeDimension, geometry);
	}
}
