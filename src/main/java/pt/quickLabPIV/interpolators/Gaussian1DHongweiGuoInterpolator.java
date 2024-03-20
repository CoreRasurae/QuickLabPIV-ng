// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.interpolators;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.iareas.InterpolateException;
import pt.quickLabPIV.maximum.MaxCrossResult;

public class Gaussian1DHongweiGuoInterpolator implements IBasicCrossCorrelationInterpolator {
    private final static Logger logger = LoggerFactory.getLogger(Gaussian1DHongweiGuoInterpolator.class);
    
	private final int pixels;
	private final int iterations;
	private final double[] zeros = new double[3];
	
	public Gaussian1DHongweiGuoInterpolator() {
		PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
		Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(Gaussian1DHongweiGuoInterpolatorConfiguration.IDENTIFIER);
		if (configurationObject == null) {
			throw new InterpolatorStateException("Couldn't retrieve Gaussian1D Hongwei Guo interpolator configuration");
		}
		Gaussian1DHongweiGuoInterpolatorConfiguration configuration = (Gaussian1DHongweiGuoInterpolatorConfiguration)configurationObject;
		pixels = configuration.getInterpolationPixels();
		iterations = configuration.getIterations();
	}
	
	public Gaussian1DHongweiGuoInterpolator(int _pixels, int _iterations) {
	    pixels = _pixels;
	    iterations = _iterations;
	}

	private RealMatrix initMatrices(RealMatrix A, RealMatrix b, float[] xs, float[] ys) {
	    for (int i = 0; i < xs.length; i++) {
	        double ySquare = ys[i] * ys[i];
	        double xSquare = xs[i] * xs[i];
	        double xCubic = xSquare * xs[i];
	        double xFourth = xCubic * xs[i];
	        double logyEst = FastMath.log(ys[i]);
	        A.addToEntry(0, 0, ySquare);
	        A.addToEntry(0, 1, xs[i] * ySquare);
	        A.addToEntry(0, 2, xSquare * ySquare);
	        b.addToEntry(0, 0, ySquare * logyEst);
	        //---------------------------------
            A.addToEntry(1, 0, xs[i] * ySquare);
            A.addToEntry(1, 1, xSquare * ySquare);
            A.addToEntry(1, 2, xCubic * ySquare);
            b.addToEntry(1, 0, xs[i] * ySquare * logyEst);
            //---------------------------------
            A.addToEntry(2, 0, xSquare * ySquare);
            A.addToEntry(2, 1, xCubic * ySquare);
            A.addToEntry(2, 2, xFourth * ySquare);
            b.addToEntry(2, 0, xSquare * ySquare * logyEst);
	    }
	    return A;
	}
	
	private RealMatrix updateMatrices(RealMatrix A, RealMatrix b, double[] coefs, float[] xs, float[] ys) {
        A.setRow(0, zeros);
        A.setRow(1, zeros);
        A.setRow(2, zeros);
        b.setColumn(0, zeros);
        
        for (int i = 0; i < xs.length; i++) {
            double yk = FastMath.exp(coefs[0] + coefs[1] * xs[i] + coefs[2] * xs[i] * xs[i]);
            
            double ySquare = yk * yk;
            double xSquare = xs[i] * xs[i];
            double xCubic = xSquare * xs[i];
            double xFourth = xCubic * xs[i];
            double logyEst = FastMath.log(ys[i]);
            A.addToEntry(0, 0, ySquare);
            A.addToEntry(0, 1, xs[i] * ySquare);
            A.addToEntry(0, 2, xSquare * ySquare);
            b.addToEntry(0, 0, ySquare * logyEst);
            //---------------------------------
            A.addToEntry(1, 0, xs[i] * ySquare);
            A.addToEntry(1, 1, xSquare * ySquare);
            A.addToEntry(1, 2, xCubic * ySquare);
            b.addToEntry(1, 0, xs[i] * ySquare * logyEst);
            //---------------------------------
            A.addToEntry(2, 0, xSquare * ySquare);
            A.addToEntry(2, 1, xCubic * ySquare);
            A.addToEntry(2, 2, xFourth * ySquare);
            b.addToEntry(2, 0, xSquare * ySquare * logyEst);
        }
	    return A;
	}
		
	private float[] estimateGaussianParameters(int iterations, float[] xs, float[] ys) {
	    float amplitude = 0.0f;
	    float mean = 0.0f;
	    double[] coefs = null;
        RealMatrix A = new Array2DRowRealMatrix(3,3);
        RealMatrix b = new Array2DRowRealMatrix(3,1);
        A = initMatrices(A,b,xs,ys);
        for (int i = 0; i < iterations; i++) {
            try {
                LUDecomposition lu = new LUDecomposition(A);
                DecompositionSolver s = lu.getSolver();
                RealMatrix c = s.solve(b);
                coefs = c.getColumn(0);
            } catch (SingularMatrixException e) {
                SingularValueDecomposition svd = new SingularValueDecomposition(A);
                DecompositionSolver solver = svd.getSolver();
                RealMatrix c = solver.solve(b);
                coefs = c.getColumn(0);
            }           
            A = updateMatrices(A, b, coefs, xs, ys);
        }
        
        mean = (float)(-coefs[1]/(2 * coefs[2]));
        amplitude = (float)(FastMath.exp(coefs[0]-coefs[1]*coefs[1]/(4*coefs[2])));
        
        return new float[] {mean, amplitude};
	}
	 
	@Override
	public MaxCrossResult interpolate(Matrix m, MaxCrossResult result) {
	    for (int peakIndex = 0; peakIndex < result.getTotalPeaks(); peakIndex++) {
    		int maxI = (int)result.getNthPeakI(peakIndex);
    		int maxJ = (int)result.getNthPeakJ(peakIndex);
    		
    		if (maxI == 0.0f && maxJ == 0.0f) {
    		    result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
    		    continue;
    		}
    					
            if (maxI - pixels/2 < 0 || maxI + pixels/2 >= m.getHeight()) {
                if (peakIndex == 0) {
                    logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                            "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                            peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                } else {
                    logger.debug("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                            "{}, dimJ: {}], max. is at: [I: {}, J: {}]", 
                            peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                }
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }
            
            if (maxJ - pixels/2 < 0 || maxJ + pixels/2 >= m.getWidth()) {
                if (peakIndex == 0) {
                    logger.warn("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                            m.getHeight() + ", dimJ: " + m.getWidth() + "], max. is at: [I: " + maxI + ", J: " + maxJ + "]",
                            peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                } else {
                    logger.debug("Peak index: {} - Cannot interpolate for {} interpolating points. It would access outside matrix M[dimI: " + 
                            m.getHeight() + ", dimJ: " + m.getWidth() + "], max. is at: [I: " + maxI + ", J: " + maxJ + "]",
                            peakIndex, pixels, m.getHeight(), m.getWidth(), maxI, maxJ);
                }
                result.setNthRelativeDisplacementFromPeak(peakIndex, result.getNthPeakI(peakIndex), result.getNthPeakJ(peakIndex));
                continue;
            }

            float minFloor = result.getMinFloor();
            if (minFloor < -5.15f) {
                throw new InterpolateException("Hongwei-Guo Gaussian 1D-1D cannot handle large negative floor level of: " + minFloor);
            }
            
            float addMinFloor = 0.0f;
            if (minFloor < 0.0f) {
                addMinFloor = -minFloor + 1e-8f;
            }
                       
            float xs[] = new float[pixels];
            float ys[] = new float[pixels];            
            //Get points in y direction
            for (int i = 0, matrixI = maxI - pixels/2; matrixI <= maxI + pixels/2; i++, matrixI++) {
                xs[i] = matrixI;
                ys[i] = m.getElement(matrixI, maxJ) + addMinFloor;                 
            }
           
            float[] yInterpResult = estimateGaussianParameters(iterations, xs, ys);
                        
            //Get points in x direction
            for (int j = 0, matrixJ = maxJ - pixels/2; matrixJ <= maxJ + pixels/2; j++, matrixJ++) {
                xs[j] = matrixJ; 
                ys[j] = m.getElement(maxI, matrixJ) + addMinFloor;                 
            }
            float[] xInterpResult = estimateGaussianParameters(iterations, xs, ys);

            float finalY = yInterpResult[0];
            //Ensure that if this is a valid sub-pixel interpolation, that the displacement adjustment is below 1.0px
            boolean yValid = true;
            if (FastMath.abs(yInterpResult[0] - (float)maxI) > pixels/2.0f) {
                //Invalid sub-pixel interpolation on y direction
                if (peakIndex == 0) {
                    logger.warn("Peak index: {} - Cannot do sub-pixel interpolatation, peak would be farther away than 1px in"
                            + " the y direction [I: {}, J:{}], using integer cross-peak instead: [I: {}, J: {}]", 
                            peakIndex, yInterpResult[0], xInterpResult[0], maxI, maxJ);
                } else {
                    logger.debug("Peak index: {} - Cannot do sub-pixel interpolatation, peak would be farther away than 1px in"
                            + " the y direction [I: {}, J:{}], using integer cross-peak instead: [I: {}, J: {}]", 
                            peakIndex, yInterpResult[0], xInterpResult[0], maxI, maxJ);
                }
                finalY = maxI; //For now, just use the integer value, but one may which to try the polynomial 1D-1D interpolation
                yValid = false;
            }

            float finalX = xInterpResult[0];
            //Ensure that if this is a valid sub-pixel interpolation, that the displacement adjustment is below 1.0px
            boolean xValid = true;
            if (FastMath.abs(xInterpResult[0] - (float)maxJ) > pixels/2.0f) {
                //Invalid sub-pixel interpolation on x direction
                if (peakIndex == 0) {
                    logger.warn("Peak index: {} - Cannot do sub-pixel interpolatation, peak would be farther away than 1px in"
                            + " the x direction [I: {}, J:{}], using integer cross-peak instead: [I: {}, J: {}]", 
                            peakIndex, yInterpResult[0], xInterpResult[0], maxI, maxJ);
                } else {
                    logger.debug("Peak index: {} - Cannot do sub-pixel interpolatation, peak would be farther away than 1px in"
                            + " the x direction [I: {}, J:{}], using integer cross-peak instead: [I: {}, J: {}]", 
                            peakIndex, yInterpResult[0], xInterpResult[0], maxI, maxJ);
                }
                finalX = maxJ; //For now, just use the integer value, but one may which to try the polynomial 1D-1D interpolation
                xValid = false;
            }
            
            result.setNthRelativeDisplacementFromPeak(peakIndex, finalY, finalX);
            float max = result.getNthPeakValue(peakIndex);
            if (yValid) {
                max = yInterpResult[1];
                if (xValid && xInterpResult[1] > max) {
                    max = xInterpResult[1];
                }
            } else if (xValid) {
                max = xInterpResult[1];
            }
            result.setNthPeakValue(peakIndex, max);		
	    }
	    
		return result;
	}

}
