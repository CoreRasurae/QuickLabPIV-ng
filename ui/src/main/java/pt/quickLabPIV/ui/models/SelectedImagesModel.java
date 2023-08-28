// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SelectedImagesModel {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private BufferedImage image;
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void setImage(BufferedImage img) {
        BufferedImage oldImage = image;
        image = img;
        pcs.firePropertyChange("image", oldImage, image);
    }
    
    public BufferedImage getImage() {
        return image;
    }
}
