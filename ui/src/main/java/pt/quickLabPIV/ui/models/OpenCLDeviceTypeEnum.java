package pt.quickLabPIV.ui.models;

public enum OpenCLDeviceTypeEnum {
    GPU("GPU"),
    CPU("CPU"), 
    Unknown("Unkown");
    
    private String description;
    
    private OpenCLDeviceTypeEnum(String _description) {
        description = _description;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    public String getDescription() {
        return description;
    }
}
