package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-Centroid2D")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsCentroid2DModel extends SubPixelInterpolationOptionsModel {
    private int numberOfPixels;
    
    public SubPixelInterpolationOptionsCentroid2DModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.Centroid2D);
    }
    
    public int getNumberOfPixels() {
        return numberOfPixels;
    }
    
    public void setNumberOfPixels(int _numberOfPixels) {
        int oldValue = numberOfPixels;
        numberOfPixels = _numberOfPixels;
        pcs.firePropertyChange("numberOfPixels", oldValue, numberOfPixels);
    }

    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsCentroid2DModel model = new SubPixelInterpolationOptionsCentroid2DModel();
        
        model.numberOfPixels = numberOfPixels;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
       visitor.visit(this);        
    }
    
    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsCentroid2DModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsCentroid2DModel anotherGaussian1D = (SubPixelInterpolationOptionsCentroid2DModel)another;
                
        if (numberOfPixels != anotherGaussian1D.numberOfPixels) {
            changed = true;
        }
        
        return changed;
    }
}
