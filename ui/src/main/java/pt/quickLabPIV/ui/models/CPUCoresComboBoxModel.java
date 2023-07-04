package pt.quickLabPIV.ui.models;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class CPUCoresComboBoxModel implements ComboBoxModel<Integer> {

    private int maxCpuCores;
    private int selectedItem;
    
    public CPUCoresComboBoxModel(int _maxCpuCores) {
        maxCpuCores = _maxCpuCores;
    }
    
    @Override
    public void addListDataListener(ListDataListener listener) {
        
    }

    @Override
    public Integer getElementAt(int index) {
        return index + 1;
    }

    @Override
    public int getSize() {
        return maxCpuCores;
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(Object _selectedItem) {
        selectedItem = (Integer)_selectedItem;
    }

}
