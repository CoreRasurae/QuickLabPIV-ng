// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
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
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;
import pt.quickLabPIV.ui.models.VelocityStabilizationOptionsMaxDisplacementModel;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class StabilizationMaxDisplacementOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<VelocityStabilizationOptionsMaxDisplacementModel, Integer, JFormattedTextField, Object> maxIterationsBinding;
    private AutoBinding<VelocityStabilizationOptionsMaxDisplacementModel, Integer, JFormattedTextField, Object> maxDisplacementPixelsBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(2);

    /**
     * 
     */
    private static final long serialVersionUID = -4418057149920524149L;

    private AppContextModel appContext;
    private VelocityStabilizationOptionsMaxDisplacementModel maxDispOptions = new VelocityStabilizationOptionsMaxDisplacementModel();
    private JFormattedTextField frmtdTxtFldMaxDisplacementPixels;
    private JFormattedTextField frmtdTxtFldMaxIterations;
    
    /**
     * Create the panel.
     */
    public StabilizationMaxDisplacementOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblMaxDisplacementPixels = new JLabel("Max. displacement pixels");
        GridBagConstraints gbc_lblMaxDisplacementPixels = new GridBagConstraints();
        gbc_lblMaxDisplacementPixels.insets = new Insets(0, 0, 5, 5);
        gbc_lblMaxDisplacementPixels.anchor = GridBagConstraints.WEST;
        gbc_lblMaxDisplacementPixels.gridx = 0;
        gbc_lblMaxDisplacementPixels.gridy = 0;
        add(lblMaxDisplacementPixels, gbc_lblMaxDisplacementPixels);
        
        frmtdTxtFldMaxDisplacementPixels = new JFormattedTextField(createPixelsNumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldMaxDisplacementPixels = new GridBagConstraints();
        gbc_frmtdTxtFldMaxDisplacementPixels.insets = new Insets(0, 0, 5, 0);
        gbc_frmtdTxtFldMaxDisplacementPixels.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldMaxDisplacementPixels.gridx = 1;
        gbc_frmtdTxtFldMaxDisplacementPixels.gridy = 0;
        add(frmtdTxtFldMaxDisplacementPixels, gbc_frmtdTxtFldMaxDisplacementPixels);
        
        JLabel lblMaxStabilizationIterations = new JLabel("Max. stabilization iterations");
        GridBagConstraints gbc_lblMaxStabilizationIterations = new GridBagConstraints();
        gbc_lblMaxStabilizationIterations.anchor = GridBagConstraints.WEST;
        gbc_lblMaxStabilizationIterations.insets = new Insets(0, 0, 0, 5);
        gbc_lblMaxStabilizationIterations.gridx = 0;
        gbc_lblMaxStabilizationIterations.gridy = 1;
        add(lblMaxStabilizationIterations, gbc_lblMaxStabilizationIterations);
        
        frmtdTxtFldMaxIterations = new JFormattedTextField(createIterationsNumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldMaxIterations = new GridBagConstraints();
        gbc_frmtdTxtFldMaxIterations.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldMaxIterations.gridx = 1;
        gbc_frmtdTxtFldMaxIterations.gridy = 1;
        add(frmtdTxtFldMaxIterations, gbc_frmtdTxtFldMaxIterations);
        initDataBindings();
        postInitDataBindings();
    }

    private DefaultFormatter createIterationsNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(2);
        format.setGroupingUsed(false);     //Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private DefaultFormatter createPixelsNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(2);
        format.setGroupingUsed(false);     //Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    public void setAppContextModel(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        maxDispOptions = (VelocityStabilizationOptionsMaxDisplacementModel)pivModel.getStabilizationOption(VelocityStabilizationModeEnum.MaxDisplacement);
        //
        maxDisplacementPixelsBinding.unbind();
        maxDisplacementPixelsBinding.setSourceObject(maxDispOptions);
        maxDisplacementPixelsBinding.bind();
        //
        maxIterationsBinding.unbind();
        maxIterationsBinding.setSourceObject(maxDispOptions);
        maxIterationsBinding.bind();
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderMaxDisplacementPixels = new ErrorBorderForComponent(frmtdTxtFldMaxDisplacementPixels);
        frmtdTxtFldMaxDisplacementPixels.setBorder(borderMaxDisplacementPixels);
        maxDisplacementPixelsBinding.addBindingListener(borderMaxDisplacementPixels);
        errorBorders.add(borderMaxDisplacementPixels);
        //
        ErrorBorderForComponent borderMaxIterations = new ErrorBorderForComponent(frmtdTxtFldMaxIterations);
        frmtdTxtFldMaxIterations.setBorder(borderMaxIterations);
        maxIterationsBinding.addBindingListener(borderMaxIterations);
        errorBorders.add(borderMaxIterations);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        IntegerRangeValidator maxDisplacementPixelsValidator = (IntegerRangeValidator)maxDisplacementPixelsBinding.getValidator();
        maxDisplacementPixelsValidator.setMinAndMax(1, 50);
        maxDisplacementPixelsValidator.setRangeType(RangeTypeEnum.ANY);
        NullGenericIntegerConverter maxDisplacmentPixelsConverter = (NullGenericIntegerConverter)maxDisplacementPixelsBinding.getConverter();
        maxDisplacmentPixelsConverter.setValidatorOnConvertForward(maxDisplacementPixelsValidator);
        maxDisplacmentPixelsConverter.addStatusListener(borderMaxDisplacementPixels);
        //
        IntegerRangeValidator maxIterationsValidator = (IntegerRangeValidator)maxIterationsBinding.getValidator();
        maxIterationsValidator.setMinAndMax(1, 10);
        maxIterationsValidator.setRangeType(RangeTypeEnum.ANY);
        NullGenericIntegerConverter maxIterationsConverter = (NullGenericIntegerConverter)maxIterationsBinding.getConverter();
        maxIterationsConverter.setValidatorOnConvertForward(maxIterationsValidator);
        maxIterationsConverter.addStatusListener(borderMaxIterations);
    }
    
    protected void initDataBindings() {
        BeanProperty<VelocityStabilizationOptionsMaxDisplacementModel, Integer> velocityStabilizationOptionsMaxDisplacementModelBeanProperty = BeanProperty.create("maxDisplacementPixels");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        maxDisplacementPixelsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, maxDispOptions, velocityStabilizationOptionsMaxDisplacementModelBeanProperty, frmtdTxtFldMaxDisplacementPixels, jFormattedTextFieldBeanProperty, "maxDisplacementPixelsBinding");
        maxDisplacementPixelsBinding.setConverter(new NullGenericIntegerConverter());
        maxDisplacementPixelsBinding.setValidator(new IntegerRangeValidator());
        maxDisplacementPixelsBinding.bind();
        //
        BeanProperty<VelocityStabilizationOptionsMaxDisplacementModel, Integer> velocityStabilizationOptionsMaxDisplacementModelBeanProperty_1 = BeanProperty.create("maxIterations");
        maxIterationsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, maxDispOptions, velocityStabilizationOptionsMaxDisplacementModelBeanProperty_1, frmtdTxtFldMaxIterations, jFormattedTextFieldBeanProperty, "maxIterationsBinding");
        maxIterationsBinding.setConverter(new NullGenericIntegerConverter());
        maxIterationsBinding.setValidator(new IntegerRangeValidator());
        maxIterationsBinding.bind();
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {     
        return errorBorders;
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }
}
