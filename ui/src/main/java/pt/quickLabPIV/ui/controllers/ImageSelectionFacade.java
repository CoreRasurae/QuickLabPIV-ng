package pt.quickLabPIV.ui.controllers;

import java.awt.Component;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ImageOnlyFileFilter;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.PIVImageTypeEnum;
import pt.quickLabPIV.ui.views.PIVImageFileDialog;

public class ImageSelectionFacade {

   public static AppContextModel showImageFolderSelectionDialog(JDialog parent, AppContextModel context) {
       PIVImageFileDialog dialog = new PIVImageFileDialog(parent);
       dialog.setAppContext(context);
       dialog.pack();
       dialog.setVisible(true);
       if (dialog.isCanceled()) {
           return context;
       }
       
       AppContextModel result = dialog.getAppContext();
       PIVConfigurationModel config = ProjectFacade.getOrCreateDefaultPIVConfiguration(context);
       
       if (dialog.isFileAndFolderMode()) {
           boolean shouldImport = askForPatternImportIfNeeded(parent, config);
           try {
               context = ProjectFacade.readImageFile(context);
           } catch (UIException e) {
               JOptionPane.showMessageDialog(parent, e.getTitleMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
           }
           
           if (shouldImport) {
               //First import patternB so that listeners receive this update first,
               //so that validators can validate patternA update.
               config.setImagePatternB(config.getSourceImageFile().getName());
               config.setImagePatternA(config.getSourceImageFile().getName());               
           }
       }
       
       return result;
   }

   public static AppContextModel showFirstImageFileChooser(Component parent, AppContextModel appContext) {
       if (appContext == null || appContext.getProject() == null || appContext.getProject().getPIVConfiguration() == null ||
           appContext.getProject().getPIVConfiguration().getSourceImageFolder() == null) {           
           throw new UIException("PIV image selection", "Please select a valid source image folder first");
       }
       
       PIVConfigurationModel config = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
       
       File sourceFolder = config.getSourceImageFolder();
       File sourceFile = config.getSourceImageFile();
       
       JFileChooser fc = new JFileChooser(appContext.getFileSystemViewFactory().createView());
       fc.setAcceptAllFileFilterUsed(false);
       fc.setCurrentDirectory(sourceFolder);
       if (sourceFile != null) {
           fc.setSelectedFile(sourceFile);
       }
       fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
       fc.setDialogType(JFileChooser.OPEN_DIALOG);
       fc.setMultiSelectionEnabled(false);
       fc.setFileFilter(new ImageOnlyFileFilter());
       fc.setControlButtonsAreShown(true);
       fc.setDialogTitle("Select first image");
       int result = fc.showDialog(parent, "Select");
       
       if (result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
           config.setSourceImageFile(fc.getSelectedFile());
           try {
               appContext = ProjectFacade.readImageFile(appContext);
           } catch (UIException e) {
               JOptionPane.showMessageDialog(parent, e.getTitleMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
           }
           
           boolean shouldImport = askForPatternImportIfNeeded(parent, config);
           
           if (shouldImport) {
               //First import patternB so that listeners receive this update first,
               //so that validators can validate patternA update.
               config.setImagePatternB(fc.getSelectedFile().getName());
               config.setImagePatternA(fc.getSelectedFile().getName());
           }
       }
       
       return appContext;
   }

   public static AppContextModel showImageMaskFileChooser(Component parent, AppContextModel appContext) {
       if (appContext == null || appContext.getProject() == null || appContext.getProject().getPIVConfiguration() == null ||
           appContext.getProject().getPIVConfiguration().getSourceImageFolder() == null) {           
           throw new UIException("PIV image mask selection", "Please select a valid source image folder first");
       }
       
       PIVConfigurationModel config = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
       
       File sourceFolder = config.getSourceImageFolder();
       File sourceFile = config.getMaskFile();
       
       JFileChooser fc = new JFileChooser(appContext.getFileSystemViewFactory().createView());
       fc.setAcceptAllFileFilterUsed(false);
       fc.setCurrentDirectory(sourceFolder);
       if (sourceFile != null) {
           fc.setSelectedFile(sourceFile);
       }
       fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
       fc.setDialogType(JFileChooser.OPEN_DIALOG);
       fc.setMultiSelectionEnabled(false);
       fc.setFileFilter(new ImageOnlyFileFilter());
       fc.setControlButtonsAreShown(true);
       fc.setDialogTitle("Select mask image");
       int result = fc.showDialog(parent, "Select");
       
       if (result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
           config.setMaskFile(fc.getSelectedFile());
       }
       
       return appContext;
   }
   
    private static boolean askForPatternImportIfNeeded(Component parent, PIVConfigurationModel config) {
        boolean askForPatternImport = false;
        if (config.getImageType() == PIVImageTypeEnum.PIVImageSequence
                && (config.getImagePatternA() != null && !config.getImagePatternA().isEmpty())) {
            askForPatternImport = true;
        }
        if (config.getImageType() == PIVImageTypeEnum.PIVImagePair
                && ((config.getImagePatternA() != null && !config.getImagePatternA().isEmpty())
                        || (config.getImagePatternB() != null && !config.getImagePatternB().isEmpty()))) {
            askForPatternImport = true;
        }

        boolean shouldImport = true;
        if (askForPatternImport) {
            int importResult = Utils.showOptionDialog(parent, "PIV Image selection",
                    "Should image pattern name A/B also be imported?");
            if (importResult != JOptionPane.YES_OPTION) {
                shouldImport = false;
            }
        }
        return shouldImport;
    }
}
