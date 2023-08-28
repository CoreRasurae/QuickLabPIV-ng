// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.jobs.validator;

import java.util.List;

import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class VectorValidatorJobParameters {
    public IterationStepTiles stepTiles;
    public List<MaxCrossResult> maxResults;
    public int currentFrame;
}
