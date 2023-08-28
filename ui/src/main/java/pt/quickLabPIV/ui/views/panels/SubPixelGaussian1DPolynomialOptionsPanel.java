// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.views.panels;

import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public class SubPixelGaussian1DPolynomialOptionsPanel extends JPanel implements IPanelWithErrorBorders {

    /**
     * 
     */
    private static final long serialVersionUID = -5314357430894951028L;

    /**
     * Create the panel.
     */
    public SubPixelGaussian1DPolynomialOptionsPanel() {

    }

    @Override
    public List<ErrorBorderForComponent> getBorders() {
        return Collections.emptyList();
    }

    @Override
    public void setAppContext(AppContextModel model) {
        
    }

    @Override
    public void dispose() {
        
    }

}
