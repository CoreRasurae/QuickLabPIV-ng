// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.exporter;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pt.quickLabPIV.Matrix;

public class MatlabLevel5UnnamedMatrix extends MatlabLevel5Matrix {
	final class MatlabLevel5ContextSetter {
		private final MatlabLevel5UnnamedMatrix matrix;
		private long currentIndex;
		private final List<byte[]> values;
		private long totalSize = -1L;
		private long currentOffset = -1L;
		private int pageIndex;
		private int pageSize;

		private long getTotalSize() {
		    if (totalSize == -1L) {
		        totalSize = 0;
		        for (byte[] page : values) {
		            totalSize += page.length;
		        }
		    }
		    
		    return totalSize;
		}
		
		private void writeByteAtPageIndex(long writeIndex, byte value) {
		    int indexInPage = 0;
		    long totalOffset = 0;
		    
		    if (currentOffset < 0 || writeIndex < currentOffset) {
		        pageIndex = 0;
    		    for (byte[] page : values) {
    		        if (writeIndex >= totalOffset + page.length) {
    		            totalOffset += page.length;
    		            pageIndex++;
    		            continue;
    		        }
    		        currentOffset = totalOffset;
    		        pageSize = page.length;
    		        indexInPage = (int)(writeIndex - totalOffset);
    		        page[indexInPage] = value;
    		        break;
    		    }
		    } else if (writeIndex < currentOffset + pageSize) {
		        indexInPage = (int)(writeIndex - currentOffset);
		        values.get(pageIndex)[indexInPage] = value;
		    } else {
		        totalOffset = currentOffset;
                for (; pageIndex < values.size(); pageIndex++) {
                    byte page[] = values.get(pageIndex);
                    if (writeIndex >= totalOffset + page.length) {
                        totalOffset += page.length;
                        continue;
                    }
                    currentOffset = totalOffset;
                    pageSize = page.length;
                    indexInPage = (int)(writeIndex - totalOffset);
                    page[indexInPage] = value;
                    break;
                }		        
		    }
		    
		}
		
		public void setValueAndIncrementIndex(float value) {
			int bits = Float.floatToIntBits(value);
			writeByteAtPageIndex(currentIndex++, (byte)bits);
			writeByteAtPageIndex(currentIndex++, (byte)(bits >> 8));
			writeByteAtPageIndex(currentIndex++, (byte)(bits >> 16));
			writeByteAtPageIndex(currentIndex++, (byte)(bits >> 24));
		}
		
		private MatlabLevel5ContextSetter(final MatlabLevel5UnnamedMatrix matrix, int frameNumber) {
			this.matrix = matrix;
			int[] dimensions = matrix.getConfiguration().dimensions;
			values = matrix.getRealValues();
			int typeSize = matrix.getConfiguration().matrixClass.getDefaultMiType().getSize();
			currentIndex = frameNumber * typeSize * dimensions[0] * dimensions[1];
			if (currentIndex < 0 || currentIndex > getTotalSize()) {
				throw new ExportFailedException("Invalid frame number was specified");
			}
		}
		
        private MatlabLevel5ContextSetter(final MatlabLevel5UnnamedMatrix matrix, int[] indicesContext) {
            this.matrix = matrix;
            int[] dimensions = matrix.getConfiguration().dimensions;
            values = matrix.getRealValues();
            int typeSize = matrix.getConfiguration().matrixClass.getDefaultMiType().getSize();
            
            currentIndex = 0;
            int currentIndexLevel = 0;
            for (int indexContext : indicesContext) {
                int offset = 1;
                for (int i = 0; i < dimensions.length - indicesContext.length + currentIndexLevel; i++) {
                    offset *= dimensions[i];
                }
                offset *= typeSize;
                currentIndex += indexContext * offset;
                currentIndexLevel++;
            }            
            if (currentIndex < 0 || currentIndex > getTotalSize()) {
                throw new ExportFailedException("Invalid index context specified");
            }
        }
	}
	
	public MatlabLevel5UnnamedMatrix(MatlabLevel5Element parentChainedElement, MatlabMxTypesEnum arrayClass,
			MatlabMiTypesEnum storageType, Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions) {
		super(parentChainedElement, arrayClass, storageType, newArrayFlags, dimensions, "");
	}

	public MatlabLevel5UnnamedMatrix(MatlabLevel5Element parentChainedElement, MatlabMxTypesEnum arrayClass, 
			Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions) {
		super(parentChainedElement, arrayClass, newArrayFlags, dimensions, "");
	}
		
	public static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5UnnamedMatrix parentChainedElement, Float value) {
		int[] dims = {1,1};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxSINGLE_CLASS, 
													Collections.emptyList(), dims);
		
		int intBits = Float.floatToIntBits(value);
	
		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)intBits;
		targetStorage[1] = (byte)(intBits >> 8);
		targetStorage[2] = (byte)(intBits >> 16);
		targetStorage[3] = (byte)(intBits >> 24);
		
		return element;
	}

	public static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5UnnamedMatrix parentChainedElement, MatlabLevel5Struct struct) {
		int[] dims = {1,1};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxSTRUCT_CLASS, 
													Collections.emptyList(), dims);
	
		return element;
	}
	
	public static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5Element parentChainedElement, int value) {
		int[] dims = {1,1};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxINT32_CLASS, 
													Collections.emptyList(), dims);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		targetStorage[1] = (byte)(value >> 8);
		targetStorage[2] = (byte)(value >> 16);
		targetStorage[3] = (byte)(value >> 24);
		
		return element;	
	}
	
	static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5Element parentChainedElement, short value) {
		int[] dims = {1,1};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxINT16_CLASS, 
													Collections.emptyList(), dims);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		targetStorage[1] = (byte)(value >> 8);
		
		return element;	
	}

	static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5Element parentChainedElement, byte value) {
		int[] dims = {1,1};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxINT8_CLASS, 
													Collections.emptyList(), dims);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		
		return element;	
	}

	static MatlabLevel5UnnamedMatrix createUnnamedVariable(MatlabLevel5Element parentChainedElement, String text) {
		int[] dims = {1, text.length()};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxCHAR_CLASS, MatlabMiTypesEnum.miUTF8, 
													Collections.emptyList(), dims);
		byte[] textBytes = null;
		try {
			textBytes = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ExportFailedException("Cannot convert text to UTF-8", e);
		}
		byte[] targetStorage = element.getRealValues().get(0);
		System.arraycopy(textBytes, 0, targetStorage, 0, textBytes.length);
		
		return element;	
	}
	
	static MatlabLevel5UnnamedMatrix createUnnamedFloatArrayVariable(MatlabLevel5Element parentChainedElement, Matrix m) {
		int[] dims = {m.getHeight(), m.getWidth()};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxSINGLE_CLASS, 
													Collections.emptyList(), dims);

		byte[] targetStorage = element.getRealValues().get(0);
		
		m.copyTransposedMatrixToFloatBytesArray(targetStorage, 0);
		
		return element;
	}
	
	static MatlabLevel5UnnamedMatrix createUnnamedUnsingByteArrayVariable(MatlabLevel5Element parentChainedElement, Matrix m) {
		int[] dims = {m.getHeight(), m.getWidth()};
		MatlabLevel5UnnamedMatrix element = new MatlabLevel5UnnamedMatrix(parentChainedElement, MatlabMxTypesEnum.mxUINT8_CLASS, 
													Collections.emptyList(), dims);

		byte[] targetStorage = element.getRealValues().get(0);
		
		m.copyTransposedMatrixToArray(targetStorage, 0);
		
		return element;
	}
	
	public MatlabLevel5ContextSetter createMultiFrameIndexContext(int frameNumber) {
		MatlabLevel5ContextSetter setter = new MatlabLevel5ContextSetter(this, frameNumber);
		return setter;
	}
	
	public MatlabLevel5ContextSetter createMultiDimensionalIndexContext(int[] indexContext) {
	    MatlabLevel5ContextSetter setter = new MatlabLevel5ContextSetter(this, indexContext);
	    return setter;
	}
}
