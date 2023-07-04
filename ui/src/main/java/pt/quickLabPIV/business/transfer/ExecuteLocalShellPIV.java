package pt.quickLabPIV.business.transfer;

import java.util.Properties;

import pt.quickLabPIV.IProgressReportObserver;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.ProgressReport;
import pt.quickLabPIV.ui.models.AppContextModel;

public class ExecuteLocalShellPIV implements IProgressReportObserver {
    private AppContextModel appContext;
    private Properties options;

    public ExecuteLocalShellPIV(AppContextModel _appContext, Properties _options) {
        appContext = _appContext;
        options = _options;
    }
    
    public void execute() {        
        ExecuteLocalPIVWorker.executePIV(appContext, PIVContextSingleton.getSingleton(), this, options);
    }

    @Override
    public void receiveUpdatedProgressReport(ProgressReport report) {
        System.out.println(report.toString());
    }

}
