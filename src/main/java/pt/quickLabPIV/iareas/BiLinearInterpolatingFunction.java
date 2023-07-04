package pt.quickLabPIV.iareas;

public class BiLinearInterpolatingFunction {
    private final double ys[];
    private final double xs[];
    private final double values[][];
    
    
    public BiLinearInterpolatingFunction(double _ys[], double _xs[], double _values[][]) {
        //Check that xs and ys are in monotonic increasing order
        double testY = _ys[0];
        for (int idxI = 1; idxI < _ys.length; idxI++) {
            if (testY >= _ys[idxI]) {
                throw new InterpolateException("Cannot create interpolator function: Ys are not monotonically increasing "); 
            }
            testY = _ys[idxI];
        }

        double testX = _xs[0];
        for (int idxJ = 1; idxJ < _xs.length; idxJ++) {
            if (testX >= _xs[idxJ]) {
                throw new InterpolateException("Cannot create interpolator function: Xs are not monotonically increasing ");
            }
            testX = _xs[idxJ];
        }
        
        //Check that the values function map has matching dimensions to the points matrix
        if (_values.length != _ys.length) {
            throw new InterpolateException("Cannot create interpolator function: There a more function values than Ys points ");
        }

        if (_values[0].length != _xs.length) {
            throw new InterpolateException("Cannot create interpolator function: There a more function values than Xs points ");
        }
        
        ys = _ys;
        xs = _xs;
        values = _values;
    }

    public double value(double y, double x) {
        //TODO How to deal with invalid vectors on inheritance? Masked vectors?
        double result = Float.NaN;
        
        if (y < ys[0] || y > ys[ys.length - 1]) {
            throw new InterpolateException("Cannot interpolate: Requested y is outside of the data range");
        }
        
        if (x < xs[0] || x > xs[xs.length - 1]) {
            throw new InterpolateException("Cannot interpolate: Requested x is outside of the data range");
        }
        
        //TODO Eventually replace by binary search
        int idxI;
        for (idxI = 1; idxI < ys.length; idxI++) {
            if (y <= ys[idxI]) {
                break;
            }
        }
        
        int idxJ;
        for (idxJ = 1; idxJ < xs.length; idxJ++) {
            if (x <= xs[idxJ]) {
                break;
            }
        }
        
        //Compute weights
        double weightY = 1.0 - ((ys[idxI] - y) / (ys[idxI] - ys[idxI - 1]));
        double weightX = 1.0 - ((xs[idxJ] - x) / (xs[idxJ] - xs[idxJ - 1])); 
        
        //Computed weighted bilinear interpolated value
        result = weightY * (weightX * values[idxI][idxJ] + (1.0 - weightX) * values[idxI][idxJ-1]) + 
                (1.0 - weightY) * (weightX * values[idxI-1][idxJ] + (1.0 - weightX) * values[idxI-1][idxJ-1]);
        
        return result;
    }
}
