// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.UnsupportedImageFormat;

public class ImageFloat implements IImage {
    private static final AtomicInteger counter = new AtomicInteger(0);
    
    private String filename;      //Is the really true Id
    float[] internalBuffer;
    private float[] workingBuffer;
    private int height;
    private int width;
    private ImageBitDepthEnum bitDepth;
    
    private final int instanceId; //Two instances of the same image file will have different Ids
    
    private ImageFloat(int _instanceId) {
        instanceId = _instanceId;
        bitDepth = ImageBitDepthEnum.BitDepth16;
    }
    
    /**
     * Creates a new Image from a BufferedImage.
     * @param newImage the image buffer containing the image
     * @param _filename the filename from which the image was loaded
     */
    public ImageFloat(final BufferedImage newImage, final ImageBitDepthEnum _bitDepth, String _filename) {
        instanceId = counter.incrementAndGet();
        filename = _filename;
        readImageToBuffer(newImage);
        bitDepth = _bitDepth;
    }
    
    /**
     * Creates a new Image from an existing Matrix.
     * @param imageInMatrix the matrix containing the image
     * @param width the image width
     * @param height the image height
     * @param _filename the filename to be associated with the image
     */
    public ImageFloat(final Matrix imageInMatrix, int width, int height, String _filename) {
        instanceId = counter.incrementAndGet();
        
        filename = _filename;

        internalBuffer = new float[width * height];
        this.width = width;
        this.height = height;
        imageInMatrix.copyMatrixToArray(internalBuffer, 0);        
    }

    @Override
    public void writeToFile(boolean overwrite) {
        short localBuffer[] = new short[width * height];
        
        for (int i = 0; i < localBuffer.length; i++) {
            localBuffer[i] = (short)FastMath.round(internalBuffer[i]);
        }
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster r = (WritableRaster)bi.getRaster();
        r.setDataElements(r.getMinX(), r.getMinY(), width, height, localBuffer); 

        File f = new File(filename);
        if (f.exists() && !overwrite) {
            throw new ImageStateException("Cannot overwrite image file");
        }
        
        try {
            ImageIO.write(bi,filename.substring(filename.lastIndexOf(".")+1), f);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public int getImageId() {
        return instanceId;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    protected void readImageToBuffer(BufferedImage bi) {
        Raster r = bi.getData();
        height = bi.getHeight();
        width = bi.getWidth();
        short[] buffer = null;

        ColorModel colorModel = bi.getColorModel();
        ColorSpace colorSpace = colorModel.getColorSpace();
        
        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY && r.getTransferType() == DataBuffer.TYPE_BYTE) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                    BufferedImage.TYPE_USHORT_GRAY);
            Graphics g = image.getGraphics(); 
            g.drawImage(bi, 0, 0, null);  
            g.dispose(); 
            
            r = image.getData();
            if (r.getTransferType() != DataBuffer.TYPE_USHORT) {
                throw new UnsupportedImageFormat("Image format is not supported even after conversion");
            }
            
            buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
        } else if (bi.getType() == BufferedImage.TYPE_CUSTOM && colorSpace.getType() == ColorSpace.TYPE_GRAY && r.getTransferType() == DataBuffer.TYPE_BYTE) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                    BufferedImage.TYPE_USHORT_GRAY);
            Graphics g = image.getGraphics();  
            g.drawImage(bi, 0, 0, null);  
            g.dispose(); 
            
            r = image.getData();
            if (r.getTransferType() != DataBuffer.TYPE_USHORT) {
                throw new UnsupportedImageFormat("Image format is not supported even after conversion");
            }
            
            buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
        } else if (bi.getType() == BufferedImage.TYPE_BYTE_INDEXED && r.getTransferType() == DataBuffer.TYPE_BYTE) {
            //Method 2 - to Convert color image to GRAY
            BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                    BufferedImage.TYPE_USHORT_GRAY);  
            Graphics g = image.getGraphics();  
            g.drawImage(bi, 0, 0, null);  
            g.dispose(); 
            
            r = image.getData();
            if (r.getTransferType() != DataBuffer.TYPE_USHORT) {
                throw new UnsupportedImageFormat("Image format is not supported even after conversion");
            }
            
            buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
        } else if (bi.getType() == BufferedImage.TYPE_USHORT_GRAY && r.getTransferType() == DataBuffer.TYPE_USHORT) {
            buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
        } else if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            if (r.getTransferType() == DataBuffer.TYPE_BYTE) {
                BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                        BufferedImage.TYPE_USHORT_GRAY);  
                Graphics g = image.getGraphics();  
                g.drawImage(bi, 0, 0, null);  
                g.dispose(); 
                
                //Replace original image with gray converted one...
                bi = image;
                
                r = bi.getData();
                if (r.getTransferType() != DataBuffer.TYPE_USHORT) {
                    throw new UnsupportedImageFormat("Image format is not supported even after conversion");
                }
                
                buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
            } else if (r.getTransferType() == DataBuffer.TYPE_INT) {
                //Highly unperformant code...
                int[] localBuffer = (int[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, null);
                if (buffer == null) {
                    buffer = new short[localBuffer.length];
                }
                for (int i = 0; i < localBuffer.length; i+=3) {
                    buffer[i] = (short)(localBuffer[i] & 0x0ffff);
                }
            } else {
                BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                        BufferedImage.TYPE_USHORT_GRAY);
                Graphics g = image.getGraphics();  
                g.drawImage(bi, 0, 0, null);  
                g.dispose(); 
                
                bi = image;
                
                r = bi.getData();
                if (r.getTransferType() != DataBuffer.TYPE_USHORT) {
                    throw new UnsupportedImageFormat("Image format is not supported even after conversion");
                }
                
                buffer = (short[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
            }
            
            bi.flush();
        } else {
            throw new UnsupportedImageFormat("Image format is not supported");
        }

        for (int i = 0; i < buffer.length; i++) {
            internalBuffer[i] = buffer[i];
        }
    }

    /**
     * Helper method that clips an image region
     * 
     * @param top the top pixel of the region to be clipped from the source image
     * @param left the left pixel of the region to be clipped from the source image
     * @param height the height of the clipped region in pixels
     * @param width the width of the clipped region in pixels
     * @param useInternalArray <ul><li>true, an internal array will be used/reused avoiding memory reallocation when possible</li>
     *            <li>false, a new array will always be allocated to store the clipped region, thus being thread safe</li></ul>  
     * @return the byte array containing the clipped region (1D array containing the clipped region, full row by full row)
     * <br/><b>NOTE:</b> Thread safety is lost when using option useInternalArray.  
     */
    protected float[] clipImageToBuffer(final int top, final int left, final int height, final int width, boolean useInternalArray) {
        float[] buffer = null;
        
        if (height * width > getWidth() * getHeight()) {
            throw new ImageStateException("Clip region cannot be greater than the image itself");
        }
        
        if (top + height > getHeight()) {
            throw new ImageStateException("Acess outside image area, pixel I: " + (top+height-1) + " of " + getHeight() + " pixels.");
        }
        if (top < 0) {
            throw new ImageStateException("Access outside image area, pixel I: " + top);
        }
        if (left + width > getWidth()) {
            throw new ImageStateException("Acess outside image area, pixel J: " + (left+width-1) + " of " + getWidth() + " pixels.");
        }
        if (left < 0) {
            throw new ImageStateException("Access outside image area, pixel J: " + left);
        }
        
        if (useInternalArray) {
            if (workingBuffer == null || workingBuffer.length < width * height) {
                workingBuffer = new float[width*height];
            }
            buffer = workingBuffer;
        } else {
            buffer = new float[width*height];
        }       
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                buffer[i * width + j] = internalBuffer[(top + i) * getWidth() + left + j]; 
            }
        }
                
        return buffer;
    }
    
    @Override
    public Matrix clipImageMatrix(int top, int left, int height, int width, boolean partialAllowed, Matrix m) {
        Matrix result = m;
        
        int imageHeight = this.height;
        int imageWidth = this.width;
        
        boolean partial = false;
        if (top < 0 || left < 0 || top + height > this.height || left + width > this.width) {
            partial = true;
        }

        //Computed parameters for region that will be clipped. All values must be inside valid image ranges.
        int clipTop = top;
        int clipLeft = left;
        int clipWidth = width;
        int clipHeight = height;

        //Computed parameters for translating clipped region into actual matrix/tile dimensions.
        //When the IA moves to the left of the extents of the image, its first left side pixels must be zero.
        //When the IA moves to the right of the extents of the image, its first right side pixels must be zero.
        int marginTop = 0;
        int marginLeft = 0;
        int adjustedHeight = height;
        int adjustedWidth = width;
        
        if (partial) {
            if (!partialAllowed) {
                throw new ImageClippingException("Partial clipping would be required but isn't allowed");
            }
            
            if (top < 0) {
                //Absolute clip height reduces by the amount of negative pixels (outside image),
                //while clipping start at 0.
                clipHeight += top;
                clipTop = 0;
                //When translating the clipped region into the final matrix a margin must be placed at
                //the top by the same amount of the off image pixels.
                marginTop = -top;
            }
            
            if (left < 0) {
                //Absolute clip height reduces by the amount of negative pixels (outside image),
                //while clipping start at 0.
                clipWidth += left;
                clipLeft = 0;
                //When translating the clipped region into the final matrix a margin must be placed at
                //the left by the same amount of the off image pixels.              
                marginLeft = -left;
            }
            
            if (top + height > imageHeight) {
                //The amount of pixels to clip must be decreased by the same amount of off image pixels at
                //the bottom side, since the clipping region ends outside the image extents.
                clipHeight -= (top + height) - imageHeight;
                //When translating the clipped region into the final matrix a margin must be placed at
                //the bottom by the same amount of the off image pixels.
                adjustedHeight -= (top + height) - imageHeight;
            }
            
            if (left + width > imageWidth) {
                //The amount of pixels to clip must be decreased by the same amount of off image pixels at
                //the right side, since the clipping region ends outside the image extents.
                clipWidth -= (left + width) - imageWidth;
                //When translating the clipped region into the final matrix a margin must be placed at
                //the right by the same amount of the off image pixels. The margin is to be filled with zeros.
                adjustedWidth -= (left + width) - imageWidth;
            }
            
            if (clipWidth < 0 || clipHeight < 0) {
                if (result == null) {
                    float[] matrixBuffer = new float[width * height];
                    result = new MatrixFloat(matrixBuffer, (short)height, (short)width);
                }
                
                result.zeroMatrix();
                
                return result;
            }
        }
                
        if (m == null) {
            //Need to allocate memory
            if (partial) {
                float[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, true);
                float[] matrixBuffer = new float[width * height];
                result = new MatrixFloat(matrixBuffer, height, width, bitDepth.getMaxValue());
                result.copySubMatrixFromArray(buffer, marginTop, marginLeft, adjustedHeight, adjustedWidth, true);
            } else {
                //NOTE: useInternalArray must be false, because MatrixByte constructor will store the buffer instance internally.
                float[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, false);
                result = new MatrixFloat(buffer, height, width, bitDepth.getMaxValue(), true);
            }
        } else {    
            //Reuse memory
            if (height > m.getHeight() || width > m.getWidth()) {
                throw new ImageStateException("Cannot clip region as it exceeds the dimensions of the provided matrix.");
            }
                        
            float[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, true);
            if (!partial) {
                result.copyMatrixFromArray(buffer, 0, true);
            } else {
                result.copySubMatrixFromArray(buffer, marginTop, marginLeft, adjustedHeight, adjustedWidth, true);
            }
        }
        
        return result;
    }

    @Override
    public void applyMask(Image mask) {
        if (mask.getHeight() != height || mask.getWidth() != width) {
            throw new ImageStateException("Cannot apply mask with different geometry than image to mask");
        }
        
        for (int i = 0; i < internalBuffer.length; i++) {
            if (mask.internalBuffer[i] == 0) {
                internalBuffer[i] = 0;
            }
        }
    }

    @Override
    public int getSpecificIntensityValueCountForRegion(int top, int left, int height, int width,
            int targetIntensityValue) {
        int count = 0;
        
        if (height * width > getWidth() * getHeight()) {
            throw new HistogramException("Region cannot be greater than the image itself");
        }
        
        if (top + height > getHeight()) {
            throw new HistogramException("Acess outside image area, pixel I: " + (top+height-1) + " of " + getHeight() + " pixels.");
        }
        if (top < 0) {
            throw new HistogramException("Access outside image area, pixel I: " + top);
        }
        if (left + width > getWidth()) {
            throw new HistogramException("Acess outside image area, pixel J: " + (left+width-1) + " of " + getWidth() + " pixels.");
        }
        
        if (left < 0) {
            throw new HistogramException("Access outside image area, pixel J: " + left);
        }

        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int localIntensityValue = FastMath.round(internalBuffer[(top + i) * getWidth() + left + j]);
                if (localIntensityValue == targetIntensityValue) {
                    count++;
                }
            }
        }
        
        return count;
    }

    @Override
    public Map<Integer, Histogram> getRegionHistogram(int top, int left, int height, int width) {
        Map<Integer, Histogram> map = new HashMap<Integer, Histogram>(10);

        if (height * width > getWidth() * getHeight()) {
            throw new ImageStateException("Region cannot be greater than the image itself");
        }
        
        if (top + height > getHeight()) {
            throw new ImageStateException("Acess outside image area, pixel I: " + (top+height-1) + " of " + getHeight() + " pixels.");
        }
        if (top < 0) {
            throw new ImageStateException("Access outside image area, pixel I: " + top);
        }
        if (left + width > getWidth()) {
            throw new ImageStateException("Acess outside image area, pixel J: " + (left+width-1) + " of " + getWidth() + " pixels.");
        }
        if (left < 0) {
            throw new ImageStateException("Access outside image area, pixel J: " + left);
        }

        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int localIntensityValue = FastMath.round(internalBuffer[(top + i) * getWidth() + left + j]);
                Histogram hist = map.computeIfAbsent(localIntensityValue, (intensity) -> new Histogram(intensity));
                hist.incrementCount();
            }
        }
        
        
        return map;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        
        if (!another.getClass().equals(ImageInt16.class)) {
            return false;
        }
        
        if (another == this) {
            return true;
        }
        
        ImageFloat anotherImg = (ImageFloat)another;
        
        if (filename == anotherImg.filename) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return filename.hashCode();
    }
    
    @Override
    public String toString() {
        return instanceId + " - " + filename;
    }

    @Override
    public IImage duplicate() {
        ImageFloat duplicated = new ImageFloat(instanceId);
        
        duplicated.filename = filename;
        duplicated.internalBuffer = Arrays.copyOf(internalBuffer, internalBuffer.length);
        duplicated.workingBuffer = null;
        duplicated.height = height;
        duplicated.width = width;
        
        return duplicated;        
    }

    public static ImageFloat sizeFrom(IImage source) {
        ImageFloat created = new ImageFloat(counter.incrementAndGet());
        
        created.filename = "Unknown";
        created.workingBuffer = null;
        created.height = source.getHeight();
        created.width = source.getWidth();

        created.internalBuffer = new float[created.height * created.width];
                
        return created;        
    }

    
    @Override
    public float readPixel(int y, int x) {
        return internalBuffer[y * width + x];
    }

    @Override
    public void writePixel(int y, int x, float value) {
        internalBuffer[y * width + x] = value;
    }

    @Override
    public float getMaximumValue() {
        return 65535.0f;
    }

    public static ImageFloat copyAndNormalize(IImage source, ImageFloat target) {
        float maxValue = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                float value = source.readPixel(y, x);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        
        if (target == null || target.getHeight() != source.getHeight() || target.getWidth() != source.getWidth()) {
            target = sizeFrom(source);
        }

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int idx = y * target.getWidth() + x;
                target.internalBuffer[idx] = source.readPixel(y, x) / maxValue;
            }
        }
        
        return target;
    }

    @Override
    public ImageFloat normalize(IImage target) {
        ImageFloat targetFloat = ImageFloat.convertFrom(target);
        float maxValue = 0;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int idx = y * getWidth() + x;
                float value = internalBuffer[idx];
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }

        if (target == null || target.getHeight() != getHeight() || target.getWidth() != getWidth()) {
            targetFloat = sizeFrom(this);
        }

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int idx = y * targetFloat.getWidth() + x;
                targetFloat.internalBuffer[idx] = internalBuffer[idx] / maxValue;
            }
        }

        return targetFloat;
    }

    @Override
    public float[] exportTo1DFloatArray(float[] buffer) {
        if (buffer == null || buffer.length < internalBuffer.length) {
            buffer = new float[internalBuffer.length];
        }
        
        for (int idx = 0; idx < internalBuffer.length; idx++) {
            float value = internalBuffer[idx];
            buffer[idx] = value;
        }
        
        return buffer;
    }

    public static ImageFloat convertFrom(ImageFloat img) {
        return img;
    }
    
    public static ImageFloat convertFrom(IImage img) {
        if (img == null) {
            return null;
        }
        
        ImageFloat imgNew = sizeFrom(img);
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                imgNew.internalBuffer[i * img.getWidth() + j] = img.readPixel(i, j);
            }
        }
        return imgNew;
    }
    
    @Override
    public IImage createImageOfSameType(int height, int width, String name) {
        ImageFloat created = new ImageFloat(counter.incrementAndGet());

        created.filename = name;

        created.internalBuffer = new float[width * height];
        created.width = width;
        created.height = height;
        
        return created;
    }

    @Override
    public void fill(float value) {
        Arrays.fill(internalBuffer, value);
    }   
}