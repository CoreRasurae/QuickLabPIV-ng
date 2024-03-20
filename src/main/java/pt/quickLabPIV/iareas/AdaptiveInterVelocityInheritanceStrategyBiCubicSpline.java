// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.PIVContextSingleton;

public class AdaptiveInterVelocityInheritanceStrategyBiCubicSpline implements IInterAreaVelocityInheritanceStrategy {
   protected IAdaptiveInterVelocityInheritanceLogger logger;
   protected boolean requiresRounding;
   
   public AdaptiveInterVelocityInheritanceStrategyBiCubicSpline() {
      PIVContextSingleton pivContext = PIVContextSingleton.getSingleton();
      logger = pivContext.getPIVRunParameters().getVelocityInheritanceLogger();
      requiresRounding = pivContext.getPIVParameters().getWarpingMode().isRequiresRounding();
   }

   @Override
   public void reuseIterationStepTilesParameters(IterationStepTiles currentStepTiles) {
      int nextIterationStep = currentStepTiles.getCurrentStep();
      
      if (nextIterationStep >= currentStepTiles.getMaxAdaptiveSteps()) {
         throw new IterationStepTilesParametersException(
               "Next iteration step to be reused has an iteration step that is outside the adaptive range parameters");
      }

      currentStepTiles.resetDisplacements();
      
      
      //Inherit displacements from corresponding parent tile displacements
      IterationStepTiles parentStepTiles = currentStepTiles.getParentStepTiles();
      if (parentStepTiles != null) {
         Tile[][] parentTiles = parentStepTiles.getTilesArray();
         Tile[][] currentTiles = currentStepTiles.getTilesArray();
         //Arrays.stream(parentTiles).map(Tile::getDisplacementU).toArray(size -> new Float[][]);
         double us[][] = new double[parentStepTiles.getNumberOfTilesInI()][parentStepTiles.getNumberOfTilesInJ()];
         double vs[][] = new double[parentStepTiles.getNumberOfTilesInI()][parentStepTiles.getNumberOfTilesInJ()];
         double xs[] = new double[parentStepTiles.getNumberOfTilesInJ()];
         double ys[] = new double[parentStepTiles.getNumberOfTilesInI()];
         for (int i = 0; i < parentTiles.length; i++) {
            for (int j = 0; j < parentTiles[0].length; j++) {
               us[i][j] = parentTiles[i][j].getDisplacementU();
               vs[i][j] = parentTiles[i][j].getDisplacementV();
               if (i == 0) {
                  //Center of IA is at the center of the 4 center pixels
                  xs[j] = parentTiles[i][j].getLeftPixel() + parentStepTiles.getTileWidth() / 2.0f - 0.5f;
               }
               if (j == 0) {
                  //Center of IA is at the center of the 4 center pixels
                  ys[i] = parentTiles[i][j].getTopPixel() + parentStepTiles.getTileHeight() / 2.0f - 0.5f;
               }
            }
         }
         
         float Xmin = (float)xs[0];
         float Xmax = (float)xs[xs.length - 1];
         float Ymin = (float)ys[0];
         float Ymax = (float)ys[ys.length - 1];
         
         PiecewiseBicubicSplineInterpolator interpolator = new PiecewiseBicubicSplineInterpolator();
         PiecewiseBicubicSplineInterpolatingFunction funcU = null;
         BiLinearInterpolatingFunction biFuncU = null;
         try {
             funcU = interpolator.interpolate(ys, xs, us);
         } catch (InsufficientDataException ex) {
             biFuncU = new BiLinearInterpolatingFunction(ys, xs, us);
         }
         PiecewiseBicubicSplineInterpolatingFunction funcV = null;
         BiLinearInterpolatingFunction biFuncV = null;
         try {
             funcV = interpolator.interpolate(ys, xs, vs);
         } catch (InsufficientDataException ex) {
             biFuncV = new BiLinearInterpolatingFunction(ys, xs, vs);
         }
         
         for (int i = 0; i < currentTiles.length; i++) {
            for (int j = 0; j < currentTiles[0].length; j++) {
               //Center of IA is at the center of the 4 center pixels
               float x = currentTiles[i][j].getLeftPixel() + currentStepTiles.getTileWidth()/2.0f - 0.5f;
               float y = currentTiles[i][j].getTopPixel() + currentStepTiles.getTileHeight()/2.0f - 0.5f;

               //PiecewiseBicubicSplineInterpolator is unable to interpolate points outside of the interpolating table,
               //so lets limit the currentTiles interpolating region to the maximum region allowed by the parentTiles,
               //this also seems to be the behavior of the SciPy.fitpack2.RectBivariateSpline
               if (x < Xmin) {
                   x = Xmin;
               } else if (x > Xmax) {
                   x = Xmax;
               }
               
               if (y < Ymin) {
                   y = Ymin;
               } else if (y > Ymax) {
                   y = Ymax;
               }

               float interpolatedU = 0.0f;
               float interpolatedV = 0.0f;
               
               if (funcU != null) {
                   interpolatedU = (float)funcU.value(y, x);
               } else {
                   interpolatedU = (float)biFuncU.value(y, x);
               }
               
               if (funcV != null) {
                   interpolatedV = (float)funcV.value(y, x);
               } else {
                   interpolatedV = (float)biFuncV.value(y, x);
               }
               
               if (funcU != null && funcV != null) {
                   if (requiresRounding) {
                       //Non-warping modes require rounding because of adaptive steps, for which the window can only be
                       //displaced by integer values, and could accumulate errors between successive adaptive steps,
                       //due to wrong sub-pixel accumulation.
                       currentTiles[i][j].accumulateDisplacement(FastMath.round(interpolatedU), FastMath.round(interpolatedV));
                    } else {
                       //Warping modes on the other hand, do not have to displace any window, instead they absorb all
                       //sub-pixel contributions into the warping process at each step, so the sub-pixel accumulation
                       //is indeed correct.
                       currentTiles[i][j].accumulateDisplacement(interpolatedU, interpolatedV);
                    }
               }                               
            }
         }
      }
   }
}
