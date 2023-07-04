package pt.quickLabPIV.exporter;

import pt.quickLabPIV.Matrix;

/**
 * Defines the interface that a Matrix exporter needs to follow.
 * 
 * @author lpnm
 */
public interface IMatrixExporterVisitor {
	
	/**
	 * Inform the exporter about the PIV context under which the data is being expeorted.
	 * Can be used by the exporter to contextualize the exported data. 
	 */
	void setPIVContext();
	
	/**
	 * Open and create a file for data export.
	 * @param filename the name of the file to create
	 */
	void openFile(String filename);
	
	/**
	 * Closes the file after data is exported.
	 * Possibly finalizes writing of some data that can only be determined at the end.
	 */
	void closeFile();
	
	/**
	 * Export data from the matrix m to the output file
	 * @param m the matrix to export.
	 */
	void exportDataToFile(Matrix m);
	
	/**
	 * Identifies if the exporter supports multi-frame data/multi-matrix or is single frame/single matrix only. 
	 * @return <ul><li>true, if multi frame export is supported</li>
	 * 				<li>false, otherwise</li></ul>
	 */
	boolean isMultiFrameSupported();
	
}
