// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.business.transfer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.internal.exception.AparapiException;

import pt.quickLabPIV.InvalidPIVParametersException;
import pt.quickLabPIV.PIVConcatException;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.exporter.ExportFailedException;
import pt.quickLabPIV.iareas.InvalidStateException;
import pt.quickLabPIV.iareas.IterationStepTilesParametersException;
import pt.quickLabPIV.images.ImageClippingException;
import pt.quickLabPIV.images.ImageNotFoundException;
import pt.quickLabPIV.images.ImageReaderException;
import pt.quickLabPIV.images.ImageStateException;
import pt.quickLabPIV.jobs.JobAnalyzeException;
import pt.quickLabPIV.jobs.JobComputeException;
import pt.quickLabPIV.ui.views.ExecutionProgressDialog;

public class LocalPIVExecutionMonitorWorker extends SwingWorker<Void, PIVCompletionStatus> {
	private static final Logger logger = LoggerFactory.getLogger(LocalPIVExecutionMonitorWorker.class);
	
    private ExecutionProgressDialog dialog;
    private ExecuteLocalPIVWorker otherWorker;
    
    public LocalPIVExecutionMonitorWorker(ExecutionProgressDialog _dialog, ExecuteLocalPIVWorker _worker) {
        dialog = _dialog;
        otherWorker = _worker;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        PIVCompletionStatus status = new PIVCompletionStatus();
        try {
            otherWorker.get();
            status.setCompleted(true);
            publish(status);
        } catch (InterruptedException e) {
            UIException ex = new UIException("PIV Processing failed", "Processing was interrupted or canceled");
            status.setException(ex);
            publish(status);
        } catch (ExecutionException e) {
            Throwable relevantEx = e.getCause();
            
            //Try to drill down to the first known exception class, the root cause with known error type...
            Throwable parseEx = e;
            while (parseEx != null) {
                if (parseEx instanceof UIException) {
                	logger.error("UI exception", parseEx);
                	relevantEx = parseEx;
                    break;
                } else if (parseEx instanceof PIVConcatException) {
                	logger.error("PIV concatenation failed", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to group PIV results: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));                    
                } else if (parseEx instanceof InvalidPIVParametersException) {
                	logger.error("Invalid PIV parameters", parseEx);
                	relevantEx = new UIException("PIV Processing failed", 
                            "PIV parameters are invalid: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof JobAnalyzeException) {
                	logger.error("Job Analyze exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to analyze job: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof JobComputeException) {
                	logger.error("Job Compute exception", parseEx);
                	relevantEx = new UIException("PIV Processing failed", 
                            "Failed to analyze job: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof ExportFailedException) {
                	logger.error("Export failed exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to export data: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof ImageClippingException) {
                	logger.error("Image Clipping exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to clip image: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof ImageNotFoundException) {
                	logger.error("Image Not Found exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to find image: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof ImageReaderException) {
                	logger.error("Image Reader exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to read image: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof ImageStateException) {
                    logger.error("Image State exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Failed to read image, reader job in invalid state: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof InvalidStateException) {
                    logger.error("Invalid State exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Interrogation area invalid state: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof IterationStepTilesParametersException) {
                    logger.error("Iteration step tiles parameters exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "Invalid tiles parameters: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                } else if (parseEx instanceof AparapiException) {
                    logger.error("Aparapi exception", parseEx);
                    relevantEx = new UIException("PIV Processing failed", 
                            "OpenCL processing failed, possibly due to insufficient device resources: " + parseEx.getMessage().replaceAll("(.{1,50})\\s+", "$1\n"));
                    break;
                }

                parseEx = parseEx.getCause();
            }
            
            //Resolve relevant exception if able to
            UIException ex = null;
            if (relevantEx instanceof UIException) {
                ex = (UIException)relevantEx;
            } else {
            	logger.error("Processing failed with uncommon cause", relevantEx);
                ex = new UIException("PIV Processing failed", "Error ocurred: " + relevantEx.getMessage());
            }
            status.setException(ex);
            publish(status);
        }

        return null;
    }

    @Override
    protected void process(List<PIVCompletionStatus> chunks) {
        for (PIVCompletionStatus chunk : chunks) {
            dialog.updateWithCompletionStatus(chunk);
        }
    }

    public void requestCancellation() {       
        otherWorker.requestCancellation();
    }
}
