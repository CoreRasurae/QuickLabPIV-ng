// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.exporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines the MATLAB Level 5 known Matrix types 
 * @author lpnm
 */
public enum MatlabMxTypesEnum {
	mxCELL_CLASS       (1, MatlabMiTypesEnum.miMATRIX, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxSTRUCT_CLASS     (2, MatlabMiTypesEnum.miMATRIX,	new ArrayList<MatlabMiTypesEnum>(3)),
	mxOBJECT_CLASS     (3, MatlabMiTypesEnum.miMATRIX, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxCHAR_CLASS       (4, MatlabMiTypesEnum.miUTF8, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxSPARSE_CLASS     (5, null, 						Collections.emptyList()),
	mxDOUBLE_CLASS     (6, MatlabMiTypesEnum.miDOUBLE, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxSINGLE_CLASS     (7, MatlabMiTypesEnum.miSINGLE, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxINT8_CLASS       (8, MatlabMiTypesEnum.miINT8, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxUINT8_CLASS      (9, MatlabMiTypesEnum.miUINT8, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxINT16_CLASS      (10, MatlabMiTypesEnum.miINT16, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxUINT16_CLASS     (11, MatlabMiTypesEnum.miUINT16,	new ArrayList<MatlabMiTypesEnum>(1)),
	mxINT32_CLASS      (12, MatlabMiTypesEnum.miINT32, 	new ArrayList<MatlabMiTypesEnum>(1)),
	mxUINT32_CLASS     (13, MatlabMiTypesEnum.miUINT32,	new ArrayList<MatlabMiTypesEnum>(1)),
	mxINT64_CLASS      (14, MatlabMiTypesEnum.miINT64,  new ArrayList<MatlabMiTypesEnum>(1)),
	mxUINT64_CLASS     (15, MatlabMiTypesEnum.miUINT64, new ArrayList<MatlabMiTypesEnum>(1));
		
	static {
		mxCELL_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miMATRIX);
		mxSTRUCT_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miMATRIX);
		mxOBJECT_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miMATRIX);
		
		mxCHAR_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUTF8);
		mxCHAR_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUTF16);
		mxCHAR_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUTF32);
		
		mxDOUBLE_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miDOUBLE);
		
		mxSINGLE_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miSINGLE);
		
		mxINT8_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miINT8);
		mxUINT8_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUINT8);
		
		mxINT16_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miINT16);
		mxUINT16_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUINT16);

		mxINT32_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miINT32);
		mxUINT32_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUINT32);
		
		mxINT64_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miINT64);
		mxUINT64_CLASS.acceptedMiTypes.add(MatlabMiTypesEnum.miUINT64);
	}
	
	private final int id;
	private final MatlabMiTypesEnum defaultMiType;
	private final List<MatlabMiTypesEnum> acceptedMiTypes;
	
	MatlabMxTypesEnum(int id, MatlabMiTypesEnum defaultMiType, List<MatlabMiTypesEnum> acceptedMiTypes) {
		this.id = id;
		this.defaultMiType = defaultMiType;
		this.acceptedMiTypes = acceptedMiTypes;
	}
	
	int getId() {
		return id;
	}
	
	MatlabMiTypesEnum getDefaultMiType() {
		return defaultMiType;
	}
	
	boolean isMiTypeAccepted(MatlabMiTypesEnum miType) {
		if (acceptedMiTypes.isEmpty()) {
			return true;
		}
		
		return acceptedMiTypes.contains(miType);
	}
}
