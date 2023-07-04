package pt.quickLabPIV.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defines a full MATLAB Level 5 arbitrary matrix exporter 
 * 
 * @author lpnm
 */
public abstract class MatlabLevel5Matrix extends MatlabLevel5Element {
	protected class SparseDimensionIndices {
		public int sparseIndicesType;
		public int sparseIndicesSize;
		public int[] sparseIndices; //Must include padding
		
		public int getBytesLength() {
			return 4 + 4 + sparseIndices.length;
		}
	}
	
	public final class MatrixConfiguration {
		int[] dimensions;
		MatlabMxTypesEnum matrixClass;
		List<Matlab5ArrayFlags> flags;
		
		public MatrixConfiguration() {
			flags = new ArrayList<Matlab5ArrayFlags>();
		}
		
		private byte computeFlags() {
			byte flagsByte = 0x00;
			for (Matlab5ArrayFlags flag : flags) {
				flagsByte |= flag.getFlagValue();
			}
			
			return flagsByte;
		}
	}

	final byte computeFlags(Collection<Matlab5ArrayFlags> flags) {
		byte flagsByte = 0x00;
		for (Matlab5ArrayFlags flag : flags) {
			flagsByte |= flag.getFlagValue();
		}
		
		return flagsByte;
	}
		
	//Helper data fields
	private MatrixConfiguration config;
	
	//Tag data
	private int dataType = MatlabMiTypesEnum.miMATRIX.getId();
	private long numberOfBytes = 0;
	//Element data
	//MATLAB variable element with minimum 56-byte header
	private int arrayFlagsType;
	private int arrayFlagsSize;
	private int arrayFlags;
	private int arrayNzMaxValue;
	private int dimensionsArrayType;
	private int dimensionsArraySize;
	private int[] dimensionsArray; //including padding to 8-byte
	private int arrayNameType;
	private int arrayNameSize;
	private byte[] arrayName;
	
	//Struct field names
	private short fieldNameBytes;
	private short fieldNameType;
	private int fieldNameSize;
	private int fieldNamesLengthType;
	private int fieldNamesLengthSize;
	private byte[] fieldNames;
	
	//Sparse data elements field
	private SparseDimensionIndices[] sparseDimensions;
	
	//Real array contents
	private int realValuesType = 0;
	private long realValuesSize;
	private List<byte[]> realValues = new ArrayList<>(1);
	//Imaginary array contents
	private int imaginaryValuesType;
	private long imaginaryValuesSize;
	private List<byte[]> imaginaryValues = new ArrayList<>(1);
	
	protected MatlabLevel5Matrix(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, MatlabMiTypesEnum storageType, 
			Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName) {
		super(parent);
		
		//Pre-create storage type
		if (!arrayClass.isMiTypeAccepted(storageType)) {
			throw new ExportFailedException("Incompatible storage type: " + storageType.name() + " for given array class: " + arrayClass.name());
		}
		
		boolean isComplex = false;
		if (newArrayFlags.contains(Matlab5ArrayFlags.Complex)) {
			isComplex = true;
		}

		config = new MatrixConfiguration();
		config.dimensions = dimensions;
		config.flags.addAll(newArrayFlags);
		config.matrixClass = arrayClass;
		
		computeStorageTypesFieldsAndArray(arrayClass, storageType, dimensions, isComplex, 0, 0);
		
		initMatrix(parent, arrayClass, computeFlags(newArrayFlags), dimensions, newArrayName);
	}
		
	
	protected MatlabLevel5Matrix(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName) {
		super(parent);
		
		//Storage types fields and array will be computed inside initMatrix when no specific initialization has been made before
		config = new MatrixConfiguration();
		config.dimensions = dimensions;
		config.flags.addAll(newArrayFlags);
		config.matrixClass = arrayClass;

		initMatrix(parent, arrayClass, computeFlags(newArrayFlags), dimensions, newArrayName);
	}

	protected MatlabLevel5Matrix(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName,
			int additionalElementsCount) {
		super(parent);

		boolean isComplex = false;
		if (newArrayFlags.contains(Matlab5ArrayFlags.Complex)) {
			isComplex = true;
		}

		config = new MatrixConfiguration();
		config.dimensions = dimensions;
		config.flags.addAll(newArrayFlags);
		config.matrixClass = arrayClass;
		
		computeStorageTypesFieldsAndArray(arrayClass, arrayClass.getDefaultMiType(), dimensions, isComplex, 0, additionalElementsCount);
		
		initMatrix(parent, arrayClass, computeFlags(newArrayFlags), dimensions, newArrayName);
	}

	
	private void computeStorageTypesFieldsAndArray(final MatlabMxTypesEnum matrixClass, final MatlabMiTypesEnum storageType, 
			final int[] dimensions, final boolean isComplex, final int userDefinedTypeSize, final int additionalElementsCount) {
		realValuesType = storageType.getId();
		int overallDimensions = 1;
		for (int dimensionSize : dimensions) {
			overallDimensions *= dimensionSize;
		}

		if (matrixClass != MatlabMxTypesEnum.mxSTRUCT_CLASS && storageType.getSize() == 0 && userDefinedTypeSize == 0) {
			//Is Matrix of Matrices allowed? Looks like so... Matrix of Objects or Matrix of structures
			//At least compressed types could be allowed, but should only be created in the end...
			throw new ExportFailedException("Types with unknown storage size aren't supported yet");
		}
		
		int typeSize = storageType.getSize();
		if (storageType.getSize() == 0) {
			typeSize = userDefinedTypeSize;
		}

		//If matrix is a sparse matrix then the maximum number of elements can be lower than the 
		//matrix dimensions allow.
		if (matrixClass == MatlabMxTypesEnum.mxSPARSE_CLASS) {
			//So Overall dimensions are now dictated by the number of non-zero values in the sparse matrix
			overallDimensions = additionalElementsCount;
			
			arrayNzMaxValue = additionalElementsCount;
			
			int sparsePadding = additionalElementsCount % 2;
			sparseDimensions = new SparseDimensionIndices[dimensions.length];
			for (SparseDimensionIndices indexObj : sparseDimensions) {
				indexObj.sparseIndicesType = MatlabMiTypesEnum.miINT32.getId();
				indexObj.sparseIndicesSize = additionalElementsCount * MatlabMiTypesEnum.miINT32.getSize();
				indexObj.sparseIndices = new int[additionalElementsCount + sparsePadding];
			}
			
		} else {
			arrayNzMaxValue = 0;
		}
		
		if (matrixClass != MatlabMxTypesEnum.mxCELL_CLASS &&
			matrixClass != MatlabMxTypesEnum.mxOBJECT_CLASS &&
			matrixClass != MatlabMxTypesEnum.mxSTRUCT_CLASS) {
			long overallSize = overallDimensions * typeSize;
			if (overallSize > MAX_UINT32) {
			    throw new ExportFailedException("Matrix size does not fit in a 32-bit unsigned sized array");
			}
			
			int padding = (int)(overallSize % (long)8);
			if (padding != 0) {
				padding = 8 - padding;
			}
			
			realValuesSize =  overallSize;
			
			long remaining = overallSize + padding;
			while (remaining > 0) {
			    if (remaining >= Integer.MAX_VALUE) {
			        byte page[] = new byte[Integer.MAX_VALUE];
			        realValues.add(page);
			        remaining -= Integer.MAX_VALUE;
			    } else {
			        byte page[] = new byte[(int)remaining];
			        realValues.add(page);
			        remaining = 0;
			    }
			    
			}
			
			if (isComplex) {
				imaginaryValuesType = storageType.getId();
				
				imaginaryValuesSize = overallSize;
				
	            remaining = overallSize + padding;
	            while (remaining > 0) {
	                if (remaining >= Integer.MAX_VALUE) {
	                    byte page[] = new byte[Integer.MAX_VALUE];
	                    imaginaryValues.add(page);
	                    remaining -= Integer.MAX_VALUE;
	                } else {
	                    byte page[] = new byte[(int)remaining];
	                    imaginaryValues.add(page);
	                    remaining = 0;
	                }
	                
	            }
			}
		}
	}
	
	private void initMatrix(MatlabLevel5Element parent, MatlabMxTypesEnum arrayClass, byte newArrayFlags, int[] dimensions, String newArrayName) {
		if (arrayClass.getDefaultMiType() == null && realValuesType == 0) {
			throw new ExportFailedException("Unknown default storage type for array class: " + arrayClass.name());
		}
		
		//Setup array type field
		arrayFlagsType = MatlabMiTypesEnum.miUINT32.getId();
		arrayFlagsSize = 8;
		
		this.arrayFlags &= ~0x0ff;
		this.arrayFlags |= arrayClass.getId();
	
		byte maskedFlags = (byte)(newArrayFlags & 0x70);
		this.arrayFlags &= ~(0x0ff << 8);
		this.arrayFlags |= maskedFlags << 8;

		//Setup dimensions array type field and value
		dimensionsArrayType = MatlabMiTypesEnum.miINT32.getId();
		dimensionsArraySize = dimensions.length * 4;
		
		int dimensionsPadding = 0;
		if (dimensions.length % 2 != 0) {
			dimensionsPadding = 1;
		}
		dimensionsArray = new int[dimensions.length + dimensionsPadding];
		for (int index = 0; index < dimensions.length; index++) {
			dimensionsArray[index] = dimensions[index];
		}
	
		//Setup array name type field and value
		byte[] localNameArray = null;
		try {
			localNameArray = newArrayName.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ExportFailedException("Couldn't export array name", e);
		}
		
		arrayNameType = MatlabMiTypesEnum.miINT8.getId();
		arrayNameSize = localNameArray.length;
	

		int namePadding = localNameArray.length % 8;
		if (namePadding != 0) {
			namePadding = 8 - namePadding;
		}
		arrayName = new byte[localNameArray.length + namePadding];
		System.arraycopy(localNameArray, 0, arrayName, 0, localNameArray.length);
		
		boolean isComplex = false;
		if ((arrayFlags & Matlab5ArrayFlags.Complex.getFlagValue()) != 0) {
			isComplex = true;
		}
		
		//Create content storage space and initialize types
		if (realValuesType == 0) {			
			computeStorageTypesFieldsAndArray(arrayClass, arrayClass.getDefaultMiType(), dimensions, isComplex, 0 ,0);
		}		
	}
	
	public MatrixConfiguration getConfiguration() {
		return config;
	}
	
	public boolean isComplex() {
		return (arrayFlags & Matlab5ArrayFlags.Complex.getFlagValue()) != 0;
	}
	
	protected void setFieldNames(String[] newFieldNames) {
		if (config.matrixClass != MatlabMxTypesEnum.mxSTRUCT_CLASS) {
			throw new ExportFailedException("Setting field names is only allowed on Struct matrices");
		}

		int length = 0;
		for (String fieldName : newFieldNames) {
			if (fieldName.length() > length) {
				length = fieldName.length();
			}
		}
		
		if (length + 1 > 32) {
			throw new ExportFailedException("Cannot export data due to too long field names: " + (length + 1) + " chars");
		}
				
		fieldNameBytes = (short)MatlabMiTypesEnum.miINT32.getSize();
		fieldNameType = (short)MatlabMiTypesEnum.miINT32.getId();
		fieldNameSize = length+1;
		fieldNamesLengthType = MatlabMiTypesEnum.miINT8.getId();
		fieldNamesLengthSize = newFieldNames.length * fieldNameSize;
		
		int padding = fieldNamesLengthSize % 8;
		if (padding != 0) {
			padding = 8 - padding;
		}
		
		fieldNames = new byte[fieldNamesLengthSize + padding];
		
		for (int index = 0; index < newFieldNames.length; index++) {
			byte[] nameBytes;
			try {
				nameBytes = newFieldNames[index].getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new ExportFailedException("Couldn't encode name to UTF-8: " + newFieldNames[index], e);
			}
			System.arraycopy(nameBytes, 0, fieldNames, index*fieldNameSize, nameBytes.length);
			
			fieldNames[index * fieldNameSize + nameBytes.length] = '\0'; 
		}
	}
	
	protected SparseDimensionIndices[] getSparseDimensions() {
		if (arrayNzMaxValue == 0) {
			throw new InvalidStateException("Matrix is not a sparse matrix");
		}
		
		return sparseDimensions;
	}
	
	protected List<byte[]> getRealValues() {
		return realValues;
	}

	protected List<byte[]> getImaginaryValues() {
		if (!isComplex()) {
			throw new InvalidStateException("Matrix is not a Complex matrix");
		}
		
		return imaginaryValues;
	}
	
	@Override
	long computeUpdatedElementNumberOfBytes() {
		long bytes = 8 + 8 + 8 + dimensionsArray.length*4;
		bytes += 8 + arrayName.length;

		if (arrayNzMaxValue > 0) {
			for (SparseDimensionIndices indexObj : sparseDimensions) {
				bytes += indexObj.getBytesLength();
			}
		}

		if (config.matrixClass != MatlabMxTypesEnum.mxCELL_CLASS &&
			config.matrixClass != MatlabMxTypesEnum.mxSTRUCT_CLASS &&
			config.matrixClass != MatlabMxTypesEnum.mxOBJECT_CLASS) {
			bytes += 8;
			for (byte[] realPage : realValues) {
			    bytes += realPage.length;
			}
			if (isComplex()) {
				bytes += 8;
	            for (byte[] imaginaryPage : imaginaryValues) {
	                bytes += imaginaryPage.length;
	            }
			}
		}
		
		if (config.matrixClass == MatlabMxTypesEnum.mxSTRUCT_CLASS) {
			bytes += 8 + 8 + fieldNames.length;
		}
		
		return bytes;
	}
	
	@Override
	long getBytesLength() {
		if (numberOfBytes == 0) {
			numberOfBytes = computeUpdatedElementNumberOfBytes();
		}
		return numberOfBytes + 8;  //8 bytes from Matrix tag header...
	}
	
	@Override
	public void writeToOuputStream(FileOutputStream fos) {
		MatlabLevel5Element parentChainedElement = getChainedElement();
		
		getBytesLength();
		if (parentChainedElement != null) {
			parentChainedElement.writeToOuputStream(fos);
		}
		
		//TODO improve performance by using FileChannel and ByteBuffer
		try {
			writeInt(fos, dataType);
			writeUInt32(fos, numberOfBytes);
			writeInt(fos, arrayFlagsType);
			writeInt(fos, arrayFlagsSize);
			writeInt(fos, arrayFlags);
			writeInt(fos, arrayNzMaxValue);
			writeInt(fos, dimensionsArrayType);
			writeInt(fos, dimensionsArraySize);
			for (int dimension : dimensionsArray) {
				writeInt(fos, dimension);
			}
			if (arrayName != null) {
				writeInt(fos, arrayNameType);
				writeInt(fos, arrayNameSize);
				fos.write(arrayName);
			}
			
			if (arrayNzMaxValue > 0) {
				for (SparseDimensionIndices indexObj : sparseDimensions) {
					writeInt(fos, indexObj.sparseIndicesType);
					writeInt(fos, indexObj.sparseIndicesSize);
					for (int sparseIndex : indexObj.sparseIndices) {
						writeInt(fos, sparseIndex);
					}
				}
			}
		
			if (config.matrixClass == MatlabMxTypesEnum.mxSTRUCT_CLASS) {
				writeInt(fos, fieldNameBytes << 16 | fieldNameType);
				writeInt(fos, fieldNameSize);
				writeInt(fos, fieldNamesLengthType);
				writeInt(fos, fieldNamesLengthSize);
				fos.write(fieldNames);
			}
			
			if (config.matrixClass != MatlabMxTypesEnum.mxOBJECT_CLASS && 
				config.matrixClass != MatlabMxTypesEnum.mxSTRUCT_CLASS &&
				config.matrixClass != MatlabMxTypesEnum.mxCELL_CLASS) {
				writeInt(fos, realValuesType);
				writeUInt32(fos, realValuesSize);
				for (byte[] realPage : realValues) {
				    fos.write(realPage);
				}
				if (isComplex()) {
					writeInt(fos, imaginaryValuesType);
					writeUInt32(fos, imaginaryValuesSize);
	                for (byte[] imaginaryPage : imaginaryValues) {
	                    fos.write(imaginaryPage);
	                }
				}
			}
		} catch (IOException e) {
			throw new ExportFailedException("Failed to export data to file", e);
		}
	}

}
