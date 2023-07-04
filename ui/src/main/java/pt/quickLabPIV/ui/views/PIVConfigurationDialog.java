package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.libs.external.DisabledPanel;
import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.ui.controllers.PIVConfigurationFacade;
import pt.quickLabPIV.ui.converters.IAResolutionToStringConverter;
import pt.quickLabPIV.ui.converters.NullClippingModeConverter;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.converters.NullInhertianceModeConverter;
import pt.quickLabPIV.ui.converters.NullInterrogationAreaEnumConverter;
import pt.quickLabPIV.ui.converters.NullMarginsConverter;
import pt.quickLabPIV.ui.converters.NullSubPixelInterpolationConverter;
import pt.quickLabPIV.ui.converters.NullVelocityStabilizationConverter;
import pt.quickLabPIV.ui.converters.NullVelocityValidationConverter;
import pt.quickLabPIV.ui.converters.NullWarpingModeConverter;
import pt.quickLabPIV.ui.converters.PercentageConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ClippingModeComboBoxModel;
import pt.quickLabPIV.ui.models.ClippingModeEnum;
import pt.quickLabPIV.ui.models.InheritanceModeComboBoxModel;
import pt.quickLabPIV.ui.models.InheritanceModeEnum;
import pt.quickLabPIV.ui.models.InterrogationAreaResolutionComboBoxModel;
import pt.quickLabPIV.ui.models.InterrogationAreaResolutionEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationComboBoxModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.VelocityStabilizationComboBoxModel;
import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationComboBoxModel;
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.models.WarpingModeComboBoxModel;
import pt.quickLabPIV.ui.models.WarpingModeEnum;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.validators.InvalidIANotAllowedValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedInheritanceValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedSubPixelInterpolationValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedVelocityStabilizationValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedVelocityValidationValidator;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedWarpingModeValidator;
import pt.quickLabPIV.ui.validators.MarginsValidator;
import pt.quickLabPIV.ui.validators.PercentageOverlapValidator;
import pt.quickLabPIV.ui.views.panels.IPanelWithErrorBorders;
import pt.quickLabPIV.ui.views.panels.ImageInfoPanel;

public class PIVConfigurationDialog extends JDialog {
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> validatorMaxIterationsBinding;
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> validatorIterateUntilNoMoreReplaced;
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> validatorReplaceAllInvalidBinding;
    private AutoBinding<AppContextModel, VelocityValidationModeEnum, JComboBox<VelocityValidationModeEnum>, Object> velocityValidationModeBinding;
    private AutoBinding<AppContextModel, Float, JFormattedTextField, Object> superpositionOverlapPercentageBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JComboBox<InterrogationAreaResolutionEnum>, Object> interpolationStartStepBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JComboBox<InterrogationAreaResolutionEnum>, Object> superpositionStartStepBinding;
    private AutoBinding<AppContextModel, InheritanceModeEnum, JComboBox<InheritanceModeEnum>, Object> inheritanceModeBinding;
    private AutoBinding<AppContextModel, VelocityStabilizationModeEnum, JComboBox<VelocityStabilizationModeEnum>, Object> velocityStabilizationModeBinding;
    private AutoBinding<AppContextModel, SubPixelInterpolationModeEnum, JComboBox<SubPixelInterpolationModeEnum>, Object> subPixelInterpolationModeBinding;
    private List<ErrorBorderForComponent> borders = new LinkedList<>();
    private AutoBinding<AppContextModel, ClippingModeEnum, JComboBox<ClippingModeEnum>, Object> clippingModeBinding;
    private AutoBinding<AppContextModel, WarpingModeEnum, JComboBox<WarpingModeEnum>, Object> warpingModeBinding;
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> topMarginBinding;
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> bottomMarginBinding;
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> rightMarginBinding;
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, Object> leftMarginBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JComboBox<InterrogationAreaResolutionEnum>, Object> iaInitialResolutionComboBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JComboBox<InterrogationAreaResolutionEnum>, Object> iaEndResolutionComboBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JLabel, String> labelClippingInitialAreaBinding;
    private AutoBinding<AppContextModel, InterrogationAreaResolutionEnum, JLabel, String> labelClippingFinalAreaBinding;
    /**
     * 
     */
    private static final long serialVersionUID = 5020012853606688712L;
    
    private final JPanel contentPanel = new JPanel();
    private AppContextModel appContext;
    private JPanel panelInterrogationArea;
    private JTabbedPane tabbedPane;
    private InterrogationAreaResolutionComboBoxModel initialIAResolutionComboModel;
    private JComboBox<InterrogationAreaResolutionEnum> comboBoxIAEndResolution;    
    private JComboBox<InterrogationAreaResolutionEnum> comboBoxIAInitialResolution;
    private JLabel lblAdaptiveStepsCount;
    private JFormattedTextField frmtdtxtfldLeftMargin;
    private JFormattedTextField frmtdtxtfldRightMargin;
    private JFormattedTextField frmtdtxtfldBottomMargin;
    private JFormattedTextField frmtdtxtfldTopMargin;
    private JComboBox<ClippingModeEnum> comboBoxClippingMode;
    private ClippingModeComboBoxModel clippingModeComboModel;
    private JComboBox<WarpingModeEnum> comboBoxWarpingMode;
    private WarpingModeComboBoxModel warpingModeComboModel;
    private ImageInfoPanel panelInfo;
    private ImageInfoPanel panelClippingInfo;
    private ImageInfoPanel panelWarpingImageInfo;
    private ImageInfoPanel panelInheritanceImageInfo;
    private ImageInfoPanel panelSuperpositionImageInfo;
    private ImageInfoPanel panelInterpolationImageInfo;
    private ImageInfoPanel panelStabilizationImageInfo;
    private ImageInfoPanel panelValidationImageInfo;
    private JLabel lblFinalArea;
    private JLabel lblInitialarea;
    private boolean cancelled;
    private JPanel panelSuperpositionParameters;
    private JPanel panelInterpolationConfig;
    private JPanel panelStabilizationConfig;
    private JPanel panelValidationConfig;
    private JCheckBox chckbxEnableVelocityStabilization;
    private JCheckBox chckbxEnableSubpixelInterpolation;
    private JCheckBox chckbxEnableSuperposition;
    private JCheckBox chckbxEnableVectorValidation;
    private JComboBox<SubPixelInterpolationModeEnum> comboBoxInterpolationAlgorithm;
    private JComboBox<VelocityStabilizationModeEnum> comboBoxStabilizationStrategy;
    private JComboBox<InheritanceModeEnum> comboBoxInheritance;
    private JComboBox<InterrogationAreaResolutionEnum> comboBoxInterpolationStartStep;
    private JComboBox<InterrogationAreaResolutionEnum> comboBoxSuperpositionStartStep;
    private JComboBox<VelocityValidationModeEnum> comboBoxValidationStrategy;
    private InterrogationAreaResolutionComboBoxModel superpositionStartStepComboModel;
    private InterrogationAreaResolutionComboBoxModel interpolationStartStepComboModel;
    private JFormattedTextField frmtdtxtfldSuperpositionPercentage;
    private JPanel currentSubPixelInterpolationOptionsPanel;
    private JPanel currentVelocityStabilizationOptionsPanel;
    private JPanel currentVelocityValidationOptionsPanel;
    private JPanel panelInterpolationAlgorithmOptions;
    private JPanel panelStabilizationAlgorithmOptions;
    private JPanel panelValidationAlgorithmOptions;
    private JCheckBox chckbxReplaceAllInvalid;
    private JCheckBox chckbxIterateUntilNoMoreReplaced;
    private JFormattedTextField frmtdtxtfldMaxiterations;
    private JScrollPane scrollPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            PIVConfigurationDialog dialog = new PIVConfigurationDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public enum PIVConfigurationTabEnum {
        InterrogationAreaTab(0),
        ClippingModeTab(1),
        WarpingModeTab(2),
        VelocityInheritanceTab(3),
        SuperpositionTab(4),
        SubPixelInterpolationTab(5),
        VelocityStabilizationTab(6),
        ValidationTab(7);
        
        private int index;
        
        private PIVConfigurationTabEnum(int _index) {
            index = _index;
        }
        
        private int getIndex() {
            return index;
        }
    }
    
    /**
     * Create the dialog.
     */
    public PIVConfigurationDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("PIV Configuration");
        setBounds(100, 100, 800, 400);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        dimension.height -= 80;
        Dimension minDimension = new Dimension(800, 400);
        setMinimumSize(minDimension);
        setPreferredSize(minDimension);
        setMaximumSize(dimension);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            {
                {
                    initialIAResolutionComboModel = new InterrogationAreaResolutionComboBoxModel();
                }
            }
            {
                {
                    {
                        clippingModeComboModel = new ClippingModeComboBoxModel();
                    }
                }
            }
            {
                {
                    {
                        warpingModeComboModel = new WarpingModeComboBoxModel();
                    }
                }
            }
            {
                {                    
                    {
                        superpositionStartStepComboModel = new InterrogationAreaResolutionComboBoxModel();
                    }
                }
            }
            {
                {
                    {
                        interpolationStartStepComboModel = new InterrogationAreaResolutionComboBoxModel();
                    }
                }
            }
        }
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{837, 0};
        gbl_contentPanel.rowHeights = new int[]{339, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            scrollPane = new JScrollPane();
            GridBagConstraints gbc_scrollPane = new GridBagConstraints();
            gbc_scrollPane.fill = GridBagConstraints.BOTH;
            gbc_scrollPane.gridx = 0;
            gbc_scrollPane.gridy = 0;
            contentPanel.add(scrollPane, gbc_scrollPane);
            {
                tabbedPane = new JTabbedPane(JTabbedPane.TOP);
                scrollPane.setViewportView(tabbedPane);
                tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            }
            panelInterrogationArea = new JPanel();
            tabbedPane.addTab("Interrogation Area", null, panelInterrogationArea, "Interrogation areas dimension and adaptive steps configuration");
            GridBagLayout gbl_panelInterrogationArea = new GridBagLayout();
            gbl_panelInterrogationArea.columnWidths = new int[]{0, 0};
            gbl_panelInterrogationArea.rowHeights = new int[]{82, 0, 0, 0};
            gbl_panelInterrogationArea.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelInterrogationArea.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
            panelInterrogationArea.setLayout(gbl_panelInterrogationArea);
            {
                panelInfo = new ImageInfoPanel();
                GridBagConstraints gbc_panelInfo = new GridBagConstraints();
                gbc_panelInfo.insets = new Insets(0, 0, 5, 0);
                gbc_panelInfo.fill = GridBagConstraints.BOTH;
                gbc_panelInfo.gridx = 0;
                gbc_panelInfo.gridy = 0;
                panelInterrogationArea.add(panelInfo, gbc_panelInfo);
            }
            JPanel panelIA = new JPanel();
            panelIA.setBorder(new TitledBorder(null, "Interrogation Area configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelIA = new GridBagConstraints();
            gbc_panelIA.insets = new Insets(0, 0, 5, 0);
            gbc_panelIA.fill = GridBagConstraints.BOTH;
            gbc_panelIA.gridx = 0;
            gbc_panelIA.gridy = 1;
            panelInterrogationArea.add(panelIA, gbc_panelIA);
            GridBagLayout gbl_panelIA = new GridBagLayout();
            gbl_panelIA.columnWidths = new int[]{0, 0, 0};
            gbl_panelIA.rowHeights = new int[]{0, 0, 0};
            gbl_panelIA.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            gbl_panelIA.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
            panelIA.setLayout(gbl_panelIA);
            {
                JLabel lblFinalInterrogationArea = new JLabel("Final interrogation area resolution");
                GridBagConstraints gbc_lblFinalInterrogationArea = new GridBagConstraints();
                gbc_lblFinalInterrogationArea.insets = new Insets(0, 0, 5, 5);
                gbc_lblFinalInterrogationArea.anchor = GridBagConstraints.EAST;
                gbc_lblFinalInterrogationArea.gridx = 0;
                gbc_lblFinalInterrogationArea.gridy = 0;
                panelIA.add(lblFinalInterrogationArea, gbc_lblFinalInterrogationArea);
            }
            {
                comboBoxIAEndResolution = new JComboBox<>();
                comboBoxIAEndResolution.addActionListener(new ActionListener() {
                    @SuppressWarnings("unchecked")
                    public void actionPerformed(ActionEvent e) {
                        updateInitialComboBoxIAResolution((JComboBox<InterrogationAreaResolutionEnum>)e.getSource());
                    }
                });
                comboBoxIAEndResolution.setModel(new InterrogationAreaResolutionComboBoxModel());                        
                GridBagConstraints gbc_comboBoxIAEndResolution = new GridBagConstraints();
                gbc_comboBoxIAEndResolution.insets = new Insets(0, 0, 5, 0);
                gbc_comboBoxIAEndResolution.fill = GridBagConstraints.HORIZONTAL;
                gbc_comboBoxIAEndResolution.gridx = 1;
                gbc_comboBoxIAEndResolution.gridy = 0;
                panelIA.add(comboBoxIAEndResolution, gbc_comboBoxIAEndResolution);
            }
            {
                JLabel lblInitialInterrogationArea = new JLabel("Initial interrogation area resolution");
                GridBagConstraints gbc_lblInitialInterrogationArea = new GridBagConstraints();
                gbc_lblInitialInterrogationArea.insets = new Insets(0, 0, 0, 5);
                gbc_lblInitialInterrogationArea.gridx = 0;
                gbc_lblInitialInterrogationArea.gridy = 1;
                panelIA.add(lblInitialInterrogationArea, gbc_lblInitialInterrogationArea);
            }
            comboBoxIAInitialResolution = new JComboBox<InterrogationAreaResolutionEnum>();
            comboBoxIAInitialResolution.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    initialResolutionChanged();                            
                }
            });
            GridBagConstraints gbc_comboBoxIAInitialResolution = new GridBagConstraints();
            gbc_comboBoxIAInitialResolution.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBoxIAInitialResolution.gridx = 1;
            gbc_comboBoxIAInitialResolution.gridy = 1;
            panelIA.add(comboBoxIAInitialResolution, gbc_comboBoxIAInitialResolution);
            comboBoxIAInitialResolution.setModel(initialIAResolutionComboModel);
            {
                JPanel panelAdaptiveSteps = new JPanel();
                panelAdaptiveSteps.setBorder(new TitledBorder(null, "Adaptive steps info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                GridBagConstraints gbc_panelAdaptiveSteps = new GridBagConstraints();
                gbc_panelAdaptiveSteps.fill = GridBagConstraints.BOTH;
                gbc_panelAdaptiveSteps.gridx = 0;
                gbc_panelAdaptiveSteps.gridy = 2;
                panelInterrogationArea.add(panelAdaptiveSteps, gbc_panelAdaptiveSteps);
                GridBagLayout gbl_panelAdaptiveSteps = new GridBagLayout();
                gbl_panelAdaptiveSteps.columnWidths = new int[]{0, 0, 0};
                gbl_panelAdaptiveSteps.rowHeights = new int[]{0, 0, 0};
                gbl_panelAdaptiveSteps.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                gbl_panelAdaptiveSteps.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
                panelAdaptiveSteps.setLayout(gbl_panelAdaptiveSteps);
                {
                    JLabel lblNumberOfAdaptive = new JLabel("Number of Adaptive steps");
                    GridBagConstraints gbc_lblNumberOfAdaptive = new GridBagConstraints();
                    gbc_lblNumberOfAdaptive.anchor = GridBagConstraints.WEST;
                    gbc_lblNumberOfAdaptive.insets = new Insets(0, 0, 0, 5);
                    gbc_lblNumberOfAdaptive.gridx = 0;
                    gbc_lblNumberOfAdaptive.gridy = 1;
                    panelAdaptiveSteps.add(lblNumberOfAdaptive, gbc_lblNumberOfAdaptive);
                }
                {
                    lblAdaptiveStepsCount = new JLabel("0");
                    GridBagConstraints gbc_lblAdaptiveStepsCount = new GridBagConstraints();
                    gbc_lblAdaptiveStepsCount.gridx = 1;
                    gbc_lblAdaptiveStepsCount.gridy = 1;
                    panelAdaptiveSteps.add(lblAdaptiveStepsCount, gbc_lblAdaptiveStepsCount);
                }
            }
            tabbedPane.setEnabledAt(0, true);
            JPanel panelClipping = new JPanel();
            tabbedPane.addTab("Clipping", null, panelClipping, "Clipping mode configuration");
            GridBagLayout gbl_panelClipping = new GridBagLayout();
            gbl_panelClipping.columnWidths = new int[]{0, 0};
            gbl_panelClipping.rowHeights = new int[]{0, 0, 112, 0};
            gbl_panelClipping.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelClipping.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
            panelClipping.setLayout(gbl_panelClipping);
            {
                panelClippingInfo = new ImageInfoPanel();                    
                GridBagConstraints gbc_panelClippingInfo = new GridBagConstraints();
                gbc_panelClippingInfo.insets = new Insets(0, 0, 5, 0);
                gbc_panelClippingInfo.fill = GridBagConstraints.BOTH;
                gbc_panelClippingInfo.gridx = 0;
                gbc_panelClippingInfo.gridy = 0;
                panelClipping.add(panelClippingInfo, gbc_panelClippingInfo);
            }
            {
                JPanel panelClippingPIVInfo = new JPanel();
                panelClippingPIVInfo.setBorder(new TitledBorder(null, "Interrogation Area info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                GridBagConstraints gbc_panelClippingPIVInfo = new GridBagConstraints();
                gbc_panelClippingPIVInfo.insets = new Insets(0, 0, 5, 0);
                gbc_panelClippingPIVInfo.fill = GridBagConstraints.BOTH;
                gbc_panelClippingPIVInfo.gridx = 0;
                gbc_panelClippingPIVInfo.gridy = 1;
                panelClipping.add(panelClippingPIVInfo, gbc_panelClippingPIVInfo);
                GridBagLayout gbl_panelClippingPIVInfo = new GridBagLayout();
                gbl_panelClippingPIVInfo.columnWidths = new int[]{0, 0, 0};
                gbl_panelClippingPIVInfo.rowHeights = new int[]{0, 0, 0, 0};
                gbl_panelClippingPIVInfo.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
                gbl_panelClippingPIVInfo.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
                panelClippingPIVInfo.setLayout(gbl_panelClippingPIVInfo);
                {
                    JLabel lblFinalInterrogationArea_1 = new JLabel("Final Interrogation Area");
                    GridBagConstraints gbc_lblFinalInterrogationArea_1 = new GridBagConstraints();
                    gbc_lblFinalInterrogationArea_1.insets = new Insets(0, 0, 5, 5);
                    gbc_lblFinalInterrogationArea_1.gridx = 0;
                    gbc_lblFinalInterrogationArea_1.gridy = 0;
                    panelClippingPIVInfo.add(lblFinalInterrogationArea_1, gbc_lblFinalInterrogationArea_1);
                }
                {
                    lblFinalArea = new JLabel("Final area");
                    GridBagConstraints gbc_lblFinalArea = new GridBagConstraints();
                    gbc_lblFinalArea.insets = new Insets(0, 0, 5, 0);
                    gbc_lblFinalArea.gridx = 1;
                    gbc_lblFinalArea.gridy = 0;
                    panelClippingPIVInfo.add(lblFinalArea, gbc_lblFinalArea);
                }
                {
                    JLabel lblInitialInterrogationArea_1 = new JLabel("Initial Interrogation Area");
                    GridBagConstraints gbc_lblInitialInterrogationArea_1 = new GridBagConstraints();
                    gbc_lblInitialInterrogationArea_1.insets = new Insets(0, 0, 5, 5);
                    gbc_lblInitialInterrogationArea_1.gridx = 0;
                    gbc_lblInitialInterrogationArea_1.gridy = 1;
                    panelClippingPIVInfo.add(lblInitialInterrogationArea_1, gbc_lblInitialInterrogationArea_1);
                }
                {
                    lblInitialarea = new JLabel("InitialArea");
                    GridBagConstraints gbc_lblInitialarea = new GridBagConstraints();
                    gbc_lblInitialarea.insets = new Insets(0, 0, 5, 0);
                    gbc_lblInitialarea.gridx = 1;
                    gbc_lblInitialarea.gridy = 1;
                    panelClippingPIVInfo.add(lblInitialarea, gbc_lblInitialarea);
                }
                {
                    JLabel lblNumberOfAdaptive_1 = new JLabel("Number of Adaptive Steps");
                    GridBagConstraints gbc_lblNumberOfAdaptive_1 = new GridBagConstraints();
                    gbc_lblNumberOfAdaptive_1.insets = new Insets(0, 0, 0, 5);
                    gbc_lblNumberOfAdaptive_1.gridx = 0;
                    gbc_lblNumberOfAdaptive_1.gridy = 2;
                    panelClippingPIVInfo.add(lblNumberOfAdaptive_1, gbc_lblNumberOfAdaptive_1);
                }
                {
                    JLabel lblAdaptiveSteps = new JLabel("Adaptive steps");
                    GridBagConstraints gbc_lblAdaptiveSteps = new GridBagConstraints();
                    gbc_lblAdaptiveSteps.gridx = 1;
                    gbc_lblAdaptiveSteps.gridy = 2;
                    panelClippingPIVInfo.add(lblAdaptiveSteps, gbc_lblAdaptiveSteps);
                }
            }
            JPanel panelClippingConfig = new JPanel();
            panelClippingConfig.setBorder(new TitledBorder(null, "Clipping and Margins configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelClippingConfig = new GridBagConstraints();
            gbc_panelClippingConfig.fill = GridBagConstraints.BOTH;
            gbc_panelClippingConfig.gridx = 0;
            gbc_panelClippingConfig.gridy = 2;
            panelClipping.add(panelClippingConfig, gbc_panelClippingConfig);
            GridBagLayout gbl_panelClippingConfig = new GridBagLayout();
            gbl_panelClippingConfig.columnWidths = new int[]{0, 185, 0, 0, 95, 259, 0, 0};
            gbl_panelClippingConfig.rowHeights = new int[]{0, 0, 0, 0};
            gbl_panelClippingConfig.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
            gbl_panelClippingConfig.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
            panelClippingConfig.setLayout(gbl_panelClippingConfig);
            {
                JLabel lblTopMargin = new JLabel("Top Margin");
                GridBagConstraints gbc_lblTopMargin = new GridBagConstraints();
                gbc_lblTopMargin.fill = GridBagConstraints.VERTICAL;
                gbc_lblTopMargin.anchor = GridBagConstraints.EAST;
                gbc_lblTopMargin.insets = new Insets(0, 0, 5, 5);
                gbc_lblTopMargin.gridx = 0;
                gbc_lblTopMargin.gridy = 0;
                panelClippingConfig.add(lblTopMargin, gbc_lblTopMargin);
            }
            {
                
                frmtdtxtfldTopMargin = new JFormattedTextField(createPixelFormatter());
                frmtdtxtfldTopMargin.setText("0");
                frmtdtxtfldTopMargin.setToolTipText("Defines the top margin of the PIV image below which the interrogation areas are to be created.");
                GridBagConstraints gbc_frmtdtxtfldTopMargin = new GridBagConstraints();
                gbc_frmtdtxtfldTopMargin.insets = new Insets(0, 0, 5, 5);
                gbc_frmtdtxtfldTopMargin.fill = GridBagConstraints.BOTH;
                gbc_frmtdtxtfldTopMargin.gridx = 1;
                gbc_frmtdtxtfldTopMargin.gridy = 0;
                panelClippingConfig.add(frmtdtxtfldTopMargin, gbc_frmtdtxtfldTopMargin);                        
            }
            {
                JLabel lblPixels = new JLabel("pixels");
                GridBagConstraints gbc_lblPixels = new GridBagConstraints();
                gbc_lblPixels.fill = GridBagConstraints.VERTICAL;
                gbc_lblPixels.insets = new Insets(0, 0, 5, 5);
                gbc_lblPixels.gridx = 2;
                gbc_lblPixels.gridy = 0;
                panelClippingConfig.add(lblPixels, gbc_lblPixels);
            }
            {
                Component horizontalStrut = Box.createHorizontalStrut(40);
                GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
                gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
                gbc_horizontalStrut.gridx = 3;
                gbc_horizontalStrut.gridy = 0;
                panelClippingConfig.add(horizontalStrut, gbc_horizontalStrut);
            }
            {
                JLabel lblBottomMargin = new JLabel("Bottom Margin");
                GridBagConstraints gbc_lblBottomMargin = new GridBagConstraints();
                gbc_lblBottomMargin.fill = GridBagConstraints.VERTICAL;
                gbc_lblBottomMargin.anchor = GridBagConstraints.EAST;
                gbc_lblBottomMargin.insets = new Insets(0, 0, 5, 5);
                gbc_lblBottomMargin.gridx = 4;
                gbc_lblBottomMargin.gridy = 0;
                panelClippingConfig.add(lblBottomMargin, gbc_lblBottomMargin);
            }
            {
                frmtdtxtfldBottomMargin = new JFormattedTextField(createPixelFormatter());
                frmtdtxtfldBottomMargin.setText("0");
                frmtdtxtfldBottomMargin.setToolTipText("Defines the bottom margin of the PIV image above which the interrogation areas are to be created.");
                GridBagConstraints gbc_frmtdtxtfldBottomMargin = new GridBagConstraints();
                gbc_frmtdtxtfldBottomMargin.insets = new Insets(0, 0, 5, 5);
                gbc_frmtdtxtfldBottomMargin.fill = GridBagConstraints.BOTH;
                gbc_frmtdtxtfldBottomMargin.gridx = 5;
                gbc_frmtdtxtfldBottomMargin.gridy = 0;
                panelClippingConfig.add(frmtdtxtfldBottomMargin, gbc_frmtdtxtfldBottomMargin);
            }
            {
                JLabel lblPixels_1 = new JLabel("pixels");
                GridBagConstraints gbc_lblPixels_1 = new GridBagConstraints();
                gbc_lblPixels_1.fill = GridBagConstraints.VERTICAL;
                gbc_lblPixels_1.insets = new Insets(0, 0, 5, 0);
                gbc_lblPixels_1.gridx = 6;
                gbc_lblPixels_1.gridy = 0;
                panelClippingConfig.add(lblPixels_1, gbc_lblPixels_1);
            }
            {
                JLabel lblLeftMargin = new JLabel("Left Margin");
                GridBagConstraints gbc_lblLeftMargin = new GridBagConstraints();
                gbc_lblLeftMargin.fill = GridBagConstraints.VERTICAL;
                gbc_lblLeftMargin.anchor = GridBagConstraints.EAST;
                gbc_lblLeftMargin.insets = new Insets(0, 0, 5, 5);
                gbc_lblLeftMargin.gridx = 0;
                gbc_lblLeftMargin.gridy = 1;
                panelClippingConfig.add(lblLeftMargin, gbc_lblLeftMargin);
            }
            {
                frmtdtxtfldLeftMargin = new JFormattedTextField(createPixelFormatter());
                frmtdtxtfldLeftMargin.setText("0");
                frmtdtxtfldLeftMargin.setToolTipText("Defines the left margin of the PIV image to the right of which the interrogation areas are to be created.");
                GridBagConstraints gbc_frmtdtxtfldLeftMargin = new GridBagConstraints();
                gbc_frmtdtxtfldLeftMargin.insets = new Insets(0, 0, 5, 5);
                gbc_frmtdtxtfldLeftMargin.fill = GridBagConstraints.BOTH;
                gbc_frmtdtxtfldLeftMargin.gridx = 1;
                gbc_frmtdtxtfldLeftMargin.gridy = 1;
                panelClippingConfig.add(frmtdtxtfldLeftMargin, gbc_frmtdtxtfldLeftMargin);
            }
            {
                JLabel lblPixels_3 = new JLabel("pixels");
                GridBagConstraints gbc_lblPixels_3 = new GridBagConstraints();
                gbc_lblPixels_3.fill = GridBagConstraints.VERTICAL;
                gbc_lblPixels_3.insets = new Insets(0, 0, 5, 5);
                gbc_lblPixels_3.gridx = 2;
                gbc_lblPixels_3.gridy = 1;
                panelClippingConfig.add(lblPixels_3, gbc_lblPixels_3);
            }
            {
                Component horizontalStrut = Box.createHorizontalStrut(40);
                GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
                gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
                gbc_horizontalStrut.gridx = 3;
                gbc_horizontalStrut.gridy = 1;
                panelClippingConfig.add(horizontalStrut, gbc_horizontalStrut);
            }
            {
                JLabel lblRightMargin = new JLabel("Right Margin");
                GridBagConstraints gbc_lblRightMargin = new GridBagConstraints();
                gbc_lblRightMargin.fill = GridBagConstraints.VERTICAL;
                gbc_lblRightMargin.anchor = GridBagConstraints.EAST;
                gbc_lblRightMargin.insets = new Insets(0, 0, 5, 5);
                gbc_lblRightMargin.gridx = 4;
                gbc_lblRightMargin.gridy = 1;
                panelClippingConfig.add(lblRightMargin, gbc_lblRightMargin);
            }
            {
                frmtdtxtfldRightMargin = new JFormattedTextField(createPixelFormatter());
                frmtdtxtfldRightMargin.setText("0");
                frmtdtxtfldRightMargin.setToolTipText("Defines the right margin of the PIV image to the left of which the interrogation areas are to be created.");
                GridBagConstraints gbc_frmtdtxtfldRightMargin = new GridBagConstraints();
                gbc_frmtdtxtfldRightMargin.insets = new Insets(0, 0, 5, 5);
                gbc_frmtdtxtfldRightMargin.fill = GridBagConstraints.BOTH;
                gbc_frmtdtxtfldRightMargin.gridx = 5;
                gbc_frmtdtxtfldRightMargin.gridy = 1;
                panelClippingConfig.add(frmtdtxtfldRightMargin, gbc_frmtdtxtfldRightMargin);
            }
            {
                JLabel lblPixels_2 = new JLabel("pixels");
                GridBagConstraints gbc_lblPixels_2 = new GridBagConstraints();
                gbc_lblPixels_2.fill = GridBagConstraints.VERTICAL;
                gbc_lblPixels_2.insets = new Insets(0, 0, 5, 0);
                gbc_lblPixels_2.gridx = 6;
                gbc_lblPixels_2.gridy = 1;
                panelClippingConfig.add(lblPixels_2, gbc_lblPixels_2);
            }
            {
                JLabel lblClippingMode = new JLabel("Clipping mode");
                GridBagConstraints gbc_lblClippingMode = new GridBagConstraints();
                gbc_lblClippingMode.fill = GridBagConstraints.VERTICAL;
                gbc_lblClippingMode.anchor = GridBagConstraints.EAST;
                gbc_lblClippingMode.insets = new Insets(0, 0, 0, 5);
                gbc_lblClippingMode.gridx = 0;
                gbc_lblClippingMode.gridy = 2;
                panelClippingConfig.add(lblClippingMode, gbc_lblClippingMode);
            }
            comboBoxClippingMode = new JComboBox<ClippingModeEnum>();
            comboBoxClippingMode.setModel(clippingModeComboModel);
            GridBagConstraints gbc_comboBoxClippingMode = new GridBagConstraints();
            gbc_comboBoxClippingMode.gridwidth = 5;
            gbc_comboBoxClippingMode.insets = new Insets(0, 0, 0, 5);
            gbc_comboBoxClippingMode.fill = GridBagConstraints.BOTH;
            gbc_comboBoxClippingMode.gridx = 1;
            gbc_comboBoxClippingMode.gridy = 2;
            panelClippingConfig.add(comboBoxClippingMode, gbc_comboBoxClippingMode);
            {
                JPanel panelWarping = new JPanel();
                tabbedPane.addTab("Warping", null, panelWarping, "Image warping configuration");
                GridBagLayout gbl_panelWarping = new GridBagLayout();
                gbl_panelWarping.columnWidths = new int[]{10, 0};
                gbl_panelWarping.rowHeights = new int[]{10, 0, 0};
                gbl_panelWarping.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelWarping.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                panelWarping.setLayout(gbl_panelWarping);
                {
                    panelWarpingImageInfo = new ImageInfoPanel();  
                    GridBagConstraints gbc_panelWarpingImageInfo = new GridBagConstraints();
                    gbc_panelWarpingImageInfo.insets = new Insets(0, 0, 5, 0);
                    gbc_panelWarpingImageInfo.fill = GridBagConstraints.HORIZONTAL;
                    gbc_panelWarpingImageInfo.anchor = GridBagConstraints.NORTH;
                    gbc_panelWarpingImageInfo.gridx = 0;
                    gbc_panelWarpingImageInfo.gridy = 0;
                    panelWarping.add(panelWarpingImageInfo, gbc_panelWarpingImageInfo);
                }
                {
                    JPanel panelWarpingConfig = new JPanel();
                    panelWarpingConfig.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Warping configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
                    GridBagConstraints gbc_panelWarpingConfig = new GridBagConstraints();
                    gbc_panelWarpingConfig.fill = GridBagConstraints.BOTH;
                    gbc_panelWarpingConfig.gridx = 0;
                    gbc_panelWarpingConfig.gridy = 1;
                    panelWarping.add(panelWarpingConfig, gbc_panelWarpingConfig);
                    GridBagLayout gbl_panelWarpingConfig = new GridBagLayout();
                    gbl_panelWarpingConfig.columnWidths = new int[]{0, 0, 0};
                    gbl_panelWarpingConfig.rowHeights = new int[]{0, 0, 0, 0};
                    gbl_panelWarpingConfig.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                    gbl_panelWarpingConfig.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
                    panelWarpingConfig.setLayout(gbl_panelWarpingConfig);
                    {
                        JLabel lblWarpingStrategy = new JLabel("Warping strategy");
                        GridBagConstraints gbc_lblWarpingStrategy = new GridBagConstraints();
                        gbc_lblWarpingStrategy.fill = GridBagConstraints.VERTICAL;
                        gbc_lblWarpingStrategy.insets = new Insets(0, 0, 5, 5);
                        gbc_lblWarpingStrategy.anchor = GridBagConstraints.EAST;
                        gbc_lblWarpingStrategy.gridx = 0;
                        gbc_lblWarpingStrategy.gridy = 1;
                        panelWarpingConfig.add(lblWarpingStrategy, gbc_lblWarpingStrategy);
                    }
                    {
                        comboBoxWarpingMode = new JComboBox<WarpingModeEnum>();
                        comboBoxWarpingMode.setModel(warpingModeComboModel);
                        GridBagConstraints gbc_comboBoxWarping = new GridBagConstraints();
                        gbc_comboBoxWarping.fill = GridBagConstraints.HORIZONTAL;
                        gbc_comboBoxWarping.insets = new Insets(0, 0, 5, 0);
                        gbc_comboBoxWarping.gridx = 1;
                        gbc_comboBoxWarping.gridy = 1;
                        panelWarpingConfig.add(comboBoxWarpingMode, gbc_comboBoxWarping);
                    }
                }
            }
            {
                JPanel panelInheritance = new JPanel();
                tabbedPane.addTab("Inheritance", null, panelInheritance, "Velocity Inheritance configuration across adaptive steps");
                GridBagLayout gbl_panelInheritance = new GridBagLayout();
                gbl_panelInheritance.columnWidths = new int[]{10, 0};
                gbl_panelInheritance.rowHeights = new int[]{10, 0, 0};
                gbl_panelInheritance.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelInheritance.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                panelInheritance.setLayout(gbl_panelInheritance);
                {
                    panelInheritanceImageInfo = new ImageInfoPanel();
                    GridBagConstraints gbc_panelInheritanceImageInfo = new GridBagConstraints();
                    gbc_panelInheritanceImageInfo.insets = new Insets(0, 0, 5, 0);
                    gbc_panelInheritanceImageInfo.fill = GridBagConstraints.HORIZONTAL;
                    gbc_panelInheritanceImageInfo.anchor = GridBagConstraints.NORTH;
                    gbc_panelInheritanceImageInfo.gridx = 0;
                    gbc_panelInheritanceImageInfo.gridy = 0;
                    panelInheritance.add(panelInheritanceImageInfo, gbc_panelInheritanceImageInfo);
                }
                {
                    JPanel panelInheritanceInfo = new JPanel();
                    panelInheritanceInfo.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Inheritance configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                    GridBagConstraints gbc_panelInheritanceInfo = new GridBagConstraints();
                    gbc_panelInheritanceInfo.fill = GridBagConstraints.BOTH;
                    gbc_panelInheritanceInfo.gridx = 0;
                    gbc_panelInheritanceInfo.gridy = 1;
                    panelInheritance.add(panelInheritanceInfo, gbc_panelInheritanceInfo);
                    GridBagLayout gbl_panelInheritanceInfo = new GridBagLayout();
                    gbl_panelInheritanceInfo.columnWidths = new int[]{0, 0, 0};
                    gbl_panelInheritanceInfo.rowHeights = new int[]{0, 0, 0, 0};
                    gbl_panelInheritanceInfo.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                    gbl_panelInheritanceInfo.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
                    panelInheritanceInfo.setLayout(gbl_panelInheritanceInfo);
                    {
                        JLabel lblInheritanceStrategy = new JLabel("Inheritance strategy");
                        GridBagConstraints gbc_lblInheritanceStrategy = new GridBagConstraints();
                        gbc_lblInheritanceStrategy.insets = new Insets(0, 0, 5, 5);
                        gbc_lblInheritanceStrategy.anchor = GridBagConstraints.EAST;
                        gbc_lblInheritanceStrategy.gridx = 0;
                        gbc_lblInheritanceStrategy.gridy = 1;
                        panelInheritanceInfo.add(lblInheritanceStrategy, gbc_lblInheritanceStrategy);
                    }
                    {
                        comboBoxInheritance = new JComboBox<>();
                        comboBoxInheritance.setModel(new InheritanceModeComboBoxModel());
                        GridBagConstraints gbc_comboBoxInheritance = new GridBagConstraints();
                        gbc_comboBoxInheritance.insets = new Insets(0, 0, 5, 0);
                        gbc_comboBoxInheritance.fill = GridBagConstraints.HORIZONTAL;
                        gbc_comboBoxInheritance.gridx = 1;
                        gbc_comboBoxInheritance.gridy = 1;
                        panelInheritanceInfo.add(comboBoxInheritance, gbc_comboBoxInheritance);
                    }
                }
            }
            
            JPanel panelSuperposition = new JPanel();
            tabbedPane.addTab("Superposition", null, panelSuperposition, "Superposition configuration");
            GridBagLayout gbl_panelSuperposition = new GridBagLayout();
            gbl_panelSuperposition.columnWidths = new int[]{0, 0};
            gbl_panelSuperposition.rowHeights = new int[]{0, 0, 0};
            gbl_panelSuperposition.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelSuperposition.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            panelSuperposition.setLayout(gbl_panelSuperposition);
            {
                panelSuperpositionImageInfo = new ImageInfoPanel();
                GridBagConstraints gbc_panelSuperpositionImageInfo = new GridBagConstraints();
                gbc_panelSuperpositionImageInfo.insets = new Insets(0, 0, 5, 0);
                gbc_panelSuperpositionImageInfo.fill = GridBagConstraints.BOTH;
                gbc_panelSuperpositionImageInfo.gridx = 0;
                gbc_panelSuperpositionImageInfo.gridy = 0;
                panelSuperposition.add(panelSuperpositionImageInfo, gbc_panelSuperpositionImageInfo);
            }
            JPanel panelSuperpositionInfo = new JPanel();
            panelSuperpositionInfo.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Superposition configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelSuperpositionInfo = new GridBagConstraints();
            gbc_panelSuperpositionInfo.fill = GridBagConstraints.BOTH;
            gbc_panelSuperpositionInfo.gridx = 0;
            gbc_panelSuperpositionInfo.gridy = 1;
            panelSuperposition.add(panelSuperpositionInfo, gbc_panelSuperpositionInfo);
            GridBagLayout gbl_panelSuperpositionInfo = new GridBagLayout();
            gbl_panelSuperpositionInfo.columnWidths = new int[]{0, 0};
            gbl_panelSuperpositionInfo.rowHeights = new int[]{0, 0, 0, 0, 0};
            gbl_panelSuperpositionInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelSuperpositionInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
            panelSuperpositionInfo.setLayout(gbl_panelSuperpositionInfo);
            {
                chckbxEnableSuperposition = new JCheckBox("Enable superposition");
                chckbxEnableSuperposition.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox)e.getSource();
                        updateSuperpositionEnableState(box.isSelected());
                    }                        
                });
                GridBagConstraints gbc_chckbxEnableSuperposition = new GridBagConstraints();
                gbc_chckbxEnableSuperposition.anchor = GridBagConstraints.WEST;
                gbc_chckbxEnableSuperposition.insets = new Insets(0, 0, 5, 0);
                gbc_chckbxEnableSuperposition.gridx = 0;
                gbc_chckbxEnableSuperposition.gridy = 1;
                panelSuperpositionInfo.add(chckbxEnableSuperposition, gbc_chckbxEnableSuperposition);
            }
            panelSuperpositionParameters = new JPanel();
            panelSuperpositionParameters.setBorder(new MatteBorder(1, 1, 1, 1, (Color) Color.LIGHT_GRAY));
            GridBagConstraints gbc_panelSuperpositionParameters = new GridBagConstraints();
            gbc_panelSuperpositionParameters.insets = new Insets(0, 0, 5, 0);
            gbc_panelSuperpositionParameters.fill = GridBagConstraints.BOTH;
            gbc_panelSuperpositionParameters.gridx = 0;
            gbc_panelSuperpositionParameters.gridy = 2;
            panelSuperpositionInfo.add(panelSuperpositionParameters, gbc_panelSuperpositionParameters);
            GridBagLayout gbl_panelSuperpositionParameters = new GridBagLayout();
            gbl_panelSuperpositionParameters.columnWidths = new int[]{0, 0, 0};
            gbl_panelSuperpositionParameters.rowHeights = new int[]{0, 0, 0};
            gbl_panelSuperpositionParameters.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            gbl_panelSuperpositionParameters.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
            panelSuperpositionParameters.setLayout(gbl_panelSuperpositionParameters);
            {
                JLabel lblSuperpositionRatio = new JLabel("Superposition percentage");
                GridBagConstraints gbc_lblSuperpositionRatio = new GridBagConstraints();
                gbc_lblSuperpositionRatio.insets = new Insets(0, 0, 5, 5);
                gbc_lblSuperpositionRatio.anchor = GridBagConstraints.EAST;
                gbc_lblSuperpositionRatio.gridx = 0;
                gbc_lblSuperpositionRatio.gridy = 0;
                panelSuperpositionParameters.add(lblSuperpositionRatio, gbc_lblSuperpositionRatio);
            }
            {
                frmtdtxtfldSuperpositionPercentage = new JFormattedTextField(createPercentageFormatter());
                GridBagConstraints gbc_formattedTextFieldSuperpositionRatio = new GridBagConstraints();
                gbc_formattedTextFieldSuperpositionRatio.insets = new Insets(0, 0, 5, 0);
                gbc_formattedTextFieldSuperpositionRatio.fill = GridBagConstraints.HORIZONTAL;
                gbc_formattedTextFieldSuperpositionRatio.gridx = 1;
                gbc_formattedTextFieldSuperpositionRatio.gridy = 0;
                panelSuperpositionParameters.add(frmtdtxtfldSuperpositionPercentage, gbc_formattedTextFieldSuperpositionRatio);
            }
            {
                JLabel lblStartingStep = new JLabel("Starting step");
                GridBagConstraints gbc_lblStartingStep = new GridBagConstraints();
                gbc_lblStartingStep.anchor = GridBagConstraints.EAST;
                gbc_lblStartingStep.insets = new Insets(0, 0, 0, 5);
                gbc_lblStartingStep.gridx = 0;
                gbc_lblStartingStep.gridy = 1;
                panelSuperpositionParameters.add(lblStartingStep, gbc_lblStartingStep);
            }
            comboBoxSuperpositionStartStep = new JComboBox<>();
            comboBoxSuperpositionStartStep.setModel(superpositionStartStepComboModel);
            GridBagConstraints gbc_comboBoxSuperpositionStartStep = new GridBagConstraints();
            gbc_comboBoxSuperpositionStartStep.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBoxSuperpositionStartStep.gridx = 1;
            gbc_comboBoxSuperpositionStartStep.gridy = 1;
            panelSuperpositionParameters.add(comboBoxSuperpositionStartStep, gbc_comboBoxSuperpositionStartStep);
            
            JPanel panelInterpolation = new JPanel();
            tabbedPane.addTab("Interpolation", null, panelInterpolation, "Sub-pixel interpolation configuration");
            GridBagLayout gbl_panelInterpolation = new GridBagLayout();
            gbl_panelInterpolation.columnWidths = new int[]{0, 0};
            gbl_panelInterpolation.rowHeights = new int[]{0, 0, 0};
            gbl_panelInterpolation.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelInterpolation.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            panelInterpolation.setLayout(gbl_panelInterpolation);
            {
                panelInterpolationImageInfo = new ImageInfoPanel();
                GridBagConstraints gbc_panelInterpolationImageInfo = new GridBagConstraints();
                gbc_panelInterpolationImageInfo.insets = new Insets(0, 0, 5, 0);
                gbc_panelInterpolationImageInfo.fill = GridBagConstraints.BOTH;
                gbc_panelInterpolationImageInfo.gridx = 0;
                gbc_panelInterpolationImageInfo.gridy = 0;
                panelInterpolation.add(panelInterpolationImageInfo, gbc_panelInterpolationImageInfo);
            }
            JPanel panelInterpolationInfo = new JPanel();
            panelInterpolationInfo.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Sub-pixel interpolation configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelInterpolationInfo = new GridBagConstraints();
            gbc_panelInterpolationInfo.fill = GridBagConstraints.BOTH;
            gbc_panelInterpolationInfo.gridx = 0;
            gbc_panelInterpolationInfo.gridy = 1;
            panelInterpolation.add(panelInterpolationInfo, gbc_panelInterpolationInfo);
            GridBagLayout gbl_panelInterpolationInfo = new GridBagLayout();
            gbl_panelInterpolationInfo.columnWidths = new int[]{0, 0};
            gbl_panelInterpolationInfo.rowHeights = new int[]{0, 0, 0, 0, 0};
            gbl_panelInterpolationInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
            gbl_panelInterpolationInfo.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
            panelInterpolationInfo.setLayout(gbl_panelInterpolationInfo);
            {
                chckbxEnableSubpixelInterpolation = new JCheckBox("Enable sub-pixel interpolation");
                chckbxEnableSubpixelInterpolation.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox)e.getSource();
                        updateInterpolationEnableState(box.isSelected());
                    }
                });
                GridBagConstraints gbc_chckbxEnableSubpixelInterpolation = new GridBagConstraints();
                gbc_chckbxEnableSubpixelInterpolation.anchor = GridBagConstraints.WEST;
                gbc_chckbxEnableSubpixelInterpolation.insets = new Insets(0, 0, 5, 0);
                gbc_chckbxEnableSubpixelInterpolation.gridx = 0;
                gbc_chckbxEnableSubpixelInterpolation.gridy = 1;
                panelInterpolationInfo.add(chckbxEnableSubpixelInterpolation, gbc_chckbxEnableSubpixelInterpolation);
            }
            panelInterpolationConfig = new JPanel();
            panelInterpolationConfig.setBorder(new MatteBorder(1, 1, 1, 1, (Color) Color.LIGHT_GRAY));
            GridBagConstraints gbc_panelInterpolationConfig = new GridBagConstraints();
            gbc_panelInterpolationConfig.insets = new Insets(0, 0, 5, 0);
            gbc_panelInterpolationConfig.fill = GridBagConstraints.BOTH;
            gbc_panelInterpolationConfig.gridx = 0;
            gbc_panelInterpolationConfig.gridy = 2;
            panelInterpolationInfo.add(panelInterpolationConfig, gbc_panelInterpolationConfig);
            GridBagLayout gbl_panelInterpolationConfig = new GridBagLayout();
            gbl_panelInterpolationConfig.columnWidths = new int[]{146, 0, 0};
            gbl_panelInterpolationConfig.rowHeights = new int[]{0, 0, 0, 0};
            gbl_panelInterpolationConfig.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            gbl_panelInterpolationConfig.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
            panelInterpolationConfig.setLayout(gbl_panelInterpolationConfig);
            {
                JLabel lblAlgorithm = new JLabel("Interpolation strategy");
                GridBagConstraints gbc_lblAlgorithm = new GridBagConstraints();
                gbc_lblAlgorithm.anchor = GridBagConstraints.WEST;
                gbc_lblAlgorithm.insets = new Insets(0, 0, 5, 5);
                gbc_lblAlgorithm.gridx = 0;
                gbc_lblAlgorithm.gridy = 0;
                panelInterpolationConfig.add(lblAlgorithm, gbc_lblAlgorithm);
            }
            {
                comboBoxInterpolationAlgorithm = new JComboBox<>();                        
                comboBoxInterpolationAlgorithm.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateSubPixelInterpolationOptionsPanel((SubPixelInterpolationModeEnum)comboBoxInterpolationAlgorithm.getSelectedItem());
                    }
                });
                comboBoxInterpolationAlgorithm.setModel(new SubPixelInterpolationComboBoxModel());
                GridBagConstraints gbc_comboBoxInterpolationAlgorithm = new GridBagConstraints();
                gbc_comboBoxInterpolationAlgorithm.insets = new Insets(0, 0, 5, 0);
                gbc_comboBoxInterpolationAlgorithm.fill = GridBagConstraints.HORIZONTAL;
                gbc_comboBoxInterpolationAlgorithm.gridx = 1;
                gbc_comboBoxInterpolationAlgorithm.gridy = 0;
                panelInterpolationConfig.add(comboBoxInterpolationAlgorithm, gbc_comboBoxInterpolationAlgorithm);
            }
            {
                JLabel lblStartStep = new JLabel("Start step");
                GridBagConstraints gbc_lblStartStep = new GridBagConstraints();
                gbc_lblStartStep.anchor = GridBagConstraints.WEST;
                gbc_lblStartStep.insets = new Insets(0, 0, 5, 5);
                gbc_lblStartStep.gridx = 0;
                gbc_lblStartStep.gridy = 1;
                panelInterpolationConfig.add(lblStartStep, gbc_lblStartStep);
            }
            comboBoxInterpolationStartStep = new JComboBox<>();
            comboBoxInterpolationStartStep.setModel(interpolationStartStepComboModel);
            GridBagConstraints gbc_comboBoxInterpolationStartStep = new GridBagConstraints();
            gbc_comboBoxInterpolationStartStep.insets = new Insets(0, 0, 5, 0);
            gbc_comboBoxInterpolationStartStep.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBoxInterpolationStartStep.gridx = 1;
            gbc_comboBoxInterpolationStartStep.gridy = 1;
            panelInterpolationConfig.add(comboBoxInterpolationStartStep, gbc_comboBoxInterpolationStartStep);
            {
                panelInterpolationAlgorithmOptions = new JPanel();
                panelInterpolationAlgorithmOptions.setBorder(new TitledBorder(new LineBorder(new Color(192, 192, 192), 1, true), "Options", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                GridBagConstraints gbc_panelInterpolationAlgorithmOptions = new GridBagConstraints();
                gbc_panelInterpolationAlgorithmOptions.gridwidth = 2;
                gbc_panelInterpolationAlgorithmOptions.insets = new Insets(0, 0, 0, 5);
                gbc_panelInterpolationAlgorithmOptions.fill = GridBagConstraints.BOTH;
                gbc_panelInterpolationAlgorithmOptions.gridx = 0;
                gbc_panelInterpolationAlgorithmOptions.gridy = 2;
                panelInterpolationConfig.add(panelInterpolationAlgorithmOptions, gbc_panelInterpolationAlgorithmOptions);
                GridBagLayout gbl_panelInterpolationAlgorithmOptions = new GridBagLayout();
                gbl_panelInterpolationAlgorithmOptions.columnWidths = new int[]{0};
                gbl_panelInterpolationAlgorithmOptions.rowHeights = new int[]{0};
                gbl_panelInterpolationAlgorithmOptions.columnWeights = new double[]{Double.MIN_VALUE};
                gbl_panelInterpolationAlgorithmOptions.rowWeights = new double[]{Double.MIN_VALUE};
                panelInterpolationAlgorithmOptions.setLayout(gbl_panelInterpolationAlgorithmOptions);
            }
            {
                JPanel panelStabilization = new JPanel();
                tabbedPane.addTab("Stabilization", null, panelStabilization, "Velocity stabilization strategy selection and configuration");
                GridBagLayout gbl_panelStabilization = new GridBagLayout();
                gbl_panelStabilization.columnWidths = new int[]{0, 0};
                gbl_panelStabilization.rowHeights = new int[]{0, 0, 0};
                gbl_panelStabilization.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelStabilization.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                panelStabilization.setLayout(gbl_panelStabilization);
                {
                    panelStabilizationImageInfo = new ImageInfoPanel();
                    GridBagConstraints gbc_panelStabilizationImageInfo = new GridBagConstraints();
                    gbc_panelStabilizationImageInfo.insets = new Insets(0, 0, 5, 0);
                    gbc_panelStabilizationImageInfo.fill = GridBagConstraints.BOTH;
                    gbc_panelStabilizationImageInfo.gridx = 0;
                    gbc_panelStabilizationImageInfo.gridy = 0;
                    panelStabilization.add(panelStabilizationImageInfo, gbc_panelStabilizationImageInfo);
                }
                {
                    JPanel panelStabilizationInfo = new JPanel();
                    panelStabilizationInfo.setBorder(new TitledBorder(null, "Velocity stabilization info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                    GridBagConstraints gbc_panelStabilizationInfo = new GridBagConstraints();
                    gbc_panelStabilizationInfo.fill = GridBagConstraints.BOTH;
                    gbc_panelStabilizationInfo.gridx = 0;
                    gbc_panelStabilizationInfo.gridy = 1;
                    panelStabilization.add(panelStabilizationInfo, gbc_panelStabilizationInfo);
                    GridBagLayout gbl_panelStabilizationInfo = new GridBagLayout();
                    gbl_panelStabilizationInfo.columnWidths = new int[]{0, 0};
                    gbl_panelStabilizationInfo.rowHeights = new int[]{0, 0, 0, 0, 0};
                    gbl_panelStabilizationInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                    gbl_panelStabilizationInfo.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
                    panelStabilizationInfo.setLayout(gbl_panelStabilizationInfo);
                    {
                        chckbxEnableVelocityStabilization = new JCheckBox("Enable Velocity stabilization");
                        chckbxEnableVelocityStabilization.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                JCheckBox box = (JCheckBox)e.getSource();
                                updateStabilizationEnableState(box.isSelected());
                            }
                        });
                        GridBagConstraints gbc_chckbxEnableVelocityStabilization = new GridBagConstraints();
                        gbc_chckbxEnableVelocityStabilization.anchor = GridBagConstraints.WEST;
                        gbc_chckbxEnableVelocityStabilization.insets = new Insets(0, 0, 5, 0);
                        gbc_chckbxEnableVelocityStabilization.gridx = 0;
                        gbc_chckbxEnableVelocityStabilization.gridy = 1;
                        panelStabilizationInfo.add(chckbxEnableVelocityStabilization, gbc_chckbxEnableVelocityStabilization);
                    }
                    {
                        panelStabilizationConfig = new JPanel();
                        GridBagConstraints gbc_panelStabilizationConfig = new GridBagConstraints();
                        gbc_panelStabilizationConfig.insets = new Insets(0, 0, 5, 0);
                        gbc_panelStabilizationConfig.fill = GridBagConstraints.BOTH;
                        gbc_panelStabilizationConfig.gridx = 0;
                        gbc_panelStabilizationConfig.gridy = 2;
                        panelStabilizationInfo.add(panelStabilizationConfig, gbc_panelStabilizationConfig);
                        GridBagLayout gbl_panelStabilizationConfig = new GridBagLayout();
                        gbl_panelStabilizationConfig.columnWidths = new int[]{220, 0, 0};
                        gbl_panelStabilizationConfig.rowHeights = new int[]{0, 0, 0};
                        gbl_panelStabilizationConfig.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                        gbl_panelStabilizationConfig.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                        panelStabilizationConfig.setLayout(gbl_panelStabilizationConfig);
                        {
                            JLabel lblAlgorithm_1 = new JLabel("Stabilization strategy");
                            GridBagConstraints gbc_lblAlgorithm_1 = new GridBagConstraints();
                            gbc_lblAlgorithm_1.anchor = GridBagConstraints.WEST;
                            gbc_lblAlgorithm_1.insets = new Insets(0, 0, 5, 5);
                            gbc_lblAlgorithm_1.gridx = 0;
                            gbc_lblAlgorithm_1.gridy = 0;
                            panelStabilizationConfig.add(lblAlgorithm_1, gbc_lblAlgorithm_1);
                        }
                        {
                            comboBoxStabilizationStrategy = new JComboBox<>();
                            comboBoxStabilizationStrategy.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    updateVelocityStabilizationOptionsPanel((VelocityStabilizationModeEnum)comboBoxStabilizationStrategy.getSelectedItem());
                                }
                            });
                            comboBoxStabilizationStrategy.setModel(new VelocityStabilizationComboBoxModel());
                            GridBagConstraints gbc_comboBoxStabilizationStrategy = new GridBagConstraints();
                            gbc_comboBoxStabilizationStrategy.insets = new Insets(0, 0, 5, 0);
                            gbc_comboBoxStabilizationStrategy.fill = GridBagConstraints.HORIZONTAL;
                            gbc_comboBoxStabilizationStrategy.gridx = 1;
                            gbc_comboBoxStabilizationStrategy.gridy = 0;
                            panelStabilizationConfig.add(comboBoxStabilizationStrategy, gbc_comboBoxStabilizationStrategy);
                        }
                        {
                            panelStabilizationAlgorithmOptions = new JPanel();
                            panelStabilizationAlgorithmOptions.setBorder(new TitledBorder(new LineBorder(new Color(192, 192, 192), 1, true), "Options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
                            GridBagConstraints gbc_panelStabilizationAlgorithmOptions = new GridBagConstraints();
                            gbc_panelStabilizationAlgorithmOptions.gridwidth = 2;
                            gbc_panelStabilizationAlgorithmOptions.insets = new Insets(0, 0, 0, 5);
                            gbc_panelStabilizationAlgorithmOptions.fill = GridBagConstraints.BOTH;
                            gbc_panelStabilizationAlgorithmOptions.gridx = 0;
                            gbc_panelStabilizationAlgorithmOptions.gridy = 1;
                            panelStabilizationConfig.add(panelStabilizationAlgorithmOptions, gbc_panelStabilizationAlgorithmOptions);
                            GridBagLayout gbl_panelStabilizationAlgorithmOptions = new GridBagLayout();
                            gbl_panelStabilizationAlgorithmOptions.columnWidths = new int[]{0};
                            gbl_panelStabilizationAlgorithmOptions.rowHeights = new int[]{0};
                            gbl_panelStabilizationAlgorithmOptions.columnWeights = new double[]{Double.MIN_VALUE};
                            gbl_panelStabilizationAlgorithmOptions.rowWeights = new double[]{Double.MIN_VALUE};
                            panelStabilizationAlgorithmOptions.setLayout(gbl_panelStabilizationAlgorithmOptions);
                        }
                    }
                }
            }
            {
                JPanel panelValidation = new JPanel();
                tabbedPane.addTab("Validation", null, panelValidation, "Velocity validation configuration");
                tabbedPane.setEnabledAt(7, true);
                GridBagLayout gbl_panelValidation = new GridBagLayout();
                gbl_panelValidation.columnWidths = new int[]{0, 0};
                gbl_panelValidation.rowHeights = new int[]{0, 0, 0};
                gbl_panelValidation.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelValidation.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                panelValidation.setLayout(gbl_panelValidation);
                {
                    panelValidationImageInfo = new ImageInfoPanel();
                    GridBagConstraints gbc_panelValidationImageInfo = new GridBagConstraints();
                    gbc_panelValidationImageInfo.insets = new Insets(0, 0, 5, 0);
                    gbc_panelValidationImageInfo.fill = GridBagConstraints.BOTH;
                    gbc_panelValidationImageInfo.gridx = 0;
                    gbc_panelValidationImageInfo.gridy = 0;
                    panelValidation.add(panelValidationImageInfo, gbc_panelValidationImageInfo);
                }
                {
                    JPanel panelVectorValidation = new JPanel();
                    panelVectorValidation.setBorder(new TitledBorder(null, "Vector validation configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                    GridBagConstraints gbc_panelVectorValidation = new GridBagConstraints();
                    gbc_panelVectorValidation.fill = GridBagConstraints.BOTH;
                    gbc_panelVectorValidation.gridx = 0;
                    gbc_panelVectorValidation.gridy = 1;
                    panelValidation.add(panelVectorValidation, gbc_panelVectorValidation);
                    GridBagLayout gbl_panelVectorValidation = new GridBagLayout();
                    gbl_panelVectorValidation.columnWidths = new int[]{207, 0, 0};
                    gbl_panelVectorValidation.rowHeights = new int[]{0, 0, 0};
                    gbl_panelVectorValidation.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                    gbl_panelVectorValidation.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                    panelVectorValidation.setLayout(gbl_panelVectorValidation);
                    {
                        chckbxEnableVectorValidation = new JCheckBox("Enable vector validation");
                        chckbxEnableVectorValidation.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                JCheckBox box = (JCheckBox)e.getSource();
                                updateValidationEnableState(box.isSelected());
                            }
                        });
                        GridBagConstraints gbc_chckbxEnableVectorValidation = new GridBagConstraints();
                        gbc_chckbxEnableVectorValidation.anchor = GridBagConstraints.WEST;
                        gbc_chckbxEnableVectorValidation.insets = new Insets(0, 0, 5, 5);
                        gbc_chckbxEnableVectorValidation.gridx = 0;
                        gbc_chckbxEnableVectorValidation.gridy = 0;
                        panelVectorValidation.add(chckbxEnableVectorValidation, gbc_chckbxEnableVectorValidation);
                    }
                    {
                        panelValidationConfig = new JPanel();
                        GridBagConstraints gbc_panelValidationConfig = new GridBagConstraints();
                        gbc_panelValidationConfig.gridwidth = 2;
                        gbc_panelValidationConfig.fill = GridBagConstraints.BOTH;
                        gbc_panelValidationConfig.gridx = 0;
                        gbc_panelValidationConfig.gridy = 1;
                        panelVectorValidation.add(panelValidationConfig, gbc_panelValidationConfig);
                        GridBagLayout gbl_panelValidationConfig = new GridBagLayout();
                        gbl_panelValidationConfig.columnWidths = new int[] {207, 0, 0};
                        gbl_panelValidationConfig.columnWeights = new double[]{0.0, 1.0, 1.0};
                        gbl_panelValidationConfig.rowWeights = new double[]{0.0, Double.MIN_VALUE, 0.0, 1.0};
                        panelValidationConfig.setLayout(gbl_panelValidationConfig);
                    }
                }

            }
            {
                chckbxReplaceAllInvalid = new JCheckBox("Replace all invalid vectors by NaN");
                GridBagConstraints gbc_chckbxReplaceAllInvalid = new GridBagConstraints();
                gbc_chckbxReplaceAllInvalid.insets = new Insets(0, 0, 5, 5);
                gbc_chckbxReplaceAllInvalid.gridx = 0;
                gbc_chckbxReplaceAllInvalid.gridy = 0;
                panelValidationConfig.add(chckbxReplaceAllInvalid, gbc_chckbxReplaceAllInvalid);
            }
            {
                JLabel lblMaxNumberOf = new JLabel("Max. number of iterations");
                GridBagConstraints gbc_lblMaxNumberOf = new GridBagConstraints();
                gbc_lblMaxNumberOf.anchor = GridBagConstraints.WEST;
                gbc_lblMaxNumberOf.insets = new Insets(0, 0, 5, 5);
                gbc_lblMaxNumberOf.gridx = 0;
                gbc_lblMaxNumberOf.gridy = 1;
                panelValidationConfig.add(lblMaxNumberOf, gbc_lblMaxNumberOf);
            }
            {
                frmtdtxtfldMaxiterations = new JFormattedTextField();
                frmtdtxtfldMaxiterations.setText("maxIterations");
                GridBagConstraints gbc_frmtdtxtfldMaxiterations = new GridBagConstraints();
                gbc_frmtdtxtfldMaxiterations.insets = new Insets(0, 0, 5, 5);
                gbc_frmtdtxtfldMaxiterations.fill = GridBagConstraints.HORIZONTAL;
                gbc_frmtdtxtfldMaxiterations.gridx = 1;
                gbc_frmtdtxtfldMaxiterations.gridy = 1;
                panelValidationConfig.add(frmtdtxtfldMaxiterations, gbc_frmtdtxtfldMaxiterations);
            }
            {
                chckbxIterateUntilNoMoreReplaced = new JCheckBox("Iterate until no more vectors replacement is possible");
                chckbxIterateUntilNoMoreReplaced.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        JCheckBox box = (JCheckBox)event.getSource();
                        updateValidatorMaxIterationsEnabledState(box.isSelected());
                    }
                });
                GridBagConstraints gbc_chckbxIterateUntilNoMoreReplaced = new GridBagConstraints();
                gbc_chckbxIterateUntilNoMoreReplaced.anchor = GridBagConstraints.WEST;
                gbc_chckbxIterateUntilNoMoreReplaced.insets = new Insets(0, 0, 5, 0);
                gbc_chckbxIterateUntilNoMoreReplaced.gridx = 2;
                gbc_chckbxIterateUntilNoMoreReplaced.gridy = 1;
                panelValidationConfig.add(chckbxIterateUntilNoMoreReplaced, gbc_chckbxIterateUntilNoMoreReplaced);
            }
            {
                JLabel lblVectorValidationStrategy = new JLabel("Vector validation strategy");
                GridBagConstraints gbc_lblVectorValidationStrategy = new GridBagConstraints();
                gbc_lblVectorValidationStrategy.anchor = GridBagConstraints.WEST;
                gbc_lblVectorValidationStrategy.insets = new Insets(0, 0, 5, 5);
                gbc_lblVectorValidationStrategy.gridx = 0;
                gbc_lblVectorValidationStrategy.gridy = 2;
                panelValidationConfig.add(lblVectorValidationStrategy, gbc_lblVectorValidationStrategy);
            }
            {
                comboBoxValidationStrategy = new JComboBox<>();
                comboBoxValidationStrategy.setModel(new VelocityValidationComboBoxModel());
                comboBoxValidationStrategy.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateVelocityValidationOptionsPanel((VelocityValidationModeEnum)comboBoxValidationStrategy.getSelectedItem());
                    }
                });
                GridBagConstraints gbc_comboBoxValidationStrategy = new GridBagConstraints();
                gbc_comboBoxValidationStrategy.gridwidth = 2;
                gbc_comboBoxValidationStrategy.insets = new Insets(0, 0, 5, 0);
                gbc_comboBoxValidationStrategy.fill = GridBagConstraints.HORIZONTAL;
                gbc_comboBoxValidationStrategy.gridx = 1;
                gbc_comboBoxValidationStrategy.gridy = 2;
                panelValidationConfig.add(comboBoxValidationStrategy, gbc_comboBoxValidationStrategy);
            }
            {
                panelValidationAlgorithmOptions = new JPanel();
                panelValidationAlgorithmOptions.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
                GridBagConstraints gbc_panelValidationAlgorithmOptions_1 = new GridBagConstraints();
                gbc_panelValidationAlgorithmOptions_1.gridwidth = 3;
                gbc_panelValidationAlgorithmOptions_1.fill = GridBagConstraints.BOTH;
                gbc_panelValidationAlgorithmOptions_1.gridx = 0;
                gbc_panelValidationAlgorithmOptions_1.gridy = 3;
                panelValidationConfig.add(panelValidationAlgorithmOptions, gbc_panelValidationAlgorithmOptions_1);
                GridBagLayout gbl_panelValidationAlgorithmOptions = new GridBagLayout();
                gbl_panelValidationAlgorithmOptions.columnWidths = new int[]{0};
                gbl_panelValidationAlgorithmOptions.rowHeights = new int[]{0};
                gbl_panelValidationAlgorithmOptions.columnWeights = new double[]{Double.MIN_VALUE};
                gbl_panelValidationAlgorithmOptions.rowWeights = new double[]{Double.MIN_VALUE};
                panelValidationAlgorithmOptions.setLayout(gbl_panelValidationAlgorithmOptions);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                    public void actionPerformed(ActionEvent e) {
                        validateAndClose(true);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        initDataBindings();
        postInitDataBindings();
        
        MarginsValidator topValidator = (MarginsValidator)topMarginBinding.getValidator();
        topValidator.setInitialValues(1200, 0, InterrogationAreaResolutionEnum.IA0);
        MarginsValidator bottomValidator = (MarginsValidator)bottomMarginBinding.getValidator();
        bottomValidator.setInitialValues(1200, 0, InterrogationAreaResolutionEnum.IA0);

        MarginsValidator leftValidator = (MarginsValidator)leftMarginBinding.getValidator();
        leftValidator.setInitialValues(1600, 0, InterrogationAreaResolutionEnum.IA0);
        MarginsValidator rightValidator = (MarginsValidator)leftMarginBinding.getValidator();
        rightValidator.setInitialValues(1600, 0, InterrogationAreaResolutionEnum.IA0);
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    private boolean validateForErrors() {
        PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        if (model.getImagePatternA() == null || model.getImagePatternA().isEmpty()) {
            return false;
        }

        if (model.getImagePatternB() == null || model.getImagePatternB().isEmpty()) {
            return false;
        }
        
        if (model.getImageType() == null) {
            return false;
        }
        
        if (model.getSourceImageFile() == null) {
            return false;
        }
        
        if (model.getSourceImageFolder() == null) {
            return false;
        }
        
        if (model.getNumberOfImages() == 0) {
            return false;
        }
        
        for (ErrorBorderForComponent border : borders) {
            if ((border.getComponent() == null || border.getComponent().isEnabled()) && border.isErrored()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void validateAndClose(boolean cancel) {
        try {
            cancelled = cancel;
            if (!cancel && !validateForErrors()) {
                JOptionPane.showMessageDialog(this, "Data entered is incorrect, or missing.\n"
                        + "Please correct or complete fields and try again.", "PIV Image Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            //
            setVisible(false);
        } finally {
            PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
            //Enforce default value for clipping mode if not set yet
            if (pivModel.getClippingMode() == null) {
                pivModel.setClippingMode(ClippingModeEnum.AllowedOutOfBoundClipping);
            }
            MarginsValidator topValidator = (MarginsValidator)topMarginBinding.getValidator();
            pivModel.removePropertyChangeListener(topValidator);

            MarginsValidator bottomValidator = (MarginsValidator)bottomMarginBinding.getValidator();
            pivModel.removePropertyChangeListener(bottomValidator);
            
            MarginsValidator leftValidator = (MarginsValidator)leftMarginBinding.getValidator();
            pivModel.removePropertyChangeListener(leftValidator);
            
            MarginsValidator rightValidator = (MarginsValidator)rightMarginBinding.getValidator();
            pivModel.removePropertyChangeListener(rightValidator);
        }
    }

    private void initialResolutionChanged() {
        updateNumberOfAdaptiveSteps();
        
        InterrogationAreaResolutionEnum endResolution  = (InterrogationAreaResolutionEnum)comboBoxIAEndResolution.getSelectedItem();
        InterrogationAreaResolutionEnum initialResolution = initialIAResolutionComboModel.getSelectedItem();
        
        updateStartStepComboBoxes(endResolution, initialResolution);
    }
    
    protected void updateInitialComboBoxIAResolution(JComboBox<InterrogationAreaResolutionEnum> source) {
        InterrogationAreaResolutionEnum endResolution  = ((InterrogationAreaResolutionComboBoxModel)source.getModel()).getSelectedItem();
        InterrogationAreaResolutionEnum[] validInitialResolutions = InterrogationAreaResolutionEnum.getResolutionsAboveOrEqual(endResolution);
        
        initialIAResolutionComboModel.updateAvailableResolutions(validInitialResolutions);
        comboBoxIAInitialResolution.setModel(initialIAResolutionComboModel);
                
        updateNumberOfAdaptiveSteps();
        
        updateStartStepComboBoxes(endResolution, initialIAResolutionComboModel.getSelectedItem());
    }
    
    private void updateStartStepComboBoxes(InterrogationAreaResolutionEnum endResolution, InterrogationAreaResolutionEnum initialResolution) {
        //Must also update start step configurations 
        // - if initial resolution could be kept:
        // Then provide all interrogation area sizes between and including initial and end resolutions
        // - If initial resolution could not be kept: show only invalid resolution for those ComboBoxes
        //Update will also have to be performed when user changes the initial resolution
       
        InterrogationAreaResolutionEnum[] validSteps = InterrogationAreaResolutionEnum.getResolutionsBetweenOrEqual(endResolution, initialResolution);
        interpolationStartStepComboModel.updateAvailableResolutions(validSteps);
        superpositionStartStepComboModel.updateAvailableResolutions(validSteps);
    }

    private void updateValidatorMaxIterationsEnabledState(boolean selected) {
        frmtdtxtfldMaxiterations.setEnabled(!selected);        
    }

    protected JPanel getPanelInterrogationArea() {
        return panelInterrogationArea;
    }
    
    protected JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    protected JComboBox<InterrogationAreaResolutionEnum> getComboBoxIAInitialResolution() {
        return comboBoxIAInitialResolution;
    }

    protected JLabel getLblAdaptiveStepsCount() {
        return lblAdaptiveStepsCount;
    }

    private void updateInitialPanelEnabledState(PIVConfigurationModel model) {
        if (model.getSuperpositionOverlapPercentage() == 0.0f) {
            updateSuperpositionEnableState(false);
        } else {
            updateSuperpositionEnableState(true);
        }
        
        if (model.getInterpolationMode() == SubPixelInterpolationModeEnum.Disabled) {
            updateInterpolationEnableState(false);
        } else {
            updateInterpolationEnableState(true);
        }
        
        if (model.getStabilizationMode() == VelocityStabilizationModeEnum.Disabled) {
            updateStabilizationEnableState(false);
        } else {
            updateStabilizationEnableState(true);
        }
        
        updateValidatorMaxIterationsEnabledState(model.isValidationIterateUntilNoMoreReplaced());
        
        if (model.getValidationMode() == VelocityValidationModeEnum.Disabled) {
            updateValidationEnableState(false);
        } else {
            updateValidationEnableState(true);
        }
    }
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        panelInfo.setAppContext(model);
        panelClippingInfo.setAppContext(model);
        panelWarpingImageInfo.setAppContext(model);
        panelInheritanceImageInfo.setAppContext(model);
        panelSuperpositionImageInfo.setAppContext(model);
        panelInterpolationImageInfo.setAppContext(model);
        panelStabilizationImageInfo.setAppContext(model);
        panelValidationImageInfo.setAppContext(model);
        
        ((InvalidNotAllowedSubPixelInterpolationValidator)subPixelInterpolationModeBinding.getValidator()).setAppContext(model);
        
        updateInitialPanelEnabledState(pivModel);

        //Margins validators depend on other values which are not bound and must be manually initialized
        MarginsValidator topValidator = (MarginsValidator)topMarginBinding.getValidator();
        topValidator.setInitialValues(pivModel.getImageHeight(), pivModel.getBottomMargin(), pivModel.getInitialResolution());
        pivModel.addPropertyChangeListener(topValidator);

        MarginsValidator bottomValidator = (MarginsValidator)bottomMarginBinding.getValidator();
        bottomValidator.setInitialValues(pivModel.getImageHeight(), pivModel.getTopMargin(), pivModel.getInitialResolution());
        pivModel.addPropertyChangeListener(bottomValidator);
        
        MarginsValidator leftValidator = (MarginsValidator)leftMarginBinding.getValidator();
        leftValidator.setInitialValues(pivModel.getImageWidth(), pivModel.getRightMargin(), pivModel.getInitialResolution());
        pivModel.addPropertyChangeListener(leftValidator);
        
        MarginsValidator rightValidator = (MarginsValidator)rightMarginBinding.getValidator();
        rightValidator.setInitialValues(pivModel.getImageWidth(), pivModel.getLeftMargin(), pivModel.getInitialResolution());
        pivModel.addPropertyChangeListener(rightValidator);
                
        iaEndResolutionComboBinding.unbind();
        iaEndResolutionComboBinding.setSourceObject(appContext);
        iaEndResolutionComboBinding.bind();
        
        iaInitialResolutionComboBinding.unbind();
        iaInitialResolutionComboBinding.setSourceObject(appContext);
        iaInitialResolutionComboBinding.bind();
        
        leftMarginBinding.unbind();
        leftMarginBinding.setSourceObject(appContext);
        leftMarginBinding.bind();
        
        rightMarginBinding.unbind();
        rightMarginBinding.setSourceObject(appContext);
        rightMarginBinding.bind();
        
        topMarginBinding.unbind();
        topMarginBinding.setSourceObject(appContext);
        topMarginBinding.bind();
        
        bottomMarginBinding.unbind();
        bottomMarginBinding.setSourceObject(appContext);
        bottomMarginBinding.bind();
        
        clippingModeBinding.unbind();
        clippingModeBinding.setSourceNullValue(ClippingModeEnum.AllowedOutOfBoundClipping);
        clippingModeBinding.setSourceObject(appContext);
        clippingModeBinding.bind();
        
        labelClippingFinalAreaBinding.unbind();
        labelClippingFinalAreaBinding.setSourceObject(appContext);
        labelClippingFinalAreaBinding.bind();
        
        labelClippingInitialAreaBinding.unbind();
        labelClippingInitialAreaBinding.setSourceObject(appContext);
        labelClippingInitialAreaBinding.bind();
        
        warpingModeBinding.unbind();
        warpingModeBinding.setSourceObject(appContext);
        warpingModeBinding.bind();
        
        inheritanceModeBinding.unbind();
        inheritanceModeBinding.setSourceObject(appContext);
        inheritanceModeBinding.bind();
        
        superpositionOverlapPercentageBinding.unbind();
        superpositionOverlapPercentageBinding.setSourceObject(appContext);
        superpositionOverlapPercentageBinding.bind();
        
        superpositionStartStepBinding.unbind();
        superpositionStartStepBinding.setSourceObject(model);
        superpositionStartStepBinding.bind();
        
        subPixelInterpolationModeBinding.unbind();
        subPixelInterpolationModeBinding.setSourceObject(model);
        subPixelInterpolationModeBinding.bind();
        
        interpolationStartStepBinding.unbind();
        interpolationStartStepBinding.setSourceObject(model);
        interpolationStartStepBinding.bind();
        
        velocityStabilizationModeBinding.unbind();
        velocityStabilizationModeBinding.setSourceObject(model);
        velocityStabilizationModeBinding.bind();
        
        validatorReplaceAllInvalidBinding.unbind();
        validatorReplaceAllInvalidBinding.setSourceObject(model);
        validatorReplaceAllInvalidBinding.bind();
        
        validatorMaxIterationsBinding.unbind();
        validatorMaxIterationsBinding.setSourceObject(model);
        validatorMaxIterationsBinding.bind();

        validatorIterateUntilNoMoreReplaced.unbind();
        validatorIterateUntilNoMoreReplaced.setSourceObject(model);
        validatorIterateUntilNoMoreReplaced.bind();

        velocityValidationModeBinding.unbind();
        velocityValidationModeBinding.setSourceObject(model);
        velocityValidationModeBinding.bind();
    }

    public void selectTab(PIVConfigurationTabEnum startTab) {
        tabbedPane.setSelectedIndex(startTab.getIndex());
    }
    
    private DefaultFormatter createPixelFormatter() {
        NumberFormat format  = new DecimalFormat("#####");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(5);
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }

    private DefaultFormatter createPercentageFormatter() {
        DecimalFormat format  = new DecimalFormat("#0.000 percent");
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        format.setRoundingMode(RoundingMode.HALF_EVEN);
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.000f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }

    private void updateNumberOfAdaptiveSteps() {
        if (comboBoxIAEndResolution.getModel().getSelectedItem() != null && initialIAResolutionComboModel.getSelectedItem() != null) {
            InterrogationAreaResolutionEnum endResolution = (InterrogationAreaResolutionEnum)comboBoxIAEndResolution.getSelectedItem();
            InterrogationAreaResolutionEnum initialResolution = initialIAResolutionComboModel.getSelectedItem();
            lblAdaptiveStepsCount.setText((initialResolution.ordinal() - endResolution.ordinal() + 1) + " steps");
        } else {
            lblAdaptiveStepsCount.setText("0 steps");
        }
    }
      
    protected JPanel getPanelInfo() {
        return panelInfo;
    }
    
    protected ImageInfoPanel getPanelClippingInfo() {
        return panelClippingInfo;
    }
    
    protected ImageInfoPanel getPanelInheritanceInfo() {
        return panelInheritanceImageInfo;
    }
    
    protected ImageInfoPanel getPanelSuperpositionInfo() {
        return panelSuperpositionImageInfo;
    }
    
    protected ImageInfoPanel getPanelInterpolationInfo() {
        return panelInterpolationImageInfo;
    }
    
    protected ImageInfoPanel getPanelStabilizationInfo() {
        return panelStabilizationImageInfo;
    }
    
    protected void postInitDataBindings() {
        //The following will fail to update model with new combo box value
        //ELProperty<JComboBox<InterrogationAreaResolutionEnum>, InterrogationAreaResolutionEnum> jComboBoxEvalutionProperty_1 = ELProperty.create("${model.selectedItem}");

        //Interrogation Area tab borders
        ErrorBorderForComponent borderIAEndResolution = new ErrorBorderForComponent(comboBoxIAEndResolution);
        comboBoxIAEndResolution.setBorder(borderIAEndResolution);
        iaEndResolutionComboBinding.addBindingListener(borderIAEndResolution);
        borders.add(borderIAEndResolution);
        //
        ErrorBorderForComponent borderIAInitialResolution = new ErrorBorderForComponent(comboBoxIAInitialResolution);
        comboBoxIAInitialResolution.setBorder(borderIAInitialResolution);
        iaInitialResolutionComboBinding.addBindingListener(borderIAInitialResolution);
        borders.add(borderIAInitialResolution);
        //
        //Clipping tab borders
        ErrorBorderForComponent borderLeftMargin = new ErrorBorderForComponent(frmtdtxtfldLeftMargin);
        frmtdtxtfldLeftMargin.setBorder(borderLeftMargin);
        leftMarginBinding.addBindingListener(borderLeftMargin);
        borders.add(borderLeftMargin);
        //
        ErrorBorderForComponent borderRightMargin = new ErrorBorderForComponent(frmtdtxtfldRightMargin);
        frmtdtxtfldRightMargin.setBorder(borderRightMargin);
        rightMarginBinding.addBindingListener(borderRightMargin);
        borders.add(borderRightMargin);
        //
        ErrorBorderForComponent borderTopMargin = new ErrorBorderForComponent(frmtdtxtfldTopMargin);
        frmtdtxtfldTopMargin.setBorder(borderTopMargin);
        topMarginBinding.addBindingListener(borderTopMargin);
        borders.add(borderTopMargin);
        //
        ErrorBorderForComponent borderBottomMargin = new ErrorBorderForComponent(frmtdtxtfldBottomMargin);
        frmtdtxtfldBottomMargin.setBorder(borderBottomMargin);
        bottomMarginBinding.addBindingListener(borderBottomMargin);
        borders.add(borderBottomMargin);
        //
        //Warping mode tab borders
        ErrorBorderForComponent borderWarping = new ErrorBorderForComponent(comboBoxWarpingMode);
        comboBoxWarpingMode.setBorder(borderWarping);
        warpingModeBinding.addBindingListener(borderWarping);
        borders.add(borderWarping);
        //
        //Inheritance tab borders
        ErrorBorderForComponent borderInheritance = new ErrorBorderForComponent(comboBoxInheritance);
        comboBoxInheritance.setBorder(borderInheritance);
        inheritanceModeBinding.addBindingListener(borderInheritance);
        borders.add(borderInheritance);
        //
        //Super-position tab borders
        ErrorBorderForComponent borderSuperpositionOverlapPercentage = new ErrorBorderForComponent(frmtdtxtfldSuperpositionPercentage);
        frmtdtxtfldSuperpositionPercentage.setBorder(borderSuperpositionOverlapPercentage);
        superpositionOverlapPercentageBinding.addBindingListener(borderSuperpositionOverlapPercentage);
        borders.add(borderSuperpositionOverlapPercentage);
        //
        ErrorBorderForComponent borderSuperpositionStartStep = new ErrorBorderForComponent(comboBoxSuperpositionStartStep);
        comboBoxSuperpositionStartStep.setBorder(borderSuperpositionStartStep);
        superpositionStartStepBinding.addBindingListener(borderSuperpositionStartStep);
        borders.add(borderSuperpositionStartStep);
        //
        //Interpolation tab borders
        ErrorBorderForComponent borderSubPixelInterpolation = new ErrorBorderForComponent(comboBoxInterpolationAlgorithm);
        comboBoxInterpolationAlgorithm.setBorder(borderSubPixelInterpolation);
        subPixelInterpolationModeBinding.addBindingListener(borderSubPixelInterpolation);
        borders.add(borderSubPixelInterpolation);
        //
        ErrorBorderForComponent borderInterpolationStartStep = new ErrorBorderForComponent(comboBoxInterpolationStartStep);
        comboBoxInterpolationStartStep.setBorder(borderInterpolationStartStep);
        interpolationStartStepBinding.addBindingListener(borderInterpolationStartStep);
        borders.add(borderInterpolationStartStep);
        //
        //Stabilization tab borders
        ErrorBorderForComponent borderVelocityStabilization = new ErrorBorderForComponent(comboBoxStabilizationStrategy);
        comboBoxStabilizationStrategy.setBorder(borderVelocityStabilization);
        velocityStabilizationModeBinding.addBindingListener(borderVelocityStabilization);
        borders.add(borderVelocityStabilization);
        //
        //Validation number of validator iterations
        ErrorBorderForComponent borderValidationNumberValidatorIterations = new ErrorBorderForComponent(frmtdtxtfldMaxiterations);
        frmtdtxtfldMaxiterations.setBorder(borderValidationNumberValidatorIterations);
        validatorMaxIterationsBinding.addBindingListener(borderValidationNumberValidatorIterations);
        borders.add(borderValidationNumberValidatorIterations);
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Interrogation Area tab bindings - error handling        
        NullInterrogationAreaEnumConverter iaEndResolutionConverter = (NullInterrogationAreaEnumConverter)iaEndResolutionComboBinding.getConverter();
        iaEndResolutionConverter.setValidatorOnConvertForward(iaEndResolutionComboBinding.getValidator());
        iaEndResolutionConverter.addStatusListener(borderIAEndResolution);
        //
        NullInterrogationAreaEnumConverter iaInitialResolutionConverter = (NullInterrogationAreaEnumConverter)iaInitialResolutionComboBinding.getConverter();
        iaInitialResolutionConverter.setValidatorOnConvertForward(iaInitialResolutionComboBinding.getValidator());
        iaInitialResolutionConverter.addStatusListener(borderIAInitialResolution);
        //
        //Clipping tab binding - error handling
        NullMarginsConverter leftMarginConverter = (NullMarginsConverter)leftMarginBinding.getConverter();
        leftMarginConverter.setValidatorOnConvertForward(leftMarginBinding.getValidator());
        leftMarginConverter.addStatusListener(borderLeftMargin);
        MarginsValidator leftMarginValidator = (MarginsValidator)leftMarginBinding.getValidator();
        leftMarginValidator.setAssociatedErrorBorder(borderLeftMargin);
        //
        NullMarginsConverter rightMarginConverter = (NullMarginsConverter)rightMarginBinding.getConverter();
        rightMarginConverter.setValidatorOnConvertForward(rightMarginBinding.getValidator());
        rightMarginConverter.addStatusListener(borderRightMargin);
        MarginsValidator rightMarginValidator = (MarginsValidator)rightMarginBinding.getValidator();
        rightMarginValidator.setAssociatedErrorBorder(borderRightMargin);
        //
        NullMarginsConverter topMarginConverter = (NullMarginsConverter)topMarginBinding.getConverter();
        topMarginConverter.setValidatorOnConvertForward(topMarginBinding.getValidator());
        topMarginConverter.addStatusListener(borderTopMargin);
        MarginsValidator topMarginValidator = (MarginsValidator)topMarginBinding.getValidator();
        topMarginValidator.setAssociatedErrorBorder(borderTopMargin);
        //
        NullMarginsConverter bottomMarginConverter = (NullMarginsConverter)bottomMarginBinding.getConverter();
        bottomMarginConverter.setValidatorOnConvertForward(bottomMarginBinding.getValidator());
        bottomMarginConverter.addStatusListener(borderBottomMargin);
        MarginsValidator bottomMarginValidator = (MarginsValidator)bottomMarginBinding.getValidator();
        bottomMarginValidator.setAssociatedErrorBorder(borderBottomMargin);
        //
        //Warping tab bindings - error handling
        NullWarpingModeConverter warpingConverter = (NullWarpingModeConverter)warpingModeBinding.getConverter();
        warpingConverter.setValidatorOnConvertForward(warpingModeBinding.getValidator());
        warpingConverter.addStatusListener(borderWarping);
        //
        //Super-position tab bindings - error handling
        PercentageConverter superpositionConverter = (PercentageConverter)superpositionOverlapPercentageBinding.getConverter();
        superpositionConverter.setValidatorOnConvertForward(superpositionOverlapPercentageBinding.getValidator());
        superpositionConverter.addStatusListener(borderSuperpositionOverlapPercentage);
        //
        NullInterrogationAreaEnumConverter superpositionStartStepConverter = (NullInterrogationAreaEnumConverter)superpositionStartStepBinding.getConverter();
        superpositionStartStepConverter.setValidatorOnConvertForward(superpositionStartStepBinding.getValidator());
        superpositionStartStepConverter.addStatusListener(borderSuperpositionStartStep);
        //
        //Inheritance tab binding - error handling
        NullInhertianceModeConverter inheritanceConverter = (NullInhertianceModeConverter)inheritanceModeBinding.getConverter();
        inheritanceConverter.setValidatorOnConvertForward(inheritanceModeBinding.getValidator());
        inheritanceConverter.addStatusListener(borderInheritance);
        //      
        //Interpolation tab binding - error handling
        NullSubPixelInterpolationConverter subPixelInterpolationConverter = (NullSubPixelInterpolationConverter)subPixelInterpolationModeBinding.getConverter();
        subPixelInterpolationConverter.setValidatorOnConvertForward(subPixelInterpolationModeBinding.getValidator());
        subPixelInterpolationConverter.addStatusListener(borderSubPixelInterpolation);
        //
        NullInterrogationAreaEnumConverter interpolationStartStepConverter = (NullInterrogationAreaEnumConverter)interpolationStartStepBinding.getConverter();
        interpolationStartStepConverter.setValidatorOnConvertForward(interpolationStartStepBinding.getValidator());
        interpolationStartStepConverter.addStatusListener(borderInterpolationStartStep);
        //
        //Stabilization tab binding - error handling
        NullVelocityStabilizationConverter velocityStabilizationConverter = (NullVelocityStabilizationConverter)velocityStabilizationModeBinding.getConverter();
        velocityStabilizationConverter.setValidatorOnConvertForward(velocityStabilizationModeBinding.getValidator());
        velocityStabilizationConverter.addStatusListener(borderVelocityStabilization);
        //
        //Validation max number of validator iterations - error handling
        NullGenericIntegerConverter validatorMaxValidatorIterationsConverter = (NullGenericIntegerConverter)validatorMaxIterationsBinding.getConverter();
        validatorMaxValidatorIterationsConverter.setValidatorOnConvertForward(validatorMaxIterationsBinding.getValidator());
        validatorMaxValidatorIterationsConverter.addStatusListener(borderValidationNumberValidatorIterations);
        IntegerRangeValidator validatorMaxValidatorIterationsValidator = (IntegerRangeValidator)validatorMaxIterationsBinding.getValidator();
        validatorMaxValidatorIterationsValidator.setMinAndMax(1, 100);
        validatorMaxValidatorIterationsValidator.setRangeType(RangeTypeEnum.ANY);
    }

    public AppContextModel getAppContext() {
        return appContext;
    }
    
    private void updateSuperpositionEnableState(boolean enable) {
        chckbxEnableSuperposition.setSelected(enable);
        if (enable) {
            DisabledPanel.enable(panelSuperpositionParameters);
        } else {
            DisabledPanel.disable(panelSuperpositionParameters);
            PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
            pivModel.setSuperpositionOverlapPercentage(0.0f);
        }
    }
    
    protected JPanel getPanelSuperpositionParameters() {
        return panelSuperpositionParameters;
    }
    
    private void updateInterpolationEnableState(boolean enable) {
        chckbxEnableSubpixelInterpolation.setSelected(enable);
        if (enable) {
            DisabledPanel.enable(panelInterpolationConfig);
        } else {
            DisabledPanel.disable(panelInterpolationConfig);
            PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
            pivModel.setInterpolationMode(SubPixelInterpolationModeEnum.Disabled);
        }
    }
    
    protected JPanel getPanelInterpolationConfig() {
        return panelInterpolationConfig;
    }
    
    private void updateStabilizationEnableState(boolean enable) {
        chckbxEnableVelocityStabilization.setSelected(enable);
        if (enable) {
            DisabledPanel.enable(panelStabilizationConfig);
        } else {
            DisabledPanel.disable(panelStabilizationConfig);
            PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
            pivModel.setStabilizationMode(VelocityStabilizationModeEnum.Disabled);
        }
    }

    private void updateValidationEnableState(boolean enable) {
        chckbxEnableVectorValidation.setSelected(enable);
        if (enable) {
            DisabledPanel.enable(panelValidationConfig);
        } else {
            DisabledPanel.disable(panelValidationConfig);
            PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
            pivModel.setValidationMode(VelocityValidationModeEnum.Disabled);
        }
    }

    private void addOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        borders.addAll(panelBorders);
    }
    
    private void removeOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        borders.removeAll(panelBorders);
    }
    
    private void updateSubPixelInterpolationOptionsPanel(SubPixelInterpolationModeEnum selectedAlgorithm) {
        if (currentSubPixelInterpolationOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentSubPixelInterpolationOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelWithBorders.dispose();
            panelInterpolationAlgorithmOptions.remove(currentSubPixelInterpolationOptionsPanel);
        }
        
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createSubPixelInterpolationOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentSubPixelInterpolationOptionsPanel = 
                PIVConfigurationFacade.updateSubPixelOptionPanelFromSelection(this, scrollPane, panelInterpolationAlgorithmOptions, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentSubPixelInterpolationOptionsPanel;
        if (panelWithBorders != null) {
        	addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }
    }
    
    private void updateVelocityStabilizationOptionsPanel(VelocityStabilizationModeEnum selectedAlgorithm) {
        if (currentVelocityStabilizationOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentVelocityStabilizationOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelStabilizationAlgorithmOptions.remove(currentVelocityStabilizationOptionsPanel);
        }
     
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createVelocityStabilizationOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentVelocityStabilizationOptionsPanel =
                PIVConfigurationFacade.updateStabilizationOptionsPanelFromSelection(this, panelStabilizationAlgorithmOptions, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentVelocityStabilizationOptionsPanel;
        if (panelWithBorders != null) {
        	addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }
    }

   private void updateVelocityValidationOptionsPanel(VelocityValidationModeEnum selectedAlgorithm) {
        if (currentVelocityValidationOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentVelocityValidationOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelValidationAlgorithmOptions.remove(currentVelocityValidationOptionsPanel);
        }
     
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createVelocityValidationOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentVelocityValidationOptionsPanel =
                PIVConfigurationFacade.updateValidationOptionsPanelFromSelection(this, panelValidationAlgorithmOptions, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentVelocityValidationOptionsPanel;
        if (panelWithBorders != null) {
            addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }
    }

    protected JPanel getPanelStabilizationConfig() {
        return panelStabilizationConfig;
    }
    
    protected JCheckBox getChckbxEnableVelocityStabilization() {
        return chckbxEnableVelocityStabilization;
    }
    
    protected JCheckBox getChckbxEnableSubpixelInterpolation() {
        return chckbxEnableSubpixelInterpolation;
    }
    
    protected JCheckBox getChckbxEnableSuperposition() {
        return chckbxEnableSuperposition;
    }
    
    protected JComboBox<InterrogationAreaResolutionEnum> getComboBoxInterpolationStartStep() {
        return comboBoxInterpolationStartStep;
    }
    
    protected JComboBox<InterrogationAreaResolutionEnum> getComboBoxSuperpositionStartStep() {
        return comboBoxSuperpositionStartStep;
    }
    
    protected JComboBox<InterrogationAreaResolutionEnum> getComboBoxIAEndResolution() {
        return comboBoxIAEndResolution;
    }
    
    protected JPanel getPanelInterpolationAlgorithmOptions() {
        return panelInterpolationAlgorithmOptions;
    }
    
    protected JPanel getPanelStabilizationAlgorithmOptions() {
        return panelStabilizationAlgorithmOptions;
    }
    
    protected JCheckBox getChckbxEnableVectorValidation() {
        return chckbxEnableVectorValidation;
    }
    
    protected JPanel getPanelValidationConfig() {
        return panelValidationConfig;
    }
    
    protected JPanel getPanelValidationAlgorithmOptions() {
        return panelValidationAlgorithmOptions;
    }
    
    protected void initDataBindings() {
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> interrogationAreaModelBeanProperty = BeanProperty.create("project.PIVConfiguration.endResolution");
        BeanProperty<JComboBox<InterrogationAreaResolutionEnum>, Object> jComboBoxEvalutionProperty = BeanProperty.create("selectedItem");
        iaEndResolutionComboBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, interrogationAreaModelBeanProperty, comboBoxIAEndResolution, jComboBoxEvalutionProperty, "iaEndResolutionComboBinding");
        iaEndResolutionComboBinding.setConverter(new NullInterrogationAreaEnumConverter());
        iaEndResolutionComboBinding.setValidator(new InvalidIANotAllowedValidator());
        iaEndResolutionComboBinding.bind();
        //
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> interrogationAreaModelBeanProperty_1 = BeanProperty.create("project.PIVConfiguration.initialResolution");
        BeanProperty<JComboBox<InterrogationAreaResolutionEnum>, Object> jComboBoxEvalutionProperty_1 = BeanProperty.create("selectedItem");
        iaInitialResolutionComboBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, interrogationAreaModelBeanProperty_1, comboBoxIAInitialResolution, jComboBoxEvalutionProperty_1, "iaInitialResolutionComboBinding");
        iaInitialResolutionComboBinding.setConverter(new NullInterrogationAreaEnumConverter());
        iaInitialResolutionComboBinding.setValidator(new InvalidIANotAllowedValidator());
        iaInitialResolutionComboBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> pIVConfigurationModelBeanProperty = BeanProperty.create("project.PIVConfiguration.leftMargin");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        leftMarginBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, pIVConfigurationModelBeanProperty, frmtdtxtfldLeftMargin, jFormattedTextFieldBeanProperty, "leftMarginBinding");
        leftMarginBinding.setConverter(new NullMarginsConverter());
        leftMarginBinding.setValidator(new MarginsValidator("rightMargin"));
        leftMarginBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> pIVConfigurationModelBeanProperty_1 = BeanProperty.create("project.PIVConfiguration.rightMargin");
        rightMarginBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, pIVConfigurationModelBeanProperty_1, frmtdtxtfldRightMargin, jFormattedTextFieldBeanProperty, "rightMarginBinding");
        rightMarginBinding.setConverter(new NullMarginsConverter());
        rightMarginBinding.setValidator(new MarginsValidator("leftMargin"));
        rightMarginBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> pIVConfigurationModelBeanProperty_2 = BeanProperty.create("project.PIVConfiguration.bottomMargin");
        bottomMarginBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, pIVConfigurationModelBeanProperty_2, frmtdtxtfldBottomMargin, jFormattedTextFieldBeanProperty, "bottomMarginBinding");
        bottomMarginBinding.setConverter(new NullMarginsConverter());
        bottomMarginBinding.setValidator(new MarginsValidator("topMargin"));
        bottomMarginBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> pIVConfigurationModelBeanProperty_3 = BeanProperty.create("project.PIVConfiguration.topMargin");
        topMarginBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, pIVConfigurationModelBeanProperty_3, frmtdtxtfldTopMargin, jFormattedTextFieldBeanProperty, "topMarginBinding");
        topMarginBinding.setConverter(new NullMarginsConverter());
        topMarginBinding.setValidator(new MarginsValidator("bottomMargin"));
        topMarginBinding.bind();
        //
        BeanProperty<AppContextModel, ClippingModeEnum> pIVConfigurationModelBeanProperty_4 = BeanProperty.create("project.PIVConfiguration.clippingMode");
        BeanProperty<JComboBox<ClippingModeEnum>, Object> jComboBoxEvalutionProperty_2 = BeanProperty.create("selectedItem");
        clippingModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, pIVConfigurationModelBeanProperty_4, comboBoxClippingMode, jComboBoxEvalutionProperty_2, "clippingModeBinding");
        clippingModeBinding.setConverter(new NullClippingModeConverter());
        clippingModeBinding.bind();
        //
        //
        BeanProperty<AppContextModel, WarpingModeEnum> appContextModelBeanProperty_12 = BeanProperty.create("project.PIVConfiguration.warpingMode");
        BeanProperty<JComboBox<WarpingModeEnum>, Object> jComboBoxBeanProperty_5 = BeanProperty.create("selectedItem");
        warpingModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_12, comboBoxWarpingMode, jComboBoxBeanProperty_5, "warpingModeComboBinding");
        warpingModeBinding.setConverter(new NullWarpingModeConverter());
        warpingModeBinding.setValidator(new InvalidNotAllowedWarpingModeValidator());
        warpingModeBinding.bind();        
        //
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> appContextModelBeanProperty = BeanProperty.create("project.PIVConfiguration.endResolution");
        BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
        labelClippingFinalAreaBinding = Bindings.createAutoBinding(UpdateStrategy.READ, appContext, appContextModelBeanProperty, lblFinalArea, jLabelBeanProperty, "labelClippingFinalAreaBinding");
        labelClippingFinalAreaBinding.setConverter(new IAResolutionToStringConverter());
        labelClippingFinalAreaBinding.bind();
        //
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> appContextModelBeanProperty_1 = BeanProperty.create("project.PIVConfiguration.initialResolution");
        labelClippingInitialAreaBinding = Bindings.createAutoBinding(UpdateStrategy.READ, appContext, appContextModelBeanProperty_1, lblInitialarea, jLabelBeanProperty, "labelClippingInitialAreaBinding");
        labelClippingInitialAreaBinding.setConverter(new IAResolutionToStringConverter());
        labelClippingInitialAreaBinding.bind();
        //
        BeanProperty<AppContextModel, SubPixelInterpolationModeEnum> appContextModelBeanProperty_2 = BeanProperty.create("project.PIVConfiguration.interpolationMode");
        BeanProperty<JComboBox<SubPixelInterpolationModeEnum>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        subPixelInterpolationModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_2, comboBoxInterpolationAlgorithm, jComboBoxBeanProperty, "subPixelInterpolationModeBinding");
        subPixelInterpolationModeBinding.setConverter(new NullSubPixelInterpolationConverter());
        subPixelInterpolationModeBinding.setValidator(new InvalidNotAllowedSubPixelInterpolationValidator());
        subPixelInterpolationModeBinding.bind();
        //
        BeanProperty<AppContextModel, VelocityStabilizationModeEnum> appContextModelBeanProperty_3 = BeanProperty.create("project.PIVConfiguration.stabilizationMode");
        BeanProperty<JComboBox<VelocityStabilizationModeEnum>, Object> jComboBoxBeanProperty_1 = BeanProperty.create("selectedItem");
        velocityStabilizationModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_3, comboBoxStabilizationStrategy, jComboBoxBeanProperty_1, "velocityStabilizationModeBinding");
        velocityStabilizationModeBinding.setConverter(new NullVelocityStabilizationConverter());
        velocityStabilizationModeBinding.setValidator(new InvalidNotAllowedVelocityStabilizationValidator());
        velocityStabilizationModeBinding.bind();
        //
        BeanProperty<AppContextModel, InheritanceModeEnum> appContextModelBeanProperty_4 = BeanProperty.create("project.PIVConfiguration.inheritanceMode");
        BeanProperty<JComboBox<InheritanceModeEnum>, Object> jComboBoxBeanProperty_2 = BeanProperty.create("selectedItem");
        inheritanceModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_4, comboBoxInheritance, jComboBoxBeanProperty_2, "inheritanceModeBinding");
        inheritanceModeBinding.setConverter(new NullInhertianceModeConverter());
        inheritanceModeBinding.setValidator(new InvalidNotAllowedInheritanceValidator());
        inheritanceModeBinding.bind();
        //
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> appContextModelBeanProperty_5 = BeanProperty.create("project.PIVConfiguration.superpositionStartStep");
        BeanProperty<JComboBox<InterrogationAreaResolutionEnum>, Object> jComboBoxBeanProperty_3 = BeanProperty.create("selectedItem");
        superpositionStartStepBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_5, comboBoxSuperpositionStartStep, jComboBoxBeanProperty_3, "superpositionStartStepBinding");
        superpositionStartStepBinding.setConverter(new NullInterrogationAreaEnumConverter());
        superpositionStartStepBinding.setValidator(new InvalidIANotAllowedValidator());
        superpositionStartStepBinding.bind();
        //
        BeanProperty<AppContextModel, InterrogationAreaResolutionEnum> appContextModelBeanProperty_6 = BeanProperty.create("project.PIVConfiguration.interpolationStartStep");
        interpolationStartStepBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_6, comboBoxInterpolationStartStep, jComboBoxBeanProperty_3, "interpolationStartStepBinding");
        interpolationStartStepBinding.setConverter(new NullInterrogationAreaEnumConverter());
        interpolationStartStepBinding.setValidator(new InvalidIANotAllowedValidator());
        interpolationStartStepBinding.bind();
        //
        BeanProperty<AppContextModel, Float> appContextModelBeanProperty_7 = BeanProperty.create("project.PIVConfiguration.superpositionOverlapPercentage");
        superpositionOverlapPercentageBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_7, frmtdtxtfldSuperpositionPercentage, jFormattedTextFieldBeanProperty, "superpositionOverlapPercentage");
        superpositionOverlapPercentageBinding.setConverter(new PercentageConverter());
        superpositionOverlapPercentageBinding.setValidator(new PercentageOverlapValidator());
        superpositionOverlapPercentageBinding.bind();
        //
        BeanProperty<AppContextModel, VelocityValidationModeEnum> appContextModelBeanProperty_8 = BeanProperty.create("project.PIVConfiguration.validationMode");
        BeanProperty<JComboBox<VelocityValidationModeEnum>, Object> jComboBoxBeanProperty_4 = BeanProperty.create("selectedItem");
        velocityValidationModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_8, comboBoxValidationStrategy, jComboBoxBeanProperty_4, "velocityValidationBinding");
        velocityValidationModeBinding.setConverter(new NullVelocityValidationConverter());
        velocityValidationModeBinding.setValidator(new InvalidNotAllowedVelocityValidationValidator());
        velocityValidationModeBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_9 = BeanProperty.create("project.PIVConfiguration.validationReplaceInvalidByNaNs");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        validatorReplaceAllInvalidBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_9, chckbxReplaceAllInvalid, jCheckBoxBeanProperty, "validatorReplaceAllInvalidBinding");
        validatorReplaceAllInvalidBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_10 = BeanProperty.create("project.PIVConfiguration.validationIterateUntilNoMoreReplaced");
        validatorIterateUntilNoMoreReplaced = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_10, chckbxIterateUntilNoMoreReplaced, jCheckBoxBeanProperty, "validatorIterateUntilNoMoreReplaced");
        validatorIterateUntilNoMoreReplaced.bind();
        //
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty_11 = BeanProperty.create("project.PIVConfiguration.validationNumberOfValidatorIterations");
        validatorMaxIterationsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_11, frmtdtxtfldMaxiterations, jFormattedTextFieldBeanProperty, "validatorMaxIterationsBinding");
        validatorMaxIterationsBinding.setConverter(new NullGenericIntegerConverter());
        validatorMaxIterationsBinding.setValidator(new IntegerRangeValidator());
        validatorMaxIterationsBinding.bind();
    }
    protected JScrollPane getScrollPane() {
        return scrollPane;
    }
}
