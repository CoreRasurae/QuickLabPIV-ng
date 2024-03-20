// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.DataExportEnvFacade;
import pt.quickLabPIV.ui.models.AppContextModel;
import javax.swing.JFormattedTextField;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;

public class DataExportEnvironmentConfigurationDialog extends JDialog {
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> numberOfMapsBinding;
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> splitExportsEnableBinding;
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> swapUVOrderBinding;

    /**
     * 
     */
    private static final long serialVersionUID = 6475695568243360504L;
    private final JPanel contentPanel = new JPanel();

    private AppContextModel appContext;
    private JCheckBox chckbxSwap;
    
    private boolean cancelled = false;
    private JCheckBox chckbxSplitExports;
    private JFormattedTextField formattedTextFieldNumberOfMapsPerFile;
    
    
    private List<ErrorBorderForComponent> borders = new LinkedList<>();
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            DataExportEnvironmentConfigurationDialog dialog = new DataExportEnvironmentConfigurationDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public DataExportEnvironmentConfigurationDialog() {
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 737, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Data export environment configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 73, 204, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            chckbxSwap = new JCheckBox("Swap <U,V> vector coordinates");
            GridBagConstraints gbc_chckbxSwap = new GridBagConstraints();
            gbc_chckbxSwap.fill = GridBagConstraints.HORIZONTAL;
            gbc_chckbxSwap.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxSwap.gridx = 1;
            gbc_chckbxSwap.gridy = 1;
            contentPanel.add(chckbxSwap, gbc_chckbxSwap);
        }
        {
            chckbxSplitExports = new JCheckBox("Split exports across mutliple files each having");
            chckbxSplitExports.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    updateTextFieldEnabledState();
                }
            });
            GridBagConstraints gbc_chckbxSplitExports = new GridBagConstraints();
            gbc_chckbxSplitExports.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxSplitExports.gridx = 1;
            gbc_chckbxSplitExports.gridy = 2;
            contentPanel.add(chckbxSplitExports, gbc_chckbxSplitExports);
        }
        {
            formattedTextFieldNumberOfMapsPerFile = new JFormattedTextField(createNumberOfFramesFormatter());
            GridBagConstraints gbc_formattedTextFieldNumberOfMapsPerFile = new GridBagConstraints();
            gbc_formattedTextFieldNumberOfMapsPerFile.insets = new Insets(0, 0, 5, 5);
            gbc_formattedTextFieldNumberOfMapsPerFile.fill = GridBagConstraints.HORIZONTAL;
            gbc_formattedTextFieldNumberOfMapsPerFile.gridx = 2;
            gbc_formattedTextFieldNumberOfMapsPerFile.gridy = 2;
            contentPanel.add(formattedTextFieldNumberOfMapsPerFile, gbc_formattedTextFieldNumberOfMapsPerFile);
        }
        {
            JLabel lblImagesVectorMaps = new JLabel("image(s) vector maps");
            GridBagConstraints gbc_lblImagesVectorMaps = new GridBagConstraints();
            gbc_lblImagesVectorMaps.anchor = GridBagConstraints.WEST;
            gbc_lblImagesVectorMaps.insets = new Insets(0, 0, 5, 5);
            gbc_lblImagesVectorMaps.gridx = 3;
            gbc_lblImagesVectorMaps.gridy = 2;
            contentPanel.add(lblImagesVectorMaps, gbc_lblImagesVectorMaps);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        confirmChanges(true);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        confirmChanges(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        initDataBindings();
        postInitDataBindings();
    }

    protected void updateTextFieldEnabledState() {
        if (chckbxSplitExports.isSelected()) {
            formattedTextFieldNumberOfMapsPerFile.setEnabled(true);
        } else {
            formattedTextFieldNumberOfMapsPerFile.setEnabled(false);
        }
        
    }

    protected void confirmChanges(boolean confirmed) {
        if (confirmed) {
            for (ErrorBorderForComponent border : borders) {
                if (border.isErrored()) {
                    JOptionPane.showMessageDialog(this, "Data entered is incorrect, or missing.\n"
                            + "Please correct or complete fields and try again.", "PIV Data Export Configuration", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            cancelled = false;
        } else {
            cancelled = true;
        }
        
        setVisible(false);        
    }
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        DataExportEnvFacade.getOrCreateDefaultDataExportConfiguration(appContext);
        
        swapUVOrderBinding.unbind();
        swapUVOrderBinding.setSourceObject(model);
        swapUVOrderBinding.bind();
        
        splitExportsEnableBinding.unbind();
        splitExportsEnableBinding.setSourceObject(model);
        splitExportsEnableBinding.bind();
        
        numberOfMapsBinding.unbind();
        numberOfMapsBinding.setSourceObject(model);
        numberOfMapsBinding.bind();
        
        updateTextFieldEnabledState();
    }
    
    public AppContextModel getAppContext() {
        return appContext;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    private DefaultFormatter createNumberOfFramesFormatter() {
        NumberFormat format  = new DecimalFormat("#######");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(7);
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }

    protected void initDataBindings() {
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty = BeanProperty.create("project.exportConfiguration.swapUVOrder");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        swapUVOrderBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty, chckbxSwap, jCheckBoxBeanProperty, "swapUVOrderBinding");
        swapUVOrderBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_1 = BeanProperty.create("project.exportConfiguration.splitExports");
        splitExportsEnableBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_1, chckbxSplitExports, jCheckBoxBeanProperty, "splitExportsEnableBinding");
        splitExportsEnableBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty_2 = BeanProperty.create("project.exportConfiguration.numberOfPIVMapsPerExportedFile");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        numberOfMapsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_2, formattedTextFieldNumberOfMapsPerFile, jFormattedTextFieldBeanProperty, "numberOfMapsBinding");
        numberOfMapsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfMapsBinding.setValidator(new IntegerRangeValidator());
        numberOfMapsBinding.bind();
    }
    
    protected void postInitDataBindings() {
        ErrorBorderForComponent numberOfMapsBorder = new ErrorBorderForComponent(formattedTextFieldNumberOfMapsPerFile);
        formattedTextFieldNumberOfMapsPerFile.setBorder(numberOfMapsBorder);
        numberOfMapsBinding.addBindingListener(numberOfMapsBorder);
        borders.add(numberOfMapsBorder);
        //
        IntegerRangeValidator numberOfMapsValidator = (IntegerRangeValidator)numberOfMapsBinding.getValidator();
        numberOfMapsValidator.setMinAndMax(0, Integer.MAX_VALUE);
        NullGenericIntegerConverter numberOfMapsConverter = (NullGenericIntegerConverter)numberOfMapsBinding.getConverter();
        numberOfMapsConverter.setValidatorOnConvertForward(numberOfMapsValidator);
    }
    protected JCheckBox getChckbxSplitExports() {
        return chckbxSplitExports;
    }
    protected JFormattedTextField getFormattedTextFieldNumberOfMapsPerFile() {
        return formattedTextFieldNumberOfMapsPerFile;
    }
}
