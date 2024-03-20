// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
