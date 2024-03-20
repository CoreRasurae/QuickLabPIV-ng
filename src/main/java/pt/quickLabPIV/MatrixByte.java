// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.exporter.NotImplementedException;

public class MatrixByte extends Matrix {
	private byte[] matrix;

    public MatrixByte(final byte[] newMatrix, int matrixHeight, int matrixWidth, boolean computeMax) {
        super(matrixHeight, matrixWidth, 255.0f);
        int maxValue = 0;
        if (computeMax) {            
            for (int i = 0; i < newMatrix.length; i++) {
                int element = newMatrix[i];
                if (element < 0) {
                   element += 256;
                }
                if (element > maxValue) {
                   maxValue = element; 
                }
            }

            if (maxValue < 16) {
                maxValue = 16;
            }
            setMaxValue(maxValue);
        }
        if (newMatrix.length != matrixHeight * matrixWidth) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        matrix = newMatrix;
    }

	
	public MatrixByte(final byte[] newMatrix, int matrixHeight, int matrixWidth) {
		super(matrixHeight, matrixWidth, 255.0f);
		if (newMatrix.length != matrixHeight * matrixWidth) {
			throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
		}
		matrix = newMatrix;
	}

	@Override
	public float getElement(int i, int j) {
		int value = matrix[i * getWidth() + j];
		
		if (value < 0) {
			value += 256;
		}
		
		return value;
	}

	@Override
	public void setElement(float value, int i, int j) {
		if (value < 0.0f || value >= 255.5f) {
			throw new MatrixConversionOverflow("Cannot set the value into a byte element matrix, it would overflow");
		}

		matrix[i * getWidth() + j] = (byte)FastMath.round(value);
	}
	
	@Override
	public void setElement(byte value, int i, int j) {
		matrix[i * getWidth() + j] = value;
	}
	
    @Override
    public void setElement(short value, int i, int j) {
        if (value < 0.0f || value >= 255.5f) {
            throw new MatrixConversionOverflow("Cannot set the value into a byte element matrix, it would overflow");
        }

        matrix[i * getWidth() + j] = (byte)FastMath.round(value);
    }
    
	@Override
	public void copyMatrixToArray(final float[] destination, final int destinationOffset) {
		for (int i = 0; i < matrix.length; i++) {
			int value = matrix[i];
			if (value < 0) {
				value += 256;
			}
			destination[destinationOffset + i] = value;
		}
	}

    @Override
    public void copyMatrixToArray(final byte[] destination, final int destinationOffset) {
        System.arraycopy(matrix, 0, destination, destinationOffset, matrix.length);
    }

    @Override
    public void copyMatrixToArray(byte[] destination, int destinationOffset, float scaleFactor) {
        for (int i = 0; i < matrix.length; i++) {
            float value = matrix[i];
            if (value < 0) {
                value += 256;
            }
            value *= scaleFactor;
            if (value > 255.5f) {
                throw new MatrixConversionOverflow("Value to large to be represented internally by a byte array");
            }
            destination[destinationOffset + i] = (byte)(value >= 128 ? value - 256 : value);
        }
    }
    
    @Override
    public void copyMatrixToArray(short[] destination, int destinationOffset) {
        for (int i = 0; i < matrix.length; i++) {
            short value = matrix[i];
            if (value < 0) {
                value += 256;
            }
            destination[destinationOffset + i] = value;
        }
    }

	@Override
	public void copyMirroredMatrixTo2DArray(float[][] destination, int offsetI, int offsetJ) {
		for (int srcI = getHeight() - 1, dstI = 0; srcI >= 0; srcI--, dstI++) {
			for (int srcJ = getWidth() - 1, dstJ = 0; srcJ >= 0; srcJ--, dstJ++) {
				int value = matrix[srcI * getWidth() + srcJ];
				if (value < 0) {
					value += 256;
				}
				destination[dstI + offsetI][dstJ + offsetJ] = value;
			}
		}
	}

	@Override
	public void copyTransposedMatrixToArray(byte[] destination, int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				destination[destinationOffset + j*getHeight() + i] = matrix[i * getWidth() + j];
			}
		}
	}
	
	@Override
	public void copyMatrixTo2DArray(final float[][] destination, int offsetI, int offsetJ) {
		int li = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				int value = matrix[li++];
				if (value < 0) {
					value += 256;
				}

				destination[offsetI + i][offsetJ + j] = value; 
			}
		}
	}

	@Override
	public void copyMatrixTo1PaddedArray(final float[] destination, int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			destination[destinationOffset + i * (getWidth() + 1)] = 0.0f;
		}
		for (int j = 0; j < getWidth(); j++) {
			destination[destinationOffset + j] = 0.0f;
		}
		int sourceIndex = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				int value = matrix[sourceIndex++];
				if (value < 0) {
					value += 256;
				}
				destination[destinationOffset + (i+1) * (getWidth() + 1) + (j+1)] = value;
			}
		}		
	}

	@Override
	public void copyMatrixFromArray(float[] source, int sourceOffset) {
		throw new NotImplementedException("Method not implemented");
	}

	@Override
	public void copyMatrixFrom2DArray(float[][] source, int offsetI, int offsetJ) {
		throw new NotImplementedException("Method not implemented");
	}
	   /**
     * Import data into the matrix from a 1-D array of unsigned 8-bit values that is ordered row by row, but
     * corresponds to a sub-region of this matrix. The 1-D array can be smaller in dimensions than the matrix.
     * @param source the 1-D source buffer 
     * @param subRegionTop the sub-region top margin at which the values should start to be copied
     * @param subRegionLeft the sub-region left margin at which the values should start to be copied
     * @param subRegionHeight the sub-region height
     * @param subRegionWidth the sub-region width
     * @throws MatrixDimensionOverflow if sub-matrix doesn't fit in Matrix dimensions.
     */

	@Override
	public void copyMatrixFromArray(byte[] source, int sourceOffset, boolean computeMaxValue) {	    
		if (source.length < matrix.length) {
			throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
		}
		
		if (computeMaxValue) {
		    int maxValue = 0;
		    for (int i = sourceOffset; i < source.length; i++) {
		        int value = source[i];
		        if (value < 0) {
		            value += 256;
		        }
		        
		        if (value > maxValue) {
		            maxValue = value;
		        }
		    }
		    if (maxValue < 16) {
		        maxValue = 16;
		    }
		    setMaxValue(maxValue);
		}
		
		System.arraycopy(source, sourceOffset, matrix, 0, matrix.length);
	}
	
    @Override
    public void copyMatrixFromArray(short[] buffer, int sourceOffset, boolean computeMaxValue) {
        throw new NotImplementedException("Method not implemented");
    }
	
    @Override
    public void copyMatrixFromArray(float[] buffer, int sourceOffset, boolean computeMax) {
        throw new NotImplementedException("Method not implemented");
    }

    
	@Override
	public void copyMatrixToFloatBytesArray(byte[] destination, int destinationOffset) {
		for (int i = 0; i < matrix.length; i++) {
			int intValue = matrix[i];
			if (intValue < 0) {
				intValue += 256;
			}
			float value = intValue;
			int ieeeValue = Float.floatToIntBits(value);
			
			destination[destinationOffset + i*Float.BYTES] = (byte)(ieeeValue & 0x0ff);
			destination[destinationOffset + i*Float.BYTES + 1] = (byte)(ieeeValue >>> 8);
			destination[destinationOffset + i*Float.BYTES + 2] = (byte)(ieeeValue >>> 16);
			destination[destinationOffset + i*Float.BYTES + 3] = (byte)(ieeeValue >>> 24);
		}
	}

	@Override
	public void copyTransposedMatrixToFloatBytesArray(byte[] destination, int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				int intValue = matrix[i * getWidth() + j];
				if (intValue < 0) {
					intValue += 256;
				}
				float value = intValue;
				int ieeeValue = Float.floatToIntBits(value);
				
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES] = (byte)(ieeeValue & 0x0ff);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 1] = (byte)(ieeeValue >>> 8);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 2] = (byte)(ieeeValue >>> 16);
				destination[destinationOffset + j*getHeight()*Float.BYTES + i*Float.BYTES + 3] = (byte)(ieeeValue >>> 24);
			}
		}
	}

	@Override
	public void copySubMatrixFromArray(byte[] buffer, int subRegionTop, int subRegionLeft,
			int subRegionHeight, int subRegionWidth, boolean computeMaxValue) {
	    int maxValue = 0;
		if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
			throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
					" exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
		}

		int index = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				if (i >= subRegionTop && j >= subRegionLeft && i < subRegionHeight && j < subRegionWidth) {
				    if (computeMaxValue) {
				        int value = buffer[index];
				        if (value < 0) {
				           value += 256;
				        }
				        
				        if (value > maxValue) {
				           maxValue = value; 
				        }
				    }
					matrix[i * getWidth() + j] = buffer[index++];
				} else {
					matrix[i * getWidth() + j] = 0;
				}
			}
		}
		
		if (computeMaxValue) {
		    if (maxValue < 16) {
		        maxValue = 16;
		    }
		    setMaxValue(maxValue); 
		} else {
		    setMaxValue(255.0f);
		}
	}

    @Override
    public void copySubMatrixFromArray(float[] buffer, int subRegionTop, int subRegionLeft, int subRegionHeight,
            int subRegionWidth, boolean computeMaxValue) {
        int maxValue = 0;
        if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
            throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
                    " exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
        }

        float bufferMaxValue = Float.MIN_VALUE; 
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > bufferMaxValue) {
                bufferMaxValue = buffer[i];
            }
        }
        
        int index = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (i >= subRegionTop && j >= subRegionLeft && i < subRegionHeight && j < subRegionWidth) {
                    int value = FastMath.round(buffer[index] / bufferMaxValue * 255.0f);
                    if (computeMaxValue) {
                        if (value < 0) {
                           value += 256;
                        }
                        
                        if (value > maxValue) {
                           maxValue = value; 
                        }
                    }
                    matrix[i * getWidth() + j] = (byte)(value & 0x0ff);
                } else {
                    matrix[i * getWidth() + j] = 0;
                }
            }
        }
        
        if (computeMaxValue) {
            if (maxValue < 16) {
                maxValue = 16;
            }
            setMaxValue(maxValue); 
        } else {
            setMaxValue(255.0f);
        }
    }

	
    @Override
    public Matrix copyMatrixRegion(short topRow, short leftColumn, short height, short width, Matrix matrix) {
        MatrixByte result = null;
        if (!MatrixByte.class.isInstance(matrix)) {
            result = new MatrixByte(new byte[height * width], height, width);
        } else {
            result = (MatrixByte)matrix;
            if (result.getHeight() != height || result.getWidth() != width) {
                result = new MatrixByte(new byte[height * width], height, width);
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
    public void copySubMatrixFromArray(short[] buffer, int marginTop, int marginLeft, int adjustedHeight,
            int adjustedWidth, boolean computeMaxValue) {        
        throw new NotImplementedException("Method not implemented");
    }


	@Override
	public void copyMatrixFromLargerArray(float[] source, int sourceOffset, int sourceWidth) {
		throw new NotImplementedException("Method not implemented");
	}

	@Override
	public float[] getFloatArray() {
		throw new NotImplementedException("Method not implemented");
	}

	@Override
	public void copyMatrixFromLargerStridedArray(float[] source, int sourceOffset, int sourceStride, int sourceWidth) {
		throw new NotImplementedException("Method not implemented");
	}

	@Override
	public void copySubMatrixFromLargerArray(float[] buffer, int offset, int subRegionTop, int subRegionLeft,
			int subRegionHeight, int subRegionWidth, int sourceWidth) {
		throw new NotImplementedException("Method not implemented");
		
	}

    @Override
    public void round() {
        //Nothing to do
        
    }
}
