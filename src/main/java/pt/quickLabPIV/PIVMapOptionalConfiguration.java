package pt.quickLabPIV;

public class PIVMapOptionalConfiguration {

    public static final String IDENTIFIER = "PIVMapOptional";
    
    private final boolean markInvalidAsNaN;
    private final boolean swapUVOrder;
    private final int mapsPerFile;
        
    public PIVMapOptionalConfiguration(boolean _markInvalidAsNaN, boolean _switchUVOrder, int _mapsPerFile) {
        markInvalidAsNaN = _markInvalidAsNaN;
        swapUVOrder = _switchUVOrder;
        mapsPerFile = _mapsPerFile;
    }
    
    public boolean isMarkInvalidAsNaN() {
        return markInvalidAsNaN;
    }
    
    public boolean isSwapUVOrder() {
        return swapUVOrder;
    }
    
    public int getMapsPerFile() {
        return mapsPerFile;
    }
}
