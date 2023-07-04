package pt.quickLabPIV.jobs.optimizers;

public class WorkItemsGeometryOptimizer {
	public static GlobalGeometry optimizeGeometry2D(int numberOfMatrices, int threadsPerMatrixI, int threadsPerMatrixJ, int maxJobsI, int maxJobsJ) {
		GlobalGeometry geom = new GlobalGeometry();
		geom.numberOfUsedTiles = numberOfMatrices;
		geom.wastedWorkItems = Integer.MAX_VALUE;
		geom.workItemsK = 1;
		geom.workItemsPerMatrixI = threadsPerMatrixI;
		geom.workItemsPerMatrixJ = threadsPerMatrixJ;
		for (int jobsJ = 1; jobsJ*threadsPerMatrixJ <= maxJobsJ; jobsJ++) {
			int jobsI = numberOfMatrices/jobsJ;
			boolean wastedElements = false;
			if (jobsI * jobsJ != numberOfMatrices) {
				jobsI++;
				wastedElements = true;
			}
			
			if (jobsI*threadsPerMatrixI > maxJobsI) {
				continue;
			}
			
			if (wastedElements) {
				//Wasted elements only exist in last row...
				int excessMatrices = numberOfMatrices % jobsJ;
				int newWastedWorkItems = jobsJ*threadsPerMatrixJ - excessMatrices*threadsPerMatrixJ;
				if (newWastedWorkItems < geom.wastedWorkItems) {
					geom.workItemsI = jobsI*threadsPerMatrixI;
					geom.workItemsJ = jobsJ*threadsPerMatrixJ;
					geom.matricesI = jobsI;
					geom.matricesJ = jobsJ;
					geom.wastedWorkItems = newWastedWorkItems; 
				}
			} else {
				geom.workItemsI = jobsI*threadsPerMatrixI;
				geom.workItemsJ = jobsJ*threadsPerMatrixJ;
				geom.matricesI = jobsI;
				geom.matricesJ = jobsJ;
				geom.wastedWorkItems = 0;
				break;
			}
		}
		
		return geom;
	}
}
