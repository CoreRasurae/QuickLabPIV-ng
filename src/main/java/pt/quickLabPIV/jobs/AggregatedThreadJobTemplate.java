package pt.quickLabPIV.jobs;

public abstract class AggregatedThreadJobTemplate<A,B> extends Job<A,B> {
	private final Job<A,B> job;
	
	private final int participatingThreadCount; //Participating thread count can be set in advance, if a thread has no work to do provides an empty input params, but must
    //calls methods in the same sequence as if it had work to do. Mixed matrix dimensions are not acceptable, thus may not be a good 
    //option for computation with
	  //stabilization strategies, as it can delay some threads until the last one stabilizes. It is up to the client code to agree when
    //all threads have stabilized. Or don't allow such computation option with stabilization strategies.
	//
	//Other option is for a thread to disable its participation in the executions for a given problem size, indicating its desire to participate in all
	//computing jobs where the thread participates, whether having work or not for them. -- This is a good option for computations with stabilization strategies.

	@Override
	protected long getThreadJobId() {
		return Thread.currentThread().getId();
	}

	/**
	* Retrieves the number of threads that participate in this 
	* @return
	*/
	public int getParticipatingThreadCount() {
		return participatingThreadCount;
	}

	
	/**
	 * Creates a new aggregated thread job template.
	 * <br/>
	 * An aggregated thread job is a job that collects input parameters from several threads and analyzes and
	 * executes the job as if being a single job from a single thread. Actually only a single thread will call analyze(),
	 * and execute(), however all threads will participate concurrently in gathering their individual results.
	 * @param regularJob the job that is to be performed in an aggregated manner.
	 * @param threadCount the total number of threads participating in the aggregated thread job. 
	 */
	public AggregatedThreadJobTemplate(Job<A,B> regularJob, int threadCount) {
		if (regularJob == null) {
			throw new NullPointerException("RegularJob must not be null");
		}
		
		if (regularJob instanceof AggregatedThreadJobTemplate) {
			throw new IllegalArgumentException("Cannot create aggregated job from Threaded Jobs");
		}
		
		job = regularJob;
		participatingThreadCount = threadCount;
	}
	
	/**
	 * Aggregates input parameters from participating threads into a single input parameter.
	 * <br/>
	 * <b>Note1: </b>A single input parameter will be built from calling threads, 
	 * by the last thread calling analyze method, thus not concurrently.
	 */
	protected abstract void aggregateInputParameters();
	
	/**
	 * Disaggregates results from the unified computation into individual thread results.
	 * <br/> 
	 * <b>Note1: </b>Each individual thread that invoked execute will call this method, so that each thread
	 * extracts its own portion of the results.
	 * <br/>
	 * <b>Note2: </b>It is required that all threads only perform read operations over the 
	 * intermediate computation results.
	 */
	protected abstract void disaggregateResults();
	
	@Override
	public void analyze() {
		//Due to the adaptive behavior of PIV it is possible that at a given level not all threads have work to do, plus
		//amount of tiles to process varies even within same dimensions.
		//It would be great if XCorr Jobs could adapt to variable input matrix dimensions - and is possible... 
		
		//Receives calls from multiple threads
		
		//Aggregates the work
		aggregateInputParameters();
		
		//Only does a single analyze for all threads
		job.analyze();
	}

	@Override
	public void compute() {
		//Receives calls from multiple threads
		
		//Only proceeds when all threads have called compute
		
		//Only a single thread performs compute
		job.compute();
		//Single thread finishes job computation
		
		//Single thread notifies all threads of result completed
		
		//All threads get their result portions
		disaggregateResults();

	}

	@Override
	public void dispose() {
		//Job should not be running when method is called.
		job.dispose();
	}

}
