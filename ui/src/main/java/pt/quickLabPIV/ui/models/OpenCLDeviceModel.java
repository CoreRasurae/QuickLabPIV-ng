// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public class OpenCLDeviceModel {
    @XmlTransient
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private OpenCLDeviceTypeEnum deviceType;
    private String name;
    private long   id;
    private float  perfScore = 1.0f;
    private boolean defaultConfig;
    private int numberOfComputeUnits;
    private int threadsPerComputeUnit;
    private int minThreadMultiple;
    private int greatestThreadCommonDivisor;
    private boolean selected;
    
    public void setDeviceType(OpenCLDeviceTypeEnum _deviceType) {
        deviceType = _deviceType;
    }
    
    public OpenCLDeviceTypeEnum getDeviceType() {
        return deviceType;
    }

    public void setName(String deviceName) {
        name = deviceName;
    }

    public String getName() {
        return name;
    }
    
    public void setId(long deviceId) {
        id = deviceId;
    }
    
    public long getId() {
        return id;
    }

    public void setPerformanceScore(float score) {
        float oldScore = perfScore;        
        perfScore = score;
        if (perfScore <= 0.0f) {
            setSelected(false);
        }
        pcs.firePropertyChange("performanceScore", oldScore, score);
    }
    
    public float getPerformanceScore() {
        return perfScore;
    }
    
    public void setDefaultConfig(boolean _defaultConfig) {
        defaultConfig = _defaultConfig;
    }
    
    public boolean isDefaultConfig() {
        return defaultConfig;
    }

    public void setNumberOfComputeUnits(int _numberOfComputeUnits) {
        numberOfComputeUnits = _numberOfComputeUnits;
    }
    
    public int getNumberOfComputeUnits() {
        return numberOfComputeUnits;
    }

    public void setThreadsPerComputeUnit(int _threadsPerComputeUnit) {
        threadsPerComputeUnit = _threadsPerComputeUnit;
    }
    
    public int getThreadsPerComputeUnit() {
        return threadsPerComputeUnit;
    }

    public void setMinimumRecommendedThreadMultiple(int _minThreadMultiple) {
        minThreadMultiple = _minThreadMultiple;
    }
    
    public int getMinimumRecommendedThreadMultiple() {
        return minThreadMultiple;
    }
    
    public void setGreatestThreadCommonDivisor(int _greatestThreadCommonDivisor) {
        greatestThreadCommonDivisor = _greatestThreadCommonDivisor;
    }
    
    public int getGreatestThreadCommonDivisor() {
        return greatestThreadCommonDivisor;
    }
    
    /**
     * Set device selection state.
     * @param _selected <ul><li>true, device will be used for PIV processing with OpenCL</li>
     *                      <li>false, device will not be involved in the PIV processing</li></ul>
     */
    public void setSelected(boolean _selected) {
        selected = _selected;
    }
    
    /**
     * Check if device has been selected for PIV processing with OpenCL
     * @return the selection state.
     */
    public boolean isSelected() {
        return selected;
    }
    
    public boolean isCompatible(OpenCLDeviceModel other) {
        return other.id == id || name.equals(other.name);
    }

    public void registerListener(PropertyChangeListener _listener) {
        pcs.addPropertyChangeListener(_listener);
    }
    
    public void unregisterListener(PropertyChangeListener _listener) {
        pcs.removePropertyChangeListener(_listener);
    }
    
    public OpenCLDeviceModel copy() {
        OpenCLDeviceModel model = new OpenCLDeviceModel();
     
        model.deviceType = deviceType;
        model.name = name;
        model.id = id;
        model.defaultConfig = defaultConfig;
        model.numberOfComputeUnits = numberOfComputeUnits;
        model.threadsPerComputeUnit = threadsPerComputeUnit;
        model.minThreadMultiple = minThreadMultiple;
        model.greatestThreadCommonDivisor = greatestThreadCommonDivisor;
        model.perfScore = perfScore;
        model.selected = selected;
        
        return model;
    }

}
