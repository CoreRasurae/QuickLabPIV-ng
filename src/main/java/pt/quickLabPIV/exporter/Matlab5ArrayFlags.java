// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

public enum Matlab5ArrayFlags {
	Complex(1 << 4),
	Global(1 << 5),
	Logical(1 << 6), 
	None(0);
	
	private byte flagValue;

	Matlab5ArrayFlags(int flag) {
		flagValue = (byte)flag;
	}
	
	public byte getFlagValue() {
		return flagValue;
	}
}
