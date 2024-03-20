// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageFileFilter;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;

public class PIVImageFileDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = -1304662791274800168L;
    private final JPanel contentPanel = new JPanel();
    private AppContextModel appContext;
    private JFileChooser fc;
    private JCheckBox chckbxFirstImgSel;
    private boolean selectFirstImage;
    private boolean canceled = false;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            PIVImageFileDialog dialog = new PIVImageFileDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public PIVImageFileDialog(Window owner) {
        setTitle("Select source image folder and first image");
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new ImageFileFilter());
        fc.setControlButtonsAreShown(false);
        contentPanel.add(fc);
        pack();

        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                chckbxFirstImgSel = new JCheckBox("Also select first image");
                chckbxFirstImgSel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateFirstImgSelectionState(chckbxFirstImgSel);
                    }                   
                });
                chckbxFirstImgSel.setSelected(true);
                updateFirstImgSelectionState(chckbxFirstImgSel);
                buttonPane.add(chckbxFirstImgSel);
            }
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateWithSelection();
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
                        cancelSelection();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }        
    }

    private void updateFirstImgSelectionState(JCheckBox chckbxFirstImgSel) {
        if (chckbxFirstImgSel.isSelected()) {
           setTitle("Source image folder and first image selection");
           fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
           selectFirstImage = true;
        } else {
           setTitle("Source image folder selection only");
           selectFirstImage = false;
           fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
    }

    private void updateWithSelection() {
        File selectedSource = fc.getSelectedFile();
        if (selectedSource == null) {
            JOptionPane.showMessageDialog(this, "Please select a source image folder first", 
                    "Source image folder selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        appContext = ProjectFacade.updateSourceImage(appContext, selectFirstImage, selectedSource);
        canceled = false;
        setVisible(false);
    }
    
    private void cancelSelection() {
        canceled = true;
        setVisible(false);
    }
    
    public void setAppContext(AppContextModel appContextModel) {
        appContext = appContextModel;
        
        fc.setFileSystemView(appContext.getFileSystemViewFactory().createView());
        PIVConfigurationModel configuration = appContext.getProject().getPIVConfiguration();
        if (configuration != null && (configuration.getSourceImageFile() != null || configuration.getSourceImageFolder() != null)) {
            fc.setSelectedFile(configuration.getSourceImageFile());
            fc.setCurrentDirectory(configuration.getSourceImageFolder());
        } else {
            String workingDir = System.getProperty("user.dir");
            fc.setCurrentDirectory(new File(workingDir));
        }
    }
    
    public AppContextModel getAppContext() {
        return appContext;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    protected JCheckBox getChckbxFirstImgSel() {
        return chckbxFirstImgSel;
    }

    public boolean isFileAndFolderMode() {       
        return chckbxFirstImgSel.isSelected();
    }
}
