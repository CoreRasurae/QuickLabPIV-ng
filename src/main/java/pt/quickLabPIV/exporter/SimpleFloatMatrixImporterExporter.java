// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleFloatMatrixImporterExporter {
  private boolean useArrayBuffer = false;
  private ByteBuffer buffer;
  private byte[] arrBuffer;
  private boolean validated = false;
  private int namesHeaderSize = -1;
  
  public void openFormattedFileAndLoadToBuffer(Path filePath, boolean map, boolean useArrBuffer) throws IOException {
      FileChannel fc = FileChannel.open(filePath, StandardOpenOption.READ);
      try {
          if (map) {
              buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
          } else {
              buffer = ByteBuffer.allocateDirect((int)fc.size());
              fc.read(buffer);
              buffer.rewind();
          }
          
          if (useArrBuffer) {
              arrBuffer = new byte[(int)fc.size()];
              buffer.get(arrBuffer);
              buffer = null;
          }
      } finally {
          fc.close();
      }
      
      useArrayBuffer = useArrBuffer;
      validated = false;
      namesHeaderSize = -1;
  }
  
  public String[] getMatricesNames() throws IOException {
      if (useArrayBuffer) {
          return getMatricesNamesFromArrayBuffer();
      } else {
          return getMatricesNames(buffer);
      }
  }
  
  public float[][] readMatrix(int idx) throws IOException {
      if (useArrayBuffer) {
          return readMatrixFromArrayBuffer(idx);
      } else {
          return readMatrix(buffer, idx);
      }
  }
  
  private String[] getMatricesNamesFromArrayBuffer() throws FileNotFoundException {
      int offset = 0;
      if (!validated) {
          int[] signature = readFromArrayBuffer(arrBuffer, 0, 9);
          if (signature[0] != 'M' || signature[1] != 'A' || signature[2] != 'T' || 
              signature[3] != 'F' || signature[4] != 'l' || signature[5] != 'o' || signature[6] != 'a' || signature[7] != 't' || signature[8] != '\0') {
              throw new FileNotFoundException("Wrong file signature");
          }
      }
      
      offset += 9;
      int numberOfMatrices = readShortFromArrayBuffer(arrBuffer, offset);
      offset += 2;

      String names[] = new String[numberOfMatrices];
      
      for (int  i = 0; i < numberOfMatrices; i++) {
          int nameLength = readShortFromArrayBuffer(arrBuffer, offset);
          offset += 2;
          String name = readNameFromArrayBuffer(arrBuffer, offset, nameLength);
          names[i] = name;
          offset += nameLength;
      }
      if (namesHeaderSize == -1) {
          namesHeaderSize = offset;
      }
      buffer.rewind();
      
      return names;      
  }

  private float[][] readMatrixFromArrayBuffer(int index) throws FileNotFoundException {
      int offset = 0;
      if (!validated) {
          int[] signature = readFromArrayBuffer(arrBuffer, 0, 9);
          if (signature[0] != 'M' || signature[1] != 'A' || signature[2] != 'T' || 
              signature[3] != 'F' || signature[4] != 'l' || signature[5] != 'o' || signature[6] != 'a' || signature[7] != 't' || signature[8] != '\0') {
              throw new FileNotFoundException("Wrong file signature");
          }
      }
      
      if (namesHeaderSize == -1) {
          offset += 9;
          int numberOfMatrices = readShortFromArrayBuffer(arrBuffer, offset);
          if (index >= numberOfMatrices) {
              throw new ArrayIndexOutOfBoundsException("The requested matrix index [" + index + "] is outside of the range of the number of matrices [" + numberOfMatrices + "] contained in the file.");
          }
          offset += 2;
          
          String nameOfMatrixToBeRead = "";
          for (int  i = 0; i < numberOfMatrices; i++) {
              int nameLength = readShortFromArrayBuffer(arrBuffer, offset);
              offset += 2;
              String name = readNameFromArrayBuffer(arrBuffer, offset, nameLength);
              if (i == index) {
                  nameOfMatrixToBeRead = name;
              }
              offset += nameLength;
          }
          System.out.println("Reading matrix: " + nameOfMatrixToBeRead);
          namesHeaderSize = offset;
      } else {
          offset = namesHeaderSize;
      }

      while (index > 0) {
          int sizeX = readShortFromArrayBuffer(arrBuffer, offset);
          offset += 2;
          int sizeY = readShortFromArrayBuffer(arrBuffer, offset);
          offset += 2;
          int sizeZ = readShortFromArrayBuffer(arrBuffer, offset);
          offset += 2;
          
          if (sizeX == 0 || sizeY == 0 || sizeZ != 0) {
              throw new FileNotFoundException("File contains a matrix with different dimension from the expected: [X= " + sizeX + ", Y= " + sizeY + ", Z= " + sizeZ + "]");
          }
          offset += sizeX * sizeY * Float.BYTES;
          index--;
      }
      
      int sizeX = readShortFromArrayBuffer(arrBuffer, offset);
      offset += 2;
      int sizeY = readShortFromArrayBuffer(arrBuffer, offset);
      offset += 2;
      int sizeZ = readShortFromArrayBuffer(arrBuffer, offset);
      offset += 2;
      
      if (sizeX == 0 || sizeY == 0 || sizeZ != 0) {
          throw new FileNotFoundException("File contains a matrix with different dimension from the expected: [X= " + sizeX + ", Y= " + sizeY + ", Z= " + sizeZ + "]");
      }

      float[][] result = new float[sizeY][sizeX];
      for (int i = 0; i < sizeY; i++) {
          for (int j = 0; j < sizeX; j++) {
              result[i][j] = readFloatFromArrayBuffer(arrBuffer, offset);
              offset += 4;
          }
      }
      
      return result;
  }
  
  private static String[] getMatricesNames(final ByteBuffer buffer) throws IOException {
      int offset = 0;
      int[] signature = readFromFormattedFile(buffer, 0, 9);
      if (signature[0] != 'M' || signature[1] != 'A' || signature[2] != 'T' || 
          signature[3] != 'F' || signature[4] != 'l' || signature[5] != 'o' || signature[6] != 'a' || signature[7] != 't' || signature[8] != '\0') {
          throw new FileNotFoundException("Wrong file signature");
      }
      
      offset += 9;
      int numberOfMatrices = readShortFromFormattedFile(buffer, offset);
      offset += 2;

      String names[] = new String[numberOfMatrices];
      
      for (int  i = 0; i < numberOfMatrices; i++) {
          int nameLength = readShortFromFormattedFile(buffer, offset);
          offset += 2;
          String name = readNameFromFormattedFile(buffer, offset, nameLength);
          names[i] = name;
          offset += nameLength;
      }
      buffer.rewind();
      
      return names;
  }
  
  public static float[][] readMatrix(final ByteBuffer buffer, int index) throws IOException {
      int offset = 0;
      int[] signature = readFromFormattedFile(buffer, 0, 9);
      if (signature[0] != 'M' || signature[1] != 'A' || signature[2] != 'T' || 
          signature[3] != 'F' || signature[4] != 'l' || signature[5] != 'o' || signature[6] != 'a' || signature[7] != 't' || signature[8] != '\0') {
          throw new FileNotFoundException("Wrong file signature");
      }
      
      offset += 9;
      int numberOfMatrices = readShortFromFormattedFile(buffer, offset);
      if (index >= numberOfMatrices) {
          throw new ArrayIndexOutOfBoundsException("The requested matrix index [" + index + "] is outside of the range of the number of matrices [" + numberOfMatrices + "] contained in the file.");
      }
      offset += 2;
      
      String nameOfMatrixToBeRead = "";
      for (int  i = 0; i < numberOfMatrices; i++) {
          int nameLength = readShortFromFormattedFile(buffer, offset);
          offset += 2;
          String name = readNameFromFormattedFile(buffer, offset, nameLength);
          if (i == index) {
              nameOfMatrixToBeRead = name;
          }
          offset += nameLength;
      }
      System.out.println("Reading matrix: " + nameOfMatrixToBeRead);

      while (index > 0) {
          int sizeX = readShortFromFormattedFile(buffer, offset);
          offset += 2;
          int sizeY = readShortFromFormattedFile(buffer, offset);
          offset += 2;
          int sizeZ = readShortFromFormattedFile(buffer, offset);
          offset += 2;
          
          if (sizeX == 0 || sizeY == 0 || sizeZ != 0) {
              throw new FileNotFoundException("File contains a matrix with different dimension from the expected: [X= " + sizeX + ", Y= " + sizeY + ", Z= " + sizeZ + "]");
          }
          offset += sizeX * sizeY * Float.BYTES;
          index--;
      }
      
      int sizeX = readShortFromFormattedFile(buffer, offset);
      offset += 2;
      int sizeY = readShortFromFormattedFile(buffer, offset);
      offset += 2;
      int sizeZ = readShortFromFormattedFile(buffer, offset);
      offset += 2;
      
      if (sizeX == 0 || sizeY == 0 || sizeZ != 0) {
          throw new FileNotFoundException("File contains a matrix with different dimension from the expected: [X= " + sizeX + ", Y= " + sizeY + ", Z= " + sizeZ + "]");
      }

      float[][] result = new float[sizeY][sizeX];
      for (int i = 0; i < sizeY; i++) {
          for (int j = 0; j < sizeX; j++) {
              result[i][j] = readFloatFromFormattedFile(buffer, offset);
              offset += 4;
          }
      }
      buffer.rewind();
      
      return result;
  }
  
  public static void writeToFormattedFile(String filename, List<String> names, List<float[][]> matrices) throws IOException {
      File f = new File(filename);
      Set<StandardOpenOption> options = new HashSet<>();
      options.add(StandardOpenOption.CREATE);
      options.add(StandardOpenOption.WRITE);
      FileChannel fc = FileChannel.open(f.toPath(), options);
      int requiredSize = 9;
      
      requiredSize += Short.BYTES;
      
      for (String name : names) {
          requiredSize += Short.BYTES;
          requiredSize += name.length();
      }
      
      requiredSize += matrices.size() * Short.BYTES * 3;
      for (float[][] matrix : matrices) {          
          requiredSize += matrix.length * matrix[0].length * Float.BYTES;
      }
            
      ByteBuffer buffer = ByteBuffer.allocate(requiredSize);
      buffer.order(ByteOrder.BIG_ENDIAN);
      String signature = "MATFloat\0";
      buffer.put(signature.getBytes());
      
      buffer.putShort((short)names.size());
      
      for (int idx = 0; idx < names.size(); idx++) {
          short nameLength = (short)names.get(idx).length();
          buffer.putShort(nameLength);
          byte[] nameBytes = names.get(idx).getBytes();
          buffer.put(nameBytes);
      }
      
      for (int idx = 0; idx < names.size(); idx++) {
          float[][] matrix = matrices.get(idx);
          short sizeX = (short)matrix[0].length;          
          short sizeY = (short)matrix.length;
          //
          buffer.putShort(sizeX);
          buffer.putShort(sizeY);
          buffer.putShort((short)0);
          for (int i = 0; i < matrix.length; i++) {
              for (int j = 0; j < matrix[0].length; j++) {
                  buffer.putFloat(matrix[i][j]);
              }
          }
      }
      buffer.rewind();
      fc.write(buffer);
      fc.close();
  }
    
  public static float[][] readFromFormattedFile(Path filePath, int index, boolean map) throws IOException {
      FileChannel fc = FileChannel.open(filePath, StandardOpenOption.READ);
      ByteBuffer buffer = null;
      if (map) {
          buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      } else {
          buffer = ByteBuffer.allocate((int)fc.size());
          fc.read(buffer);
          buffer.rewind();
      }

      float[][] result = null;
      try {
          result = readMatrix(buffer, index);
      } finally {
          fc.close();    
      }
      
      return result;
  }

  public static float[][] readFromFormattedFile(String filename, int index) throws IOException {      
      File f = new File(filename);
      return readFromFormattedFile(f.toPath(), index, true);
  }
  
  public static int getNumberOfMatrices(String filename) throws IOException {
      File f = new File(filename);
      FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.READ);
      MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
      
      int offset = 0;
      int[] signature = readFromFormattedFile(buffer, 0, 9);
      if (signature[0] != 'M' || signature[1] != 'A' || signature[2] != 'T' || 
          signature[3] != 'F' || signature[4] != 'l' || signature[5] != 'o' || signature[6] != 'a' || signature[7] != 't' || signature[8] != '\0') {
          fc.close();
          throw new FileNotFoundException("Wrong file signature");
      }
      
      offset += 9;
      int numberOfMatrices = readShortFromFormattedFile(buffer, offset);

      fc.close();
      
      return numberOfMatrices;
  }
  
  public static String[] getMatricesNames(String filename) throws IOException {
      File f = new File(filename);
      FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.READ);
      MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, f.length());

      String names[] = null;
      try {
          names = getMatricesNames(buffer);
      } finally {
          fc.close();
      }

      return names;
  }
  
  public static int getMatrixIndexFromName(String filename, String matrixName) throws IOException {
      String[] names = getMatricesNames(filename);
      for (int index = 0; index < names.length; index++) {
          if (matrixName.equalsIgnoreCase(names[index])) {
              return index;
          }
      }
      
      return -1;
  }
  
  static int[] readFromFormattedFile(ByteBuffer buf, int offset, int bytes) throws IOException {
      buf.position(offset);
      int[] ba = new int[bytes];
      int read = 0;
      while (read < bytes) {
          int readByte = buf.get();
          if (readByte < 0) {
              readByte += 256;
          }
          ba[read++] = readByte;
      }
      return ba;
  }

  static String readNameFromFormattedFile(ByteBuffer buf, int offset, int len) throws IOException {
      int[] nameArray = readFromFormattedFile(buf, offset, len); 
      
      char[] nameChars = new char[len];
      for (int i = 0; i < len; i++) {
          nameChars[i] = (char)nameArray[i];
      }
      
      String name = String.valueOf(nameChars);
      
      return name;
  }

  static int readShortFromFormattedFile(ByteBuffer buf, int offset) throws IOException {
      int[] sizeArray = readFromFormattedFile(buf, offset, 2); 
      int value = sizeArray[0] << 8 | sizeArray[1];
      return value;
  }

  static float readFloatFromFormattedFile(ByteBuffer buf, int offset) throws IOException {
      int[] floatArray = readFromFormattedFile(buf, offset, 4);
      int intValue = floatArray[0] << 24 | floatArray[1] << 16 | floatArray[2] << 8 | floatArray[3];
      float value = Float.intBitsToFloat(intValue);
      return value;
  }
  
  private int[] readFromArrayBuffer(byte buf[], int offset, int bytes) {
      int[] ba = new int[bytes];
      int read = 0;
      while (read < bytes) {
          int readByte = buf[offset++];
          if (readByte < 0) {
              readByte += 256;
          }
          ba[read++] = readByte;
      }
      return ba;
  }
  
  private int readShortFromArrayBuffer(byte buf[], int offset) {
      int[] sizeArray = readFromArrayBuffer(buf, offset, 2); 
      int value = sizeArray[0] << 8 | sizeArray[1];
      return value;
  }

  private float readFloatFromArrayBuffer(byte buf[], int offset) {
      int[] floatArray = readFromArrayBuffer(buf, offset, 4);
      int intValue = floatArray[0] << 24 | floatArray[1] << 16 | floatArray[2] << 8 | floatArray[3];
      float value = Float.intBitsToFloat(intValue);
      return value;
  }


  private String readNameFromArrayBuffer(byte buf[], int offset, int len) {
      int[] nameArray = readFromArrayBuffer(buf, offset, len); 
      
      char[] nameChars = new char[len];
      for (int i = 0; i < len; i++) {
          nameChars[i] = (char)nameArray[i];
      }
      
      String name = String.valueOf(nameChars);
      
      return name;
  }
}
