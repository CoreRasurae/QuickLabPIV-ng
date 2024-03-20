// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.business.facade;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.ui.controllers.ImageIOException;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.PIVConfigurationModel;
import pt.quickLabPIV.ui.models.ProjectModel;
import pt.quickLabPIV.ui.models.SelectedImagesModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationModeEnum;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsBiCubicModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian1DModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsGaussian2DModel;
import pt.quickLabPIV.ui.models.SubPixelInterpolationOptionsModel;

public class ProjectFacade {
    public static AppContextModel createNewProject(ProjectModel projectModel) {
        AppContextModel model = new AppContextModel();
        model.setProject(projectModel);
        AppContextFactory.completeModel(model);
        
        return model;
    }
    
    public static AppContextModel saveProject(AppContextModel context) {
        try {
            File destinationFile = context.getProjectFile();
            FileOutputStream fos = new FileOutputStream(destinationFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            JAXBContext jaxbContext = JAXBContext.newInstance(AppContextModel.class,
                    SubPixelInterpolationOptionsGaussian2DModel.class, 
                    SubPixelInterpolationOptionsBiCubicModel.class,
                    SubPixelInterpolationOptionsGaussian1DModel.class);
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(context, bos);
            bos.flush();
            bos.close();
        } catch (JAXBException e) {
            throw new ProjectSaveException("Couldn't save project", "There was a XML export problem.\n" + 
                    "Project: " + context.getProject().getTitle() +
                    "\nFile: " + context.getProjectFile().getAbsolutePath(), e);
        } catch (FileNotFoundException e) {
            throw new ProjectSaveException("Couldn't save project", "Path is invalid, or user has no write permissions.\n" + 
                    "Project: " + context.getProject().getTitle() +
                    "\nFile: " + context.getProjectFile().getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ProjectSaveException("Couldn't save project", "There was a problem writing to the file.\n" +  
                    "Project: " + context.getProject().getTitle() +
                    "\nFile: " + context.getProjectFile().getAbsolutePath(), e);
        }
        
        context.setLoadedProject(context.getProject());
        context.setProject(context.getProject().copy());
        return context;
    }

    public static AppContextModel loadProject(File sourceFile) {
        return loadProject(sourceFile, false);
    }
    
    public static AppContextModel loadProject(File sourceFile, boolean previewProject) {
    	AppContextModel context = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AppContextModel.class, 
                    SubPixelInterpolationOptionsGaussian2DModel.class, 
                    SubPixelInterpolationOptionsBiCubicModel.class,
                    SubPixelInterpolationOptionsGaussian1DModel.class);
            Unmarshaller unm = jaxbContext.createUnmarshaller();
            context = (AppContextModel)unm.unmarshal(sourceFile);
        } catch (JAXBException e) {
            throw new ProjectOpenException("Couldn't open project", "There was a XML import problem.\n" + 
                    "File: " + sourceFile.getAbsolutePath(), e);
        }
        
        context = AppContextFactory.completeModel(context);
        context.setProjectFile(sourceFile);
        context.setLoadedProject(context.getProject().copy());
        //Ensure parent-child relationship is complete, because JAXB, doens't call the setters (setProject, setPIVConfiguration, etc),
        //when using field based attributes 
        context.setProject(context.getProject());
        
        try {
            context = readImageFile(context, false);
        } catch (ImageIOException e) {
              //Intentionally ignore failed image read on project load   
        }
        
        return context;
    }

    public static AppContextModel updateSourceImage(AppContextModel appContext, boolean selectFirstImage,
            File selectedSource) {
        PIVConfigurationModel model = appContext.getProject().getPIVConfiguration();
        if (model == null) {
            appContext = createDefaultPIVConfiguration(appContext);
            model = appContext.getProject().getPIVConfiguration();
        }
        if (selectFirstImage) {
            String path = selectedSource.getAbsolutePath();
            path = path.substring(0, path.lastIndexOf(File.separatorChar));
            File folder = new File(path);
            model.setSourceImageFolder(folder);
            model.setSourceImageFile(selectedSource);
            
        } else {
            model.setSourceImageFolder(selectedSource);
            
        }
        return appContext;
    }

    private static AppContextModel createDefaultPIVConfiguration(AppContextModel appContext) {
        PIVConfigurationModel config = new PIVConfigurationModel();
        appContext.getProject().setPIVConfiguration(config);
        return appContext;
    }

    public static PIVConfigurationModel getOrCreateDefaultPIVConfiguration(AppContextModel appContext) {
        PIVConfigurationModel configuration = appContext.getProject().getPIVConfiguration();
        if (configuration == null) {
            appContext = createDefaultPIVConfiguration(appContext);
            configuration = appContext.getProject().getPIVConfiguration();
        }

        return configuration;
    }
    
    public static AppContextModel readImageFile(AppContextModel appContext) {
        return readImageFile(appContext, true);
    }
    
    public static AppContextModel readImageFile(AppContextModel appContext, boolean updateImageResolution) {
        //TODO needs to handle remote call to read file as well
        SelectedImagesModel imageModel = appContext.getSelectedImagesModel();
        PIVConfigurationModel model = ProjectFacade.getOrCreateDefaultPIVConfiguration(appContext);
        
        if (model.getSourceImageFile() != null) {
            BufferedImage bi;
            try {
                bi = ImageIO.read(model.getSourceImageFile());
            } catch (IOException e) {
                throw new ImageIOException("Cannot read image", "Failed to read image: " + model.getSourceImageFile().getAbsolutePath(), e);
            }
            imageModel.setImage(bi);
            if (bi != null) {
                model.setImageHeight(bi.getHeight());
                model.setImageWidth(bi.getWidth());
            } else {
                model.setImageHeight(0);
                model.setImageWidth(0);
            }
        }
        
        return appContext;
    }
    
    public static String computeProjectOutputPathAndFilename(PIVInputParameters pivParameters, AppContextModel appContext) {
        String outputPath = appContext.getProjectFile().getParent();
        String projectOutputFilename = appContext.getProjectFile().getName();
        int lastIndexFileExtension = projectOutputFilename.lastIndexOf('.');
        if (lastIndexFileExtension == -1) {
            lastIndexFileExtension = projectOutputFilename.length();
        }
        
        
        if (DataExportEnvFacade.isMultiVolumeExport(appContext)) {
            projectOutputFilename = "velocities_" + projectOutputFilename.substring(0, lastIndexFileExtension) + "_MV0.mat";
        } else {
            projectOutputFilename = "velocities_" + projectOutputFilename.substring(0, lastIndexFileExtension) + ".mat";
        }

        if (pivParameters != null) {
            if (outputPath == null) {
                //This happens if the project file is in the same folder as the application program, 
                //thus replace null with current folder.
                outputPath = ".";
            }
            pivParameters.setOutputPath(outputPath);
            pivParameters.setOutputFilename(projectOutputFilename);
        }               
        
        return outputPath + File.separator + projectOutputFilename;
    }    
}
