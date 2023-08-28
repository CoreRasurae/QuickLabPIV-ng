// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class CreateNewProjectWithChangesDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = -4787099055130947346L;
    private boolean createNew = false;;
    private final JPanel contentPanel = new JPanel();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            CreateNewProjectWithChangesDialog dialog = new CreateNewProjectWithChangesDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public CreateNewProjectWithChangesDialog() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("Create new project");
        setBounds(100, 100, 481, 157);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{458, 0};
        gridBagLayout.rowHeights = new int[]{29, 12, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc_contentPanel = new GridBagConstraints();
        gbc_contentPanel.gridheight = 3;
        gbc_contentPanel.gridwidth = 3;
        gbc_contentPanel.fill = GridBagConstraints.BOTH;
        gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
        gbc_contentPanel.gridx = 0;
        gbc_contentPanel.gridy = 0;
        getContentPane().add(contentPanel, gbc_contentPanel);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{100, 333, 0};
        gbl_contentPanel.rowHeights = new int[]{28, 0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblCurrentProjectHas = new JLabel("Current project has pending unsaved changes!");
            lblCurrentProjectHas.setVerticalAlignment(SwingConstants.TOP);
            lblCurrentProjectHas.setHorizontalAlignment(SwingConstants.CENTER);
            GridBagConstraints gbc_lblCurrentProjectHas = new GridBagConstraints();
            gbc_lblCurrentProjectHas.gridwidth = 3;
            gbc_lblCurrentProjectHas.insets = new Insets(0, 0, 5, 0);
            gbc_lblCurrentProjectHas.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblCurrentProjectHas.gridx = 0;
            gbc_lblCurrentProjectHas.gridy = 0;
            contentPanel.add(lblCurrentProjectHas, gbc_lblCurrentProjectHas);
        }
        {
            JTextArea txtrCreatingANew = new JTextArea();
            txtrCreatingANew.setWrapStyleWord(true);
            txtrCreatingANew.setEditable(false);
            txtrCreatingANew.setLineWrap(true);
            txtrCreatingANew.setText("Creating a new project will discard all data from current project.\nThis action cannot be undone.");
            GridBagConstraints gbc_txtrCreatingANew = new GridBagConstraints();
            gbc_txtrCreatingANew.gridheight = 2;
            gbc_txtrCreatingANew.insets = new Insets(0, 0, 5, 0);
            gbc_txtrCreatingANew.gridwidth = 3;
            gbc_txtrCreatingANew.fill = GridBagConstraints.BOTH;
            gbc_txtrCreatingANew.gridx = 0;
            gbc_txtrCreatingANew.gridy = 1;
            contentPanel.add(txtrCreatingANew, gbc_txtrCreatingANew);
        }
        {
            JLabel label = new JLabel("Do you really want to create a new project?");
            GridBagConstraints gbc_label = new GridBagConstraints();
            gbc_label.insets = new Insets(0, 0, 5, 0);
            gbc_label.gridwidth = 3;
            gbc_label.gridx = 0;
            gbc_label.gridy = 3;
            contentPanel.add(label, gbc_label);
        }
        {
            JSeparator separator = new JSeparator();
            GridBagConstraints gbc_separator = new GridBagConstraints();
            gbc_separator.anchor = GridBagConstraints.NORTH;
            gbc_separator.gridwidth = 3;
            gbc_separator.fill = GridBagConstraints.HORIZONTAL;
            gbc_separator.insets = new Insets(0, 0, 0, 5);
            gbc_separator.gridx = 0;
            gbc_separator.gridy = 3;
            contentPanel.add(separator, gbc_separator);
        }
        {
            JPanel buttonPane = new JPanel();
            GridBagConstraints gbc_buttonPane = new GridBagConstraints();
            gbc_buttonPane.gridwidth = 3;
            gbc_buttonPane.anchor = GridBagConstraints.SOUTH;
            gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
            gbc_buttonPane.gridx = 0;
            gbc_buttonPane.gridy = 3;
            getContentPane().add(buttonPane, gbc_buttonPane);
            GridBagLayout gbl_buttonPane = new GridBagLayout();
            gbl_buttonPane.columnWidths = new int[]{273, 117, 67, 0};
            gbl_buttonPane.rowHeights = new int[]{38, 0};
            gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
            gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
            buttonPane.setLayout(gbl_buttonPane);
            {
                JButton okButton = new JButton("Create New");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        createNew = true;
                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                GridBagConstraints gbc_okButton = new GridBagConstraints();
                gbc_okButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_okButton.insets = new Insets(0, 0, 0, 5);
                gbc_okButton.gridx = 1;
                gbc_okButton.gridy = 0;
                buttonPane.add(okButton, gbc_okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Back");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        createNew = false;
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                GridBagConstraints gbc_cancelButton = new GridBagConstraints();
                gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_cancelButton.gridx = 2;
                gbc_cancelButton.gridy = 0;
                buttonPane.add(cancelButton, gbc_cancelButton);
            }
        }
    }

    
    public boolean isCreateNew() {
        return createNew;
    }
}
