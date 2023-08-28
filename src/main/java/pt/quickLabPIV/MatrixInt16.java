// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.exporter.NotImplementedException;

public class MatrixInt16 extends Matrix {
	private short[] matrix;
	
	public MatrixInt16(final short[] newMatrix, int matrixHeight, int matrixWidth, float maxValue, boolean computeMaxValue) {
        super(matrixHeight, matrixWidth, maxValue);
        if (newMatrix.length != matrixHeight * matrixWidth) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        if (computeMaxValue) {
            maxValue = 0;
            
            for (int i = 0; i < newMatrix.length; i++) {
                int value = newMatrix[i];
                if (value < 0) {
                    value += 65536;
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
        matrix = newMatrix;     
	}
	
	public MatrixInt16(final short[] newMatrix, int matrixHeight, int matrixWidth, float maxValue) {
        super(matrixHeight, matrixWidth, maxValue);
        if (newMatrix.length != matrixHeight * matrixWidth) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        matrix = newMatrix;	    
	}

    public MatrixInt16(final short[] newMatrix, int matrixHeight, int matrixWidth, boolean computeMaxValue) {
        super(matrixHeight, matrixWidth, 65535.0f);
        if (newMatrix.length != matrixHeight * matrixWidth) {
            throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
        }
        if (computeMaxValue) {
            int maxValue = 0;
            
            for (int i = 0; i < newMatrix.length; i++) {
                int value = newMatrix[i];
                if (value < 0) {
                    value += 65536;
                }
                
                if (value > maxValue) {
                    maxValue = value;
                }                
            }
            setMaxValue(maxValue);
        }
        matrix = newMatrix;
    }

	
	public MatrixInt16(final short[] newMatrix, int matrixHeight, int matrixWidth) {
		super(matrixHeight, matrixWidth, 65535.0f);
		if (newMatrix.length != matrixHeight * matrixWidth) {
			throw new MatrixDimensionOverflow("Provided array doesn't match provided matrix geometry");
		}
		matrix = newMatrix;
	}

	@Override
	public float getElement(int i, int j) {
		float value = matrix[i * getWidth() + j];
		
		if (value < 0) {
			value += 65536;
		}
				
		return value;
	}

	@Override
	public void setElement(float value, int i, int j) {
		if (value < 0.0f || value >= 65535.5f) {
			throw new MatrixConversionOverflow("Cannot set the value into a short element matrix, it would overflow");
		}

		matrix[i * getWidth() + j] = (byte)FastMath.round(value);
	}
	
	@Override
	public void setElement(byte value, int i, int j) {
		matrix[i * getWidth() + j] = (short)(value < 0 ? value + 256 : value);
	}
	
	@Override
	public void setElement(short value, int i, int j) {
	    matrix[i * getWidth() + j] = value;
	}
	
    @Override
    public void copyMatrixToArray(byte[] destination, int destinationOffset, float scaleFactor) {
        for (int i = 0; i < matrix.length; i++) {
            float value = matrix[i];
            value *= scaleFactor;
            if (value > 255.5f) {
                throw new MatrixConversionOverflow("Value to large to be represented internally by a byte array");
            }
            destination[destinationOffset + i] = (byte)(value >= 128 ? value - 256 : value);
        }
    }
    
	@Override
	public void copyMatrixToArray(final float[] destination, final int destinationOffset) {
		for (int i = 0; i < matrix.length; i++) {
			int value = matrix[i];
			if (value < 0) {
				value += 65536;
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
					value += 65536;
				}
				destination[dstI + offsetI][dstJ + offsetJ] = value;
			}
		}
	}

	@Override
	public void copyTransposedMatrixToArray(byte[] destination, int destinationOffset) {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				destination[destinationOffset + j*getHeight() + i] = (byte)(matrix[i * getWidth() + j] >> 8);
			}
		}
	}

	@Override
	public void copyMatrixToArray(final byte[] destination, final int destinationOffset) {
        for (int i = 0; i < matrix.length; i++) {
            int value = matrix[i];
            
            if (value < 0 || value > 255) {
                throw new MatrixConversionOverflow("Cannot set the value into a byte element matrix, it would overflow");
            }
            destination[destinationOffset + i] = (byte)value;
        }
	}

    public void copyMatrixToArray(final short[] destination, int destinationOffset) {
        System.arraycopy(matrix, 0, destination, destinationOffset, matrix.length);
    }
	
	@Override
	public void copyMatrixTo2DArray(final float[][] destination, int offsetI, int offsetJ) {
		int li = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				float value = matrix[li++];
				if (value < 0) {
					value += 65536;
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
					value += 65536;
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
	
	@Override
	public void copyMatrixFromArray(byte[] source, int sourceOffset, boolean computeMaxValue) {
		if (source.length < matrix.length) {
			throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
		}
		
		int maxValue = 0;
		for (int i = 0; i < matrix.length; i++) {
		    matrix[i] = (short)(source[i] < 0 ? source[i] + 256 : source[i]);
		    if (computeMaxValue && matrix[i] > maxValue) {
		        maxValue = matrix[i];
		    }
		}
		
		if (computeMaxValue) {
		    if (maxValue < 16) {
		        maxValue = 16;
		    }
		    setMaxValue(maxValue);
		}
	}

    @Override
    public void copyMatrixFromArray(short[] source, int sourceOffset, boolean computeMaxValue) {
        if (source.length < matrix.length) {
            throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
        }

        if (computeMaxValue) {
            int maxValue = 0;
            for (int i = sourceOffset; i < source.length; i++) {
                int value = source[i];
                if (value < 0) {
                    value += 65536;
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
    public void copyMatrixFromArray(float[] source, int sourceOffset, boolean computeMaxValue) {
        if (source.length < matrix.length) {
            throw new IllegalArgumentException("Cannot copy data from source array because it is smaller than matrix");
        }
        
        float maxValue = 0;
        for (int i = 0; i < matrix.length; i++) {
            if (source[i] > maxValue) {
                maxValue = source[i];
            }
        }
        boolean normalize = false;
        if (maxValue > 65535.0f) {
            normalize = true;
        }
        
        for (int i = 0; i < matrix.length; i++) {
            int value = 0;
            if (normalize) {
                value = FastMath.round(source[i] / maxValue * 65535.0f);
            } else {
                value = FastMath.round(source[i]);
            }
            matrix[i] = (short)value;
        }
        
        setMaxValue(maxValue);
    }

    
	@Override
	public void copyMatrixToFloatBytesArray(byte[] destination, int destinationOffset) {
		for (int i = 0; i < matrix.length; i++) {
			int intValue = matrix[i];
			if (intValue < 0) {
				intValue += 65536;
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
					intValue += 65536;
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
		if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
			throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
					" exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
		}
        
		int maxValue = 0;
		int index = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				if (i >= subRegionTop && j >= subRegionLeft && i < subRegionHeight && j < subRegionWidth) {
				    short value = buffer[index++];
				    if (value < 0) {
				        value += 256;
				    }
					matrix[i * getWidth() + j] = value;
					if (computeMaxValue && value > maxValue) {
					    maxValue = value;
					}
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
		}
	}
	
    @Override
    public void copySubMatrixFromArray(short[] buffer, int subRegionTop, int subRegionLeft, int subRegionHeight,
            int subRegionWidth, boolean computeMaxValue) {
        if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
            throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
                    " exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
        }

        int maxValue = 0;
        int index = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (i >= subRegionTop && j >= subRegionLeft && i < subRegionHeight && j < subRegionWidth) {
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
        }
    }

    @Override
    public void copySubMatrixFromArray(float[] buffer, int subRegionTop, int subRegionLeft, int subRegionHeight,
            int subRegionWidth, boolean computeMaxValue) {
        if (subRegionWidth > getWidth() || subRegionHeight > getHeight()) {
            throw new MatrixDimensionOverflow("Sub region dimensions [I: " + (subRegionTop + getHeight()) + ", J: " + (subRegionWidth + getWidth()) + 
                    " exceed matrix dimensions [I: " + subRegionHeight + ", J: " + subRegionWidth + "]");
        }

        float maxValue = Float.MIN_VALUE;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > maxValue) {
                maxValue = buffer[i];
            }
        }
        
        int localMax = 0;
        int index = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (i >= subRegionTop && j >= subRegionLeft && i < subRegionHeight && j < subRegionWidth) {
                    int value = FastMath.round(buffer[index++] / maxValue * 65535.0f);
                    if (computeMaxValue) {
                        if (value > localMax) {
                            localMax = value;
                        }
                    }
                    matrix[i * getWidth() + j] = (short)value;
                } else {
                    matrix[i * getWidth() + j] = 0;
                }
            }
        }
        
        if (computeMaxValue) {
            if (localMax < 16) {
                localMax = 16;
            }
            setMaxValue(localMax);
        }
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
    public Matrix copyMatrixRegion(short topRow, short leftColumn, short height, short width, Matrix matrix) {
        MatrixInt16 result = null;
        if (!MatrixInt16.class.isInstance(matrix)) {
            result = new MatrixInt16(new short[height * width], height, width);
        } else {
            result = (MatrixInt16)matrix;
            if (result.getHeight() != height || result.getWidth() != width) {
                result = new MatrixInt16(new short[height * width], height, width);
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
        // Nothing to do
    }
}
