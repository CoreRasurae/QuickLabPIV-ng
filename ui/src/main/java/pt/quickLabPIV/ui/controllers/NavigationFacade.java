package pt.quickLabPIV.ui.controllers;

import java.awt.Component;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.business.facade.AppContextFactory;
import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.business.facade.ProjectOpenException;
import pt.quickLabPIV.business.facade.ProjectSaveException;
import pt.quickLabPIV.business.transfer.ExecuteLocalPIVWorker;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ProjectModel;
import pt.quickLabPIV.ui.views.AboutDialog;
import pt.quickLabPIV.ui.views.CreateNewOrEditProjectDialog;
import pt.quickLabPIV.ui.views.CreateNewProjectWithChangesDialog;
import pt.quickLabPIV.ui.views.DataExportEnvironmentConfigurationDialog;
import pt.quickLabPIV.ui.views.DataProcessingEnvironmentConfiguration;
import pt.quickLabPIV.ui.views.ExecutionProgressDialog;
import pt.quickLabPIV.ui.views.PIVConfigurationDialog;
import pt.quickLabPIV.ui.views.PIVImageSelection;
import pt.quickLabPIV.ui.views.ProjectOpenDialog;
import pt.quickLabPIV.ui.views.CreateNewOrEditProjectDialog.CreateOrEditMode;
import pt.quickLabPIV.ui.views.PIVConfigurationDialog.PIVConfigurationTabEnum;
import pt.quickLabPIV.ui.views.PIVImagePreProcessingDialog;
import pt.quickLabPIV.ui.views.PIVImagePreProcessingDialog.PIVImagePreProcessingTabEnum;
import pt.quickLabPIV.ui.views.panels.ProjectPreviewPanel;

public class NavigationFacade {
    private static Logger logger = LoggerFactory.getLogger(NavigationFacade.class);
    
    public static AppContextModel createNewProject(AppContextModel appContextModel) {
        boolean createNewProject = true;
        if (appContextModel != null && appContextModel.isPendingChanges()) {
            createNewProject = shouldCreateNewProjectWithChangesDialog();
        }
        
        if (createNewProject) {
            CreateNewOrEditProjectDialog dialog = new CreateNewOrEditProjectDialog();
            dialog.setCreateOrEditMode(CreateOrEditMode.CREATE);
            dialog.pack();
            dialog.setMinimumSize(dialog.getSize());
            dialog.setVisible(true);
            if (!dialog.isCanceled()) {
                appContextModel = ProjectFacade.createNewProject(dialog.getProject());
            }
        }
        
        return appContextModel;
    }
    
    public static AppContextModel editProject(Component parent, AppContextModel appContextModel) {
        if (appContextModel == null || appContextModel.getProject() == null) {
            JOptionPane.showMessageDialog(parent, "Please create a project first.", "Edit project", JOptionPane.INFORMATION_MESSAGE);
            return appContextModel;
        }
        
        ProjectModel project = appContextModel.getProject();
        ProjectModel projectForEditing = project.copy();
        CreateNewOrEditProjectDialog dialog = new CreateNewOrEditProjectDialog();
        dialog.setCreateOrEditMode(CreateOrEditMode.EDIT);
        dialog.setProject(projectForEditing);
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(parent); //Center dialog on parent frame
        dialog.setVisible(true);
        if (!dialog.isCanceled() && project.isChanged(projectForEditing)) {
            appContextModel.setProject(projectForEditing);
            appContextModel = AppContextFactory.completeModel(appContextModel);
        }
        
        return appContextModel;
    }
    
    public static boolean shouldCreateNewProjectWithChangesDialog() {
        CreateNewProjectWithChangesDialog dialog = new CreateNewProjectWithChangesDialog();
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setVisible(true);
        return dialog.isCreateNew();
    }

    public static AppContextModel saveProject(JComponent parent, AppContextModel appContextModel) {
        return saveProject(parent, appContextModel, false);
    }
    
    public static AppContextModel saveProject(JComponent parent, AppContextModel appContextModel, boolean saveAsMode) {
        final FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory() || f.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Project files (*.xml)";
            }
        };
        
        File projectFile = null;
        File currentDirectory = new File(System.getProperty("user.dir"));
        if (appContextModel.getProjectFile() == null || saveAsMode) {
            boolean repeat = true;
            while (repeat) {
                JFileChooser fileChooserDialog = new JFileChooser(currentDirectory);
                fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooserDialog.setDialogTitle("Save project");
                fileChooserDialog.setMultiSelectionEnabled(false);
                fileChooserDialog.setAcceptAllFileFilterUsed(false);
                fileChooserDialog.setDialogType(JFileChooser.SAVE_DIALOG);
                fileChooserDialog.setFileFilter(fileFilter);  
               int status = fileChooserDialog.showSaveDialog(parent);
               if (status != JFileChooser.APPROVE_OPTION) {
                   return appContextModel;
               }
            
               projectFile = fileChooserDialog.getSelectedFile();
               if (!projectFile.getAbsolutePath().toLowerCase().endsWith(".xml")) {
                   projectFile = new File(projectFile.getAbsolutePath()+ ".xml");
               }
               currentDirectory = fileChooserDialog.getCurrentDirectory();
               
               if (projectFile.exists()) {
                   int selection = Utils.showOptionDialog(parent, "Project Save", "File already exists.\nDo you wish to overwrite it?");
                   if (selection == JOptionPane.CANCEL_OPTION) {
                       return appContextModel;
                   } else if (selection == JOptionPane.YES_OPTION) {
                       repeat = false;
                   }
               } else {
                   repeat = false;
               }
            }
            appContextModel.setProjectFile(projectFile);
        }
        
        AppContextModel result = appContextModel;
        try {
            result = ProjectFacade.saveProject(appContextModel);
        } catch (ProjectSaveException e) {
            logger.error("Failed to save project: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(parent, e.getMessage(), e.getTitleMessage(), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
            result.setProjectFile(null);
        }
        
        return result;
    }

    public static AppContextModel saveProjectAs(JComponent parent, AppContextModel appContext) {       
        return saveProject(parent, appContext, true);
    }
    
    public static AppContextModel loadProject(AppContextModel appContextModel) {
        ProjectOpenDialog openDialog = new ProjectOpenDialog();
        openDialog.setAppContextModel(appContextModel);
        openDialog.pack();
        openDialog.setMinimumSize(openDialog.getSize());
        openDialog.setVisible(true);
        
        AppContextModel result = appContextModel;
        
        //Once modal form is closed
        File projectFile = openDialog.getSelectedProjectFile();
        if (projectFile == null) {
            return result;
        }
        
        try {
            result = ProjectFacade.loadProject(projectFile);
        } catch (ProjectOpenException e) {
            logger.error("Failed to load project: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(openDialog, e.getMessage(), e.getTitleMessage(), JOptionPane.INFORMATION_MESSAGE);
        }
        
        return result;
    }

    public static void previewProject(ProjectPreviewPanel projectPreviewPanel, File selectedProject) {
        if (selectedProject == null) {
            projectPreviewPanel.setPreviewDetails("", "", "", "");
        }
        
        try {
            AppContextModel previewModel = ProjectFacade.loadProject(selectedProject, true);
            ProjectModel project = previewModel.getProject();
            projectPreviewPanel.setPreviewDetails(project.getTitle(), project.getDate(), project.getDescription(), project.getProjectType().name());
        } catch (ProjectOpenException e) {
            //For preview purposes ignore the exception
            projectPreviewPanel.setPreviewDetails("Invalid project file", "", "Couldn't preview selected file", "");
        }
        
    }

    public static boolean projectConfirmOpenWithPendingChanges(Component parentComponent, AppContextModel appContextModel) {
        //Check if a project is open with pending changes...
        if (appContextModel != null && appContextModel.isPendingChanges()) {
            int selection = Utils.showOptionDialog(parentComponent, "Load Project", 
                    "Current project has unsaved changes.\n" +
                    "Unsaved changes will be discarded! Do you wish to proceed?");
            if (selection != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }

    public static void projectLoadFileWithNoProjectFileSelected(Component parentComponent) {
        JOptionPane.showMessageDialog(parentComponent, "Please select a project file to open.", "Open Project", JOptionPane.INFORMATION_MESSAGE);
    }

    public static AppContextModel showPIVConfiguration(JFrame frame, AppContextModel appContextModel,
            PIVConfigurationTabEnum startTab) {
        if (appContextModel == null) {
            return appContextModel;
        }
        
        PIVConfigurationDialog dialog = new PIVConfigurationDialog();
        dialog.setAppContext(appContextModel.copy());
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.selectTab(startTab);
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);
        
        if (!dialog.isCancelled()) {
            appContextModel = dialog.getAppContext();
        }
        
        return appContextModel;
    }

    public static AppContextModel showImagePreProcessingDialog(JFrame frame,
            AppContextModel appContextModel, PIVImagePreProcessingTabEnum startTab) {
        if (appContextModel == null) {
            return appContextModel;
        }
        
        PIVImagePreProcessingDialog dialog = new PIVImagePreProcessingDialog();
        dialog.setAppContext(appContextModel.copy());
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.selectTab(startTab);
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);
        
        if (!dialog.isCanceled()) {
            appContextModel = dialog.getAppContext();
        }
        
        return appContextModel;
    }
    
    public static void setLookAndFeel() {
        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            //MetalLookAndFeel.setCurrentTheme();
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.NimbusLookAndFeel");
            UIManager.setLookAndFeel(new javax.swing.plaf.nimbus.NimbusLookAndFeel());
            
        /*} catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();*/
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static AppContextModel showPIVImageSelectionDialog(JFrame frame, AppContextModel appContextModel) {
        PIVImageSelection dialog = new PIVImageSelection();
        dialog.setAppContext(appContextModel.copy());
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);
        if (!dialog.isCanceled()) {
            return dialog.getAppContext();
        }
        return appContextModel;
    }

    public static AppContextModel showAboutDialog(JFrame frame, AppContextModel appContextModel) {
        AboutDialog dialog = new AboutDialog();
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);

        return appContextModel;
    }

    public static AppContextModel executeProject(JFrame parent, AppContextModel appContext) {
        //TODO Check if project is saved
        
        //Check if output file will overwritten
        String outputPathAndFilename = ProjectFacade.computeProjectOutputPathAndFilename(null, appContext);
        File file = appContext.getFileFactory().createFile(outputPathAndFilename);
        if (file.exists()) {
            int option = Utils.showOptionDialog(parent, "PIV Processing", 
                    "Output file " + outputPathAndFilename + " already exists.\n" +
                    "Do you wisth to overwrite it?");
            if (option != JOptionPane.YES_OPTION) {
                return appContext;
            }
        }
        
        ExecutionProgressDialog dialog = new ExecutionProgressDialog(parent);         
        ExecuteLocalPIVWorker localWorker = new ExecuteLocalPIVWorker(dialog, appContext);
        localWorker.execute();  
        dialog.startTrackingThread(localWorker);
        dialog.setVisible(true);
        return appContext;
    }

    public static AppContextModel configDataExportEnv(JFrame frame, AppContextModel appContextModel) {
        DataExportEnvironmentConfigurationDialog dialog = new DataExportEnvironmentConfigurationDialog();
        dialog.setAppContext(appContextModel.copy());
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
            return dialog.getAppContext();
        }
        return appContextModel;
    }

    public static AppContextModel configProcessingEnv(JFrame frame, AppContextModel appContextModel) {
        DataProcessingEnvironmentConfiguration dialog = new DataProcessingEnvironmentConfiguration();
        dialog.setAppContext(appContextModel.copy());
        dialog.setMinimumSize(dialog.getSize());
        dialog.pack();
        dialog.setLocationRelativeTo(frame); //Center dialog on parent frame
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
            return dialog.getAppContext();
        }
        return appContextModel;
    }

}
