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

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixByte;
import pt.quickLabPIV.UnsupportedImageFormat;

/**
 * Class responsible for handling Images. Provides an abstraction over third-party frameworks. 
 * @author lpnm
 *
 */
public final class Image implements IImage {
	private static final AtomicInteger counter = new AtomicInteger(0);
	
	private String filename;      //Is the really true Id
	byte[] internalBuffer;
	private byte[] workingBuffer;
	private int height;
	private int width;	
	
	private final int instanceId; //Two instances of the same image file will have different Ids
	
	private Image(int _instanceId) {
	    instanceId = _instanceId;
	}
	
	/**
	 * Creates a new Image from a BufferedImage.
	 * @param newImage the image buffer containing the image
	 * @param _filename the filename from which the image was loaded
	 */
	public Image(final BufferedImage newImage, String _filename) {
		instanceId = counter.incrementAndGet();
		filename = _filename;
		readImageToBuffer(newImage);
	}
	
	/**
	 * Creates a new Image from an existing Matrix.
	 * @param imageInMatrix the matrix containing the image
	 * @param width the image width
	 * @param height the image height
	 * @param _filename the filename to be associated with the image
	 */
	public Image(final Matrix imageInMatrix, int width, int height, String _filename) {
		instanceId = counter.incrementAndGet();
		
		filename = _filename;

		internalBuffer = new byte[width * height];
		this.width = width;
		this.height = height;
		
		if (imageInMatrix.getMaxValue() > 255.0f) {
		    float scale = 255.0f / imageInMatrix.getMaxValue();
		    
		    imageInMatrix.copyMatrixToArray(internalBuffer, 0, scale);
		} else {
		    imageInMatrix.copyMatrixToArray(internalBuffer, 0);
		}
	}

	/**
	 * Writes the image content to the associated filename.
	 * @param overwrite <ul><li>true, if overwriting is allowed</li>
	 * <li>false, otherwise</li></ul>
	 */
	@Override
    public void writeToFile(boolean overwrite) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = (WritableRaster)bi.getRaster();
		r.setDataElements(r.getMinX(), r.getMinY(), width, height, internalBuffer);	

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
	
	/**
	 * Obtains the filename from which this image was loaded.
	 * @return the filename
	 */
	@Override
    public String getName() {
		return filename;
	}
	
	/**
	 * Get the unique instance image Id.
	 * @return the image id
	 */
	@Override
    public int getImageId() {
		return instanceId;
	}
	
	/**
	 * Retrieves the image height.
	 * @return the height in pixels
	 */
	@Override
    public int getHeight() {
		return height;
	}
	
	/**
	 * Retrieves the image width.
	 * @return the width in pixels.
	 */
	@Override
    public int getWidth() {
		return width;
	}
	
	
	protected void readImageToBuffer(BufferedImage bi) {
		Raster r = bi.getData();
		height = bi.getHeight();
		width = bi.getWidth();
		byte[] buffer = null;

		ColorModel colorModel = bi.getColorModel();
        ColorSpace colorSpace = colorModel.getColorSpace();
		
		if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY && r.getTransferType() == DataBuffer.TYPE_BYTE) {
			buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		} else if (bi.getType() == BufferedImage.TYPE_CUSTOM && colorSpace.getType() == ColorSpace.TYPE_RGB) {
		    BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
                    BufferedImage.TYPE_BYTE_GRAY);  
            Graphics g = image.getGraphics();  
            g.drawImage(bi, 0, 0, null);  
            g.dispose(); 
            
            r = image.getData();
            if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
                throw new UnsupportedImageFormat("Image format is not supported even after conversion");
            }
            
            buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		} else if (bi.getType() == BufferedImage.TYPE_CUSTOM && colorSpace.getType() == ColorSpace.TYPE_GRAY && r.getTransferType() == DataBuffer.TYPE_BYTE) {
		    if (colorModel.hasAlpha()) {
		        BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
	                    BufferedImage.TYPE_BYTE_GRAY);  
	            Graphics g = image.getGraphics();  
	            g.drawImage(bi, 0, 0, null);  
	            g.dispose(); 
	            
	            r = image.getData();
	            if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
	                throw new UnsupportedImageFormat("Image format is not supported even after conversion");
	            }
	            
	            buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		    } else {
		        buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		    }
		} else if (bi.getType() == BufferedImage.TYPE_BYTE_INDEXED && r.getTransferType() == DataBuffer.TYPE_BYTE) {
			//BYTE INDEXED -- implies a color translation map - must be converted to gray
			
			//Method 1 - to Convert color image to GRAY
			/*ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
			ColorConvertOp op = new ColorConvertOp(cs, null);  
			BufferedImage image = op.filter(bi, null);*/
			
			//Method 2 - to Convert color image to GRAY
			BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
				    BufferedImage.TYPE_BYTE_GRAY);  
			Graphics g = image.getGraphics();  
			g.drawImage(bi, 0, 0, null);  
			g.dispose(); 
			
			r = image.getData();
			if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
				throw new UnsupportedImageFormat("Image format is not supported even after conversion");
			}
			
			buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		} else if (bi.getType() == BufferedImage.TYPE_USHORT_GRAY && r.getTransferType() == DataBuffer.TYPE_USHORT) {
			//Try to adjust pixel intensity range to 16
			WritableRaster origRaster = bi.getRaster();
			int min = Integer.MAX_VALUE;
			int max = 0;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int val = origRaster.getSample(x, y, 0);
					if (val > max) {
						max = val;
					}
					if (val < min) {
						min = val;
					}
				}
			}
			
			int factor = 65535/(max-min);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int val = origRaster.getSample(x, y, 0);
					origRaster.setSample(x, y, 0, (val - min) * factor);
				}
			}
			
			BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
				    BufferedImage.TYPE_BYTE_GRAY);  
			Graphics g = image.getGraphics();  
			g.drawImage(bi,      0, 0, null);  
			g.dispose(); 
				
			//r = bi.getData();
			r = image.getData();
			if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
				throw new UnsupportedImageFormat("Image format is not supported even after conversion");
			}
				
			buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
		} else if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
			/*if (r.getTransferType() == DataBuffer.TYPE_BYTE) {
			    System.out.println("B1");
				//Highly unperformant code
				//byte[] localBuffer = (byte[])r.getDataElements(r.getMinX() + left, r.getMinY() + top, width, height, null);
				//if (buffer == null) {					
				//	buffer = new byte[localBuffer.length/3];
				//}
				//for (int i = 0; i < localBuffer.length; i+=3) {
				//	buffer[i/3] = localBuffer[i];
				//}

				//Using Get samples instead should be faster, but can only read ints not bytes... 
				//r.getSamples(x, y, w, h, b, iArray)
				
				BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
					    BufferedImage.TYPE_BYTE_GRAY);  
				Graphics g = image.getGraphics();  
				g.drawImage(bi, 0, 0, null);  
				g.dispose(); 
				
				//Replace original image with gray converted one...
				bi = image;
				
				r = bi.getData();
				if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
					throw new UnsupportedImageFormat("Image format is not supported even after conversion");
				}
				
				buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);

			} else if (r.getTransferType() == DataBuffer.TYPE_INT) {
			    System.out.println("B2");
				//Highly unperformant code...
				int[] localBuffer = (int[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, null);
				if (buffer == null) {
					buffer = new byte[localBuffer.length];
				}
				for (int i = 0; i < localBuffer.length; i+=3) {
					buffer[i] = (byte)(localBuffer[i] & 0x0ff);
				}
			}*/
			   
			BufferedImage image = new BufferedImage(getWidth(), getHeight(),  
				    BufferedImage.TYPE_BYTE_GRAY);  
			Graphics g = image.getGraphics();  
			g.drawImage(bi, 0, 0, null);  
			g.dispose(); 
			
			bi = image;
			
			r = bi.getData();
			if (r.getTransferType() != DataBuffer.TYPE_BYTE) {
				throw new UnsupportedImageFormat("Image format is not supported even after conversion");
			}
			
			buffer = (byte[])r.getDataElements(r.getMinX(), r.getMinY(), width, height, buffer);
			
			bi.flush();
		} else {
			throw new UnsupportedImageFormat("Image format is not supported");
		}
		
        int count = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == -1) {
                count++;
            }
        }
        System.out.println(filename + " " + count);

		//TODO Add option for clipping... Check why high (255) values are being clipped to 0
		/*for (int i = 0; i < buffer.length; i++) {
		    buffer[i] = (buffer[i] < 65 && buffer[i] > 0)? 0 : buffer[i];
		}*/
		
		internalBuffer = buffer;
	}
	
	/**
	 * Helper method that clips an image region providing proper color conversion to an 8-bit Gray color space.
	 * 
	 * @param top the top pixel of the region to be clipped from the source image
	 * @param left the left pixel of the region to be clipped from the source image
	 * @param height the height of the clipped region in pixels
	 * @param width the width of the clipped region in pixels
	 * @param useInternalArray <ul><li>true, an internal array will be used/reused avoiding memory reallocation when possible</li>
	 * 		      <li>false, a new array will always be allocated to store the clipped region, thus being thread safe</li></ul>  
	 * @return the byte array containing the clipped region (1D array containing the clipped region, full row by full row)
	 * <br/><b>NOTE:</b> Thread safety is lost when using option useInternalArray.  
	 */
	protected byte[] clipImageToBuffer(final int top, final int left, final int height, final int width, boolean useInternalArray) {
		byte[] buffer = null;
		
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
				workingBuffer = new byte[width*height];
			}
			buffer = workingBuffer;
		} else {
			buffer = new byte[width*height];
		}		
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				buffer[i * width + j] = internalBuffer[(top + i) * getWidth() + left + j]; 
			}
		}
				
		return buffer;
	}
	
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
	@Override
    public Matrix clipImageMatrix(final int top, final int left, final int height, final int width, boolean partialAllowed, Matrix m) {
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
					byte[] matrixBuffer = new byte[width * height];
					result = new MatrixByte(matrixBuffer, (short)height, (short)width);
				}
				
				result.zeroMatrix();
				
				return result;
			}
		}
				
		if (m == null) {
		    //Need to allocate memory
			if (partial) {
				byte[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, true);
				byte[] matrixBuffer = new byte[width * height];
				result = new MatrixByte(matrixBuffer, height, width);
				result.copySubMatrixFromArray(buffer, marginTop, marginLeft, adjustedHeight, adjustedWidth, true);
			} else {
				//NOTE: useInternalArray must be false, because MatrixByte constructor will store the buffer instance internally.
				byte[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, false);
				result = new MatrixByte(buffer, height, width, true);
			}
		} else {	
		    //Reuse memory
			if (height > m.getHeight() || width > m.getWidth()) {
				throw new ImageStateException("Cannot clip region as it exceeds the dimensions of the provided matrix.");
			}
						
			byte[] buffer = clipImageToBuffer(clipTop, clipLeft, clipHeight, clipWidth, true);
			if (!partial) {
				result.copyMatrixFromArray(buffer, 0, true);
			} else {
			    result.zeroMatrix();
				result.copySubMatrixFromArray(buffer, marginTop, marginLeft, adjustedHeight, adjustedWidth, true);
			}
		}
		
		return result;
	}
	
    public void applyMask(Image mask) {
	    if (mask.height != height || mask.width != width) {
	        throw new ImageStateException("Cannot apply mask with different geometry than image to mask");
	    }
	    
	    for (int i = 0; i < internalBuffer.length; i++) {
	        if (mask.internalBuffer[i] == 0) {
	            internalBuffer[i] = 0;
	        }
	    }
	}
    
	@Override
    public int getSpecificIntensityValueCountForRegion(final int top, final int left, final int height, final int width, final int targetIntensityValue) {
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
                int localIntensityValue = internalBuffer[(top + i) * getWidth() + left + j];
                if ((localIntensityValue < 0 ? 256 + localIntensityValue : localIntensityValue) == targetIntensityValue) {
                    count++;
                }
            }
        }
        
        return count;
	}
	
	@Override
    public Map<Integer,Histogram> getRegionHistogram(final int top, final int left, final int height, final int width) {
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
                int localIntensityValue = internalBuffer[(top + i) * getWidth() + left + j];
                localIntensityValue = localIntensityValue < 0 ? 256 + localIntensityValue : localIntensityValue;
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
		
		if (!another.getClass().equals(Image.class)) {
			return false;
		}
		
		if (another == this) {
			return true;
		}
		
		Image anotherImg = (Image)another;
		
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
        Image duplicated = new Image(instanceId);
        
        duplicated.filename = filename;
        duplicated.internalBuffer = Arrays.copyOf(internalBuffer, internalBuffer.length);
        duplicated.workingBuffer = null;
        duplicated.height = height;
        duplicated.width = width;
        
        return duplicated;        
    }

    @Override
    public float readPixel(int y, int x) {
        float localIntensityValue = internalBuffer[y * getWidth() + x];
        if (localIntensityValue < 0.0f) {
            localIntensityValue += 256.0f;
        }
        return localIntensityValue;
    }

    @Override
    public void writePixel(int y, int x, float value) {
        if (value > 255.0f) {
            throw new ImageUpdateException("Trying to set a pixel with a value to large for internal representation");
        }
        internalBuffer[y * getWidth() + x] = (byte)value;        
    }

    @Override
    public float getMaximumValue() {
        return 255.0f;
    }

    @Override
    public ImageFloat normalize(IImage target) {
        ImageFloat targetFloat = ImageFloat.convertFrom(target);
        ImageFloat result = ImageFloat.copyAndNormalize(this, targetFloat);
        
        return result;
    }

    @Override
    public float[] exportTo1DFloatArray(float[] buffer) {
        if (buffer == null || buffer.length < internalBuffer.length) {
            buffer = new float[internalBuffer.length];
        }
        
        for (int idx = 0; idx < internalBuffer.length; idx++) {
            float value = internalBuffer[idx];
            if (value < 0.0f) {
                value += 256.0f;
            }
            buffer[idx] = value;
        }
        
        return buffer;
    }

    @Override
    public IImage createImageOfSameType(int height, int width, String name) {
        Image created = new Image(counter.incrementAndGet());

        created.filename = name;

        created.internalBuffer = new byte[width * height];
        created.width = width;
        created.height = height;
        
        return created;
    }

    @Override
    public void fill(float value) {
        if (value > 255.0f) {
            value = 255.0f;
        }
        if (value < 0.0f) {
            value = 0.0f;
        }
        Arrays.fill(internalBuffer, (byte)value);
    }	
}
