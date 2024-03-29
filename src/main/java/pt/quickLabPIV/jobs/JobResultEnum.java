// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

public enum JobResultEnum {
    JOB_RESULT_IMAGES_TO_MASK,
	JOB_RESULT_IMAGES,
	JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING,
	JOB_RESULT_FILTERED_IMAGES,
	JOB_RESULT_CLIPPED_TILES_A,
	JOB_RESULT_CLIPPED_TILES_B,
	JOB_RESULT_TILES,
	JOB_RESULT_FILTERED_TILES,
	JOB_RESULT_CROSS_MATRICES,
	JOB_RESULT_CROSS_MAXIMUM,
	JOB_RESULT_MASK, 
	JOB_RESULT_CROSS_MAXIMUM_MASKED,
	JOB_RESULT_VALIDATOR_DATA,
	JOB_RESULT_VALIDATED_VECTORS,
	JOB_RESULT_PIV,
	JOB_RESULT_OPTICAL_FLOW, 
	JOB_RESULT_TEST_DEVICE;
}
