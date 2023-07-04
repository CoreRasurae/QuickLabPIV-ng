package pt.quickLabPIV.exporter;

/**
 * Common optional configuration properties for all exporters.
 * 
 * @author lpnm
 */
public class GenericExporterOptionalConfiguration {
    private final boolean markInvalidAsNaN;
    
    public GenericExporterOptionalConfiguration(boolean _markInvalidAsNaN) {
        markInvalidAsNaN = _markInvalidAsNaN;
    }
    
    public boolean isMarkInvalidAsNaN() {
        return markInvalidAsNaN;
    }
}
