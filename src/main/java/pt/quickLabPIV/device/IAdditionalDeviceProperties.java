// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.device;

import com.aparapi.device.OpenCLDevice;

public interface IAdditionalDeviceProperties {
	public void apply(OpenCLDevice device);

}
