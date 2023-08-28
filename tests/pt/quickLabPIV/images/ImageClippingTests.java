// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixByte;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVContextTestsSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.ImageClippingException;
import pt.quickLabPIV.images.ImageFactoryEnum;

public class ImageClippingTests {
    private IImage img;
    
    @Before
    public void setup() {
        PIVContextSingleton singleton = PIVContextTestsSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        parameters.setPixelDepth(ImageFactoryEnum.Image8Bit);
        img = ImageTestHelper.getImage("testFiles" + File.separator + "image_1.3or93zbi.000000a.jpg");
    }
    
	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail1() {
		img.clipImageMatrix(-2, 0, 64, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail2() {
		img.clipImageMatrix(0, -2, 64, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail3() {
		img.clipImageMatrix(0, 1537, 64, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail4() {
		img.clipImageMatrix(1137, 0, 64, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail5() {
		img.clipImageMatrix(1137, 1537, 64, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail6() {
		img.clipImageMatrix(0, 0, 1201, 64, false, null);
	}

	@Test(expected=ImageClippingException.class)
	public void testPartialClippingFail7() {
		img.clipImageMatrix(0, 0, 64, 1601, false, null);
	}
	
	@Test
	public void testPartialClippingPass1() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(0, 0, 64, 64, false, null);
		
		checkClippedRegion(reference, clipped, 0, 0, 0, 0, 0, 0, 64, 64);
	}

	@Test
	public void testPartialClippingPass2() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(1137, 1537, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 1137, 1537, 0, 0, 1, 1, 64, 64);
	}

	@Test
	public void testPartialClippingPass3() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(-1, -1, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 0, 0, 1, 1, 0, 0, 64, 64);
	}

	@Test
	public void testPartialClippingPass4() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(-1, 1537, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 0, 1537, 1, 0, 0, 1, 64, 64);
	}

	@Test
	public void testPartialClippingPass5() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(1137, -1, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 1137, 0, 0, 1, 1, 0, 64, 64);
	}

	@Test
	public void testPartialClippingCompleteOutsidePass1() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(-64, -64, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 0, 0, 64, 64, 0, 0, 64, 64);
	}

	@Test
	public void testPartialClippingCompleteOutsidePass2() {
		Matrix reference = createMatrixFromImage(img);
		
		Matrix clipped = img.clipImageMatrix(-64, 1600, 64, 64, true, null);
		
		checkClippedRegion(reference, clipped, 0, 1600, 64, 0, 0, 64, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingPass1() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(0, 0, 64, 64, false, input);
		
		checkClippedRegion(reference, clipped, 0, 0, 0, 0, 0, 0, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingPass2() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(1137, 1537, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 1137, 1537, 0, 0, 1, 1, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingPass3() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(-1, -1, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 0, 0, 1, 1, 0, 0, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingPass4() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(-1, 1537, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 0, 1537, 1, 0, 0, 1, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingPass5() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(1137, -1, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 1137, 0, 0, 1, 1, 0, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingCompleteOutsidePass1() {
		Matrix reference = createMatrixFromImage(img);

		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(-64, -64, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 0, 0, 64, 64, 0, 0, 64, 64);
	}

	@Test
	public void testPartialNonNullClippingCompleteOutsidePass2() {
		Matrix reference = createMatrixFromImage(img);
		
		byte[] buffer = new byte[64*64];
		Matrix input = new MatrixByte(buffer, (short)64, (short)64);
		
		Matrix clipped = img.clipImageMatrix(-64, 1600, 64, 64, true, input);
		
		checkClippedRegion(reference, clipped, 0, 1600, 64, 0, 0, 64, 64, 64);
	}

	private Matrix createMatrixFromImage(IImage img) {
		Matrix m = img.clipImageMatrix(0, 0, img.getHeight(), img.getWidth(), false, null);
		return m;
	}
	
	/**
	 * Checks that the clipped region as correct data by comparing it to the corresponding region in
	 * the original unclipped image, or zero value for clipped sub-regions that fall outside the 
	 * image extents.
	 * 
	 * @param reference the original image matrix
	 * @param test the clipped region matrix to test
	 * @param top the top position in the original image (cannot be less than 0)
	 * @param left the left position in the original image (cannot be less than 0)
	 * @param marginTop the margin at the top in pixels (clipped region starting in -1, the margin is 1)
	 * @param marginLeft the margin at the left in pixels (clipped region starting in -1, the margin is 1)
	 * @param marginBottom the margin at the bottom in pixels (clipped region ending outside by 1 pixel, the margin is 1)
	 * @param marginRight the margin at the right in pixels (clipped region ending outsude by 1 pixel, the margin is 1)
	 * @param width the width of the clipped tile
	 * @param height the height of the clipped tile
	 */
	private void checkClippedRegion(Matrix reference, Matrix test, int top, int left, 
			int marginTop, int marginLeft, int marginBottom, int marginRight, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (top - marginTop + i >= 0 && top + i - marginTop <= reference.getHeight() - marginBottom &&
						left - marginLeft + j >= 0 && left + j - marginLeft <= reference.getWidth() - marginRight) {
					assertEquals("Pixel at image [I: "  + (top + i) + ", J: " + (left + j) + "], doesn't match clipped region at: [I: " + i + ", J: " + j + "]",
							reference.getElement(top + i - marginTop, left + j - marginLeft), test.getElement(i, j), 0.0001f);
				} else {
					assertEquals("Clipped portion should be zero at: [I: " + i + ", J: " + j + "]", 0.0f, test.getElement(i, j), 0.0001f);
				}
			}
		}
	}
}
