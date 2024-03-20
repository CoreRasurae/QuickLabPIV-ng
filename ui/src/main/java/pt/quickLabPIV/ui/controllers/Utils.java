// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.controllers;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Utils {

    public static int showOptionDialog(Component parent, String title, String message) {
           String[] options = { UIManager.getString("OptionPane.yesButtonText"),
                                UIManager.getString("OptionPane.noButtonText"),
                                UIManager.getString("OptionPane.cancelButtonText") };
           String defaultOption = UIManager.getString("OptionPane.noButtonText");
           int selection = JOptionPane.showOptionDialog(parent, message, title, 
                   JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, defaultOption);
        return selection;
    }

    public static int showConfirmationDialog(Component parent, String title, String message) {
        String[] options = { UIManager.getString("OptionPane.okButtonText")};
        String defaultOption = UIManager.getString("OptionPane.okButtonText");
        int selection = JOptionPane.showOptionDialog(parent, message, title, 
                JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, defaultOption);
     return selection;
 }

}
