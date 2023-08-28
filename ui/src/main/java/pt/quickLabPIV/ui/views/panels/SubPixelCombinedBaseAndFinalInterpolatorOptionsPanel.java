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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.libs.external.DisabledPanel;
import pt.quickLabPIV.ui.controllers.PIVConfigurationFacade;
import pt.quickLabPIV.ui.converters.NullSubPixelInterpolationConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationComboBoxModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel;
import pt.quickLabPIV.ui.validators.InvalidNotAllowedCombinedSubPixelInterpolationValidator;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel extends JPanel implements IPanelWithErrorBorders, PropertyChangeListener {
    private AutoBinding<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, SubPixelInterpolationModeEnum, JComboBox<SubPixelInterpolationModeEnum>, Object> finalSubPixelModeBinding;
    private AutoBinding<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, SubPixelInterpolationModeEnum, JComboBox<SubPixelInterpolationModeEnum>, Object> baseSubPixelBinding;
    private AutoBinding<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, Boolean, JCheckBox, Boolean> applyLucasKanadeAsAbsoluteFinalStepBinding;
    private AutoBinding<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, Boolean, JCheckBox, Boolean> mainInterpolatorAtLastStepBinding;

    /**
     * 
     */
    private static final long serialVersionUID = 2266374709404009373L;
    
    private JDialog dialog;
    private JScrollPane scrollPane;
    private JPanel panelFinalInnerPanel;
    private JPanel panelBaseInnerPanel;
    private JCheckBox checkBoxLastStep;
    
    private AppContextModel appContext;
    private SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel optionsModel;
    private List<ErrorBorderForComponent> errorBorders = new ArrayList<>(10);
    private JCheckBox chckbxApplyFinalAsAbsoluteFinal;
    private JComboBox<SubPixelInterpolationModeEnum> comboBoxFinalSubPixel;
    private JLabel lblFinalSubpixelMethod;
    private JComboBox<SubPixelInterpolationModeEnum> comboBoxBaseSubPixel;
    private JLabel lblBaseSubpixelMethod;
    
    private JPanel currentBaseSubPixelOptionsPanel;
    private JPanel currentFinalSubPixelOptionsPanel;
    
    /**
     * Create the panel.
     */
    public SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{567, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JPanel panelBaseInterpolator = new JPanel();
        panelBaseInterpolator.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Base  sub-pixel configuration for first PIV levels", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        GridBagConstraints gbc_panelBaseInterpolator = new GridBagConstraints();
        gbc_panelBaseInterpolator.insets = new Insets(0, 0, 5, 0);
        gbc_panelBaseInterpolator.fill = GridBagConstraints.BOTH;
        gbc_panelBaseInterpolator.gridx = 0;
        gbc_panelBaseInterpolator.gridy = 0;
        add(panelBaseInterpolator, gbc_panelBaseInterpolator);
        GridBagLayout gbl_panelBaseInterpolator = new GridBagLayout();
        gbl_panelBaseInterpolator.columnWidths = new int[]{154, 0, 0};
        gbl_panelBaseInterpolator.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelBaseInterpolator.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelBaseInterpolator.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        panelBaseInterpolator.setLayout(gbl_panelBaseInterpolator);
        
        lblBaseSubpixelMethod = new JLabel("Base sub-pixel method");
        GridBagConstraints gbc_lblBaseSubpixelMethod = new GridBagConstraints();
        gbc_lblBaseSubpixelMethod.insets = new Insets(0, 0, 5, 5);
        gbc_lblBaseSubpixelMethod.anchor = GridBagConstraints.WEST;
        gbc_lblBaseSubpixelMethod.gridx = 0;
        gbc_lblBaseSubpixelMethod.gridy = 0;
        panelBaseInterpolator.add(lblBaseSubpixelMethod, gbc_lblBaseSubpixelMethod);
        
        checkBoxLastStep = new JCheckBox("Also apply base sub-pixel at the last PIV step before applying the final sub-pixel method");
        GridBagConstraints gbc_checkBoxLastStep = new GridBagConstraints();
        gbc_checkBoxLastStep.gridwidth = 2;
        gbc_checkBoxLastStep.anchor = GridBagConstraints.WEST;
        gbc_checkBoxLastStep.insets = new Insets(0, 0, 5, 5);
        gbc_checkBoxLastStep.gridx = 0;
        gbc_checkBoxLastStep.gridy = 1;
        panelBaseInterpolator.add(checkBoxLastStep, gbc_checkBoxLastStep);
        
        panelBaseInnerPanel = new JPanel();
        GridBagConstraints gbc_panelBaseInnerPanel = new GridBagConstraints();
        gbc_panelBaseInnerPanel.gridwidth = 2;
        gbc_panelBaseInnerPanel.insets = new Insets(0, 0, 0, 5);
        gbc_panelBaseInnerPanel.fill = GridBagConstraints.BOTH;
        gbc_panelBaseInnerPanel.gridx = 0;
        gbc_panelBaseInnerPanel.gridy = 2;
        panelBaseInterpolator.add(panelBaseInnerPanel, gbc_panelBaseInnerPanel);
        GridBagLayout gbl_panelBaseInnerPanel = new GridBagLayout();
        gbl_panelBaseInnerPanel.columnWidths = new int[]{0};
        gbl_panelBaseInnerPanel.rowHeights = new int[]{0};
        gbl_panelBaseInnerPanel.columnWeights = new double[]{Double.MIN_VALUE};
        gbl_panelBaseInnerPanel.rowWeights = new double[]{Double.MIN_VALUE};
        panelBaseInnerPanel.setLayout(gbl_panelBaseInnerPanel);
        
        comboBoxBaseSubPixel = new JComboBox<>(new SubPixelInterpolationComboBoxModel(true, false));
        comboBoxBaseSubPixel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateBaseSubPixelInterpolationOptionsPanel((SubPixelInterpolationModeEnum)comboBoxBaseSubPixel.getSelectedItem());
            }
        });
        GridBagConstraints gbc_comboBoxBaseSubPixel = new GridBagConstraints();
        gbc_comboBoxBaseSubPixel.insets = new Insets(0, 0, 5, 0);
        gbc_comboBoxBaseSubPixel.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxBaseSubPixel.gridx = 1;
        gbc_comboBoxBaseSubPixel.gridy = 0;
        panelBaseInterpolator.add(comboBoxBaseSubPixel, gbc_comboBoxBaseSubPixel);
        
        JPanel panelFinalInterpolator = new JPanel();
        panelFinalInterpolator.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Final sub-pixel configuration for last PIV level", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        GridBagConstraints gbc_panelFinalInterpolator = new GridBagConstraints();
        gbc_panelFinalInterpolator.fill = GridBagConstraints.BOTH;
        gbc_panelFinalInterpolator.gridx = 0;
        gbc_panelFinalInterpolator.gridy = 1;
        add(panelFinalInterpolator, gbc_panelFinalInterpolator);
        GridBagLayout gbl_panelFinalInterpolator = new GridBagLayout();
        gbl_panelFinalInterpolator.columnWidths = new int[]{0, 0, 0};
        gbl_panelFinalInterpolator.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelFinalInterpolator.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelFinalInterpolator.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        panelFinalInterpolator.setLayout(gbl_panelFinalInterpolator);
        
        lblFinalSubpixelMethod = new JLabel("Final sub-pixel method");
        GridBagConstraints gbc_lblFinalSubpixelMethod = new GridBagConstraints();
        gbc_lblFinalSubpixelMethod.insets = new Insets(0, 0, 5, 5);
        gbc_lblFinalSubpixelMethod.anchor = GridBagConstraints.WEST;
        gbc_lblFinalSubpixelMethod.gridx = 0;
        gbc_lblFinalSubpixelMethod.gridy = 0;
        panelFinalInterpolator.add(lblFinalSubpixelMethod, gbc_lblFinalSubpixelMethod);
        
        chckbxApplyFinalAsAbsoluteFinal = new JCheckBox("Apply final sub-pixel method after PIV processing and validation (as absolute final)");
        GridBagConstraints gbc_chckbxApplyFinalAsAbsoluteFinal = new GridBagConstraints();
        gbc_chckbxApplyFinalAsAbsoluteFinal.gridwidth = 2;
        gbc_chckbxApplyFinalAsAbsoluteFinal.anchor = GridBagConstraints.WEST;
        gbc_chckbxApplyFinalAsAbsoluteFinal.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxApplyFinalAsAbsoluteFinal.gridx = 0;
        gbc_chckbxApplyFinalAsAbsoluteFinal.gridy = 1;
        panelFinalInterpolator.add(chckbxApplyFinalAsAbsoluteFinal, gbc_chckbxApplyFinalAsAbsoluteFinal);
        
        panelFinalInnerPanel = new JPanel();
        GridBagConstraints gbc_panelFinalInnerPanel = new GridBagConstraints();
        gbc_panelFinalInnerPanel.gridwidth = 2;
        gbc_panelFinalInnerPanel.insets = new Insets(0, 0, 0, 5);
        gbc_panelFinalInnerPanel.fill = GridBagConstraints.BOTH;
        gbc_panelFinalInnerPanel.gridx = 0;
        gbc_panelFinalInnerPanel.gridy = 2;
        panelFinalInterpolator.add(panelFinalInnerPanel, gbc_panelFinalInnerPanel);
        GridBagLayout gbl_panelFinalInnerPanel = new GridBagLayout();
        gbl_panelFinalInnerPanel.columnWidths = new int[]{0};
        gbl_panelFinalInnerPanel.rowHeights = new int[]{0};
        gbl_panelFinalInnerPanel.columnWeights = new double[]{Double.MIN_VALUE};
        gbl_panelFinalInnerPanel.rowWeights = new double[]{Double.MIN_VALUE};
        panelFinalInnerPanel.setLayout(gbl_panelFinalInnerPanel);
        
        comboBoxFinalSubPixel = new JComboBox<>(new SubPixelInterpolationComboBoxModel(true, true));
        comboBoxFinalSubPixel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateFinalSubPixelInterpolationOptionsPanel((SubPixelInterpolationModeEnum)comboBoxFinalSubPixel.getSelectedItem());
            }
        });
        GridBagConstraints gbc_comboBoxFinalSubPixel = new GridBagConstraints();
        gbc_comboBoxFinalSubPixel.insets = new Insets(0, 0, 5, 0);
        gbc_comboBoxFinalSubPixel.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxFinalSubPixel.gridx = 1;
        gbc_comboBoxFinalSubPixel.gridy = 0;
        panelFinalInterpolator.add(comboBoxFinalSubPixel, gbc_comboBoxFinalSubPixel);
        initDataBindings();
        postInitDataBindings();
    }
    
    public void setParentDialogAndScrollPane(JDialog _dialog, JScrollPane _scrollPane) {
        dialog = _dialog;
        scrollPane = _scrollPane;
    }
    
    private void addOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        errorBorders.addAll(panelBorders);
    }
    
    private void removeOptionPanelErrorBorders(List<ErrorBorderForComponent> panelBorders) {
        errorBorders.removeAll(panelBorders);
    }
    
    private void updateBaseSubPixelInterpolationOptionsPanel(SubPixelInterpolationModeEnum selectedAlgorithm) {       
        if (currentBaseSubPixelOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentBaseSubPixelOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelBaseInnerPanel.remove(currentBaseSubPixelOptionsPanel);
        }
        
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createSubPixelInterpolationOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentBaseSubPixelOptionsPanel = 
                PIVConfigurationFacade.updateSubPixelOptionPanelFromSelection(dialog, scrollPane, panelBaseInnerPanel, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentBaseSubPixelOptionsPanel;
        if (panelWithBorders != null) {
            addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }
        
        if (selectedAlgorithm == optionsModel.getFinalSubPixelMode() && optionsModel.getFinalSubPixelMode() != SubPixelInterpolationModeEnum.Disabled) {
            if (currentFinalSubPixelOptionsPanel != null) {
                DisabledPanel.disable(currentFinalSubPixelOptionsPanel);
            }
        } else {
            if (currentBaseSubPixelOptionsPanel != null) {
                DisabledPanel.enable(currentBaseSubPixelOptionsPanel);
            }
            if (currentFinalSubPixelOptionsPanel != null) {
                DisabledPanel.enable(currentFinalSubPixelOptionsPanel);
            }
        }
    }

    private void updateFinalSubPixelInterpolationOptionsPanel(SubPixelInterpolationModeEnum selectedAlgorithm) {
        if (currentFinalSubPixelOptionsPanel != null) {
            IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentFinalSubPixelOptionsPanel;
            removeOptionPanelErrorBorders(panelWithBorders.getBorders());
            panelFinalInnerPanel.remove(currentFinalSubPixelOptionsPanel);
        }
        
        appContext = pt.quickLabPIV.business.facade.PIVConfigurationFacade.createSubPixelInterpolationOptionsModelForOption(appContext, selectedAlgorithm);
        
        currentFinalSubPixelOptionsPanel = 
                PIVConfigurationFacade.updateSubPixelOptionPanelFromSelection(dialog, scrollPane, panelFinalInnerPanel, selectedAlgorithm, appContext);
        IPanelWithErrorBorders panelWithBorders = (IPanelWithErrorBorders)currentFinalSubPixelOptionsPanel;
        if (panelWithBorders != null) {
            addOptionPanelErrorBorders(panelWithBorders.getBorders());
        }

        if (selectedAlgorithm == optionsModel.getBaseSubPixelMode() && optionsModel.getBaseSubPixelMode() != SubPixelInterpolationModeEnum.Disabled) {
            if (currentBaseSubPixelOptionsPanel != null) {
                DisabledPanel.disable(currentBaseSubPixelOptionsPanel);
            }
        } else {
            if (currentBaseSubPixelOptionsPanel != null) {
                DisabledPanel.enable(currentBaseSubPixelOptionsPanel);
            }
            if (currentFinalSubPixelOptionsPanel != null) {
                DisabledPanel.enable(currentFinalSubPixelOptionsPanel);
            }
        }
    }
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        
        PIVConfigurationModel pivModel = appContext.getProject().getPIVConfiguration();
        optionsModel = (SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel)pivModel.getInterpolationOption(SubPixelInterpolationModeEnum.CombinedBaseAndFinalInterpolator);
        
        ((InvalidNotAllowedCombinedSubPixelInterpolationValidator)baseSubPixelBinding.getValidator()).setAppContext(model);
        ((InvalidNotAllowedCombinedSubPixelInterpolationValidator)finalSubPixelModeBinding.getValidator()).setAppContext(model);
        
        mainInterpolatorAtLastStepBinding.unbind();
        mainInterpolatorAtLastStepBinding.setSourceObject(optionsModel);
        mainInterpolatorAtLastStepBinding.bind();
        
        applyLucasKanadeAsAbsoluteFinalStepBinding.unbind();
        applyLucasKanadeAsAbsoluteFinalStepBinding.setSourceObject(optionsModel);
        applyLucasKanadeAsAbsoluteFinalStepBinding.bind();
        
        baseSubPixelBinding.unbind();
        baseSubPixelBinding.setSourceObject(optionsModel);
        baseSubPixelBinding.bind();

        finalSubPixelModeBinding.unbind();
        finalSubPixelModeBinding.setSourceObject(optionsModel);
        finalSubPixelModeBinding.bind();
        
        //Force Swing components to validate and generate corresponding error boxes, if needed.
        baseSubPixelBinding.saveAndNotify();
        finalSubPixelModeBinding.saveAndNotify();
        
        optionsModel.addPropertyChangeListener(this);
        
    }

    private void postInitDataBindings() {
        ErrorBorderForComponent borderBaseSubPixel = new ErrorBorderForComponent(comboBoxBaseSubPixel);
        comboBoxBaseSubPixel.setBorder(borderBaseSubPixel);
        baseSubPixelBinding.addBindingListener(borderBaseSubPixel);
        errorBorders.add(borderBaseSubPixel);

        ErrorBorderForComponent borderFinalSubPixel = new ErrorBorderForComponent(comboBoxFinalSubPixel);
        comboBoxFinalSubPixel.setBorder(borderFinalSubPixel);
        finalSubPixelModeBinding.addBindingListener(borderFinalSubPixel);
        errorBorders.add(borderFinalSubPixel);
        
        InvalidNotAllowedCombinedSubPixelInterpolationValidator finalValidator = (InvalidNotAllowedCombinedSubPixelInterpolationValidator)finalSubPixelModeBinding.getValidator();
        InvalidNotAllowedCombinedSubPixelInterpolationValidator baseValidator = (InvalidNotAllowedCombinedSubPixelInterpolationValidator)baseSubPixelBinding.getValidator();
        finalValidator.setOtherValidator(baseValidator);
        baseValidator.setOtherValidator(finalValidator);
    }
    
    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return errorBorders;
    }

    protected JPanel getPanelLucasKanadeInnerPanel() {
        return panelFinalInnerPanel;
    }
    protected JPanel getPanelHongweiGuoInnerPanel() {
        return panelBaseInnerPanel;
    }
    protected JCheckBox getCheckBoxLastStep() {
        return checkBoxLastStep;
    }
    protected void initDataBindings() {
        BeanProperty<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, Boolean> subPixelInterpolationOptionsGaussian1DHongweiGuoWithLucasKanadeFinalModelBeanProperty = BeanProperty.create("alsoApplyMainInterpolationOnLastStep");
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        mainInterpolatorAtLastStepBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, optionsModel, subPixelInterpolationOptionsGaussian1DHongweiGuoWithLucasKanadeFinalModelBeanProperty, checkBoxLastStep, jCheckBoxBeanProperty, "mainInterpolatorAtLastStepBinding");
        mainInterpolatorAtLastStepBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, Boolean> subPixelInterpolationOptionsGaussian1DHongweiGuoWithLucasKanadeFinalModelBeanProperty_1 = BeanProperty.create("applyFinalInterpolationAsLastPIVProcessingStep");
        applyLucasKanadeAsAbsoluteFinalStepBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, optionsModel, subPixelInterpolationOptionsGaussian1DHongweiGuoWithLucasKanadeFinalModelBeanProperty_1, chckbxApplyFinalAsAbsoluteFinal, jCheckBoxBeanProperty, "applyLucasKanadeAsAbsoluteFinalStepBinding");
        applyLucasKanadeAsAbsoluteFinalStepBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, SubPixelInterpolationModeEnum> subPixelInterpolationOptionsCombinedBaseInterpolatorAndFinalInterpolatorModelBeanProperty = BeanProperty.create("baseSubPixelMode");
        BeanProperty<JComboBox<SubPixelInterpolationModeEnum>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        baseSubPixelBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, optionsModel, subPixelInterpolationOptionsCombinedBaseInterpolatorAndFinalInterpolatorModelBeanProperty, comboBoxBaseSubPixel, jComboBoxBeanProperty, "baseSubPixelBinding");
        baseSubPixelBinding.setConverter(new NullSubPixelInterpolationConverter());
        baseSubPixelBinding.setValidator(new InvalidNotAllowedCombinedSubPixelInterpolationValidator());
        baseSubPixelBinding.bind();
        //
        BeanProperty<SubPixelInterpolationOptionsCombinedBaseAndFinalInterpolatorModel, SubPixelInterpolationModeEnum> subPixelInterpolationOptionsCombinedBaseInterpolatorAndFinalInterpolatorModelBeanProperty_1 = BeanProperty.create("finalSubPixelMode");
        finalSubPixelModeBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, optionsModel, subPixelInterpolationOptionsCombinedBaseInterpolatorAndFinalInterpolatorModelBeanProperty_1, comboBoxFinalSubPixel, jComboBoxBeanProperty, "finalSubPixelModeBinding");
        finalSubPixelModeBinding.setConverter(new NullSubPixelInterpolationConverter());
        finalSubPixelModeBinding.setValidator(new InvalidNotAllowedCombinedSubPixelInterpolationValidator());
        finalSubPixelModeBinding.bind();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if ("baseSubPixelMode".equals(pce.getPropertyName()) ||
            "finalSubPixelMode".equals(pce.getPropertyName())) {
            finalSubPixelModeBinding.saveAndNotify();
            baseSubPixelBinding.saveAndNotify();
        }        
    }

    @Override
    public void dispose() {
        if (optionsModel != null) {
            optionsModel.removePropertyChangeListener(this);
        }
    }
}