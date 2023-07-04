package pt.quickLabPIV.maximum;

public class FindMaximumMultiPeaksConfiguration {
	public final static String IDENTIFIER = "MAX_MULTI_PEAKS_CFG";
	
	private int numberOfPeaks;
	private int kernelWidth;
	
	public FindMaximumMultiPeaksConfiguration(int _numberOfPeaks, int _kernelWidth) {
		numberOfPeaks = _numberOfPeaks;
		kernelWidth = _kernelWidth;
	}
	
	public int getNumberOfPeaks() {
		return numberOfPeaks;
	}
	
	public int getKernelWidth() {
		return kernelWidth;
	}
}
