// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

import java.util.Collection;

public class MatlabLevel5Cell extends MatlabLevel5Matrix {

	public MatlabLevel5Cell(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, MatlabMiTypesEnum storageType,
			Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName) {
		super(parent, arrayClass, storageType, newArrayFlags, dimensions, newArrayName);
	}

	public MatlabLevel5Cell(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, Collection<Matlab5ArrayFlags> newArrayFlags,
			int[] dimensions, String newArrayName) {
		super(parent, arrayClass, newArrayFlags, dimensions, newArrayName);
	}
	

}
