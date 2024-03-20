// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.ui.converters.FileConverter;
import pt.quickLabPIV.ui.converters.ImageResolutionConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;

public class ImageInfoPanel extends JPanel {
    private AutoBinding<PIVConfigurationModel, int[], JLabel, String> imageResolutionBinding;
    private AutoBinding<PIVConfigurationModel, String, JLabel, String> imagePatternABinding;
    private AutoBinding<PIVConfigurationModel, File, JLabel, String> imagePathBinding;

    /**
     * 
     */
    private static final long serialVersionUID = 5633514892894831499L;
    
    private AppContextModel appContext;
    private PIVConfigurationModel pivModel;
    private JLabel labelImagePath;
    private JLabel lblFilePattern;
    private JLabel lblResolution;

    public static void main(String[] args) {
        try {
            ImageInfoPanel panel = new ImageInfoPanel();
            JDialog dialog = new JDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            dialog.getContentPane().add(panel, FlowLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ImageInfoPanel() {
        setBorder(new TitledBorder(null, "Image info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));        
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{220, 0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_panel);
        
        JLabel label = new JLabel("Image path");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        add(label, gbc_label);
        
        labelImagePath = new JLabel("");
        labelImagePath.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_labelImagePath = new GridBagConstraints();
        gbc_labelImagePath.fill = GridBagConstraints.HORIZONTAL;
        gbc_labelImagePath.anchor = GridBagConstraints.WEST;
        gbc_labelImagePath.insets = new Insets(0, 0, 5, 0);
        gbc_labelImagePath.gridx = 1;
        gbc_labelImagePath.gridy = 0;
        add(labelImagePath, gbc_labelImagePath);
        
        JLabel label_2 = new JLabel("File pattern A");
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.anchor = GridBagConstraints.WEST;
        gbc_label_2.insets = new Insets(0, 0, 5, 5);
        gbc_label_2.gridx = 0;
        gbc_label_2.gridy = 1;
        add(label_2, gbc_label_2);
        
        lblFilePattern = new JLabel("");
        lblFilePattern.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblFilePattern = new GridBagConstraints();
        gbc_lblFilePattern.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblFilePattern.anchor = GridBagConstraints.WEST;
        gbc_lblFilePattern.insets = new Insets(0, 0, 5, 0);
        gbc_lblFilePattern.gridx = 1;
        gbc_lblFilePattern.gridy = 1;
        add(lblFilePattern, gbc_lblFilePattern);
        
        JLabel label_4 = new JLabel("Resolution");
        GridBagConstraints gbc_label_4 = new GridBagConstraints();
        gbc_label_4.anchor = GridBagConstraints.WEST;
        gbc_label_4.insets = new Insets(0, 0, 0, 5);
        gbc_label_4.gridx = 0;
        gbc_label_4.gridy = 2;
        add(label_4, gbc_label_4);
        
        lblResolution = new JLabel("1600x1200");
        lblResolution.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblResolution = new GridBagConstraints();
        gbc_lblResolution.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblResolution.gridx = 1;
        gbc_lblResolution.gridy = 2;
        add(lblResolution, gbc_lblResolution);
        
        initDataBindings();
    }
    
    public void setAppContext(AppContextModel model) {
        appContext = model;
        pivModel = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        
        //Re-bind to model, so that updated values are seen and can be updated by the GUI
        imagePathBinding.unbind();
        imagePathBinding.setSourceObject(pivModel);
        imagePathBinding.bind();
        //
        imagePatternABinding.unbind();
        imagePatternABinding.setSourceObject(pivModel);
        imagePatternABinding.bind();
        //
        imageResolutionBinding.unbind();
        imageResolutionBinding.setSourceObject(pivModel);
        imageResolutionBinding.bind();
    }

    protected void initDataBindings() {
        BeanProperty<PIVConfigurationModel, File> pIVConfigurationModelBeanProperty_5 = BeanProperty.create("sourceImageFile");
        BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
        imagePathBinding = Bindings.createAutoBinding(UpdateStrategy.READ, pivModel, pIVConfigurationModelBeanProperty_5, labelImagePath, jLabelBeanProperty, "imagePathBinding");
        imagePathBinding.setConverter(new FileConverter());
        imagePathBinding.bind();
        //
        BeanProperty<PIVConfigurationModel, String> pIVConfigurationModelBeanProperty_6 = BeanProperty.create("imagePatternA");
        imagePatternABinding = Bindings.createAutoBinding(UpdateStrategy.READ, pivModel, pIVConfigurationModelBeanProperty_6, lblFilePattern, jLabelBeanProperty, "imagePatternABinding");
        imagePatternABinding.bind();
        //
        ELProperty<PIVConfigurationModel, int[]> pIVConfigurationModelEvalutionProperty = ELProperty.create("${imageResolution}");
        imageResolutionBinding = Bindings.createAutoBinding(UpdateStrategy.READ, pivModel, pIVConfigurationModelEvalutionProperty, lblResolution, jLabelBeanProperty, "imageResolutionBinding");
        imageResolutionBinding.setConverter(new ImageResolutionConverter());
        imageResolutionBinding.bind();
    }
    
    protected JLabel getLabelImagePath() {
        return labelImagePath;
    }
    
    protected JLabel getLblFilePattern() {
        return lblFilePattern;
    }
    
    protected JLabel getLblResolution() {
        return lblResolution;
    }
}
