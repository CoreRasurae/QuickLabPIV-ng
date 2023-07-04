package pt.quickLabPIV.exporter;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

public class MatlabLevel5SimpleVariable extends MatlabLevel5Matrix {

	protected MatlabLevel5SimpleVariable(MatlabLevel5Element parentChainedElement, MatlabMxTypesEnum arrayClass,
			MatlabMiTypesEnum storageType, Collection<Matlab5ArrayFlags> newArrayFlags, int[] dimensions, String newArrayName) {
		super(parentChainedElement, arrayClass, storageType, newArrayFlags, dimensions, newArrayName);
	}

	protected MatlabLevel5SimpleVariable(MatlabLevel5Element parentChainedElement, MatlabMxTypesEnum arrayClass, Collection<Matlab5ArrayFlags> newArrayFlags,
			int[] dimensions, String newArrayName) {
		super(parentChainedElement, arrayClass, newArrayFlags, dimensions, newArrayName);
	}
	
	public static MatlabLevel5SimpleVariable createNamedVariable(MatlabLevel5Element parentChainedElement, String name, float value) {
		int[] dims = {1,1};
		MatlabLevel5SimpleVariable element = new MatlabLevel5SimpleVariable(parentChainedElement, MatlabMxTypesEnum.mxSINGLE_CLASS, 
													Collections.emptyList(), dims, name);
		
		int intBits = Float.floatToIntBits(value);
	
		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)intBits;
		targetStorage[1] = (byte)(intBits >> 8);
		targetStorage[2] = (byte)(intBits >> 16);
		targetStorage[3] = (byte)(intBits >> 24);
		
		return element;
	}
	
	public static MatlabLevel5SimpleVariable createNamedVariable(MatlabLevel5Element parentChainedElement, String name, int value) {
		int[] dims = {1,1};
		MatlabLevel5SimpleVariable element = new MatlabLevel5SimpleVariable(parentChainedElement, MatlabMxTypesEnum.mxINT32_CLASS, 
													Collections.emptyList(), dims, name);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		targetStorage[1] = (byte)(value >> 8);
		targetStorage[2] = (byte)(value >> 16);
		targetStorage[3] = (byte)(value >> 24);
		
		return element;	
	}
	
	public static MatlabLevel5SimpleVariable createNamedVariable(MatlabLevel5Element parentChainedElement, String name, short value) {
		int[] dims = {1,1};
		MatlabLevel5SimpleVariable element = new MatlabLevel5SimpleVariable(parentChainedElement, MatlabMxTypesEnum.mxINT16_CLASS, 
													Collections.emptyList(), dims, name);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		targetStorage[1] = (byte)(value >> 8);
		
		return element;	
	}

	public static MatlabLevel5SimpleVariable createNamedVariable(MatlabLevel5Element parentChainedElement, String name, byte value) {
		int[] dims = {1,1};
		MatlabLevel5SimpleVariable element = new MatlabLevel5SimpleVariable(parentChainedElement, MatlabMxTypesEnum.mxINT8_CLASS, 
													Collections.emptyList(), dims, name);

		byte[] targetStorage = element.getRealValues().get(0);
		targetStorage[0] = (byte)value;
		
		return element;	
	}

	public static MatlabLevel5SimpleVariable createNamedVariable(MatlabLevel5Element parentChainedElement, String name, String text) {
		int[] dims = {1, text.length()};
		MatlabLevel5SimpleVariable element = new MatlabLevel5SimpleVariable(parentChainedElement, MatlabMxTypesEnum.mxCHAR_CLASS, MatlabMiTypesEnum.miUTF8, 
													Collections.emptyList(), dims, name);
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
}
