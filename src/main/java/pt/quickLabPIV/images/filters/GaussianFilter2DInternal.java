package pt.quickLabPIV.images.filters;

class GaussianFilter2DInternal<R> {
    private IConvolution1D convolver;
    private final float[] kernel;
    private float[] bufX;
    private float[] outputX;
    private float[] bufY;
    private float[] outputY;

    GaussianFilter2DInternal(float[] _kernel, IConvolution1D _convolver) {
        kernel = _kernel;
        convolver = _convolver;
    }

    @FunctionalInterface
    interface GetWidthFunction<R> {        
        public int getWidth(R obj);
    }

    @FunctionalInterface
    interface GetHeightFunction<R> {        
        public int getHeight(R obj);
    }
        
    @FunctionalInterface
    interface ReaderFunction<R> {        
        public float reader(int i, int j, R obj);
    }
    
    @FunctionalInterface
    interface WriterFunction<R> {        
        public void writer(int i, int j, float v, R obj);
    }
    
    /** 
     * Internal helper interface for applying a filter to a specific object, while sharing a generic implementation.
     * @param cf CreatorFunction functor to create an object instance
     * @param rf ReaderFunction functor to read a value from the object instance
     * @param wf WriterFunction functor to write a value to the object instance
     * @return an instance of the filtered object
     */
    public R applyFilter(GetWidthFunction<R> gwf, GetHeightFunction<R> ghf, ReaderFunction<R> rf, WriterFunction<R> wf, R input, R output){
        final int width = gwf.getWidth(input);
        final int height = ghf.getHeight(input);
        
        int halfSize = kernel.length / 2;
        if (bufY == null || bufY.length < width + kernel.length - 1) {
            bufY = new float[width + kernel.length - 1];
            outputY = new float[width + kernel.length - 1];
        }
        
        int bufYSize = width + kernel.length - 1;
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bufY[j + halfSize] = rf.reader(i, j, input);
            }
            for (int j = 0; j < halfSize; j++) {
                bufY[halfSize - 1 - j] = rf.reader(i, j, input);
                bufY[bufYSize - 1 - j] = rf.reader(i, width - 1 - j, input);
            }
            outputY = convolver.convolve1D(bufY, bufYSize, kernel, kernel.length, outputY);
            for (int j = 0; j < width; j++) {
                wf.writer(i, j, outputY[j + halfSize], output);
            }
        }
        
        if (bufX == null || bufX.length < height + kernel.length - 1) {
            bufX = new float[height + kernel.length - 1];
            outputX = new float[height + kernel.length - 1];
        }
        
        int bufXSize = height + kernel.length - 1;
        
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < height; i++) {
                bufX[i + halfSize] = rf.reader(i, j, output);
            }
            for (int i = 0; i < halfSize; i++) {
                bufX[halfSize - 1 - i] = rf.reader(i, j, output);
                bufX[bufXSize - 1 - i] = rf.reader(height - 1 - i, j, output);
            }
            outputX = convolver.convolve1D(bufX, bufXSize, kernel, kernel.length, outputX);
            for (int i = 0; i < height; i++) {
                wf.writer(i, j, outputX[i + halfSize], output);
            }
        }
        
        return output;
    }

    public float[][] applyFilter(float[][] input, float[][] output) {
        if (output == null || output.length != input.length || output[0].length != input[0].length) {
            output = new float[input.length][input[0].length];
        }
        
        int halfSize = kernel.length / 2;
        if (bufY == null || bufY.length < input[0].length + kernel.length - 1) {
            bufY = new float[input[0].length + kernel.length - 1];
            outputY = new float[input[0].length + kernel.length - 1];
        }
        
        int bufYSize = input[0].length + kernel.length - 1;
        
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                bufY[j + halfSize] = input[i][j];
            }
            for (int j = 0; j < halfSize; j++) {
                bufY[halfSize - 1 - j] = input[i][j];
                bufY[bufYSize - 1 - j] = input[i][input[0].length - 1 - j];
            }
            outputY = convolver.convolve1D(bufY, bufYSize, kernel, kernel.length, outputY);
            for (int j = 0; j < input[0].length; j++) {
                output[i][j] = outputY[j + halfSize];
            }
        }
        
        if (bufX == null || bufX.length < input.length + kernel.length - 1) {
            bufX = new float[input.length + kernel.length - 1];
            outputX = new float[input.length + kernel.length - 1];
        }
        
        int bufXSize = input.length + kernel.length - 1;
        
        for (int j = 0; j < input[0].length; j++) {
            for (int i = 0; i < input.length; i++) {
                bufX[i + halfSize] = output[i][j];
            }
            for (int i = 0; i < halfSize; i++) {
                bufX[halfSize - 1 - i] = output[i][j];
                bufX[bufXSize - 1 - i] = output[input.length - 1 - i][j];
            }
            outputX = convolver.convolve1D(bufX, bufXSize, kernel, kernel.length, outputX);
            for (int i = 0; i < input.length; i++) {
                output[i][j] = outputX[i + halfSize];
            }
        }
        
        return output;
    }
}
