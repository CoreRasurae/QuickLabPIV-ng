// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

public class OpenCLAssignmentsTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 6666046409888755403L;

    private String columnNames[] = {"<html><center>Thread Index</center></html>",
                                    "<html><center>OpenCL device</center></html>"};
    
    private List<OpenCLDeviceModel> openClModels = Collections.emptyList();
    private List<Integer> indices = Collections.emptyList();
    private List<Long> openClAssignments = Collections.emptyList();
    private List<Boolean> validAssignments = Collections.emptyList();
    
    public void updateSelectedModels(List<OpenCLDeviceModel> _models, List<Integer> _indices) {
        openClModels = _models;
        indices = _indices;
        
        //Check if any assignments were invalidated 
    }
    
    public void updateCpuAssignments(List<Long> _openClAssignments, List<Boolean> _validAssignments) {
        openClAssignments = _openClAssignments;
        validAssignments = _validAssignments;
        
        fireTableDataChanged();
    }    
    
    public List<Boolean> getValidAssignments() {
        return validAssignments;
    }
    
    public List<Long> getOpenClAssignments() {
        return openClAssignments;
    }
    
    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return openClAssignments.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return String.class;
        default:
            return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return false;
        case 1:
            return true;
        default:
            return false;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rowIndex + 1;
        } else if (columnIndex == 1) {
            if (validAssignments.get(rowIndex)) {
                long deviceId = openClAssignments.get(rowIndex);
                int idx = 0;
                for (OpenCLDeviceModel model : openClModels) {
                    if (model.getId() == deviceId) {
                        break;
                    }
                    idx++;
                }
                //
                StringBuilder sb = new StringBuilder(25);
                sb.append(indices.get(idx));
                sb.append(" - ");
                sb.append(openClModels.get(idx).getName());
                return sb.toString();
            } else {
                return "0 - Assignment invalidated";
            }
        }
        
        return null;
    }

    @Override
    public void setValueAt(Object obj, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            String selection = (String)obj;
            long oldDeviceID = openClAssignments.get(rowIndex);
            boolean oldValid = validAssignments.get(rowIndex);
            boolean newValid = false;
            for (int idx = 0; idx < openClModels.size(); idx++) {
                OpenCLDeviceModel model = openClModels.get(idx);
         
                StringBuilder sb = new StringBuilder(25);
                sb.append(indices.get(idx));
                sb.append(" - ");
                sb.append(model.getName());
                
                if (sb.toString().equals(selection)) {
                    long deviceID = model.getId();
                    if (deviceID != oldDeviceID || oldValid == false) {                        
                        newValid = true;
                        openClAssignments.set(rowIndex, deviceID);
                        validAssignments.set(rowIndex, newValid);
                        fireTableCellUpdated(rowIndex, columnIndex);
                    }
                }
            }
            
            if (newValid != oldValid && newValid == false) {
                validAssignments.set(rowIndex, newValid);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
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

    public JComboBox<String> getComboBoxWithOptions() {
        JComboBox<String> cBox = new JComboBox<>();
        cBox.addItem("0 - Assignment Invalidated");
        for (int idx = 0; idx < openClModels.size(); idx++) {
            StringBuilder sb = new StringBuilder(25);
            sb.append(indices.get(idx));
            sb.append(" - ");
            sb.append(openClModels.get(idx).getName());
            
            cBox.addItem(sb.toString());
        }
        
        return cBox;
    }

    public Color getRowForegroundColor(int row) {
        if (validAssignments.get(row)) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }
    
    public Color getRowBackgroundColor(int row) {
        if (validAssignments.get(row)) {
            return Color.WHITE;
        } else {
            return Color.RED;
        }
    }
}
