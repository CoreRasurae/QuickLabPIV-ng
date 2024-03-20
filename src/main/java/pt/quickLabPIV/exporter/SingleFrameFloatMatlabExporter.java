// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.quickLabPIV.Matrix;

/**
 * This class provides export functionality for a single 2D Matrix as a Float array in MATLAB Level 5 file format.
 * Additionally this class allows to export additional fields of individual variables or single level structures. 
 * @author lpnm
 */
public class SingleFrameFloatMatlabExporter implements IMatrixExporterVisitor, IAdditionalSimpleAndStructFieldsExporter {
	private String filename;
	private FileOutputStream fos;
	private MatlabLevel5Header header = new MatlabLevel5Header();
	private MatlabLevel5MatrixFloat matrix;
	private List<MatlabLevel5Element> dataElements = new ArrayList<MatlabLevel5Element>(5);
	private Map<String, MatlabLevel5Element> dataElementsMap = new HashMap<String, MatlabLevel5Element>(); 
	
	public SingleFrameFloatMatlabExporter() {
		
	}

	/**
	 * Helper method to navigate through a dot separated struct path
	 * @param path the path to resolve
	 * @return the resolved struct object
	 */
	protected MatlabLevel5Struct pathResolver(String path) {
		String pathElements[] = path.trim().split("\\.");
		if (pathElements.length == 0) {
			pathElements = new String[1];
			pathElements[0] = path;
		}

		MatlabLevel5Struct struct = null;
		for (String pathElement : pathElements) {
			if (struct == null) {
				struct =  (MatlabLevel5Struct)dataElementsMap.get(pathElement.trim());
				if (struct == null) {
					throw new ExportFailedException("No Struct with name: \"" + pathElement + "\" was found for path: " + path);
				}
			} else {
				struct = (MatlabLevel5Struct)struct.getField(pathElement.trim());
				if (struct == null) {
					throw new ExportFailedException("No Struct with name: \"" + pathElement + "\" was found for path: " + path);
				}
			}
		}
		
		return struct;
	}

	/**
	 * Helper method to create a new struct element as the last node of the path
	 * @param path the path to resolve
	 * @return the resolved struct object
	 */
	protected MatlabLevel5Struct pathResolverCreateStruct(String path) {
		String pathElements[] = path.trim().split("\\.");
		if (pathElements.length == 0) {
			pathElements = new String[1];
			pathElements[0] = path;
		}
		MatlabLevel5Struct struct = null;
		for (int pathIndex = 0; pathIndex < pathElements.length - 1; pathIndex++) {
			String pathElement = pathElements[pathIndex].trim();
			if (struct == null) {
				struct = (MatlabLevel5Struct)dataElementsMap.get(pathElement.trim());
				if (struct == null) {
					throw new ExportFailedException("No Struct with name: \"" + pathElement + "\" was found for path: " + path);
				}
			} else {
				struct = (MatlabLevel5Struct)struct.getField(pathElement.trim());
				if (struct == null) {
					throw new ExportFailedException("No Struct with name: \"" + pathElement + "\" was found for path: " + path);
				}
			}
		}
		
		int[] dimensions = { 1, 1 };
		
		String structName = pathElements[pathElements.length - 1];
		MatlabLevel5Element parent = null;
		if (struct == null) {
			//Only root elements have to be chained, otherwise export will fail...
			parent = dataElements.get(dataElements.size()-1);
		}
		
		MatlabLevel5Struct element = new MatlabLevel5Struct(parent, Collections.emptyList(), dimensions, structName);

		if (struct == null) {
			//Check if simple variable already exists
			if (dataElementsMap.containsKey(structName)) {
				throw new FieldAlreadyExistsException("A field with the same name already exists");
			}

			dataElementsMap.put(structName, element);
			dataElements.add(element);			
		} else {
			struct.addFieldStructVariable(structName, element);
		}
		
		return element;
	}

	@Override
	public void setPIVContext() {
		dataElements.add(header);
				
		Date d = new Date();
		header.setTitle("MATLAB 5.0 - ViPIVIST-ng Float Matrix exported on: " + d.toString());
		
		//Add data elements
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(header, "exportDate", d.toString());
		dataElementsMap.put("exportDate", element);
		dataElements.add(element);
	}

	@Override
	public void openFile(String filename) {
		this.filename = filename;
		
		fos = null;
		try {
			fos = new FileOutputStream(filename, false);
		} catch (FileNotFoundException e) {
			throw new ExportFailedException("Failed to open file for data export: " + filename, e);
		}
	}

	@Override
	public void closeFile() {
		if (dataElements.size() > 0) {			
			dataElements.get(dataElements.size() - 1).writeToOuputStream(fos);
		}
		
		try {
			fos.flush();
			fos.close();
		} catch (IOException e) {
			throw new ExportFailedException("Failed to close file", e);
		}
	}
	
	@Override
	public void exportDataToFile(Matrix m) {
		int dimI = m.getHeight();
		int dimJ = m.getWidth();
		
		int[] dimensions = { dimI, dimJ };
		
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), "dimI", dimI);
		dataElementsMap.put("dimI", element);
		dataElements.add(element);
		element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), "dimJ", dimJ);
		dataElementsMap.put("dimJ", element);
		dataElements.add(element);
		element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), "file", filename);
		dataElementsMap.put("file", element);
		dataElements.add(element);
		
		matrix = new MatlabLevel5MatrixFloat(dataElements.get(dataElements.size()-1), dimensions, "matrix");
		dataElementsMap.put("mat", element);
		dataElements.add(matrix);
		
		matrix.writeMatrix(m);
	}
	
	@Override
	public boolean isMultiFrameSupported() {
		return false;
	}

	@Override
	public void addField(String fieldName, String fieldValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
		
		//Check if simple variable already exists
		if (dataElementsMap.containsKey(fieldName)) {
			throw new FieldAlreadyExistsException("A field with the same name already exists");
		}
		
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), fieldName, fieldValue);
		dataElementsMap.put(fieldName, element);
		dataElements.add(element);		
	}

	@Override
	public void addField(String fieldName, int fieldValue) throws ExportFailedException, FieldAlreadyExistsException {
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
		
		//Check if simple variable already exists
		if (dataElementsMap.containsKey(fieldName)) {
			throw new FieldAlreadyExistsException("A field with the same name already exists");
		}

		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), fieldName, fieldValue);
		dataElementsMap.put(fieldName, element);
		dataElements.add(element);		
	}

	@Override
	public void addField(String fieldName, Matrix matrixValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
		
		//Check if simple variable already exists
		if (dataElementsMap.containsKey(fieldName)) {
			throw new FieldAlreadyExistsException("A field with the same name already exists");
		}

		int[] dimensions = {matrixValue.getHeight(), matrixValue.getWidth()};
		
		MatlabLevel5MatrixFloat matrix = new MatlabLevel5MatrixFloat(dataElements.get(dataElements.size()-1), dimensions, "matrix");
		matrix.writeMatrix(matrixValue);
		dataElementsMap.put(fieldName, matrix);
		dataElements.add(matrix);		
	}

	@Override
	public void addField(String fieldName, float fieldValue) throws ExportFailedException, FieldAlreadyExistsException {
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
		
		//Check if simple variable already exists
		if (dataElementsMap.containsKey(fieldName)) {
			throw new FieldAlreadyExistsException("A field with the same name already exists");
		}
		
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(dataElements.get(dataElements.size()-1), fieldName, fieldValue);
		dataElementsMap.put(fieldName, element);
		dataElements.add(element);		
	}

	@Override
	public void createStruct(String structName) throws FieldAlreadyExistsException {
		if (structName == null || structName.trim().isEmpty()) {
			throw new ExportFailedException("Struct name cannot be empty");
		}
		
		pathResolverCreateStruct(structName);
	}

	@Override
	public void addStructField(String structName, String fieldName, String fieldValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (structName == null || structName.trim().isEmpty()) {
			throw new ExportFailedException("Struct name cannot be empty");
		}
		
		MatlabLevel5Struct structElement = pathResolver(structName);
		
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
				
		structElement.addFieldVariable(fieldName, fieldValue);
	}

	@Override
	public void addStructField(String structName, String fieldName, int fieldValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (structName == null || structName.trim().isEmpty()) {
			throw new ExportFailedException("Struct name cannot be empty");
		}

		MatlabLevel5Struct structElement = pathResolver(structName);
		
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
				
		structElement.addFieldVariable(fieldName, fieldValue);
	}

	@Override
	public void addStructField(String structName, String fieldName, float fieldValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (structName == null || structName.trim().isEmpty()) {
			throw new ExportFailedException("Struct name cannot be empty");
		}
		
		MatlabLevel5Struct structElement = pathResolver(structName);
		
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
				
		structElement.addFieldVariable(fieldName, fieldValue);
	}

	@Override
	public void addStructField(String structName, String fieldName, Matrix fieldValue)
			throws ExportFailedException, FieldAlreadyExistsException {
		if (structName == null || structName.trim().isEmpty()) {
			throw new ExportFailedException("Struct name cannot be empty");
		}
		
		MatlabLevel5Struct structElement = pathResolver(structName);
		
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new ExportFailedException("Field name cannot be empty");
		}
				
		structElement.addFieldFloatArrayVariable(fieldName, fieldValue);
	}
}
