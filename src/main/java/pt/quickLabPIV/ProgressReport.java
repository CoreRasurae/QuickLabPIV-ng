// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

public class ProgressReport {
    private float completed;
    private int processedImages;
    private int totalImages;
    private float timePerImage;
    private float elapsedTime;
    private float remainingTime;
    private String outputPath = null;
    private ProgressReport next = null;
    
    private void updateCompleted() {
        if (totalImages > 0) {
            completed = (float)processedImages/(float)totalImages*100.0f;
            timePerImage = elapsedTime/(float)processedImages;
            if (processedImages != totalImages) {
                remainingTime = (totalImages * timePerImage + 30) - elapsedTime;
            } else {
                remainingTime = 0.0f;
            }
        } else {
            completed = 0.0f;
        }        
    }
    
    public void updateStatus(int _processedImages, float _elapsedTime) {
        processedImages = _processedImages;
        elapsedTime = _elapsedTime;
        updateCompleted();
    }
    
    public void setTotalImages(int _totalImages) {
        totalImages = _totalImages;
        updateCompleted();
    }

    public float getCompleted() {
        return completed;
    }
    
    public int getProcessedImages() {
        return processedImages;
    }
    
    public int getTotalImages() {
        return totalImages;
    }
    
    public float getTimePerImage() {
        return timePerImage;
    }
    
    public float getElapsedTime() {
        return elapsedTime;
    }
    
    public float getRemainingTime() {
        return remainingTime;
    }
     
    public void setResultOutputPathAndFilename(String _outputPath) {
        outputPath = _outputPath;        
    }
    
    public String getResultOutputPathAndFilename() {
        return outputPath;
    }

    public void setNext(ProgressReport _next) {
        next = _next;
    }
    
    public ProgressReport getNext() {
        return next;
    }
    
	public ProgressReport copy() {
		ProgressReport report = new ProgressReport();
		
		report.outputPath = outputPath;
		report.completed = completed;
		report.elapsedTime = elapsedTime;
		report.next = null;
		report.processedImages = processedImages;
		report.remainingTime = remainingTime;
		report.timePerImage = timePerImage;
		report.totalImages = totalImages;
		
		return report;
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder(100);
	    
	    sb.append("Completed: ");
	    sb.append(completed);
	    sb.append("% - Image: ");
	    sb.append(processedImages);
	    sb.append(" of ");
	    sb.append(totalImages);
	    sb.append(" - Elapsed: ");
	    sb.append(elapsedTime);
	    sb.append(" s - Remaining: ");
	    sb.append(remainingTime);
	    sb.append(" s");
	    
	    return sb.toString();
	}
}
