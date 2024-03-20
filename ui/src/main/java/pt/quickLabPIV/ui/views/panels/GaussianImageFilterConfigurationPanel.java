// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.ui.converters.NullGenericFloatConverter;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageFilterOptionsGaussian2DModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.validators.FloatRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class GaussianImageFilterConfigurationPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<ImageFilterOptionsGaussian2DModel, Integer, JFormattedTextField, Object> widthPxBinding;
    private AutoBinding<ImageFilterOptionsGaussian2DModel, Float, JFormattedTextField, Object> sigmaBinding;

    /**
     * 
     */
    private static final long serialVersionUID = -8165903475694519011L;

    AppContextModel appContext;
    private JFormattedTextField formattedTextFieldSigma;
    private JFormattedTextField formattedTextFieldFilterWidth;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<ErrorBorderForComponent>(2);
    private ImageFilterOptionsGaussian2DModel gaussianFilterOptions;
    
    /**
     * Create the panel.
     */
    public GaussianImageFilterConfigurationPanel() {
        setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Gaussian Image filter configuration options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {30, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {30, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        setLayout(gridBagLayout);
        
        Component verticalStrut = Box.createVerticalStrut(20);
        GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
        gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut.gridx = 1;
        gbc_verticalStrut.gridy = 0;
        add(verticalStrut, gbc_verticalStrut);
        
        JLabel lblSigmaValuestd = new JLabel("Sigma value (Std. dev.)");
        GridBagConstraints gbc_lblSigmaValuestd = new GridBagConstraints();
        gbc_lblSigmaValuestd.anchor = GridBagConstraints.WEST;
        gbc_lblSigmaValuestd.insets = new Insets(0, 0, 5, 5);
        gbc_lblSigmaValuestd.gridx = 1;
        gbc_lblSigmaValuestd.gridy = 1;
        add(lblSigmaValuestd, gbc_lblSigmaValuestd);
        
        formattedTextFieldSigma = new JFormattedTextField(createSigmaNumberFormatter());
        formattedTextFieldSigma.setToolTipText("The Gaussian standard deviation value (between 0.0 and 6.0)");
        GridBagConstraints gbc_formattedTextFieldSigma = new GridBagConstraints();
        gbc_formattedTextFieldSigma.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldSigma.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldSigma.gridx = 2;
        gbc_formattedTextFieldSigma.gridy = 1;
        add(formattedTextFieldSigma, gbc_formattedTextFieldSigma);
        
        Component horizontalStrut = Box.createHorizontalStrut(20);
        GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
        gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
        gbc_horizontalStrut.gridx = 3;
        gbc_horizontalStrut.gridy = 1;
        add(horizontalStrut, gbc_horizontalStrut);
        
        JLabel lblFilterWidthpixels = new JLabel("Filter width (pixels)");
        GridBagConstraints gbc_lblFilterWidthpixels = new GridBagConstraints();
        gbc_lblFilterWidthpixels.anchor = GridBagConstraints.WEST;
        gbc_lblFilterWidthpixels.insets = new Insets(0, 0, 5, 5);
        gbc_lblFilterWidthpixels.gridx = 1;
        gbc_lblFilterWidthpixels.gridy = 2;
        add(lblFilterWidthpixels, gbc_lblFilterWidthpixels);
        
        formattedTextFieldFilterWidth = new JFormattedTextField(createWidthPixelsNumberFormatter());
        formattedTextFieldFilterWidth.setToolTipText("Select the Gaussian filter width in pixels (a value of 3 should be good and must be an odd number in any case)");
        GridBagConstraints gbc_formattedTextFieldFilterWidth = new GridBagConstraints();
        gbc_formattedTextFieldFilterWidth.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldFilterWidth.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldFilterWidth.gridx = 2;
        gbc_formattedTextFieldFilterWidth.gridy = 2;
        add(formattedTextFieldFilterWidth, gbc_formattedTextFieldFilterWidth);
        
        Component horizontalStrut_1 = Box.createHorizontalStrut(20);
        GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
        gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 0);
        gbc_horizontalStrut_1.gridx = 3;
        gbc_horizontalStrut_1.gridy = 2;
        add(horizontalStrut_1, gbc_horizontalStrut_1);
        
        Component verticalStrut_1 = Box.createVerticalStrut(20);
        GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
        gbc_verticalStrut_1.insets = new Insets(0, 0, 0, 5);
        gbc_verticalStrut_1.gridx = 1;
        gbc_verticalStrut_1.gridy = 3;
        add(verticalStrut_1, gbc_verticalStrut_1);
        initDataBindings();
        postInitDataBindings();
    }

    private DefaultFormatter createSigmaNumberFormatter() {
        DecimalFormat format  = new DecimalFormat("0.0#");
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(1);
        format.setParseIntegerOnly(false);
        format.setGroupingUsed(false);     //Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.0f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private DefaultFormatter createWidthPixelsNumberFormatter() {
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
        gaussianFilterOptions = (ImageFilterOptionsGaussian2DModel)pivModel.getImageFilterOption(ImageFilteringModeEnum.ApplyImageFilteringGaussian2D);
        
        sigmaBinding.unbind();
        sigmaBinding.setSourceObject(gaussianFilterOptions);
        sigmaBinding.bind();
        
        widthPxBinding.unbind();
        widthPxBinding.setSourceObject(gaussianFilterOptions);
        widthPxBinding.bind();
    }

    private void postInitDataBindings() {
        //
        //////////////////////////
        //Bindings
        //////////////////////////
        //
        //Error borders attachment and registration
        ErrorBorderForComponent borderSigma = new ErrorBorderForComponent(formattedTextFieldSigma);
        formattedTextFieldSigma.setBorder(borderSigma);
        sigmaBinding.addBindingListener(borderSigma);
        errorBorders.add(borderSigma);

        ErrorBorderForComponent borderWidthPx = new ErrorBorderForComponent(formattedTextFieldFilterWidth);
        formattedTextFieldFilterWidth.setBorder(borderWidthPx);
        widthPxBinding.addBindingListener(borderWidthPx);
        errorBorders.add(borderWidthPx);
        
        //
        //Error handling
        //
        FloatRangeValidator sigmaValidator = (FloatRangeValidator)sigmaBinding.getValidator();
        sigmaValidator.setMinAndMax(0, 6);
        NullGenericFloatConverter sigmaConverter = (NullGenericFloatConverter)sigmaBinding.getConverter();
        sigmaConverter.setValidatorOnConvertForward(sigmaValidator);
        sigmaConverter.addStatusListener(borderSigma);
        
        IntegerRangeValidator widthPxValidator = (IntegerRangeValidator)widthPxBinding.getValidator();
        widthPxValidator.setMinAndMax(3, 15);
        widthPxValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter widthPxConverter = (NullGenericIntegerConverter)widthPxBinding.getConverter();
        widthPxConverter.setValidatorOnConvertForward(widthPxValidator);
        widthPxConverter.addStatusListener(borderWidthPx);

    }
    
    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    protected JFormattedTextField getFormattedTextFieldSigma() {
        return formattedTextFieldSigma;
    }
    protected JFormattedTextField getFormattedTextFieldFilterWidth() {
        return formattedTextFieldFilterWidth;
    }
    
    protected void initDataBindings() {
        BeanProperty<ImageFilterOptionsGaussian2DModel, Float> imageFilterOptionsGaussian2DModelBeanProperty = BeanProperty.create("sigma");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        sigmaBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianFilterOptions, imageFilterOptionsGaussian2DModelBeanProperty, formattedTextFieldSigma, jFormattedTextFieldBeanProperty, "sigmaBinding");
        sigmaBinding.setConverter(new NullGenericFloatConverter());
        sigmaBinding.setValidator(new FloatRangeValidator());
        sigmaBinding.bind();
        //
        BeanProperty<ImageFilterOptionsGaussian2DModel, Integer> imageFilterOptionsGaussian2DModelBeanProperty_1 = BeanProperty.create("widthPx");
        widthPxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, gaussianFilterOptions, imageFilterOptionsGaussian2DModelBeanProperty_1, formattedTextFieldFilterWidth, jFormattedTextFieldBeanProperty, "widthPxBinding");
        widthPxBinding.setConverter(new NullGenericIntegerConverter());
        widthPxBinding.setValidator(new IntegerRangeValidator());
        widthPxBinding.bind();
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }
}
