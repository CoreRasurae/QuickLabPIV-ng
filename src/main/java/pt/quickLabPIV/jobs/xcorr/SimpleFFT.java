package pt.quickLabPIV.jobs.xcorr;

import org.apache.commons.math3.util.FastMath;

public class SimpleFFT {
    private float w0[], w1[];
    private float ws[][] = {w0, w1};

    public static boolean DUMP_INFO = false;

    public enum FFTRange {
        LowerHalf,
        UpperHalf,
        Full
    }
    
    public SimpleFFT(int length1, int length2) {
        initEulerTable(length1, 0);
        initEulerTable(length2, 1);
    }

    /**
     * Shuffles the input array data to mimic the FFT expansion phase, without needing to allocate additional memory (perfect shuffle).
     * @param inputArray the input array to be perfect shuffled into initial FFT order
     */
    public void perfectShuffleFFTInput(float[] inputArray) {
        int inputSize = inputArray.length;
        //Each outer loop performs an FFT ordering at the respective depth-level: moving the even indices to the left (even side)
        //ordered by increasing index order of even indices and moving the odd indices to the right (odd side) ordered by increseaing order
        //of odd indices.
        //Example:
        //        Level 1                              Level  2                                       Level 3
        //[ a0 a1 a2 a3 a4 a5 a6 a7 ] -> [ [ a0 a2 a4 a6 ] [ a1 a3 a5 a7 ] ] -> [ [ [ a0 a4 ] [ a2 a6 ] ] [ [ a1 a5 ] [ a3 a7 ] ] ]
        //Computational Complexity: Not easy to assess (N=8 -> 6, N=16 -> 24, N=32 -> 80)
        int swaps = 0;
        for (int outerSplitFactor=1, outerDepthSize = inputSize; outerDepthSize > 2; outerDepthSize >>>= 1, outerSplitFactor <<= 1) {
            for (int outerSplitIndex = 0; outerSplitIndex < outerSplitFactor; outerSplitIndex++) {
                int outerOffset =  outerSplitIndex * outerDepthSize;
                //These inner loops start by interchanging the odd elements of the even-side (left side) with event elements
                //of the odd-side. As this is not exactly the desired ordering for the FFT at this depth level, additional similar shuffles 
                //must be performed to bring align the indices into order.
                //Example:
                //[ a0 a1 a2 a3 a4 a5 a6 a7 ] -> [ [a0 a4 a2 a6] [a1 a5 a3 a7] -> [ [a0 a2] [a4 a6] [a1 a3] [a5 a7] ] 
                //[ [a0 a2 a4 a6] [a1 a3 a5 a7] ] (finally in FFT order for the second depth level)
                for (int splitFactor=1, depthSize = outerDepthSize; depthSize > 2; depthSize >>>= 1, splitFactor <<= 1) {
                    for (int splitIndex = 0; splitIndex < splitFactor; splitIndex++) {
                        int innerOffset = splitIndex * depthSize;
                        for (int i = 0; i < depthSize/2; i+=2) {
                            int src = outerOffset + innerOffset + i + 1;
                            int dst = outerOffset + innerOffset + depthSize/2 + i;
                            float temp = inputArray[src];
                            swaps++;
                            inputArray[src] = inputArray[dst];
                            inputArray[dst] = temp;
                        }
                    }
                }
            }
        }
        if (DUMP_INFO) {
            System.out.println("Total swaps: " + swaps);
        }
    }

   public static void printArray(int[] inputArray) {
       if (DUMP_INFO) {
            System.out.print("[ ");
            for (int i = 0; i < inputArray.length; i++) {
                System.out.print(inputArray[i] + ", ");
            }
            System.out.println(" ]");
       }
    }
    
    public static void printArray(float[] inputArray) {
        if (DUMP_INFO) {
            System.out.print("[ ");
            for (int i = 0; i < inputArray.length; i++) {
                System.out.print(inputArray[i] + ", ");
            }
            System.out.println(" ]");
        }
    }
    
    /**
     * <b>NOTE:</b> it is required that the FFT reduction phase reordering has already been performed before calling this method.
     * Otherwise the FFT will be computed wrongly.
     * @param xr
     * @param xi
     */
    public void computeFFTSerial(final float xr[], final float xi[]) {
        final int N = xr.length;
        int step = N;
        for (int level=1; level < N; level <<= 1) {
            int level2 = level << 1;
            step >>>= 1;
            if (DUMP_INFO) {
                System.out.println("#############At level: " + level + " ######################");
            }
            for (int tidx = 0; tidx < N/2; tidx++) {
                computeFFTStep(xr, xi, step, level, level2, tidx, N);
            }           
        }
    }
    
    /**
     * Compute a FFT parallel step... this is the FFT kernel. 
     * @param xr the real      input vector component
     * @param xi the imaginary input vector component
     * @param step the current FFT block size (in the expansion phase)
     * @param level the current FFT size at the current depth level
     * @param level2 the FFT size at next depth level
     * @param tidx the index within the current FFT size
     * @param N the original FFT size
     */
    public void computeFFTStep(final float[] xr, final float[] xi, final int step, int level, int level2, int tidx, final int N) {
        //
        int coordIndex = N == ws[0].length ? 0 : 1;
        //step * (tid % level) - splits the fft into the fft blocks index expected at a given fft depth level.
        //
        //example A: depth Level 1 - two mixed blocks of N/2 
        //step=N/2
        //level=1
        //k is always zero for every tidx - the second N/2 half is automatically handled by the code below.
        //                                  simple signal changes to the symmetric of the intermixed second half.
        //
        //example B: depth Level 2 - four blocks of N/4, that means two intermixed blocks in the first N/2 mixed half that must be handled
        //step=N/4
        //level=2
        //k is N/4 * (tidx % 2)
        //tidx even goes to k=0  ( 0 degrees = 1)
        //tidx odd goes to k=N/4 (90 degrees = j)
        //
        int k = step * (tidx % level);           //twiddle index in sine table, also matches Euler index
        
        //Example A: depth Level 1 - two mixed blocks of N/2 (with N=8)
        //tidx=0, level=1, level2=2 -> i=0/1*2+0 = 0, j=1
        //tidx=1, level=1, level2=2 -> i=1/1*2+0 = 2, j=3
        //tidx=2, level=1, level2=2 -> i=2/1*2+0 = 4, j=5
        //tidx=3, level=1, level2=2 -> i=3/1*2+0 = 6, j=7
        //
        //Example B: depth Level 2 - four mixed blocks of N/4 (with N=8)
        //tidx=0, level=2, level2=4 -> i=0/2*4+(0%2) = 0, j=2
        //tidx=1, level=2, level2=4 -> i=1/2*4+(1%2) = 1, j=3
        //tidx=2, level=2, level2=4 -> i=2/2*4+(2%2) = 4, j=6
        //tidx=3, level=2, level2=4 -> i=3/2*4+(3%2) = 5, j=7
        //
        int i = (tidx/level)*level2+(tidx%level); //source index (even index)
        int j = i + level;                        //destination index (odd index)
        
        if (DUMP_INFO) {
            System.out.println("level: " + level + ", tidx: " + tidx + ", k: " + k + ", i: " + i + ", j: " + j);
        }
        
        //NOTES A)
        //Cos(x) = Sin(Pi/2 - x)
        //
        //n=N/4 in table (w) is -> 2*Pi*n/N = 2*Pi*N/4/N/1 = 2*Pi*1/4 = Pi/2
        //
        //Since Euler function is exp(-2j*Pi*n/N) = cos(-2*Pi*n/N) + j sin(-2*Pi*n/N)
        //and cos(k) = sin(Pi/2 - k) -> cos(-2*Pi*k/N) = sin(Pi/2 - (- 2*Pi*k/N)) = sin(Pi/2 + 2*Pi*k/N)
        //and j sin(-2*Pi*k/N) = - j sin(2*Pi*k/N)
        //Thus Euler table (w) can have only the values of sin(2*Pi*n/N) and
        // w(k + N/4) = sin(2*Pi*k/N + Pi/2)
        // and
        //-w(k) = - j sin(2*Pi*k/N)
        float wr = ws[coordIndex][k + N/4]; //twiddle value, real part
        float wi = -ws[coordIndex][k];      //twiddle value, imaginary part
        
        //FFT Exploit N/2 Euler symmetries of complex products
        
        /*//Not working well... due to likely compiler instruction reordering, although prints don't evidence such...
        System.out.println("A: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);
        //Handle the lower half = Ek + Wk Ok
        float tempLowerHalfReal = xr[i] + (wr*xr[j]) - (wi*xi[j]);
        float tempLowerHalfImg  = xi[i] + (wr*xi[j]) + (wi*xr[j]);
        
        System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);
        //Handle the upper half = Ek - Wk Ok
        xr[j] = xr[i] - (wr*xr[j]) + (wi*xi[j]);
        xi[j] = xi[i] - (wr*xi[j]) - (wi*xr[j]);
        System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);

        xr[i] = tempLowerHalfReal;
        xi[i] = tempLowerHalfImg;
        System.out.println("D: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i] + ", Xr[j]: " + xr[j] + ", Xi[j]: " + xi[j]);*/
        
        
        //Option A: That is working properly... Apparently saving both results to temporary variables allows proper instruction ordering. 
        //System.out.println("A: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
        float tempLowerHalfReal = xr[i] + (wr*xr[j]) - (wi*xi[j]);
        float tempLowerHalfImg  = xi[i] + (wr*xi[j]) + (wi*xr[j]);
        
        //Handle the upper half = Ek - Wk Ok
        float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
        float tempUpperHalfImg = xi[i] - (wr*xi[j]) - (wi*xr[j]);
        //System.out.println("B: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);

        xr[i] = tempLowerHalfReal;
        xi[i] = tempLowerHalfImg;
        //System.out.println("C: Xr[i]: " + xr[i] + ", Xi[i]: " + xi[i]);
        xr[j] = tempUpperHalfReal;
        xi[j] = tempUpperHalfImg;
        
        //Option B: This one is working with the compiler.... by switching the order of the temporary results
        /*float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
        float tempUpperHalfImg  = xi[i] - (wr*xi[j]) - (wi*xr[j]);
        
        //Handle the lower half = Ek - Wk Ok
        xr[i] = xr[i] + (wr*xr[j]) - (wi*xi[j]);
        xi[i] = xi[i] + (wr*xi[j]) + (wi*xr[j]);

        xr[j] = tempUpperHalfReal;
        xi[j] = tempUpperHalfImg;*/
    }

    /**
     * <b>NOTE:</b> it is required that the IFFT reduction phase reordering has already been performed before calling this method.
     * Otherwise the FFT will be computed wrongly.
     * @param xr
     * @param xi
     */
    public void computeIFFTSerial(float xr[], float xi[]) {
        divideArray(xr, xr.length);
        divideArray(xi, xr.length);

        final int N = xr.length;        
        int step = N;
        for (int level=1; level < N; level <<= 1) {
            int level2 = level << 1;
            step >>>= 1;
            for (int tidx = 0; tidx < N/2; tidx++) {
                computeIFFTStep(xr, xi, step, level, level2, tidx, N);
            }           
        }       
    }

    
    /**
     * Compute an IFFT parallel step... this is the FFT kernel. 
     * @param xr the real      input vector component
     * @param xi the imaginary input vector component
     * @param step the current FFT block size (in the expansion phase)
     * @param level the current FFT size at the current depth level
     * @param level2 the FFT size at next depth level
     * @param tidx the index within the current FFT size
     * @param N the original FFT size
     */
    public void computeIFFTStep(float[] xr, float[] xi, final int step, int level, int level2, int tidx, final int N) {
        int coordIndex = N == ws[0].length ? 0 : 1;
        //step * (tid % level) - splits the fft into the fft blocks index expected at a given fft depth level.
        //
        //example A: depth Level 1 - two mixed blocks of N/2 
        //step=N/2
        //level=1
        //k is always zero for every tidx - the second N/2 half is automatically handled by the code below.
        //                                  simple signal changes to the symmetric of the intermixed second half.
        //
        //example B: depth Level 2 - four blocks of N/4, that means two intermixed blocks in the first N/2 mixed half that must be handled
        //step=N/4
        //level=2
        //k is N/4 * (tidx % 2)
        //tidx even goes to k=0  ( 0 degrees = 1)
        //tidx odd goes to k=N/4 (90 degrees = j)
        //
        int k = step * (tidx % level);           //twiddle index in sine table, also matches Euler index
        
        //Example A: depth Level 1 - two mixed blocks of N/2 (with N=8)
        //tidx=0, level=1, level2=2 -> i=0/1*2+0 = 0, j=1
        //tidx=1, level=1, level2=2 -> i=1/1*2+0 = 2, j=3
        //tidx=2, level=1, level2=2 -> i=2/1*2+0 = 4, j=5
        //tidx=3, level=1, level2=2 -> i=3/1*2+0 = 6, j=7
        //
        //Example B: depth Level 2 - four mixed blocks of N/4 (with N=8)
        //tidx=0, level=2, level2=4 -> i=0/2*4+(0%2) = 0, j=2
        //tidx=1, level=2, level2=4 -> i=1/2*4+(1%2) = 1, j=3
        //tidx=2, level=2, level2=4 -> i=2/2*4+(2%2) = 4, j=6
        //tidx=3, level=2, level2=4 -> i=3/2*4+(3%2) = 5, j=7
        //
        int i = (tidx/level)*level2+(tidx%level); //source index (even index)
        int j = i + level;                        //destination index (odd index)
        
        //NOTES A)
        //Cos(x) = Sin(Pi/2 - x)
        //
        //n=N/4 in table (w) is -> 2*Pi*n/N = 2*Pi*N/4/N/1 = 2*Pi*1/4 = Pi/2
        //
        //Since Euler function is exp(2j*Pi*n/N) = cos(2*Pi*n/N) + j sin(2*Pi*n/N)
        //and cos(k) = sin(Pi/2 - k) -> cos(2*Pi*k/N) = sin(Pi/2 - 2*Pi*k/N)
        //Thus Euler table (w) can have only the values of sin(2*Pi*n/N) and
        // w(N/4 - k) = sin(Pi/2 - 2*Pi*k/N)
        // and
        //w(k) = j sin(2*Pi*k/N)
        float wr;
        if (N/4 - k >= 0) {
            wr = ws[coordIndex][N/4 - k]; //twiddle value, real part
        } else {
            //Compensate negative indices by summing 2Pi (N)
            wr = ws[coordIndex][N + (N/4 - k)]; //twiddle value, real part
        }
        float wi = ws[coordIndex][k];       //twiddle value, imaginary part
        
        //FFT Exploit N/2 Euler symmetries of complex products
        //Handle the upper half = Ek - Wk Ok 
        float tempUpperHalfReal = xr[i] - (wr*xr[j]) + (wi*xi[j]);
        float tempUpperHalfImg  = xi[i] - (wr*xi[j]) - (wi*xr[j]);
        
        //Handle the lower half = Ek - Wk Ok
        xr[i] = xr[i] + (wr*xr[j]) - (wi*xi[j]);
        xi[i] = xi[i] + (wr*xi[j]) + (wi*xr[j]);

        xr[j] = tempUpperHalfReal;
        xi[j] = tempUpperHalfImg;
    }

    
    private void initEulerTable(int N, int coordIndex) {
        ws[coordIndex] = new float[N];
        
        for (int n = 0; n < N; n++) {
            //This should be the Euler table of exp(-2j*Pi*n/N) however due to the NOTES A) above, it suffices to have sin(2*Pi*n/N)
            ws[coordIndex][n] = (float)FastMath.sin(2.0f*FastMath.PI*(float)n/(float)N);
        }
    }
    
    
    public void divideArray(float [] inputArray, float value) {
        for (int index = 0; index < inputArray.length; index++) {
            inputArray[index] /= value;
        }
    }
    
    public void conjugate(final float[] xr, final float[] xi) {
        for (int index = 0; index < xr.length; index++) {
            xi[index] = -xi[index];
        }
    }

    public void computeIFFTSerialA(float[] xr, float[] xi) {
        computeFFTSerial(xi, xr);
        divideArray(xr, xr.length);
        divideArray(xi, xr.length);
    }

    public void computeIFFTSerialB(float[] xr, float[] xi) {
        conjugate(xr, xi);
        computeFFTSerial(xr, xi);
        conjugate(xr, xi);
        divideArray(xr, xr.length);
        divideArray(xi, xr.length);
    }

    public void computeIFFTSerialC(float[] xr, float[] xi) {
        divideArray(xr, xr.length);
        divideArray(xi, xr.length);
        computeFFTSerial(xi, xr);
    }

    public void computeIFFTSerialD(float[] xr, float[] xi) {
        divideArray(xr, xr.length);
        divideArray(xi, xr.length);
        conjugate(xr, xi);
        computeFFTSerial(xr, xi);
        conjugate(xr, xi);
    }
    
    @FunctionalInterface
    interface FFTfunction <A, B> { 
        //R is like Return, but doesn't have to be last in the list nor named R.
        public void apply (A a, B b);
    }
    
    void computeFFT2DSerial(final float[][] xr, final float[][] xi, FFTRange range, FFTfunction<float[],float[]> fftMethod) {
        int startIndex = 0;
        int endIndex = xr.length;
        
        if (range == FFTRange.UpperHalf) {
            startIndex = xr.length / 2 - 1;
        } else if (range == FFTRange.LowerHalf) {
            endIndex = xr.length / 2;
        }

        for (int i = startIndex; i < endIndex; i++) {
            perfectShuffleFFTInput(xr[i]);
            perfectShuffleFFTInput(xi[i]);
            fftMethod.apply(xr[i], xi[i]);
        }
        
        //dump2DArray("xr2D_1Half", xr);
        //dump2DArray("xi2D_1Half", xi);
        
        float newXr[][] = new float[xr[0].length][xr.length];
        float newXi[][] = new float[xr[0].length][xr.length];
        
        //Transpose matrix....
        for (int i = 0; i < xr.length; i++) {
            for (int j = 0; j < xr[0].length; j++) {
                newXr[j][i] = xr[i][j];
                newXi[j][i] = xi[i][j];
            }
        }
        
        //dump2DArray("xr2D_Trn1", xr);
        //dump2DArray("xi2D_Trn1", xi);
        
        for (int i = 0; i < newXr.length; i++) {
            perfectShuffleFFTInput(newXr[i]);
            perfectShuffleFFTInput(newXi[i]);
            fftMethod.apply(newXr[i], newXi[i]);
        }

        if (DUMP_INFO) {
            dump2DArray("xr2D_2Half", newXr);
            dump2DArray("xi2D_2Half", newXi);
        }
        
        //Transpose again....
        for (int i = 0; i < newXr.length; i++) {
            for (int j = 0; j < newXr[0].length; j++) {
                float tempRe = newXr[i][j];
                float tempIm = newXi[i][j];
                xr[j][i]=tempRe;
                xi[j][i]=tempIm;
            }
        }
    }
    
    public void computeFFT2D(float[][] xr, float[][] xi) { 
        initEulerTable(xr.length, 0);
        initEulerTable(xr[0].length, 1);
                
        computeFFT2DSerial(xr, xi, FFTRange.Full, (float[] lambdaXr, float[] lambdaXi) -> computeFFTSerial(lambdaXr, lambdaXi));
    }
    
    public void computeIFFT2D(float[][] xr, float[][] xi) {
        initEulerTable(xr.length, 0);
        initEulerTable(xr[0].length, 1);
        
        computeFFT2DSerial(xr, xi, FFTRange.Full, (float[] lambdaXr, float[] lambdaXi) -> computeIFFTSerial(lambdaXr, lambdaXi));
    }
    
    public static void dump2DArray(String name, int[][] xr) {
        if (DUMP_INFO) {
            System.out.println(name);
            for (int i = 0; i < xr.length; i++) {
                printArray(xr[i]);
            }
        }
     }

     
     public static void dump2DArray(String name, float[][] xr) {
         if (DUMP_INFO) {
             System.out.println(name);
             for (int i = 0; i < xr.length; i++) {
                 printArray(xr[i]);
             }
         }
     }

     public void mirrorNonZeroValues(float yr[][]) {
         //Mirror yr only, only mirror non-zero padded region. yi is all zeros and doesn't need mirroring        
         for (int i = 0; i < yr.length / 4; i++) {
             for (int j = 0; j < yr[0].length / 2; j++) {
                 float tempRe = yr[yr.length / 2 - 1 - i][j];
                 yr[yr.length / 2 - 1 - i][j] = yr[i][j];
                 yr[i][j] = tempRe;
             }
         }
         
         for (int i = 0; i < yr.length / 2; i++) {
             for (int j = 0; j < yr[0].length / 4; j++) {
                 float tempRe = yr[i][yr[0].length / 2 - 1 - j];
                 yr[i][yr[0].length / 2 - 1 - j] = yr[i][j];
                 yr[i][j] = tempRe;
             }
         }       
     }   

     
     public void computeCrossCorrelationFFT2DSerial(float xr[][], float xi[][], float yr[][], float yi[][]) {
         //NOTE: This computes the cross-correlation through FFT just like the ViPIVIST-ng by Definition implementations both GPU and CPU junit.
         //To compute like MATLAB exchange xr with yr (i.e. y[-n,-m]*x[n,m] instead of x[-n,-m]*y[n,m]
         mirrorNonZeroValues(xr);
         
         computeFFT2DSerial(xr, xi, FFTRange.LowerHalf, (float[] lambdaXr, float[] lambdaXi) -> computeFFTSerial(lambdaXr, lambdaXi));
         computeFFT2DSerial(yr, yi, FFTRange.LowerHalf, (float[] lambdaXr, float[] lambdaXi) -> computeFFTSerial(lambdaXr, lambdaXi));
         
         //dump2DArray("xr", xr);
         //dump2DArray("xi", xi);
         
         //By now xr, xi and yr, yi have the FFT transform with real and imaginary values
         //NOTE: That cross-correlation is being computed by a convolution, thus the product must be made like x[-n, -m]*y[n, m],
         //only the non-zero valued data is reversed before padding with zeros.
         for (int i = 0; i < xr.length; i++) {
             for (int j = 0; j < xr[0].length; j++) {
                 //(xr + j xi) * (yr + j yi) = (xr * yr - xi * yi) + j (xr * yi + xi * yr)
                 float tempRe = xr[i][j]*yr[i][j]-xi[i][j]*yi[i][j];
                 float tempIm = xr[i][j]*yi[i][j]+xi[i][j]*yr[i][j];
                 xr[i][j] = tempRe;
                 xi[i][j] = tempIm;
             }
         }

         if (DUMP_INFO) {
             dump2DArray("xrProd", xr);
             dump2DArray("xiProd", xi);
             System.out.println("##### IFFT2D #####");
         }
         
         computeFFT2DSerial(xr, xi, FFTRange.Full, (float[] lambdaXr, float[] lambdaXi) -> computeIFFTSerial(lambdaXr, lambdaXi));
     }
     
}
