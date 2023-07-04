package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
@XmlSeeAlso({VelocityStabilizationOptionsMaxDisplacementModel.class})
public abstract class VelocityStabilizationOptionsModel {
    @XmlTransient
    private PIVConfigurationModel parent;
    
    @XmlTransient
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private VelocityStabilizationModeEnum stabilizationMode = VelocityStabilizationModeEnum.Disabled;
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public PIVConfigurationModel getParent() {
        return parent;
    }

    public void setParent(PIVConfigurationModel model) {
        parent = model;
    }
    
    public VelocityStabilizationModeEnum getStabilizationMode() {
        return stabilizationMode;
    }
    
    protected void setStabilizationMode(VelocityStabilizationModeEnum mode) {
        stabilizationMode = mode;
    }

    public abstract VelocityStabilizationOptionsModel copy();

    public abstract void accept(IPIVConfigurationVisitor visitor);

    public abstract boolean isChanged(VelocityStabilizationOptionsModel another);
}
