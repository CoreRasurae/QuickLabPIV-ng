// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import pt.quickLabPIV.ui.models.VelocityValidationOptionsNormalizedMedianModel;
import pt.quickLabPIV.ui.validators.FloatRangeValidator;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class VectorValidationNormalizedMedianOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    /**
     * 
     */
    private static final long serialVersionUID = 1723986831715828830L;
    private AutoBinding<VelocityValidationOptionsNormalizedMedianModel, Float, JFormattedTextField, Object> epsilon0Binding;
    private AutoBinding<VelocityValidationOptionsNormalizedMedianModel, Float, JFormattedTextField, Object> distanceThresholdPixelsBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(2);


    private VelocityValidationModeEnum validationMode = VelocityValidationModeEnum.NormalizedMedian;
    private AppContextModel appContext;
    private VelocityValidationOptionsNormalizedMedianModel normMedianOptions = new VelocityValidationOptionsNormalizedMedianModel();
    private JFormattedTextField frmtdTxtFldDistanceThresholdPixels;
    private JFormattedTextField frmtdTxtFldEpsilon0;
    
    /**
     * Create the panel.
     */
    public VectorValidationNormalizedMedianOptionsPanel() {
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
        GridBagConstraints gbc_frmtdTxtFldDistanceThresholdPixels = new GridBagConstraints();
        gbc_frmtdTxtFldDistanceThresholdPixels.insets = new Insets(0, 0, 5, 0);
        gbc_frmtdTxtFldDistanceThresholdPixels.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldDistanceThresholdPixels.gridx = 1;
        gbc_frmtdTxtFldDistanceThresholdPixels.gridy = 0;
        add(frmtdTxtFldDistanceThresholdPixels, gbc_frmtdTxtFldDistanceThresholdPixels);
        
        JLabel lblEpsilon0 = new JLabel("Epsilon0");
        GridBagConstraints gbc_lblEpsilon0 = new GridBagConstraints();
        gbc_lblEpsilon0.anchor = GridBagConstraints.WEST;
        gbc_lblEpsilon0.insets = new Insets(0, 0, 0, 5);
        gbc_lblEpsilon0.gridx = 0;
        gbc_lblEpsilon0.gridy = 1;
        add(lblEpsilon0, gbc_lblEpsilon0);
        
        frmtdTxtFldEpsilon0 = new JFormattedTextField(createEpsilon0NumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldEpsilon0 = new GridBagConstraints();
        gbc_frmtdTxtFldEpsilon0.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldEpsilon0.gridx = 1;
        gbc_frmtdTxtFldEpsilon0.gridy = 1;
        add(frmtdTxtFldEpsilon0, gbc_frmtdTxtFldEpsilon0);
        initDataBindings();
        postInitDataBindings();
    }
    
    public void setTargetValidationMode(VelocityValidationModeEnum _validationMode) {
        if (_validationMode != VelocityValidationModeEnum.NormalizedMedian &&
            _validationMode != VelocityValidationModeEnum.NormalizedMedianOnly) {
            throw new UIException("Software Bug", "Trying to set Normalized Median vector validation panel to an unsupported value");
        }
        validationMode = _validationMode;
    }

    private DefaultFormatter createEpsilon0NumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0.0");
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(0);
        //format.setGroupingUsed(false);     //NOTE: This will cause decimal separator to stop working too - Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.0f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private DefaultFormatter createPixelsNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0.0");
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(3);
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
        VelocityValidationOptionsNormalizedMedianModel options = (VelocityValidationOptionsNormalizedMedianModel)pivModel.getValidationOption(validationMode);
        
        setAppContextModel(model, options);
    }
    
    public void setAppContextModel(AppContextModel model, VelocityValidationOptionsNormalizedMedianModel modelOptions) {
        appContext = model;
        normMedianOptions = modelOptions;
        
        FloatRangeValidator distanceThresholdPixelsValidator = (FloatRangeValidator)distanceThresholdPixelsBinding.getValidator();
        normMedianOptions.addPropertyChangeListener(distanceThresholdPixelsValidator);
        //
        distanceThresholdPixelsBinding.unbind();
        distanceThresholdPixelsBinding.setSourceObject(normMedianOptions);
        distanceThresholdPixelsBinding.bind();
        //
        epsilon0Binding.unbind();
        epsilon0Binding.setSourceObject(normMedianOptions);
        epsilon0Binding.bind();
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderDistanceThresholdPixels = new ErrorBorderForComponent(frmtdTxtFldDistanceThresholdPixels);
        frmtdTxtFldDistanceThresholdPixels.setBorder(borderDistanceThresholdPixels);
        distanceThresholdPixelsBinding.addBindingListener(borderDistanceThresholdPixels);
        errorBorders.add(borderDistanceThresholdPixels);
        //
        ErrorBorderForComponent borderEpsilon0 = new ErrorBorderForComponent(frmtdTxtFldEpsilon0);
        frmtdTxtFldEpsilon0.setBorder(borderEpsilon0);
        epsilon0Binding.addBindingListener(borderEpsilon0);
        errorBorders.add(borderEpsilon0);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        NullGenericFloatConverter distanceThresholdPixelsConverter = (NullGenericFloatConverter)distanceThresholdPixelsBinding.getConverter();
        FloatRangeValidator distanceThresholdPixelsValidator = (FloatRangeValidator)distanceThresholdPixelsBinding.getValidator();
        distanceThresholdPixelsValidator.setMinAndMax(0.01f, 128.0f);
        distanceThresholdPixelsConverter.setValidatorOnConvertForward(distanceThresholdPixelsValidator);
        distanceThresholdPixelsConverter.addStatusListener(borderDistanceThresholdPixels);
        //
        FloatRangeValidator epsilon0Validator = (FloatRangeValidator)epsilon0Binding.getValidator();
        epsilon0Validator.setMinAndMax(0.01f, 0.3f);
        NullGenericFloatConverter epsilon0Converter = (NullGenericFloatConverter)epsilon0Binding.getConverter();
        epsilon0Converter.setValidatorOnConvertForward(epsilon0Validator);
        epsilon0Converter.addStatusListener(borderEpsilon0);
    }
    
    protected void initDataBindings() {
        BeanProperty<VelocityValidationOptionsNormalizedMedianModel, Float> velocityValidationOptionsNormalizedMedianModelBeanProperty = BeanProperty.create("distanceThresholdPixels");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        distanceThresholdPixelsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, normMedianOptions, velocityValidationOptionsNormalizedMedianModelBeanProperty, frmtdTxtFldDistanceThresholdPixels, jFormattedTextFieldBeanProperty, "distanceThresholdPixelsBinding");
        distanceThresholdPixelsBinding.setConverter(new NullGenericFloatConverter());
        distanceThresholdPixelsBinding.setValidator(new FloatRangeValidator());
        distanceThresholdPixelsBinding.bind();
        //
        BeanProperty<VelocityValidationOptionsNormalizedMedianModel, Float> velocityValidationOptionsNormalizedMedianModelBeanProperty_1 = BeanProperty.create("epsilon0");
        epsilon0Binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, normMedianOptions, velocityValidationOptionsNormalizedMedianModelBeanProperty_1, frmtdTxtFldEpsilon0, jFormattedTextFieldBeanProperty, "epsilon0Binding");
        epsilon0Binding.setConverter(new NullGenericFloatConverter());
        epsilon0Binding.setValidator(new FloatRangeValidator());
        epsilon0Binding.bind();
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
