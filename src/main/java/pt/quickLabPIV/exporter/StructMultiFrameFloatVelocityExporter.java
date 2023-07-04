package pt.quickLabPIV.exporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVMap;
import pt.quickLabPIV.PIVMapOptionalConfiguration;
import pt.quickLabPIV.PIVResults;
import pt.quickLabPIV.Velocities;
import pt.quickLabPIV.iareas.ICrossCorrelationDumpMatcher;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class StructMultiFrameFloatVelocityExporter implements IVelocityExporterVisitor {
	private MatlabLevel5Header header = new MatlabLevel5Header();
	private Map<Integer, MatlabLevel5Struct> outputVelocityMatrixByHeightMap = new HashMap<Integer, MatlabLevel5Struct>();
	private Map<Integer, MatlabLevel5Struct> outputCrossMatrixByFrameMap = new HashMap<Integer, MatlabLevel5Struct>();
	private List<MatlabLevel5Element> dataElements = new ArrayList<MatlabLevel5Element>(5);
	private String filename;
	private FileOutputStream fos;
	private boolean fileIsOpen;
	private boolean markInvalidAsNaN = false;
	private boolean multiVolume = false;
	
	public StructMultiFrameFloatVelocityExporter() {
	    PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
	    Object optionalConfig = singleton.getPIVParameters().getSpecificConfiguration(MemoryCachedMATLABExporterOptionalConfiguration.IDENTIFIER);
	    if (optionalConfig != null) {
	        MemoryCachedMATLABExporterOptionalConfiguration optional = (MemoryCachedMATLABExporterOptionalConfiguration)optionalConfig;
	        markInvalidAsNaN = optional.isMarkInvalidAsNaN();
	    }
	}
	
	@Override
	public void openFile(String filename) {
		if (fileIsOpen) {
			throw new InvalidStateException("File has already been opened");
		}
		
		fos = null;
		try {
			fos = new FileOutputStream(filename, false);
		} catch (FileNotFoundException e) {
			throw new ExportFailedException("Failed to create file for writing");
		}
		
		fileIsOpen = true;
		
		dataElements.add(header);
		
		Date d = new Date();
		header.setTitle("MATLAB 5.0 - QuickLabPIV-ng Velocities exported on: " + d.toString());
		
		//Add data elements
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(header, "exportDate", d.toString());
		dataElements.add(element);
		
		this.filename = filename;

		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		PIVInputParameters pivParameters = singleton.getPIVParameters();
		
	    boolean markInvalidAsNaN = false;
        boolean swapUVOrder = false;
        
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(PIVMapOptionalConfiguration.IDENTIFIER);
        if (configurationObject != null) {
            PIVMapOptionalConfiguration optional = (PIVMapOptionalConfiguration)configurationObject;
            markInvalidAsNaN = optional.isMarkInvalidAsNaN();
            swapUVOrder = optional.isSwapUVOrder();
            multiVolume = optional.getMapsPerFile() < pivParameters.getNumberOfVelocityFrames();
        }

		int[] parametersDimensions = {1, 1}; //Struct dimensions 
		MatlabLevel5Struct parameters = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), parametersDimensions, "parameters");
		
		parameters.addFieldVariable("imageWidth", pivParameters.getImageWidthPixels());
		parameters.addFieldVariable("imageHeight", pivParameters.getImageHeightPixels());
		parameters.addFieldVariable("iaStartI", pivParameters.getInterrogationAreaStartIPixels());
		parameters.addFieldVariable("iaEndI", pivParameters.getInterrogationAreaEndIPixels());
		parameters.addFieldVariable("iaStartJ", pivParameters.getInterrogationAreaStartJPixels());
		parameters.addFieldVariable("iaEndJ", pivParameters.getInterrogationAreaEndJPixels());
		parameters.addFieldVariable("overlapFactor", pivParameters.getOverlapFactor());
        parameters.addFieldVariable("hasSwappedUVVectors", swapUVOrder ? 1 : 0);
        parameters.addFieldVariable("markInvalidAsNaN", markInvalidAsNaN ? 1 : 0);
        parameters.addFieldVariable("totalNumberOfVelocityMaps", pivParameters.getNumberOfVelocityFrames());
        parameters.addFieldVariable("multiVolumeExport", multiVolume ? 1 : 0);
        if (multiVolume && pivParameters.getNextFilename() != null) {
            parameters.addFieldVariable("multiVolumeLastVolume", 0);
            parameters.addFieldVariable("multiVolumeNextFilename", pivParameters.getNextFilename());
        } else {
            parameters.addFieldVariable("multiVolumeLastVolume", 1);
        }
		//matrix.addFieldVariable("file", filename);
		dataElements.add(parameters);
	}

	@Override
	public void closeFile() {
		dataElements.get(dataElements.size()-1).writeToOuputStream(fos);
		try {
		    if (fos != null) {
    			fos.flush();
    			fos.close();
		    }
			fos = null;
		} catch (IOException e) {
			throw new ExportFailedException("Failed to close file", e);
		} finally {
			fileIsOpen = false;
		}
	}

	@Override
	public void exportDataToFile(int frameNumber, IterationStepTiles iterTiles) {
		int tileHeight = iterTiles.getTileHeight();
		int tileWidth = iterTiles.getTileWidth();
		
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		PIVInputParameters pivParameters = singleton.getPIVParameters();
		
		MatlabLevel5Struct velocities = outputVelocityMatrixByHeightMap.get(tileHeight);
		if (velocities == null) {
			int[] dimensions = {1, 1};
			MatlabLevel5Struct matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "velocities" + tileHeight + "x" + tileWidth);
			matrix.addFieldVariable("iaHeight", tileHeight);
			matrix.addFieldVariable("iaWidth", tileWidth);
			matrix.addFieldVariable("adaptiveStep", iterTiles.getCurrentStep());
			dataElements.add(matrix);
			
			MatlabLevel5Struct margins = new MatlabLevel5Struct(null, Collections.emptyList(), dimensions, "margins");
			margins.addFieldVariable("top", iterTiles.getMarginTop());
			margins.addFieldVariable("left", iterTiles.getMarginLeft());
			margins.addFieldVariable("bottom", iterTiles.getMarginBottom());
			margins.addFieldVariable("right", iterTiles.getMarginRight());
			matrix.addFieldStructVariable("margins", margins);
			
			int[] dimensionsElements = {iterTiles.getNumberOfTilesInI(), iterTiles.getNumberOfTilesInJ(), pivParameters.getNumberOfVelocityFrames()};
			matrix.createMultiFrameFieldFloatArrayVariable("u", dimensionsElements);
			matrix.createMultiFrameFieldFloatArrayVariable("v", dimensionsElements);
			
			outputVelocityMatrixByHeightMap.put(tileHeight, matrix);
			
			velocities = matrix;
		}
	 
		MatlabLevel5UnnamedMatrix velocitiesU = (MatlabLevel5UnnamedMatrix)velocities.getField("u");
		MatlabLevel5UnnamedMatrix velocitiesV = (MatlabLevel5UnnamedMatrix)velocities.getField("v");
		
		MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterU = velocitiesU.createMultiFrameIndexContext(frameNumber);
		MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterV = velocitiesV.createMultiFrameIndexContext(frameNumber);
		for (int j = 0; j < iterTiles.getNumberOfTilesInJ(); j++) {
			for (int i = 0; i < iterTiles.getNumberOfTilesInI(); i++) {
				Tile tile = iterTiles.getTile(i, j);
				if (markInvalidAsNaN && (tile.isInvalidDisplacement() || tile.isMaskedDisplacement())) {
				    setterU.setValueAndIncrementIndex(Float.NaN);
				    setterV.setValueAndIncrementIndex(Float.NaN);
				} else {
                    setterU.setValueAndIncrementIndex(tile.getDisplacementU());
                    setterV.setValueAndIncrementIndex(tile.getDisplacementV());				    
				}
			}
		}
	}

	public void exportDataToFile(PIVResults results) {
	    if (results.isDenseMap()) {
	        exportDenseDataToFile(results);
	        return;
	    }
	    	    
		List<PIVMap> maps = results.getAllMaps();
		for (PIVMap map : maps) {
			int tileHeight = map.getIAHeight();
			int tileWidth = map.getIAWidth();

			int[] dimensions = {1, 1};
			MatlabLevel5Struct matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "velocities" + tileHeight + "x" + tileWidth);
			matrix.addFieldVariable("iaHeight", tileHeight);
			matrix.addFieldVariable("iaWidth", tileWidth);
			matrix.addFieldVariable("multiVolumeExport", multiVolume ? 1 : 0);
			if (multiVolume) {
			    matrix.addFieldVariable("multiVolumeStartOffset", maps.get(0).getAbsoluteFrameOffset());
			}
			//matrix.addFieldVariable("adaptiveStep", map.getCurrentStep());
			dataElements.add(matrix);
			
			MatlabLevel5Struct margins = new MatlabLevel5Struct(null, Collections.emptyList(), dimensions, "margins");
			margins.addFieldVariable("top", map.getMarginTop());
			margins.addFieldVariable("left", map.getMarginLeft());
			margins.addFieldVariable("bottom", map.getMarginBottom());
			margins.addFieldVariable("right", map.getMarginRight());
			matrix.addFieldStructVariable("margins", margins);
			
			int[] dimensionsElements = {map.getHeight(), map.getWidth(), map.getNumberOfMaps()};
			matrix.createMultiFrameFieldFloatArrayVariable("u", dimensionsElements);
			matrix.createMultiFrameFieldFloatArrayVariable("v", dimensionsElements);
			
			outputVelocityMatrixByHeightMap.put(tileHeight, matrix);
	
			//FIXME May need to handle discontiguous velocity Maps, by filling multiple structures for each partial result (Computation cancelled halfway)
			MatlabLevel5UnnamedMatrix velocitiesU = (MatlabLevel5UnnamedMatrix)matrix.getField("u");
			MatlabLevel5UnnamedMatrix velocitiesV = (MatlabLevel5UnnamedMatrix)matrix.getField("v");

			Iterator<Velocities> velocityIter = map.iterator();
			
			while (velocityIter.hasNext()) {
				Velocities velocityMap = velocityIter.next();
				MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterU = velocitiesU.createMultiFrameIndexContext(velocityMap.getFrameNumber());
				MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterV = velocitiesV.createMultiFrameIndexContext(velocityMap.getFrameNumber());
				for (int j = 0; j < map.getWidth(); j++) {
					for (int i = 0; i < map.getHeight(); i++) {			
						setterU.setValueAndIncrementIndex(velocityMap.getU()[i][j]);
						setterV.setValueAndIncrementIndex(velocityMap.getV()[i][j]);
					}
				}
			}
		}
	}

   public void exportDenseDataToFile(PIVResults results) {
        List<PIVMap> maps = results.getAllMaps();
        for (PIVMap map : maps) {
            int tileHeight = map.getIAHeight();
            int tileWidth = map.getIAWidth();

            int[] dimensions = {1, 1};
            MatlabLevel5Struct matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "velocities");
            matrix.addFieldVariable("iaHeight", tileHeight);
            matrix.addFieldVariable("iaWidth", tileWidth);
            matrix.addFieldVariable("multiVolumeExport", multiVolume ? 1 : 0);
            if (multiVolume) {
                matrix.addFieldVariable("multiVolumeStartOffset", maps.get(0).getAbsoluteFrameOffset());
            }
            //matrix.addFieldVariable("adaptiveStep", map.getCurrentStep());
            dataElements.add(matrix);
            
            MatlabLevel5Struct margins = new MatlabLevel5Struct(null, Collections.emptyList(), dimensions, "margins");
            margins.addFieldVariable("top", map.getMarginTop());
            margins.addFieldVariable("left", map.getMarginLeft());
            margins.addFieldVariable("bottom", map.getMarginBottom());
            margins.addFieldVariable("right", map.getMarginRight());
            matrix.addFieldStructVariable("margins", margins);
            
            final int vectorsHeight = map.getImageHeight() - map.getMarginTop() - map.getMarginBottom();
            final int vectorsWidth  = map.getImageWidth()  - map.getMarginLeft() - map.getMarginRight();
            
            int[] dimensionsElements = {vectorsHeight, vectorsWidth, map.getNumberOfMaps()};
            matrix.createMultiFrameFieldFloatArrayVariable("u", dimensionsElements);
            matrix.createMultiFrameFieldFloatArrayVariable("v", dimensionsElements);
            
            outputVelocityMatrixByHeightMap.put(tileHeight, matrix);
    
            //FIXME May need to handle uncontiguous velocity Maps, by filling multiple structures for each partial result (Computation cancelled halfway)
            MatlabLevel5UnnamedMatrix velocitiesU = (MatlabLevel5UnnamedMatrix)matrix.getField("u");
            MatlabLevel5UnnamedMatrix velocitiesV = (MatlabLevel5UnnamedMatrix)matrix.getField("v");

            Iterator<Velocities> velocityIter = map.iterator();
            
            while (velocityIter.hasNext()) {
                Velocities velocityMap = velocityIter.next();
                MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterU = velocitiesU.createMultiFrameIndexContext(velocityMap.getFrameNumber());
                MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setterV = velocitiesV.createMultiFrameIndexContext(velocityMap.getFrameNumber());
                for (int j = 0; j < vectorsWidth; j++) {
                    for (int i = 0; i < vectorsHeight; i++) {         
                        setterU.setValueAndIncrementIndex(velocityMap.getU()[i][j]);
                        setterV.setValueAndIncrementIndex(velocityMap.getV()[i][j]);
                    }
                }
            }
        }
    }

	@Override
	public boolean isMultiFrameSupported() {
		return true;
	}

    public synchronized void exportCrossDataToFile(int frameNumber, List<MaxCrossResult> maxResults, IterationStepTiles stepTiles, ICrossCorrelationDumpMatcher matcher) {
        //FIXME Enable exporter with SPARSE matrices support and create an Exporter that operates on File as not to overload RAM memory
        int[] dimensions = {1, 1};
        
        MatlabLevel5Struct matrix = outputCrossMatrixByFrameMap.get(frameNumber);
        if (matrix == null) {
            matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, 
                    "crossCorrelation_" + String.format("frame%05d", frameNumber));
            dataElements.add(matrix);
            outputCrossMatrixByFrameMap.put(frameNumber, matrix);
        }
       
        String stepStructName = String.format("step%d", stepTiles.getCurrentStep());
        MatlabLevel5Struct stepStruct = (MatlabLevel5Struct)matrix.getField(stepStructName);
        if (stepStruct ==  null) {
            //When stabilization iterations exist, this matrix must be reused/updated with last cross correlation, instead of trying to create a new one,
            //operation that will fail.
            stepStruct = new MatlabLevel5Struct(null, Collections.emptyList(), dimensions, stepStructName);        
            stepStruct.addFieldVariable("step", stepTiles.getCurrentStep());        
            matrix.addFieldStructVariable(stepStructName, stepStruct);
            int[] dimensionsElements = {stepTiles.getTileHeight()*2-1, stepTiles.getTileWidth()*2-1, stepTiles.getNumberOfTilesInI(), stepTiles.getNumberOfTilesInJ()};
            stepStruct.createMultiFrameFieldFloatArrayVariable("crossCorrelations", dimensionsElements);
        }
        MatlabLevel5UnnamedMatrix crossCorrelations = (MatlabLevel5UnnamedMatrix)stepStruct.getField("crossCorrelations");       
        for (MaxCrossResult maxResult : maxResults) {
            MatlabLevel5UnnamedMatrix.MatlabLevel5ContextSetter setter = crossCorrelations.createMultiDimensionalIndexContext(new int[] {maxResult.tileA.getTileIndexI(), maxResult.tileA.getTileIndexJ()});
            Matrix crossMatrix = maxResult.getCrossMatrix();
            for (int j = 0; j < crossMatrix.getWidth(); j++) {
                for (int i = 0; i < crossMatrix.getHeight(); i++) {
                    setter.setValueAndIncrementIndex(crossMatrix.getElement(i, j));
                }
            }
        }
    }

}
