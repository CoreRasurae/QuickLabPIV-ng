// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.images.IImage;

public interface IFilter {
    /**
     * Applies a 2D filter to a Matrix.
     *
     * @param input the input matrix
     * @param output the output matrix
     * @return the filtered matrix
     */
    Matrix applyFilter(Matrix input, Matrix output);
    
    /**
     * Applies a 2D filter to a 2D float array.
     *
     * @param input the input 2D array matrix
     * @param output the output 2D array matrix
     * @return the filtered 2D array
     */
    float[][] applyFilter(float[][] input, float[][] output);

    /**
     * Applies a 2D filter to an image.
     *
     * @param input the input image
     * @param output the output image
     * @return the filtered image
     */
    IImage applyFilter(IImage input, IImage output);
}
