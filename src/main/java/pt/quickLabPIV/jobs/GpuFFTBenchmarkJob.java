package pt.quickLabPIV.jobs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.DeviceRuntimeConfiguration;
import pt.quickLabPIV.Matrix;
import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVRunParameters;
import pt.quickLabPIV.WarpingModeFactoryEnum;
import pt.quickLabPIV.device.ComputationDevice;
import pt.quickLabPIV.exporter.SimpleFloatMatrixImporterExporter;
import pt.quickLabPIV.iareas.AdaptiveInterAreaStrategyNoSuperPosition;
import pt.quickLabPIV.iareas.InterAreaStableStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.InterAreaVelocityStrategiesFactoryEnum;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.iareas.TilesOrderEnum;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageReaderException;
import pt.quickLabPIV.jobs.xcorr.CrossCorrelationRealFFTParStdJob;
import pt.quickLabPIV.jobs.xcorr.XCorrelationResults;

public class GpuFFTBenchmarkJob extends Job<ComputationDevice, Float> {
    private static Logger logger = LoggerFactory.getLogger(GpuFFTBenchmarkJob.class);

    private IImage images[] = new IImage[2];
    
    private IterationStepTiles stepTilesA, stepTilesB;

    private Job<List<Tile>, XCorrelationResults> openCLJob;
    private ImageWarpingAndClippingJob warpingAndClippingJob;
    private boolean exportResults = false;
        
    private Path[] getImagesPaths() {
        URI jarPath = null;
        try {
            jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e1) {
            return null;
        }
        
        try {            
            FileSystem fs = null;
            String path = "";
            if (jarPath.getPath().endsWith(".jar")) {
                URI uri = URI.create("jar:file:" + jarPath.getRawPath());
                path = File.separator + "resources" + File.separator + "fftBenchmark" + File.separator;
                try {
                    fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException e) {
                    fs = FileSystems.getFileSystem(uri);
                }
            } else {
                path = Paths.get(jarPath) + File.separator + ".." + File.separator + "resources" + File.separator + "fftBenchmark" + File.separator;
                fs = FileSystems.getDefault();
            }

            List<Path> collect  = Files.walk(fs.getPath(path + "rankine_vortex01_0.tif"))
                                  .filter(Files::isRegularFile)
                                  .sorted()
                                  .collect(Collectors.toList());
       
            List<Path> collect2 = Files.walk(fs.getPath(path + "rankine_vortex01_1.tif"))
                                .filter(Files::isRegularFile)
                                .sorted()
                                .collect(Collectors.toList());

            collect.addAll(collect2);
            for (Path file : collect)
                logger.info("Found file {}", file.toString());
            
            if (collect.size() == 0) {
                return null;
            }

            logger.info("Found {} images.", collect.size());
            Path filesPaths[] = new Path[] {collect.get(0), collect.get(1)};
            return filesPaths;
        } catch (IOException e) {
            return null;
        }
    }

    private Path getFFTValidationData() {
        URI jarPath = null;
        try {
            jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e1) {
            return null;
        }
        
        try {            
            FileSystem fs = null;
            String path = "";
            if (jarPath.getPath().endsWith(".jar")) {
                URI uri = URI.create("jar:file:" + jarPath.getRawPath());
                path = File.separator + "resources" + File.separator + "fftBenchmark" + File.separator;
                try {
                    fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException e) {
                    fs = FileSystems.getFileSystem(uri);
                }
            } else {
                path = Paths.get(jarPath) + File.separator + ".." + File.separator + "resources" + File.separator + "fftBenchmark" + File.separator;
                fs = FileSystems.getDefault();
            }

            List<Path> collect  = Files.walk(fs.getPath(path + "gpuFFTBenchmarkValidationData.matFloat"))
                                  .filter(Files::isRegularFile)
                                  .collect(Collectors.toList());
       
            for (Path file : collect)
                logger.info("Found FFT validation data file {}", file.toString());
            
            if (collect.size() == 0) {
                return null;
            }

            logger.info("Found {} validation datas files.", collect.size());
            return collect.get(0);
        } catch (IOException e) {
            return null;
        }
    }
    
    @Override
    public void analyze() {
        Path[] imagesPaths = getImagesPaths();
        int idx = 0;
        for (Path path : imagesPaths) {
           try {
               BufferedImage bi = null;
               try {
                   bi = ImageIO.read(path.toUri().toURL());
               } catch (IOException e) {
                   throw new ImageReaderException("Failed to read file: " + path.toUri().toURL(), e);
               }
               Image image = new Image(bi, path.toString());
               images[idx++] = image;
            } catch (MalformedURLException e) {
               e.printStackTrace();
            }
        }

        PIVInputParameters parameters = PIVContextSingleton.getSingleton().getPIVParameters();
        parameters.setAreaStableStrategy(InterAreaStableStrategiesFactoryEnum.SimpleStrategy);
        parameters.setVelocityInheritanceStrategy(InterAreaVelocityStrategiesFactoryEnum.Direct);
        parameters.setImageHeightPixels(images[0].getHeight());
        parameters.setImageWidthPixels(images[0].getWidth());
        parameters.setMarginPixelsITop(0);
        parameters.setMarginPixelsIBottom(0);
        parameters.setMarginPixelsJLeft(0);
        parameters.setMarginPixelsJRight(0);
        parameters.setInterrogationAreaStartIPixels(16);
        parameters.setInterrogationAreaEndIPixels(16);
        parameters.setInterrogationAreaStartJPixels(16);
        parameters.setInterrogationAreaEndJPixels(16);
        parameters.setWarpingMode(WarpingModeFactoryEnum.NoImageWarping);
        
        ComputationDevice computationDevice = getInputParameters(JobResultEnum.JOB_RESULT_TEST_DEVICE);
        
        DeviceRuntimeConfiguration runDevConfig = new DeviceRuntimeConfiguration();
        runDevConfig.setCpuThreadAssignments(new int[] {0});
        runDevConfig.setDevice(computationDevice);
        runDevConfig.setScore(1.0f);
        
        PIVRunParameters runParameters = PIVContextSingleton.getSingleton().getPIVRunParameters();
        runParameters.setUseOpenCL(true);
        runParameters.setTotalNumberOfThreads(1);        
        runParameters.putDeviceConfiguration(runDevConfig);
        
        warpingAndClippingJob = new ImageWarpingAndClippingJob();
    
        //TODO Add support for validation data export upon special request, in order to build the cross correlation validation data files
        //
        AdaptiveInterAreaStrategyNoSuperPosition strategy = new AdaptiveInterAreaStrategyNoSuperPosition();
        stepTilesA = strategy.createIterationStepTilesParameters(TilesOrderEnum.FirstImage, null);
        stepTilesB = strategy.createIterationStepTilesParameters(TilesOrderEnum.SecondImage, null);
        //        
        openCLJob = new CrossCorrelationRealFFTParStdJob(false, computationDevice, null, true);
        ImageWarpingInputData warpingInputData = new ImageWarpingInputData();
        warpingInputData.imageA = images[0];
        warpingInputData.imageB = images[1];
        warpingInputData.stepTilesA = stepTilesA;
        warpingInputData.stepTilesB = stepTilesB;
        warpingAndClippingJob.setInputParameters(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING, warpingInputData);
        warpingAndClippingJob.analyze();
    }

    @Override
    public void compute() {
        warpingAndClippingJob.compute();
        IterationStepTiles[] allStepTiles = warpingAndClippingJob.getJobResult(JobResultEnum.JOB_RESULT_IMAGES_FOR_WARPING_AND_CLIPPING);
        List<Tile> tilesA = new ArrayList<Tile>(allStepTiles[0].getNumberOfTilesInI() * allStepTiles[0].getNumberOfTilesInJ());
        List<Tile> tilesB = new ArrayList<Tile>(allStepTiles[0].getNumberOfTilesInI() * allStepTiles[0].getNumberOfTilesInJ());
        for (int i = 0; i < allStepTiles[0].getNumberOfTilesInI(); i++) {
            for (int j = 0; j < allStepTiles[0].getNumberOfTilesInJ(); j++) {
                Tile tileA = allStepTiles[0].getTile(i, j);
                Tile tileB = allStepTiles[1].getTile(i, j);
                tilesA.add(tileA);
                tilesB.add(tileB);
            }
        }
        openCLJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesA);
        openCLJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesB);
        openCLJob.analyze();        
        openCLJob.compute();
        XCorrelationResults results = openCLJob.getJobResult(JobResultEnum.JOB_RESULT_CROSS_MATRICES);
        List<Matrix> crossMatrices = results.getCrossMatrices();
        boolean validatedOk = exportOrValidateFFTResults(allStepTiles, results, crossMatrices);
        if (!validatedOk) {
            return;
        }
       
        //TODO Refactorize GpuFFTJobTemplate and respective concretizations to enable profiling with registerProfileObserver(...)
        long globalElapsed = 0;
        for (int i = 0; i < 40; i++) {
            openCLJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_A, tilesA);
            openCLJob.setInputParameters(JobResultEnum.JOB_RESULT_CLIPPED_TILES_B, tilesB);
            openCLJob.analyze();        
            long startTime = System.currentTimeMillis(); 
            openCLJob.compute();
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            globalElapsed += elapsed;
        }
        
        float elapsedSeconds = (float)globalElapsed / 1000.0f;
        
        setJobResult(JobResultEnum.JOB_RESULT_TEST_DEVICE, (1.0f / elapsedSeconds) * 100.0f + 1.0f);
    }

    private boolean exportOrValidateFFTResults(IterationStepTiles[] allStepTiles, XCorrelationResults results,
            List<Matrix> crossMatrices) {
        if (exportResults) {
            List<float[][]> crossArrays = new ArrayList<>(crossMatrices.size());
            List<String> names = new ArrayList<>(crossMatrices.size());
            //
            int i = 0;
            int j = 0;
            for (Matrix crossMatrix : crossMatrices) {
                int crossDimI = crossMatrix.getHeight();
                int crossDimJ = crossMatrix.getWidth();
                //
                float[][] crossMatrixF = new float[crossDimI][crossDimJ];
                crossMatrix.copyMatrixTo2DArray(crossMatrixF, 0, 0);
                StringBuilder sb = new StringBuilder(20);
                sb.append("crossMatrix_");
                sb.append(i);
                sb.append("x");
                sb.append(j);
                String name = sb.toString();
                
                crossArrays.add(crossMatrixF);
                names.add(name);
                
                j++;
                if (j == allStepTiles[0].getNumberOfTilesInJ()) {
                    j = 0;
                    i++;
                }
            }
            //
            try {
                SimpleFloatMatrixImporterExporter.writeToFormattedFile("gpuFFTBenchmarkValidationData.matFloat", names, crossArrays);
            } catch (Exception ex) {
                throw new JobComputeException("Failed to export validation datafile");
            }
            
            return false;
        } else {
            int idx = 0;
            Path path = getFFTValidationData();
            SimpleFloatMatrixImporterExporter validationFloatMat = new SimpleFloatMatrixImporterExporter();
            try {
                validationFloatMat.openFormattedFileAndLoadToBuffer(path, false, true);
            } catch (IOException e) {
                throw new JobComputeException("Failed to read validation datafile at matrix index: " + idx);
            }

            for (Matrix crossMatrix : crossMatrices) {
                int crossDimI = crossMatrix.getHeight();
                int crossDimJ = crossMatrix.getWidth();

                float validationMatrix[][] = null;
                try {
                    validationMatrix = validationFloatMat.readMatrix(idx++);
                } catch (IOException e) {
                    throw new JobComputeException("Failed to read validation datafile at matrix index: " + idx);
                }
                
                for (int i = 0; i < crossDimI; i++) {
                    for (int j = 0; j < crossDimJ; j++) {
                        if (FastMath.abs(crossMatrix.getElement(i, j) - validationMatrix[i][j]) > 1e-2f) {
                            setJobResult(JobResultEnum.JOB_RESULT_TEST_DEVICE, 0.0f);
                            return false;
                        }
                    }
                }
            }
            
            return true;
        }
    }

    @Override
    public void dispose() {
        if (openCLJob != null) {
            openCLJob.dispose();
        }

        stepTilesA = null;
        stepTilesB = null;
        images = null;
        openCLJob = null;
        
        PIVContextSingleton.getSingleton().resetParametersInstances();
    }


}
