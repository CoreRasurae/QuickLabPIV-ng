// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
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
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLucasKanadeModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsLucasKanadeOpenCLModel;
import pt.quickLabPIV.ui.validators.FloatRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator;
import pt.quickLabPIV.ui.validators.IntegerRangeValidator.RangeTypeEnum;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;
import javax.swing.JCheckBox;

public class SubPixelLucasKanadeOptionsPanel extends JPanel implements IPanelWithErrorBorders {
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeModel, Boolean, JCheckBox, Boolean> ignorePIVDisplacementsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeOpenCLModel, Boolean, JCheckBox, Boolean> denseExportBinding;
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeModel, Integer, JFormattedTextField, Object> windowSizeBinding;
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeModel, Integer, JFormattedTextField, Object> numberOfIterationsBinding;
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeModel, Integer, JFormattedTextField, Object> filterWidthPxBinding;
    private AutoBinding<SubPixelInterpolationOptionsLucasKanadeModel, Float, JFormattedTextField, Object> filterSigmaBinding;

    /**
     * 
     */
    private static final long serialVersionUID = 7298248035052385836L;

    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(4);
    private JRadioButton rdbtnWarpingMode;
    private JRadioButton rdbtnAverageMode;
    private JFormattedTextField formattedTextFieldIterations;
    private JFormattedTextField formattedTextFieldWindowSize;
    private JFormattedTextField formattedTextFieldFilterSigma;
    private JFormattedTextField formattedTextFieldFilterWidth;
    
    private AppContextModel appContext;
    private SubPixelInterpolationOptionsLucasKanadeModel lkOptions;
    private SubPixelInterpolationOptionsLucasKanadeOpenCLModel lkClOptions;
    private final ButtonGroup buttonGroupWarping = new ButtonGroup();
    private JCheckBox chckbxExportDenseVectors;
    private JCheckBox chkboxIgnorePIV;
    private JPanel panelIgnorePIV;
    private JRadioButton rdbtnPivU;
    private JRadioButton rdbtnPivV;
    private JRadioButton rdbtnPivBothUV;
    private final ButtonGroup buttonGroupIgnorePIV = new ButtonGroup();
    private JRadioButton rdbtnIgnoreAuto;
    private JRadioButton rdbtnIgnoreAutoSmall;
    
    /**
     * Create the panel.
     */
    public SubPixelLucasKanadeOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{662, 0};
        gridBagLayout.rowHeights = new int[]{46, 42, 81, 127, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JPanel panelFilterOptions = new JPanel();
        panelFilterOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Gaussian filter options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelFilterOptions = new GridBagConstraints();
        gbc_panelFilterOptions.insets = new Insets(0, 0, 5, 0);
        gbc_panelFilterOptions.fill = GridBagConstraints.BOTH;
        gbc_panelFilterOptions.gridx = 0;
        gbc_panelFilterOptions.gridy = 2;
        add(panelFilterOptions, gbc_panelFilterOptions);
        GridBagLayout gbl_panelFilterOptions = new GridBagLayout();
        gbl_panelFilterOptions.columnWidths = new int[]{0, 149, 0, 0, 0};
        gbl_panelFilterOptions.rowHeights = new int[]{0, 0, 0};
        gbl_panelFilterOptions.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panelFilterOptions.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        panelFilterOptions.setLayout(gbl_panelFilterOptions);
        
        JLabel lblSigmaValue = new JLabel("Sigma value");
        GridBagConstraints gbc_lblSigmaValue = new GridBagConstraints();
        gbc_lblSigmaValue.anchor = GridBagConstraints.WEST;
        gbc_lblSigmaValue.insets = new Insets(0, 0, 5, 5);
        gbc_lblSigmaValue.gridx = 1;
        gbc_lblSigmaValue.gridy = 0;
        panelFilterOptions.add(lblSigmaValue, gbc_lblSigmaValue);
        
        formattedTextFieldFilterSigma = new JFormattedTextField(createFloatNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldFilterSigma = new GridBagConstraints();
        gbc_formattedTextFieldFilterSigma.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldFilterSigma.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldFilterSigma.gridx = 2;
        gbc_formattedTextFieldFilterSigma.gridy = 0;
        panelFilterOptions.add(formattedTextFieldFilterSigma, gbc_formattedTextFieldFilterSigma);
        
        JLabel lblWidthpixels = new JLabel("Width (pixels)");
        GridBagConstraints gbc_lblWidthpixels = new GridBagConstraints();
        gbc_lblWidthpixels.anchor = GridBagConstraints.WEST;
        gbc_lblWidthpixels.insets = new Insets(0, 0, 0, 5);
        gbc_lblWidthpixels.gridx = 1;
        gbc_lblWidthpixels.gridy = 1;
        panelFilterOptions.add(lblWidthpixels, gbc_lblWidthpixels);
        
        formattedTextFieldFilterWidth = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldFilterWidth = new GridBagConstraints();
        gbc_formattedTextFieldFilterWidth.insets = new Insets(0, 0, 0, 5);
        gbc_formattedTextFieldFilterWidth.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldFilterWidth.gridx = 2;
        gbc_formattedTextFieldFilterWidth.gridy = 1;
        panelFilterOptions.add(formattedTextFieldFilterWidth, gbc_formattedTextFieldFilterWidth);
        
        JPanel panelLucasKanadeOptions = new JPanel();
        panelLucasKanadeOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Sparse Lucas-Kanade algorithm options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelLucasKanadeOptions = new GridBagConstraints();
        gbc_panelLucasKanadeOptions.fill = GridBagConstraints.BOTH;
        gbc_panelLucasKanadeOptions.gridx = 0;
        gbc_panelLucasKanadeOptions.gridy = 3;
        add(panelLucasKanadeOptions, gbc_panelLucasKanadeOptions);
        GridBagLayout gbl_panelLucasKanadeOptions = new GridBagLayout();
        gbl_panelLucasKanadeOptions.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panelLucasKanadeOptions.rowHeights = new int[]{0, 0, 0, 0, 0};
        gbl_panelLucasKanadeOptions.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panelLucasKanadeOptions.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelLucasKanadeOptions.setLayout(gbl_panelLucasKanadeOptions);
        
        JLabel lblMode = new JLabel("Mode");
        GridBagConstraints gbc_lblMode = new GridBagConstraints();
        gbc_lblMode.anchor = GridBagConstraints.WEST;
        gbc_lblMode.gridheight = 2;
        gbc_lblMode.insets = new Insets(0, 0, 5, 5);
        gbc_lblMode.gridx = 1;
        gbc_lblMode.gridy = 0;
        panelLucasKanadeOptions.add(lblMode, gbc_lblMode);
        
        rdbtnWarpingMode = new JRadioButton("Warping at center of IA mode");
        buttonGroupWarping.add(rdbtnWarpingMode);
        rdbtnWarpingMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateSelectedMode(false);
            }
        });
        rdbtnWarpingMode.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_rdbtnWarpingMode = new GridBagConstraints();
        gbc_rdbtnWarpingMode.anchor = GridBagConstraints.WEST;
        gbc_rdbtnWarpingMode.insets = new Insets(0, 0, 5, 5);
        gbc_rdbtnWarpingMode.gridx = 2;
        gbc_rdbtnWarpingMode.gridy = 0;
        panelLucasKanadeOptions.add(rdbtnWarpingMode, gbc_rdbtnWarpingMode);
        
        rdbtnAverageMode = new JRadioButton("Average of four pixels around center of IA mode");
        buttonGroupWarping.add(rdbtnAverageMode);
        rdbtnAverageMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateSelectedMode(true);
            }
        });
        GridBagConstraints gbc_rdbtnAverageMode = new GridBagConstraints();
        gbc_rdbtnAverageMode.insets = new Insets(0, 0, 5, 5);
        gbc_rdbtnAverageMode.anchor = GridBagConstraints.WEST;
        gbc_rdbtnAverageMode.gridx = 2;
        gbc_rdbtnAverageMode.gridy = 1;
        panelLucasKanadeOptions.add(rdbtnAverageMode, gbc_rdbtnAverageMode);
        
        JLabel lblNumberOfIterations = new JLabel("Number of iterations");
        GridBagConstraints gbc_lblNumberOfIterations = new GridBagConstraints();
        gbc_lblNumberOfIterations.anchor = GridBagConstraints.EAST;
        gbc_lblNumberOfIterations.insets = new Insets(0, 0, 5, 5);
        gbc_lblNumberOfIterations.gridx = 1;
        gbc_lblNumberOfIterations.gridy = 2;
        panelLucasKanadeOptions.add(lblNumberOfIterations, gbc_lblNumberOfIterations);
        
        formattedTextFieldIterations = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldIterations = new GridBagConstraints();
        gbc_formattedTextFieldIterations.insets = new Insets(0, 0, 5, 5);
        gbc_formattedTextFieldIterations.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldIterations.gridx = 2;
        gbc_formattedTextFieldIterations.gridy = 2;
        panelLucasKanadeOptions.add(formattedTextFieldIterations, gbc_formattedTextFieldIterations);
        
        JLabel lblWindowSize = new JLabel("Window size");
        GridBagConstraints gbc_lblWindowSize = new GridBagConstraints();
        gbc_lblWindowSize.anchor = GridBagConstraints.EAST;
        gbc_lblWindowSize.insets = new Insets(0, 0, 0, 5);
        gbc_lblWindowSize.gridx = 1;
        gbc_lblWindowSize.gridy = 3;
        panelLucasKanadeOptions.add(lblWindowSize, gbc_lblWindowSize);
        
        formattedTextFieldWindowSize = new JFormattedTextField(createIntegerNumberFormatter());
        GridBagConstraints gbc_formattedTextFieldWindowSize = new GridBagConstraints();
        gbc_formattedTextFieldWindowSize.insets = new Insets(0, 0, 0, 5);
        gbc_formattedTextFieldWindowSize.fill = GridBagConstraints.HORIZONTAL;
        gbc_formattedTextFieldWindowSize.gridx = 2;
        gbc_formattedTextFieldWindowSize.gridy = 3;
        panelLucasKanadeOptions.add(formattedTextFieldWindowSize, gbc_formattedTextFieldWindowSize);
        
        JPanel panelGenericOptions = new JPanel();
        panelGenericOptions.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Generic options", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
        GridBagConstraints gbc_panelGenericOptions = new GridBagConstraints();
        gbc_panelGenericOptions.insets = new Insets(0, 0, 5, 0);
        gbc_panelGenericOptions.gridheight = 2;
        gbc_panelGenericOptions.fill = GridBagConstraints.BOTH;
        gbc_panelGenericOptions.gridx = 0;
        gbc_panelGenericOptions.gridy = 0;
        add(panelGenericOptions, gbc_panelGenericOptions);
        GridBagLayout gbl_panelGenericOptions = new GridBagLayout();
        gbl_panelGenericOptions.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_panelGenericOptions.rowHeights = new int[]{0, 0, 0};
        gbl_panelGenericOptions.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panelGenericOptions.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        panelGenericOptions.setLayout(gbl_panelGenericOptions);
        
        chckbxExportDenseVectors = new JCheckBox("Export dense vectors");
        GridBagConstraints gbc_chckbxExportDenseVectors = new GridBagConstraints();
        gbc_chckbxExportDenseVectors.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxExportDenseVectors.gridx = 1;
        gbc_chckbxExportDenseVectors.gridy = 0;
        panelGenericOptions.add(chckbxExportDenseVectors, gbc_chckbxExportDenseVectors);
        
        chkboxIgnorePIV = new JCheckBox("Ignore PIV displacement for dense Optical Flow");
        chkboxIgnorePIV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clickedIgnorePIV();
            }
        });
        GridBagConstraints gbc_chkboxIgnoreDensePIV = new GridBagConstraints();
        gbc_chkboxIgnoreDensePIV.anchor = GridBagConstraints.WEST;
        gbc_chkboxIgnoreDensePIV.insets = new Insets(0, 0, 5, 5);
        gbc_chkboxIgnoreDensePIV.gridx = 3;
        gbc_chkboxIgnoreDensePIV.gridy = 0;
        panelGenericOptions.add(chkboxIgnorePIV, gbc_chkboxIgnoreDensePIV);
        
        panelIgnorePIV = new JPanel();
        panelIgnorePIV.setBorder(new LineBorder(Color.GRAY, 1, true));
        GridBagConstraints gbc_panelIgnorePIV = new GridBagConstraints();
        gbc_panelIgnorePIV.insets = new Insets(0, 0, 0, 5);
        gbc_panelIgnorePIV.fill = GridBagConstraints.BOTH;
        gbc_panelIgnorePIV.gridx = 3;
        gbc_panelIgnorePIV.gridy = 1;
        panelGenericOptions.add(panelIgnorePIV, gbc_panelIgnorePIV);
        GridBagLayout gbl_panelIgnorePIV = new GridBagLayout();
        gbl_panelIgnorePIV.columnWidths = new int[]{53, 51, 0, 0, 0, 0};
        gbl_panelIgnorePIV.rowHeights = new int[]{23, 0};
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
        rdbtnPivU.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_rdbtnPivU = new GridBagConstraints();
        gbc_rdbtnPivU.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnPivU.anchor = GridBagConstraints.NORTHWEST;
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
        GridBagConstraints gbc_rdbtnPivBothUV = new GridBagConstraints();
        gbc_rdbtnPivBothUV.insets = new Insets(0, 0, 0, 5);
        gbc_rdbtnPivBothUV.anchor = GridBagConstraints.WEST;
        gbc_rdbtnPivBothUV.gridx = 2;
        gbc_rdbtnPivBothUV.gridy = 0;
        panelIgnorePIV.add(rdbtnPivBothUV, gbc_rdbtnPivBothUV);
        
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

    protected void clickedIgnorePIV() {
        if (chkboxIgnorePIV.isSelected()) {
            DisabledPanel.enable(panelIgnorePIV);
        } else {
            DisabledPanel.disable(panelIgnorePIV);
        }
    }

    private void updateIgnorePIVMode() {
        switch (lkOptions.getIgnorePIVBaseDisplacementsMode()) {
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
            lkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreU);
        } else if (rdbtnPivV.isSelected()) {
            lkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreV);
        } else if (rdbtnIgnoreAuto.isSelected()){
            lkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreAuto);
        } else if (rdbtnIgnoreAutoSmall.isSelected()) {
            lkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreAutoSmall);
        } else {
            lkOptions.setIgnorePIVBaseDisplacementsMode(IgnorePIVBaseDisplacementsModeEnum.IgnoreUV);
        }
    }
    
    protected void updateSelectedMode(boolean average) {
        lkOptions.setAverageOfFourPixels(average);
    }

    public void setAppContext(AppContextModel model, boolean isOpenCL) {
        appContext = model;
        PIVConfigurationModel pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        if (isOpenCL) {
            lkOptions = (SubPixelInterpolationOptionsLucasKanadeModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LucasKanadeOpenCL);
            lkClOptions = (SubPixelInterpolationOptionsLucasKanadeOpenCLModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LucasKanadeOpenCL);
            chckbxExportDenseVectors.setEnabled(true);
        } else {
            lkOptions = (SubPixelInterpolationOptionsLucasKanadeModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.LucasKanade);
            chckbxExportDenseVectors.setEnabled(false);
        }
        
        filterSigmaBinding.unbind();
        filterSigmaBinding.setSourceObject(lkOptions);
        filterSigmaBinding.bind();
        
        filterWidthPxBinding.unbind();
        filterWidthPxBinding.setSourceObject(lkOptions);
        filterWidthPxBinding.bind();
        
        if (lkOptions.isAverageOfFourPixels()) {
            rdbtnAverageMode.setSelected(true);
        } else {
            rdbtnWarpingMode.setSelected(true);
        }
        
        numberOfIterationsBinding.unbind();
        numberOfIterationsBinding.setSourceObject(lkOptions);
        numberOfIterationsBinding.bind();
        
        windowSizeBinding.unbind();
        windowSizeBinding.setSourceObject(lkOptions);
        windowSizeBinding.bind();

        ignorePIVDisplacementsBinding.unbind();
        ignorePIVDisplacementsBinding.setSourceObject(lkOptions);
        ignorePIVDisplacementsBinding.bind();
        
        updateIgnorePIVMode();
        
        if (isOpenCL) {
            denseExportBinding.unbind();
            denseExportBinding.setSourceObject(lkClOptions);
            denseExportBinding.bind();
        }
    }
    
    private void postInitDataB1indings() {
        //Error borders attachment and registration
        ErrorBorderForComponent borderFilterSigma = new ErrorBorderForComponent(formattedTextFieldFilterSigma);
        formattedTextFieldFilterSigma.setBorder(borderFilterSigma);
        filterSigmaBinding.addBindingListener(borderFilterSigma);
        errorBorders.add(borderFilterSigma);

        ErrorBorderForComponent borderFilterWidthPx = new ErrorBorderForComponent(formattedTextFieldFilterWidth);
        formattedTextFieldFilterWidth.setBorder(borderFilterWidthPx);
        filterWidthPxBinding.addBindingListener(borderFilterWidthPx);
        errorBorders.add(borderFilterWidthPx);
        
        ErrorBorderForComponent borderNumberOfIterations = new ErrorBorderForComponent(formattedTextFieldIterations);
        formattedTextFieldIterations.setBorder(borderNumberOfIterations);
        numberOfIterationsBinding.addBindingListener(borderNumberOfIterations);
        errorBorders.add(borderNumberOfIterations);

        ErrorBorderForComponent borderWindowSize = new ErrorBorderForComponent(formattedTextFieldWindowSize);
        formattedTextFieldWindowSize.setBorder(borderWindowSize);
        windowSizeBinding.addBindingListener(borderWindowSize);
        errorBorders.add(borderWindowSize);

        //
        //////////////////////////
        //Bindings
        //////////////////////////        
        //
        //Error handling
        //
        FloatRangeValidator filterSigmaValidator = (FloatRangeValidator)filterSigmaBinding.getValidator();
        filterSigmaValidator.setMinAndMax(0.0f, 6.0f);
        NullGenericFloatConverter filterSigmaConverter = (NullGenericFloatConverter)filterSigmaBinding.getConverter();
        filterSigmaConverter.setValidatorOnConvertForward(filterSigmaValidator);
        filterSigmaConverter.addStatusListener(borderFilterSigma);
        
        IntegerRangeValidator filterWidthValidator = (IntegerRangeValidator)filterWidthPxBinding.getValidator();
        filterWidthValidator.setMinAndMax(3, 15);
        filterWidthValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter filterWidthPxConverter = (NullGenericIntegerConverter)filterWidthPxBinding.getConverter();
        filterWidthPxConverter.setValidatorOnConvertForward(filterWidthValidator);
        filterWidthPxConverter.addStatusListener(borderFilterWidthPx);
        
        IntegerRangeValidator numberOfIterationsValidator = (IntegerRangeValidator)numberOfIterationsBinding.getValidator();
        numberOfIterationsValidator.setMinAndMax(1, 15);        
        NullGenericIntegerConverter numberOfIterationsConverter = (NullGenericIntegerConverter)numberOfIterationsBinding.getConverter();
        numberOfIterationsConverter.setValidatorOnConvertForward(numberOfIterationsValidator);
        numberOfIterationsConverter.addStatusListener(borderNumberOfIterations);
        
        IntegerRangeValidator windowSizeValidator = (IntegerRangeValidator)windowSizeBinding.getValidator();
        windowSizeValidator.setMinAndMax(3, 31);
        windowSizeValidator.setRangeType(RangeTypeEnum.ODD);
        NullGenericIntegerConverter windowSizeConverter = (NullGenericIntegerConverter)windowSizeBinding.getConverter();
        windowSizeConverter.setValidatorOnConvertForward(windowSizeValidator);
        windowSizeConverter.addStatusListener(borderWindowSize);
    }

    public void setParentDialog(JDialog _dialog) {
    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    protected JRadioButton getRdbtnWarpingMode() {
        return rdbtnWarpingMode;
    }
    protected JRadioButton getRdbtnAverageMode() {
        return rdbtnAverageMode;
    }
    protected JFormattedTextField getFormattedTextFieldIterations() {
        return formattedTextFieldIterations;
    }
    protected JFormattedTextField getFormattedTextFieldWindowSize() {
        return formattedTextFieldWindowSize;
    }
    protected JFormattedTextField getFormattedTextFieldFilterSigma() {
        return formattedTextFieldFilterSigma;
    }
    protected JFormattedTextField getFormattedTextFieldFilterWidth() {
        return formattedTextFieldFilterWidth;
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }

    @Override
    public void dispose() {
        
    }
    protected JCheckBox getChckbxExportDenseVectors() {
        return chckbxExportDenseVectors;
    }
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeModel, Float> subPixelInterpolationOptionsLucasKanadeModelBeanProperty = BeanProperty.create("filterSigma");
        BeanProperty<JFormattedTextField, Object> jFormattedTextFieldBeanProperty = BeanProperty.create("value");
        filterSigmaBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty, formattedTextFieldFilterSigma, jFormattedTextFieldBeanProperty, "filterSigmaBinding");
        filterSigmaBinding.setConverter(new NullGenericFloatConverter());
        filterSigmaBinding.setValidator(new FloatRangeValidator());
        filterSigmaBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_1 = BeanProperty.create("filterWidthPx");
        filterWidthPxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_1, formattedTextFieldFilterWidth, jFormattedTextFieldBeanProperty, "filterWidthPxBinding");
        filterWidthPxBinding.setConverter(new NullGenericIntegerConverter());
        filterWidthPxBinding.setValidator(new IntegerRangeValidator());
        filterWidthPxBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_2 = BeanProperty.create("numberOfIterations");
        numberOfIterationsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_2, formattedTextFieldIterations, jFormattedTextFieldBeanProperty, "numberOfIterationsBinding");
        numberOfIterationsBinding.setConverter(new NullGenericIntegerConverter());
        numberOfIterationsBinding.setValidator(new IntegerRangeValidator());
        numberOfIterationsBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeModel, Integer> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_3 = BeanProperty.create("windowSize");
        windowSizeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_3, formattedTextFieldWindowSize, jFormattedTextFieldBeanProperty, "windowSizeBinding");
        windowSizeBinding.setConverter(new NullGenericIntegerConverter());
        windowSizeBinding.setValidator(new IntegerRangeValidator());
        windowSizeBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeOpenCLModel, Boolean> subPixelInterpolationOptionsLucasKanadeOpenCLModelBeanProperty = BeanProperty.create("denseExport");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        denseExportBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkClOptions, subPixelInterpolationOptionsLucasKanadeOpenCLModelBeanProperty, chckbxExportDenseVectors, jCheckBoxBeanProperty, "denseExportBinding");
        denseExportBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsLucasKanadeModel, Boolean> subPixelInterpolationOptionsLucasKanadeModelBeanProperty_4 = BeanProperty.create("ignorePIVBaseDisplacements");
        ignorePIVDisplacementsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, lkOptions, subPixelInterpolationOptionsLucasKanadeModelBeanProperty_4, chkboxIgnorePIV, jCheckBoxBeanProperty, "ignorePIVDisplacementsBinding");
        ignorePIVDisplacementsBinding.bind();
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
    protected JRadioButton getRdbtnIgnoreAuto() {
        return rdbtnIgnoreAuto;
    }
    protected JRadioButton getRdbtnIgnoreAutoSmall() {
        return rdbtnIgnoreAutoSmall;
    }
}
