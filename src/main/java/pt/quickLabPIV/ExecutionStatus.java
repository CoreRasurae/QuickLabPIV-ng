// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionStatus {
    private static final int NUMBER_OF_REPORTS = 3;
    
    private final Object lock = new Object(); 
    private ProgressReport freeReports = null;
    private IProgressReportObserver observer = null;
    private long startTime;
    private Long endTime = null;
    private AtomicInteger processedImages = new AtomicInteger(0);
    private AtomicInteger updatesWithoutReport = new AtomicInteger(0);
    private int updateStep;
    
    public ExecutionStatus() {
        ProgressReport first = new ProgressReport();
        ProgressReport element = first;
        for (int i = 0; i < NUMBER_OF_REPORTS-1; i++) {
            ProgressReport nextElement = new ProgressReport();
            element.setNext(nextElement);
            element = nextElement;
        }
        freeReports = first;
    }
    
    public void setReportObserver(IProgressReportObserver _observer) {
        observer = _observer;
    }
    
    public void start() {
        startTime = System.currentTimeMillis();
        endTime = null;
        processedImages.set(0);
        updatesWithoutReport.set(0);
        
        ProgressReport report = getNextFreeReport();
        if (report == null) {
            //TODO Log
            return;
        }
        
        notifyReport(report);        
    }

    public void continueAt(int count) {
        processedImages.set(count);
        updatesWithoutReport.set(0);
        
        ProgressReport report = getNextFreeReport();
        if (report == null) {
            //TODO Log
            return;
        }
        
        notifyReport(report);        
    }
    
    public synchronized void end() {
        endTime = System.currentTimeMillis();
        ProgressReport report = getNextFreeReport();
        if (report == null) {
            //TODO Log
            return;
        }
        
        notifyReport(report);
    }
    
    public void setReportUpdateStep(int _updateStep) {
        updateStep = _updateStep;
    }

    public void setInitialConfiguration(int totalImages, String outputPathAndFilename) {
        for (ProgressReport report = freeReports; report != null; report = report.getNext()) {
            report.setResultOutputPathAndFilename(outputPathAndFilename);
            report.setTotalImages(totalImages);
        }
    }

    
    public void setTotalImages(int totalImages) {
        for (ProgressReport report = freeReports; report != null; report = report.getNext()) {
            report.setTotalImages(totalImages);
        }
    }
    
    public void incrementProcessedImages() {
        processedImages.incrementAndGet();
        System.out.println("Incremented...");
        int updatedCount = updatesWithoutReport.incrementAndGet();
        System.out.println("Updated count: " + updatedCount + ", updateStep: " + updateStep);
        if (updatedCount == updateStep && updateStep != 0) {
            updatesWithoutReport.addAndGet(-updateStep);
            ProgressReport report = getNextFreeReport();            
            if (report == null) {
            	System.out.println("REport is null");
                //TODO Log
                return;
            }
            
            notifyReport(report);            
        }
    }
    
    private ProgressReport getNextFreeReport() {
        ProgressReport freeReport = null;
        
        synchronized (lock) {
            if (freeReports != null) {
                freeReport = freeReports;
                freeReports = freeReports.getNext();
                freeReport.setNext(null);
            }
        }
        
        return freeReport;
    }
    
    private void reinsertFreeReport(ProgressReport reportToFree) {
        synchronized (lock) {
            if (freeReports == null) {
                freeReports = reportToFree;
                reportToFree.setNext(null);
            } else {
                reportToFree.setNext(freeReports);
                freeReports = reportToFree;
            }
        }
    }
    
    private synchronized void notifyReport(ProgressReport report) {
        float elapsedTime = 0.0f;
        System.out.println("Reporting...");
        if (observer == null) {
            return;
        }
        
        if (endTime != null) {
            elapsedTime = (endTime - startTime)/1000.0f; //Compute elapsed milliseconds and convert to seconds
        } else {
        	elapsedTime = (System.currentTimeMillis() - startTime)/1000.0f;
        }
        report.updateStatus(processedImages.get(), elapsedTime);
        
        observer.receiveUpdatedProgressReport(report);
        reinsertFreeReport(report);
    }
}
