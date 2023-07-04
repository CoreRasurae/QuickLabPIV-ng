package pt.quickLabPIV.ui.models;

public class VelocityValidationOptionsDifferenceModel extends VelocityValidationOptionsModel {
    private float distanceThresholdPixels;
    
    public VelocityValidationOptionsDifferenceModel() {
        setValidationMode(VelocityValidationModeEnum.Difference);
    }
    
    public float getDistanceThresholdPixels() {
        return distanceThresholdPixels;
    }
    
    public void setDistanceThresholdPixels(float _distanceThresholdPixels) {
        float oldValue = distanceThresholdPixels;
        distanceThresholdPixels = _distanceThresholdPixels;
        pcs.firePropertyChange("distanceThresholdPixels", oldValue, distanceThresholdPixels);
    }
        
    @Override
    public VelocityValidationOptionsModel copy() {
        VelocityValidationOptionsDifferenceModel model = new VelocityValidationOptionsDifferenceModel();
        
        model.distanceThresholdPixels = distanceThresholdPixels;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(VelocityValidationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof VelocityValidationOptionsDifferenceModel)) {
            return true;            
        }
        
        VelocityValidationOptionsDifferenceModel anotherDifference = (VelocityValidationOptionsDifferenceModel)another;
                
        if (distanceThresholdPixels != anotherDifference.distanceThresholdPixels) {
            changed = true;
        }
                
        return changed;
    }

}
