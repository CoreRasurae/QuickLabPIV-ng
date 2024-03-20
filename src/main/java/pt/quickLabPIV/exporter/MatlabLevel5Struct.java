// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import pt.quickLabPIV.Matrix;

public class MatlabLevel5Struct extends MatlabLevel5Matrix {
	//Fields Inner Elements
	private HashMap<String,MatlabLevel5Element> fieldsMap = new HashMap<String, MatlabLevel5Element>();
	private List<String> orderedFieldNames = new LinkedList<String>();
		
	/**
	 * Creates a new Matlab Level 5 Struct.
	 * @param parent the parent Matlab Data Element
	 * @param newArrayFlags the struct flags
	 * @param dimensions the array with the struct dimensions 
	 * @param newArrayName the name of the struct to create
	 * @throws IllegalArgumentException if invalid parameters are detected.
	 */
	MatlabLevel5Struct(MatlabLevel5Element parent, 
			Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName) {
		super(parent, MatlabMxTypesEnum.mxSTRUCT_CLASS, newArrayFlags, dimensions, newArrayName);		
	}

	MatlabLevel5Element getField(String name) {
		return fieldsMap.get(name);
	}
	
	@Override
	long computeUpdatedElementNumberOfBytes() {
		long bytes = super.computeUpdatedElementNumberOfBytes();
		for (MatlabLevel5Element field : fieldsMap.values()) {
			bytes += field.getBytesLength();
		}
		
		return bytes;
	}

	@Override
	protected void prepareForWriting() {
		setFieldNames(orderedFieldNames.toArray(new String[orderedFieldNames.size()]));
	}
	
	@Override
	public void writeToOuputStream(FileOutputStream fos) {
		for (String fieldName : orderedFieldNames) {
			if (fieldName.length() > 31) {
				throw new ExportFailedException("Field name: " + fieldName + " is longer than 31 characters.");
			}
			fieldsMap.get(fieldName).prepareForWriting();
		}
		setFieldNames(orderedFieldNames.toArray(new String[orderedFieldNames.size()]));
				
		super.writeToOuputStream(fos);
		//Ensure elements are written the order they were created
		for (String fieldName : orderedFieldNames) {
			MatlabLevel5Element field = fieldsMap.get(fieldName);
			field.writeToOuputStream(fos);
		}
	}
	
	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldVariable(String name, float value) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}

		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedVariable(null, value);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);
	}

	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldVariable(String name, int value) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}
		
		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedVariable(null, value);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);
	}
	
	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldVariable(String name, short value) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}
		
		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedVariable(null, value);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);
	}

	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldVariable(String name, byte value) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}
		
		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedVariable(null, value);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);
	}

	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldVariable(String name, String text) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}
		
		if (fieldsMap.containsKey(name)) {
			throw new ExportFailedException("Field with name: \"" + name + "\" already exists");
		}
 		
		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedVariable(null, text);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);
	}

	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldFloatArrayVariable(String name, Matrix m) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}

		if (fieldsMap.containsKey(name)) {
			throw new ExportFailedException("Field with name: \"" + name + "\" already exists");
		}

		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedFloatArrayVariable(null, m);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);		
	}
	
	/**
	 * Adds a field to the MATLAB Level 5 Struct matrix.
	 * @param name the name of the field to add (max. 31 characters)
	 * @param value the value to store
	 */
	public void addFieldUnsignedByteArrayVariable(String name, Matrix m) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}

		if (fieldsMap.containsKey(name)) {
			throw new ExportFailedException("Field with name: \"" + name + "\" already exists");
		}

		MatlabLevel5UnnamedMatrix field = MatlabLevel5UnnamedMatrix.createUnnamedUnsingByteArrayVariable(null, m);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);		
	}

	/**
	 * Creates and inserts a new named multi-frame field to the MATLAB Level 5 Struct. 
	 * @param name the name of the field to add (max. 31 characters)
	 * @param multiFrameDimensions the dimensions of the muti-frame array field.
	 */
	public void createMultiFrameFieldFloatArrayVariable(String name, int[] multiFrameDimensions) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}

		if (fieldsMap.containsKey(name)) {
			throw new ExportFailedException("Field with name: \"" + name + "\" already exists");
		}

		MatlabLevel5UnnamedMatrix field = new MatlabLevel5UnnamedMatrix(null, MatlabMxTypesEnum.mxSINGLE_CLASS, Collections.emptyList(), multiFrameDimensions);
		fieldsMap.put(name, field);
		orderedFieldNames.add(name);		
	}
	
	public void addFieldStructVariable(String name, MatlabLevel5Struct struct) {
		if (name.length() > 31) {
			throw new IllegalArgumentException("Field name: " + name + " is longer than 31 characters.");
		}

		if (fieldsMap.containsKey(name)) {
			throw new ExportFailedException("Field with name: \"" + name + "\" already exists");
		}

		fieldsMap.put(name, struct);
		orderedFieldNames.add(name);
	}
}
