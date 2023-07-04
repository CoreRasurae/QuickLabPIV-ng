package pt.quickLabPIV.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageFactoryEnum;
import pt.quickLabPIV.images.ImageTestHelper;
import pt.quickLabPIV.jobs.ImageReaderJob;
import pt.quickLabPIV.jobs.JobResultEnum;

public class ImageReaderJobTests {

	public ImageReaderJobTests() {
	}
	
	@Before
	public void setup() {
        PIVContextTestsSingleton.setSingleton();
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        singleton.getPIVParameters().setPixelDepth(ImageFactoryEnum.Image8Bit);
	}
	
	public BufferedImage convertToGray1(BufferedImage bi) {
		BufferedImage image = new BufferedImage(bi.getWidth(), bi.getHeight(),  
			    BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = image.getGraphics();  
		g.drawImage(bi, 0, 0, null);  
		g.dispose(); 
		
		return image;
	}
	
	@Test
	public void simpleImageFileTestPass() {	    
		ImageReaderJob job = new ImageReaderJob("testFiles" + File.separator + "test1.tif");
		job.analyze();
		job.compute();
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		int height = bi.getHeight();
		int width = bi.getWidth();
		int type = bi.getType();
		
		assertEquals("Image width doesn't match", 2320, width);
		assertEquals("Image height doesn't match", 450, height);
		assertEquals("Image type doesn't match", BufferedImage.TYPE_BYTE_INDEXED, type);
		SampleModel sm = bi.getSampleModel();
		assertEquals("Image number of bands doesn't match", 1, sm.getNumBands());
		
		Raster r = bi.getData();
		assertEquals("Image number of data elements doesn't match", 1, r.getNumDataElements());
	}
	
	@Test
	public void simpleImageJPGTestPass() {
		ImageReaderJob job = new ImageReaderJob("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		
		BufferedImage bi2 = convertToGray1(bi);
		
		int height = bi.getHeight();
		int width = bi.getWidth();
		int type = bi.getType();
		
		assertEquals("Image width doesn't match", 1600, width);
		assertEquals("Image height doesn't match", 1200, height);
		assertEquals("Image type doesn't match", BufferedImage.TYPE_3BYTE_BGR, type);
		SampleModel sm = bi.getSampleModel();
		assertEquals("Image number of bands doesn't match", 3, sm.getNumBands());
		
		Raster r = bi.getData();
		Raster r2 = bi2.getData();
		assertEquals("Image number of data elements doesn't match", 3, r.getNumDataElements());
		
		byte[] outData = new byte[3];
		byte[] outData2 = new byte[1];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				r.getDataElements(x, y, outData);
				r2.getDataElements(x, y, outData2);
				
				assertEquals("Channel R doesn't match Channel G", outData[0], outData[1]);
				assertEquals("Channel G doesn't match Cahnnel B", outData[1], outData[2]);
				assertEquals("Gray conversion doesn't match", outData2[0], outData[0]);
			}
		}
	}
	
	@Test
	public void simpleImagePNGTestFail() {
		String filename = "testFiles" + File.separator + "img1.png";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "img1.png");
		
		int height = bi.getHeight();
		int width = bi.getWidth();
		int type = bi.getType();
		
		assertEquals("Image width doesn't match", 640, width);
		assertEquals("Image height doesn't match", 640, height);
		assertEquals("Image type doesn't match", BufferedImage.TYPE_USHORT_GRAY, type);
		SampleModel sm = bi.getSampleModel();
		assertEquals("Image number of bands doesn't match", 1, sm.getNumBands());
		
		Raster r = bi.getData();
		assertEquals("Image number of data elements doesn't match", 1, r.getNumDataElements());
		
		short[] outData = new short[1];
		int max = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				r.getDataElements(x, y, outData);
				
				int value = outData[0];
				if (value < 0) {
					value += 65536;
				}
				if (outData[0] > max) {
					max = value;
				}
			}
		}
		
		assertFalse("Max. Gray scale is too small: " + max, max < 256);
	}
	
	@Test
	public void checkImageOrderingTestPass() {
		ImageReaderJob job = new ImageReaderJob("testFiles" + File.separator + "test1.tif");
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		
		Raster r = bi.getData();
		byte[] imageArray=(byte[])r.getDataElements(r.getMinX()+100,r.getMinY()+50, 100, 50, null);
		
		//Test first line matches at correct position...
		for (int x = 0; x < 100; x++) {
			int value = r.getSample(r.getMinX() + 100 + x, r.getMinY() + 50, 0);
			int testValue = imageArray[x];
			if (testValue < 0) {
				testValue = testValue + 256;
			}
			
			assertEquals("Pixel doesn't match", value, testValue);
		}
		
		//Test middle region match..
		for (int y = 10; y < 40; y++) {
			for (int x = 25; x < 75; x++) {
				int value = r.getSample(r.getMinX() + 100 + x, r.getMinY() + 50 + y, 0);
				int testValue = imageArray[y * 100 + x];
				if (testValue < 0) {
					testValue = testValue + 256;
				}
				
				assertEquals("Pixel doesn't match", value, testValue);				
			}
		}
		
		/*
		 * Region obtained with getDataElements(...) should match the following structure:
		 * 
		 * clippedRegionArray=[ <line 1> <line 2> <line 3> ... ]
		 */
	}
	
	@Test
	public void checkImageAndMatrixByteFloatTestPass() {
		String filename = "testFiles" + File.separator + "test1.tif";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		Matrix mb = img.clipImageMatrix(60, 125, 30, 50, false, null);
		int length = img.getWidth() * img.getHeight();
		
		float[] buf = new float[length+100];
		mb.copyMatrixToArray(buf, 100);
		int i = 0;
		for (int y = 10; y < 40; y++) {
			for (int x = 25; x < 75; x++) {
				int value = r.getSample(r.getMinX() + 100 + x, r.getMinY() + 50 + y, 0);
				float testValue = buf[100+i++];
				
				assertEquals("Pixel doesn't match", value, testValue, 1e-10);				
			}
		}
	}

	@Test
	public void checkImageAndMatrixByteByteTestPass() {
		String filename = "testFiles" + File.separator + "test1.tif";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		Matrix mb = img.clipImageMatrix(60, 125, 30, 50, false, null);
		int length = img.getWidth() * img.getHeight();
		
		byte[] buf = new byte[length+100];
		mb.copyMatrixToArray(buf, 100);
		int i = 0;
		for (int y = 10; y < 40; y++) {
			for (int x = 25; x < 75; x++) {
				int value = r.getSample(r.getMinX() + 100 + x, r.getMinY() + 50 + y, 0);
				int testValue = buf[100+i++];
				if (testValue < 0) {
					testValue = testValue + 256;
				}
				
				assertEquals("Pixel doesn't match", value, testValue, 1e-10);				
			}
		}
	}
	
	@Test 
	public void checkImageAndMatrixByteFloat2DTestPass() {
		String filename = "testFiles" + File.separator + "test1.tif";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		Matrix mb = img.clipImageMatrix(60, 125, 30, 50, false, null);
		
		float[][] buf = new float[img.getHeight()+50][img.getWidth()+100];
		mb.copyMatrixTo2DArray(buf, 50, 100);
		int i = 0;
		for (int y = 10; y < 40; y++) {
			int j = 0;
			for (int x = 25; x < 75; x++) {
				int value = r.getSample(r.getMinX() + 100 + x, r.getMinY() + 50 + y, 0);
				float testValue = buf[50+i][100+j];
				
				assertEquals("Pixel doesn't match", value, testValue, 1e-10);
				
				j++;
			}
			
			i++;
		}		
	}
	
	@Test
	public void checkPNGClippingPass() {
		String filename = "testFiles" + File.separator + "img1.png";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "img1.png");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		byte[] imgArray = (byte[])r.getDataElements(320, 320, 128, 128, null);
		
		Matrix mb = img.clipImageMatrix(320, 320, 128, 128, false, null);
		
		for (short i = 0; i < 128; i++) {
			for (short j = 0; j < 128; j++) {
				float value = mb.getElement(i, j);
				byte byteValue = (byte)value;
				
				assertEquals("Pixel doesn't match", byteValue, imgArray[i * 128 + j]);
				
				j++;
			}
			
			i++;
		}
	}

	@Test
	public void checkJPGClippingPass() {
		String filename = "testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		byte[] imgArray = (byte[])r.getDataElements(320, 320, 128, 128, null);
		
		Matrix mb = img.clipImageMatrix(320, 320, 128, 128, false, null);
		
		for (short i = 0; i < 128; i++) {
			for (short j = 0; j < 128; j++) {
				float value = mb.getElement(i, j);
				byte byteValue = (byte)value;
				
				assertEquals("Pixel doesn't match", byteValue, imgArray[i * 128 + j]);
				
				j++;
			}
			
			i++;
		}
	}
	
	@Test
	public void checkTIFClippingPass() {
		String filename = "testFiles" + File.separator + "test1.tif";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		BufferedImage bi = ImageTestHelper.getImageBI("testFiles" + File.separator + "test1.tif");
		
		bi = convertToGray1(bi);
		
		Raster r = bi.getData();
		byte[] imgArray = (byte[])r.getDataElements(320, 320, 128, 128, null);
		
		Matrix mb = img.clipImageMatrix(320, 320, 128, 128, false, null);
		
		for (short i = 0; i < 128; i++) {
			for (short j = 0; j < 128; j++) {
				float value = mb.getElement(i, j);
				byte byteValue = (byte)value;
				
				assertEquals("Pixel doesn't match", byteValue, imgArray[i * 128 + j]);
				
				j++;
			}
			
			i++;
		}
	}
	
	@Test
	public void checkImageAndMatrixByteTestPass() {
		String filename = "testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg";
		ImageReaderJob job = new ImageReaderJob(filename);
		job.analyze();
		job.compute();
		
		IImage img = job.getJobResult(JobResultEnum.JOB_RESULT_IMAGES).get(0);
		//BufferedImage bi = img.bi;
		
		Matrix mb = img.clipImageMatrix(0, 0, img.getHeight(), img.getWidth(), false, null);
		float[][] buf = new float[img.getHeight()][img.getWidth()];
		mb.copyMatrixTo2DArray(buf, 0, 0);
		
		assertEquals("Matrix Height doesn't match image geometry", img.getHeight(), buf.length);
		assertEquals("Matrix Width doesn't match image geometry", img.getWidth(), buf[0].length);
	}
}
