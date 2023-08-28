// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.DataExportConfigurationModel;

public class DataExportEnvFacade {
    private static AppContextModel createDefaultDataExportConfiguration(AppContextModel appContext) {
        DataExportConfigurationModel config = new DataExportConfigurationModel();
        appContext.getProject().setExportConfiguration(config);
        return appContext;
    }

    public static DataExportConfigurationModel getOrCreateDefaultDataExportConfiguration(AppContextModel appContext) {
        DataExportConfigurationModel configuration = appContext.getProject().getExportConfiguration();
        if (configuration == null) {
            appContext = createDefaultDataExportConfiguration(appContext);
            configuration = appContext.getProject().getExportConfiguration();
        }

        return configuration;
    }
    
    public static boolean validateDataExportConfiguration(AppContextModel appContext) {
        if (appContext.getProject() == null) {
            return false;
        }
        
        if (appContext.getProject().getExportConfiguration() == null) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isMultiVolumeExport(AppContextModel appContext) { 
        DataExportConfigurationModel model = getOrCreateDefaultDataExportConfiguration(appContext);
        return model.isSplitExports();
    }

    public static long getNumberOfImagerPerMultiVolumeFile(AppContextModel appContext) {
        DataExportConfigurationModel model = getOrCreateDefaultDataExportConfiguration(appContext);
        return model.getNumberOfPIVMapsPerExportedFile();
    }
}
