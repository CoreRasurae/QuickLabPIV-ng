package pt.quickLabPIV.iareas;

import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.exception.InsufficientDataException;

public class BiCubicSplineInterpolatorWithBiLinearBackup {
    private final BicubicSplineInterpolator splineInterpolator = new BicubicSplineInterpolator();
    private final BicubicSplineInterpolatingFunction splineFuncU;
    private final BiLinearInterpolatingFunction biLinearFuncU;
    private final BicubicSplineInterpolatingFunction splineFuncV;
    private final BiLinearInterpolatingFunction biLinearFuncV;
    
    private final float Xmin;
    private final float Xmax;
    private final float Ymin;
    private final float Ymax;
            
    BiCubicSplineInterpolatorWithBiLinearBackup(final double ys[], final double xs[],
                                                  final double us[][], final double vs[][],
                                                  final float _Ymin, final float _Ymax,
                                                  final float _Xmin, final float _Xmax) {
        //TODO Create our own float type and fast BicubicSpline implementation
        BicubicSplineInterpolatingFunction _splineFuncU = null;
        BiLinearInterpolatingFunction _biLinearFuncU = null;
        try {
            _splineFuncU = splineInterpolator.interpolate(ys, xs, us);
        } catch (InsufficientDataException ex) {
            _biLinearFuncU = new BiLinearInterpolatingFunction(ys, xs, us);
        }

        BicubicSplineInterpolatingFunction _splineFuncV = null;
        BiLinearInterpolatingFunction _biLinearFuncV = null;
        try {
            _splineFuncV = splineInterpolator.interpolate(ys, xs, vs);
        } catch (InsufficientDataException ex) {
            _biLinearFuncV = new BiLinearInterpolatingFunction(ys, xs, vs);
        }
        
        splineFuncU = _splineFuncU;
        splineFuncV = _splineFuncV;
        biLinearFuncU = _biLinearFuncU;
        biLinearFuncV = _biLinearFuncV;
        
        Xmin = _Xmin;
        Xmax = _Xmax;
        Ymin = _Ymin;
        Ymax = _Ymax;
    }

    public float[][][] interpolateDisplacements(int top, int left, int width, int height, float[][][] interpolatedResults) {
        if (interpolatedResults == null || interpolatedResults.length < height || interpolatedResults[0].length < width) {
            interpolatedResults = new float[height][width][2];
        }
       
        short i = 0;
        for (float y = top; y < top + height; y += 1.0f) {
            short j = 0;
            for (float x = left; x < left + width; x += 1.0f) {
                float xInterp = x;
                float yInterp = y;
                //PiecewiseBicubicSplineInterpolator is unable to interpolate points outside of the interpolating table,
                //so lets limit the currentTiles interpolating region to the maximum region allowed by the parentTiles,
                //this also seems to be the behavior of the SciPy.fitpack2.RectBivariateSpline
                if (xInterp < Xmin) {
                    xInterp = Xmin;
                } else if (xInterp > Xmax) {
                    xInterp = Xmax;
                }
                
                if (yInterp < Ymin) {
                    yInterp = Ymin;
                } else if (yInterp > Ymax) {
                    yInterp = Ymax;
                }

                float interpolatedU = 0.0f;
                float interpolatedV = 0.0f;
                
                if (splineFuncU != null) {
                    interpolatedU = (float)splineFuncU.value(yInterp, xInterp);
                } else {
                    interpolatedU = (float)biLinearFuncU.value(yInterp, xInterp);
                }
                
                if (splineFuncV != null) {
                    interpolatedV = (float)splineFuncV.value(yInterp, xInterp);
                } else {
                    interpolatedV = (float)biLinearFuncV.value(yInterp, xInterp);
                }               
                
                interpolatedResults[i][j][0] = interpolatedU;
                interpolatedResults[i][j][1] = interpolatedV;
                
                j++;
            }
            i++;
        }

        return interpolatedResults;
    }

    public float[][] interpolateDisplacements(int height, int width, float offsetY, float offsetX, float interpolatedResults[][]) {
        if (interpolatedResults == null || interpolatedResults.length < 2 || interpolatedResults[0].length < height*width) {
            interpolatedResults = new float[2][height*width];
        }
       
        short i = 0;
        for (float y = offsetY; y <= height - offsetY; y += 1.0f) {
            if (y == height) {
                continue;
            }
            
            short j = 0;
            for (float x = offsetX; x <= width - offsetX; x += 1.0f) {
                if (x == width) {
                    continue;
                }
                final int idx = i * width + j;
                
                float xInterp = x;
                float yInterp = y;
                //PiecewiseBicubicSplineInterpolator is unable to interpolate points outside of the interpolating table,
                //so lets limit the currentTiles interpolating region to the maximum region allowed by the parentTiles,
                //this also seems to be the behavior of the SciPy.fitpack2.RectBivariateSpline
                if (xInterp < Xmin) {
                    xInterp = Xmin;
                } else if (xInterp > Xmax) {
                    xInterp = Xmax;
                }
                
                if (yInterp < Ymin) {
                    yInterp = Ymin;
                } else if (yInterp > Ymax) {
                    yInterp = Ymax;
                }

                float interpolatedU = 0.0f;
                float interpolatedV = 0.0f;
                
                if (splineFuncU != null) {
                    interpolatedU = (float)splineFuncU.value(yInterp, xInterp);
                } else {
                    interpolatedU = (float)biLinearFuncU.value(yInterp, xInterp);
                }
                
                if (splineFuncV != null) {
                    interpolatedV = (float)splineFuncV.value(yInterp, xInterp);
                } else {
                    interpolatedV = (float)biLinearFuncV.value(yInterp, xInterp);
                }               
                
                interpolatedResults[0][idx] = interpolatedU;
                interpolatedResults[1][idx] = interpolatedV;
                
                j++;
            }
            i++;
        }
        
        return interpolatedResults;
    }
    
    public float interpolateLocationForU(double y, double x) {
        double xInterp = x;
        double yInterp = y;
        
        if (xInterp < Xmin) {
            xInterp = Xmin;
        } else if (xInterp > Xmax) {
            xInterp = Xmax;
        }
        
        if (yInterp < Ymin) {
            yInterp = Ymin;
        } else if (yInterp > Ymax) {
            yInterp = Ymax;
        }

        float interpolatedU = 0.0f;
        
        if (splineFuncU != null) {
            interpolatedU = (float)splineFuncU.value(yInterp, xInterp);
        } else {
            interpolatedU = (float)biLinearFuncU.value(yInterp, xInterp);
        }
        
        return interpolatedU;
    }

    public float interpolateLocationForV(double y, double x) {
        double xInterp = x;
        double yInterp = y;
        
        if (xInterp < Xmin) {
            xInterp = Xmin;
        } else if (xInterp > Xmax) {
            xInterp = Xmax;
        }
        
        if (yInterp < Ymin) {
            yInterp = Ymin;
        } else if (yInterp > Ymax) {
            yInterp = Ymax;
        }

        float interpolatedV = 0.0f;
        
        if (splineFuncV != null) {
            interpolatedV = (float)splineFuncV.value(yInterp, xInterp);
        } else {
            interpolatedV = (float)biLinearFuncV.value(yInterp, xInterp);
        }
        
        return interpolatedV;
    }

    public static BiCubicSplineInterpolatorWithBiLinearBackup createTileDisplacementInterpolator(IterationStepTiles stepTiles) {
        Tile[][] tiles = stepTiles.getTilesArray();
        double us[][] = new double[stepTiles.getNumberOfTilesInI()][stepTiles.getNumberOfTilesInJ()];
        double vs[][] = new double[stepTiles.getNumberOfTilesInI()][stepTiles.getNumberOfTilesInJ()];
        double xs[] = new double[stepTiles.getNumberOfTilesInJ()];
        double ys[] = new double[stepTiles.getNumberOfTilesInI()];
        for (int i = 0; i < tiles.length; i++) {
           for (int j = 0; j < tiles[0].length; j++) {
              us[i][j] = tiles[i][j].getDisplacementU();
              vs[i][j] = tiles[i][j].getDisplacementV();
              if (tiles[i][j].isMaskedDisplacement()) {
                  us[i][j] = 0.0f;
                  vs[i][j] = 0.0f;
              }
              if (i == 0) {
                 xs[j] = tiles[i][j].getLeftPixel() + stepTiles.getTileWidth() / 2.0f - 0.5f;
              }
              if (j == 0) {
                 ys[i] = tiles[i][j].getTopPixel() + stepTiles.getTileHeight() / 2.0f - 0.5f;
              }
           }
        }
        
        float Xmin = (float)xs[0];
        float Xmax = (float)xs[xs.length - 1];
        float Ymin = (float)ys[0];
        float Ymax = (float)ys[ys.length - 1];
        
        return new BiCubicSplineInterpolatorWithBiLinearBackup(ys, xs, us, vs, Ymin, Ymax, Xmin, Xmax);        
     }
    
    public static BiCubicSplineInterpolatorWithBiLinearBackup createDisplacementInterpolator(double[] ys, double[] xs, double us[][], double vs[][]) {
        float Xmin = (float)xs[0];
        float Xmax = (float)xs[xs.length - 1];
        float Ymin = (float)ys[0];
        float Ymax = (float)ys[ys.length - 1];
        
        return new BiCubicSplineInterpolatorWithBiLinearBackup(ys, xs, us, vs, Ymin, Ymax, Xmin, Xmax);
    }
}
