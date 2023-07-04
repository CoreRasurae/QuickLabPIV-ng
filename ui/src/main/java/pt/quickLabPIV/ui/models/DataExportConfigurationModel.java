package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DataExportConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataExportConfigurationModel {
    private boolean swapUVOrder = false;
    private boolean splitExports = false;
    private int numberOfPIVMapsPerExportedFile = 0;
    
    public void setSwapUVOrder(boolean swap) {
        swapUVOrder = swap;
    }
    
    public boolean isSwapUVOrder() {
        return swapUVOrder;
    }
    
    public void setSplitExports(boolean split) {
        splitExports = split;
    }
    
    public boolean isSplitExports() {
        return splitExports;
    }
    
    public void setNumberOfPIVMapsPerExportedFile(int value) {
        numberOfPIVMapsPerExportedFile = value;
    }
    
    public int getNumberOfPIVMapsPerExportedFile() {
        return numberOfPIVMapsPerExportedFile;
    }

    public boolean isChanged(DataExportConfigurationModel another) {
        if (another == null) {
            return true;
        }
        
        if (swapUVOrder != another.swapUVOrder) {
            return true;
        }
        
        if (splitExports != another.splitExports) {
            return true;
        }
        
        if (numberOfPIVMapsPerExportedFile != another.numberOfPIVMapsPerExportedFile) {
            return true;
        }
        
        return false;
    }

    public DataExportConfigurationModel copy() {
        DataExportConfigurationModel copy = new DataExportConfigurationModel();
        
        copy.swapUVOrder = swapUVOrder;
        copy.splitExports = splitExports;
        copy.numberOfPIVMapsPerExportedFile = numberOfPIVMapsPerExportedFile;
        
        return copy;
    }
}
