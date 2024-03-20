// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

public enum MatlabMiTypesEnum {
	miINT8       (1,	1),
	miUINT8      (2,	1),
	miINT16      (3,	2),
	miUINT16     (4,	2),
	miINT32      (5,	4),
	miUINT32     (6,	4),
	miSINGLE     (7,	4),
	miDOUBLE     (9,	8),
	miINT64      (12,	8),
	miUINT64     (13,	8),
	miMATRIX     (14,	0),
	miCOMPRESSED (15,	0),
	miUTF8       (16,	1),
	miUTF16      (17,	2),
	miUTF32      (18,	4);
	
	/**
	 * MATLAB type id
	 */
	private final byte id;
	
	/**
	 * Size in bytes of data type
	 */
	private final byte size;
	
	/**
	 * Creates a new MatlabMiTypes enum entry.
	 * @param id the Matlab object id
	 * @param size the type size in bytes, or 0 if size is unknown.
	 */
	MatlabMiTypesEnum(int id, int size) {
		this.id = (byte)id;
		this.size = (byte)size;
	}
	
	/**
	 * The MATLAB type id.
	 * @return the type id for this MATLAB type. 
	 */
	int getId() {
		return id;
	}
	
	/**
	 * The size in bytes of the MATLAB type.
	 * @return <ul><li>the size in bytes for this MATLAB type,</li>
	 * 			<li>or 0 if size is unknown for this type</li></ul>
	 */
	int getSize() {
		return size;
	}
}
