// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.views.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ImagePanel extends JPanel implements Scrollable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8444658053622982966L;
	private BufferedImage image = null;

	public ImagePanel() {
	}

	public ImagePanel(LayoutManager layout) {
		super(layout);
	}

	public ImagePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public ImagePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}
	
	public void setImage(BufferedImage _image) {
		setAutoscrolls(true);
		image = _image;
		if (image != null) {
		    setSize(image.getWidth(), image.getHeight());
	    }
		repaint();
	}

	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);        
        if (image != null) {
        	g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters     
        }
    }

	@Override
	public Dimension getPreferredSize() {
		Dimension d = new Dimension();
		if (image == null) {
			d = getParent().getSize(d);
		}else {
			d=getSize(d);
		}
		return d;
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (image == null) {
			return 0;
		}

		int displacementIntention = 1;
		
		int pixelDisplacement = 0;
		if (orientation == SwingConstants.VERTICAL) {
			if (direction == SwingConstants.NORTH) {
				displacementIntention = -1;
			}
			pixelDisplacement = computePixelsDisplacement(image.getHeight(), (float)visibleRect.getY(), (float)visibleRect.getHeight(), displacementIntention);
		} else if (orientation == SwingConstants.HORIZONTAL) {
			if (direction == SwingConstants.LEFT) { 
				displacementIntention = -1;
			}

			pixelDisplacement = computePixelsDisplacement(image.getWidth(), (float)visibleRect.getX(), (float)visibleRect.getWidth(), displacementIntention);
		}
		
		return pixelDisplacement;
	}

	private int computePixelsDisplacement(int imageWidth, float x, float visibleWidth, int direction) {
		int displacement = 0;
		float proportion = imageWidth / visibleWidth;
		if (proportion <= 1.0) {
			return 0;
		}
		
		if (direction > 0) {
			//TODO
			displacement = 20;
		} else {
			displacement = -20;
		}

		return displacement;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int pixelsIncrement = 10;
		
		if (orientation == SwingConstants.VERTICAL) {
			float heigthRatio = (float)image.getHeight() / (float)visibleRect.getHeight();
			if (heigthRatio <= 1.0) {
				pixelsIncrement = 0;
			} else if (direction == SwingConstants.NORTH) {
				if (visibleRect.y+visibleRect.getHeight() > image.getHeight() - 10) {
					pixelsIncrement = image.getHeight() - (int)(visibleRect.y+visibleRect.getHeight());
				}
			} else if (direction == SwingConstants.SOUTH) {
				pixelsIncrement = -10;
				if (visibleRect.y < 10) {
					pixelsIncrement = -visibleRect.y;
				}
			}
		} else if (orientation == SwingConstants.HORIZONTAL) {
			float widthRatio = (float)image.getWidth() / (float)visibleRect.getWidth();
			if (widthRatio <= 1.0) {
				pixelsIncrement = 0;
			} else if (direction == SwingConstants.LEFT) {
				if (visibleRect.getX()+visibleRect.getWidth() > image.getWidth() - 10) {
					pixelsIncrement = image.getWidth() - (int)(visibleRect.x+visibleRect.getWidth());
				}
			} else if (direction == SwingConstants.RIGHT) {
				pixelsIncrement = -10;
				if (visibleRect.getX() < 10) {
					pixelsIncrement = -visibleRect.x;
				}
			}
		}
		return pixelsIncrement;
	}	
}
