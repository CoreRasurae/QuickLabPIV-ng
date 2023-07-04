package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.libs.external.DisabledPanel;
import pt.quickLabPIV.ui.controllers.ImageSelectionFacade;
import pt.quickLabPIV.ui.converters.AvailableImagesConverter;
import pt.quickLabPIV.ui.converters.ConverterWithForwardValidator;
import pt.quickLabPIV.ui.converters.FileConverter;
import pt.quickLabPIV.ui.converters.NullStringConverter;
import pt.quickLabPIV.ui.converters.NumberOfImagesConverter;
import pt.quickLabPIV.ui.converters.SeparatePathFileOnlyConverter;
import pt.quickLabPIV.ui.converters.TotalImagesConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.PIVImageTypeEnum;
import pt.quickLabPIV.ui.validators.CompositeValidator;
import pt.quickLabPIV.ui.validators.DifferentStringValidator;
import pt.quickLabPIV.ui.validators.FileValidator;
import pt.quickLabPIV.ui.validators.FolderValidator;
import pt.quickLabPIV.ui.validators.NumberOfImagesValidator;
import pt.quickLabPIV.ui.validators.RegexpValidator;

public class PIVImageSelection extends JDialog {
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> maskOnlyAtExportBinding;
    private AutoBinding<AppContextModel, Boolean, JCheckBox, Boolean> maskEnableBinding;
    private AutoBinding<AppContextModel, File, JTextField, String> maskFileBinding;
    private AutoBinding<AppContextModel, Integer, JFormattedTextField, String> numberOfImagesBinding;
    private AutoBinding<AppContextModel, Integer, JLabel, String> totalImagesBinding;
    private AutoBinding<AppContextModel, Integer, JLabel, String> availableImagesBinding;
    private AutoBinding<AppContextModel, String, JTextField, String> patternBBinding;
    private AutoBinding<AppContextModel, String, JTextField, String> patternABinding;
    private AutoBinding<AppContextModel, File, JTextField, String> sourceImageFileBinding;
    private AutoBinding<AppContextModel, File, JTextField, String> sourceImageFolderBinding;
    private SeparatePathFileOnlyConverter imageFileConverter;
    private List<ErrorBorderForComponent> borders = new LinkedList<ErrorBorderForComponent>();
    
    /**
     * 
     */
    private static final long serialVersionUID = 5869785217949514413L;
    private Logger logger = LoggerFactory.getLogger(PIVImageSelection.class);
    
    private AppContextModel appContext;
    private boolean canceled = false;
    
    private final JPanel contentPanel = new JPanel();
    private JTextField textField;
    private final ButtonGroup buttonGroupImageType = new ButtonGroup();
    private JTextField textFieldPatternA;
    private JTextField textFieldPatternB;
    private JTextField textFieldFirstFilename;
    private JRadioButton rdbtnImagePairs;
    private JRadioButton rdbtnImageSequence;
    private JLabel lblFromATotal;
    private JFormattedTextField formattedTextFieldNumberOfImages;
    private JTextField textFieldMaskFilename;
    private JCheckBox chckbxEnableImageMasking;
    protected JPanel panelImageMaskInner;
    private JCheckBox chckbxOnlyMaskAtExport;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            PIVImageSelection dialog = new PIVImageSelection();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create the dialog.
     */
    public PIVImageSelection() {
        setTitle("Images source selection");
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 800, 490);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{783, 0};
        gbl_contentPanel.rowHeights = new int[] {30, 0, 0, 0, 92};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JPanel panelImageSource = new JPanel();
            panelImageSource.setBorder(new TitledBorder(null, "Source image folder", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelImageSource = new GridBagConstraints();
            gbc_panelImageSource.anchor = GridBagConstraints.NORTH;
            gbc_panelImageSource.insets = new Insets(0, 0, 5, 0);
            gbc_panelImageSource.fill = GridBagConstraints.HORIZONTAL;
            gbc_panelImageSource.gridx = 0;
            gbc_panelImageSource.gridy = 0;
            contentPanel.add(panelImageSource, gbc_panelImageSource);
            GridBagLayout gbl_panelImageSource = new GridBagLayout();
            gbl_panelImageSource.columnWidths = new int[] {0, 0, 0};
            gbl_panelImageSource.rowHeights = new int[]{0, 0};
            gbl_panelImageSource.columnWeights = new double[]{0.0, 1.0, 0.0};
            gbl_panelImageSource.rowWeights = new double[]{0.0, Double.MIN_VALUE};
            panelImageSource.setLayout(gbl_panelImageSource);
            {
                JLabel lblPivImagesFolder = new JLabel("PIV Images folder");
                GridBagConstraints gbc_lblPivImagesFolder = new GridBagConstraints();
                gbc_lblPivImagesFolder.insets = new Insets(0, 0, 0, 5);
                gbc_lblPivImagesFolder.gridx = 0;
                gbc_lblPivImagesFolder.gridy = 0;
                panelImageSource.add(lblPivImagesFolder, gbc_lblPivImagesFolder);
            }
            {
                textField = new JTextField();
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        sourceImageFolderBinding.refreshAndNotify();
                    }
                });
                textField.setBorder(new ErrorBorderForComponent(textField));
                GridBagConstraints gbc_textField = new GridBagConstraints();
                gbc_textField.fill = GridBagConstraints.BOTH;
                gbc_textField.gridx = 1;
                gbc_textField.gridy = 0;
                panelImageSource.add(textField, gbc_textField);
                textField.setColumns(10);
            }
            {
                JButton buttonFolderSelection = new JButton("...");
                buttonFolderSelection.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showSourceFolderSelection();
                    }
                });
                GridBagConstraints gbc_buttonFolderSelection = new GridBagConstraints();
                gbc_buttonFolderSelection.insets = new Insets(0, 0, 1, 0);
                gbc_buttonFolderSelection.gridx = 2;
                gbc_buttonFolderSelection.gridy = 0;
                panelImageSource.add(buttonFolderSelection, gbc_buttonFolderSelection);
                buttonFolderSelection.setHorizontalAlignment(SwingConstants.LEFT);
            }
        }
        {
            JPanel panelImagesType = new JPanel();
            panelImagesType.setBorder(new TitledBorder(null, "Image type", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelImagesType = new GridBagConstraints();
            gbc_panelImagesType.insets = new Insets(0, 0, 5, 0);
            gbc_panelImagesType.fill = GridBagConstraints.BOTH;
            gbc_panelImagesType.gridx = 0;
            gbc_panelImagesType.gridy = 1;
            contentPanel.add(panelImagesType, gbc_panelImagesType);
            {
                rdbtnImagePairs = new JRadioButton("Image pairs");
                rdbtnImagePairs.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateImageType();
                    }
                });
                rdbtnImagePairs.setSelected(true);
                buttonGroupImageType.add(rdbtnImagePairs);
                panelImagesType.add(rdbtnImagePairs);
            }
            {
                rdbtnImageSequence = new JRadioButton("Image sequence");
                rdbtnImageSequence.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateImageType();
                    }
                });
                buttonGroupImageType.add(rdbtnImageSequence);
                panelImagesType.add(rdbtnImageSequence);
            }
        }
        {
            JPanel panelFilePattern = new JPanel();
            panelFilePattern.setBorder(new TitledBorder(null, "Image name patterns", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelFilePattern = new GridBagConstraints();
            gbc_panelFilePattern.insets = new Insets(0, 0, 5, 0);
            gbc_panelFilePattern.fill = GridBagConstraints.BOTH;
            gbc_panelFilePattern.gridx = 0;
            gbc_panelFilePattern.gridy = 2;
            contentPanel.add(panelFilePattern, gbc_panelFilePattern);
            GridBagLayout gbl_panelFilePattern = new GridBagLayout();
            gbl_panelFilePattern.columnWidths = new int[]{0, 0, 0};
            gbl_panelFilePattern.rowHeights = new int[]{0, 0, 0};
            gbl_panelFilePattern.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            gbl_panelFilePattern.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
            panelFilePattern.setLayout(gbl_panelFilePattern);
            {
                JLabel lblPatternA = new JLabel("Pattern A");
                GridBagConstraints gbc_lblPatternA = new GridBagConstraints();
                gbc_lblPatternA.anchor = GridBagConstraints.EAST;
                gbc_lblPatternA.insets = new Insets(0, 0, 5, 5);
                gbc_lblPatternA.gridx = 0;
                gbc_lblPatternA.gridy = 0;
                panelFilePattern.add(lblPatternA, gbc_lblPatternA);
            }
            {
                textFieldPatternA = new JTextField();
                GridBagConstraints gbc_textFieldPatternA = new GridBagConstraints();
                gbc_textFieldPatternA.insets = new Insets(0, 0, 5, 0);
                gbc_textFieldPatternA.fill = GridBagConstraints.HORIZONTAL;
                gbc_textFieldPatternA.gridx = 1;
                gbc_textFieldPatternA.gridy = 0;
                panelFilePattern.add(textFieldPatternA, gbc_textFieldPatternA);
                textFieldPatternA.setColumns(10);
            }
            {
                JLabel lblPatternB = new JLabel("Pattern B");
                GridBagConstraints gbc_lblPatternB = new GridBagConstraints();
                gbc_lblPatternB.anchor = GridBagConstraints.EAST;
                gbc_lblPatternB.insets = new Insets(0, 0, 0, 5);
                gbc_lblPatternB.gridx = 0;
                gbc_lblPatternB.gridy = 1;
                panelFilePattern.add(lblPatternB, gbc_lblPatternB);
            }
            {
                textFieldPatternB = new JTextField();
                GridBagConstraints gbc_textFieldPatternB = new GridBagConstraints();
                gbc_textFieldPatternB.fill = GridBagConstraints.HORIZONTAL;
                gbc_textFieldPatternB.gridx = 1;
                gbc_textFieldPatternB.gridy = 1;
                panelFilePattern.add(textFieldPatternB, gbc_textFieldPatternB);
                textFieldPatternB.setColumns(10);
            }
        }
        {
            JPanel panelImagesSelection = new JPanel();
            panelImagesSelection.setBorder(new TitledBorder(null, "Images Selection", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelImagesSelection = new GridBagConstraints();
            gbc_panelImagesSelection.insets = new Insets(0, 0, 5, 0);
            gbc_panelImagesSelection.fill = GridBagConstraints.BOTH;
            gbc_panelImagesSelection.gridx = 0;
            gbc_panelImagesSelection.gridy = 3;
            contentPanel.add(panelImagesSelection, gbc_panelImagesSelection);
            GridBagLayout gbl_panelImagesSelection = new GridBagLayout();
            gbl_panelImagesSelection.columnWidths = new int[]{0, 0, 0, 0, 0};
            gbl_panelImagesSelection.rowHeights = new int[]{0, 0, 0, 0, 0};
            gbl_panelImagesSelection.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
            gbl_panelImagesSelection.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
            panelImagesSelection.setLayout(gbl_panelImagesSelection);
            {
                JLabel lblStartFilename = new JLabel("Start filename");
                GridBagConstraints gbc_lblStartFilename = new GridBagConstraints();
                gbc_lblStartFilename.anchor = GridBagConstraints.EAST;
                gbc_lblStartFilename.insets = new Insets(0, 0, 5, 5);
                gbc_lblStartFilename.gridx = 0;
                gbc_lblStartFilename.gridy = 1;
                panelImagesSelection.add(lblStartFilename, gbc_lblStartFilename);
            }
            {
                textFieldFirstFilename = new JTextField();
                textFieldFirstFilename.setBorder(new ErrorBorderForComponent(textFieldFirstFilename));
                textFieldFirstFilename.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        sourceImageFileBinding.refreshAndNotify();
                    }
                });
                GridBagConstraints gbc_textFieldFistFilename = new GridBagConstraints();
                gbc_textFieldFistFilename.gridwidth = 2;
                gbc_textFieldFistFilename.insets = new Insets(0, 0, 5, 0);
                gbc_textFieldFistFilename.fill = GridBagConstraints.BOTH;
                gbc_textFieldFistFilename.gridx = 1;
                gbc_textFieldFistFilename.gridy = 1;
                panelImagesSelection.add(textFieldFirstFilename, gbc_textFieldFistFilename);
                textFieldFirstFilename.setColumns(10);
            }
            {
                JButton button = new JButton("...");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showFirstImageFileChooser();
                    }                    
                });
                GridBagConstraints gbc_button = new GridBagConstraints();
                gbc_button.fill = GridBagConstraints.BOTH;
                gbc_button.insets = new Insets(0, 0, 6, 0);
                gbc_button.gridx = 3;
                gbc_button.gridy = 1;
                panelImagesSelection.add(button, gbc_button);
            }
            {
                JLabel lblNumberOfImages = new JLabel("Number of images");
                GridBagConstraints gbc_lblNumberOfImages = new GridBagConstraints();
                gbc_lblNumberOfImages.anchor = GridBagConstraints.EAST;
                gbc_lblNumberOfImages.insets = new Insets(0, 0, 5, 5);
                gbc_lblNumberOfImages.gridx = 0;
                gbc_lblNumberOfImages.gridy = 2;
                panelImagesSelection.add(lblNumberOfImages, gbc_lblNumberOfImages);
            }
            {
                formattedTextFieldNumberOfImages = new JFormattedTextField(createNumberFormatter());
                GridBagConstraints gbc_formattedTextFieldNumberOfImages = new GridBagConstraints();
                gbc_formattedTextFieldNumberOfImages.insets = new Insets(0, 0, 5, 5);
                gbc_formattedTextFieldNumberOfImages.fill = GridBagConstraints.HORIZONTAL;
                gbc_formattedTextFieldNumberOfImages.gridx = 1;
                gbc_formattedTextFieldNumberOfImages.gridy = 2;
                panelImagesSelection.add(formattedTextFieldNumberOfImages, gbc_formattedTextFieldNumberOfImages);
            }
            {
                lblFromATotal = new JLabel("from #### available and a total of #### that match the image pattern");
                GridBagConstraints gbc_lblFromATotal = new GridBagConstraints();
                gbc_lblFromATotal.insets = new Insets(0, 0, 5, 5);
                gbc_lblFromATotal.gridx = 2;
                gbc_lblFromATotal.gridy = 2;
                panelImagesSelection.add(lblFromATotal, gbc_lblFromATotal);
            }
        }
        {
            JPanel panelImageMask = new JPanel();
            panelImageMask.setBorder(new TitledBorder(null, "Image Mask", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            GridBagConstraints gbc_panelImageMask = new GridBagConstraints();
            gbc_panelImageMask.gridheight = 2;
            gbc_panelImageMask.insets = new Insets(5, 0, 10, 0);
            gbc_panelImageMask.fill = GridBagConstraints.BOTH;
            gbc_panelImageMask.gridx = 0;
            gbc_panelImageMask.gridy = 4;
            contentPanel.add(panelImageMask, gbc_panelImageMask);
            GridBagLayout gbl_panelImageMask = new GridBagLayout();
            gbl_panelImageMask.columnWidths = new int[] {127, 30, 0, 3};
            gbl_panelImageMask.rowHeights = new int[] {41, 0, 0};
            gbl_panelImageMask.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
            gbl_panelImageMask.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
            panelImageMask.setLayout(gbl_panelImageMask);
            {
                chckbxEnableImageMasking = new JCheckBox("Use Image Mask");
                chckbxEnableImageMasking.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateImageMaskEnableState();                        
                    }
                });
                GridBagConstraints gbc_chckbxEnableImageMasking = new GridBagConstraints();
                gbc_chckbxEnableImageMasking.anchor = GridBagConstraints.WEST;
                gbc_chckbxEnableImageMasking.insets = new Insets(0, 0, 0, 5);
                gbc_chckbxEnableImageMasking.gridx = 0;
                gbc_chckbxEnableImageMasking.gridy = 0;
                panelImageMask.add(chckbxEnableImageMasking, gbc_chckbxEnableImageMasking);
            }
            {
                panelImageMaskInner = new JPanel();
                GridBagConstraints gbc_panel = new GridBagConstraints();
                gbc_panel.gridwidth = 2;
                gbc_panel.fill = GridBagConstraints.BOTH;
                gbc_panel.gridx = 1;
                gbc_panel.gridy = 0;
                panelImageMask.add(panelImageMaskInner, gbc_panel);
                GridBagLayout gbl_panel = new GridBagLayout();
                gbl_panel.columnWidths = new int[]{404, 45, 0};
                gbl_panel.rowHeights = new int[] {37, 2, 0};
                gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
                gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
                panelImageMaskInner.setLayout(gbl_panel);
                {
                    chckbxOnlyMaskAtExport = new JCheckBox("Only mask for the export step");
                    GridBagConstraints gbc_chckbxOnlyMaskAtExport = new GridBagConstraints();
                    gbc_chckbxOnlyMaskAtExport.anchor = GridBagConstraints.WEST;
                    gbc_chckbxOnlyMaskAtExport.insets = new Insets(0, 0, 5, 5);
                    gbc_chckbxOnlyMaskAtExport.gridx = 0;
                    gbc_chckbxOnlyMaskAtExport.gridy = 0;
                    panelImageMaskInner.add(chckbxOnlyMaskAtExport, gbc_chckbxOnlyMaskAtExport);
                }
                {
                    textFieldMaskFilename = new JTextField();
                    GridBagConstraints gbc_textFieldMaskFilename = new GridBagConstraints();
                    gbc_textFieldMaskFilename.insets = new Insets(5, 0, 0, 5);
                    gbc_textFieldMaskFilename.fill = GridBagConstraints.BOTH;
                    gbc_textFieldMaskFilename.gridx = 0;
                    gbc_textFieldMaskFilename.gridy = 2;
                    panelImageMaskInner.add(textFieldMaskFilename, gbc_textFieldMaskFilename);
                    textFieldMaskFilename.setColumns(60);
                }
                {
                    JButton btnSelectMaskFile = new JButton("...");
                    btnSelectMaskFile.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showImageMaskFileChooser();
                        }
                    });
                    GridBagConstraints gbc_btnSelectMaskFile = new GridBagConstraints();
                    gbc_btnSelectMaskFile.insets = new Insets(5, 0, 0, 0);
                    gbc_btnSelectMaskFile.fill = GridBagConstraints.HORIZONTAL;
                    gbc_btnSelectMaskFile.gridx = 1;
                    gbc_btnSelectMaskFile.gridy = 2;
                    panelImageMaskInner.add(btnSelectMaskFile, gbc_btnSelectMaskFile);
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
        furtherInitDataBindings();
    }    
    
    protected JRadioButton getRdbtnImagePairs() {
        return rdbtnImagePairs;
    }
    protected JRadioButton getRdbtnImageSequence() {
        return rdbtnImageSequence;
    }

    public boolean isCanceled() {       
        return canceled;
    }

    private void updateImageMaskEnableState() {        
        if (chckbxEnableImageMasking.isSelected()) {
            DisabledPanel.enable(panelImageMaskInner);
        } else {
            DisabledPanel.disable(panelImageMaskInner);
        }
    }
    
    private DefaultFormatter createNumberFormatter() {        
        NumberFormat format  = new DecimalFormat("0");
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setMaximumIntegerDigits(10);
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setOverwriteMode(true);
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private void showSourceFolderSelection() {
        try {
            appContext = ImageSelectionFacade.showImageFolderSelectionDialog(this, appContext);
        } catch (UIException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getTitleMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            logger.error("Failed to showSourceFolderSelection()", e);
            JOptionPane.showMessageDialog(this, "Unknown error occurred, see log for details.", "PIV Image selection", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setAppContext(AppContextModel appContextModel) {
       appContext = appContextModel;
       //Ensure that PIVConfigurationModel exists...
       PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
       
       setImageType();
       imageFileConverter.setFileFactory(appContext.getFileFactory());

       //This must be done before rebinding so that validations are performed correctly
       NumberOfImagesValidator validatorNumberOfImages = (NumberOfImagesValidator)numberOfImagesBinding.getValidator();
       model.addPropertyChangeListener(validatorNumberOfImages);       
       //Ensure that the total number of image files is defined in the validator, because when opening
       //projects no update event is generated for this property, and the validator needs to know the current value,
       //as well as the future updates of this initial value.
       validatorNumberOfImages.setTotalNumberOfImagesAccepted(model.getAvailableImageFiles());
       
       TotalImagesConverter totalImagesLabelConverter = (TotalImagesConverter)totalImagesBinding.getConverter();
       model.addPropertyChangeListener(totalImagesLabelConverter);
       totalImagesLabelConverter.setAvailableImages(model.getAvailableImageFiles());
       AvailableImagesConverter availableImagesLabelConverter = (AvailableImagesConverter)availableImagesBinding.getConverter();
       model.addPropertyChangeListener(availableImagesLabelConverter);
       availableImagesLabelConverter.setTotalImages(model.getTotalImageFiles());
       //
       CompositeValidator<? super String> validatorPatternA = (CompositeValidator<? super String>)patternABinding.getValidator();
       DifferentStringValidator differentValidatorPatternA = (DifferentStringValidator)validatorPatternA.getValidatorB();
       model.addPropertyChangeListener(differentValidatorPatternA);
       differentValidatorPatternA.setInitialOtherValue(model.getImagePatternB(), model.getImageType() == PIVImageTypeEnum.PIVImagePair);
       //
       //We can now re-bind...
       //
       //sourceImageFolderBinding has a failed binding because the path to the object has null entries, starting
       //by AppContextModel. So we have to re-bind with a valid source object that has the path up to PIVConfiguration, 
       //and since PIVConfiguration has PropertyChangeSupport it will notify the binding when a property changes.
       sourceImageFolderBinding.unbind();
       //The source object can only be set on unbound Bindings
       sourceImageFolderBinding.setSourceObject(appContext);
       sourceImageFolderBinding.bind();
       //
       sourceImageFileBinding.unbind();
       sourceImageFileBinding.setSourceObject(appContext);
       sourceImageFileBinding.bind();
       //
       patternABinding.unbind();
       patternABinding.setSourceObject(appContext);
       patternABinding.bind();
       //
       patternBBinding.unbind();
       patternBBinding.setSourceObject(appContext);
       patternBBinding.bind();
       //
       totalImagesBinding.unbind();
       totalImagesBinding.setSourceObject(appContext);
       totalImagesBinding.bind();
       //
       availableImagesBinding.unbind();
       availableImagesBinding.setSourceObject(appContext);
       availableImagesBinding.bind();
       //
       numberOfImagesBinding.unbind();
       numberOfImagesBinding.setSourceObject(appContext);
       numberOfImagesBinding.bind();
       //
       maskEnableBinding.unbind();
       maskEnableBinding.setSourceObject(appContext);
       maskEnableBinding.bind();
       //
       maskFileBinding.unbind();
       maskFileBinding.setSourceObject(appContext);
       maskFileBinding.bind();
       //
       maskOnlyAtExportBinding.unbind();
       maskOnlyAtExportBinding.setSourceObject(appContext);
       maskOnlyAtExportBinding.bind();
       //
       updateImageMaskEnableState();
    }

    private void showFirstImageFileChooser() {
        try {
            appContext = ImageSelectionFacade.showFirstImageFileChooser(this, appContext);
        } catch (UIException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), ex.getTitleMessage(), JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showImageMaskFileChooser() {
        try {
            appContext = ImageSelectionFacade.showImageMaskFileChooser(this, appContext);
        } catch (UIException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), ex.getTitleMessage(), JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public AppContextModel getAppContext() {
        return appContext;
    }
    
    protected void furtherInitDataBindings() {
        //LM IMPORTANT: Comment must be here otherwise it will be lost by automated bindings generator
        //Ensure that in  protected void initDataBindings() is:
        //imageFileConverter = new SeparatePathFileOnlyConverter();
        //and not:
        //Converter imageFileConverter = new SeparatePathFileOnlyConverter();
        
        //Here border is defined in constructor()
        ErrorBorderForComponent borderFolder = (ErrorBorderForComponent)textField.getBorder();
        //So that validation errors can be communicated to the error border:
        sourceImageFolderBinding.addBindingListener(borderFolder);
        borders.add(borderFolder);
        
        ErrorBorderForComponent borderFile = (ErrorBorderForComponent)textFieldFirstFilename.getBorder();
        sourceImageFileBinding.addBindingListener(borderFile);
        //So that errors can be set when model value is invalid
        imageFileConverter.setValidatorOnConvertForward(sourceImageFileBinding.getValidator());
        imageFileConverter.addStatusListener(borderFile);
        borders.add(borderFile);
        
        //Here border is newly created here
        ErrorBorderForComponent borderPatternA = new ErrorBorderForComponent(textFieldPatternA);
        textFieldPatternA.setBorder(borderPatternA);
        ConverterWithForwardValidator<String, String> converterPatternA = (ConverterWithForwardValidator<String, String>)patternABinding.getConverter();
        converterPatternA.setValidatorOnConvertForward(patternABinding.getValidator());
        converterPatternA.addStatusListener(borderPatternA);
        CompositeValidator<? super String> compositeValidatorPatternA = (CompositeValidator<? super String>)patternABinding.getValidator();
        DifferentStringValidator validatorPatternA = (DifferentStringValidator)compositeValidatorPatternA.getValidatorB();
        validatorPatternA.setErrorBorder(borderPatternA);
        patternABinding.addBindingListener(borderPatternA);
        borders.add(borderPatternA);
        
        ErrorBorderForComponent borderPatternB = new ErrorBorderForComponent(textFieldPatternB);
        textFieldPatternB.setBorder(borderPatternB);
        ConverterWithForwardValidator<String, String> converterPatternB = (ConverterWithForwardValidator<String, String>)patternBBinding.getConverter();
        converterPatternB.setValidatorOnConvertForward(patternBBinding.getValidator());
        converterPatternB.addStatusListener(borderPatternB);
        patternBBinding.addBindingListener(borderPatternB);
        borders.add(borderPatternB);
        
        ErrorBorderForComponent borderNumberOfImages = new ErrorBorderForComponent(formattedTextFieldNumberOfImages);
        formattedTextFieldNumberOfImages.setBorder(borderNumberOfImages);
        ConverterWithForwardValidator<Integer, String> converterNumberOfImages = (ConverterWithForwardValidator<Integer, String>)numberOfImagesBinding.getConverter();
        converterNumberOfImages.setValidatorOnConvertForward(numberOfImagesBinding.getValidator());
        converterNumberOfImages.addStatusListener(borderNumberOfImages);
        numberOfImagesBinding.addBindingListener(borderNumberOfImages);
        borders.add(borderNumberOfImages);
        
        ErrorBorderForComponent borderMaskFile = new ErrorBorderForComponent(textFieldMaskFilename);
        textFieldMaskFilename.setBorder(borderMaskFile);
        ConverterWithForwardValidator<File, String> converterFileMask = (ConverterWithForwardValidator<File,String>)maskFileBinding.getConverter();
        converterFileMask.setValidatorOnConvertForward(maskFileBinding.getValidator());
        converterFileMask.addStatusListener(borderMaskFile);
        maskFileBinding.addBindingListener(borderMaskFile);
        borders.add(borderMaskFile);      
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
        
        if (model.isMaskEnabled() && model.getMaskFile() == null) {
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
        canceled = cancel;
        if (!cancel && !validateForErrors()) {
            JOptionPane.showMessageDialog(this, "Data entered is incorrect, or missing.\n"
                    + "Please correct or complete fields and try again.", "PIV Image Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        NumberOfImagesValidator validatorNumberOfImages = (NumberOfImagesValidator)numberOfImagesBinding.getValidator();
        model.removePropertyChangeListener(validatorNumberOfImages);
        //
        CompositeValidator<? super String> validatorPatternA = (CompositeValidator<? super String>)patternABinding.getValidator();
        DifferentStringValidator differentValidatorPatternA = (DifferentStringValidator)validatorPatternA.getValidatorB();
        model.removePropertyChangeListener(differentValidatorPatternA);
        //
        TotalImagesConverter totalImagesLabelConverter = (TotalImagesConverter)totalImagesBinding.getConverter();
        model.removePropertyChangeListener(totalImagesLabelConverter);
        AvailableImagesConverter availableImagesLabelConverter = (AvailableImagesConverter)availableImagesBinding.getConverter();
        model.removePropertyChangeListener(availableImagesLabelConverter);
        //
        setVisible(false);
    }

    private void setImageType() {
        PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        
        PIVImageTypeEnum imageType = model.getImageType();
        if (imageType == null) {
            updateImageType();
            return;
        }
        
        switch (imageType) {
        case PIVImagePair:
            rdbtnImagePairs.setSelected(true);
            textFieldPatternB.setEnabled(true);
            break;
        case PIVImageSequence:
            rdbtnImageSequence.setSelected(true);
            textFieldPatternB.setEnabled(false);
            break;
        default:
            JOptionPane.showMessageDialog(this, "Unsupported image type", "Image selection", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateImageType() {
        PIVImageTypeEnum imageType = null;
        
        if (rdbtnImagePairs.isSelected()) {
            imageType = PIVImageTypeEnum.PIVImagePair;
        } else if (rdbtnImageSequence.isSelected()){
            imageType = PIVImageTypeEnum.PIVImageSequence;
        }
            
        PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        model.setImageType(imageType);
        switch (imageType) {
        case PIVImagePair:
            textFieldPatternB.setEnabled(true);
            break;
        case PIVImageSequence:
            textFieldPatternB.setEnabled(false);
            break;
        default:
            JOptionPane.showMessageDialog(this, "Unsupported image type", "Image selection", JOptionPane.ERROR_MESSAGE);
        }
    }
    protected void initDataBindings() {
        BeanProperty<AppContextModel, File> appContextModelBeanProperty = BeanProperty.create("project.PIVConfiguration.sourceImageFolder");
        BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
        sourceImageFolderBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty, textField, jTextFieldBeanProperty, "sourceImageFolderBinding");
        sourceImageFolderBinding.setConverter(new FileConverter());
        sourceImageFolderBinding.setValidator(new FolderValidator());
        sourceImageFolderBinding.bind();
        //
        BeanProperty<AppContextModel, File> appContextModelBeanProperty_1 = BeanProperty.create("project.PIVConfiguration.sourceImageFile");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        sourceImageFileBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_1, textFieldFirstFilename, jTextFieldBeanProperty_1, "sourceImageFileBinding");
        imageFileConverter = new SeparatePathFileOnlyConverter();
        sourceImageFileBinding.setConverter(imageFileConverter);
        sourceImageFileBinding.setValidator(new FileValidator());
        sourceImageFileBinding.bind();
        //
        BeanProperty<AppContextModel, String> appContextModelBeanProperty_2 = BeanProperty.create("project.PIVConfiguration.imagePatternA");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
        patternABinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_2, textFieldPatternA, jTextFieldBeanProperty_2, "patternABinding");
        patternABinding.setConverter(new NullStringConverter());
        patternABinding.setValidator(new CompositeValidator<String>(new RegexpValidator(), new DifferentStringValidator("imagePatternB")));
        patternABinding.bind();
        //
        BeanProperty<AppContextModel, String> appContextModelBeanProperty_3 = BeanProperty.create("project.PIVConfiguration.imagePatternB");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
        patternBBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_3, textFieldPatternB, jTextFieldBeanProperty_3, "patternBBinding");
        patternBBinding.setConverter(new NullStringConverter());
        patternBBinding.setValidator(new RegexpValidator());
        patternBBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty_4 = BeanProperty.create("project.PIVConfiguration.totalImageFiles");
        BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
        totalImagesBinding = Bindings.createAutoBinding(UpdateStrategy.READ, appContext, appContextModelBeanProperty_4, lblFromATotal, jLabelBeanProperty, "totalImagesBinding");
        totalImagesBinding.setConverter(new TotalImagesConverter());
        totalImagesBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty_5 = BeanProperty.create("project.PIVConfiguration.availableImageFiles");
        BeanProperty<JLabel, String> jLabelBeanProperty_2 = BeanProperty.create("text");
        availableImagesBinding = Bindings.createAutoBinding(UpdateStrategy.READ, appContext, appContextModelBeanProperty_5, lblFromATotal, jLabelBeanProperty_2, "availableImagesBinding");
        availableImagesBinding.setConverter(new AvailableImagesConverter());
        availableImagesBinding.bind();
        //
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty_6 = BeanProperty.create("project.PIVConfiguration.numberOfImages");
        BeanProperty<JFormattedTextField, String> jFormattedTextFieldBeanProperty = BeanProperty.create("text");
        numberOfImagesBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_6, formattedTextFieldNumberOfImages, jFormattedTextFieldBeanProperty, "numberOfImagesBinding");
        numberOfImagesBinding.setConverter(new NumberOfImagesConverter());
        numberOfImagesBinding.setValidator(new NumberOfImagesValidator());
        numberOfImagesBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_7 = BeanProperty.create("project.PIVConfiguration.maskEnabled");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        maskEnableBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_7, chckbxEnableImageMasking, jCheckBoxBeanProperty, "maskEnabledBinding");
        maskEnableBinding.bind();
        //
        BeanProperty<AppContextModel, File> appContextModelBeanProperty_8 = BeanProperty.create("project.PIVConfiguration.maskFile");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
        maskFileBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_8, textFieldMaskFilename, jTextFieldBeanProperty_4, "maskFileBinding");
        maskFileBinding.setConverter(new FileConverter());
        maskFileBinding.setValidator(new FileValidator());
        maskFileBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_9 = BeanProperty.create("project.PIVConfiguration.maskOnlyAtExport");
        maskOnlyAtExportBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContext, appContextModelBeanProperty_9, chckbxOnlyMaskAtExport, jCheckBoxBeanProperty, "onlyMaskAtExportBinding");
        maskOnlyAtExportBinding.bind();
    }
 }
