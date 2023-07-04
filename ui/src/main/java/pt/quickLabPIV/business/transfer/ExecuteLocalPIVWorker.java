package pt.quickLabPIV.business.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.ExecutionStatus;
import pt.quickLabPIV.IProgressReportObserver;
import pt.quickLabPIV.InputFiles;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVResults;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.ProgressReport;
import pt.quickLabPIV.business.facade.DataExportEnvFacade;
import pt.quickLabPIV.business.facade.PIVConfigurationFacade;
import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.exceptions.InvalidExecutionEnvException;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.iareas.validation.CombinedValidatorAndReplacementConfiguration;
import pt.quickLabPIV.iareas.validation.VectorValidatorFactoryEnum;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.managers.OpenClGpuManager;
import pt.quickLabPIV.maximum.MaximumFinderFactoryEnum;
import pt.quickLabPIV.ui.controllers.DataProcessingEnvFacade;
import pt.quickLabPIV.ui.controllers.Utils;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.DataExportConfigurationModel;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;
import pt.quickLabPIV.ui.views.ExecutionProgressDialog;

public class ExecuteLocalPIVWorker extends SwingWorker<Void, ProgressReport> implements IProgressReportObserver {
    private final static Logger logger = LoggerFactory.getLogger(ExecuteLocalPIVWorker.class);
    private final ExecutionProgressDialog dialog;
    private final AppContextModel appContext;

    public ExecuteLocalPIVWorker(ExecutionProgressDialog _dialog, AppContextModel context) {        
        dialog = _dialog;
        appContext = context;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        executePIV(appContext, PIVContextSingleton.getSingleton(), this, new Properties());
        
        return null;
    }

    static void executePIV(AppContextModel appContext, PIVContextSingleton singleton, IProgressReportObserver observer, final Properties options) {        
        PIVInputParameters pivParameters = singleton.getPIVParameters();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();
        
        final PIVConfigurationConverterVisitor converter = new PIVConfigurationConverterVisitor();
        final ExecutionEnvConverterVisitor execEnvConverter = new ExecutionEnvConverterVisitor(runParameters);
        
        //Check if project has changes and if it has... require user to save project before executing.
        if (appContext.isPendingChanges()) {
            throw new UIException("PIV Processing", "PIV project has pending changes, please save project first.");
        }
        
        if (!DataExportEnvFacade.validateDataExportConfiguration(appContext)) {
            throw new UIException("PIV Processing", "Cannot proceed. Inconsistent configuration was found for the data export environment.");
        }
        
        try {
            DataProcessingEnvFacade.validate(appContext);
        } catch (InvalidExecutionEnvException ex) {            
            ExecutionEnvModel execEnv = appContext.getExecutionEnvironment();
            execEnv = DataProcessingEnvFacade.getDeviceListAndCheckExecutionModelValidity(execEnv);
            appContext.setExecutionEnvironment(execEnv);
        }
        
        try {
            DataProcessingEnvFacade.validate(appContext);
        } catch (InvalidExecutionEnvException ex) {
            throw new UIException("PIV Processing", "Cannot proceed. Inconsistent configuration was found for the execution environment.");
        }

        if (!PIVConfigurationFacade.validateCoherencyWithDataProcessingEnv(appContext)) {
            throw new UIException("PIV Processing", "Cannot proceed. Inconsistent configuration was found for the sub-pixel interpolation mode.");
        }

        if (!PIVConfigurationFacade.validateExportFileStorageLimits(appContext)) {
            throw new UIException("PIV Processing", "Cannot proceed. Current configuration will exceed export file storage limits of 4GB.\n" +
                                                    "Please change the export configuration to split export data into multiple volumes.\n" +
                                                    "The recommended maximum number of maps per exported volume file is: " + PIVConfigurationFacade.getMaximumNumberOfMapsPerVolume(appContext));
        }
        
        converter.setTargetForConversion(pivParameters);
        appContext.getExecutionEnvironment().accept(execEnvConverter);
        appContext.getProject().getPIVConfiguration().accept(converter);
        String outputPathAndFilename = ProjectFacade.computeProjectOutputPathAndFilename(pivParameters, appContext);        
        
        if (pivParameters.getVectorValidatorStrategy() == null || pivParameters.getVectorValidatorStrategy() == VectorValidatorFactoryEnum.None) {
            CombinedValidatorAndReplacementConfiguration[] config = {};
            pivParameters.setSpecificConfiguration(CombinedValidatorAndReplacementConfiguration.IDENTIFIER, config);
            MaximumFinderFactoryEnum maxStrategy = MaximumFinderFactoryEnum.MaximumFinderFromCenter;
            pivParameters.setMaximumFinderStrategy(maxStrategy);
        }
        
        ImageFactoryEnum pixelDepth = ImageFactoryEnum.Image8Bit;
        if (options.containsKey(CommandLineOptionsEnum.PIXEL_DEPTH.key())) {
            int numericPixelDepth = (Integer)options.get(CommandLineOptionsEnum.PIXEL_DEPTH.key());
            switch (numericPixelDepth) {
            case 8:
                logger.info("Forcing 8-bit per pixel depth for PIV images.");
                pixelDepth = ImageFactoryEnum.Image8Bit;
                break;
            case 10:
                logger.info("Forcing 10-bit per pixel depth for PIV images.");
                pixelDepth = ImageFactoryEnum.Image10Bit;
                break;
            case 12:
                logger.info("Forcing 12-bit per pixel depth for PIV images.");
                pixelDepth = ImageFactoryEnum.Image12Bit;
                break;
            case 16:
                logger.info("Forcing 16-bit per pixel depth for PIV images.");
                pixelDepth = ImageFactoryEnum.Image16Bit;
                break;
            default:
            }
        }
        pivParameters.setPixelDepth(pixelDepth);
        
        //Export all cross correlations
        /*pivParameters.setCrossCorrelationDumpMatcher(new ICrossCorrelationDumpMatcher() {
            
            @Override
            public boolean matches(Tile tile) {
                return true;
            }
            
            @Override
            public boolean matches(IterationStepTiles stepTiles) {
                return true;
            }
        });*/
        
        ExecutionStatus execStatus = runParameters.getExecutionStatus();
        execStatus.setReportObserver(observer);
        runParameters.setCancelRequested(false);
        
        //Log velocity inheritance for the specified tiles at the specified adaptive step.
        /*AdaptiveInterVelocityInheritanceFileLogger velocityLogger = null;
        TileMatcher matcher = new TileMatcher(9,4,1,-1,-1);
        velocityLogger = new AdaptiveInterVelocityInheritanceFileLogger("inheritance_log.txt");
        velocityLogger.addRelevantTile(matcher);
        matcher = new TileMatcher(9,3,1,-1,-1);
        velocityLogger.addRelevantTile(matcher);
        matcher = new TileMatcher(8,4,1,-1,-1);
        velocityLogger.addRelevantTile(matcher);

        runParameters.setVelocityInheritanceLogger(velocityLogger);*/
                
        List<List<File>> filesToProcess = PIVConfigurationFacade.getFileListForProcessing(appContext.getProject().getPIVConfiguration());        
        int remainingFilesSize = filesToProcess.get(0).size();
        
        DataExportConfigurationModel dataExportModel = appContext.getProject().getExportConfiguration();
        int mapsPerFile = remainingFilesSize;
        if (dataExportModel.isSplitExports() && dataExportModel.getNumberOfPIVMapsPerExportedFile() < remainingFilesSize) {
            mapsPerFile = dataExportModel.getNumberOfPIVMapsPerExportedFile();
        }
        
        execStatus.setInitialConfiguration(filesToProcess.get(0).size(), outputPathAndFilename);

        List<Integer> offsets = new ArrayList<>(10);
        List<List<File>> filesA = new ArrayList<>(mapsPerFile);
        List<List<File>> filesB = new ArrayList<>(mapsPerFile);
        
        int startOffset = 0;
        while (remainingFilesSize > 0) {
            int filesToAdd = mapsPerFile;
            if (remainingFilesSize < mapsPerFile) {
                filesToAdd = remainingFilesSize;
            }
            filesA.add(filesToProcess.get(0).subList(startOffset, startOffset + filesToAdd));
            filesB.add(filesToProcess.get(1).subList(startOffset, startOffset + filesToAdd));
            offsets.add(startOffset);
            
            remainingFilesSize -= filesToAdd;
            startOffset += filesToAdd;
        }
        offsets.add(startOffset);

        String currentOutputPathAndFilename = outputPathAndFilename;
        StructMultiFrameFloatVelocityExporter exporter = null;
        for (int index = 0; index < filesA.size(); index++) {
            InputFiles inputFiles = new InputFiles(offsets.get(index), 0, filesA.get(index), filesB.get(index));
            
            //Moved here because Windows can fail opening the export file after opening large amount of image files
            //for PIV processing (around 4500).
            exporter = new StructMultiFrameFloatVelocityExporter();
            String nextFilename = null;
            if (filesA.size() > 1) {
                String volumeFileEndingName = "_MV" + index + ".mat";
                String nextVolumeFileEndingName = "_MV" + (index + 1) + ".mat";
                currentOutputPathAndFilename = outputPathAndFilename.replaceFirst("_MV0.mat", volumeFileEndingName);
                 
                if (index != filesA.size() - 1) {
                    nextFilename = pivParameters.getOutputFilename().replaceFirst("_MV0.mat", nextVolumeFileEndingName);
                }
            }
            pivParameters.setNextFilename(nextFilename);
            
            exporter.openFile(currentOutputPathAndFilename);
            singleton.getPIVRunParameters().setExporter(exporter);
            
            System.out.println("Started");
            OpenClGpuManager managerJob = new OpenClGpuManager(inputFiles);
            managerJob.analyze();
            System.out.println("Started computing");
            managerJob.compute();
            
            PIVResults results = managerJob.getJobResult(JobResultEnum.JOB_RESULT_PIV);
            try {
                if (runParameters.isCancelRequested()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showConfirmationDialog(null, "QuickLab PIV", 
                                    "PIV processing was successfully cancelled.");
                        }                    
                    });
                } else {
                    exporter.exportDataToFile(results);
                }
            } finally {
                exporter.closeFile();
                singleton.getPIVRunParameters().setExporter(null);
            }

            results.clear();
        }        
    }
    
    @Override
    public void receiveUpdatedProgressReport(ProgressReport report) {
        publish(report.copy());
    }
    
    @Override
    protected void process(List<ProgressReport> chunks) {
        if (dialog != null) {
        	System.out.println("Updating with chunk...");
            dialog.updateWithReport(chunks.get(chunks.size()-1));
        }
    }

    public void requestCancellation() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();        
        singleton.cancelExecution();
    }

}
