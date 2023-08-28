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

import javax.swing.JDialog;
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
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsBiCubicModel;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class SubPixelBiCubicOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<SubPixelInterpolationOptionsBiCubicModel, Integer, JFormattedTextField, Object> numberOfPixelsBinding;
    private AutoBinding<SubPixelInterpolationOptionsBiCubicModel, Integer, JFormattedTextField, Object> numberOfDecimalPointsBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(2);

    /**
     * 
     */
    private static final long serialVersionUID = 2020243910706108098L;

    private AppContextModel appContext;
    private SubPixelInterpolationOptionsBiCubicModel biCubicOptions = new SubPixelInterpolationOptionsBiCubicModel();
    private JFormattedTextField frmtdTxtFldDecimalPoints;
    private JFormattedTextField frmtdTxtFldNumberPixels;
    
    /**
     * Create the panel.
     */
    public SubPixelBiCubicOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{180, 0, 0};
        gridBagLayout.rowHeights = new int[]{15, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblNumberOfDecimal = new JLabel("Number of decimal points");
        GridBagConstraints gbc_lblNumberOfDecimal = new GridBagConstraints();
        gbc_lblNumberOfDecimal.insets = new Insets(0, 0, 5, 5);
        gbc_lblNumberOfDecimal.anchor = GridBagConstraints.WEST;
        gbc_lblNumberOfDecimal.gridx = 0;
        gbc_lblNumberOfDecimal.gridy = 0;
        add(lblNumberOfDecimal, gbc_lblNumberOfDecimal);
        
        frmtdTxtFldDecimalPoints = new JFormattedTextField(createDecimalPointsNumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldDecimalPoints = new GridBagConstraints();
        gbc_frmtdTxtFldDecimalPoints.insets = new Insets(0, 0, 5, 0);
        gbc_frmtdTxtFldDecimalPoints.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldDecimalPoints.gridx = 1;
        gbc_frmtdTxtFldDecimalPoints.gridy = 0;
        add(frmtdTxtFldDecimalPoints, gbc_frmtdTxtFldDecimalPoints);
        
        JLabel lblNumberOfPixels = new JLabel("Number of pixels");
        GridBagConstraints gbc_lblNumberOfPixels = new GridBagConstraints();
        gbc_lblNumberOfPixels.anchor = GridBagConstraints.WEST;
        gbc_lblNumberOfPixels.insets = new Insets(0, 0, 0, 5);
        gbc_lblNumberOfPixels.gridx = 0;
        gbc_lblNumberOfPixels.gridy = 1;
        add(lblNumberOfPixels, gbc_lblNumberOfPixels);
        
        frmtdTxtFldNumberPixels = new JFormattedTextField(createPixelsNumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldNumberPixels = new GridBagConstraints();
        gbc_frmtdTxtFldNumberPixels.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldNumberPixels.gridx = 1;
        gbc_frmtdTxtFldNumberPixels.gridy = 1;
        add(frmtdTxtFldNumberPixels, gbc_frmtdTxtFldNumberPixels);
        initDataBindings();
        postInitDataBindings();
    }

    private DefaultFormatter createDecimalPointsNumberFormatter() {
        NumberFormat format  = new DecimalFormat("####0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);        
        format.setMaximumIntegerDigits(5);
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
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        biCubicOptions = (SubPixelInterpolationOptionsBiCubicModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.BiCubic);
        //
        numberOfDecimalPointsBinding.unbind();
        numberOfDecimalPointsBinding.setSourceObject(biCubicOptions);
        numberOfDecimalPointsBinding.bind();
        //
        numberOfPixelsBinding.unbind();
        numberOfPixelsBinding.setSourceObject(biCubicOptions);
        numberOfPixelsBinding.bind();
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderNumberOfDecimalPoints = new ErrorBorderForComponent(frmtdTxtFldDecimalPoints);
        frmtdTxtFldDecimalPoints.setBorder(borderNumberOfDecimalPoints);
        numberOfDecimalPointsBinding.addBindingListener(borderNumberOfDecimalPoints);
        errorBorders.add(borderNumberOfDecimalPoints);
        //
        ErrorBorderForComponent borderNumberOfPixels = new ErrorBorderForComponent(frmtdTxtFldNumberPixels);
        frmtdTxtFldNumberPixels.setBorder(borderNumberOfPixels);
        numberOfPixelsBinding.addBindingListener(borderNumberOfPixels);
        errorBorders.add(borderNumberOfPixels);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        IntegerRangeValidator numberOfDecimalPointsValidator = (IntegerRangeValidator)numberOfDecimalPointsBinding.getValidator();
        numberOfDecimalPointsValidator.setMinAndMax(100, 10000);
        numberOfDecimalPointsValidator.setRangeType(RangeTypeEnum.ANY);
        NullGenericIntegerConverter numberOfDecimalPointsConverter = (NullGenericIntegerConverter)numberOfDecimalPointsBinding.getConverter();
        numberOfDecimalPointsConverter.setValidatorOnConvertForward(numberOfDecimalPointsValidator);
        numberOfDecimalPointsConverter.addStatusListener(borderNumberOfDecimalPoints);
        //
        IntegerRangeValidator numberOfPixelsValidator = (IntegerRangeValidator)numberOfPixelsBinding.getValidator();
        numberOfPixelsValidator.setMinAndMax(3, 15);
        numberOfPixelsValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter numberOfPixelsConverter = (NullGenericIntegerConverter)numberOfPixelsBinding.getConverter();
        numberOfPixelsConverter.setValidatorOnConvertForward(numberOfPixelsValidator);
        numberOfPixelsConverter.addStatusListener(borderNumberOfPixels);
    }
    
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsBiCubicModel, Integer> subPixelInterpolationOptionsBiCubicModelBeanProperty = BeanProperty.create("numberOfDecimalPoints");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        numberOfDecimalPointsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, biCubicOptions, subPixelInterpolationOptionsBiCubicModelBeanProperty, frmtdTxtFldDecimalPoints, jFormattedTextFieldBeanProperty, "numberOfDecimalPointsBinding");
        numberOfDecimalPointsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfDecimalPointsBinding.setValidator(new IntegerRangeValidator());
        numberOfDecimalPointsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsBiCubicModel, Integer> subPixelInterpolationOptionsBiCubicModelBeanProperty_1 = BeanProperty.create("numberOfPixels");
        numberOfPixelsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, biCubicOptions, subPixelInterpolationOptionsBiCubicModelBeanProperty_1, frmtdTxtFldNumberPixels, jFormattedTextFieldBeanProperty, "numberOfPixelsBinding");
        numberOfPixelsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfPixelsBinding.setValidator(new IntegerRangeValidator());
        numberOfPixelsBinding.bind();
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    public void setParentDialog(JDialog _dialog) {
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
