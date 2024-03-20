// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.business.facade.PIVExecutionEnvironmentFacade;
import pt.quickLabPIV.business.transfer.TransferFacade;
import pt.quickLabPIV.exceptions.InvalidExecutionEnvException;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.CPUCoresComboBoxModel;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;
import pt.quickLabPIV.ui.models.OpenCLDeviceModel;

public class DataProcessingEnvFacade {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessingEnvFacade.class);

    public static ExecutionEnvModel getDeviceListAndCheckExecutionModelValidity(ExecutionEnvModel execModel) {
        List<OpenCLDeviceModel> devices = PIVExecutionEnvironmentFacade.getAvailableGpuDevicesAsModels();
        int totalRealCores = getTotalNumberOfRealCores();
        execModel = checkExecutionModelValidityOnLoad(execModel, devices, totalRealCores);
        
        return execModel;
    }

    private static ExecutionEnvModel checkExecutionModelValidityOnLoad(ExecutionEnvModel execModel, List<OpenCLDeviceModel> newlyDetectedDevices, int totalRealCores) {
        final class IDMap {
            long oldId;
            long newId;
        }
        
        if (execModel == null) {
            execModel = new ExecutionEnvModel();
        }
        
        int threads = execModel.getCpuThreads();
        if (totalRealCores < threads ) {
            threads = totalRealCores;
            execModel.setCpuThreads(threads);
        }
                
        List<OpenCLDeviceModel> oldDevices = execModel.getOpenClDevices();
        //The issue here is that OpenCL device IDs are not consistent across reboots, so we need to keep a translation table.
        List<IDMap> idMaps = new ArrayList<>(oldDevices.size());
        
        //Keep old devices configurations, with the exception that if the newly detected corresponding compatible device
        //is found and migrates from a default configuration to a specific configuration, then it must be updated,
        //however the current selection state must be preserved.
        //A device may have to be removed if it is on longer detected.
        int currentIdx = 0;
        while (currentIdx < oldDevices.size()) {
            for (; currentIdx < oldDevices.size(); currentIdx++) {
                OpenCLDeviceModel oldModel = oldDevices.get(currentIdx);
                boolean found = false;
                for (int j = 0; j < newlyDetectedDevices.size(); j++) {
                    OpenCLDeviceModel newModel = newlyDetectedDevices.get(j);
                    if (oldModel.isCompatible(newModel) && !idMaps.stream().anyMatch(map -> map.newId == newModel.getId())) {                        
                        found = true;
                        
                        IDMap idmap = new IDMap();
                        idmap.newId = newModel.getId();
                        idmap.oldId = oldModel.getId();
                        idMaps.add(idmap);
                        
                        oldModel.setId(newModel.getId());
                        
                        if (oldModel.isDefaultConfig() && !newModel.isDefaultConfig()) {
                            //Update configuration if required...
                            if (oldModel.isSelected()) {
                                newModel.setSelected(true);
                            } else {
                                newModel.setSelected(false);
                            }
                            oldDevices.set(currentIdx, newModel);
                        }
                        break;
                    }
                }
                
                if (!found) {
                    //Remove no longer found devices
                    oldDevices.remove(currentIdx);
                    break;
                }
            }
        }
        
        //Now check if there are new devices that need to be added
        List<OpenCLDeviceModel> newModels = new ArrayList<>(2);
        for (int i = 0; i < newlyDetectedDevices.size(); i++) {
            OpenCLDeviceModel model = newlyDetectedDevices.get(i);
            boolean found = false;
            for (IDMap map : idMaps) {
                if (map.newId == model.getId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newModels.add(model);
            }
        }
        oldDevices.addAll(newModels);
        
        execModel.setOpenClDevices(oldDevices);
        if (oldDevices.size() == 0) {
            logger.warn("Disabling OpenCL since no OpenCL capable devices are available. This may cause configuration issues.");
            execModel.setEnableOpenCL(false);
            return execModel;
        }
                
        List<Long> oldOpenClAssignments = execModel.getOpenClAssignments();
        List<Boolean> oldValidAssignments = execModel.getValidAssignments();
        if (oldOpenClAssignments.size() == 0) {
            return execModel;
        }

        for (int idx = 0; idx < oldOpenClAssignments.size(); idx++) {
            Long oldOpenClID = oldOpenClAssignments.get(idx);
            boolean valid = false;
            for (IDMap map : idMaps) {
                if (map.oldId == oldOpenClID) {
                    oldOpenClAssignments.set(idx, map.newId);
                    valid = true;
                    break;
                }
            }
            
            oldValidAssignments.set(idx, valid);
        }

        
        if (threads < oldValidAssignments.size()) {
            oldOpenClAssignments = oldOpenClAssignments.subList(0, threads);
            oldValidAssignments = oldValidAssignments.subList(0, threads);
        }
        execModel.setOpenClAssignments(oldOpenClAssignments, oldValidAssignments);
        
        return execModel;
    }
    
    public static int getTotalNumberOfRealCores() {
        return TransferFacade.getNumberOfRealCores();
    }
    
    public static CPUCoresComboBoxModel getCpuCoresComboBoxModel() {
        return new CPUCoresComboBoxModel(getTotalNumberOfRealCores());        
    }

    public static boolean validate(AppContextModel appContextModel) {
        ExecutionEnvModel model = appContextModel.getExecutionEnvironment();
        if (model == null) {
            throw new InvalidExecutionEnvException("Please create a Data Execution environment configuration first.", "No execution environment model instance to validate");
        }
        if (!model.isEnableOpenCL()) {
            if (model.getCpuThreads() > 0 && model.getCpuThreads() <= getTotalNumberOfRealCores()) {
                return true;
            }
            
            throw new InvalidExecutionEnvException("Please re-check the number of assigned CPU threads.", "Invalid number of CPU threads specified: " + model.getCpuThreads());
        } else {
            List<OpenCLDeviceModel> openClDevices = model.getOpenClDevices();
            List<Long> cpuAssignments = model.getOpenClAssignments();
            List<Boolean> validAssignments = model.getValidAssignments();

            if (model.getCpuThreads() < 0 || model.getCpuThreads() > getTotalNumberOfRealCores()) {
                throw new InvalidExecutionEnvException("Please re-check the number of assigned CPU threads.", "Invalid number of CPU threads specified: " + model.getCpuThreads());
            }

            if (model.getCpuThreads() != cpuAssignments.size()) {
                throw new InvalidExecutionEnvException("Please re-check the number of assigned CPU threads.", "Inconsistent number of selected CPU threads(" + 
                              model.getCpuThreads() + "and device assignments (" + cpuAssignments.size() + ")" );
            }
            
            int i = 0;
            for (; i < cpuAssignments.size(); i++) {
                long openClID = cpuAssignments.get(i);
                boolean valid = validAssignments.get(i);
                
                if (!valid) {
                    throw new InvalidExecutionEnvException("Please re-check the OpenCL device assignments to CPU threads.", "Device assignment [" + 
                            (i+1) + "] refers to an invalid device.");
                }
                
                boolean validated = false;
                for (OpenCLDeviceModel device : openClDevices) {
                    if (device.getId() == openClID && device.isSelected()) {
                        validated = true;
                        break;
                    }
                }
                if (!validated) {
                    throw new InvalidExecutionEnvException("Please re-check the OpenCL device assignments to CPU threads.", "Device assignment [" + 
                                                           (i+1) + "] refers to non-available device with ID: " + openClID);
                }
            }
            
            List<OpenCLDeviceModel> availableDevices = PIVExecutionEnvironmentFacade.getAvailableGpuDevicesAsModels();
            for (OpenCLDeviceModel device : openClDevices) {
                if (!device.isSelected()) {
                    continue;
                }

                if (device.getPerformanceScore() <= 0.0f) {
                    throw new InvalidExecutionEnvException("A OpenCL device that is not working properly was selected for PIV processing", 
                            "OpenCL device: " + device.getId() + " - " + device.getName() + " is not suitable for PIV processing, but was selected");
                }
                
                boolean found = false;
                for (OpenCLDeviceModel availableDevice : availableDevices) {
                    if (availableDevice.getId() == device.getId()) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    throw new InvalidExecutionEnvException("A non-available OpenCL device is currently assigned to a CPU thread", "Device assignment [" + 
                            (i+1) + "] refers to non-available device with ID: " + device.getId() + " - " + device.getName());
                }                
            }                
            
            return true;
        }        
        
    }
}
