package pt.quickLabPIV.ui.views;

import javax.swing.JFormattedTextField;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;

/**
 * This is a wrapper to allow WindowBuilder to work in design mode without crashing. As its reflection code is unable to
 * handle templated constructors.
 * @author lpnm
 *
 */
public class JFormattedTextFieldLayerWrapper {
    private JLayer<JFormattedTextField> layer;

    public JFormattedTextFieldLayerWrapper(JFormattedTextField c, LayerUI<JFormattedTextField> ui) {
        layer = new JLayer<JFormattedTextField>(c, ui);
    }
    
    public JLayer<JFormattedTextField> getLayer() {
        return layer;
    }
}
