// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.business.transfer.TransferFacade;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.jobs.GpuFFTBenchmarkJob;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.ui.models.OpenCLDeviceModel;
import pt.quickLabPIV.ui.models.OpenCLDeviceTableModel;

public class PIVExecutionEnvironmentFacade {
    private static final Logger logger = LoggerFactory.getLogger(PIVExecutionEnvironmentFacade.class);
    
    public static List<OpenCLDeviceModel> getAvailableGpuDevicesAsModels() {
        List<ComputationDevice> devices = TransferFacade.getAvailableGpuDevices();
        List<OpenCLDeviceModel> models = new ArrayList<OpenCLDeviceModel>();
        
        for (ComputationDevice device : devices) {
            OpenCLDeviceModel model = TransferFacade.convertToModel(device);
            models.add(model);
        }
        
        return models;
    }
    
    public static void testProfileGPU(JTable table, int row) {
        OpenCLDeviceModel model = ((OpenCLDeviceTableModel)table.getModel()).getModels().get(row);
        ComputationDevice device = TransferFacade.convertToComputeDevice(model);
        //
        GpuFFTBenchmarkJob benchmark = new GpuFFTBenchmarkJob();
        benchmark.setInputParameters(JobResultEnum.JOB_RESULT_TEST_DEVICE, device);
        try {
            benchmark.analyze();
            benchmark.compute();
            float score = benchmark.getJobResult(JobResultEnum.JOB_RESULT_TEST_DEVICE);
            model.setPerformanceScore(score);
        } catch (Exception ex) {
            logger.warn("Failed to benchmark device: " + model, ex);
            model.setPerformanceScore(0.0f);
        }
            
        benchmark.dispose();
    }
    
}
