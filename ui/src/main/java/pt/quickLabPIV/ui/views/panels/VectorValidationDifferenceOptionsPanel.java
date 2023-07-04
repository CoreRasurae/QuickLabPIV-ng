package pt.quickLabPIV.ui.views.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.converters.NullGenericFloatConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsDifferenceModel;
import pt.quickLabPIV.ui.validators.FloatRangeValidator;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class VectorValidationDifferenceOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    /**
     * 
     */
    private static final long serialVersionUID = -1673539733472085879L;
    private AutoBinding<VelocityValidationOptionsDifferenceModel, Float, JFormattedTextField, Object> distanceThresholdPixelsBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(2);


    private VelocityValidationModeEnum validationMode = VelocityValidationModeEnum.Difference;
    private AppContextModel appContext;
    private VelocityValidationOptionsDifferenceModel differenceOptions = new VelocityValidationOptionsDifferenceModel();
    private JFormattedTextField frmtdTxtFldDistanceThresholdPixels;
    
    /**
     * Create the panel.
     */
    public VectorValidationDifferenceOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblDistanceThresholdPixels = new JLabel("Distance threshold pixels");
        GridBagConstraints gbc_lblDistanceThresholdPixels = new GridBagConstraints();
        gbc_lblDistanceThresholdPixels.insets = new Insets(0, 0, 5, 5);
        gbc_lblDistanceThresholdPixels.anchor = GridBagConstraints.WEST;
        gbc_lblDistanceThresholdPixels.gridx = 0;
        gbc_lblDistanceThresholdPixels.gridy = 0;
        add(lblDistanceThresholdPixels, gbc_lblDistanceThresholdPixels);
        
        frmtdTxtFldDistanceThresholdPixels = new JFormattedTextField(createPixelsNumberFormatter());
        frmtdTxtFldDistanceThresholdPixels.addInputMethodListener(new InputMethodListener() {
            public void caretPositionChanged(InputMethodEvent event) {
                frmtdTxtFldDistanceThresholdPixels.setValue(frmtdTxtFldDistanceThresholdPixels.getText());
            }
            
            public void inputMethodTextChanged(InputMethodEvent event) {
            }
        });
        GridBagConstraints gbc_frmtdTxtFldDistanceThresholdPixels = new GridBagConstraints();
        gbc_frmtdTxtFldDistanceThresholdPixels.insets = new Insets(0, 0, 5, 0);
        gbc_frmtdTxtFldDistanceThresholdPixels.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldDistanceThresholdPixels.gridx = 1;
        gbc_frmtdTxtFldDistanceThresholdPixels.gridy = 0;
        add(frmtdTxtFldDistanceThresholdPixels, gbc_frmtdTxtFldDistanceThresholdPixels);
        initDataBindings();
        postInitDataBindings();
    }
    
    public void setTargetValidationMode(VelocityValidationModeEnum mode) {
        if (mode != VelocityValidationModeEnum.Difference && mode != VelocityValidationModeEnum.DifferenceOnly) {
            throw new UIException("Software bug", "Trying to set Difference vector validation panel to an unsupported value");
        }
        validationMode = mode;
    }
    
    private DefaultFormatter createPixelsNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0.0");
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(3);
        format.setParseIntegerOnly(false);
        format.setGroupingUsed(false);     //NOTE: Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.0f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    public void setAppContextModel(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        differenceOptions = (VelocityValidationOptionsDifferenceModel)pivModel.getValidationOption(validationMode);
        //
        distanceThresholdPixelsBinding.unbind();
        distanceThresholdPixelsBinding.setSourceObject(differenceOptions);
        distanceThresholdPixelsBinding.bind();
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderDistanceThresholdPixels = new ErrorBorderForComponent(frmtdTxtFldDistanceThresholdPixels);
        frmtdTxtFldDistanceThresholdPixels.setBorder(borderDistanceThresholdPixels);
        distanceThresholdPixelsBinding.addBindingListener(borderDistanceThresholdPixels);
        errorBorders.add(borderDistanceThresholdPixels);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        FloatRangeValidator distanceThresholdPixelsValidator = (FloatRangeValidator)distanceThresholdPixelsBinding.getValidator();
        distanceThresholdPixelsValidator.setMinAndMax(0.01f, 128);
        NullGenericFloatConverter distanceThresholdPixelsConverter = (NullGenericFloatConverter)distanceThresholdPixelsBinding.getConverter();
        distanceThresholdPixelsConverter.setValidatorOnConvertForward(distanceThresholdPixelsValidator);
        distanceThresholdPixelsConverter.addStatusListener(borderDistanceThresholdPixels);
    }
    
    protected void initDataBindings() {
        BeanProperty<VelocityValidationOptionsDifferenceModel, Float> velocityValidationOptionsDifferenceModelBeanProperty = BeanProperty.create("distanceThresholdPixels");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        distanceThresholdPixelsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, differenceOptions, velocityValidationOptionsDifferenceModelBeanProperty, frmtdTxtFldDistanceThresholdPixels, jFormattedTextFieldBeanProperty, "maxDisplacementPixelsBinding");
        distanceThresholdPixelsBinding.setConverter(new NullGenericFloatConverter());
        distanceThresholdPixelsBinding.setValidator(new FloatRangeValidator());
        distanceThresholdPixelsBinding.bind();
        //
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {     
        return errorBorders;
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }

    @Override
    public void dispose() {
        
    }
}
