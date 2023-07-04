package pt.quickLabPIV.jobs.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.util.FastMath;

import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.ExecutionStatus;
import pt.quickLabPIV.InputFiles;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVResults;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.JobComputeException;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.LocalPIVOpenCLGpuJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTBasicJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParBlockStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationRealFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationSoftRealFFTJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTTemplate.EmulationModeEnum;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationJob;

public class OpenClGpuManager extends Job<ManagerParameters, PIVResults> {
    private class JTPKernelManager extends KernelManager {
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.JTP);
    	}
    }
    
	private InputFiles inputFiles;
	private int numberOfThreads;

	
	private List<LocalPIVOpenCLGpuJob> gpuJobs;
	
	public OpenClGpuManager(InputFiles inputFiles) {
		this.inputFiles = inputFiles;
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();

        numberOfThreads = runParameters.getTotalNumberOfThreads();
        if (inputFiles.size() < numberOfThreads) {
            numberOfThreads = inputFiles.size();
        }
	}
	
	public void analyze() {		
		List<InputFiles> inputFilesPerThread = inputFiles.splitIntoThreads();
		
		gpuJobs = new ArrayList<LocalPIVOpenCLGpuJob>(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			LocalPIVOpenCLGpuJob localJob = new LocalPIVOpenCLGpuJob();
			if (inputFilesPerThread.get(i).size() > 0) {
    			localJob.setInputImages(inputFilesPerThread.get(i));
    			localJob.analyze();
    			gpuJobs.add(localJob);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void compute() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();

	    List<Future<PIVResults>> jobsFutures = new ArrayList<Future<PIVResults>>(numberOfThreads);		
		ComputationDevice jobComputationDevicesByThreadIdx[] = new ComputationDevice[numberOfThreads];
   
		if (runParameters.isUseOpenCL()) {		        
		    for (DeviceRuntimeConfiguration config : runParameters.getDeviceConfigurations()) {
		        for (int threadIdx : config.getCpuThreadAssignments()) {
		            if (threadIdx < numberOfThreads) {
                        System.out.println("Thread " + threadIdx + " will use compute device: " + config.getDevice().getDeviceName() +
                                ", with id: " + config.getDevice().getDeviceId());
            			jobComputationDevicesByThreadIdx[threadIdx] = config.getDevice();
		            }
		        }
    		}
		}
		
		
		final PIVInputParameters inputParameters = PIVContextSingleton.getSingleton().getPIVParameters();
		runParameters.setExecutionJob(this);
		final ExecutionStatus execStatus = runParameters.getExecutionStatus();
		int iaStartPixelsI = inputParameters.getInterrogationAreaStartIPixels();
		int iaEndPixelsI = inputParameters.getInterrogationAreaEndIPixels();
		
		final int adaptiveLevels = (int)(FastMath.log(2, iaStartPixelsI) - FastMath.log(2, iaEndPixelsI)) + 1;
		
		execStatus.setReportUpdateStep(numberOfThreads);
		if (inputFiles.getAbsoluteStartFrame() == 0) {
		    execStatus.start();
		} else {
		    execStatus.continueAt(inputFiles.getAbsoluteStartFrame());
		}
		Iterator<LocalPIVOpenCLGpuJob> jobsIter = gpuJobs.iterator();
		int jobIndex = 0;
		while (jobsIter.hasNext()) {
			LocalPIVOpenCLGpuJob job = jobsIter.next();
			
			Job<List<Tile>, XCorrelationResults>[] openCLJobs = new Job[adaptiveLevels];
			for (int i = 0; i < adaptiveLevels; i++) {
			    if (runParameters.isUseOpenCL()) {
			        openCLJobs[i] = new CrossCorrelationRealFFTParStdJob(false, jobComputationDevicesByThreadIdx[jobIndex], null).setEmulationMode(EmulationModeEnum.GPU);
			    } else {
			        //Java CPU based Real FFT Cross Correlation
			        openCLJobs[i] = new CrossCorrelationSoftRealFFTJob();
			    }
			    //OpenCL accelerated Cross correlation by definition
			    //openCLJobs[i] = new CrossCorrelationJob(false, jobComputationDevices[jobIndex], null);
			    //Not working
			    //openCLJobs[i] = new CrossCorrelationFFTStdJob(false, jobComputationDevices[jobIndex], null);
			    //Not working
			    //openCLJobs[i] = new CrossCorrelationFFTParBlockStdJob(false, jobComputationDevices[jobIndex], null);
			    //Not working
			    //openCLJobs[i] = new CrossCorrelationFFTBasicJob(false, jobComputationDevices[jobIndex], null);
			}
			
			ManagerParameters parameters = new ManagerParameters(jobIndex, openCLJobs, true);
			job.setParameters(parameters);
			job.compute();
			Future<PIVResults> future = job.getJobResult(JobResultEnum.JOB_RESULT_PIV);
			jobsFutures.add(future);
			jobIndex++;
		}

		try {
    		Iterator<Future<PIVResults>> futuresIter = jobsFutures.iterator();
    		PIVResults lastResult = null;
    		while (futuresIter.hasNext()) {
    			Future<PIVResults> future = futuresIter.next();
    			PIVResults result = null;
    			try {
    				result = future.get();
                    if (runParameters.isCancelRequested()) {
                        return;
                    }
    			} catch (InterruptedException e) {
    				if (runParameters.isCancelRequested()) {
    				    return;
    				}
    			} catch (ExecutionException e) {
    				throw new JobComputeException(e);
    			}
    			
    			if (lastResult != null) {
    				lastResult.concatenate(result);
    			} else {
    				setJobResult(JobResultEnum.JOB_RESULT_PIV, result);
    			}
    			
    			lastResult = result;
    		}
    		execStatus.end();
		} finally {
    	    jobsIter = gpuJobs.iterator();
    		while (jobsIter.hasNext()) {
    			LocalPIVOpenCLGpuJob job = jobsIter.next();
    			job.dispose();
    		}
		}
		
	}

	@Override
	public void dispose() {
		
	}
}
