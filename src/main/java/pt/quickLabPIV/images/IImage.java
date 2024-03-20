// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

import java.util.Map;

import pt.quickLabPIV.Matrix;

public interface IImage {

    /**
     * Writes the image content to the associated filename.
     * @param overwrite <ul><li>true, if overwriting is allowed</li>
     * <li>false, otherwise</li></ul>
     */
    void writeToFile(boolean overwrite);

    /**
     * Obtains the filename from which this image was loaded.
     * @return the filename
     */
    String getName();

    /**
     * Get the unique instance image Id.
     * @return the image id
     */
    int getImageId();

    /**
     * Retrieves the image height.
     * @return the height in pixels
     */
    int getHeight();

    /**
     * Retrieves the image width.
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Clips an image region into a new matrix instance that is created with clipped region width and height.
     * @param top the top pixel of the region to be clipped
     * @param left the left pixel of the region to be clipped
     * @param height the height of the region to be clipped
     * @param width the width of the region to be clipped
     * @param partialAllowed flag indicating if partial clipping is allowed when region is partially out of bounds
     * @param m matrix to reuse if not null, otherwise a matrix will be allocated with the specified height and width
     * @return the matrix containing the clipped region
     */
    Matrix clipImageMatrix(int top, int left, int height, int width, boolean partialAllowed, Matrix m);

    void applyMask(Image mask);

    int getSpecificIntensityValueCountForRegion(int top, int left, int height, int width, int targetIntensityValue);

    Map<Integer, Histogram> getRegionHistogram(int top, int left, int height, int width);

    /**
     * Deep copies the image, including the image buffer
     * @return the duplicated image
     */
    IImage duplicate();
    
    /**
     * Reads a pixel directly at the specified coordinates from the buffer and converts to a float format 
     * @param y the y image coordinate
     * @param x the x image coordinate
     * @return the pixel intensity value after conversion to a float format
     */
    float readPixel(int y, int x);
    
    /**
     * Write a pixel directly into the image buffer at the specified coordinates, while converting from the float format,
     * to the internal representation.
     * @param y the y image coordinate
     * @param x the x image coordinate
     * @param value the pixel intensity value
     */
    void writePixel(int y, int x, float value);
    
    /**
     * 
     * @return the maximum value accepted by the image type
     */
    float getMaximumValue();
    
    /**
     * Normalize image to values between 0.0 and 1.0 (implies converting to ImageFloat)
     * @param  target the target image float instance, for reuse, or null to instantiate a new one.
     * @return the normalized IImage instance
     */
    ImageFloat normalize(IImage target);

    /**
     * Exports the image directly to 1D Float array, organized scan line by scan line.
     * @param buffer the target float buffer, if null, a new one will be instantiated
     * @return the image exported to the float buffer
     */
    float[] exportTo1DFloatArray(float[] buffer);

    /**
     * Creates an image of the same type with the specified dimensions and name.
     * @param height the new image height
     * @param width the new image width
     * @param name the image name
     * @return the new IImage instance
     */
    public IImage createImageOfSameType(int height, int width, String name);
    
    /**
     * Fills the images/erases it with specified constant value.
     * @param value the value use for filling/erasing
     */
    public void fill(float value);
    
    
    /**
     * Clips an image region into another image instance.
     * @param top the top pixel of the region to clip
     * @param left the left pixel of the region to clip
     * @param height the height of the region to clip
     * @param width the width of the region to clip
     * @param nearest <ul><li>true, use nearest pixel if outside image boundary</li>
     *                    <li>false, leave pixels outside of the boundary set to zero</li></ul>
     * @param target the target image instance to store the results, or null, or null instantiate a new one.
     * @return the target image instance, or a new instance if target is null or doesn't have appropriate dimensions.
     */
    public default IImage clipImage(int top, int left, int height, int width, boolean nearest, IImage target) {
        if (target == null || target.getHeight() != height || target.getWidth() != width) {
            int index = getName().lastIndexOf('.');
            String prefix = getName();
            String suffix = "";
            if (index > 0) {
                prefix = getName().substring(0, index - 1);
                suffix = getName().substring(getName().lastIndexOf('.'));
            }
            StringBuilder sb = new StringBuilder(40);
            sb.append(prefix);
            sb.append("_");
            sb.append(top);
            sb.append("_");
            sb.append(left);
            sb.append(suffix);
            target = createImageOfSameType(height, width, sb.toString());
        }
        
        int sourceHeight = getHeight();
        int sourceWidth = getWidth();

        if (nearest) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int sourceI = top + i;
                    if (sourceI  < 0) {
                        sourceI = 0;
                    } else if (sourceI >= sourceHeight) {
                        sourceI = sourceHeight - 1;
                    }
                    
                    int sourceJ = left + j;
                    if (sourceJ < 0) {
                        sourceJ = 0;
                    } else if (sourceJ >= sourceWidth) {
                        sourceJ = sourceWidth - 1;
                    }
                    
                    target.writePixel(i, j, readPixel(sourceI, sourceJ));
                }
            }
        } else {
            target.fill(0.0f);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int sourceI = top + i;
                    int sourceJ = left + j;
                    
                    if (sourceI < 0 || sourceI >= sourceHeight || sourceJ < 0 || sourceJ >= sourceWidth) {
                        continue;
                    }
                    
                    target.writePixel(i,j, readPixel(sourceI, sourceJ));
                }
            }
        }
        
        return target;
    }
}