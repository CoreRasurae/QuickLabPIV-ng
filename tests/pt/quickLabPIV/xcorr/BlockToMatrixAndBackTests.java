// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.xcorr;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.MatrixFloat;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationFFTParallelBlockKernel;

/**
 * Tests functions to read and write from/to a matrix of blocks, where the number of Threads in I direction matches the block height.
 * This matches the GPU implementation requirements.
 * @author lpnm
 *
 */
public class BlockToMatrixAndBackTests {

	//Creates a matrix of 4x4 blocks, each with 2x2 block elements.
	//Each block is ordered block row, by block row.
	public float[] create2x2Block4x4Matrix() {
		float[] blocks2x2Matrix = {  1,  2,  9, 10, // block 1 (row 1 and 2, columns 1 and 2)
				                     3,  4, 11, 12, // block 2 (row 1 and 2, columns 3 and 4)
				                     5,  6, 13, 14, // block 3 (row 1 and 2, columns 5 and 6)
				                     7,  8, 15, 16, // block 4 (row 1 and 2, columns 7 and 8) ... matrix rows 1 and 2
				                    17, 18, 25, 26, // block 5 (row 3 and 4, columns 1 and 2)
				                    19, 20, 27, 28, // block 6 (row 3 and 4, columns 3 and 4)
				                    21, 22, 29, 30, // block 7 (row 3 and 4, columns 5 and 6)
				                    23, 24, 31, 32, // block 8 (row 3 and 4, columns 7 and 8) ... matrix rows 3 and 4
				                    33, 34, 41, 42, // block 9 (row 5 and 6, columns 1 and 2)
				                    35, 36, 43, 44, // block 10 (row 5 and 6, columns 3 and 4)
				                    37, 38, 45, 46, // block 11 (row 5 and 6, columns 5 and 6)
				                    39, 40, 47, 48, // block 12 (row 5 and 6, columns 7 and 8) ... matrix rows 5 and 6
				                    49, 50, 57, 58, // block 13 (row 7 and 8, columns 1 and 2)
				                    51, 52, 59, 60, // block 14 (row 7 and 8, columns 3 and 4)
				                    53, 54, 61, 62, // block 15 (row 7 and 8, columns 5 and 6)
				                    55, 56, 63, 64  // block 16 (row 7 and 8, columns 7 and 8) ... matrix rows 7 and 8
		};
		
		return blocks2x2Matrix;
	}

	public float[] create4x4Block2x2Matrix() {
		float[] blocks4x4Matrix = {  1,  2,  3,  4,  9, 10, 11, 12, 17, 18, 19, 20, 25, 26, 27, 28,// block 1 (row 1, 2, 3, 4, columns 1, 2, 3 and 4)
				                     5,  6,  7,  8, 13, 14, 15, 16, 21, 22, 23, 24, 29, 30, 31, 32,// block 2 (row 1, 3, 4, 5, columns 5, 6, 7 and 8) ... matrix rows 1,2,3,4                     
				                    33, 34, 35, 36, 41, 42, 43, 44, 49, 50, 51, 52, 57, 58, 59, 60,// block 3 (row 5, 6, 7, 8, columns 1, 2, 3 and 4)
				                    37, 38, 39, 40, 45, 46, 47, 48, 53, 54, 55, 56, 61, 62, 63, 64 // block 4 (row 5, 6, 7, 8, columns 5, 6, 7 and 8) ... matrix rows 5,6,7,8 
		};
		
		return blocks4x4Matrix;
	}

	
	public float[] createMatrix() {
		float[] matrix = new float[64];
		for (int i = 0; i < 64; i++) {
			matrix[i] = i+1;
		}
		return matrix;
	}

	//Blocks internally transposed but otherwise in standard block form
	public float[] create2x2Block4x4TransposedBlockMatrix() {
		float[] blocks2x2Matrix = {  1,  9,  2, 10, // block 1 (row 1 and 2, columns 1 and 2)
				                     3, 11,  4, 12, // block 2 (row 1 and 2, columns 3 and 4)
				                     5, 13,  6, 14, // block 3 (row 1 and 2, columns 5 and 6)
				                     7, 15,  8, 16, // block 4 (row 1 and 2, columns 7 and 8) ... matrix rows 1 and 2
				                    17, 25, 18, 26, // block 5 (row 3 and 4, columns 1 and 2)
				                    19, 27, 20, 28, // block 6 (row 3 and 4, columns 3 and 4)
				                    21, 29, 22, 30, // block 7 (row 3 and 4, columns 5 and 6)
				                    23, 31, 24, 32, // block 8 (row 3 and 4, columns 7 and 8) ... matrix rows 3 and 4
				                    33, 41, 34, 42, // block 9 (row 5 and 6, columns 1 and 2)
				                    35, 43, 36, 44, // block 10 (row 5 and 6, columns 3 and 4)
				                    37, 45, 38, 46, // block 11 (row 5 and 6, columns 5 and 6)
				                    39, 47, 40, 48, // block 12 (row 5 and 6, columns 7 and 8) ... matrix rows 5 and 6
				                    49, 57, 50, 58, // block 13 (row 7 and 8, columns 1 and 2)
				                    51, 59, 52, 60, // block 14 (row 7 and 8, columns 3 and 4)
				                    53, 61, 54, 62, // block 15 (row 7 and 8, columns 5 and 6)
				                    55, 63, 56, 64  // block 16 (row 7 and 8, columns 7 and 8) ... matrix rows 7 and 8
		};
		
		return blocks2x2Matrix;
	}

	//Blocks internally transposed but otherwise in standard block form
	public float[] create4x4Block2x2TransposedBlockMatrix() {
		float[] blocks4x4Matrix = create4x4Block2x2Matrix();
		
		int blockHeight = 4;
		int blockWidth = 4;
		
		for (int blockIndexI = 0; blockIndexI < 2; blockIndexI++) {
			for (int blockIndexJ = 0; blockIndexJ < 2; blockIndexJ++) {
				int blockOffset = blockIndexI*2*blockHeight*blockWidth + blockIndexJ*blockHeight*blockWidth;
				for (int i = 0; i < blockHeight; i++) {
					for (int j = i; j < blockWidth; j++) {
						float temp = blocks4x4Matrix[blockOffset + i * blockWidth + j];
						blocks4x4Matrix[blockOffset + i * blockWidth + j] = blocks4x4Matrix[blockOffset + j * blockHeight + i];
						blocks4x4Matrix[blockOffset + j * blockHeight + i] = temp;
					}
				}
			}
		}
		
		return blocks4x4Matrix;
	}

	
	//Creates a transposed block matrix 4x4 of transposed blocks 2x2 - currently not in use
	/*public float[] create2x2Block4x4TransposedMatrix() {
		float[] blocks2x2Matrix = {  1,  9,  2, 10, // block 1  B(0,0) (row 1 and 2, columns 1 and 2)
									17, 25, 18, 26, // block 5  B(1,0) (row 3 and 4, columns 1 and 2) 
									33, 41, 34, 42, // block 9  B(2,0) (row 5 and 6, columns 1 and 2) 
									49, 57, 50, 58, // block 13 B(3,0) (row 7 and 8, columns 1 and 2)
				                     
				                     3, 11,  4, 12, // block 2  B(0,1) (row 1 and 2, columns 3 and 4)				                 
				                    19, 27, 20, 28, // block 6  B(1,1) (row 3 and 4, columns 3 and 4)
				                    35, 43, 36, 44, // block 10 B(2,1) (row 5 and 6, columns 3 and 4)
				                    51, 59, 52, 60, // block 14 B(3,1) (row 7 and 8, columns 3 and 4)
				                    
				                    5, 13,  6, 14, // block 3  B(0,2) (row 1 and 2, columns 5 and 6)
				                    21, 29, 22, 30, // block 7  B(1,2) (row 3 and 4, columns 5 and 6)				         
				                    37, 45, 38, 46, // block 11 B(2,2) (row 5 and 6, columns 5 and 6)
				                    53, 61, 54, 62, // block 15 B(3,2) (row 7 and 8, columns 5 and 6)
				                    
				                    7, 15,  8, 16, // block 4  B(0,3) (row 1 and 2, columns 7 and 8) ... matrix rows 1 and 2
				                    23, 31, 24, 32, // block 8  B(1,3) (row 3 and 4, columns 7 and 8) ... matrix rows 3 and 4
				                    39, 47, 40, 48, // block 12 B(2,3) (row 5 and 6, columns 7 and 8) ... matrix rows 5 and 6				                
				                    55, 63, 56, 64  // block 16 B(3,3) (row 7 and 8, columns 7 and 8) ... matrix rows 7 and 8
		};
		
		return blocks2x2Matrix;
	}*/

	/**
	 * Tests copying from global memory organized in block form, along the row,  into local memory in linearized format.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x4ThreadsCopyFromGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create4x4Block2x2Matrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float expected = 1.0f;
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false);
				}
			}
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected, lineArray[i * 8 + j], 0.001f);
					expected += 1.0f;
				}
			}
		}		
	}

	/**
	 * Tests copying from global memory organized in block form, along the row, into local memory in linearized format.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x2ThreadsCopyFromGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create4x4Block2x2Matrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float expected = 1.0f;
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false);
				}
			}
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected, lineArray[i * 8 + j], 0.001f);
					expected += 1.0f;
				}
			}
		}		
	}

	/**
	 * Tests copying from global memory organized in block form, along the row, into local memory in linearized format.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x4ThreadsCopyFromGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create2x2Block4x4Matrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float expected = 1.0f;
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false);
				}
			}
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected, lineArray[i * 8 + j], 0.001f);
					expected += 1.0f;
				}
			}
		}		
	}
	
	/**
	 * Tests copying from global memory organized in block form, along the row, into local memory in linearized format.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x2ThreadsCopyFromGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create2x2Block4x4Matrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float expected = 1.0f;
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false);
				}
			}
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected, lineArray[i * 8 + j], 0.001f);
					expected += 1.0f;
				}
			}
		}		
	}

	/**
	 * Tests copying from global memory organized in block form, along the column (transposed blocks), into local memory in linearized format.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x4ThreadsCopyFromGlobalTransposedPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create2x2Block4x4TransposedBlockMatrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float[][] expectedArrays = { {  1,  9, 17, 25, 33, 41, 49, 57,
			                            2, 10, 18, 26, 34, 42, 50, 58 },
				                     {  3, 11, 19, 27, 35, 43, 51, 59,
			                            4, 12, 20, 28, 36, 44, 52, 60 },
				                     {  5, 13, 21, 29, 37, 45, 53, 61,
			                            6, 14, 22, 30, 38, 46, 54, 62 },
				                     {  7, 15, 23, 31, 39, 47, 55, 63,
			                            8, 16, 24, 32, 40, 48, 56, 64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] expectedArray = expectedArrays[blocksIdx];
			
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true);
				}
			}
			
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expectedArray[i * 8 + j], lineArray[i * 8 + j], 0.001f);
				}
			}
		}		
	}

	/**
	 * Tests copying from global memory organized in block form, along the column (transposed blocks), into local memory in linearized format.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x2ThreadsCopyFromGlobalTransposedPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create2x2Block4x4TransposedBlockMatrix();
		float[] lineArray = new float[blockHeight * dimJ];

		float[][] expectedArrays = { {  1,  9, 17, 25, 33, 41, 49, 57,
			                            2, 10, 18, 26, 34, 42, 50, 58 },
				                     {  3, 11, 19, 27, 35, 43, 51, 59,
			                            4, 12, 20, 28, 36, 44, 52, 60 },
				                     {  5, 13, 21, 29, 37, 45, 53, 61,
			                            6, 14, 22, 30, 38, 46, 54, 62 },
				                     {  7, 15, 23, 31, 39, 47, 55, 63,
			                            8, 16, 24, 32, 40, 48, 56, 64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] expectedArray = expectedArrays[blocksIdx];
			
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true);
				}
			}
			
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expectedArray[i * 8 + j], lineArray[i * 8 + j], 0.001f);
				}
			}
		}		
	}
	
	/**
	 * Tests copying from global memory organized in block form, along the column (transposed blocks), into local memory in linearized format.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x2ThreadsCopyFromGlobalTransposedPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create4x4Block2x2TransposedBlockMatrix();
		float[] lineArray = new float[dimI * blockWidth];

		//The completely transposed array as expected in final result
		float[][] expectedArrays = { {  1,  9, 17, 25, 33, 41, 49, 57,
			                            2, 10, 18, 26, 34, 42, 50, 58,
				                        3, 11, 19, 27, 35, 43, 51, 59,
			                            4, 12, 20, 28, 36, 44, 52, 60 },
				                     {  5, 13, 21, 29, 37, 45, 53, 61,
			                            6, 14, 22, 30, 38, 46, 54, 62,
				                        7, 15, 23, 31, 39, 47, 55, 63,
			                            8, 16, 24, 32, 40, 48, 56, 64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] expectedArray = expectedArrays[blocksIdx];
			
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true);
				}
			}
			
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expectedArray[i * 8 + j], lineArray[i * 8 + j], 0.001f);
				}
			}
		}		
	}
	
	/**
	 * Tests copying from global memory organized in block form, along the column (transposed blocks), into local memory in linearized format.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x4ThreadsCopyFromGlobalTransposedPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;
		
		float[] blockArray = create4x4Block2x2TransposedBlockMatrix();
		float[] lineArray = new float[dimI * blockWidth];

		//The completely transposed array as expected in final result
		float[][] expectedArrays = { {  1,  9, 17, 25, 33, 41, 49, 57,
			                            2, 10, 18, 26, 34, 42, 50, 58,
				                        3, 11, 19, 27, 35, 43, 51, 59,
			                            4, 12, 20, 28, 36, 44, 52, 60 },
				                     {  5, 13, 21, 29, 37, 45, 53, 61,
			                            6, 14, 22, 30, 38, 46, 54, 62,
				                        7, 15, 23, 31, 39, 47, 55, 63,
			                            8, 16, 24, 32, 40, 48, 56, 64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] expectedArray = expectedArrays[blocksIdx];
			
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.readBlockFromMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true);
				}
			}
			
			for (int i = 0; i < blockHeight; i++) {
				for (int j = 0; j < dimJ; j++) {
					assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expectedArray[i * 8 + j], lineArray[i * 8 + j], 0.001f);
				}
			}
		}		
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x2ThreadsCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16 },
			                    {17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48 },
			                    {49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, false);
				}
			}
		}
		
		float[] expected = create2x2Block4x4Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[i * 8 + j], blockArray[i * 8 + j], 0.001f);
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x4ThreadsCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16 },
			                    {17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48 },
			                    {49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, false);
				}
			}
		}
		
		float[] expected = create2x2Block4x4Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[i * 8 + j], blockArray[i * 8 + j], 0.001f);
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x2ThreadsCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16,
			                     17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48,
			                     49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, false);
				}
			}
		}
		
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[i * 8 + j], blockArray[i * 8 + j], 0.001f);
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x4ThreadsCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16,
			                     17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48,
			                     49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, false);
				}
			}
		}
		
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[i * 8 + j], blockArray[i * 8 + j], 0.001f);
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row, but with transposed blocks.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x2ThreadsTransposedBlockCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16 },
			                    {17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48 },
			                    {49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, true);
				}
			}
		}
		
		float[] expected = create2x2Block4x4Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int blocksI = i / 2;
				int offsetBlocksI = i % 2;
				int blocksJ = j / 2;
				int offsetBlocksJ = j % 2;
				int offsetE = blocksI * 2*2*4 + blocksJ * 2*2 + offsetBlocksJ*2 + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * 2*2*4 + blocksJ * 2*2 + offsetBlocksI*2 + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row, but with transposed blocks.
	 * Blocks are sized 2x2.
	 * Resulting in matrix of 4x4 blocks.
	 * With 2 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test2x2Block4x4Matrix2x4ThreadsTransposedBlockCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 2;
		final int blockWidth = 2;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16 },
			                    {17,18,19,20,21,22,23,24,
			                     25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48 },
			                    {49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, true);
				}
			}
		}
		
		float[] expected = create2x2Block4x4Matrix();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int blocksI = i / 2;
				int offsetBlocksI = i % 2;
				int blocksJ = j / 2;
				int offsetBlocksJ = j % 2;
				int offsetE = blocksI * 2*2*4 + blocksJ * 2*2 + offsetBlocksJ*2 + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * 2*2*4 + blocksJ * 2*2 + offsetBlocksI*2 + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row, but with transposed blocks.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 2 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x2ThreadsTransposedBlockCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                       9,10,11,12,13,14,15,16,
			                      17,18,19,20,21,22,23,24,
			                      25,26,27,28,29,30,31,32 },
			                    { 33,34,35,36,37,38,39,40,
			                      41,42,43,44,45,46,47,48,
			                      49,50,51,52,53,54,55,56,
			                      57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, true);
				}
			}
		}
		
		final int blockSize = blockWidth * blockHeight;
		final int blocksPerRow = dimI / blockWidth;
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < dimI; i++) {
			for (int j = 0; j < dimJ; j++) {
				int blocksI = i / blockHeight;
				int offsetBlocksI = i % blockHeight;
				int blocksJ = j / blockWidth;
				int offsetBlocksJ = j % blockWidth;
				int offsetE = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksJ*blockWidth + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksI*blockWidth + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the row, but with transposed blocks.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x4ThreadsTransposedBlockCopyToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 5, 6, 7, 8, 
			                      9,10,11,12,13,14,15,16,
			                      17,18,19,20,21,22,23,24,
			                      25,26,27,28,29,30,31,32 },
			                    {33,34,35,36,37,38,39,40,
			                     41,42,43,44,45,46,47,48,
			                     49,50,51,52,53,54,55,56,
			                     57,58,59,60,61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, false, true);
				}
			}
		}
		
		final int blockSize = blockWidth * blockHeight;
		final int blocksPerRow = dimI / blockWidth;
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < dimI; i++) {
			for (int j = 0; j < dimJ; j++) {
				int blocksI = i / blockHeight;
				int offsetBlocksI = i % blockHeight;
				int blocksJ = j / blockWidth;
				int offsetBlocksJ = j % blockWidth;
				int offsetE = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksJ*blockWidth + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksI*blockWidth + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the column, but with transposed blocks.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x4ThreadsTransposedBlockCopyColumnToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 4;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 33,34,35,36,  
			                       9,10,11,12, 41,42,43,44,
			                      17,18,19,20, 49,50,51,52,
			                      25,26,27,28, 57,58,59,60 },
			                    {  5, 6, 7, 8, 37,38,39,40,
			                      13,14,15,16, 45,46,47,48,
			                      21,22,23,24, 53,54,55,56,
			                      29,30,31,32, 61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true, true);
				}
			}
		}
		
		final int blockSize = blockWidth * blockHeight;
		final int blocksPerRow = dimI / blockWidth;
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < dimI; i++) {
			for (int j = 0; j < dimJ; j++) {
				int blocksI = i / blockHeight;
				int offsetBlocksI = i % blockHeight;
				int blocksJ = j / blockWidth;
				int offsetBlocksJ = j % blockWidth;
				int offsetE = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksJ*blockWidth + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksI*blockWidth + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}

	/**
	 * Tests copying from local memory in linearized format into global memory organized in block form, along the column, but with transposed blocks.
	 * Blocks are sized 4x4.
	 * Resulting in matrix of 2x2 blocks.
	 * With 4 pseudo-threads in I direction (always the number of block lines) and 4 pseudo-threads in J direction.  
	 */
	@Test
	public void test4x4Block2x2Matrix4x2ThreadsTransposedBlockCopyColumnToGlobalPass() {
		final int dimI = 8;
		final int dimJ = 8;
		final int blockHeight = 4;
		final int blockWidth = 4;
		final int matrixOffset = 0; //Just consider first matrix
		
		final int threadsInI = blockHeight;
		final int threadsInJ = 2;
		final int blocksIPerMatrix = dimI / blockHeight;

		float[] blockArray = new float[dimI * dimJ];
		float[][] lineArrays = { { 1, 2, 3, 4, 33,34,35,36,  
			                       9,10,11,12, 41,42,43,44,
			                      17,18,19,20, 49,50,51,52,
			                      25,26,27,28, 57,58,59,60 },
			                    {  5, 6, 7, 8, 37,38,39,40,
			                      13,14,15,16, 45,46,47,48,
			                      21,22,23,24, 53,54,55,56,
			                      29,30,31,32, 61,62,63,64 } };
		
		for (int blocksIdx = 0; blocksIdx < blocksIPerMatrix; blocksIdx++) {
			float[] lineArray = lineArrays[blocksIdx];
			for (int threadI = 0; threadI < threadsInI; threadI++) {
				for (int threadJ = 0; threadJ < threadsInJ; threadJ++) {
					CrossCorrelationFFTParallelBlockKernel.writeBlockToMem(blockArray, lineArray, matrixOffset, blocksIdx, blockWidth, blockHeight, dimJ, threadsInJ, threadJ, threadI, true, true);
				}
			}
		}
		
		final int blockSize = blockWidth * blockHeight;
		final int blocksPerRow = dimI / blockWidth;
		float[] expected = create4x4Block2x2Matrix();
		for (int i = 0; i < dimI; i++) {
			for (int j = 0; j < dimJ; j++) {
				int blocksI = i / blockHeight;
				int offsetBlocksI = i % blockHeight;
				int blocksJ = j / blockWidth;
				int offsetBlocksJ = j % blockWidth;
				int offsetE = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksJ*blockWidth + offsetBlocksI; //Expected array is in non-transposed block form, so translation is needed
				int offsetA = blocksI * blockSize*blocksPerRow + blocksJ * blockSize + offsetBlocksI*blockWidth + offsetBlocksJ;
				assertEquals("Error at matrix element (i: " + i + ", j: " + j + ")", expected[offsetE], blockArray[offsetA], 0.001f); //blockArray[i * 8 + j]
			}
		}
	}
	
	/**
	 * Test copy matrix to array of blocks.
	 * Block 2x2.
	 * Array 8x8.
	 */
	@Test
	public void testCreateBlock2x2_8x8() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[64];
		m.copyMatrixToBlockArray(block, 0, 8, 2, 2);
		
		float[] refBlock = create2x2Block4x4Matrix();
		assertEquals("Block arrray size doesn't match", refBlock.length, block.length);
		for (int i = 0; i < refBlock.length; i++) {
			assertEquals("Block array value at: " + i + " doesn't match", refBlock[i], block[i], 1e-3f);
		}
	}

	/**
	 * Test copy matrix to mirrored array of blocks.
	 * Block 2x2.
	 * Array 8x8.
	 */
	@Test
	public void testCreateMirroredBlock2x2_8x8() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[64];
		m.copyMirroredMatrixToBlockArray(block, 0, 8, 2, 2);
		
		float[] refBlock = create2x2Block4x4Matrix();
		assertEquals("Block arrray size doesn't match", refBlock.length, block.length);
		for (int i = 0; i < refBlock.length; i++) {
			assertEquals("Block array value at: " + i + " doesn't match", refBlock[i], block[63 - i], 1e-3f);
		}
	}

	/**
	 * Test copy matrix to array of blocks.
	 * Block 4x4.
	 * Array 8x8.
	 */
	@Test
	public void testCreateBlock4x4_8x8() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[64];
		m.copyMatrixToBlockArray(block, 0, 8, 4, 4);
		
		float[] refBlock = create4x4Block2x2Matrix();
		assertEquals("Block arrray size doesn't match", refBlock.length, block.length);
		for (int i = 0; i < refBlock.length; i++) {
			assertEquals("Block array value at: " + i + " doesn't match", refBlock[i], block[i], 1e-3f);
		}
	}

	/**
	 * Test copy matrix to mirrored array of blocks.
	 * Block 4x4.
	 * Array 8x8.
	 */
	@Test
	public void testCreateMirroredBlock4x4_8x8() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[64];
		m.copyMirroredMatrixToBlockArray(block, 0, 8, 4, 4);
		
		float[] refBlock = create4x4Block2x2Matrix();
		assertEquals("Block arrray size doesn't match", refBlock.length, block.length);
		for (int i = 0; i < refBlock.length; i++) {
			assertEquals("Block array value at: " + i + " doesn't match", refBlock[i], block[63 - i], 1e-3f);
		}
	}

	/**
	 * Test copy matrix to zero-padded array of blocks.
	 * Block 2x2.
	 * Array 16x16.
	 */
	@Test
	public void testCreateBlock2x2_16x16CrossPad() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[16*16];
		m.copyMatrixToBlockArray(block, 0, 16, 2, 2);
		
		float[] refBlock = create2x2Block4x4Matrix();
		for (int blocksI = 0; blocksI < 4; blocksI++) {
			for (int blocksJ = 0; blocksJ < 4; blocksJ++) {
				for (int i = 0; i < 4; i++) {
					assertEquals("Block array value at: (blockI: " + blocksI + ", blockJ: " + blocksJ + ") - " + i + " doesn't match", 
							refBlock[blocksI * 2*8 + blocksJ * 4 + i], block[blocksI * 2* 16 + blocksJ * 4 + i], 1e-3f);
				}
			}
		}
	}

	/**
	 * Test copy matrix to zero-padded mirrored array of blocks.
	 * Block 2x2.
	 * Array 16x16.
	 */
	@Test
	public void testCreateMirroredBlock2x2_16x16CrossPad() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[16*16];
		m.copyMirroredMatrixToBlockArray(block, 0, 16, 2, 2);
		
		float[] refBlock = create2x2Block4x4Matrix();
		for (int blocksI = 0; blocksI < 4; blocksI++) {
			for (int blocksJ = 0; blocksJ < 4; blocksJ++) {
				for (int i = 0; i < 4; i++) {
					assertEquals("Block array value at: (blockI: " + blocksI + ", blockJ: " + blocksJ + ") - " + i + " doesn't match", 
							refBlock[blocksI * 2*8 + blocksJ * 4 + i], block[(3 - blocksI) * 2* 16 + (3 - blocksJ) * 4 + (3 - i)], 1e-3f);
				}
			}
		}
	}

	/**
	 * Test copy matrix to zero-padded array of blocks.
	 * Block 4x4.
	 * Array 16x16.
	 */
	@Test
	public void testCreateBlock4x4_16x16CrossPad() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[16*16];
		m.copyMatrixToBlockArray(block, 0, 16, 4, 4);
		
		float[] refBlock = create4x4Block2x2Matrix();
		for (int blocksI = 0; blocksI < 2; blocksI++) {
			for (int blocksJ = 0; blocksJ < 2; blocksJ++) {
				for (int i = 0; i < 16; i++) {
					assertEquals("Block array value at: (blockI: " + blocksI + ", blockJ: " + blocksJ + ") - " + i + " doesn't match", 
							refBlock[blocksI * 4*8 + blocksJ * 16 + i], block[blocksI * 4* 16 + blocksJ * 16 + i], 1e-3f);
				}
			}
		}
	}

	/**
	 * Test copy matrix to zero-padded mirrored array of blocks.
	 * Block 4x4.
	 * Array 16x16.
	 */
	@Test
	public void testCreateMirroredBlock4x4_16x16CrossPad() {
		float[] array = createMatrix();
		Matrix m = new MatrixFloat(array, 8, 8);
		float[] block = new float[16*16];
		m.copyMirroredMatrixToBlockArray(block, 0, 16, 4, 4);
		
		float[] refBlock = create4x4Block2x2Matrix();
		for (int blocksI = 0; blocksI < 2; blocksI++) {
			for (int blocksJ = 0; blocksJ < 2; blocksJ++) {
				for (int i = 0; i < 16; i++) {
					assertEquals("Block array value at: (blockI: " + blocksI + ", blockJ: " + blocksJ + ") - " + i + " doesn't match", 
							refBlock[blocksI * 4*8 + blocksJ * 16 + i], block[(1 - blocksI) * 4* 16 + (1 - blocksJ) * 16 + (15 - i)], 1e-3f);
				}
			}
		}
	}
	
	@Test
	public void testCreateMatrixFromBlock4x4() {
		float blockMatrix[] = create4x4Block2x2Matrix();
		
		float resultMatrix[] = new float[7 * 7];
		Matrix m = new MatrixFloat(resultMatrix, 7, 7);
		m.copyMatrixFromLargerBlockArray(blockMatrix, 0, 8, 4, 4);
		
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				float value = m.getElement(i, j);
				assertEquals("Incorrect value at: (i: " + i + ", j: "+ j + ")", i * 8 + (j + 1), value, 1e-4f);
			}
		}
		
	}
}

