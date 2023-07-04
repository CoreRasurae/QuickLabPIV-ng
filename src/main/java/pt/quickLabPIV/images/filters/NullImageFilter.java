package pt.quickLabPIV.images.filters;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.images.IImage;

public class NullImageFilter implements IFilter {

    @Override
    public Matrix applyFilter(Matrix input, Matrix output) {        
        return input;
    }

    @Override
    public float[][] applyFilter(float[][] input, float[][] output) {
        return input;
    }

    @Override
    public IImage applyFilter(IImage input, IImage output) {
        return input;
    }

}
