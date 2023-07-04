package pt.quickLabPIV.jobs;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Job<InputParams,Result> {
    private boolean cancelled = false;
	private final ConcurrentHashMap<Long, Map<JobResultEnum, InputParams>> inputParametersPerThreadJob = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Map<JobResultEnum, Result>> jobResultsPerThreadJob = new ConcurrentHashMap<>();
	
	public Job() {
	}
	
	public abstract void analyze();
	
	public abstract void compute();
	
	/**
	 * The internal Id for the thread calling the methods for setting parameters and for retrieving results. 
	 * @return the thread id
	 */
	protected long getThreadJobId() {
		return 0L;
	}
	
	public void setInputParameters(JobResultEnum key, InputParams parameters) {
		Map<JobResultEnum,InputParams> inputMap = inputParametersPerThreadJob.computeIfAbsent(getThreadJobId(), k ->  new EnumMap<>(JobResultEnum.class) );
		inputMap.put(key, parameters);
	}
	
	protected InputParams getInputParameters(JobResultEnum key) {
		Map<JobResultEnum,InputParams> inputMap = inputParametersPerThreadJob.getOrDefault(getThreadJobId(), Collections.emptyMap());
		return inputMap.getOrDefault(key, null);
	}
	
	public Result getJobResult(JobResultEnum resultEnum) {
		Map<JobResultEnum, Result> resultMap = jobResultsPerThreadJob.getOrDefault(getThreadJobId(), Collections.emptyMap());
		return resultMap.getOrDefault(resultEnum, null);
	}

	public abstract void dispose();
	
	protected void clearResults() {
		jobResultsPerThreadJob.clear();
	}
	
	protected void setJobResult(JobResultEnum resultEnum, Result value) {
		Map<JobResultEnum, Result> resultMap = jobResultsPerThreadJob.computeIfAbsent(getThreadJobId(), k ->  new EnumMap<>(JobResultEnum.class));
		resultMap.put(resultEnum, value);
	}

    public void cancel() {
        cancelled = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
}
