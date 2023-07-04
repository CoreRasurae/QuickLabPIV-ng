package pt.quickLabPIV.exporter;

import pt.quickLabPIV.Matrix;

public interface IAdditionalFieldsExporter {
	/**
	 * Adds a field variable to be exported at the root MATLAB structure level.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addField(String fieldName, String fieldValue) throws ExportFailedException, FieldAlreadyExistsException;

	/**
	 * Adds a field variable to be exported at the root MATLAB structure level.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addField(String fieldName, int fieldValue) throws ExportFailedException, FieldAlreadyExistsException;
	
	/**
	 * Adds a field variable to be exported at the root MATLAB structure level.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addField(String fieldName, float fieldValue) throws ExportFailedException, FieldAlreadyExistsException;
	
	/**
	 * Adds a field variable to be exported at the root MATLAB structure level.
	 * @param fieldName the name of the field variable to create.
	 * @param fieldValue the field value to be stored.
	 * @throws ExportFailedException if the field name is invalid, or otherwise a critical error occurred.
	 * @throws FieldAlreadyExistsException if a field variable already exists inside the MATLAB structure.
	 */
	void addField(String fieldName, Matrix matrixValue) throws ExportFailedException, FieldAlreadyExistsException;
}
