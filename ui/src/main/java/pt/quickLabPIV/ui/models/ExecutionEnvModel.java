// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import pt.quickLabPIV.business.transfer.ExecutionEnvConverterVisitor;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutionEnvModel {
    @XmlTransient
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @XmlTransient
    private AppContextModel context;

    private int cpuThreads = 1;
    private boolean enableOpenCL = true;
    
    @XmlElementWrapper(name = "OpenCL-Devices")
    @XmlElements (
        { 
            @XmlElement(name="OpenCL-Device", type=OpenCLDeviceModel.class),
        }
    )
    private List<OpenCLDeviceModel> openClDevices = new ArrayList<OpenCLDeviceModel>();
    
    private List<Long> openClAssignments = new ArrayList<Long>();
    private List<Boolean> validAssignments = new ArrayList<Boolean>();
   
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void setCpuThreads(int _cpuThreads) {
       int oldValue = cpuThreads;
       cpuThreads = _cpuThreads;
       pcs.firePropertyChange("cpuThreads", oldValue, cpuThreads);
    }
    
    public int getCpuThreads() {
        return cpuThreads;
    }
    
    public void setEnableOpenCL(boolean _enable) {
        boolean oldValue = enableOpenCL;
        enableOpenCL = _enable;
        pcs.firePropertyChange("enableOpenCL", oldValue, enableOpenCL);
    }
    
    public boolean isEnableOpenCL() {
        return enableOpenCL;
    }
    
    public void setOpenClDevices(List<OpenCLDeviceModel> devices) {
        List<OpenCLDeviceModel> oldDevices = openClDevices;
        openClDevices = devices;
        pcs.firePropertyChange("openClDevices", oldDevices, openClDevices);
    }
    
    public List<OpenCLDeviceModel> getOpenClDevices() {
        return openClDevices;
    }
        
    public void setParent(AppContextModel appContextModel) {
        context = appContextModel;        
    }

    public List<Long> getOpenClAssignments() {
        return openClAssignments;
    }
    
    public void setOpenClAssignments(List<Long> _openClAssignments, List<Boolean> _validAssignments) {
        openClAssignments = _openClAssignments;
        validAssignments = _validAssignments;
    }
    
    public List<Boolean> getValidAssignments() {
        return validAssignments;
    }

    
    public ExecutionEnvModel copy() {
        ExecutionEnvModel model = new ExecutionEnvModel();
        
        model.context = context;
        model.cpuThreads = cpuThreads;
        model.enableOpenCL = enableOpenCL;
        model.openClDevices = copyOpenClDevices(openClDevices);
        model.openClAssignments = copyOpenClAssignments(openClAssignments);
        model.validAssignments = copyValidAssignments(validAssignments);
        
        return model;
    }

    private List<Boolean> copyValidAssignments(List<Boolean> _validAssignments) {
        List<Boolean> copyList = new ArrayList<>(_validAssignments.size());
        copyList.addAll(_validAssignments);
        return copyList;
    }

    private List<OpenCLDeviceModel> copyOpenClDevices(List<OpenCLDeviceModel> _openClDevices) {
        List<OpenCLDeviceModel> copyList = new ArrayList<OpenCLDeviceModel>(_openClDevices.size());
        
        for (OpenCLDeviceModel model : _openClDevices) {
            copyList.add(model.copy());
        }
        
        return copyList;
    }
    
    private List<Long> copyOpenClAssignments(List<Long> _openClAssignments) {
        List<Long> copyList = new ArrayList<Long>(_openClAssignments.size());
        
        for (Long value : _openClAssignments) {
            copyList.add(value);
        }
        
        return copyList;
    }

    public void accept(IExecutionEnvVisitor execEnvConverter) {
        execEnvConverter.visit(this);
    }
}
