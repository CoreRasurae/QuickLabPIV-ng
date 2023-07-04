package pt.quickLabPIV.exporter;

import java.util.Collections;

import pt.quickLabPIV.Matrix;

public class MatlabLevel5MatrixFloat extends MatlabLevel5Matrix {

	public MatlabLevel5MatrixFloat(MatlabLevel5Element parent, int[] dimensions, String newArrayName) {
		super(parent, MatlabMxTypesEnum.mxSINGLE_CLASS, Collections.emptyList(), dimensions, newArrayName);
	}
	
	public void writeMatrix(Matrix matrix) {
		matrix.copyTransposedMatrixToFloatBytesArray(getRealValues().get(0), 0);
	}

}
