// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.controllers;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageFilteringModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.VelocityStabilizationModeEnum;
import pt.quickLabPIV.ui.models.VelocityValidationModeEnum;
import pt.quickLabPIV.ui.views.PIVConfigurationDialog;
import pt.quickLabPIV.ui.views.PIVImagePreProcessingDialog;
import pt.quickLabPIV.ui.views.panels.GaussianImageFilterConfigurationPanel;
import pt.quickLabPIV.ui.views.panels.StabilizationMaxDisplacementOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelBiCubicOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelCentroid2DOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian1DHongweiGuoOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian1DOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian1DPolynomialOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian2DLinearRegressionOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian2DOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelGaussian2DPolynomialOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelLiuShenWithLucasKanadeOptionsPanel;
import pt.quickLabPIV.ui.views.panels.SubPixelLucasKanadeOptionsPanel;
import pt.quickLabPIV.ui.views.panels.VectorValidationDifferenceOptionsPanel;
import pt.quickLabPIV.ui.views.panels.VectorValidationMultiPeakNormalizedMedianOptionsPanel;
import pt.quickLabPIV.ui.views.panels.VectorValidationNormalizedMedianOptionsPanel;

public class PIVConfigurationFacade {

    public static JPanel updateSubPixelOptionPanelFromSelection(JDialog dialog, JScrollPane scrollPane, JPanel parentPanel, SubPixelInterpolationModeEnum strategy, AppContextModel model) {
        JPanel panel = null;
        switch (strategy) {
        case BiCubic:
            SubPixelBiCubicOptionsPanel bicubicPanel = new SubPixelBiCubicOptionsPanel();
            bicubicPanel.setAppContext(model);
            bicubicPanel.setParentDialog(dialog);
            panel = bicubicPanel;            
            break;
        case Gaussian1D:
            SubPixelGaussian1DOptionsPanel gaussian1DPanel = new SubPixelGaussian1DOptionsPanel();
            gaussian1DPanel.setAppContext(model);
            gaussian1DPanel.setParentDialog(dialog);
            panel = gaussian1DPanel;
            break;
        case Gaussian1DHongweiGuo:
            SubPixelGaussian1DHongweiGuoOptionsPanel gaussian1DHongweiPanel = new SubPixelGaussian1DHongweiGuoOptionsPanel();
            gaussian1DHongweiPanel.setAppContext(model);
            panel = gaussian1DHongweiPanel;
            break;
        case Gaussian1DPolynomial:
            SubPixelGaussian1DPolynomialOptionsPanel gaussian1DPolyPanel = new SubPixelGaussian1DPolynomialOptionsPanel();
            panel = gaussian1DPolyPanel;
            break;
        case Centroid2D:
            SubPixelCentroid2DOptionsPanel centroidPanel = new SubPixelCentroid2DOptionsPanel();
            centroidPanel.setAppContext(model);
            panel = centroidPanel;
            break;
        case Gaussian2D:
            SubPixelGaussian2DOptionsPanel gaussian2DPanel = new SubPixelGaussian2DOptionsPanel();
            gaussian2DPanel.setAppContext(model);
            panel = gaussian2DPanel;
            break;
        case Gaussian2DPolynomial:
            SubPixelGaussian2DPolynomialOptionsPanel gaussian2DPolyPanel = new SubPixelGaussian2DPolynomialOptionsPanel();
            gaussian2DPolyPanel.setAppContext(model);
            panel = gaussian2DPolyPanel;
            break;            
        case Gaussian2DLinearRegression:
            SubPixelGaussian2DLinearRegressionOptionsPanel gaussian2DLinearPanel = new SubPixelGaussian2DLinearRegressionOptionsPanel();
            gaussian2DLinearPanel.setAppContext(model);
            panel = gaussian2DLinearPanel;
            break;
        case LucasKanade:
            SubPixelLucasKanadeOptionsPanel lucasKanadePanel = new SubPixelLucasKanadeOptionsPanel();
            lucasKanadePanel.setAppContext(model, false);
            panel = lucasKanadePanel;
            break;
        case LucasKanadeOpenCL:
            SubPixelLucasKanadeOptionsPanel lucasKanadeOpenCLPanel = new SubPixelLucasKanadeOptionsPanel();
            lucasKanadeOpenCLPanel.setAppContext(model, true);
            panel = lucasKanadeOpenCLPanel;
            break;
        case CombinedBaseAndFinalInterpolator:
            SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel combinedBaseAndFinalInterpolatorPanel = new SubPixelCombinedBaseAndFinalInterpolatorOptionsPanel();
            combinedBaseAndFinalInterpolatorPanel.setAppContext(model);
            combinedBaseAndFinalInterpolatorPanel.setParentDialogAndScrollPane(dialog, scrollPane);
            panel = combinedBaseAndFinalInterpolatorPanel;
            break;
        case LiuShenWithLucasKanade:
            SubPixelLiuShenWithLucasKanadeOptionsPanel liuShenWithLucasKanadePanel = new SubPixelLiuShenWithLucasKanadeOptionsPanel();
            liuShenWithLucasKanadePanel.setAppContext(model, false);
            panel = liuShenWithLucasKanadePanel;
            break;
        case LiuShenWithLucasKanadeOpenCL:
            SubPixelLiuShenWithLucasKanadeOptionsPanel liuShenWithLucasKanadeOpenCLPanel = new SubPixelLiuShenWithLucasKanadeOptionsPanel();
            liuShenWithLucasKanadeOpenCLPanel.setAppContext(model, true);
            panel = liuShenWithLucasKanadeOpenCLPanel;
            break;
        case Disabled:
            break;
        default:
            break;        
        }
        
        if (panel != null) {
            panel.setSize(panel.getPreferredSize());
            panel.setVisible(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;            
            parentPanel.add(panel, gbc);
        }
        
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        dimension.height -= 80;
        
        if (dialog != null) {
            dialog.invalidate();
            dialog.pack();
            dialog.repaint();
            if (dialog.getSize().height < dimension.height && scrollPane.getViewport().getViewSize().height > scrollPane.getViewportBorderBounds().getHeight()) {
                Dimension preferredDimensionDialog = null;
                Dimension preferredDimensionScrollBar = null;
                int deltaHeight = scrollPane.getViewport().getViewSize().height - scrollPane.getViewportBorderBounds().height;
                int deltaWidth = scrollPane.getViewport().getViewSize().width - scrollPane.getViewportBorderBounds().width;
                if (deltaHeight + dialog.getSize().height < dimension.height) {
                    preferredDimensionDialog = new Dimension(dialog.getSize().width + deltaWidth, deltaHeight + dialog.getSize().height);
                    preferredDimensionScrollBar = new Dimension(scrollPane.getSize().width + deltaWidth, scrollPane.getHeight() + deltaHeight);
                } else {
                    int scrollBarSize = scrollPane.getVerticalScrollBar().getWidth();
                    preferredDimensionDialog = new Dimension(dialog.getSize().width + deltaWidth + scrollBarSize, dimension.height);
                    deltaHeight = dimension.height - dialog.getSize().height;
                    preferredDimensionScrollBar = new Dimension(scrollPane.getSize().width + deltaWidth + scrollBarSize, scrollPane.getHeight() + deltaHeight);
                }

                dialog.setPreferredSize(preferredDimensionDialog);
                
                Rectangle dialogBounds = dialog.getBounds();
                if (dialogBounds.y + preferredDimensionDialog.height > dimension.height) {
                    dialogBounds.y -= (dialogBounds.y + preferredDimensionDialog.height - dimension.height) / 2;
                    dialogBounds.height = preferredDimensionDialog.height;
                    dialogBounds.width = preferredDimensionDialog.width;
                    dialog.setBounds(dialogBounds);
                } else {
                    dialog.setSize(preferredDimensionDialog);
                }
                
                dialog.revalidate();
                scrollPane.setSize(preferredDimensionScrollBar);
                scrollPane.revalidate();
            }
        }
            
        return panel;
    }
    
    public static JPanel updateStabilizationOptionsPanelFromSelection(PIVConfigurationDialog dialog, JPanel parentPanel, VelocityStabilizationModeEnum strategy, AppContextModel model) {
        JPanel panel = null;
        
        if (strategy == null) {
            strategy = VelocityStabilizationModeEnum.Disabled;
        }
        
        switch (strategy) {
        case MaxDisplacement:
            StabilizationMaxDisplacementOptionsPanel maxDisplacementPanel = new StabilizationMaxDisplacementOptionsPanel();
            maxDisplacementPanel.setAppContextModel(model);
            panel = maxDisplacementPanel;
            break;
        case Disabled:
            break;
        default:
            break;
        }
        
        if (panel != null) {
            panel.setSize(panel.getPreferredSize());
            panel.setVisible(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;            
            parentPanel.add(panel, gbc);
        }

        dialog.invalidate();
        dialog.pack();
        dialog.repaint();

        return panel;
    }

    public static JPanel updateValidationOptionsPanelFromSelection(PIVConfigurationDialog dialog,
            JPanel parentPanel, VelocityValidationModeEnum strategy,
            AppContextModel model) {
        JPanel panel = null;
        
        if (strategy == null) {
            strategy = VelocityValidationModeEnum.Disabled;
        }
        
        switch (strategy) {
        case DifferenceOnly:
            VectorValidationDifferenceOptionsPanel differenceOnlyPanel = new VectorValidationDifferenceOptionsPanel();
            differenceOnlyPanel.setTargetValidationMode(VelocityValidationModeEnum.DifferenceOnly);
            differenceOnlyPanel.setAppContextModel(model);
            panel = differenceOnlyPanel;
            break;
        case Difference:
            VectorValidationDifferenceOptionsPanel differencePanel = new VectorValidationDifferenceOptionsPanel();
            differencePanel.setAppContextModel(model);
            panel = differencePanel;
            break;
        case NormalizedMedianOnly:
            VectorValidationNormalizedMedianOptionsPanel normalizedMedianOnlyPanel = new VectorValidationNormalizedMedianOptionsPanel();
            normalizedMedianOnlyPanel.setTargetValidationMode(VelocityValidationModeEnum.NormalizedMedianOnly);
            normalizedMedianOnlyPanel.setAppContextModel(model);           
            panel = normalizedMedianOnlyPanel;
            break;
        case NormalizedMedian:
            VectorValidationNormalizedMedianOptionsPanel normalizedMedianPanel = new VectorValidationNormalizedMedianOptionsPanel();
            normalizedMedianPanel.setAppContextModel(model);
            panel = normalizedMedianPanel;
            break;
        case MultiPeakNormalizedMedian:
            VectorValidationMultiPeakNormalizedMedianOptionsPanel multiPeakNormMedianPanel = new VectorValidationMultiPeakNormalizedMedianOptionsPanel();
            multiPeakNormMedianPanel.setAppContextModel(model);
            panel = multiPeakNormMedianPanel;
            break;
        case Disabled:
            break;
        default:
            break;
        }
        
        if (panel != null) {
            panel.setSize(panel.getPreferredSize());
            panel.setVisible(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;            
            parentPanel.add(panel, gbc);
        }

        dialog.invalidate();
        dialog.pack();
        dialog.repaint();

        return panel;
    }

    public static JPanel updateImageFilterOptionPanelFromSelection(
            PIVImagePreProcessingDialog dialog, JPanel parentPanel,
            ImageFilteringModeEnum option, AppContextModel model) {
        JPanel panel = null;
        
        if (option == null) {
            option = ImageFilteringModeEnum.DoNotApplyImageFiltering;
        }

        switch (option) {
        case ApplyImageFilteringGaussian2D:
            GaussianImageFilterConfigurationPanel gaussianPanel = new GaussianImageFilterConfigurationPanel();
            gaussianPanel.setAppContextModel(model);
            panel = gaussianPanel;
            break;
            
        case DoNotApplyImageFiltering:
            break;
        default:
            break;
        }

        if (panel != null) {
            panel.setSize(panel.getPreferredSize());
            panel.setVisible(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            parentPanel.add(panel, gbc);
        }

        dialog.invalidate();
        dialog.pack();
        dialog.repaint();

        return panel;
    }
}
