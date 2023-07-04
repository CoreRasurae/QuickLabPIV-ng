package pt.quickLabPIV.ui.models;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class OpenCLDeviceTableModel extends AbstractTableModel implements PropertyChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 3549362311997045922L;
    
    JDialog parent;

    private List<OpenCLDeviceModel> models = Collections.emptyList();
    
    private String columnNames[] = {"<html><center>Selected<br>for PIV</br></center></html>",
                                    "Index",
                                    "Device Type", 
                                    "Device name", 
                                    "Device ID", 
                                    "<html><center>Number of<br>Compute Units</br></center></html>", 
                                    "<html><center>Threads per<br>Compute Unit</br></center></html>",
                                    "<html><center>Minimum Recommended<br>Thread Multiple</br></center></html>",
                                    "<html><center>Greatest Thread<br>Common Divisor</br></center></html>",
                                    "<html><center>Performance<br>Score</br></center></html>",
                                    "<html><center>Test/Profile<br>device</br></center></html>"};
    
    private List<Color> rowBackgroundColours = Collections.emptyList();
    private List<Color> rowForegroundColours = Collections.emptyList();
    
    public OpenCLDeviceTableModel(JDialog _parent) {
        parent = _parent;
    }
    
    public void setModels(List<OpenCLDeviceModel> _models) {
        unregisterListeners();
        models = _models;
        if (models != null) {
            for (OpenCLDeviceModel model : models) {
                model.registerListener(this);
            }
        }
        updateColors();
        fireTableDataChanged();
    }
    
    public void unregisterListeners() {
        if (models != null) {
            for (OpenCLDeviceModel model : models) {
                model.unregisterListener(this);
            }
        }        
    }
    
    public List<OpenCLDeviceModel> getModels() {
        return models;
    }
    
    public void updateColors() {
        rowBackgroundColours = new ArrayList<Color>(models.size());
        rowForegroundColours = new ArrayList<Color>(models.size());
        for (OpenCLDeviceModel model : models) {            
            if (model.getPerformanceScore() == 0.0f) {
                rowBackgroundColours.add(Color.RED);
                rowForegroundColours.add(Color.WHITE);
            } else if (model.isDefaultConfig()) {
                rowBackgroundColours.add(Color.YELLOW);
                rowForegroundColours.add(Color.BLACK);
            } else {
                rowBackgroundColours.add(Color.GREEN);
                rowForegroundColours.add(Color.BLACK);
            }
        }
    }
    
    public Color getRowBackgroundColor(int row) {
        return rowBackgroundColours.get(row);
    }
    
    public Color getRowForegroundColor(int row) {
        return rowForegroundColours.get(row);
    }
    
    @Override
    public int getColumnCount() {
        return 11;
    }

    @Override
    public int getRowCount() {
        return models.size();
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OpenCLDeviceModel model = models.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return model.isSelected();
        case 1:
            return rowIndex+1;
        case 2:
            return model.getDeviceType();
        case 3:
            return model.getName();
        case 4:
            return model.getId();
        case 5:
            return model.getNumberOfComputeUnits();
        case 6:
            return model.getThreadsPerComputeUnit();
        case 7:
            return model.getMinimumRecommendedThreadMultiple();
        case 8:
            return model.getGreatestThreadCommonDivisor();
        case 9:
            return model.getPerformanceScore();
        case 10:
            return "Test";
        default:
            return null;
        }
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        OpenCLDeviceModel model = models.get(rowIndex);
        switch (columnIndex) {
        case 0:
            boolean selectedState = (Boolean)value;
            if (model.getPerformanceScore() <= 0.0f && selectedState) {
                JOptionPane.showMessageDialog(parent, "Device cannot be selected since it was deemed unsuitable for PIV processing.",
                        "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                model.setSelected((Boolean) value);
            }
            break;
        case 1:
            break;
        case 2:
            model.setDeviceType((OpenCLDeviceTypeEnum)value);
            break;
        case 3:
            model.setName((String) value);
            break;
        case 4:
            model.setId((Long) value);
            break;
        case 5:
            int nrComputeUnits = (Integer)value;
            if (nrComputeUnits <= 0) {
                JOptionPane.showMessageDialog(parent, "Number of compute units must be greater than 0. Keeping old value.",
                                              "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                model.setNumberOfComputeUnits(nrComputeUnits);
            }
            break;
        case 6:
            int threadsPerComputeUnit = (Integer)value;
            if (threadsPerComputeUnit <= 0) {
                JOptionPane.showMessageDialog(parent, "Number threads per compute unit must be greater than 0. Keeping old value.", 
                                              "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;                
            } else {                
                model.setThreadsPerComputeUnit(threadsPerComputeUnit);
            }
            break;
        case 7:
            int minRecommendedThreadMultiple = (Integer)value;
            if (minRecommendedThreadMultiple <= 0) {
                JOptionPane.showMessageDialog(parent, "Minimum recomended thread multiple must be greater than 0. Keeping old value.", 
                                              "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                model.setMinimumRecommendedThreadMultiple(minRecommendedThreadMultiple);
            }
            break;
        case 8:
            int greatestCommonDivisor = (Integer)value;
            if (greatestCommonDivisor <= 0) {
                JOptionPane.showMessageDialog(parent, "Greatest common divisor must be greater than 0. Keeping old value.", 
                                              "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                model.setGreatestThreadCommonDivisor((Integer) value);
            }
            break;
        case 9:
            float oldValue = model.getPerformanceScore();
            float newValue = (float)value;
            if (newValue < 0.0f) {
                JOptionPane.showMessageDialog(parent, "Performance score must be a non-negative value. Keeping old value.", 
                        "Invalid value entered", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            model.setPerformanceScore((Float) value);
            if (oldValue <= 0 && newValue > 0) {
                updateColors();                
                fireTableRowsUpdated(rowIndex, rowIndex);
            } else if (oldValue > 0 && newValue <= 0) {
                model.setSelected(false);
                updateColors();                
                fireTableRowsUpdated(rowIndex, rowIndex);                
            }
            break;
        default:
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Boolean.class;
        case 1:
            return Integer.class;
        case 2:
            return OpenCLDeviceTypeEnum.class;
        case 3:
            return String.class;
        case 4:
            return Long.class;
        case 5:
            return Integer.class;
        case 6:
            return Integer.class;
        case 7:
            return Integer.class;
        case 8:
            return Integer.class;
        case 9:
            return Float.class;
        case 10:
            return String.class;
        default:
            return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return true;
        case 1:
            return false;
        case 2:
            //This is an immutable property
            return false;
        case 3:
            //This is an immutable property -- Unless an user alias is created, however, since OpenCL device IDs are not consistent across reboots... 
            return false;
        case 4:
            //Id cannot be editable as it is a unique identifier
            return false;
        case 5:
            return true;
        case 6:
            return true;            
        case 7:
            return true;
        case 8:
            return true;
        case 9:
            return true;
        case 10:
            return true;
        default:
            //Keeping it safe
            return false;
        }
    }
    
    @Override
    public String getColumnName(int column) {
        if (column < columnNames.length) {
            return columnNames[column];
        } else {
            return "";
        }
    }
    
    @Override 
    public int findColumn(String name) {
        for (int idx = 0; idx < columnNames.length; idx++) {
            if (columnNames[idx].equals(name)) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("performanceScore")) {
            int rowIndex = 0;            
            for (OpenCLDeviceModel model : models) {
                if (model == evt.getSource()) {
                    break;
                }
                rowIndex++;
            }
            
            float oldValue = (float)evt.getOldValue();
            float newValue = (float)evt.getNewValue();
            if (oldValue <= 0 && newValue > 0 || oldValue > 0 && newValue <= 0) {
                updateColors();
                fireTableRowsUpdated(rowIndex, rowIndex);
            } else {
                fireTableCellUpdated(rowIndex, 9);
            }            
        }
    }

    public String getToolTipText(int rowIndex, int colIndex) {
        if (models.size() > 0 && rowIndex < models.size()) {
            OpenCLDeviceModel model = models.get(rowIndex);
            if (model.getPerformanceScore() > 0.0f) {
                if (model.isDefaultConfig()) {
                    return "This device seems okay for PIV processing with QuickLabPIV-ng, however this device is not in QuickLabPIV-ng database";
                } else {
                    return "This device is okay for PIV processing with QuickLabPIV-ng";
                }
            } else {
                return "This device is not suitable for PIV processing, since it is not working reliably with QuickLabPIV-ng";
            }
        }
        return null;
    }
}
