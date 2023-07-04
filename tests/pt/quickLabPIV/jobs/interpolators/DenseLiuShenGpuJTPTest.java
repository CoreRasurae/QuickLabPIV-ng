package pt.quickLabPIV.jobs.interpolators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.internal.kernel.KernelManager;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.device.DeviceManager;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFloat;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.images.filters.GaussianFilter2D;
import pt.quickLabPIV.images.filters.IFilter;
import pt.quickLabPIV.interpolators.LiuShenFloat;
import pt.quickLabPIV.interpolators.LiuShenInterpolatorConfiguration;
import pt.quickLabPIV.jobs.JobResultEnum;
import pt.quickLabPIV.jobs.interpolators.DenseLucasKanadeAparapiJob;
import pt.quickLabPIV.jobs.interpolators.LucasKanadeOptions;
import pt.quickLabPIV.jobs.interpolators.OpticalFlowInterpolatorInput;
import pt.quickLabPIV.jobs.interpolators.DenseLiuShenGpuTestKernel.IDenseLiuShenListener;

public class DenseLiuShenGpuJTPTest {
    private final static ComputationDevice gpuDevice = DeviceManager.getSingleton().getGPU();
    
    private int blockItemsPerWorkGroupI;
    private int blockItemsPerWorkGroupJ;
    private int blockSizeI;
    private int blockSizeJ;
    private int globalSizeI;
    private int globalSizeJ;
    private int localSizeI;
    private int localSizeJ;
    private int windowSize;
    private int imageWidth;
    private int imageHeight;
    private int iterations;
    private float imageA[];
    private float imageB[];

    private class JTPKernelManager extends KernelManager {
        private JTPKernelManager() {
            LinkedHashSet<Device> preferredDevices = new LinkedHashSet<Device>(1);
            preferredDevices.add(JavaDevice.THREAD_POOL);
            setDefaultPreferredDevices(preferredDevices);
        }
        @Override
        protected List<Device.TYPE> getPreferredDeviceTypes() {
            return Arrays.asList(Device.TYPE.JTP);
        }
    }

    private class GPUKernelManager extends KernelManager {
        private GPUKernelManager() {
            LinkedHashSet<Device> preferredDevices = new LinkedHashSet<Device>(1);
            preferredDevices.add(gpuDevice.getAparapiDevice());
            setDefaultPreferredDevices(preferredDevices);
        }
        @Override
        protected List<Device.TYPE> getPreferredDeviceTypes() {
            return Arrays.asList(Device.TYPE.GPU);
        }
    }

    private class DefaultKernelManager extends KernelManager {
        
    }
    
    @Before
    public void setup() {
        KernelManager.setKernelManager(new JTPKernelManager());        
        windowSize = 27;
        blockSizeI = 4;
        blockSizeJ = 4;
        localSizeI = 8;
        localSizeJ = 8;

        imageWidth = 43;
        imageHeight = 39;
        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];
        iterations = 1;
        
        blockItemsPerWorkGroupI = (windowSize + blockSizeI-1) / localSizeI;
        if ((windowSize + blockSizeI-1) % localSizeI != 0) {
            blockItemsPerWorkGroupI++;
        }
        blockItemsPerWorkGroupJ = (windowSize + blockSizeJ-1) / localSizeJ;
        if ((windowSize + blockSizeJ-1) % localSizeJ != 0) {
            blockItemsPerWorkGroupJ++;
        }
        
        globalSizeI = imageHeight / blockSizeI;
        if (imageHeight % blockSizeI != 0) {
            globalSizeI++;
        }
        globalSizeI *= localSizeI;
        
        globalSizeJ = imageWidth / blockSizeJ;
        if (imageWidth % blockSizeJ != 0) {
            globalSizeJ++;
        }
        globalSizeJ *= localSizeJ;
    }
    
    @After
    public void tearDown() {
        KernelManager.setKernelManager(new DefaultKernelManager());
    }
    
    @Test
    public void testImageToLocalImgBufferDirectTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];

        globalSizeI = imageHeight / blockSizeI;
        if (imageHeight % blockSizeI != 0) {
            globalSizeI++;
        }
        globalSizeI *= localSizeI;
        
        globalSizeJ = imageWidth / blockSizeJ;
        if (imageWidth % blockSizeJ != 0) {
            globalSizeJ++;
        }
        globalSizeJ *= localSizeJ;

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        
        img1 = new ImageFloat(region1, imageHeight, imageWidth, "img1.png");
        img2 = new ImageFloat(region2, imageHeight, imageWidth, "img2.png");

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);

        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);
        
        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];

        float[][] iixVal = new float[imageHeight][imageWidth];
        float[][] iiyVal = new float[imageHeight][imageWidth];
        float[][] iiVal = new float[imageHeight][imageWidth];
        float[][] ixtVal = new float[imageHeight][imageWidth];
        float[][] iytVal = new float[imageHeight][imageWidth];
        float[][] b00Val = new float[imageHeight][imageWidth];
        float[][] b01Val = new float[imageHeight][imageWidth];
        float[][] b11Val = new float[imageHeight][imageWidth];
        float[][] usLiuInVal = new float[imageHeight][imageWidth];
        float[][] vsLiuInVal = new float[imageHeight][imageWidth];
        float[][] usLiuOutVal = new float[imageHeight][imageWidth];
        float[][] vsLiuOutVal = new float[imageHeight][imageWidth];
        float[][] busVal = new float[imageHeight][imageWidth];
        float[][] bvsVal = new float[imageHeight][imageWidth];
        float[][] totalErrorVal = new float[imageHeight][imageWidth];
        final float lambda = 1000.0f;
        int iterationsLK = 1;        
        
        computeValidationUsAndVs(img1, img2, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, usLiuOutVal, vsLiuOutVal, 1, false);
        
        float totalErrorResultVal = computeBusBvsAndTotalError(lambda, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, busVal, bvsVal, totalErrorVal);
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                         blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                         windowSize, iterationsLK, imageHeight, imageWidth, iterations, 1000.0f);
        
        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);
        
        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);
        
        
        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, false);

        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
            
            public IDenseLiuShenListener setImages(IImage _img1, IImage _img2) {
                img1 = _img1;
                img2 = _img2;
                return this;
            }
            
            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer1 = validateImgBuffer("img2", img2, imgBuffer, pixelI, pixelJ, false);
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer2 = validateImgBuffer("img1", img1, imgBuffer, pixelI, pixelJ, false);                
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }
                        
                        float pixelValue1 = getNearestPixelWithWarp(img1, sourceImgI, sourceImgJ);
                        float pixelValue2 = getNearestPixelWithWarp(img2, sourceImgI, sourceImgJ);
                        assertEquals("Img1 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue1, pixelValues[(i+1) * 3 + (j+1)], 1e-3f);
                        assertEquals("Img2 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue2, pixelValues[(i+1) * 3 + (j+1) + 9], 1e-3f);
                    }
                }
                
            }

            @Override
            public void fixedImageDerivativesComputed(float IIx, float IIy, float II, float Ixt, float Iyt, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertEquals("IIx at I: " + sourceImgI + ", J: " + sourceImgJ, iixVal[sourceImgI][sourceImgJ], IIx  , 1e-5f);
                    assertEquals("IIy at I: " + sourceImgI + ", J: " + sourceImgJ, iiyVal[sourceImgI][sourceImgJ], IIy  , 1e-5f);
                    assertEquals("II  at I: " + sourceImgI + ", J: " + sourceImgJ, iiVal [sourceImgI][sourceImgJ], II   , 1e-5f);
                    assertEquals("Ixt at I: " + sourceImgI + ", J: " + sourceImgJ, ixtVal[sourceImgI][sourceImgJ], Ixt  , 1e-5f);
                    assertEquals("Iyt at I: " + sourceImgI + ", J: " + sourceImgJ, iytVal[sourceImgI][sourceImgJ], Iyt  , 1e-5f);
                    assertEquals("B00 at I: " + sourceImgI + ", J: " + sourceImgJ, b00Val[sourceImgI][sourceImgJ], Bs[0], 1e-5f);
                    assertEquals("B01 at I: " + sourceImgI + ", J: " + sourceImgJ, b01Val[sourceImgI][sourceImgJ], Bs[1], 1e-5f);
                    assertEquals("B11 at I: " + sourceImgI + ", J: " + sourceImgJ, b11Val[sourceImgI][sourceImgJ], Bs[2], 1e-5f);
                }
            }

            @Override
            public void vectorsRefined(int w, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[], 
                                       int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {

                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertTrue("Weight should be one at I: " + sourceImgI + ", J: " + sourceImgJ, w == 1);
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int idx = (i + 1) * 3 + (j + 1);
                            float uLiuInVal = getValueComplete(usLiuInVal, sourceImgI + i, sourceImgJ + j);
                            float vLiuInVal = getValueComplete(vsLiuInVal, sourceImgI + i, sourceImgJ + j);
                            assertEquals("usLiuIn at I: "    + sourceImgI+i + ", J: " + sourceImgJ+j, uLiuInVal, usLiuIn[idx], 1e-5f);
                            assertEquals("vsLiuIn at I: "    + sourceImgI+i + ", J: " + sourceImgJ+j, vLiuInVal, vsLiuIn[idx], 1e-5f);
                        }
                    }
                    assertEquals("bu at I: "         + sourceImgI + ", J: " + sourceImgJ, busVal       [sourceImgI][sourceImgJ], bu        , 1e-2f);
                    assertEquals("bv at I: "         + sourceImgI + ", J: " + sourceImgJ, bvsVal       [sourceImgI][sourceImgJ], bv        , 1e-2f);
                    assertEquals("usNew at I: "      + sourceImgI + ", J: " + sourceImgJ, usLiuOutVal  [sourceImgI][sourceImgJ], unew      , 1e-5f);
                    assertEquals("vsNew at I: "      + sourceImgI + ", J: " + sourceImgJ, vsLiuOutVal  [sourceImgI][sourceImgJ], vnew      , 1e-5f);
                    assertEquals("totalErrot at I: " + sourceImgI + ", J: " + sourceImgJ, totalErrorVal[sourceImgI][sourceImgJ], totalError, 1e-5f);
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = 0; i < usLiuOutVal.length; i++) {
                    for (int j = 0; j < usLiuOutVal[0].length; j++) {
                        int idx = i * usLiuOutVal[0].length + j;
                        assertEquals("us at I: " + i + ", J: " + j, usLiuOutVal[i][j], us[idx], 1e-5f);
                        assertEquals("vs at I: " + i + ", J: " + j, vsLiuOutVal[i][j], vs[idx], 1e-5f);
                    }
                }                
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                assertEquals("totalError does not match", totalErrorResultVal, totalErrorGlobal[0], 1e-5f);
            }
        }.setImages(imgLS1, imgLS2));
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 2);

    }

    @Test
    public void testImageToLocalImgBufferOffsetTestPass() {
        IImage img1 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_0.tif");
        IImage img2 = ImageTestHelper.getImage("testFiles" + File.separator + "rankine_vortex01_1.tif");
        
        //Setup the new size
        imageHeight = windowSize + blockSizeI-1 + 3;
        imageWidth  = windowSize + blockSizeJ-1 + 3;

        imageA = new float[imageHeight * imageWidth];
        imageB = new float[imageHeight * imageWidth];
        

        globalSizeI = imageHeight / blockSizeI;
        if (imageHeight % blockSizeI != 0) {
            globalSizeI++;
        }
        globalSizeI *= localSizeI;
        
        globalSizeJ = imageWidth / blockSizeJ;
        if (imageWidth % blockSizeJ != 0) {
            globalSizeJ++;
        }
        globalSizeJ *= localSizeJ;

        Matrix region1 = img1.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        Matrix region2 = img2.clipImageMatrix(0, 0, imageHeight, imageWidth, false, null);
        
        img1 = new ImageFloat(region1, imageHeight, imageWidth, "img1.png");
        img2 = new ImageFloat(region2, imageHeight, imageWidth, "img2.png");

        IFilter filter = new GaussianFilter2D(2.0f, 3);
        IImage imgLK1 = filter.applyFilter(img1, null);
        IImage imgLK2 = filter.applyFilter(img2, null);

        IFilter filter2 = new GaussianFilter2D(0.48f, 5);
        IImage imgLS1 = filter2.applyFilter(img1, null);
        IImage imgLS2 = filter2.applyFilter(img2, null);
        imgLS1 = imgLS1.normalize((ImageFloat)imgLS1);
        imgLS2 = imgLS2.normalize((ImageFloat)imgLS2);
        
        float us[] = new float[imageHeight * imageWidth];
        float vs[] = new float[imageHeight * imageWidth];
        float usNew[] = new float[imageHeight * imageWidth];
        float vsNew[] = new float[imageHeight * imageWidth];
        float totalError[] = new float[globalSizeJ * globalSizeI + 1];

        float[][] iixVal = new float[imageHeight][imageWidth];
        float[][] iiyVal = new float[imageHeight][imageWidth];
        float[][] iiVal = new float[imageHeight][imageWidth];
        float[][] ixtVal = new float[imageHeight][imageWidth];
        float[][] iytVal = new float[imageHeight][imageWidth];
        float[][] b00Val = new float[imageHeight][imageWidth];
        float[][] b01Val = new float[imageHeight][imageWidth];
        float[][] b11Val = new float[imageHeight][imageWidth];
        float[][] usLiuInVal = new float[imageHeight][imageWidth];
        float[][] vsLiuInVal = new float[imageHeight][imageWidth];
        float[][] usLiuOutVal = new float[imageHeight][imageWidth];
        float[][] vsLiuOutVal = new float[imageHeight][imageWidth];
        float[][] busVal = new float[imageHeight][imageWidth];
        float[][] bvsVal = new float[imageHeight][imageWidth];
        float[][] totalErrorVal = new float[imageHeight][imageWidth];
        final float lambda = 1000.0f;
        int iterationsLK = 1;
        
        computeValidationUsAndVs(img1, img2, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, usLiuOutVal, vsLiuOutVal, 1, true);
        
        float totalErrorResultVal = computeBusBvsAndTotalError(lambda, iixVal, iiyVal, iiVal, ixtVal, iytVal, b00Val, b01Val, b11Val, usLiuInVal, vsLiuInVal, busVal, bvsVal, totalErrorVal);
        
        DenseLiuShenGpuTestKernel kernel = new DenseLiuShenGpuTestKernel(localSizeI, localSizeJ, blockSizeI, blockSizeJ,  
                                                                         blockItemsPerWorkGroupI, blockItemsPerWorkGroupJ, 
                                                                         windowSize, iterationsLK, imageHeight, imageWidth, iterations, lambda);
        
        imageA = imgLK1.exportTo1DFloatArray(imageA);
        imageB = imgLK2.exportTo1DFloatArray(imageB);
        
        float[] imageLSA = imgLS1.exportTo1DFloatArray(null);
        float[] imageLSB = imgLS2.exportTo1DFloatArray(null);
        
        kernel.setKernelArgs(imageA, imageB, imageLSA, imageLSB, us, vs, usNew, vsNew, totalError, true);

        kernel.registerListener(new IDenseLiuShenListener() {            
            private IImage img1;
            private IImage img2;
            private float[][] validationBuffer1;
            private float[][] validationBuffer2;
    
            public IDenseLiuShenListener setImages(IImage _img1, IImage _img2) {
                img1 = _img1;
                img2 = _img2;
                return this;
            }
            
            @Override
            public void imageBClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer1 = validateImgBuffer("img2", img2, imgBuffer, pixelI, pixelJ, true);
            }
            
            @Override
            public void imageAClippedAndLoaded(float[] imgBuffer, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                int pixelI = gidI * localSizeI - 1;
                int pixelJ = gidJ * localSizeJ - 1;
                
                validationBuffer2 = validateImgBuffer("img1", img1, imgBuffer, pixelI, pixelJ, true);                
            }

            @Override
            public void pixelValuesLoaded(float[] pixelValues, int idI, int idJ, int gidI, int gidJ, int tidI,
                    int tidJ) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int sourceImgI = gidI * localSizeI + tidI + i;
                        int sourceImgJ = gidJ * localSizeJ + tidJ + j;
                        if (gidI * localSizeI + tidI >= img1.getHeight() || gidJ * localSizeJ + tidJ >= img1.getWidth()) {
                            continue;
                        }

                        float pixelValue1 = getNearestPixelWithWarp(img1, sourceImgI + 0.5f, sourceImgJ + 0.5f);
                        float pixelValue2 = getNearestPixelWithWarp(img2, sourceImgI + 0.5f, sourceImgJ + 0.5f);
                        assertEquals("Img1 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue1, pixelValues[(i+1) * 3 + (j+1)], 1e-3f);
                        assertEquals("Img2 pixel values do not match at I: " + sourceImgI + ", J: " + sourceImgJ, pixelValue2, pixelValues[(i+1) * 3 + (j+1) + 9], 1e-3f);
                    }
                }                
            }

            @Override
            public void fixedImageDerivativesComputed(float IIx, float IIy, float II, float Ixt, float Iyt, float[] Bs,
                    int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
                
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertEquals("IIx at I: " + sourceImgI + ", J: " + sourceImgJ, iixVal[sourceImgI][sourceImgJ], IIx  , 1e-5f);
                    assertEquals("IIy at I: " + sourceImgI + ", J: " + sourceImgJ, iiyVal[sourceImgI][sourceImgJ], IIy  , 1e-5f);
                    assertEquals("II  at I: " + sourceImgI + ", J: " + sourceImgJ, iiVal [sourceImgI][sourceImgJ], II   , 1e-5f);
                    assertEquals("Ixt at I: " + sourceImgI + ", J: " + sourceImgJ, ixtVal[sourceImgI][sourceImgJ], Ixt  , 1e-5f);
                    assertEquals("Iyt at I: " + sourceImgI + ", J: " + sourceImgJ, iytVal[sourceImgI][sourceImgJ], Iyt  , 1e-5f);
                    assertEquals("B00 at I: " + sourceImgI + ", J: " + sourceImgJ, b00Val[sourceImgI][sourceImgJ], Bs[0], 1e-5f);
                    assertEquals("B01 at I: " + sourceImgI + ", J: " + sourceImgJ, b01Val[sourceImgI][sourceImgJ], Bs[1], 1e-5f);
                    assertEquals("B11 at I: " + sourceImgI + ", J: " + sourceImgJ, b11Val[sourceImgI][sourceImgJ], Bs[2], 1e-5f);
                }
            }

            @Override
            public void vectorsRefined(int w, float bu, float bv, float unew, float vnew, float totalError,
                                       float usLiuIn[], float vsLiuIn[],
                                       int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                int sourceImgI = gidI * localSizeI + tidI;
                int sourceImgJ = gidJ * localSizeJ + tidJ;
               
                if (sourceImgI < img1.getHeight() && sourceImgJ < img1.getWidth()) {
                    assertTrue("Weight should be one at I: " + sourceImgI + ", J: " + sourceImgJ, w == 1);
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int idx = (i + 1) * 3 + (j + 1);
                            float uLiuInVal = getValueComplete(usLiuInVal, sourceImgI + i, sourceImgJ + j);
                            float vLiuInVal = getValueComplete(vsLiuInVal, sourceImgI + i, sourceImgJ + j);
                            assertEquals("usLiuIn at I: "    + sourceImgI+i + ", J: " + sourceImgJ+j, uLiuInVal, usLiuIn[idx], 1e-5f);
                            assertEquals("vsLiuIn at I: "    + sourceImgI+i + ", J: " + sourceImgJ+j, vLiuInVal, vsLiuIn[idx], 1e-5f);
                        }
                    }
                    assertEquals("bu at I: "         + sourceImgI + ", J: " + sourceImgJ, busVal       [sourceImgI][sourceImgJ], bu        , 1e-2f);
                    assertEquals("bv at I: "         + sourceImgI + ", J: " + sourceImgJ, bvsVal       [sourceImgI][sourceImgJ], bv        , 1e-2f);
                    assertEquals("usNew at I: "      + sourceImgI + ", J: " + sourceImgJ, usLiuOutVal  [sourceImgI][sourceImgJ], unew      , 1e-5f);
                    assertEquals("vsNew at I: "      + sourceImgI + ", J: " + sourceImgJ, vsLiuOutVal  [sourceImgI][sourceImgJ], vnew      , 1e-5f);
                    assertEquals("totalErrot at I: " + sourceImgI + ", J: " + sourceImgJ, totalErrorVal[sourceImgI][sourceImgJ], totalError, 1e-5f);
                } else {
                    assertTrue("Weight should be zero at I: " + sourceImgI + ", J: " + sourceImgJ, w == 0);
                }                
            }

            @Override
            public void vectorsCopied(float[] us, float[] vs, 
                                      int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                for (int i = 0; i < usLiuOutVal.length; i++) {
                    for (int j = 0; j < usLiuOutVal[0].length; j++) {
                        int idx = i * usLiuOutVal[0].length + j;
                        assertEquals("us at I: " + i + ", J: " + j, usLiuOutVal[i][j], us[idx], 1e-5f);
                        assertEquals("vs at I: " + i + ", J: " + j, vsLiuOutVal[i][j], vs[idx], 1e-5f);
                    }
                }
                
            }

            @Override
            public void totalErrorComputed(float[] totalErrorGlobal, 
                                           int idI, int idJ, int gidI, int gidJ, int tidI, int tidJ) {
                assertEquals("totalError does not match", totalErrorResultVal, totalErrorGlobal[0], 1e-5f);
            }
        }.setImages(imgLS1, imgLS2));
        
        kernel.execute(Range.create2D(globalSizeJ, globalSizeI, localSizeJ, localSizeI), 2);
    }

    public float getNearestPixel(IImage img, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= img.getHeight()) {
            i = img.getHeight() - 1;
        }
        
        if (j < 0) {
            j = 0;
        }
        if (j >= img.getWidth()) {
            j = img.getWidth() - 1;
        }
        
        return img.readPixel(i, j);
    }
    
    public float getNearestPixelWithWarp(IImage img, float locI, float locJ) {
        int i = (int) locI;
        int j = (int) locJ;
        
        float deltaI = locI - i;
        float deltaJ = locJ - j;
        
        if (deltaI < 0) {
            i--;
            deltaI += 1.0;
        }
        
        if (deltaJ < 0) {
            j--;
            deltaJ += 1.0;
        }
        
        float value = (1.0f - deltaI) * ((1.0f - deltaJ) * getNearestPixel(img, i  ,j) + deltaJ * getNearestPixel(img, i  ,j+1)) + 
                              deltaI  * ((1.0f - deltaJ) * getNearestPixel(img, i+1,j) + deltaJ * getNearestPixel(img, i+1,j+1));
        
        return value;
    }
    
    protected float[][] validateImgBuffer(String title, IImage img, float[] imgBuffer, int pixelI, int pixelJ, boolean offsetPixel) {
        float[][] validationBuffer = new float[(localSizeI + 2)][(localSizeJ + 2)];
        
        float offset = 0.0f;
        if (offsetPixel) {
            offset = 0.5f;
        }
        
        for (int i = 0; i < localSizeI + 2; i++) {
            for (int j = 0; j < localSizeJ + 2; j++) {
                validationBuffer[i][j] = getNearestPixelWithWarp(img, pixelI + i + offset, pixelJ + j + offset);
                assertEquals("Pixel value does not match for " + title + " at I: " + i + ", J:" + j, validationBuffer[i][j], imgBuffer[i * (localSizeJ + 2) + j], 1e-3f);
            }
        }
        
        return validationBuffer;
    }

    void computeValidationUsAndVs(IImage img1, IImage img2, float[][] IIx, float[][] IIy, float[][] II, float[][] Ixt, float[][] Iyt, float[][] B00, float[][] B01, float[][] B11, 
                                                            float[][] usVal, float[][] vsVal, float[][] usInOut, float[][] vsInOut, int iterations, boolean offset) {
        IImage imgLK1;
        IImage imgLK2;

        imgLK1 = ImageFloat.convertFrom(img1);
        imgLK2 = ImageFloat.convertFrom(img2);
        IFilter filter = new GaussianFilter2D(2.0f, 3);
        imgLK1 = filter.applyFilter(imgLK1, imgLK1);
        imgLK2 = filter.applyFilter(imgLK2, imgLK2);

        float us[] = new float[usInOut.length * usInOut[0].length];
        float vs[] = new float[usInOut.length * usInOut[0].length];
        
        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                us[idx] = usInOut[i][j];
                vs[idx] = usInOut[i][j];
            }
        }
        
        KernelManager.setKernelManager(new GPUKernelManager());
        
        DenseLucasKanadeAparapiJob job = new DenseLucasKanadeAparapiJob(gpuDevice);
        LucasKanadeOptions options = new LucasKanadeOptions();
        options.iterations = 1;
        options.windowSize = 27;
        OpticalFlowInterpolatorInput jobInput = new OpticalFlowInterpolatorInput();
        jobInput.imageA = imgLK1;
        jobInput.imageB = imgLK2;
        jobInput.halfPixelOffset = offset;
        jobInput.us = us;
        jobInput.vs = vs;
        jobInput.options = options;
        
        job.setInputParameters(JobResultEnum.JOB_RESULT_OPTICAL_FLOW, jobInput);
        job.analyze();
        job.compute();
        //OpticalFlowInterpolatorInput jobResult = job.getJobResult(JobResultEnum.JOB_RESULT_OPTICAL_FLOW);

        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                usVal[i][j] = us[idx];
                vsVal[i][j] = vs[idx];
            }
        }
        
        PIVContextTestsSingleton.setSingleton();
        LiuShenInterpolatorConfiguration lsConfig = new LiuShenInterpolatorConfiguration();
        lsConfig.setFilterSigmaLK(2.0f);
        lsConfig.setFilterWidthPxLK(3);
        lsConfig.setNumberOfIterationsLK(1);
        lsConfig.setWindowSizeLK(27);
        lsConfig.setFilterSigmaLS(0.48f);
        lsConfig.setFilterWidthPxLS(5);
        lsConfig.setMultiplierLagrangeLS(1000.0f);
        lsConfig.setNumberOfIterationsLS(iterations);
        lsConfig.setVectorsWindowSizeLS(3);
        lsConfig.setDenseVectors(offset ? false : true);
        PIVInputParameters params = PIVContextSingleton.getSingleton().getPIVParameters();
        params.setSpecificConfiguration(LiuShenInterpolatorConfiguration.IDENTIFIER, lsConfig);
        
        LiuShenFloat liuShenValidation = new LiuShenFloat();
        liuShenValidation.updateImageA(img1);
        liuShenValidation.updateImageB(img2);
        liuShenValidation.computeFromVelocities(us, vs);

        float IIxL []   = liuShenValidation.getIIx();
        float IIyL []   = liuShenValidation.getIIy();
        float IIL  []   = liuShenValidation.getII();
        float IxtL []   = liuShenValidation.getIxt();
        float IytL []   = liuShenValidation.getIyt();
        float BsL  [][] = liuShenValidation.getBs();
        for (int i = 0; i < usInOut.length; i++) {
            for (int j = 0; j < usInOut[0].length; j++) {
                int idx = i * usInOut[0].length + j;
                usInOut[i][j] = us[idx];
                vsInOut[i][j] = vs[idx];
                IIx[i][j] = IIxL[idx];
                IIy[i][j] = IIyL[idx];
                II[i][j] = IIL[idx];
                Ixt[i][j] = IxtL[idx];
                Iyt[i][j] = IytL[idx];
                B00[i][j] = BsL[idx][0];
                B01[i][j] = BsL[idx][1];
                B11[i][j] = BsL[idx][2];
            }
        }
        
        KernelManager.setKernelManager(new JTPKernelManager());
    }

    @FunctionalInterface
    private interface GVec {
        float get(int i, int j);
    }
    
    private final float getValueComplete(float[][] arr, int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (i >= arr.length) {
            i = arr.length - 1;
        }
        if (j < 0) {
            j = 0;
        }
        if (j >= arr[0].length) {
            j = arr[0].length - 1;
        }
        
        return arr[i][j];
    }

    
    public float computeBusBvsAndTotalError(float lambda, float IIx[][], float[][] IIy, float[][] II, float[][] Ixt, float[][] Iyt, 
                                           float B00[][], float B01[][], float B11[][],
                                           float us[][], float vs[][],
                                           float bus[][], float bvs[][], float totalError[][]) {
        final GVec gus = (i, j) -> getValueComplete(us, i, j);
        final GVec gvs = (i, j) -> getValueComplete(vs, i, j);

        float totalErrorResult = 0.0f;
        
        for (int i = 0; i < B00.length; i++) {
            for (int j = 0; j < B00[0].length; j++) {                
                float bu = 2.0f * IIx[i][j] * (gus.get(i-1, j  ) - gus.get(i+1, j  )) / 2.0f + 
                                  IIx[i][j] * (gvs.get(i  , j-1) - gvs.get(i  , j+1)) / 2.0f +
                                  IIy[i][j] * (gvs.get(i-1, j  ) - gvs.get(i+1, j  )) / 2.0f + 
                                   II[i][j] * (gus.get(i-1, j  ) + gus.get(i+1, j  )) +
                                   II[i][j] * (gvs.get(i-1, j-1) - gvs.get(i-1, j+1) - gvs.get(i+1, j-1) + gvs.get(i+1, j+1)) / 4.0f +
                                    lambda * (gus.get(i-1, j-1) + gus.get(i-1, j  ) + gus.get(i-1, j+1) +
                                              gus.get(i  , j-1) + gus.get(i  , j+1) +
                                              gus.get(i+1, j-1) + gus.get(i+1, j  ) + gus.get(i+1, j+1)) + Ixt[i][j];
                
                float bv = 2.0f * IIy[i][j] * (gvs.get(i  , j-1) - gvs.get(i  , j+1)) / 2.0f +
                                  IIy[i][j] * (gus.get(i-1, j  ) - gus.get(i+1, j  )) / 2.0f +
                                  IIx[i][j] * (gus.get(i  , j-1) - gus.get(i  , j+1)) / 2.0f +                                       
                                   II[i][j] * (gvs.get(i  , j-1) + gvs.get(i  , j+1)) +
                                   II[i][j] * (gus.get(i-1, j-1) - gus.get(i-1, j+1) - gus.get(i+1, j-1) + gus.get(i+1, j+1)) / 4.0f +
                                    lambda * (gvs.get(i-1, j-1) + gvs.get(i-1, j  ) + gvs.get(i-1, j+1) +
                                              gvs.get(i  , j-1) + gvs.get(i  , j+1) +
                                              gvs.get(i+1, j-1) + gvs.get(i+1, j  ) + gvs.get(i+1, j+1)) + Iyt[i][j];
                
                bus[i][j] = bu;
                bvs[i][j] = bv;
                
                float unew = (float)-(B00[i][j]*bu + B01[i][j]*bv);
                float vnew = (float)-(B01[i][j]*bu + B11[i][j]*bv);
                
                totalError[i][j] = (float)FastMath.sqrt((unew - gus.get(i, j))*(unew - gus.get(i, j)) + (vnew - gvs.get(i, j))*(vnew - gvs.get(i, j)));
                totalErrorResult += totalError[i][j];
                
                //usNew[i][j] = unew;
                //vsNew[i][j] = vnew;
            }                    
        }
        totalErrorResult /= (float)(B00.length * B00[0].length);
        
        return totalErrorResult;
    }
    
    public static void dump1DArray(String name, int[] xr) {
        System.out.println(name);
        printArray(xr);
    }
    
    public static void dump1DArray(String name, float[] xr) {
        System.out.println(name);
        printArray(xr);
    }
    
    public static void dump2DArray(String name, int[][] xr) {
        System.out.println(name);
        for (int i = 0; i < xr.length; i++) {
            printArray(xr[i]);
        }
     }

    public static void dump2DArray(String name, float[][] xr) {
         System.out.println(name);
         for (int i = 0; i < xr.length; i++) {
             printArray(xr[i]);
         }
    }

    public static void dump2DArray(String name, double[][] xr) {
        System.out.println(name);
        for (int i = 0; i < xr.length; i++) {
            printArray(xr[i]);
        }
    }

    public static void printArray(int[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }
     
    public static void printArray(float[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }

    public static void printArray(double[] inputArray) {
        System.out.print("[ ");
        for (int i = 0; i < inputArray.length; i++) {
            System.out.print(inputArray[i] + ", ");
        }
        System.out.println(" ]");
    }

}
