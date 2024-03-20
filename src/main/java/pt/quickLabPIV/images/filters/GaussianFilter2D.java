// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

import org.apache.commons.math3.util.FastMath;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;

public final class GaussianFilter2D implements IFilter {
    final float sigma;
    final GaussianFilter2DInternal<IImage> imageFilter;
    final GaussianFilter2DInternal<Matrix> matrixFilter;
    final GaussianFilter2DInternal.GetWidthFunction<Matrix> gwMatrix;
    final GaussianFilter2DInternal.GetHeightFunction<Matrix> ghMatrix;
    final GaussianFilter2DInternal.ReaderFunction<Matrix> rMatrix;
    final GaussianFilter2DInternal.WriterFunction<Matrix> wMatrix;
    final GaussianFilter2DInternal.GetWidthFunction<IImage> gwImage;
    final GaussianFilter2DInternal.GetHeightFunction<IImage> ghImage;
    final GaussianFilter2DInternal.ReaderFunction<IImage> rImage;
    final GaussianFilter2DInternal.WriterFunction<IImage> wImage;
    
    private static final float[] createKernel(float sigma, int kernelPx) {
        double[] kd = new double[kernelPx];
        float[] k = new float[kernelPx];
        int halfKernelPx = kernelPx / 2;
        
        double sum = 0.0;
        for (int i = 0; i < kernelPx; i++) {            
           double x = i - halfKernelPx;
           kd[i] = 1.0 / FastMath.sqrt(2.0 * FastMath.PI * sigma * sigma) * FastMath.exp(-x*x / (2.0 * sigma * sigma));
           sum += kd[i];
        }
        for (int i = 0; i < kernelPx; i++) {
            k[i] = (float)(kd[i] / sum);
        }
        return k;
    }

    public GaussianFilter2D(float _sigma, int kernelPx) {
        IConvolution1D convolver = new DirectConvolution();
        float[] kernel = createKernel(_sigma, kernelPx);
        //Templated filter helpers
        matrixFilter = new GaussianFilter2DInternal<Matrix>(kernel, convolver);
        imageFilter = new GaussianFilter2DInternal<IImage>(kernel, convolver);
        //Matrix lambdas
        gwMatrix = (Matrix m) -> m.getWidth();
        ghMatrix = (Matrix m) -> m.getHeight();
        rMatrix = (int i, int j, Matrix m) -> m.getElement(i, j);
        wMatrix = (int i, int j, float v, Matrix m) -> m.setElement(v, i, j); 
        //IImage lambdas
        gwImage = (IImage ii) -> ii.getWidth();
        ghImage = (IImage ii) -> ii.getHeight();
        rImage = (int i, int j, IImage ii) -> ii.readPixel(i, j);
        wImage = (int i, int j, float v, IImage ii) -> ii.writePixel(i, j, v);
        sigma = _sigma;
    }

    
    public GaussianFilter2D() {
        IConvolution1D convolver = new DirectConvolution();
        PIVContextSingleton context = PIVContextSingleton.getSingleton();
        PIVInputParameters inputParameters = context.getPIVParameters();
        GaussianFilter2DConfiguration configuration = (GaussianFilter2DConfiguration)inputParameters.getSpecificConfiguration(GaussianFilter2DConfiguration.IDENTIFER);
        sigma = configuration.getSigma();
        int kernelPx = configuration.getKernelPx();
        float[] kernel = createKernel(sigma, kernelPx);
        //Templated filter helpers
        matrixFilter = new GaussianFilter2DInternal<Matrix>(kernel, convolver);
        imageFilter = new GaussianFilter2DInternal<IImage>(kernel, convolver);
        //Matrix lambdas
        gwMatrix = (Matrix m) -> m.getWidth();
        ghMatrix = (Matrix m) -> m.getHeight();
        rMatrix = (int i, int j, Matrix m) -> m.getElement(i, j);
        wMatrix = (int i, int j, float v, Matrix m) -> m.setElement(v, i, j); 
        //IImage lambdas
        gwImage = (IImage ii) -> ii.getWidth();
        ghImage = (IImage ii) -> ii.getHeight();
        rImage = (int i, int j, IImage ii) -> ii.readPixel(i, j);
        wImage = (int i, int j, float v, IImage ii) -> ii.writePixel(i, j, v);
    }
    
    @Override
    public Matrix applyFilter(Matrix input, Matrix output) {
        if (sigma < 1e-5f) {
            return input;
        }
        
        if (output == null || output.getWidth() != input.getWidth() || output.getHeight() != input.getHeight()) {
            output = new MatrixFloat(input.getHeight(), input.getWidth());
        }
        
        matrixFilter.applyFilter(gwMatrix, ghMatrix, rMatrix, wMatrix, input, output);        
        
        return output;
    }

    @Override
    public float[][] applyFilter(float[][] input, float[][] output) {
        if (sigma == 0.0f) {
            return input;
        }
        
        return matrixFilter.applyFilter(input, output);
    }

    @Override
    public IImage applyFilter(IImage input, IImage output) {
        if (sigma == 0.0f) {
            return input;
        }
        
        if (output == null || output.getWidth() != input.getWidth() || output.getHeight() != input.getHeight()) {
            output = ImageFloat.sizeFrom(input);
        }
        
        imageFilter.applyFilter(gwImage, ghImage, rImage, wImage, input, output);
        
        return output;
    }
    
}
