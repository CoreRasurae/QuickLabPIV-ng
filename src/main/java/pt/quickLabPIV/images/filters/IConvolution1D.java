package pt.quickLabPIV.images.filters;

public interface IConvolution1D {
   /**
    * Computes the 1D convolution of two 1D input arrays and writes the output with the same length of the largest 1D input array.
    * <br/>NOTES:
    * <ul><li>The convolution implementation must not require a power of two in the input and output dimensions.</li>
    * <li>The implementations of this interface are intended to convolve small kernels, and should be optimized for that end only.</li></ul>
    * 
    * @param a the input vector a (1D input array)
    * @param sizeA the size of the relevant data to convolve for the array a
    * @param b the input vector b (1D input array)
    * @param sizeB the size of the relevant data to convolve for the array b
    * @param result the previous output vector for reuse, if null a new one will be created
    * @return the convolution result
    */
   public float[] convolve1D(float[] a, int sizeA, float[] b, int sizeB, float[] result);
}