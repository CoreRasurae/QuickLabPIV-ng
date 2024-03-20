// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import pt.quickLabPIV.business.facade.IFileFactory;
import pt.quickLabPIV.business.facade.IFileSystemViewFactory;

@XmlRootElement(name = "QuickLabPIVng-AppContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppContextModel {    
    @XmlTransient
    private File projectFile;
    
    @XmlTransient
    private ProjectModel loadedProject;
    
    @XmlTransient
    private SelectedImagesModel selectedImages = new SelectedImagesModel();
    
    @XmlTransient
    private IFileFactory fileFactory;
    
    @XmlTransient
    private IFileSystemViewFactory fileSystemViewFactory;
    
    private ProjectModel project;
    
    private ExecutionEnvModel executionEnv;
    
    public boolean isPendingChanges() {
        if (project == null) {
            return false;
        }
        
        if (loadedProject == null) {
            return true;
        }
        
        return project.isChanged(loadedProject);
    }
    
    public void setProject(ProjectModel _project) {
        project = _project;
        _project.setParent(this);
    }
    
    public ProjectModel getProject() {
        return project;
    }
    
    public void setExecutionEnvironment(ExecutionEnvModel env) {
        executionEnv = env;
        env.setParent(this);
    }
    
    public ExecutionEnvModel getExecutionEnvironment() {
        return executionEnv;
    }
    
    public void setProjectFile(File _projectFile) {
        projectFile = _projectFile;
    }
    
    public File getProjectFile() {
        return projectFile;
    }
    
    public void setLoadedProject(ProjectModel project) {
        loadedProject = project;
        project.setParent(this);
    }
    
    public ProjectModel getLoadedProject() {
        return loadedProject;
    }

    public SelectedImagesModel getSelectedImagesModel() {
        return selectedImages;
    }
    
    public void setFileFactory(IFileFactory _fileFactory) {
       fileFactory = _fileFactory;
    }

    public IFileFactory getFileFactory() {
        return fileFactory;
    }
    
    public void setFileSystemViewFactory(IFileSystemViewFactory _fileSystemViewFactory) {
       fileSystemViewFactory = _fileSystemViewFactory;
    }
    
    public IFileSystemViewFactory getFileSystemViewFactory() {
        return fileSystemViewFactory;
    }
    
    public AppContextModel copy() {
        AppContextModel model = new AppContextModel();
        
        model.projectFile = projectFile;
        model.loadedProject = loadedProject;
        model.selectedImages = selectedImages;
        model.fileFactory = fileFactory;
        model.fileSystemViewFactory = fileSystemViewFactory;
        if (project != null) {
            model.setProject(project.copy());
        }
        if (executionEnv != null) {
            model.setExecutionEnvironment(executionEnv.copy());
        }
        
        return model;
    }
}
