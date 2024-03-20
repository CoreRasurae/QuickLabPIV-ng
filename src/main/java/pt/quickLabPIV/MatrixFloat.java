// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.images.IImage;

public class MatrixFloat extends Matrix {
	private float[] matrix;
	
	public MatrixFloat(int height, int width, float maxValue) {
	    super(height, width, maxValue);
	    
	    matrix = new float[height*width];
	}
	
	public MatrixFloat(int height, int width) {
		super(height, width, Float.MAX_VALUE);
		
		matrix = new float[height*width];
	}
	
	public MatrixFloat(final float[] newMatrix, int height, int width) {
		super(height, width, Float.MAX_VALUE);
		if (newMatrix.length != height * width) {
			throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
		}
		matrix = newMatrix;
	}

    public MatrixFloat(final float[] newMatrix, int height, int width, float maxValue) {
        super(height, width, maxValue);
        if (newMatrix.length != height * width) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        matrix = newMatrix;
    }

	
	public MatrixFloat(float[] buffer, int height, int width, float maxValue, boolean computeMaxValue) {
	    super(height, width, maxValue);
	    if (computeMaxValue) {
	        float myMaxValue = 0;
	        for (int i = 0; i < buffer.length; i++) {
	            if (buffer[i] > myMaxValue) {
	                myMaxValue = buffer[i];
	            }
	        }
	        setMaxValue(myMaxValue);
	    }
        if (buffer.length != height * width) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        matrix = buffer;
	}

    @Override
	public float getElement(int i, int j) {
		return matrix[i * getWidth() + j];
	}

	@Override
	public void setElement(float value, int i, int j) {
		matrix[i * getWidth() + j] = value;
	}

	@Override
	public void setElement(byte value, int i, int j) {
		int intValue = value;
		if (value < 0) {
			value += 256;
		}
		matrix[i * getWidth() + j] = intValue;
	}
	
    @Override
    public void setElement(short value, int i, int j) {
        int intValue = value;
        if (value < 0) {
            value += 65356;
        }
        matrix[i * getWidth() + j] = intValue;
    }

    @Override
    public void copyMatrixToArray(byte[] destination, int destinationOffset, float scaleFactor) {
        for (int i = 0; i < matrix.length; i++) {
            float value = matrix[i];
            if (value < 0) {
                value += 65536;
            }
            value *= scaleFactor;
            if (value > 255.5f) {
                throw new MatrixConversionOverflow("Value to large to be represented internally by a byte array");
            }
            destination[destinationOffset + i] = (byte)(value >= 128 ? value - 256 : value);
        }
    }
    
	@Override
	public void copyMatrixToArray(final float[] destination, final int destinationOffset) {
		System.arraycopy(matrix, 0, destination, destinationOffset, matrix.length);
	}
	
	@Override
	public void copyMatrixToArray(final float[] destination, final int destinationOffset, final int destinationWidth) {
		final int width = getWidth();
		for (int i = 0; i < getHeight(); i++) {
			System.arraycopy(matrix, i*width, destination, destinationOffset + i *destinationWidth, width);
		}
	}
	
	@Override
	public void copyMirroredMatrixTo2DArray(float[][] destination, int offsetI, int offsetJ) {
		for (int srcI = getHeight() - 1, dstI = 0; srcI >= 0; srcI--, dstI++) {
			for (int srcJ = getWidth() - 1, dstJ = 0; srcJ >= 0; srcJ--, dstJ++) {
				destination[offsetI + dstI][offsetJ + dstJ] = matrix[srcI * getWidth() + srcJ];
			}
		}
	}
	
	@Override
	public void copyMatrixToArray(final byte[] destination, final int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				float value = matrix[i * getWidth() + j];
				if (value >= 255.5f || value < 0.0f) {
					throw new MatrixConversionOverflow("Cannot convert float value to unsigned 8-bit, it would overflow");
				}
				byte convertedValue = (byte)FastMath.round(value);
				
				destination[destinationOffset + i*getWidth() + j] = convertedValue;
			}
		}		
	}
	
    @Override
    public void copyMatrixToArray(final short[] destination, final int destinationOffset) {
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                float value = matrix[i * getWidth() + j];
                if (value >= 65535.5f || value < 0.0f) {
                    throw new MatrixConversionOverflow("Cannot convert float value to unsigned 8-bit, it would overflow");
                }
                short convertedValue = (short)FastMath.round(value);
                
                destination[destinationOffset + i*getWidth() + j] = convertedValue;
            }
        }       
    }	
	
	@Override
	public void copyMatrixToFloatBytesArray(final byte[] destination, final int destinationOffset) {
		for (int i = 0; i < matrix.length; i++) {
			float value = matrix[i];
			int ieeeValue = Float.floatToIntBits(value);
			
			destination[destinationOffset + i*Float.BYTES] = (byte)(ieeeValue & 0x0ff);
			destination[destinationOffset + i*Float.BYTES + 1] = (byte)(ieeeValue >>> 8);
			destination[destinationOffset + i*Float.BYTES + 2] = (byte)(ieeeValue >>> 16);
			destination[destinationOffset + i*Float.BYTES + 3] = (byte)(ieeeValue >>> 24);
		}
	}

	@Override
	public void copyTransposedMatrixToArray(final byte[] destination, final int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				float value = matrix[i * getWidth() + j];
				if (value >= 255.5f || value < 0.0f) {
					throw new MatrixConversionOverflow("Cannot convert float value to unsigned 8-bit, it would overflow");
				}
				byte convertedValue = (byte)FastMath.round(value);
				
				destination[destinationOffset + j*getHeight() + i] = convertedValue;
			}
		} 
	}
	
	@Override
	public void copyTransposedMatrixToFloatBytesArray(final byte[] destination, final int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				float value = matrix[i * getWidth() + j];
				int ieeeValue = Float.floatToIntBits(value);
				
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES] = (byte)(ieeeValue & 0x0ff);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 1] = (byte)(ieeeValue >>> 8);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 2] = (byte)(ieeeValue >>> 16);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 3] = (byte)(ieeeValue >>> 24);
			}
		}
	}

	@Override
	public void copyMatrixTo2DArray(final float[][] destination, final int offsetI, final int offsetJ) {
		int li = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				float value = matrix[li++];
				destination[offsetI + i][offsetJ + j] = value; 
			}
		}
	}

	@Override
	public void copyMatrixTo1PaddedArray(final float[] destination, final int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			destination[destinationOffset + i * (getWidth() + 1)] = 0.0f;
		}
		for (int j = 0; j < getHeight(); j++) {
			destination[destinationOffset + j] = 0.0f;
		}
		int sourceIndex = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				destination[destinationOffset + (i+1) * (getWidth() + 1) + (j+1)] = matrix[sourceIndex++];
			}
		}
	}

	@Override
	public void copyMatrixFromArray(float[] source, int sourceOffset) {
		for (int i = 0; i < getHeight()*getWidth(); i++) {
			matrix[i] = source[sourceOffset + i];
		}
	}

	@Override
	public void copyMatrixFrom2DArray(float[][] source, int offsetI, int offsetJ) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				matrix[i * getWidth() + j] = source[offsetI + i][offsetJ + j];
			}
		}
		
	}
	
	@Override
	public void copyMatrixFromArray(byte[] source, int sourceOffset, boolean computeMax) {
	    int maxValue = 0;
	    
		if (source.length < matrix.length) {
			throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
		}
		
		for (int index = 0; index < matrix.length; index++) {
			int value = source[sourceOffset + index];
			if (value < 0) {
				value += 256;
			}
			if (computeMax && value > maxValue) {
			    maxValue = value;
			}
			matrix[index] = value;
		}
		
		if (computeMax) {
		    setMaxValue(maxValue);
		}
	}

    @Override
    public void copyMatrixFromArray(short[] source, int sourceOffset, boolean computeMaxValue) {
        int maxValue = 0;
        if (source.length < matrix.length) {
            throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
        }
        
        for (int index = 0; index < matrix.length; index++) {
            int value = source[sourceOffset + index];
            if (value < 0) {
                value += 65536;
            }
            
            if (computeMaxValue && value > maxValue) {
                maxValue = value;
            }
            
            matrix[index] = value;
        }
        
        if (computeMaxValue) {
            setMaxValue(maxValue);
        }
    }
	
    @Override
    public void copyMatrixFromArray(float[] source, int sourceOffset, boolean computeMaxValue) {
        float maxValue = 0;
        if (source.length < matrix.length) {
            throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
        }
        
        for (int index = 0; index < matrix.length; index++) {
            float value = source[sourceOffset + index];

            if (computeMaxValue && value > maxValue) {
                maxValue = value;
            }
            
            matrix[index] = value;
        }
        
        if (computeMaxValue) {
            setMaxValue(maxValue);
        }
    }
    
	@Override
	public void copySubMatrixFromArray(byte[] buffer, int subRegionTop, int subRegionLeft,
			int subRegionHeight, int subRegionWidth, boolean computeMax) {
	    int maxValue = 0;
		if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
			throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
					" exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
		}
		
		int index = 0;
		for (int i = subRegionTop; i < subRegionHeight; i++) {
			for (int j = subRegionLeft; j < subRegionWidth; j++) {
			    if (computeMax) {
    			    int value = buffer[index];
    			    if (value < 0) {
    			        value += 256;
    			    }
    			    if (value > maxValue) {
    			        maxValue = value;
    			    }
			    }
				matrix[i * getWidth() + j] = buffer[index++];
			}
		}
		if (computeMax) {
		    setMaxValue(maxValue);
		}
	}

    @Override
    public void copySubMatrixFromArray(short[] buffer, int subRegionTop, int subRegionLeft,
            int subRegionHeight, int subRegionWidth, boolean computeMaxValue) {
        if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
            throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
                    " exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
        }
        
        int maxValue = 0;
        int index = 0;
        for (int i = subRegionTop; i < subRegionHeight; i++) {
            for (int j = subRegionLeft; j < subRegionWidth; j++) {
                if (computeMaxValue) {
                    int value = buffer[index];
                    if (value < 0) {
                        value += 65536;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                    }
                }
                matrix[i * getWidth() + j] = buffer[index++];
            }
        }
        
        if (computeMaxValue) {
            setMaxValue(maxValue);
        }
    }
	
    @Override
    public void copySubMatrixFromArray(float[] buffer, int subRegionTop, int subRegionLeft, int subRegionHeight,
            int subRegionWidth, boolean computeMaxValue) {
        if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
            throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
                    " exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
        }
        
        float maxValue = 0;
        int index = 0;
        for (int i = subRegionTop; i < subRegionHeight; i++) {
            for (int j = subRegionLeft; j < subRegionWidth; j++) {
                if (computeMaxValue) {
                    float value = buffer[index];
                    if (value > maxValue) {
                        maxValue = value;
                    }
                }
                matrix[i * getWidth() + j] = buffer[index++];
            }
        }
        
        if (computeMaxValue) {
            setMaxValue(maxValue);
        }
    }

    
	@Override
	public void copyMatrixFromLargerArray(float[] source, final int sourceOffset, final int sourceWidth) {
		final int width = getWidth();
		for (int i = 0; i < getHeight(); i++) {
			System.arraycopy(source, sourceOffset + i * sourceWidth, matrix, i * width, width);
		}
	}

	@Override
	public void copyMatrixFromLargerStridedArray(float[] source, int sourceOffset, int sourceStride, int sourceWidth) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				matrix[i * getWidth() + j] = source[sourceOffset + (i * sourceWidth + j)*sourceStride];
			}
		}
	}
	
	@Override
	public float[] getFloatArray() {
		return matrix;
	}

	@Override
	public void copySubMatrixFromLargerArray(float[] buffer, int offset, int subRegionTop, int subRegionLeft, int subRegionHeight,
			int subRegionWidth, int sourceWidth) {
		for (int i = 0; i < subRegionHeight; i++) {
			final int dstLineOffset = (subRegionTop + i) * getWidth();
			final int srcLineOffset = offset + (subRegionTop + i) * sourceWidth;
			for (int j = 0; j < subRegionWidth; j++) {
				matrix[dstLineOffset + subRegionLeft + j] = buffer[srcLineOffset + subRegionLeft + j];
			}
		}
	}

    @Override
    public Matrix copyMatrixRegion(short topRow, short leftColumn, short height, short width, Matrix matrix) {
        MatrixFloat result = null;
        if (!MatrixFloat.class.isInstance(matrix)) {
            result = new MatrixFloat(new float[height * width], height, width, this.getMaxValue());
        } else {
            result = (MatrixFloat)matrix;
            if (result.getHeight() != height || result.getWidth() != width) {
                result = new MatrixFloat(new float[height * width], height, width, this.getMaxValue());
            }
        }
       
        for (short rowIdx = 0; rowIdx < height; rowIdx++) {
            for (short columnIdx = 0; columnIdx < width; columnIdx++) {
                result.matrix[rowIdx * width + columnIdx] = this.matrix[(topRow + rowIdx) * getWidth() + (leftColumn + columnIdx)];
            }
        }        
        
        return result;
    }

    @Override
    public void round() {
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = FastMath.round(matrix[i]);
        }
        
    }
}
