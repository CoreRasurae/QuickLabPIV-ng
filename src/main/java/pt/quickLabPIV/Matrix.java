package pt.quickLabPIV;

import pt.quickLabPIV.exporter.IMatrixExporterVisitor;

public abstract class Matrix {
	private int id;
	private final short width;
	private final short height;
	private float maxValue = 1.0f; //Ensure a maxValue always exists, so that the normalization never fails
	
    public Matrix(final int matrixHeight, final int matrixWidth, float _maxValue) {
        width = (short)matrixWidth;
        height = (short)matrixHeight;
        maxValue = _maxValue;
    }

	
    protected void setMaxValue(float _maxValue) {
        if (_maxValue < 0.0f) {
            throw new MatrixConversionOverflow("Matrix max. value cannot be below 0.0");
        }
        if (_maxValue == 0.0f) {
            _maxValue = 1.0f;
        }
        maxValue = _maxValue;
    }
    
    public float getMaxValue() {
        return maxValue;
    }
    
	/**
	 * Retrieves the matrix width.
	 * @return the number of columns
	 */
	public short getWidth() {
		return width;
	}
	
	/**
	 * Retrieves the matrix height.
	 * @return the number of rows
	 */
	public short getHeight() {
		return height;
	}
	
	/**
	 * Setter for the matrix ID.
	 * @param id to be set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Getter for the matrix ID.
	 * @return matrix ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Exports the current matrix values to the provided exporter.
	 * @param exporterVisitor the exporter instance that will export the matrix data
	 */
	public void exportToFile(final IMatrixExporterVisitor exporterVisitor) {
		exporterVisitor.exportDataToFile(this);
	}
	
	/**
	 * Retrieves the matrix element at row i, column j.
	 * @param i the row index
	 * @param j the column index
	 * @return the element value
	 */
	public abstract float getElement(int i, int j);
	
	/**
	 * Sets the matrix element value at row i, column j.
	 * @param value the value to be set
	 * @param i the row index of the element to be set
	 * @param j the column index of the element to be set
	 */
	public abstract void setElement(float value, int i, int j);
	
	/**
	 * Sets the matrix element value at row i, column j.
	 * @param value the value to be set
	 * @param i the row index of the element to be set
	 * @param j the column index of the element to be set
	 */
	public abstract void setElement(byte value, int i, int j);

	/**
     * Sets the matrix element value at row i, column j.
     * @param value the value to be set
     * @param i the row index of the element to be set
     * @param j the column index of the element to be set
     */
	public abstract void setElement(short value, int i, int j);
	
	/**
	 * Import data into the matrix from a 2-D array of float values (first index is the row, second index is the column).
	 * @param source the source array 
	 * @param offsetI the I offset at which the values should start to be copied
	 * @param offsetJ the J offset at which the values should start to be copied
	 * @throws MatrixConversionOverflow if conversion cannot be performed without overflowing
	 */
	public abstract void copyMatrixFrom2DArray(final float[][] source, final int offsetI, final int offsetJ);
	
	/**
	 * Copies the content of a Matrix to a 2-D array of float values. <br/> 
	 * The target array can be larger than the original array.
	 * @param destination the destination array
	 * @param offsetI the offsetI in the destination array
	 * @param offsetJ the offsetJ in the destination array
	 */
	public abstract void copyMatrixTo2DArray(final float[][] destination, final int offsetI, final int offsetJ);

    /**
     * Copies the content of a Matrix to a 2-D array of float values and normalize and offset. <br/> 
     * The target array can be larger than the original array.
     * @param destination the destination array
     * @param offsetI the offsetI in the destination array
     * @param offsetJ the offsetJ in the destination array
     */ 
    public void copyMatrixTo2DArrayAndNormalizeAndOffset(final float[][] destination, int offsetI, int offsetJ) {
        for (int srcI = 0, dstI = 0; srcI < getHeight(); srcI++, dstI++) {
            for (int srcJ = 0, dstJ = 0; srcJ < getWidth(); srcJ++, dstJ++) {
                float value = getElement(srcI, srcJ);
                destination[dstI + offsetI][dstJ + offsetJ] = value / getMaxValue() * 16.0f + 1.0f;
            }
        }
    }
	
	/**
	 * Copies the content of a Matrix to a 2-D array of float values in mirrored order, both in I and J. <br/> 
	 * The target array can be larger than the original array.
	 * @param destination the destination array
	 * @param offsetI the offsetI in the destination array
	 * @param offsetJ the offsetJ in the destination array
	 */
	public abstract void copyMirroredMatrixTo2DArray(final float[][] source, final int offsetI, int offsetJ);

	/**
	 * Copies the content of a Matrix to a 1-D array of float values that is ordered row by row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 */
	public abstract void copyMatrixToArray(final float[] destination, final int destinationOffset);

	/**
	 * Copies the content of a Matrix to a 1-D array of float values that is ordered row by row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 * @param destinationWidth the number of columns in the destination matrix (must be equal or greater to 
	 * the number of columns in the source matrix) 
	 */
	public void copyMatrixToArray(final float[] destination, final int destinationOffset, final int destinationWidth) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				destination[destinationOffset + i * destinationWidth + j] = getElement(i, j);
			}
		}
	}

    public void copyMatrixToArrayAndNormalizeAndOffset(final float[] destination, final int destinationOffset, final int destinationWidth) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                destination[destinationOffset + i * destinationWidth + j] = getElement(i, j) / getMaxValue() * 16.0f + 1.0f;
            }
        }
    }

	
	/**
	 * 
	 * @param array the interleaved array
	 * @param offset absolute offset accounting with stride* 255.0f
	 * @param stride the stride value
	 * @param arrayWidth the width of the array without accounting for the stride
	 */
	public void copyMatrixToStridedArray(float[] array, int offset, int stride, int arrayWidth) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				array[offset + (i * arrayWidth + j)*stride] = getElement(i, j);
			}
		}		
	}

	/**
	 * Copies the content of a Matrix to a 1-D array of unsigned 8-bit values that is ordered row by row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 * @throws MatrixConversionOverflow if conversion cannot be performed without overflowing
	 */
	public abstract void copyMatrixToArray(final byte[] destination, final int destinationOffset);

	public abstract void copyMatrixToArray(final byte[] destination, final int destinationOffset, final float scaleFactor);
	
	public abstract void copyMatrixToArray(short[] destination, int destinationOffset);
	
	/*** 255.0f
	 * Copies the content of a Matrix to a 1-D array of float values that is ordered block row by block row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 * @param destinationWidth the number of columns in the destination matrix (must be equal or greater to 
	 * the number of columns in the source matrix) 
	 * @param blockWidth the width of the block
	 * @param blockHeight the height of the block
	 */
	public void copyMatrixToBlockArray(final float[] destination, final int destinationOffset, final int destinationWidth,
			final int blockWidth, final int blockHeight) {
		int lineOffset = destinationOffset;
		for (int i = 0, blockOffsetI = 0; i < height; i++, blockOffsetI++) {			
			if (blockOffsetI == blockHeight) {
				blockOffsetI = 0;
				lineOffset += destinationWidth * blockHeight;
			}
			
			int blockOffset = lineOffset + blockOffsetI * blockWidth;
			
			for (int j = 0, blockOffsetJ = 0; j < destinationWidth; j++, blockOffsetJ++) {				
				if (blockOffsetJ == blockWidth) {
					blockOffsetJ = 0;
					blockOffset += blockHeight*blockWidth;
				}		
				
				if (j < width) {
					destination[blockOffset + blockOffsetJ] = getElement(i, j);
				}
			}			
		}
	}
	
	/**
	 * Copies the content of a Matrix to a 1-D array of bytes in IEEE 754 format in LSB first order row by row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 */
	public abstract void copyMatrixToFloatBytesArray(final byte[] destination, final int destinationOffset);
	
	/**
     * Copies the content of a Matrix to a 1-D array of unsigned 8-bit values that is ordered column by column.
     * @param destination the destination array
     * @param destinationOffset the offset at which the transposed values should start to be copied
     * @throws MatrixConversionOverflow if conversion cannot be performed without overflowing
     */
	public abstract void copyTransposedMatrixToArray(final byte[] destination, final int destinationOffset);
	
	/**
	 * Copies the content of a Matrix to a 1-D array of bytes in IEEE 754 format in LSB first order column by column. 
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the transposed values should start to be copied
	 */
	public abstract void copyTransposedMatrixToFloatBytesArray(final byte[] destination, final int destinationOffset);

	/**
	 * Copies the content of a Matrix to a 1-D array of floats starting at the specified offset and accounting
	 * for a destination having a different geometry.
	 * @param destination the destination array
	 * @param offset the offset at which the copy is intended to start
	 * @param destinationWidth the number of columns in the destination matrix (must be equal or greater to 
	 * the number of columns in the source matrix)
	 */
	public void copyMirroredMatrixToArray(float[] destination, int offset, int destinationWidth) {
		for (int srcI = height - 1, dstI = 0; srcI >= 0; srcI--, dstI++) {
			for (int srcJ = width - 1, dstJ = 0; srcJ >= 0; srcJ--, dstJ++) {
				destination[offset + dstI * destinationWidth + dstJ] = getElement(srcI, srcJ);
			}
		}
	}

    public void copyMirroredMatrixToArrayNormalizeAndOffset(float[] destination, int offset, int destinationWidth) {
        for (int srcI = height - 1, dstI = 0; srcI >= 0; srcI--, dstI++) {
            for (int srcJ = width - 1, dstJ = 0; srcJ >= 0; srcJ--, dstJ++) {
                destination[offset + dstI * destinationWidth + dstJ] = getElement(srcI, srcJ) / getMaxValue() * 16.0f + 1.0f;
            }
        }
    }

	
	public void copyMirroredMatrixToStridedArray(float[] array, int offset, int stride, int destinationWidth) {
		for (int srcI = height - 1, dstI = 0; srcI >= 0; srcI--, dstI++) {
			for (int srcJ = width - 1, dstJ = 0; srcJ >= 0; srcJ--, dstJ++) {
				array[offset + (dstI * destinationWidth + dstJ) * stride] = getElement(srcI, srcJ);
			}
		}		
	}

	/**
	 * Copies the content of a Matrix to a 1-D array of float values that is ordered block row by block row.
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied
	 * @param destinationWidth the number of columns in the destination matrix (must be equal or greater to 
	 * the number of columns in the source matrix) 
	 * @param blockWidth the width of the block
	 * @param blockHeight the height of the block
	 */
	public void copyMirroredMatrixToBlockArray(final float[] destination, final int destinationOffset, final int destinationWidth,
			final int blockWidth, final int blockHeight) {
		int lineOffset = destinationOffset;
		for (int i = 0, blockOffsetI = 0; i < height; i++, blockOffsetI++) {			
			if (blockOffsetI == blockHeight) {
				blockOffsetI = 0;
				lineOffset += destinationWidth * blockHeight;
			}
			
			int blockOffset = lineOffset + blockOffsetI * blockWidth;
			
			for (int j = 0, blockOffsetJ = 0; j < destinationWidth; j++, blockOffsetJ++) {				
				if (blockOffsetJ == blockWidth) {
					blockOffsetJ = 0;
					blockOffset += blockHeight*blockWidth;
				}		
				
				if (j < width) {
					destination[blockOffset + blockOffsetJ] = getElement((height-1) - i, (width - 1) - j);
				}
			}			
		}
	}
	
	/**
	 * Copies the content of a Matrix to a 1-D array of float values that is ordered row by row and has
	 * one extra column on the left side and an extra row at the top, both used for padding purposes. 
	 * @param destination the destination array
	 * @param destinationOffset the offset at which the values should start to be copied not accounting for
	 *  the padding offsets for the present   
	 */
	public abstract void copyMatrixTo1PaddedArray(final float[] destination, final int destinationOffset);
		
	/**
	 * Imports data into the matrix from an array of 1-D floats that is ordered row by row.
	 * @param source the source array
	 * @param sourceOffset the offset from which the values should be sequentially imported
	 * @throws MatrixConversionOverflow if conversion cannot be performed without overflowing
	 */
	public abstract void copyMatrixFromArray(final float[] source, final int sourceOffset);

	/**
	 * Import data into the matrix from an 1-D array of unsigned 8-bit values that is ordered row by row.
	 * @param source the source array
	 * @param sourceOffset the offset from which the values should be sequentially imported
	 */
	public abstract void copyMatrixFromArray(byte[] source, int sourceOffset, boolean computeMax);

	/**
     * Import data into the matrix from an 1-D array of unsigned 16-bit values that is ordered row by row.
     * @param source the source array
     * @param sourceOffset the offset from which the values should be sequentially imported
     */
    public abstract void copyMatrixFromArray(short[] buffer, int sourceOffset, boolean computeMax);

    /**
     * Import data into the matrix from an 1-D array of unsigned 16-bit values that is ordered row by row.
     * @param source the source array
     * @param sourceOffset the offset from which the values should be sequentially imported
     */
    public abstract void copyMatrixFromArray(float[] buffer, int sourceOffset, boolean computeMax);
    
	/**
	 * Imports data into the matrix from an array of 1-D floats that is ordered row by row.
	 * @param source the source array
	 * @param sourceOffset the offset from which the values should be sequentially imported
	 * @param sourceWidth the number of columns in the source matrix (can be greater or equal to 
	 * the matrix respective dimensions)
	 * @throws MatrixConversionOverflow if conversion cannot be performed without overflowing
	 */
	public abstract void copyMatrixFromLargerArray(float[] source, int sourceOffset, int sourceWidth);
	
	/**
	 * Imports data into the matrix from an array of 1-D floats that is ordered row by row.
	 * @param source the source array
	 * @param sourceOffset the offset from which the data is to be imported accounting with stride and minor offset
	 * @param sourceStride the stride of the source array
	 * @param sourceWidth the number of columns in the source matrix without stride (can be greater or equal to 
	 * the matrix respective dimensions)
	 */
	public abstract void copyMatrixFromLargerStridedArray(float[] source, int sourceOffset, int sourceStride, int sourceWidth);

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
	public abstract void copySubMatrixFromArray(final byte[] buffer, int subRegionTop, int subRegionLeft, int subRegionHeight, int subRegionWidth, boolean computeMaxValue);

	/**
     * Import data into the matrix from a 1-D array of unsigned 16-bit values that is ordered row by row, but
     * corresponds to a sub-region of this matrix. The 1-D array can be smaller in dimensions than the matrix.
     * @param source the 1-D source buffer 
     * @param subRegionTop the sub-region top margin at which the values should start to be copied
     * @param subRegionLeft the sub-region left margin at which the values should start to be copied
     * @param subRegionHeight the sub-region height
     * @param subRegionWidth the sub-region width
     * @throws MatrixDimensionOverflow if sub-matrix doesn't fit in Matrix dimensions.
     */
    public abstract void copySubMatrixFromArray(short[] buffer, int subRegionTop, int subRegionLeft,
            int subRegionHeight, int subRegionWidth, boolean computeMaxValue);

    /**
     * Import data into the matrix from a 1-D array of float values that is ordered row by row, but
     * corresponds to a sub-region of this matrix. The 1-D array can be smaller in dimensions than the matrix.
     * @param source the 1-D source buffer 
     * @param subRegionTop the sub-region top margin at which the values should start to be copied
     * @param subRegionLeft the sub-region left margin at which the values should start to be copied
     * @param subRegionHeight the sub-region height
     * @param subRegionWidth the sub-region width
     * @throws MatrixDimensionOverflow if sub-matrix doesn't fit in Matrix dimensions.
     */
    public abstract void copySubMatrixFromArray(float[] buffer, int subRegionTop, int subRegionLeft,
            int subRegionHeight, int subRegionWidth, boolean computeMaxValue);

	
	public abstract void copySubMatrixFromLargerArray(final float[] buffer, int offset, 
					int subRegionTop, int subRegionLeft, int subRegionHeight, int subRegionWidth,
					int sourceWidth);

	/*public void copyMatrixFromLargerBlockArray(float[] sourceArray, int sourceOffset, int sourceWidth, final int blockWidth, final int blockHeight) {
		final int blocksPerMatrixLine = sourceWidth / blockWidth;
		final int blockIncrement = blockWidth * blockHeight;
		final int blockLineIncrement = blockIncrement * blocksPerMatrixLine;
		for (int blockIndexI = 0, blockLineStartOffset = sourceOffset; blockIndexI < blocksPerMatrixLine; blockIndexI++, blockLineStartOffset += blockLineIncrement) {
			for (int blockIndexJ = 0, blockStartOffset = blockLineStartOffset; blockIndexJ < blocksPerMatrixLine; blockIndexJ++, blockStartOffset += blockIncrement) {
				for (int indexI = 0, innerLineOffset = blockStartOffset; indexI < blockHeight; indexI++, innerLineOffset += blockWidth) {
					for (int indexJ = 0; indexJ < blockWidth; indexJ++) {
						final int i = blockIndexI * blockHeight + indexI;
						final int j = blockIndexJ * blockWidth + indexJ;
						if (i < height && j < width) {
							setElement(sourceArray[innerLineOffset + indexJ], i, j);
						}
					}
				}
			}
		}
	}*/
	
	public void copyMatrixFromLargerBlockArray(float[] sourceArray, int sourceOffset, int sourceWidth, final int blockWidth, final int blockHeight) {
		int lineOffset = sourceOffset;
		for (int i = 0, blockOffsetI = 0; i < height; i++, blockOffsetI++) {			
			if (blockOffsetI == blockHeight) {
				blockOffsetI = 0;
				lineOffset += sourceWidth * blockHeight;
			}
			
			int blockOffset = lineOffset + blockOffsetI * blockWidth;
			
			for (int j = 0, blockOffsetJ = 0; j < sourceWidth; j++, blockOffsetJ++) {				
				if (blockOffsetJ == blockWidth) {
					blockOffsetJ = 0;
					blockOffset += blockHeight*blockWidth;
				}		
				
				if (j < width) {
					setElement(sourceArray[blockOffset + blockOffsetJ], i, j);
				}
			}			
		}
	}

	/**
	 * Create a Matrix of the same type as the original one from a region of the original matrix. 
	 * @param topRow the top row of the region to copy
	 * @param leftColumn the left column of the region to copy
	 * @param height the number of rows to copy
	 * @param width the number of column to copy
	 * @param matrix an existing matrix instance of the compatible type, to avoid instantiation
	 * @return the matrix with the sub-region
	 */
    public abstract Matrix copyMatrixRegion(short topRow, short leftColumn, short height, short width, Matrix matrix);
	
	/**
	 * Zeroes all the matrix values.
	 */
	public void zeroMatrix() {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				setElement((byte)0, i, j);
			}
		}
		
	}

	public abstract float[] getFloatArray();


    public abstract void round();


    public void computeMaxValue() {
        float maxValueLocal = 0.0f;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float value = getElement(i, j);
                if (value > maxValueLocal) {
                    maxValueLocal = value;
                }
            }
        }
        
        maxValue = maxValueLocal;
    }
}
