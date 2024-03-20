// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import pt.quickLabPIV.ui.models.AppContextModel;

public class ExitConfirmationDialog extends JDialog {
    private AppContextModel contextModel;
    private boolean quit = false;

    /**
     * 
     */
    private static final long serialVersionUID = -1588985259723998111L;
    private final JPanel contentPanel = new JPanel();
    private JTextPane txtpnAttentionThereAre;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ExitConfirmationDialog dialog = new ExitConfirmationDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAppContextModel(AppContextModel model) {
        contextModel = model;
        txtpnAttentionThereAre.setVisible(contextModel != null && contextModel.isPendingChanges());
    }
    
    /**
     * Create the dialog.
     */
    public ExitConfirmationDialog() {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);
        setBounds(100, 100, 448, 147);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{448, 0};
        gridBagLayout.rowHeights = new int[]{47, 35, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc_contentPanel = new GridBagConstraints();
        gbc_contentPanel.gridwidth = 3;
        gbc_contentPanel.gridheight = 2;
        gbc_contentPanel.fill = GridBagConstraints.BOTH;
        gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
        gbc_contentPanel.gridx = 0;
        gbc_contentPanel.gridy = 0;
        getContentPane().add(contentPanel, gbc_contentPanel);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{47, 354, 0};
        gbl_contentPanel.rowHeights = new int[]{15, 36, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblAreYouSure = new JLabel("Are you sure you want to Quit Application?");
            GridBagConstraints gbc_lblAreYouSure = new GridBagConstraints();
            gbc_lblAreYouSure.anchor = GridBagConstraints.NORTH;
            gbc_lblAreYouSure.insets = new Insets(0, 0, 5, 0);
            gbc_lblAreYouSure.gridx = 1;
            gbc_lblAreYouSure.gridy = 0;
            contentPanel.add(lblAreYouSure, gbc_lblAreYouSure);
        }
        {
            txtpnAttentionThereAre = new JTextPane();
            GridBagConstraints gbc_txtpnAttentionThereAre = new GridBagConstraints();
            gbc_txtpnAttentionThereAre.anchor = GridBagConstraints.NORTHWEST;
            gbc_txtpnAttentionThereAre.gridx = 1;
            gbc_txtpnAttentionThereAre.gridy = 1;
            contentPanel.add(txtpnAttentionThereAre, gbc_txtpnAttentionThereAre);
            txtpnAttentionThereAre.setEditable(false);
            txtpnAttentionThereAre.setText("ATTENTION: There are pending changes that will be lost\n by quitting at this moment.");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            GridBagConstraints gbc_buttonPane = new GridBagConstraints();
            gbc_buttonPane.anchor = GridBagConstraints.NORTH;
            gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
            gbc_buttonPane.gridx = 0;
            gbc_buttonPane.gridy = 2;
            getContentPane().add(buttonPane, gbc_buttonPane);
            {
                JButton okButton = new JButton("Quit");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        quit = true;
                        setVisible(false);
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
                        quit = false;
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }

    protected JTextPane getTxtpnAttentionThereAre() {
        return txtpnAttentionThereAre;
    }

    public void showDialog() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pack();
        setVisible(true);
    }
    
    public boolean isQuit() {
        return quit;
    }
}
