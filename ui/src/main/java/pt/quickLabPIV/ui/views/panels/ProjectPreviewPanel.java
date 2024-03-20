// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JTextField;

import pt.quickLabPIV.ui.controllers.NavigationFacade;

import javax.swing.JTextArea;

public class ProjectPreviewPanel extends JPanel implements PropertyChangeListener {
    /**
     * 
     */
    private static final long serialVersionUID = -2257196437685260579L;
    private JTextField titleTextField;
    private JTextField dateTextField;
    private JTextArea descriptionTextArea;
    private JLabel lblProjectTypeDetail;

    /**
     * Create the panel.
     */
    public ProjectPreviewPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 28, 0, 28, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel lblTitle = new JLabel("Title");
        GridBagConstraints gbc_lblTitle = new GridBagConstraints();
        gbc_lblTitle.anchor = GridBagConstraints.WEST;
        gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
        gbc_lblTitle.gridx = 0;
        gbc_lblTitle.gridy = 0;
        add(lblTitle, gbc_lblTitle);
        
        titleTextField = new JTextField();
        titleTextField.setEditable(false);
        GridBagConstraints gbc_titleTextField = new GridBagConstraints();
        gbc_titleTextField.insets = new Insets(0, 0, 5, 0);
        gbc_titleTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_titleTextField.gridx = 1;
        gbc_titleTextField.gridy = 0;
        add(titleTextField, gbc_titleTextField);
        titleTextField.setColumns(10);
        
        JLabel lblDescription = new JLabel("Description");
        GridBagConstraints gbc_lblDescription = new GridBagConstraints();
        gbc_lblDescription.anchor = GridBagConstraints.WEST;
        gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
        gbc_lblDescription.gridx = 0;
        gbc_lblDescription.gridy = 2;
        add(lblDescription, gbc_lblDescription);
        
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setEditable(false);
        GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
        gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
        gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
        gbc_descriptionTextArea.gridx = 1;
        gbc_descriptionTextArea.gridy = 2;
        add(descriptionTextArea, gbc_descriptionTextArea);
        
        JLabel lblCreationDate = new JLabel("Creation date");
        GridBagConstraints gbc_lblCreationDate = new GridBagConstraints();
        gbc_lblCreationDate.anchor = GridBagConstraints.WEST;
        gbc_lblCreationDate.insets = new Insets(0, 0, 5, 5);
        gbc_lblCreationDate.gridx = 0;
        gbc_lblCreationDate.gridy = 1;
        add(lblCreationDate, gbc_lblCreationDate);
        
        dateTextField = new JTextField();
        dateTextField.setEditable(false);
        GridBagConstraints gbc_dateTextField = new GridBagConstraints();
        gbc_dateTextField.insets = new Insets(0, 0, 5, 0);
        gbc_dateTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_dateTextField.gridx = 1;
        gbc_dateTextField.gridy = 1;
        add(dateTextField, gbc_dateTextField);
        dateTextField.setColumns(10);
        
        JLabel lblProjectType = new JLabel("Project type");
        GridBagConstraints gbc_lblProjectType = new GridBagConstraints();
        gbc_lblProjectType.insets = new Insets(0, 0, 0, 5);
        gbc_lblProjectType.gridx = 0;
        gbc_lblProjectType.gridy = 3;
        add(lblProjectType, gbc_lblProjectType);
        
        lblProjectTypeDetail = new JLabel("projectTypeDetail");
        GridBagConstraints gbc_lblProjectTypeDetail = new GridBagConstraints();
        gbc_lblProjectTypeDetail.gridx = 1;
        gbc_lblProjectTypeDetail.gridy = 3;
        add(lblProjectTypeDetail, gbc_lblProjectTypeDetail);
    }

    protected JTextArea getDescriptionTextArea() {
        return descriptionTextArea;
    }
    
    public void setPreviewDetails(String title, String date, String description, String projectType) {
        titleTextField.setText(title);
        dateTextField.setText(date);
        descriptionTextArea.setText(description);
        lblProjectTypeDetail.setText(projectType);
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        File selectedProject = null;
        boolean update = false;
        String prop = evt.getPropertyName();
 
        //If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            selectedProject = null;
            update = true;
        //If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            selectedProject = (File) evt.getNewValue();
            update = true;
        }
 
        //Update the preview accordingly.
        if (update) {
            NavigationFacade.previewProject(this, selectedProject);
        }
    }
    protected JTextField getDateTextField() {
        return dateTextField;
    }
    protected JLabel getLblProjectTypeDetail() {
        return lblProjectTypeDetail;
    }
}
