package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.models.AppContextModel;

public class AppContextFactory {
    public static AppContextModel completeModel(AppContextModel model) {
        if (model == null || model.getProject() == null) {
            throw new UIException("Cannot complete project model", "Invalid project state");
        }      
        
        switch (model.getProject().getProjectType()) {
        case Local:
            model.setFileFactory(new LocalFileFactory());
            model.setFileSystemViewFactory(new LocalFileSystemViewFactory());
            break;
        case Remote:
            model.setFileFactory(new RemoteFileFactory());
            model.setFileSystemViewFactory(new RemoteFileSystemViewFactory());
            break;
        default:
            throw new UIException("Unknown project type","Cannot complete application context from project");
        }
        
        return model;
    }
}
 