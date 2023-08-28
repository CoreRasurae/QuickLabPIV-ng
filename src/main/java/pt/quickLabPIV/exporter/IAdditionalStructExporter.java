// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.exporter;

import pt.quickLabPIV.Matrix;

public interface IAdditionalStructExporter {
	/**
	 * Creates a new MATLAB Structure field to be exported.
	 * @param structName the name of the MATLAB Structure to be created.
	 * @throws FieldAlreadyExistsException if a MATLAB Structure with the same name already exists.
	 */
	void createStruct(String structName) throws FieldAlreadyExistsException;
	
	/**
	 * Adds a field variable to an already existing MATLAB Structure.
	 * @param structName the name of the MATLAB structure to which the field variable should be appended.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if no MATLAB Structure with name structName was found, or if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addStructField(String structName, String fieldName, String fieldValue) throws ExportFailedException, FieldAlreadyExistsException;

	/**
	 * Adds a field variable to an already existing MATLAB Structure.
	 * @param structName the name of the MATLAB structure to which the field variable should be appended.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if no MATLAB Structure with name structName was found, or if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addStructField(String structName, String fieldName, int fieldValue) throws ExportFailedException, FieldAlreadyExistsException;
	
	/**
	 * Adds a field variable to an already existing MATLAB Structure.
	 * @param structName the name of the MATLAB structure to which the field variable should be appended.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if no MATLAB Structure with name structName was found, or if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addStructField(String structName, String fieldName, float fieldValue) throws ExportFailedException, FieldAlreadyExistsException;

	/**
	 * Adds a field variable to an already existing MATLAB Structure.
	 * @param structName the name of the MATLAB structure to which the field variable should be appended.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if no MATLAB Structure with name structName was found, or if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addStructField(String structName, String fieldName, Matrix fieldValue) throws ExportFailedException, FieldAlreadyExistsException;

	//Optional API allowing a previous added field value to be replaced, it should ensure that the replacing request has the same field type as the one
	//previously inserted.
	//void replaceStructField(String structName, String fieldName, String fieldValue) throws ExportFailedException, FieldAlreadyExistsException;
}
