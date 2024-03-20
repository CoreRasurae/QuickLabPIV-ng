// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
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
import pt.quickLabPIV.ui.converters.NullGaussian2DStrategiesConverter;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.Gaussian2DStrategiesComboBoxModel;
import pt.quickLabPIV.ui.models.Gaussian2DStrategiesEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian2DModel;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedGaussian2DStrategiesValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class SubPixelGaussian2DOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<SubPixelInterpolationOptionsGaussian2DModel, Gaussian2DStrategiesEnum, JComboBox<Gaussian2DStrategiesEnum>, Object> selectedAlgorithmBinding;
    private AutoBinding<SubPixelInterpolationOptionsGaussian2DModel, Integer, JFormattedTextField, Object> numberOfPixelsInYBinding;
    private AutoBinding<SubPixelInterpolationOptionsGaussian2DModel, Integer, JFormattedTextField, Object> numberOfPixelsInXBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(3);
    
    /**
     * 
     */
    private static final long serialVersionUID = 4134173360298567141L;

    private AppContextModel appContext;
    private SubPixelInterpolationOptionsGaussian2DModel gaussianOptions;
    private JFormattedTextField fmtdTxtFldNumberPixelsX;
    private JFormattedTextField fmtdTxtFldNumberPixelsY;
    private JComboBox<Gaussian2DStrategiesEnum> comboBoxGaussianStrategy;
    
    public static void main(String[] args) {
        try {
            SubPixelGaussian2DOptionsPanel panel = new SubPixelGaussian2DOptionsPanel();
            JDialog dialog = new JDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            dialog.getContentPane().add(panel, FlowLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create the panel.
     */
    public SubPixelGaussian2DOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{217, 0, 0};
        gridBagLayout.rowHeights = new int[]{15, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblGaussianMethod = new JLabel("Gaussian computation method");
        GridBagConstraints gbc_lblGaussianMethod = new GridBagConstraints();
        gbc_lblGaussianMethod.insets = new Insets(0, 0, 5, 5);
        gbc_lblGaussianMethod.anchor = GridBagConstraints.WEST;
        gbc_lblGaussianMethod.gridx = 0;
        gbc_lblGaussianMethod.gridy = 0;
        add(lblGaussianMethod, gbc_lblGaussianMethod);
        
        comboBoxGaussianStrategy = new JComboBox<>();
        comboBoxGaussianStrategy.setModel(new Gaussian2DStrategiesComboBoxModel());
        GridBagConstraints gbc_comboBoxGaussianStrategy = new GridBagConstraints();
        gbc_comboBoxGaussianStrategy.insets = new Insets(0, 0, 5, 0);
        gbc_comboBoxGaussianStrategy.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxGaussianStrategy.gridx = 1;
        gbc_comboBoxGaussianStrategy.gridy = 0;
        add(comboBoxGaussianStrategy, gbc_comboBoxGaussianStrategy);
        
        JLabel lblNumberOfPixels = new JLabel("Number of pixels in X");
        GridBagConstraints gbc_lblNumberOfPixels = new GridBagConstraints();
        gbc_lblNumberOfPixels.anchor = GridBagConstraints.WEST;
        gbc_lblNumberOfPixels.insets = new Insets(0, 0, 5, 5);
        gbc_lblNumberOfPixels.gridx = 0;
        gbc_lblNumberOfPixels.gridy = 1;
        add(lblNumberOfPixels, gbc_lblNumberOfPixels);
        
        fmtdTxtFldNumberPixelsX = new JFormattedTextField(createPixelsNumberFormatter());
        GridBagConstraints gbc_fmtdTxtFldNumberPixelsX = new GridBagConstraints();
        gbc_fmtdTxtFldNumberPixelsX.insets = new Insets(0, 0, 5, 0);
        gbc_fmtdTxtFldNumberPixelsX.fill = GridBagConstraints.HORIZONTAL;
        gbc_fmtdTxtFldNumberPixelsX.gridx = 1;
        gbc_fmtdTxtFldNumberPixelsX.gridy = 1;
        add(fmtdTxtFldNumberPixelsX, gbc_fmtdTxtFldNumberPixelsX);
        
        JLabel lblNumberOfPixels_1 = new JLabel("Number of pixels in Y");
        GridBagConstraints gbc_lblNumberOfPixels_1 = new GridBagConstraints();
        gbc_lblNumberOfPixels_1.anchor = GridBagConstraints.WEST;
        gbc_lblNumberOfPixels_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblNumberOfPixels_1.gridx = 0;
        gbc_lblNumberOfPixels_1.gridy = 2;
        add(lblNumberOfPixels_1, gbc_lblNumberOfPixels_1);
        
        fmtdTxtFldNumberPixelsY = new JFormattedTextField(createPixelsNumberFormatter());
        GridBagConstraints gbc_fmtdTxtFldNumberPixelsY = new GridBagConstraints();
        gbc_fmtdTxtFldNumberPixelsY.fill = GridBagConstraints.HORIZONTAL;
        gbc_fmtdTxtFldNumberPixelsY.gridx = 1;
        gbc_fmtdTxtFldNumberPixelsY.gridy = 2;
        add(fmtdTxtFldNumberPixelsY, gbc_fmtdTxtFldNumberPixelsY);
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
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        gaussianOptions = (SubPixelInterpolationOptionsGaussian2DModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.Gaussian2D);
        //
        selectedAlgorithmBinding.unbind();
        selectedAlgorithmBinding.setSourceObject(gaussianOptions);
        selectedAlgorithmBinding.bind();
        //
        numberOfPixelsInXBinding.unbind();
        numberOfPixelsInXBinding.setSourceObject(gaussianOptions);
        numberOfPixelsInXBinding.bind();
        //
        numberOfPixelsInYBinding.unbind();
        numberOfPixelsInYBinding.setSourceObject(gaussianOptions);
        numberOfPixelsInYBinding.bind();
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderGaussianStrategy = new ErrorBorderForComponent(comboBoxGaussianStrategy);
        comboBoxGaussianStrategy.setBorder(borderGaussianStrategy);
        selectedAlgorithmBinding.addBindingListener(borderGaussianStrategy);
        errorBorders.add(borderGaussianStrategy);
        //
        ErrorBorderForComponent borderNumberOfPixelsInX = new ErrorBorderForComponent(fmtdTxtFldNumberPixelsX);
        fmtdTxtFldNumberPixelsX.setBorder(borderNumberOfPixelsInX);
        numberOfPixelsInXBinding.addBindingListener(borderNumberOfPixelsInX);
        errorBorders.add(borderNumberOfPixelsInX);
        //
        ErrorBorderForComponent borderNumberOfPixelsInY = new ErrorBorderForComponent(fmtdTxtFldNumberPixelsY);
        fmtdTxtFldNumberPixelsY.setBorder(borderNumberOfPixelsInY);
        numberOfPixelsInYBinding.addBindingListener(borderNumberOfPixelsInY);
        errorBorders.add(borderNumberOfPixelsInY);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        NullGaussian2DStrategiesConverter alogrithmConverter = (NullGaussian2DStrategiesConverter)selectedAlgorithmBinding.getConverter();
        alogrithmConverter.setValidatorOnConvertForward(selectedAlgorithmBinding.getValidator());
        alogrithmConverter.addStatusListener(borderGaussianStrategy);        
        //
        IntegerRangeValidator numberOfPixelsInYValidator = (IntegerRangeValidator)numberOfPixelsInYBinding.getValidator();
        numberOfPixelsInYValidator.setMinAndMax(3, 15);
        numberOfPixelsInYValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter numberOfPixelsInYConverter = (NullGenericIntegerConverter)numberOfPixelsInYBinding.getConverter();
        numberOfPixelsInYConverter.setValidatorOnConvertForward(numberOfPixelsInYValidator);
        numberOfPixelsInYConverter.addStatusListener(borderNumberOfPixelsInY);
        //
        IntegerRangeValidator numberOfPixelsInXValidator = (IntegerRangeValidator)numberOfPixelsInXBinding.getValidator();
        numberOfPixelsInXValidator.setMinAndMax(3, 15);
        numberOfPixelsInXValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter numberOfPixelsInXConverter = (NullGenericIntegerConverter)numberOfPixelsInXBinding.getConverter();
        numberOfPixelsInXConverter.setValidatorOnConvertForward(numberOfPixelsInXValidator);
        numberOfPixelsInXConverter.addStatusListener(borderNumberOfPixelsInX);
    }
    
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsGaussian2DModel, Integer> subPixelInterpolationOptionsGaussian2DModelBeanProperty = BeanProperty.create("numberOfPixelsInX");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        numberOfPixelsInXBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianOptions, subPixelInterpolationOptionsGaussian2DModelBeanProperty, fmtdTxtFldNumberPixelsX, jFormattedTextFieldBeanProperty, "numberOfPixelsInXBinding");
        numberOfPixelsInXBinding.setConverter(new NullGenericIntegerConverter());
        numberOfPixelsInXBinding.setValidator(new IntegerRangeValidator());
        numberOfPixelsInXBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsGaussian2DModel, Integer> subPixelInterpolationOptionsGaussian2DModelBeanProperty_1 = BeanProperty.create("numberOfPixelsInY");
        numberOfPixelsInYBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianOptions, subPixelInterpolationOptionsGaussian2DModelBeanProperty_1, fmtdTxtFldNumberPixelsY, jFormattedTextFieldBeanProperty, "numberOfPixelsInYBinding");
        numberOfPixelsInYBinding.setConverter(new NullGenericIntegerConverter());
        numberOfPixelsInYBinding.setValidator(new IntegerRangeValidator());
        numberOfPixelsInYBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsGaussian2DModel, Gaussian2DStrategiesEnum> subPixelInterpolationOptionsGaussian2DModelBeanProperty_2 = BeanProperty.create("algorithm");
        BeanProperty<JComboBox<Gaussian2DStrategiesEnum>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        selectedAlgorithmBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianOptions, subPixelInterpolationOptionsGaussian2DModelBeanProperty_2, comboBoxGaussianStrategy, jComboBoxBeanProperty, "selectedAlgorithmBinding");
        selectedAlgorithmBinding.setConverter(new NullGaussian2DStrategiesConverter());
        selectedAlgorithmBinding.setValidator(new InvalidNotAllowedGaussian2DStrategiesValidator());
        selectedAlgorithmBinding.bind();
    }
    
    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    public void setParentDialog(JDialog _dialog) {
    }

    @Override
    public void dispose() {
        
    }

}
