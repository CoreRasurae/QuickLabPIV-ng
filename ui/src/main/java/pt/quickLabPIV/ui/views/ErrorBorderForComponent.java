package pt.quickLabPIV.ui.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.Validator.Result;

public class ErrorBorderForComponent implements Border, BindingListener {
    private String originalToolTipText;
    private Color errorColor;
    private JComponent targetComponent;
    private Border originalBorder;
    private boolean isErrored = false;
    private boolean isInternallySet = false;
    
    public ErrorBorderForComponent(JComponent component) {
        setConfig(component, null);
    }
    
    public ErrorBorderForComponent(JComponent component, Color color) {
        setConfig(component, color);
    }
    
    private void setConfig(JComponent component, Color color) {
        targetComponent = component;
        originalBorder = component.getBorder();
        if (color == null) {
            errorColor = Color.RED;
        } else {
            errorColor = color;
        }        
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (originalBorder != null) {
            originalBorder.paintBorder(c, g, x, y, width, height);
        }
        if (isErrored) {
            Graphics bg = g.create();
            bg.setColor(errorColor);
            bg.drawRect(x, y, width-1, height-1);
        }
    }
    
    public void setError(boolean errored) {
        if (isErrored != errored) {
            isErrored = errored;
            
            if (!isErrored && originalToolTipText != null) {
                if (originalToolTipText.isEmpty()) {
                    targetComponent.setToolTipText(null);
                } else {
                    targetComponent.setToolTipText(originalToolTipText);
                }
                originalToolTipText = null;
            }
            
            targetComponent.repaint();
        }
    }
    
    public Component getComponent() {
        return targetComponent;
    }
    
    public boolean isErrored() {
        return isErrored;
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        if (originalBorder != null) {
            return originalBorder.getBorderInsets(c);
        } else {         
            return new Insets(2, 2, 2, 2);
        }
    }

    @Override
    public boolean isBorderOpaque() {
        return originalBorder.isBorderOpaque();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bindingBecameBound(Binding binding) {
        //Empty on purpose        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bindingBecameUnbound(Binding binding) {
        //Empty on purpose
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void syncFailed(Binding binding, SyncFailure failure) {
        if (Binding.SyncFailureType.CONVERSION_FAILED == failure.getType()) {
            internalUpdateStatus("Invalid value entered");
        } else if (Binding.SyncFailureType.VALIDATION_FAILED == failure.getType()) {
            internalUpdateStatus(failure.getValidationResult().getDescription());
        }
        setError(true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void synced(Binding binding) {
        if (!isInternallySet) {
            internalUpdateStatus(null);
        }
        isInternallySet = false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sourceChanged(Binding binding, PropertyStateEvent event) {
        //Empty on purpose
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void targetChanged(Binding binding, PropertyStateEvent event) {
        //Empty on purpose
    }

    private void internalUpdateStatus(String message) {
        if (message == null) {
            setError(false);
        } else {
            if (originalToolTipText == null) {
                originalToolTipText = targetComponent.getToolTipText();
                if (originalToolTipText == null) {
                    originalToolTipText = "";
                }
            }
            
            targetComponent.setToolTipText(message);
            setError(true);
        }
    }
    
    @SuppressWarnings("rawtypes")
    public void updateStatus(Result r) {
        if (r == null) {
            internalUpdateStatus(null);
        } else {
            internalUpdateStatus(r.getDescription());
            isInternallySet = true;
        }
    }
}
