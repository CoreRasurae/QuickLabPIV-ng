package pt.quickLabPIV.jobs.managers;

import java.util.List;

import pt.quickLabPIV.InputFiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.jobs.Job;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class ManagerParameters {
	//How to do this in a generic way...
	//Input files splitting based on device performance and memory limit...
	//How many CPU threads? Which CPUs?
	//Which GPU for each CPU?
	//Use thread affinity? how to assign?
	//Is joint GPU execution?
	//Reuse same GPU job for different problem geometries?

	private final int threadIdx;
	private Job<List<Tile>, XCorrelationResults>[] openCLJobs;
	private boolean sameXCorrJobForAllAdaptiveLevels;

	public ManagerParameters(int _threadIdx, Job<List<Tile>, XCorrelationResults>[] _openCLJobs, boolean sameXCorrJob) {
		threadIdx = _threadIdx;
		openCLJobs = _openCLJobs;
		sameXCorrJobForAllAdaptiveLevels = sameXCorrJob;
	}
	
	public int getThreadIdx() {
		return threadIdx;
	}
		
	public Job<List<Tile>, XCorrelationResults>[] getOpenCLJobs() {
		return openCLJobs;
	}
	
	public boolean isSameXCorrJobForAllAdaptiveLevels() {
		return sameXCorrJobForAllAdaptiveLevels;
	}
}
