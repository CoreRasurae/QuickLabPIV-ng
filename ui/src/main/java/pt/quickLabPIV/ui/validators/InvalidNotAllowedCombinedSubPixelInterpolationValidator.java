package pt.quickLabPIV.ui.validators;

import org.jdesktop.beansbinding.Validator;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel;

public class InvalidNotAllowedCombinedSubPixelInterpolationValidator extends Validator<SubPixelInterpolationModeEnum> {
    private AppContextModel appContext;
    private InvalidNotAllowedCombinedSubPixelInterpolationValidator otherValidator;
    private SubPixelInterpolationModeEnum cachedValue;
    
    
    public void setAppContext(AppContextModel _appContext) {
        appContext = _appContext;
    }
    public void setOtherValidator(InvalidNotAllowedCombinedSubPixelInterpolationValidator _otherValidator) {
        otherValidator = _otherValidator;
    }
    
    
    @Override
    public Validator<SubPixelInterpolationModeEnum>.Result validate(SubPixelInterpolationModeEnum value) {
        Validator<SubPixelInterpolationModeEnum>.Result result = null;
        
        cachedValue = value;
        
        SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel option =
           (SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel)
           appContext.getProject().getPIVConfiguration().getInterpolationOption(SubPixelInterpolationModeEnum.CombinedBaseAndFinalInterpolator);
        
        if (value == SubPixelInterpolationModeEnum.Disabled) {
            result = new InvalidNotAllowedCombinedSubPixelInterpolationValidator.Result(null, "Selected interpolation strategy is invalid.\\nConsider disabling validation.");
        } else if (value == SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL || 
                   value == SubPixelInterpolationModeEnum.LucasKanadeOpenCL) {
            if (appContext.getExecutionEnvironment() != null && !appContext.getExecutionEnvironment().isEnableOpenCL()) {
                result = new InvalidNotAllowedCombinedSubPixelInterpolationValidator.Result(null, "Selected interpolation strategy cannot be employed with OpenCL disabled.");
            }
        } 
        
        if (result == null) {
            if (value == otherValidator.getCachedValue() && value != SubPixelInterpolationModeEnum.Disabled) {
                result = new InvalidNotAllowedCombinedSubPixelInterpolationValidator.Result(null, "Base and Final sub-pixel methods must not be the same.");
            }
        }
        
        return result;
    }
    
    private SubPixelInterpolationModeEnum getCachedValue() {
        return cachedValue;
    }
}
