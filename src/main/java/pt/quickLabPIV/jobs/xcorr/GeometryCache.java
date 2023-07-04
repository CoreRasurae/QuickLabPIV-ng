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
