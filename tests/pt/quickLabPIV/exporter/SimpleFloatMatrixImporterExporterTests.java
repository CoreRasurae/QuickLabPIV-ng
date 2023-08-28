// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.exporter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InvalidNameException;

import org.junit.Test;

import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;

public class SimpleFloatMatrixImporterExporterTests {

    @Test
    public void testFloatMatrixWritesTest() throws IOException, InvalidNameException {
        String inputTestDataFilename = "testFiles" + File.separator + "denseLucasKanade_with_LiuShen_2LevelsPyr_Fs2_0_Fs_0_48_validation.matFloat";
        String outputTestDataFilename = "simpleFloatMatrixWritesTest.matFloat";
        int imageAIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(inputTestDataFilename, "imageA");
        if (imageAIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        float[][] imageValidLSA = SimpleFloatMatrixImporterExporter.readFromFormattedFile(inputTestDataFilename, imageAIndex);
        int imageBIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(inputTestDataFilename, "imageB");
        if (imageBIndex < 0) {
            throw new InvalidNameException("Cannot find matrix");
        }
        float[][] imageValidLSB = SimpleFloatMatrixImporterExporter.readFromFormattedFile(inputTestDataFilename, imageBIndex);
        
        List<String> names = new ArrayList<String>(2);
        names.add("testA");
        names.add("testB");
        
        List<float[][]> matrices = new ArrayList<float[][]>(2);
        matrices.add(imageValidLSA);
        matrices.add(imageValidLSB);

        SimpleFloatMatrixImporterExporter.writeToFormattedFile(outputTestDataFilename, names, matrices);
        
        String[] namesRead = SimpleFloatMatrixImporterExporter.getMatricesNames(outputTestDataFilename);
        assertEquals("Number of matrices in file does not match the expected", 2, namesRead.length);
        
        assertEquals("Name of first matrix does not match the expected", "testA", namesRead[0]);
        assertEquals("Name of second matrix does not match the expected", "testB", namesRead[1]);
        
        int testAIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(outputTestDataFilename, "testA");
        int testBIndex = SimpleFloatMatrixImporterExporter.getMatrixIndexFromName(outputTestDataFilename, "testB");

        assertEquals("Index of first matrix does not match the expected", 0, testAIndex);
        assertEquals("Index of second matrix does not match the expected", 1, testBIndex);

        float[][] testA = SimpleFloatMatrixImporterExporter.readFromFormattedFile(outputTestDataFilename, testAIndex);
        for (int i = 0; i < testA.length; i++) {
            for (int j = 0; j < testA[0].length; j++) {
                assertEquals("Matrix A does not match at: (I= " + i + ", J= " + j + ")", imageValidLSA[i][j], testA[i][j], 1e-8f);
            }
        }

        float[][] testB = SimpleFloatMatrixImporterExporter.readFromFormattedFile(outputTestDataFilename, testBIndex);
        for (int i = 0; i < testB.length; i++) {
            for (int j = 0; j < testB[0].length; j++) {
                assertEquals("Matrix B does not match at: (I= " + i + ", J= " + j + ")", imageValidLSB[i][j], testB[i][j], 1e-8f);
            }
        }
        
        File f = new File(outputTestDataFilename);
        if (f.exists()) {
            f.delete();
        }
    }
}
