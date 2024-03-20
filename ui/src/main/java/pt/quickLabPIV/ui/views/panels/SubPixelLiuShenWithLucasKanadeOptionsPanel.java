// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.Color;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.libs.external.DisabledPanel;
import pt.quickLabPIV.ui.converters.NullGenericFloatConverter;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.IgnorePIVBaseDisplacementsModeEnum;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel;
import pt.quickLabPIV.ui.validators.FloatRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class SubPixelLiuShenWithLucasKanadeOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Boolean, JCheckBox, Boolean> ignorePIVBaseDisplacementsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel, Boolean, JCheckBox, Boolean> denseExportBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float, JFormattedTextField, Object> multiplierLsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> vectorsWindowSizeLsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> numberOfIterationsLsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> filterWidthPxLsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float, JFormattedTextField, Object> filterSigmaLsBinding;
    /**
     * 
     */
    private static final long serialVersionUID = -8477340924998944889L;
    
    
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> windowSizeBindingLK;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> numberOfIterationsBindingLK;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer, JFormattedTextField, Object> filterWidthPxBindingLK;
    private AutoBinding<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float, JFormattedTextField, Object> filterSigmaBindingLK;

    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(4);
    private JFormattedTextField formattedTextFieldIterationsLK;
    private JFormattedTextField formattedTextFieldWindowSizeLK;
    private JFormattedTextField formattedTextFieldFilterSigmaLK;
    private JFormattedTextField formattedTextFieldFilterWidthLK;
    
    private AppContextModel appContext;
    private SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel lsLkOptions;
    private SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel lsLkClOptions;
    private JFormattedTextField formattedTextFieldSigmaLS;
    private JFormattedTextField formattedTextFieldWidthPxLS;
    private JFormattedTextField formattedTextFieldIterationsLS;
    private JFormattedTextField formattedTextFieldVectorsSizeLS;
    private JFormattedTextField formattedTextFieldMultiplierLS;
    private JCheckBox chckbxExportDenseVectors;
    private JCheckBox chkboxIgnorePivDisplacement;
    private final ButtonGroup buttonGroupIgnorePIV = new ButtonGroup();
    private JRadioButton rdbtnPivU;
    private JRadioButton rdbtnPivV;
    private JRadioButton rdbtnPivBothUV;
    private JPanel panelIgnorePIV;
    private JRadioButton rdbtnIgnoreAuto;
    private JRadioButton rdbtnIgnoreAutoSmall;
    
    /**
     * Create the panel.
     */
    public SubPixelLiuShenWithLucasKanadeOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{382, 0, 0};
        gridBagLayout.rowHeights = new int[]{45, 47, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JPanel panelLK = new JPanel();
        panelLK.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Lucas-Kanade configuration options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelLK = new GridBagConstraints();
        gbc_panelLK.insets = new Insets(0, 0, 0, 5);
        gbc_panelLK.fill = GridBagConstraints.BOTH;
        gbc_panelLK.gridx = 0;
        gbc_panelLK.gridy = 2;
        add(panelLK, gbc_panelLK);
        GridBagLayout gbl_panelLK = new GridBagLayout();
        gbl_panelLK.columnWidths = new int[]{392, 0};
        gbl_panelLK.rowHeights = new int[]{111, 121, 0};
        gbl_panelLK.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_panelLK.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        panelLK.setLayout(gbl_panelLK);
        
        JPanel panelFilterOptions = new JPanel();
        GridBagConstraints gbc_panelFilterOptions = new GridBagConstraints();
        gbc_panelFilterOptions.fill = GridBagConstraints.BOTH;
        gbc_panelFilterOptions.insets = new Insets(0, 0, 5, 0);
        gbc_panelFilterOptions.gridx = 0;
        gbc_panelFilterOptions.gridy = 0;
        panelLK.add(panelFilterOptions, gbc_panelFilterOptions);
        panelFilterOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Gaussian filter options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagLayout gbl_panelFilterOptions = new GridBagLayout();
        gbl_panelFilterOptions.columnWidths = new int[]{0, 149, 171, 0, 0};
        gbl_panelFilterOptions.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelFilterOptions.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelFilterOptions.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelFilterOptions.setLayout(gbl_panelFilterOptions);
        
        JLabel lblSigmaValue = new JLabel("Sigma value");
        GridBagConstraints gbc_lblSigmaValue = new GridBagConstraints();
        gbc_lblSigmaValue.anchor = GridBagConstraints.WEST;
        gbc_lblSigmaValue.insets = new Insets(0, 0, 5, 5);
        gbc_lblSigmaValue.gridx = 1;
        gbc_lblSigmaValue.gridy = 1;
        panelFilterOptions.add(lblSigmaValue, gbc_lblSigmaValue);
        
        formattedTextFieldFilterSigmaLK = new JFormattedTextField(createFloatNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldFilterSigmaLK = new GridBagConstraints();
        gbc_formattedTextFieldFilterSigmaLK.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldFilterSigmaLK.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldFilterSigmaLK.gridx = 2;
        gbc_formattedTextFieldFilterSigmaLK.gridy = 1;
        panelFilterOptions.add(formattedTextFieldFilterSigmaLK, gbc_formattedTextFieldFilterSigmaLK);
        
        JLabel lblWidthpixels = new JLabel("Width (pixels)");
        GridBagConstraints gbc_lblWidthpixels = new GridBagConstraints();
        gbc_lblWidthpixels.anchor = GridBagConstraints.WEST;
        gbc_lblWidthpixels.insets = new Insets(0, 0, 0, 5);
        gbc_lblWidthpixels.gridx = 1;
        gbc_lblWidthpixels.gridy = 2;
        panelFilterOptions.add(lblWidthpixels, gbc_lblWidthpixels);
        
        formattedTextFieldFilterWidthLK = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldFilterWidthLK = new GridBagConstraints();
        gbc_formattedTextFieldFilterWidthLK.insets = new Insets(0, 0, 0, 5);
        gbc_formattedTextFieldFilterWidthLK.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldFilterWidthLK.gridx = 2;
        gbc_formattedTextFieldFilterWidthLK.gridy = 2;
        panelFilterOptions.add(formattedTextFieldFilterWidthLK, gbc_formattedTextFieldFilterWidthLK);
        
        JPanel panelLucasKanadeOptions = new JPanel();
        GridBagConstraints gbc_panelLucasKanadeOptions = new GridBagConstraints();
        gbc_panelLucasKanadeOptions.fill = GridBagConstraints.BOTH;
        gbc_panelLucasKanadeOptions.gridx = 0;
        gbc_panelLucasKanadeOptions.gridy = 1;
        panelLK.add(panelLucasKanadeOptions, gbc_panelLucasKanadeOptions);
        panelLucasKanadeOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Sparse Lucas-Kanade algorithm options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagLayout gbl_panelLucasKanadeOptions = new GridBagLayout();
        gbl_panelLucasKanadeOptions.columnWidths = new int[]{0, 0, 171, 0, 0};
        gbl_panelLucasKanadeOptions.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelLucasKanadeOptions.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelLucasKanadeOptions.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelLucasKanadeOptions.setLayout(gbl_panelLucasKanadeOptions);
        
        JLabel lblNumberOfIterationsLK = new JLabel("Number of iterations");
        GridBagConstraints gbc_lblNumberOfIterationsLK = new GridBagConstraints();
        gbc_lblNumberOfIterationsLK.anchor = GridBagConstraints.EAST;
        gbc_lblNumberOfIterationsLK.insets = new Insets(0, 0, 5, 5);
        gbc_lblNumberOfIterationsLK.gridx = 1;
        gbc_lblNumberOfIterationsLK.gridy = 1;
        panelLucasKanadeOptions.add(lblNumberOfIterationsLK, gbc_lblNumberOfIterationsLK);
        
        formattedTextFieldIterationsLK = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldIterationsLK = new GridBagConstraints();
        gbc_formattedTextFieldIterationsLK.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldIterationsLK.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldIterationsLK.gridx = 2;
        gbc_formattedTextFieldIterationsLK.gridy = 1;
        panelLucasKanadeOptions.add(formattedTextFieldIterationsLK, gbc_formattedTextFieldIterationsLK);
        
        JLabel lblWindowSizeLK = new JLabel("Window size");
        GridBagConstraints gbc_lblWindowSizeLK = new GridBagConstraints();
        gbc_lblWindowSizeLK.anchor = GridBagConstraints.EAST;
        gbc_lblWindowSizeLK.insets = new Insets(0, 0, 0, 5);
        gbc_lblWindowSizeLK.gridx = 1;
        gbc_lblWindowSizeLK.gridy = 2;
        panelLucasKanadeOptions.add(lblWindowSizeLK, gbc_lblWindowSizeLK);
        
        formattedTextFieldWindowSizeLK = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldWindowSizeLK = new GridBagConstraints();
        gbc_formattedTextFieldWindowSizeLK.insets = new Insets(0, 0, 0, 5);
        gbc_formattedTextFieldWindowSizeLK.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldWindowSizeLK.gridx = 2;
        gbc_formattedTextFieldWindowSizeLK.gridy = 2;
        panelLucasKanadeOptions.add(formattedTextFieldWindowSizeLK, gbc_formattedTextFieldWindowSizeLK);
        
        JPanel panelLS = new JPanel();
        panelLS.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Liu-Shen configuration options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelLS = new GridBagConstraints();
        gbc_panelLS.fill = GridBagConstraints.BOTH;
        gbc_panelLS.gridx = 1;
        gbc_panelLS.gridy = 2;
        add(panelLS, gbc_panelLS);
        GridBagLayout gbl_panelLS = new GridBagLayout();
        gbl_panelLS.columnWidths = new int[]{0, 0};
        gbl_panelLS.rowHeights = new int[]{111, 121, 0};
        gbl_panelLS.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panelLS.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        panelLS.setLayout(gbl_panelLS);
        
        JPanel panelFilterLS = new JPanel();
        panelFilterLS.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Gaussian filter options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelFilterLS = new GridBagConstraints();
        gbc_panelFilterLS.insets = new Insets(0, 0, 5, 0);
        gbc_panelFilterLS.fill = GridBagConstraints.BOTH;
        gbc_panelFilterLS.gridx = 0;
        gbc_panelFilterLS.gridy = 0;
        panelLS.add(panelFilterLS, gbc_panelFilterLS);
        GridBagLayout gbl_panelFilterLS = new GridBagLayout();
        gbl_panelFilterLS.columnWidths = new int[] {30, 149, 171, 29, 0};
        gbl_panelFilterLS.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelFilterLS.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panelFilterLS.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelFilterLS.setLayout(gbl_panelFilterLS);
        
        JLabel lblSigmaValue_1 = new JLabel("Sigma value");
        GridBagConstraints gbc_lblSigmaValue_1 = new GridBagConstraints();
        gbc_lblSigmaValue_1.anchor = GridBagConstraints.WEST;
        gbc_lblSigmaValue_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblSigmaValue_1.gridx = 1;
        gbc_lblSigmaValue_1.gridy = 1;
        panelFilterLS.add(lblSigmaValue_1, gbc_lblSigmaValue_1);
        
        formattedTextFieldSigmaLS = new JFormattedTextField(createFloatNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldSigmaLS = new GridBagConstraints();
        gbc_formattedTextFieldSigmaLS.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldSigmaLS.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldSigmaLS.gridx = 2;
        gbc_formattedTextFieldSigmaLS.gridy = 1;
        panelFilterLS.add(formattedTextFieldSigmaLS, gbc_formattedTextFieldSigmaLS);
        
        JLabel lblWidthpixels_1 = new JLabel("Width (pixels)");
        GridBagConstraints gbc_lblWidthpixels_1 = new GridBagConstraints();
        gbc_lblWidthpixels_1.anchor = GridBagConstraints.WEST;
        gbc_lblWidthpixels_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblWidthpixels_1.gridx = 1;
        gbc_lblWidthpixels_1.gridy = 2;
        panelFilterLS.add(lblWidthpixels_1, gbc_lblWidthpixels_1);
        
        formattedTextFieldWidthPxLS = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldWidthPxLS = new GridBagConstraints();
        gbc_formattedTextFieldWidthPxLS.insets = new Insets(0, 0, 0, 5);
        gbc_formattedTextFieldWidthPxLS.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldWidthPxLS.gridx = 2;
        gbc_formattedTextFieldWidthPxLS.gridy = 2;
        panelFilterLS.add(formattedTextFieldWidthPxLS, gbc_formattedTextFieldWidthPxLS);
        
        JPanel panelOptionsLS = new JPanel();
        panelOptionsLS.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Sparse Liu-Shen algorithm options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelOptionsLS = new GridBagConstraints();
        gbc_panelOptionsLS.fill = GridBagConstraints.BOTH;
        gbc_panelOptionsLS.gridx = 0;
        gbc_panelOptionsLS.gridy = 1;
        panelLS.add(panelOptionsLS, gbc_panelOptionsLS);
        GridBagLayout gbl_panelOptionsLS = new GridBagLayout();
        gbl_panelOptionsLS.columnWidths = new int[] {30, 0, 192, 30};
        gbl_panelOptionsLS.rowHeights = new int[]{0, 0, 0, 0, 0};
        gbl_panelOptionsLS.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panelOptionsLS.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelOptionsLS.setLayout(gbl_panelOptionsLS);
        
        JLabel lblNumberOfIterations_1 = new JLabel("Number of iterations");
        GridBagConstraints gbc_lblNumberOfIterations_1 = new GridBagConstraints();
        gbc_lblNumberOfIterations_1.anchor = GridBagConstraints.EAST;
        gbc_lblNumberOfIterations_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNumberOfIterations_1.gridx = 1;
        gbc_lblNumberOfIterations_1.gridy = 1;
        panelOptionsLS.add(lblNumberOfIterations_1, gbc_lblNumberOfIterations_1);
        
        formattedTextFieldIterationsLS = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldIterationsLS = new GridBagConstraints();
        gbc_formattedTextFieldIterationsLS.insets = new Insets(0, 0, 5, 0);
        gbc_formattedTextFieldIterationsLS.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldIterationsLS.gridx = 2;
        gbc_formattedTextFieldIterationsLS.gridy = 1;
        panelOptionsLS.add(formattedTextFieldIterationsLS, gbc_formattedTextFieldIterationsLS);
        
        JLabel lblVectorsWindowSize = new JLabel("Vectors window size");
        GridBagConstraints gbc_lblVectorsWindowSize = new GridBagConstraints();
        gbc_lblVectorsWindowSize.anchor = GridBagConstraints.EAST;
        gbc_lblVectorsWindowSize.insets = new Insets(0, 0, 5, 5);
        gbc_lblVectorsWindowSize.gridx = 1;
        gbc_lblVectorsWindowSize.gridy = 2;
        panelOptionsLS.add(lblVectorsWindowSize, gbc_lblVectorsWindowSize);
        
        formattedTextFieldVectorsSizeLS = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldVectorsSizeLS = new GridBagConstraints();
        gbc_formattedTextFieldVectorsSizeLS.insets = new Insets(0, 0, 5, 0);
        gbc_formattedTextFieldVectorsSizeLS.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldVectorsSizeLS.gridx = 2;
        gbc_formattedTextFieldVectorsSizeLS.gridy = 2;
        panelOptionsLS.add(formattedTextFieldVectorsSizeLS, gbc_formattedTextFieldVectorsSizeLS);
        
        JLabel lblLagrangeMultiplier = new JLabel("Lagrange multiplier");
        GridBagConstraints gbc_lblLagrangeMultiplier = new GridBagConstraints();
        gbc_lblLagrangeMultiplier.anchor = GridBagConstraints.EAST;
        gbc_lblLagrangeMultiplier.insets = new Insets(0, 0, 0, 5);
        gbc_lblLagrangeMultiplier.gridx = 1;
        gbc_lblLagrangeMultiplier.gridy = 3;
        panelOptionsLS.add(lblLagrangeMultiplier, gbc_lblLagrangeMultiplier);
        
        formattedTextFieldMultiplierLS = new JFormattedTextField(createMultiplierNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldMultiplierLS = new GridBagConstraints();
        gbc_formattedTextFieldMultiplierLS.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldMultiplierLS.gridx = 2;
        gbc_formattedTextFieldMultiplierLS.gridy = 3;
        panelOptionsLS.add(formattedTextFieldMultiplierLS, gbc_formattedTextFieldMultiplierLS);
        
        JPanel panelGenericOptions = new JPanel();
        panelGenericOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Generic options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelGenericOptions = new GridBagConstraints();
        gbc_panelGenericOptions.gridheight = 2;
        gbc_panelGenericOptions.gridwidth = 2;
        gbc_panelGenericOptions.fill = GridBagConstraints.BOTH;
        gbc_panelGenericOptions.gridx = 0;
        gbc_panelGenericOptions.gridy = 0;
        add(panelGenericOptions, gbc_panelGenericOptions);
        GridBagLayout gbl_panelGenericOptions = new GridBagLayout();
        gbl_panelGenericOptions.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_panelGenericOptions.rowHeights = new int[]{0, 54, 0};
        gbl_panelGenericOptions.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panelGenericOptions.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        panelGenericOptions.setLayout(gbl_panelGenericOptions);
        
        chckbxExportDenseVectors = new JCheckBox("Export dense vectors");
        GridBagConstraints gbc_chckbxExportDenseVectors = new GridBagConstraints();
        gbc_chckbxExportDenseVectors.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxExportDenseVectors.gridx = 1;
        gbc_chckbxExportDenseVectors.gridy = 0;
        panelGenericOptions.add(chckbxExportDenseVectors, gbc_chckbxExportDenseVectors);
        
        chkboxIgnorePivDisplacement = new JCheckBox("Ignore PIV displacement for dense Optical Flow");
        chkboxIgnorePivDisplacement.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                clickedIgnorePIVDisplacement();
            }
        });
        GridBagConstraints gbc_chkboxIgnorePivDisplacement = new GridBagConstraints();
        gbc_chkboxIgnorePivDisplacement.anchor = GridBagConstraints.WEST;
        gbc_chkboxIgnorePivDisplacement.insets = new Insets(0, 0, 5, 5);
        gbc_chkboxIgnorePivDisplacement.gridx = 3;
        gbc_chkboxIgnorePivDisplacement.gridy = 0;
        panelGenericOptions.add(chkboxIgnorePivDisplacement, gbc_chkboxIgnorePivDisplacement);
        
        panelIgnorePIV = new JPanel();
        GridBagConstraints gbc_panelIgnorePIV = new GridBagConstraints();
        gbc_panelIgnorePIV.insets = new Insets(0, 0, 0, 5);
        gbc_panelIgnorePIV.fill = GridBagConstraints.BOTH;
        gbc_panelIgnorePIV.gridx = 3;
        gbc_panelIgnorePIV.gridy = 1;
        panelGenericOptions.add(panelIgnorePIV, gbc_panelIgnorePIV);
        GridBagLayout gbl_panelIgnorePIV = new GridBagLayout();
        gbl_panelIgnorePIV.columnWidths = new int[]{66, 64, 93, 0, 0, 0};
        gbl_panelIgnorePIV.rowHeights = new int[]{0, 0};
        gbl_panelIgnorePIV.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelIgnorePIV.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panelIgnorePIV.setLayout(gbl_panelIgnorePIV);
        
        rdbtnPivU = new JRadioButton("U");
        rdbtnPivU.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clickedIgnorePIVMode();
            }
        });
        buttonGroupIgnorePIV.add(rdbtnPivU);
        GridBagConstraints gbc_rdbtnPivU = new GridBagConstraints();
        gbc_rdbtnPivU.anchor = GridBagConstraints.WEST;
        gbc_rdbtnPivU.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnPivU.gridx = 0;
        gbc_rdbtnPivU.gridy = 0;
        panelIgnorePIV.add(rdbtnPivU, gbc_rdbtnPivU);
        
        rdbtnPivV = new JRadioButton("V");
        rdbtnPivV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clickedIgnorePIVMode();
            }
        });
        buttonGroupIgnorePIV.add(rdbtnPivV);
        GridBagConstraints gbc_rdbtnPivV = new GridBagConstraints();
        gbc_rdbtnPivV.anchor = GridBagConstraints.WEST;
        gbc_rdbtnPivV.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnPivV.gridx = 1;
        gbc_rdbtnPivV.gridy = 0;
        panelIgnorePIV.add(rdbtnPivV, gbc_rdbtnPivV);
        
        rdbtnPivBothUV = new JRadioButton("U and V");
        rdbtnPivBothUV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                clickedIgnorePIVMode();
            }
        });
        rdbtnPivBothUV.setSelected(true);
        buttonGroupIgnorePIV.add(rdbtnPivBothUV);
        GridBagConstraints gbc_rdbtnPivBothU = new GridBagConstraints();
        gbc_rdbtnPivBothU.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnPivBothU.anchor = GridBagConstraints.WEST;
        gbc_rdbtnPivBothU.gridx = 2;
        gbc_rdbtnPivBothU.gridy = 0;
        panelIgnorePIV.add(rdbtnPivBothUV, gbc_rdbtnPivBothU);
        
        rdbtnIgnoreAuto = new JRadioButton("Auto");
        rdbtnIgnoreAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clickedIgnorePIVMode();
            }
        });
        buttonGroupIgnorePIV.add(rdbtnIgnoreAuto);
        GridBagConstraints gbc_rdbtnIgnoreAuto = new GridBagConstraints();
        gbc_rdbtnIgnoreAuto.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnIgnoreAuto.gridx = 3;
        gbc_rdbtnIgnoreAuto.gridy = 0;
        panelIgnorePIV.add(rdbtnIgnoreAuto, gbc_rdbtnIgnoreAuto);
        
        rdbtnIgnoreAutoSmall = new JRadioButton("Auto (Small norm)");
        rdbtnIgnoreAutoSmall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clickedIgnorePIVMode();
            }
        });
        buttonGroupIgnorePIV.add(rdbtnIgnoreAutoSmall);
        GridBagConstraints gbc_rdbtnIgnoreAutoSmall = new GridBagConstraints();
        gbc_rdbtnIgnoreAutoSmall.gridx = 4;
        gbc_rdbtnIgnoreAutoSmall.gridy = 0;
        panelIgnorePIV.add(rdbtnIgnoreAutoSmall, gbc_rdbtnIgnoreAutoSmall);
        initDataBindings();
        postInitDataB1indings();
    }

    private DefaultFormatter createFloatNumberFormatter() {
        NumberFormat format  = new DecimalFormat("0.00");
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(1);
        format.setGroupingUsed(false);     //Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.0f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }
    
    private DefaultFormatter createIntegerNumberFormatter() {
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

    private DefaultFormatter createMultiplierNumberFormatter() {
        NumberFormat format  = new DecimalFormat("#########0.00");
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setMaximumIntegerDigits(10);
        format.setGroupingUsed(false);     //Do not group digits and do not add group separator
        
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0.0f);
        formatter.setOverwriteMode(false); //Important to avoid strange auto-number rearrangements
        formatter.setAllowsInvalid(false);
        
        return formatter;
    }

    protected void clickedIgnorePIVDisplacement() {
        if (chkboxIgnorePivDisplacement.isSelected()) {
            DisabledPanel.enable(panelIgnorePIV);
        } else {
            DisabledPanel.disable(panelIgnorePIV);
        }
    }
    
    private void updateIgnorePIVMode() {
        switch (lsLkOptions.getIgnorePIVBaseDisplacementsMode()) {
        case IgnoreU:
            rdbtnPivU.setSelected(true);
            break;
        case IgnoreV:
            rdbtnPivV.setSelected(true);
            break;
        case IgnoreUV:
            rdbtnPivBothUV.setSelected(true);
            break;
        case IgnoreAuto:
            rdbtnIgnoreAuto.setSelected(true);
            break;
        case IgnoreAutoSmall:
            rdbtnIgnoreAutoSmall.setSelected(true);
            break;
        default:
            throw new UIException("PIV Configuration - Lucas-Kanade Sub-pixel mode", "Unknown mode for PIV Ignore base displacement");
        }
    }

    protected void clickedIgnorePIVMode() {
        if (rdbtnPivU.isSelected()) {
            lsLkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreU);
        } else if (rdbtnPivV.isSelected()) {
            lsLkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreV);
        } else if (rdbtnIgnoreAuto.isSelected()){
            lsLkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreAuto);
        } else if (rdbtnIgnoreAutoSmall.isSelected()) {
            lsLkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreAutoSmall);
        } else {
            lsLkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreUV);
        }
    }
    
    public void setAppContext(AppContextModel model, boolean openCL) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        if (openCL) {
            lsLkOptions = (SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL);
            lsLkClOptions = (SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL);
        } else {
            lsLkOptions = (SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LiuShenWithLucasKanade);
            chckbxExportDenseVectors.setEnabled(false);
        }
        
        filterSigmaBindingLK.unbind();
        filterSigmaBindingLK.setSourceObject(lsLkOptions);
        filterSigmaBindingLK.bind();
        
        filterWidthPxBindingLK.unbind();
        filterWidthPxBindingLK.setSourceObject(lsLkOptions);
        filterWidthPxBindingLK.bind();
        
        numberOfIterationsBindingLK.unbind();
        numberOfIterationsBindingLK.setSourceObject(lsLkOptions);
        numberOfIterationsBindingLK.bind();
        
        windowSizeBindingLK.unbind();
        windowSizeBindingLK.setSourceObject(lsLkOptions);
        windowSizeBindingLK.bind();

        //--------------------------------------------------------
        filterSigmaLsBinding.unbind();
        filterSigmaLsBinding.setSourceObject(lsLkOptions);
        filterSigmaLsBinding.bind();
        
        filterWidthPxLsBinding.unbind();
        filterWidthPxLsBinding.setSourceObject(lsLkOptions);
        filterWidthPxLsBinding.bind();
        
        numberOfIterationsLsBinding.unbind();
        numberOfIterationsLsBinding.setSourceObject(lsLkOptions);
        numberOfIterationsLsBinding.bind();
        
        vectorsWindowSizeLsBinding.unbind();
        vectorsWindowSizeLsBinding.setSourceObject(lsLkOptions);
        vectorsWindowSizeLsBinding.bind();
        
        multiplierLsBinding.unbind();
        multiplierLsBinding.setSourceObject(lsLkOptions);
        multiplierLsBinding.bind();
       
        ignorePIVBaseDisplacementsBinding.unbind();
        ignorePIVBaseDisplacementsBinding.setSourceObject(lsLkOptions);
        ignorePIVBaseDisplacementsBinding.bind();
        
        updateIgnorePIVMode();
        
        if (openCL) {
            denseExportBinding.unbind();
            denseExportBinding.setSourceObject(lsLkClOptions);
            denseExportBinding.bind();
        }
                
    }
    
    private void postInitDataB1indings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderFilterSigmaLk = new ErrorBorderForComponent(formattedTextFieldFilterSigmaLK);
        formattedTextFieldFilterSigmaLK.setBorder(borderFilterSigmaLk);
        filterSigmaBindingLK.addBindingListener(borderFilterSigmaLk);
        errorBorders.add(borderFilterSigmaLk);

        ErrorBorderForComponent borderFilterWidthPxLk = new ErrorBorderForComponent(formattedTextFieldFilterWidthLK);
        formattedTextFieldFilterWidthLK.setBorder(borderFilterWidthPxLk);
        filterWidthPxBindingLK.addBindingListener(borderFilterWidthPxLk);
        errorBorders.add(borderFilterWidthPxLk);
        
        ErrorBorderForComponent borderNumberOfIterationsLk = new ErrorBorderForComponent(formattedTextFieldIterationsLK);
        formattedTextFieldIterationsLK.setBorder(borderNumberOfIterationsLk);
        numberOfIterationsBindingLK.addBindingListener(borderNumberOfIterationsLk);
        errorBorders.add(borderNumberOfIterationsLk);

        ErrorBorderForComponent borderWindowSizeLk = new ErrorBorderForComponent(formattedTextFieldWindowSizeLK);
        formattedTextFieldWindowSizeLK.setBorder(borderWindowSizeLk);
        windowSizeBindingLK.addBindingListener(borderWindowSizeLk);
        errorBorders.add(borderWindowSizeLk);
        
        //----------------------------------------------- Liu-Shen ----------------------------------------------

        ErrorBorderForComponent borderFilterSigmaLs = new ErrorBorderForComponent(formattedTextFieldSigmaLS);
        formattedTextFieldSigmaLS.setBorder(borderFilterSigmaLs);
        filterSigmaLsBinding.addBindingListener(borderFilterSigmaLs);
        errorBorders.add(borderFilterSigmaLs);

        ErrorBorderForComponent borderFilterWidthPxLs = new ErrorBorderForComponent(formattedTextFieldWidthPxLS);
        formattedTextFieldWidthPxLS.setBorder(borderFilterWidthPxLs);
        filterWidthPxLsBinding.addBindingListener(borderFilterWidthPxLs);
        errorBorders.add(borderFilterWidthPxLs);
        
        ErrorBorderForComponent borderNumberOfIterationsLs = new ErrorBorderForComponent(formattedTextFieldIterationsLS);
        formattedTextFieldIterationsLS.setBorder(borderNumberOfIterationsLs);
        numberOfIterationsLsBinding.addBindingListener(borderNumberOfIterationsLs);
        errorBorders.add(borderNumberOfIterationsLs);

        ErrorBorderForComponent borderVectorsWindowSizeLs = new ErrorBorderForComponent(formattedTextFieldVectorsSizeLS);
        formattedTextFieldVectorsSizeLS.setBorder(borderVectorsWindowSizeLs);
        vectorsWindowSizeLsBinding.addBindingListener(borderVectorsWindowSizeLs);
        errorBorders.add(borderVectorsWindowSizeLs);

        ErrorBorderForComponent borderMultiplierLs = new ErrorBorderForComponent(formattedTextFieldMultiplierLS);
        formattedTextFieldMultiplierLS.setBorder(borderMultiplierLs);
        multiplierLsBinding.addBindingListener(borderMultiplierLs);
        errorBorders.add(borderMultiplierLs);

        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        FloatRangeValidator filterSigmaValidatorLk = (FloatRangeValidator)filterSigmaBindingLK.getValidator();
        filterSigmaValidatorLk.setMinAndMax(0.0f, 6.0f);
        NullGenericFloatConverter filterSigmaConverterLk = (NullGenericFloatConverter)filterSigmaBindingLK.getConverter();
        filterSigmaConverterLk.setValidatorOnConvertForward(filterSigmaValidatorLk);
        filterSigmaConverterLk.addStatusListener(borderFilterSigmaLk);
        
        IntegerRangeValidator filterWidthValidatorLk = (IntegerRangeValidator)filterWidthPxBindingLK.getValidator();
        filterWidthValidatorLk.setMinAndMax(3, 15);
        filterWidthValidatorLk.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter filterWidthPxConverterLk = (NullGenericIntegerConverter)filterWidthPxBindingLK.getConverter();
        filterWidthPxConverterLk.setValidatorOnConvertForward(filterWidthValidatorLk);
        filterWidthPxConverterLk.addStatusListener(borderFilterWidthPxLk);
        
        IntegerRangeValidator numberOfIterationsValidatorLk = (IntegerRangeValidator)numberOfIterationsBindingLK.getValidator();
        numberOfIterationsValidatorLk.setMinAndMax(1, 15);        
        NullGenericIntegerConverter numberOfIterationsConverterLk = (NullGenericIntegerConverter)numberOfIterationsBindingLK.getConverter();
        numberOfIterationsConverterLk.setValidatorOnConvertForward(numberOfIterationsValidatorLk);
        numberOfIterationsConverterLk.addStatusListener(borderNumberOfIterationsLk);
        
        IntegerRangeValidator windowSizeValidatorLk = (IntegerRangeValidator)windowSizeBindingLK.getValidator();
        windowSizeValidatorLk.setMinAndMax(3, 31);
        windowSizeValidatorLk.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter windowSizeConverterLk = (NullGenericIntegerConverter)windowSizeBindingLK.getConverter();
        windowSizeConverterLk.setValidatorOnConvertForward(windowSizeValidatorLk);
        windowSizeConverterLk.addStatusListener(borderWindowSizeLk);

        //----------------------------------------------------------------------------------------------------------------------------------
        FloatRangeValidator filterSigmaValidatorLs = (FloatRangeValidator)filterSigmaLsBinding.getValidator();
        filterSigmaValidatorLs.setMinAndMax(0.0f, 6.0f);
        NullGenericFloatConverter filterSigmaConverterLs = (NullGenericFloatConverter)filterSigmaLsBinding.getConverter();
        filterSigmaConverterLs.setValidatorOnConvertForward(filterSigmaValidatorLs);
        filterSigmaConverterLs.addStatusListener(borderFilterSigmaLs);
        
        IntegerRangeValidator filterWidthValidatorLs = (IntegerRangeValidator)filterWidthPxLsBinding.getValidator();
        filterWidthValidatorLs.setMinAndMax(3, 15);
        filterWidthValidatorLs.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter filterWidthPxConverterLs = (NullGenericIntegerConverter)filterWidthPxLsBinding.getConverter();
        filterWidthPxConverterLs.setValidatorOnConvertForward(filterWidthValidatorLs);
        filterWidthPxConverterLs.addStatusListener(borderFilterWidthPxLs);
        
        IntegerRangeValidator numberOfIterationsValidatorLs = (IntegerRangeValidator)numberOfIterationsLsBinding.getValidator();
        numberOfIterationsValidatorLs.setMinAndMax(1, 120);        
        NullGenericIntegerConverter numberOfIterationsConverterLs = (NullGenericIntegerConverter)numberOfIterationsLsBinding.getConverter();
        numberOfIterationsConverterLs.setValidatorOnConvertForward(numberOfIterationsValidatorLs);
        numberOfIterationsConverterLs.addStatusListener(borderNumberOfIterationsLs);
        
        IntegerRangeValidator vectorsWindowSizeValidatorLs = (IntegerRangeValidator)vectorsWindowSizeLsBinding.getValidator();
        vectorsWindowSizeValidatorLs.setMinAndMax(3, 31);
        vectorsWindowSizeValidatorLs.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter vectorsWindowSizeConverterLs = (NullGenericIntegerConverter)vectorsWindowSizeLsBinding.getConverter();
        vectorsWindowSizeConverterLs.setValidatorOnConvertForward(vectorsWindowSizeValidatorLs);
        vectorsWindowSizeConverterLs.addStatusListener(borderVectorsWindowSizeLs);

        FloatRangeValidator multiplierValidatorLs = (FloatRangeValidator)multiplierLsBinding.getValidator();
        multiplierValidatorLs.setMinAndMax(0.0f, 1e8f);
        NullGenericFloatConverter multiplierConverterLs = (NullGenericFloatConverter)multiplierLsBinding.getConverter();
        multiplierConverterLs.setValidatorOnConvertForward(multiplierValidatorLs);
        multiplierConverterLs.addStatusListener(borderMultiplierLs);
    }

    public void setParentDialog(JDialog _dialog) {
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    protected JFormattedTextField getFormattedTextFieldIterations() {
        return formattedTextFieldIterationsLK;
    }
    
    protected JFormattedTextField getFormattedTextFieldWindowSize() {
        return formattedTextFieldWindowSizeLK;
    }
    
    protected JFormattedTextField getFormattedTextFieldFilterSigma() {
        return formattedTextFieldFilterSigmaLK;
    }
    
    protected JFormattedTextField getFormattedTextFieldFilterWidth() {
        return formattedTextFieldFilterWidthLK;
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }

    @Override
    public void dispose() {
        
    }
    
    protected JCheckBox getChkboxIgnorePivDisplacement() {
        return chkboxIgnorePivDisplacement;
    }
    
    protected JCheckBox getChckbxExportDenseVectors() {
        return chckbxExportDenseVectors;
    }
    
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float> subPixelInterpolationOptionsLucasKanadeModelBeanProperty = BeanProperty.create("filterSigmaLK");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        filterSigmaBindingLK = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty, formattedTextFieldFilterSigmaLK, jFormattedTextFieldBeanProperty, "filterSigmaBindingLK");
        filterSigmaBindingLK.setConverter(new NullGenericFloatConverter());
        filterSigmaBindingLK.setValidator(new FloatRangeValidator());
        filterSigmaBindingLK.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_1 = BeanProperty.create("filterWidthPxLK");
        filterWidthPxBindingLK = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_1, formattedTextFieldFilterWidthLK, jFormattedTextFieldBeanProperty, "filterWidthPxBindingLK");
        filterWidthPxBindingLK.setConverter(new NullGenericIntegerConverter());
        filterWidthPxBindingLK.setValidator(new IntegerRangeValidator());
        filterWidthPxBindingLK.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_2 = BeanProperty.create("numberOfIterationsLK");
        numberOfIterationsBindingLK = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_2, formattedTextFieldIterationsLK, jFormattedTextFieldBeanProperty, "numberOfIterationsBindingLK");
        numberOfIterationsBindingLK.setConverter(new NullGenericIntegerConverter());
        numberOfIterationsBindingLK.setValidator(new IntegerRangeValidator());
        numberOfIterationsBindingLK.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_3 = BeanProperty.create("windowSizeLK");
        windowSizeBindingLK = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_3, formattedTextFieldWindowSizeLK, jFormattedTextFieldBeanProperty, "windowSizeBindingLK");
        windowSizeBindingLK.setConverter(new NullGenericIntegerConverter());
        windowSizeBindingLK.setValidator(new IntegerRangeValidator());
        windowSizeBindingLK.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty = BeanProperty.create("filterSigmaLS");
        filterSigmaLsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty, formattedTextFieldSigmaLS, jFormattedTextFieldBeanProperty, "filterSigmaLsBinding");
        filterSigmaLsBinding.setConverter(new NullGenericFloatConverter());
        filterSigmaLsBinding.setValidator(new FloatRangeValidator());
        filterSigmaLsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_1 = BeanProperty.create("filterWidthPxLS");
        filterWidthPxLsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_1, formattedTextFieldWidthPxLS, jFormattedTextFieldBeanProperty, "widthPxLsBinding");
        filterWidthPxLsBinding.setConverter(new NullGenericIntegerConverter());
        filterWidthPxLsBinding.setValidator(new IntegerRangeValidator());
        filterWidthPxLsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_2 = BeanProperty.create("numberOfIterationsLS");
        numberOfIterationsLsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_2, formattedTextFieldIterationsLS, jFormattedTextFieldBeanProperty, "numberOfIterationsLsBinding");
        numberOfIterationsLsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfIterationsLsBinding.setValidator(new IntegerRangeValidator());
        numberOfIterationsLsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Integer> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_3 = BeanProperty.create("vectorsWindowSizeLS");
        vectorsWindowSizeLsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_3, formattedTextFieldVectorsSizeLS, jFormattedTextFieldBeanProperty, "vectorsWindowSizeLsBinding");
        vectorsWindowSizeLsBinding.setConverter(new NullGenericIntegerConverter());
        vectorsWindowSizeLsBinding.setValidator(new IntegerRangeValidator());
        vectorsWindowSizeLsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Float> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_4 = BeanProperty.create("multiplierLS");
        multiplierLsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_4, formattedTextFieldMultiplierLS, jFormattedTextFieldBeanProperty, "multiplierLsBinding");
        multiplierLsBinding.setConverter(new NullGenericFloatConverter());
        multiplierLsBinding.setValidator(new FloatRangeValidator());
        multiplierLsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel, Boolean> subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModelBeanProperty = BeanProperty.create("denseExport");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        denseExportBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkClOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModelBeanProperty, chckbxExportDenseVectors, jCheckBoxBeanProperty, "denseExportBinding");
        denseExportBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel, Boolean> subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_5 = BeanProperty.create("ignorePIVBaseDisplacements");
        ignorePIVBaseDisplacementsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lsLkOptions, subPixelInterpolationOptionsLiuShenWithLucasKanadeModelBeanProperty_5, chkboxIgnorePivDisplacement, jCheckBoxBeanProperty, "ignorePIVBaseDisplacementsBinding");
        ignorePIVBaseDisplacementsBinding.bind();
    }
    protected JRadioButton getRdbtnPivU() {
        return rdbtnPivU;
    }
    protected JRadioButton getRdbtnPivV() {
        return rdbtnPivV;
    }
    protected JRadioButton getRdbtnPivBothUV() {
        return rdbtnPivBothUV;
    }
    protected JPanel getPanelIgnorePIV() {
        return panelIgnorePIV;
    }
    protected JRadioButton getRdbtnIgnoreAuto() {
        return rdbtnIgnoreAuto;
    }
    protected JRadioButton getRdbtnIgnoreAutoSmall() {
        return rdbtnIgnoreAutoSmall;
    }
}
