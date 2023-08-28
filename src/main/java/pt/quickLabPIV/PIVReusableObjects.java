// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import pt.quickLabPIV.exporter.StructMultiFrameFloatVelocityExporter;
import pt.quickLabPIV.interpolators.CrossCorrelationInterpolatorFactoryEnum;
import pt.quickLabPIV.interpolators.IBasicCrossCorrelationInterpolator;
import pt.quickLabPIV.interpolators.ICrossCorrelationInterpolator;

public class PIVReusableObjects {
	private AtomicReference<CrossCorrelationInterpolatorFactoryEnum> interpolatorStrategy = new AtomicReference<CrossCorrelationInterpolatorFactoryEnum>(null); 
	private ConcurrentHashMap<Long, ICrossCorrelationInterpolator> interpolatorByThread = 
							new ConcurrentHashMap<Long, ICrossCorrelationInterpolator>();
	/**
	 * Retrieves a dedicated interpolator per each client Thread, if one is not available yet, it will be created, according to
	 * specified strategy.
	 * @return the thread dedicated cross-correlation interpolator instance
	 */
	public ICrossCorrelationInterpolator getOrCreateInterpolator() {
		long threadId = Thread.currentThread().getId();
		ICrossCorrelationInterpolator interpolator = interpolatorByThread.get(threadId);
		if (interpolator == null) {
			if (interpolatorStrategy.get() != CrossCorrelationInterpolatorFactoryEnum.None) {
				PIVContextSingleton context = PIVContextSingleton.getSingleton();
				CrossCorrelationInterpolatorFactoryEnum strategy = context.getPIVParameters().getInterpolationStrategy();
				interpolatorStrategy.compareAndSet(null, strategy); //It doesn't matter which thread succeeds with the update
				if (strategy != CrossCorrelationInterpolatorFactoryEnum.None) {
					interpolator = CrossCorrelationInterpolatorFactoryEnum.createInterpolator(strategy);
					interpolatorByThread.putIfAbsent(threadId, interpolator);
				}				
			}
		}
		
		return interpolator;
	}
}
