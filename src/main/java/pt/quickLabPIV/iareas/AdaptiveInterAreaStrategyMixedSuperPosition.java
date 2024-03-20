// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;

/**
 * AdaptiveInterAreaStrategyMixedSuperPosition implements a mixed strategy, where Interrogation Areas start with a no 
 * super position area division/splitting, and at a given specified step the strategy changes to a super position strategy.
 * 
 * @author lpnm
 */
public class AdaptiveInterAreaStrategyMixedSuperPosition implements IInterAreaDivisionStrategy {
	private PIVInputParameters parameters;
	private int iterationStepToStartSuperPosition = 0;
	
	private AdaptiveInterAreaStrategyNoSuperPosition startStrategy;
	private AdaptiveInterAreaStrategySuperPosition endStrategy;

	public AdaptiveInterAreaStrategyMixedSuperPosition() {
		this.parameters = PIVContextSingleton.getSingleton().getPIVParameters();
		iterationStepToStartSuperPosition = parameters.getSuperPositionIterationStepStart();
	}
	
	@Override
	public IterationStepTiles createIterationStepTilesParameters(TilesOrderEnum order, IterationStepTiles currentStepTiles) {
		IterationStepTiles nextIterationStepTiles;
		if (currentStepTiles == null) {
			if (iterationStepToStartSuperPosition == 0) {
				startStrategy = null;
				endStrategy = new AdaptiveInterAreaStrategySuperPosition();
				
				nextIterationStepTiles = endStrategy.createIterationStepTilesParametersInternal(order, currentStepTiles, 0);
				
				//By default endStrategy will set itself as the area division/split strategy for the next iteration step tiles configuration,
				//in this case in not critical because we have already switched to super-position right at the initial step.
				nextIterationStepTiles.replaceAreadDivisionStrategy(this);
			} else {
				startStrategy = new AdaptiveInterAreaStrategyNoSuperPosition();
				endStrategy = null;
				
				nextIterationStepTiles = startStrategy.createIterationStepTilesParametersInternal(order, currentStepTiles, 0);

				//By default startStrategy will set itself as the area division/split strategy for the next iteration step tiles configuration,
				//thus it is specially important to override it, otherwise the current mixed strategy will no longer be called for the next iteration
				//steps, thus never switching to super-position strategy mode.
				nextIterationStepTiles.replaceAreadDivisionStrategy(this);
			}
		} else {
            if (order != currentStepTiles.getTilesOrder()) {
                throw new IterationStepTilesParametersException("Inconsistent tiles order specified");
            }

			if (iterationStepToStartSuperPosition <= currentStepTiles.getCurrentStep() + 1) {
				IterationStepTiles superPositionCurrentStepTiles = currentStepTiles;
				if (iterationStepToStartSuperPosition == currentStepTiles.getCurrentStep() + 1) {
					superPositionCurrentStepTiles = null;
					endStrategy = new AdaptiveInterAreaStrategySuperPosition();
				}
				
				//It may seem that this will cause a margins change, based on the computation of a new start strategy, but since we are
				//specifying an offset, the computed initial margins should be compatible since they are computed based on the same start
				//step with the same interrogation area sizes/dimensions.
				nextIterationStepTiles = endStrategy.createIterationStepTilesParametersInternal(order, superPositionCurrentStepTiles, iterationStepToStartSuperPosition);
				
				//nextIterationStepTiles can be null if the adaptive process has already reached the last step in the previous adaptive iteration step.
				if (nextIterationStepTiles != null) {
					//It is specially relevant to override the parent step tiles at the adaptive iteration step where the strategy switch is made from no super-position,
					//to super-position mode, because endStrategy.createIterationStepTilesParametersInternal(...) assumes it is at the first iteration step and sets
					//parent tiles to null.
					//So it is the mixed strategy responsibility to make things right, since it knows the strategy switch context.
					nextIterationStepTiles.setParentStepTiles(currentStepTiles);
				}
			} else {
			    nextIterationStepTiles = startStrategy.createIterationStepTilesParametersInternal(order, currentStepTiles, 0);
				
				//nextIterationStepTiles can be null if the adaptive process has already reached the last step in the previous adaptive iteration step.
				if (nextIterationStepTiles != null) {
					//By default startStrategy will set itself as the area division/split strategy for the next iteration step tiles configuration,
					//thus it is specially important to override it, otherwise the current mixed strategy will no longer be called for the next iteration
					//steps, thus never switching to super-position strategy mode.
					nextIterationStepTiles.replaceAreadDivisionStrategy(this);
				}
			}
		}
		
		return nextIterationStepTiles;
	}
}
