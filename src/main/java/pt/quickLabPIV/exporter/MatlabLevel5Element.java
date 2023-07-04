package pt.quickLabPIV.exporter;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provides necessary interface for proper chaining of Matlab Level 5 data structures in an single chained manner.
 * <br/><b>NOTES:</b> <ul><li>Please note that all MATLAB Level 5 elements are fairly independent between them, except inner data elements
 * which have to report their sizes to the container element, however there is no global file size. New data elements can be appended
 * to the MATLAB file without having to update any of the previously written data.</li>
 * <li>All variables including simples ones are written as MATLAB matrices with dimensions 1x1.</li></ul>
 * @author lpnm
 *
 */
abstract class MatlabLevel5Element {
     static final long MAX_UINT32 = 4294967295L;
    
	private MatlabLevel5Element parentChainedElement;
	
	/**
	 * Creates a new MatlabLevel5 data element instance.
	 * @param chainedElement the MatlabLevel5 data element to which this new instance should be chained to. 
	 */
	MatlabLevel5Element(MatlabLevel5Element chainedElement) {
		this.parentChainedElement = chainedElement;
	}
	
	/**
	 * Retrieves the parent data element to which the current instance is chained to. 
	 * @return <ul><li>the data element to which the current data element is chained</li>
	 * <li>null, if instance has no chained element</li></ul>
	 */
	MatlabLevel5Element getChainedElement() {
		return parentChainedElement;
	}
	
	/**
	 * Computes the current element total bytes excluding Matlab Level 5 start tag.
	 * <b>NOTE:</b> This method is to be always called before calling writeToOutputStream(...)
	 * @return the number of bytes.
	 */
	abstract long computeUpdatedElementNumberOfBytes();
	
	/**
	 * Getter that return the total bytes count for the current Matlab Level 5 element including inner data data elements if existing,
	 * plus MATLAB Level 5 start element tags size.
	 * <b>NOTE:</b> This method must also update the instance state with the internal number of bytes excluding the start element tags size.
	 * @return the total bytes count
	 */	
	abstract long getBytesLength();
	
	/**
	 * Writes Matlab data Element to the output stream.
	 * @param fos the file stream to write to
	 */
	public abstract void writeToOuputStream(FileOutputStream fos);
	
	protected void writeShort(FileOutputStream fos, short value) throws IOException {
		fos.write(value);
		fos.write(value >>> 8);
	}
	
	protected void writeUInt32(FileOutputStream fos, long value) throws IOException {
	    if (value > MAX_UINT32) {
	        throw new ExportFailedException("Number to large to fit inside a 32-bit unsigned");
	    }
		fos.write((int)(value & 0x0ff));
		fos.write((int)((value >>> 8)  & 0x0ff));
		fos.write((int)((value >>> 16) & 0x0ff));
		fos.write((int)((value >>> 24) & 0x0ff));
	}

   protected void writeInt(FileOutputStream fos, int value) throws IOException {
        fos.write(value & 0x0ff);
        fos.write(value >>> 8);
        fos.write(value >>> 16);
        fos.write(value >>> 24);
    }

	/**
	 * Allows element to inform inner elements that a write/export is about to occur, so that they can
	 * update all relevant fields that may still be uninitialized due to the dynamic usage behavior of the elements,
	 * where additional data and fields can be added during code execution, before export starts.
	 */
	protected void prepareForWriting() {
		
	}
}
