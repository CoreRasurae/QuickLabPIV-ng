// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import java.awt.image.BufferedImage;

import pt.quickLabPIV.PIVInputParameters;

public enum ImageFactoryEnum {
	Image8Bit,
	Image10Bit,
	Image12Bit,
	Image16Bit;
	
	static public IImage create(PIVInputParameters parameters, BufferedImage bi, String filename) {
		switch (parameters.getPixelDepth()) {
		case Image8Bit:
			return new Image(bi, filename);
		case Image10Bit:
		    return new ImageInt16(bi, ImageBitDepthEnum.BitDepth10, filename);
		case Image12Bit:
		    return new ImageInt16(bi, ImageBitDepthEnum.BitDepth12, filename);
		case Image16Bit:			
			return new ImageInt16(bi, ImageBitDepthEnum.BitDepth16, filename);
		}
		
		return null;
	}
}
