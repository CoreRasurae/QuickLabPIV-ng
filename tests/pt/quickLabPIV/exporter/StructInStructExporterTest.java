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
import java.util.List;

import org.junit.Test;

import pt.quickLabPIV.exporter.ExportFailedException;
import pt.quickLabPIV.exporter.MatlabLevel5Element;
import pt.quickLabPIV.exporter.MatlabLevel5Header;
import pt.quickLabPIV.exporter.MatlabLevel5Struct;

public class StructInStructExporterTest {

	@Test
	public void exportStructInStruct() {
		MatlabLevel5Header header = new MatlabLevel5Header();
		List<MatlabLevel5Element> dataElements = new ArrayList<MatlabLevel5Element>(5);
		
		header.setTitle("MATLAB 5.0 - ViPIVIST-ng Velocities exported on: ");
		dataElements.add(header);
		
		int[] dimensions = {1, 1};
		MatlabLevel5Struct parameters = new MatlabLevel5Struct(dataElements.get(dataElements.size()-1), Collections.emptyList(), dimensions, "parameters");
		parameters.addFieldVariable("imageWidth", 	1024);
		parameters.addFieldVariable("imageHeight", 768);
		parameters.addFieldVariable("iaStartI", 20);
		parameters.addFieldVariable("iaEndI", 10);
		parameters.addFieldVariable("iaStartJ", 20);
		parameters.addFieldVariable("iaEndJ", 10);
		parameters.addFieldVariable("overlapFactor", 1.0f/3.0f);
		dataElements.add(parameters);
		
		MatlabLevel5Struct margins = new MatlabLevel5Struct(null, Collections.emptyList(), dimensions, "margins");
		margins.addFieldVariable("top", 30);
		margins.addFieldVariable("bottom", 20);
		parameters.addFieldStructVariable("margins", margins);
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("test1.mat", false);
		} catch (FileNotFoundException e) {
			throw new ExportFailedException("Failed to create file for writing");
		}

		dataElements.get(dataElements.size()-1).writeToOuputStream(fos);
		
		try {
			fos.flush();
		} catch (IOException e1) {
			//Not relevant..
		}
		try {			
			fos.close();
		} catch (IOException e) {
			throw new ExportFailedException("Failed to close file", e);
		}
	}
}
