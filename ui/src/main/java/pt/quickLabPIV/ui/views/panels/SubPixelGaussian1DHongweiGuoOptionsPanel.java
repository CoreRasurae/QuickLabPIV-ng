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
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian1DHongweiGuoModel;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class SubPixelGaussian1DHongweiGuoOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<SubPixelInterpolationOptionsGaussian1DHongweiGuoModel, Integer, JFormattedTextField, Object> numberOfIterationsBinding;
    /**
     * 
     */
    private static final long serialVersionUID = -8687304030874884491L;
    
    private AutoBinding<SubPixelInterpolationOptionsGaussian1DHongweiGuoModel, Integer, JFormattedTextField, Object> numberOfPixelsBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(1);
    
    private AppContextModel appContext;
    private SubPixelInterpolationOptionsGaussian1DHongweiGuoModel gaussianOptions;
    private JFormattedTextField frmtdTxtFldNumberPixels;
    private JFormattedTextField frmtfTxtFldNumberIterations;

    /**
     * Create the panel.
     */
    public SubPixelGaussian1DHongweiGuoOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{118, 0, 0};
        gridBagLayout.rowHeights = new int[]{15, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblPixelsLabel = new JLabel("Number of pixels");
        GridBagConstraints gbc_lblPixelsLabel = new GridBagConstraints();
        gbc_lblPixelsLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblPixelsLabel.anchor = GridBagConstraints.EAST;
        gbc_lblPixelsLabel.gridx = 0;
        gbc_lblPixelsLabel.gridy = 0;
        add(lblPixelsLabel, gbc_lblPixelsLabel);
        
        frmtdTxtFldNumberPixels = new JFormattedTextField(createPixelsNumberFormatter());
        GridBagConstraints gbc_frmtdTxtFldNumberPixels = new GridBagConstraints();
        gbc_frmtdTxtFldNumberPixels.insets = new Insets(0, 0, 5, 0);
        gbc_frmtdTxtFldNumberPixels.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtdTxtFldNumberPixels.gridx = 1;
        gbc_frmtdTxtFldNumberPixels.gridy = 0;
        add(frmtdTxtFldNumberPixels, gbc_frmtdTxtFldNumberPixels);
        
        JLabel lblNumberOfIterations = new JLabel("Number of iterations");
        GridBagConstraints gbc_lblNumberOfIterations = new GridBagConstraints();
        gbc_lblNumberOfIterations.anchor = GridBagConstraints.EAST;
        gbc_lblNumberOfIterations.insets = new Insets(0, 0, 0, 5);
        gbc_lblNumberOfIterations.gridx = 0;
        gbc_lblNumberOfIterations.gridy = 1;
        add(lblNumberOfIterations, gbc_lblNumberOfIterations);
        
        frmtfTxtFldNumberIterations = new JFormattedTextField(createIterationsNumberFormatter());
        GridBagConstraints gbc_frmtfTxtFldNumberIterations = new GridBagConstraints();
        gbc_frmtfTxtFldNumberIterations.fill = GridBagConstraints.HORIZONTAL;
        gbc_frmtfTxtFldNumberIterations.gridx = 1;
        gbc_frmtfTxtFldNumberIterations.gridy = 1;
        add(frmtfTxtFldNumberIterations, gbc_frmtfTxtFldNumberIterations);
        initDataBindings();
        postInitDataBindings();
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
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        gaussianOptions = (SubPixelInterpolationOptionsGaussian1DHongweiGuoModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.Gaussian1DHongweiGuo);
        //
        numberOfPixelsBinding.unbind();
        numberOfPixelsBinding.setSourceObject(gaussianOptions);
        numberOfPixelsBinding.bind();
        //
        numberOfIterationsBinding.unbind();
        numberOfIterationsBinding.setSourceObject(gaussianOptions);
        numberOfIterationsBinding.bind();        
    }

    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderNumberOfPixels = new ErrorBorderForComponent(frmtdTxtFldNumberPixels);
        frmtdTxtFldNumberPixels.setBorder(borderNumberOfPixels);
        numberOfPixelsBinding.addBindingListener(borderNumberOfPixels);
        errorBorders.add(borderNumberOfPixels);
        
        ErrorBorderForComponent borderNumberOfIterations = new ErrorBorderForComponent(frmtfTxtFldNumberIterations);
        frmtfTxtFldNumberIterations.setBorder(borderNumberOfIterations);
        numberOfIterationsBinding.addBindingListener(borderNumberOfIterations);
        errorBorders.add(borderNumberOfIterations);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        IntegerRangeValidator numberOfPixelsValidator = (IntegerRangeValidator)numberOfPixelsBinding.getValidator();
        numberOfPixelsValidator.setMinAndMax(3, 15);
        numberOfPixelsValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter numberOfPixelsConverter = (NullGenericIntegerConverter)numberOfPixelsBinding.getConverter();
        numberOfPixelsConverter.setValidatorOnConvertForward(numberOfPixelsValidator);
        numberOfPixelsConverter.addStatusListener(borderNumberOfPixels);
        
        IntegerRangeValidator numberOfIterationsValidator = (IntegerRangeValidator)numberOfIterationsBinding.getValidator();
        numberOfIterationsValidator.setMinAndMax(6, 50);
        numberOfIterationsValidator.setRangeType(RangeTypeEnum.ANY);
        NullGenericIntegerConverter numberOfIterationsConverter = (NullGenericIntegerConverter)numberOfIterationsBinding.getConverter();
        numberOfIterationsConverter.setValidatorOnConvertForward(numberOfIterationsValidator);
        numberOfIterationsConverter.addStatusListener(borderNumberOfIterations);
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsGaussian1DHongweiGuoModel, Integer> subPixelInterpolationOptionsGaussian1DModelBeanProperty = BeanProperty.create("numberOfPixels");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        numberOfPixelsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianOptions, subPixelInterpolationOptionsGaussian1DModelBeanProperty, frmtdTxtFldNumberPixels, jFormattedTextFieldBeanProperty, "numberOfPixelsBinding");
        numberOfPixelsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfPixelsBinding.setValidator(new IntegerRangeValidator());
        numberOfPixelsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsGaussian1DHongweiGuoModel, Integer> subPixelInterpolationOptionsGaussian1DHongweiGuoModelBeanProperty = BeanProperty.create("numberOfIterations");
        numberOfIterationsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianOptions, subPixelInterpolationOptionsGaussian1DHongweiGuoModelBeanProperty, frmtfTxtFldNumberIterations, jFormattedTextFieldBeanProperty, "numberOfIterationsBinding");
        numberOfIterationsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfIterationsBinding.setValidator(new IntegerRangeValidator());
        numberOfIterationsBinding.bind();
    }
    protected JFormattedTextField getFrmtfTxtFldNumberIterations() {
        return frmtfTxtFldNumberIterations;
    }

    public void setParentDialog(JDialog _dialog) {
    }

    @Override
    public void dispose() {
        
    }
}
