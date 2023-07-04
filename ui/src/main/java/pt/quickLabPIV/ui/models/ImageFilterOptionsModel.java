package pt.quickLabPIV.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
@XmlSeeAlso({ImageFilterOptionsGaussian2DModel.class})
public abstract class ImageFilterOptionsModel {
    
    @XmlTransient
    private PIVConfigurationModel parent;
    
    @XmlTransient
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private ImageFilteringModeEnum filterMode = ImageFilteringModeEnum.DoNotApplyImageFiltering;
    
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
    
    public ImageFilteringModeEnum getFilterMode() {
        return filterMode;
    }
    
    protected void setFilterMode(ImageFilteringModeEnum mode) {
        filterMode = mode;
    }

    public abstract ImageFilterOptionsModel copy();

    public abstract void accept(IPIVConfigurationVisitor visitor);

    public abstract boolean isChanged(ImageFilterOptionsModel another);
}
