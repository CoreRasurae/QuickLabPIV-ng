// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Project")
@XmlType(propOrder = {"title", "date", "description", "projectType", "remoteServer", "pivConfiguration", "exportConfiguration"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectModel {
    @XmlTransient
    private AppContextModel context;
    
    private String title;
    private String date;
    private String description;
    private LocalRemoteEnum projectType;
    private String remoteServer;
    @XmlElement(name="piv-configuration")
    private PIVConfigurationModel pivConfiguration;
    
    @XmlElement(name="export-configuration")
    private DataExportConfigurationModel exportConfiguration;
    
    public void setParent(AppContextModel model) {
        context = model;
        //Ensure that parent-child relationship is correctly set, since JAXB won't call the setter setPIVConfiguration(...), since is using field based attributes.
        if (pivConfiguration != null && pivConfiguration.getParent() == null) {
        	pivConfiguration.setParent(this);
        }
    }
    
    public AppContextModel getParent() {
        return context;
    }
    
    public void setTitle(String _title) {
        title = _title;
        if (title == null) {
            title = "";
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setDescription(String _description) {
        description = _description;
        if (description == null) {
            description = "";
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setPIVConfiguration(PIVConfigurationModel config) {
        pivConfiguration = config;
        config.setParent(this);
    }
    
    public PIVConfigurationModel getPIVConfiguration() {
        return pivConfiguration;
    }

    public void setExportConfiguration(DataExportConfigurationModel config) {
        exportConfiguration = config;
    }
    
    public DataExportConfigurationModel getExportConfiguration() {
        return exportConfiguration;
    }
    
    public void setDate(String _date) {
        date = _date;
        if (date == null) {
            date = "";
        }
    }
    
    public String getDate() {
        return date;
    }

    public LocalRemoteEnum getProjectType() {
        return projectType;
    }

    public void setProjectType(LocalRemoteEnum _projectType) {
        projectType = _projectType;
    }

    public String getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServer(String _remoteServer) {
        remoteServer = _remoteServer;
        if (remoteServer == null) {
            remoteServer = "";
        }
    }
    
    public ProjectModel copy() {
        if (title == null) {
            title = "";
        }
        
        if (description == null) {
            description = "";
        }
        
        if (date == null) {
            date = "";
        }
        
        if (remoteServer == null) {
            remoteServer = "";
        }

        ProjectModel model = new ProjectModel();
        model.title = title;
        model.date = date;
        model.description = description;
        model.projectType = projectType;
        model.remoteServer = remoteServer;
        if (pivConfiguration != null) {
            model.setPIVConfiguration(pivConfiguration.copy());
            
        }
        
        if (exportConfiguration != null) {
            model.setExportConfiguration(exportConfiguration.copy());
        }
        
        return model;
    }
    
    public boolean isChanged(ProjectModel another) {
        boolean changed = false;
        if (title == null) {
            title = "";
        }
        
        if (description == null) {
            description = "";
        }
        
        if (date == null) {
            date = "";
        }
        
        if (remoteServer == null) {
            remoteServer = "";
        }
        
        if (!title.equals(another.title)) {
            changed = true;
        }

        if (!date.equals(another.date)) {
            changed = true;
        }
        
        if (!description.equals(another.description)) {
            changed = true;
        }
        
        if (projectType != another.projectType) {
            changed = true;
        }
        
        if (!remoteServer.equals(another.remoteServer)) {
            changed = true;
        }

        if (pivConfiguration != another.pivConfiguration) {        
            if (pivConfiguration == null && another.pivConfiguration != null) {
                changed = true;
            }

            if (pivConfiguration != null && another.pivConfiguration == null) {
                changed = true;
            }

            if (pivConfiguration.isChanged(another.pivConfiguration)) {
                changed = true;
            }
        }
        
        if (exportConfiguration != another.exportConfiguration) {
            if (exportConfiguration == null && another.exportConfiguration != null) {
                changed = true;
            }

            if (exportConfiguration != null && another.exportConfiguration == null) {
                changed = true;
            }

            if (exportConfiguration.isChanged(another.exportConfiguration)) {
                changed = true;
            }            
        }
        
        return changed;
    }
}
