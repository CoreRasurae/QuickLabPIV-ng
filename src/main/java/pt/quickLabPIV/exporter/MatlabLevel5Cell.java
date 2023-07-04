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
