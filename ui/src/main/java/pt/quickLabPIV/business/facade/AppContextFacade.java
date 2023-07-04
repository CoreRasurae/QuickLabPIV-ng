package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;

public class AppContextFacade {

    public static ExecutionEnvModel getOrCreateDefaultExecutionEnvModel(AppContextModel appContext) {
        ExecutionEnvModel execModel = appContext.getExecutionEnvironment();
        if (execModel == null) {
            execModel = new ExecutionEnvModel();
            appContext.setExecutionEnvironment(execModel);
        }
        
        return execModel;
    }

}
