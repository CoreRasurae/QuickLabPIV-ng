package pt.quickLabPIV.ui.models;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-CombinedBaseAndFinal")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel extends SubPixelInterpolationOptionsModel {
    public SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.CombinedBaseAndFinalInterpolator);
        super.setSubInterpolationModes(new SubPixelInterpolationModeEnum[] {SubPixelInterpolationModeEnum.Disabled, SubPixelInterpolationModeEnum.Disabled});
    }
    
    private boolean alsoApplyMainInterpolationOnLastStep;
    private boolean applyFinalInterpolationAsLastPIVProcessingStep;
    
    public boolean isAlsoApplyMainInterpolationOnLastStep() {
        return alsoApplyMainInterpolationOnLastStep;
    }
    
    public void setAlsoApplyMainInterpolationOnLastStep(boolean _alsoApplyOnLastStep) {
        boolean oldValue = alsoApplyMainInterpolationOnLastStep;
        alsoApplyMainInterpolationOnLastStep = _alsoApplyOnLastStep;
        pcs.firePropertyChange("alsoApplyMainInterpolationOnLastStep", oldValue, alsoApplyMainInterpolationOnLastStep);
    }
    
    public boolean isApplyFinalInterpolationAsLastPIVProcessingStep() {
        return applyFinalInterpolationAsLastPIVProcessingStep;
    }
    
    public void setApplyFinalInterpolationAsLastPIVProcessingStep(boolean applyAsLastStep) {
        boolean oldValue = applyFinalInterpolationAsLastPIVProcessingStep;
        applyFinalInterpolationAsLastPIVProcessingStep = applyAsLastStep;
        pcs.firePropertyChange("applyFinalInterpolationAsLastPIVProcessingStep", oldValue, applyFinalInterpolationAsLastPIVProcessingStep);
    }
    
    public void setBaseSubPixelMode(SubPixelInterpolationModeEnum _newMode) {
        SubPixelInterpolationModeEnum modes[] = getSubInterpolationModes();
        SubPixelInterpolationModeEnum oldMode = modes[0];
        modes[0] = _newMode;
        setSubInterpolationModes(modes);
        pcs.firePropertyChange("baseSubPixelMode", oldMode, modes[0]);
    }
    
    public SubPixelInterpolationModeEnum getBaseSubPixelMode() {
        return getSubInterpolationModes()[0];
    }

    public void setFinalSubPixelMode(SubPixelInterpolationModeEnum _newMode) {
        SubPixelInterpolationModeEnum modes[] = getSubInterpolationModes();
        SubPixelInterpolationModeEnum oldMode = modes[1];
        modes[1] = _newMode;
        setSubInterpolationModes(modes);
        pcs.firePropertyChange("finalSubPixelMode", oldMode, modes[1]);
    }
    
    public SubPixelInterpolationModeEnum getFinalSubPixelMode() {
        return getSubInterpolationModes()[1];
    }

    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel model = new SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel();
        
        model.setSubInterpolationModes(Arrays.copyOf(getSubInterpolationModes(), getSubInterpolationModes().length));
        model.alsoApplyMainInterpolationOnLastStep = alsoApplyMainInterpolationOnLastStep;
        model.applyFinalInterpolationAsLastPIVProcessingStep = applyFinalInterpolationAsLastPIVProcessingStep;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel other = (SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel)another;
        
        SubPixelInterpolationModeEnum otherModes[] = other.getSubInterpolationModes();
        SubPixelInterpolationModeEnum myModes[] = getSubInterpolationModes();
        for (int i = 0; i < getSubInterpolationModes().length; i++) {
            if (otherModes[i] != myModes[i]) {
                changed = true;
                break;
            }
        }
        
        if(alsoApplyMainInterpolationOnLastStep != other.alsoApplyMainInterpolationOnLastStep) {
            changed = true;
        }
        
        if (applyFinalInterpolationAsLastPIVProcessingStep != other.applyFinalInterpolationAsLastPIVProcessingStep) {
            changed = true;
        }
                
        return changed;
    }
    
}
