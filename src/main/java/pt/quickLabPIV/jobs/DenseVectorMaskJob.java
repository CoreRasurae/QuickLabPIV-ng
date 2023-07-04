package pt.quickLabPIV.jobs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.images.Image;
import pt.quickLabPIV.images.ImageNotFoundException;
import pt.quickLabPIV.images.ImageReaderException;

public class DenseVectorMaskJob extends Job<IterationStepTiles, IterationStepTiles> {
    private final String maskFilename;
    private IImage maskImage;
    
    public DenseVectorMaskJob() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVInputParameters parameters = singleton.getPIVParameters();
        maskFilename = parameters.getMaskFilename();
    }
    
    @Override
    public void analyze() {
        if (maskFilename != null && !maskFilename.isEmpty() && maskImage == null) {
            File f = new File(maskFilename);
            if (!f.exists() || f.isDirectory()) {
                throw new ImageNotFoundException("Cannot find image file"); 
            }
            
            if (!f.canRead()) {
                throw new ImageReaderException("Insuficient permissions to read file");
            }
            
            BufferedImage bi;
            try {
                bi = ImageIO.read(f);
            } catch (IOException e) {
                throw new ImageReaderException("Failed to read file: " + f.getAbsolutePath(), e);
            }
            
            maskImage = new Image(bi, f.getAbsolutePath());
            List<IImage> masks = new ArrayList<IImage>();
            masks.add(maskImage);
        }        
    }

    @Override
    public void compute() {
        IterationStepTiles stepTiles = getInputParameters(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM);
        float uBuffer[] = stepTiles.getUBuffer();
        float vBuffer[] = stepTiles.getVBuffer();
        
        if (maskFilename != null && !maskFilename.isEmpty() && uBuffer != null && uBuffer.length > 0 && vBuffer != null && vBuffer.length > 0) {
            short imageOffsetJ = stepTiles.getMarginLeft();
            short imageOffsetI = stepTiles.getMarginTop();
            for (int i = 0; i < stepTiles.getDenseHeight(); i++) {
                for (int j = 0; j < stepTiles.getDenseWidth(); j++) {                    
                    if (maskImage.readPixel(i + imageOffsetI, j + imageOffsetJ) == 0.0f) {
                        uBuffer[i * stepTiles.getDenseWidth() + j] = 0.0f;
                        vBuffer[i * stepTiles.getDenseWidth() + j] = 0.0f;
                    }                
                }
            }
        }
        setJobResult(JobResultEnum.JOB_RESULT_CROSS_MAXIMUM_MASKED, stepTiles);
    }

    @Override
    public void dispose() {
        
    }

}
