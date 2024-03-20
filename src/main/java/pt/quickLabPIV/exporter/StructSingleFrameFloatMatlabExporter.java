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
import java.util.List;

import pt.quickLabPIV.Matrix;

public class StructSingleFrameFloatMatlabExporter implements IMatrixExporterVisitor {
	private String filename;
	private FileOutputStream fos;
	private MatlabLevel5Header header = new MatlabLevel5Header();
	private MatlabLevel5Struct matrix;
	private List<MatlabLevel5Element> dataElements = new ArrayList<MatlabLevel5Element>(5);
	
	public StructSingleFrameFloatMatlabExporter() {
		
	}

	@Override
	public void setPIVContext() {
		dataElements.add(header);
		
		Date d = new Date();
		header.setTitle("MATLAB 5.0 - ViPIVIST-ng Float Matrix exported on: " + d.toString());
		
		//Add data elements
		MatlabLevel5SimpleVariable element = MatlabLevel5SimpleVariable.createNamedVariable(header, "exportDate", d.toString());
		dataElements.add(element);
	}

	@Override
	public void openFile(String filename) {
		this.filename = filename;
		
		fos = null;
		try {
			fos = new FileOutputStream(filename, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void closeFile() {
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
		
		int[] dimensionsElements = { dimI, dimJ };
		int[] dimensions = { 1, 1 };
		
		String fields[] = {"date", "u"};
		
		matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "parameters");
		
		matrix.addFieldVariable("dimI", dimI);
		matrix.addFieldVariable("dimJ", dimJ);
		matrix.addFieldVariable("file", filename);
		dataElements.add(matrix);
				
		matrix = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "velocities");
		matrix.addFieldFloatArrayVariable("u", m);
		
		matrix.writeToOuputStream(fos);
	}

	@Override
	public boolean isMultiFrameSupported() {
		return false;
	}

}
