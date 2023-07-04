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
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationOptionsMultiPeakNormalizedMedianModel;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class VectorValidationMultiPeakNormalizedMedianOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    /**
     * 
     */
    private static final long serialVersionUID = -8370063199680354354L;
    /**
     * 
     */
    private AutoBinding<VelocityValidationOptionsMultiPeakNormalizedMedianModel, Integer, JFormattedTextField, Object> numberOfPeaksBinding;
    private AutoBinding<VelocityValidationOptionsMultiPeakNormalizedMedianModel, Integer, JFormattedTextField, Object> kernelSizeBinding;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(2);


    private AppContextModel appContext;
    private VelocityValidationOptionsMultiPeakNormalizedMedianModel multiPeakOptions = new VelocityValidationOptionsMultiPeakNormalizedMedianModel();
    private JFormattedTextField formattedTextFieldNumberOfPeaks;
    private JFormattedTextField formattedTextFieldKernelSize;
    private VectorValidationNormalizedMedianOptionsPanel normalizedMedianPanel = new VectorValidationNormalizedMedianOptionsPanel();
    
    /**
     * Create the panel.
     */
    public VectorValidationMultiPeakNormalizedMedianOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JPanel panelMultiPeak = new JPanel();
        panelMultiPeak.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Multi-peak search configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        GridBagConstraints gbc_panelMultiPeak = new GridBagConstraints();
        gbc_panelMultiPeak.gridwidth = 2;
        gbc_panelMultiPeak.insets = new Insets(0, 0, 5, 0);
        gbc_panelMultiPeak.fill = GridBagConstraints.BOTH;
        gbc_panelMultiPeak.gridx = 0;
        gbc_panelMultiPeak.gridy = 0;
        add(panelMultiPeak, gbc_panelMultiPeak);
        GridBagLayout gbl_panelMultiPeak = new GridBagLayout();
        gbl_panelMultiPeak.columnWidths = new int[]{189, 66, 0};
        gbl_panelMultiPeak.rowHeights = new int[]{15, 0, 0};
        gbl_panelMultiPeak.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelMultiPeak.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        panelMultiPeak.setLayout(gbl_panelMultiPeak);
        
        JLabel labelPeaksNumber = new JLabel("Number of peaks to search for");
        GridBagConstraints gbc_labelPeaksNumber = new GridBagConstraints();
        gbc_labelPeaksNumber.insets = new Insets(0, 0, 5, 5);
        gbc_labelPeaksNumber.anchor = GridBagConstraints.NORTHWEST;
        gbc_labelPeaksNumber.gridx = 0;
        gbc_labelPeaksNumber.gridy = 0;
        panelMultiPeak.add(labelPeaksNumber, gbc_labelPeaksNumber);
        
        formattedTextFieldNumberOfPeaks = new JFormattedTextField(createPeaksNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldNumberOfPeaks = new GridBagConstraints();
        gbc_formattedTextFieldNumberOfPeaks.insets = new Insets(0, 0, 5, 0);
        gbc_formattedTextFieldNumberOfPeaks.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldNumberOfPeaks.gridx = 1;
        gbc_formattedTextFieldNumberOfPeaks.gridy = 0;
        panelMultiPeak.add(formattedTextFieldNumberOfPeaks, gbc_formattedTextFieldNumberOfPeaks);
        
        JLabel lblKernelSize = new JLabel("Kernel size");
        GridBagConstraints gbc_lblKernelSize = new GridBagConstraints();
        gbc_lblKernelSize.anchor = GridBagConstraints.WEST;
        gbc_lblKernelSize.insets = new Insets(0, 0, 0, 5);
        gbc_lblKernelSize.gridx = 0;
        gbc_lblKernelSize.gridy = 1;
        panelMultiPeak.add(lblKernelSize, gbc_lblKernelSize);
        
        formattedTextFieldKernelSize = new JFormattedTextField(createKernelSizeNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldKernelSize = new GridBagConstraints();
        gbc_formattedTextFieldKernelSize.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldKernelSize.gridx = 1;
        gbc_formattedTextFieldKernelSize.gridy = 1;
        panelMultiPeak.add(formattedTextFieldKernelSize, gbc_formattedTextFieldKernelSize);
        
        JPanel panelValidator = new JPanel();
        panelValidator.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Normalized Median validator configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        GridBagConstraints gbc_panelValidator = new GridBagConstraints();
        gbc_panelValidator.gridwidth = 2;
        gbc_panelValidator.fill = GridBagConstraints.BOTH;
        gbc_panelValidator.gridx = 0;
        gbc_panelValidator.gridy = 1;
        add(panelValidator, gbc_panelValidator);
        GridBagLayout gbl_panelValidator = new GridBagLayout();
        gbl_panelValidator.columnWidths = new int[]{0};
        gbl_panelValidator.rowHeights = new int[]{0};
        gbl_panelValidator.columnWeights = new double[]{Double.MIN_VALUE};
        gbl_panelValidator.rowWeights = new double[]{Double.MIN_VALUE};
        panelValidator.setLayout(gbl_panelValidator);
        GridBagConstraints gbc_normalizedMedianPanel = new GridBagConstraints();
        gbc_normalizedMedianPanel.fill = GridBagConstraints.HORIZONTAL;
        panelValidator.add(normalizedMedianPanel, gbc_normalizedMedianPanel);
        initDataBindings();
        postInitDataBindings();
    }

    private DefaultFormatter createPeaksNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(2);
        //format.setGroupingUsed(false);     //NOTE: This will cause decimal separator to stop working too - Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private DefaultFormatter createKernelSizeNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(1);
        format.setGroupingUsed(false);     //NOTE: Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    public void setAppContextModel(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        multiPeakOptions = (VelocityValidationOptionsMultiPeakNormalizedMedianModel)pivModel.getValidationOption(VelocityValidationModeEnum.MultiPeakNormalizedMedian);
        //
        kernelSizeBinding.unbind();
        kernelSizeBinding.setSourceObject(multiPeakOptions);
        kernelSizeBinding.bind();
        //
        numberOfPeaksBinding.unbind();
        numberOfPeaksBinding.setSourceObject(multiPeakOptions);
        numberOfPeaksBinding.bind();
        //
        normalizedMedianPanel.setAppContextModel(model, multiPeakOptions);
    }
    
    private void postInitDataBindings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderNumberOfPeaks = new ErrorBorderForComponent(formattedTextFieldNumberOfPeaks);
        formattedTextFieldNumberOfPeaks.setBorder(borderNumberOfPeaks);
        numberOfPeaksBinding.addBindingListener(borderNumberOfPeaks);
        errorBorders.add(borderNumberOfPeaks);
        //
        ErrorBorderForComponent borderKernelSize = new ErrorBorderForComponent(formattedTextFieldKernelSize);
        formattedTextFieldKernelSize.setBorder(borderKernelSize);
        kernelSizeBinding.addBindingListener(borderKernelSize);
        errorBorders.add(borderKernelSize);
        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        NullGenericIntegerConverter numberOfPeaksConverter = (NullGenericIntegerConverter)numberOfPeaksBinding.getConverter();
        IntegerRangeValidator numberOfPeaksValidator = (IntegerRangeValidator)numberOfPeaksBinding.getValidator();
        numberOfPeaksValidator.setMinAndMax(1, 5);
        numberOfPeaksConverter.setValidatorOnConvertForward(numberOfPeaksValidator);
        numberOfPeaksConverter.addStatusListener(borderNumberOfPeaks);
        //
        IntegerRangeValidator kernelSizeValidator = (IntegerRangeValidator)kernelSizeBinding.getValidator();
        kernelSizeValidator.setMinAndMax(3, 9);
        NullGenericIntegerConverter kernelSizeConverter = (NullGenericIntegerConverter)kernelSizeBinding.getConverter();
        kernelSizeConverter.setValidatorOnConvertForward(kernelSizeValidator);
        kernelSizeConverter.addStatusListener(borderKernelSize);
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {     
        return errorBorders;
    }
    
    protected void initDataBindings() {
        BeanProperty<VelocityValidationOptionsMultiPeakNormalizedMedianModel, Integer> velocityValidationOptionsNormalizedMedianModelBeanProperty = BeanProperty.create("numberOfPeaks");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        numberOfPeaksBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, multiPeakOptions, velocityValidationOptionsNormalizedMedianModelBeanProperty, formattedTextFieldNumberOfPeaks, jFormattedTextFieldBeanProperty, "distanceThresholdPixelsBinding");
        numberOfPeaksBinding.setConverter(new NullGenericIntegerConverter());
        numberOfPeaksBinding.setValidator(new IntegerRangeValidator());
        numberOfPeaksBinding.bind();
        //
        BeanProperty<VelocityValidationOptionsMultiPeakNormalizedMedianModel, Integer> velocityValidationOptionsNormalizedMedianModelBeanProperty_1 = BeanProperty.create("kernelSize");
        kernelSizeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, multiPeakOptions, velocityValidationOptionsNormalizedMedianModelBeanProperty_1, formattedTextFieldKernelSize, jFormattedTextFieldBeanProperty, "kernelSizeBinding");
        kernelSizeBinding.setConverter(new NullGenericIntegerConverter());
        kernelSizeBinding.setValidator(new IntegerRangeValidator());
        kernelSizeBinding.bind();
    }
    
    protected JFormattedTextField getFormattedTextFieldNumberOfPeaks() {
        return formattedTextFieldNumberOfPeaks;
    }
    
    protected JFormattedTextField getFormattedTextFieldKernelSize() {
        return formattedTextFieldKernelSize;
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }

    @Override
    public void dispose() {
        
    }
}
