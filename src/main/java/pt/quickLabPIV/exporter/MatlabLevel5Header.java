// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

public class MatlabLevel5Header extends MatlabLevel5Element {
	private String title;
	private byte[] descriptiveText = new byte[116];
	private long subsysDataOffset; //64 bits
	private short version;
	private short endianIndicator;

	MatlabLevel5Header() {
		super(null);
		setVersion((short)0x0100);
		setDefaultEndianIndicator();
		setSubsysDataOffset(0);
	}

	/**
	 * Setter for the Matlab Level 5 Header element descriptive text field.
	 * @param title the descriptive text to set (up to 116 chars)
	 */
	void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Setter for the Matlab Level 5 Header element file data format version field.
	 * @param the version identifier as specified by Matlab file format specifications document.
	 */
	void setVersion(short version) {
		this.version = version;
	}

	/**
	 * Set Matlab Level 5 Header element default endian indicator field with current machine endianess.
	 */
	protected void setDefaultEndianIndicator() {
		endianIndicator = (short)('M' << 8 | 'I');
	}

	/**
	 * Setter for Matlab Level 5 Header element subsystem offset field.<br/>
	 * <b>NOTE:</b> All zeros or all spaces in this field indicate that there is
     * no subsystem-specific data stored in the file.
	 * @param offset offset to subsystem-specific data in the MAT-file.
	 */
	void setSubsysDataOffset(long offset) {
		subsysDataOffset = offset;
	}

	@Override
	long computeUpdatedElementNumberOfBytes() {
		return 116 + 8 + 2 + 2;
	}
	
	@Override
	long getBytesLength() {
		return computeUpdatedElementNumberOfBytes();
	}

	@Override
	public void writeToOuputStream(FileOutputStream fos) {
		byte[] titleBytes;
		try {
			if (title == null || title.length() == 0) {
				//FORCE MATLAB Level 5 export by ensuring the first 4 bytes of the header are non-null
				Date d = new Date();
				String text = "MATLAB 5.0, ViPIVIST-ng matrix export on: " + d.toString(); 

				titleBytes = text.getBytes("UTF-8"); 
			} else {
				titleBytes = title.getBytes("UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			throw new ExportFailedException("Failed to convert title to UTF-8", e);
		}
		
		Arrays.fill(descriptiveText, (byte)0x00);
		int bytesToWrite = titleBytes.length;
		if (titleBytes.length > 116) {
			bytesToWrite = 116;
		}
		
		System.arraycopy(titleBytes, 0, descriptiveText, 0, bytesToWrite);
		
		try {
			fos.write(descriptiveText);
			writeInt(fos, (int)subsysDataOffset >>> 32);
			writeInt(fos, (int)subsysDataOffset & 0x0ffffffff);
			writeShort(fos, version);
			writeShort(fos, endianIndicator);
		} catch (IOException e) {
			throw new ExportFailedException("Failed to export data to file", e);
		}
	}
	
}	
