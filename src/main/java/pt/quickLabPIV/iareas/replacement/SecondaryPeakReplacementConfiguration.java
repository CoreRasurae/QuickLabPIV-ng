package pt.quickLabPIV.iareas.replacement;

public class SecondaryPeakReplacementConfiguration {
    public static final String IDENTIFIER = "SECONDARY_PEAK";
    
    private int peakIndex;
    
    public SecondaryPeakReplacementConfiguration(int _peakIndex) {
        peakIndex = _peakIndex;
    }
    
    public int getPeakIndex() {
        return peakIndex;
    }

}