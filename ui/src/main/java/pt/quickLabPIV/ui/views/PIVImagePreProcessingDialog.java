package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.ui.controllers.PIVConfigurationFacade;
import pt.quickLabPIV.ui.converters.NullImageFilteringModeConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeComboBoxModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;
import pt.quickLabPIV.ui.views.panels.IPanelWithErrorBorders;

public class PIVImagePreProcessingDialog extends JDialog {
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> filterAfterWarpingBinding;
    private AutoBinding<AppContextModel, ImageFilteringModeEnum, JComboBox<ImageFilteringModeEnum>, Object> imageFilteringModeBinding;

    /**
     * 
     */
    private static final long serialVersionUID = 6788137705079343155L;
    private final JPanel contentPanel = new JPanel();
    private AppContextModel appContext;
    private List<ErrorBorderForComponent> borders = new LinkedList<ErrorBorderForComponent>();
    private ImageFilteringModeComboBoxModel imageFilteringModeComboModel = new ImageFilteringModeComboBoxModel();
    private JComboBox<ImageFilteringModeEnum> comboBoxImageFilter;
    private JCheckBox chckbxCheckIfFilter;
    private JPanel panelFilterConfigurationOptions;
    private JPanel currentImageFilteringOptionsPanel;
    private JTabbedPane tabbedPane;
    private JPanel panelImgFiltering;
    private boolean canceled = false;
    
    public enum PIVImagePreProcessingTabEnum {
        ImageFiltering(0);

        private int index;
        
        private PIVImagePreProcessingTabEnum(int _index) {
            index = _index;
        }
        
        private int getIndex() {
            return index;
        }
    }
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            PIVImagePreProcessingDialog dialog = new PIVImagePreProcessingDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public PIVImagePreProcessingDialog() {
        setTitle("Image Pre-Processing configuration");
        setResizable(false);
        setBounds(100, 100, 718, 347);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{315, 0};
        gbl_contentPanel.rowHeights = new int[]{29, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
            gbc_tabbedPane.fill = GridBagConstraints.BOTH;
            gbc_tabbedPane.gridx = 0;
            gbc_tabbedPane.gridy = 0;
            contentPanel.add(tabbedPane, gbc_tabbedPane);
            {
                panelImgFiltering = new JPanel();
                tabbedPane.addTab("Image filtering", null, panelImgFiltering, null);
                GridBagLayout gbl_panelImgFiltering = new GridBagLayout();
                gbl_panelImgFiltering.columnWidths = new int[]{0, 0};
                gbl_panelImgFiltering.rowHeights = new int[]{0, 0, 0, 0};
                gbl_panelImgFiltering.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelImgFiltering.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
                panelImgFiltering.setLayout(gbl_panelImgFiltering);
                {
                    JPanel panelImgFilteringConfig = new JPanel();
                    panelImgFilteringConfig.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Image filtering configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                    GridBagConstraints gbc_panelImgFilteringConfig = new GridBagConstraints();
                    gbc_panelImgFilteringConfig.gridwidth = 2;
                    gbc_panelImgFilteringConfig.gridheight = 3;
                    gbc_panelImgFilteringConfig.fill = GridBagConstraints.BOTH;
                    gbc_panelImgFilteringConfig.gridx = 0;
                    gbc_panelImgFilteringConfig.gridy = 0;
                    panelImgFiltering.add(panelImgFilteringConfig, gbc_panelImgFilteringConfig);
                    GridBagLayout gbl_panelImgFilteringConfig = new GridBagLayout();
                    gbl_panelImgFilteringConfig.columnWidths = new int[]{125, 70};
                    gbl_panelImgFilteringConfig.rowHeights = new int[]{15, 0, 0};
                    gbl_panelImgFilteringConfig.columnWeights = new double[]{0.0, 1.0};
                    gbl_panelImgFilteringConfig.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                    panelImgFilteringConfig.setLayout(gbl_panelImgFilteringConfig);
                    {
                        JPanel panelFilterSelection = new JPanel();
                        panelFilterSelection.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128)), "Filter selection", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                        GridBagConstraints gbc_panelFilterSelection = new GridBagConstraints();
                        gbc_panelFilterSelection.gridwidth = 2;
                        gbc_panelFilterSelection.insets = new Insets(0, 0, 5, 0);
                        gbc_panelFilterSelection.fill = GridBagConstraints.BOTH;
                        gbc_panelFilterSelection.gridx = 0;
                        gbc_panelFilterSelection.gridy = 0;
                        panelImgFilteringConfig.add(panelFilterSelection, gbc_panelFilterSelection);
                        GridBagLayout gbl_panelFilterSelection = new GridBagLayout();
                        gbl_panelFilterSelection.columnWidths = new int[]{105, 148, 0};
                        gbl_panelFilterSelection.rowHeights = new int[]{25, 18, 0};
                        gbl_panelFilterSelection.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                        gbl_panelFilterSelection.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
                        panelFilterSelection.setLayout(gbl_panelFilterSelection);
                        {
                            JLabel lblImageFilter = new JLabel("Image filter");
                            GridBagConstraints gbc_lblImageFilter = new GridBagConstraints();
                            gbc_lblImageFilter.anchor = GridBagConstraints.EAST;
                            gbc_lblImageFilter.insets = new Insets(0, 0, 5, 5);
                            gbc_lblImageFilter.gridx = 0;
                            gbc_lblImageFilter.gridy = 0;
                            panelFilterSelection.add(lblImageFilter, gbc_lblImageFilter);
                        }
                        {
                            comboBoxImageFilter = new JComboBox<>();
                            GridBagConstraints gbc_comboBoxImageFilter = new GridBagConstraints();
                            gbc_comboBoxImageFilter.fill = GridBagConstraints.HORIZONTAL;
                            gbc_comboBoxImageFilter.anchor = GridBagConstraints.NORTH;
                            gbc_comboBoxImageFilter.insets = new Insets(0, 0, 5, 0);
                            gbc_comboBoxImageFilter.gridx = 1;
                            gbc_comboBoxImageFilter.gridy = 0;
                            panelFilterSelection.add(comboBoxImageFilter, gbc_comboBoxImageFilter);
                            comboBoxImageFilter.setModel(imageFilteringModeComboModel);
                            comboBoxImageFilter.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    updateImageFilteringOptionsPanel((ImageFilteringModeEnum)comboBoxImageFilter.getSelectedItem());
                                }
                            });
                        }
                        {
                            chckbxCheckIfFilter = new JCheckBox("Check if filter should only be applied after Warping the image, if Warping is enabled");
                            GridBagConstraints gbc_chckbxCheckIfFilter = new GridBagConstraints();
                            gbc_chckbxCheckIfFilter.anchor = GridBagConstraints.NORTHWEST;
                            gbc_chckbxCheckIfFilter.gridx = 1;
                            gbc_chckbxCheckIfFilter.gridy = 1;
                            panelFilterSelection.add(chckbxCheckIfFilter, gbc_chckbxCheckIfFilter);
                        }
                    }
                    {
                        panelFilterConfigurationOptions = new JPanel();
                        GridBagConstraints gbc_panelFilterConfiguration = new GridBagConstraints();
                        gbc_panelFilterConfiguration.gridwidth = 2;
                        gbc_panelFilterConfiguration.fill = GridBagConstraints.BOTH;
                        gbc_panelFilterConfiguration.gridx = 0;
                        gbc_panelFilterConfiguration.gridy = 1;
                        panelImgFilteringConfig.add(panelFilterConfigurationOptions, gbc_panelFilterConfiguration);
                        GridBagLayout gbl_panelFilterConfigurationOptions = new GridBagLayout();
                        gbl_panelFilterConfigurationOptions.columnWidths = new int[]{0};
                        gbl_panelFilterConfigurationOptions.rowHeights = new int[]{0};
                        gbl_panelFilterConfigurationOptions.columnWeights = new double[]{Double.MIN_VALUE};
                        gbl_panelFilterConfigurationOptions.rowWeights = new double[]{Double.MIN_VALUE};
                        panelFilterConfigurationOptions.setLayout(gbl_panelFilterConfigurationOptions);
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        validateAndClose(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        validateAndClose(true);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        initDataBindings();
    }
    
    public AppContextModel getAppContext() {
        return appContext;
    }

    public void setAppContext(AppContextModel model) {
        appContext = model;
        ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        
        imageFilteringModeBinding.unbind();
        imageFilteringModeBinding.setSourceNullValue(ImageFilteringModeEnum.DoNotApplyImageFiltering);
        imageFilteringModeBinding.setSourceObject(appContext);
        imageFilteringModeBinding.bind();
        
        filterAfterWarpingBinding.unbind();
        filterAfterWarpingBinding.setSourceObject(appContext);
        filterAfterWarpingBinding.bind();
    }
    
    private void addOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        borders.addAll(panelBorders);
    }
    
    private void removeOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        borders.removeAll(panelBorders);
    }
    
    private void updateImageFilteringOptionsPanel(ImageFilteringModeEnum selectedAlgorithm) {
        if (currentImageFilteringOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentImageFilteringOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelFilterConfigurationOptions.remove(currentImageFilteringOptionsPanel);
        }
        
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createImageFilterOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentImageFilteringOptionsPanel = 
                PIVConfigurationFacade.updateImageFilterOptionPanelFromSelection(this, panelFilterConfigurationOptions, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentImageFilteringOptionsPanel;
        if (panelWithBorders != null) {
            addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }
    }

    private boolean validateForErrors() {
        //PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        for (ErrorBorderForComponent border : borders) {
            if ((border.getComponent() == null || border.getComponent().isEnabled()) && border.isErrored()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void validateAndClose(boolean cancel) {
        canceled = cancel;
        if (!cancel && !validateForErrors()) {
            JOptionPane.showMessageDialog(this, "Data entered is incorrect, or missing.\n"
                    + "Please correct or complete fields and try again.", "PIV Image Pre-Processing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        //
        setVisible(false);
    }

    public void selectTab(PIVImagePreProcessingTabEnum startTab) {
        tabbedPane.setSelectedIndex(startTab.getIndex());
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    protected JComboBox<ImageFilteringModeEnum> getComboBoxImageFilter() {
        return comboBoxImageFilter;
    }
    protected JCheckBox getChckbxCheckIfFilter() {
        return chckbxCheckIfFilter;
    }
    protected JPanel getPanelFilterConfiguration() {
        return panelFilterConfigurationOptions;
    }

    protected JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    protected JPanel getPanelImgFiltering() {
        return panelImgFiltering;
    }
    protected void initDataBindings() {
        BeanProperty<AppContextModel, ImageFilteringModeEnum> appContextModelBeanProperty = BeanProperty.create("project.PIVConfiguration.imageFilteringMode");
        BeanProperty<JComboBox<ImageFilteringModeEnum>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        imageFilteringModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty, comboBoxImageFilter, jComboBoxBeanProperty, "imageFilteringModeBinding");
        imageFilteringModeBinding.setConverter(new NullImageFilteringModeConverter());
        imageFilteringModeBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_1 = BeanProperty.create("project.PIVConfiguration.imageFilteringAfterWarpingOnly");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        filterAfterWarpingBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_1, chckbxCheckIfFilter, jCheckBoxBeanProperty, "filterAfterWarpingBinding");
        filterAfterWarpingBinding.bind();
    }
}
