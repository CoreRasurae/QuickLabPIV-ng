// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

public enum ImageBitDepthEnum {
    BitDepth8(255.0f),
    BitDepth10(1023.0f),
    BitDepth12(4095.0f),
    BitDepth16(65535.0f);
    
    private float maxValue;
    
    private ImageBitDepthEnum(float _maxValue) {
        maxValue = _maxValue;
    }
    
    public float getMaxValue() {
        return maxValue;
    }
}
