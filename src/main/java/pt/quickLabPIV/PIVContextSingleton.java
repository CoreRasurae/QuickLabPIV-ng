// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

import java.util.concurrent.atomic.AtomicReference;

public class PIVContextSingleton {
	protected static final AtomicReference<PIVContextSingleton> singleton = new AtomicReference<PIVContextSingleton>(null); 
		
	public static PIVContextSingleton getSingleton() {
		PIVContextSingleton context  = singleton.get();
		if (context == null) {
			context = new PIVContextSingleton();
			if (!singleton.compareAndSet(null, context)) {
				context = singleton.get();
			}
		}
		
		return context;
	}

	/**
	 * Replaces singleton instance parameters with new parameters instances.
	 */
    public synchronized void resetParametersInstances() {
        parameters = new PIVInputParameters();
        reusableObjects = new PIVReusableObjects();
        runParameters = new PIVRunParameters();        
    }

	protected PIVInputParameters parameters;
	protected PIVReusableObjects reusableObjects;
	protected PIVRunParameters   runParameters;
	
	public PIVContextSingleton() {
		parameters = new PIVInputParameters();
		reusableObjects = new PIVReusableObjects();
		runParameters = new PIVRunParameters();
	}
	
	public PIVInputParameters getPIVParameters() {
		return parameters;
	}
	
	public PIVReusableObjects getPIVReusableObjects() {
		return reusableObjects;
	}
	
	public PIVRunParameters getPIVRunParameters() {
		return runParameters;
	}

    public void cancelExecution() {
        runParameters.setCancelRequested(true);
    }
    
}
