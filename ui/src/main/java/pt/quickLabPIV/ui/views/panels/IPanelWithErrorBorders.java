package pt.quickLabPIV.ui.views.panels;

import java.util.List;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.views.ErrorBorderForComponent;

public interface IPanelWithErrorBorders {
    public List<ErrorBorderForComponent> getBorders();

    public void setAppContext(AppContextModel model);
    
    public void dispose();
}
