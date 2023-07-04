package pt.quickLabPIV;

import java.util.List;

import pt.quickLabPIV.iareas.Tile;
import pt.quickLabPIV.images.IImage;
import pt.quickLabPIV.jobs.JavaImageFilterJob;
import pt.quickLabPIV.jobs.JavaNullImageFilterJob;
import pt.quickLabPIV.jobs.JavaNullTilesFilterJob;
import pt.quickLabPIV.jobs.JavaTilesFilterJob;
import pt.quickLabPIV.jobs.Job;

public enum ImageFilteringModeFactoryEnum {
    NoImageFiltering,
    ImageFilteringBeforeWarping,
    ImageFilteringAfterWarping;

    public static Job<IImage, IImage> createMainImageAFilterJob(PIVInputParameters inputParameters) {
        ImageFilteringModeFactoryEnum mode = inputParameters.getImageFilteringMode();
        //If doing warping on the first or second image only, only the corresponding filter needs to be performed here,
        //while filtering the other non-warped image as a whole at the start (first iteration only)
        WarpingModeFactoryEnum warpingMode = inputParameters.getWarpingMode();
        
        switch (mode) {
        case NoImageFiltering:
            return new JavaNullImageFilterJob();
        case ImageFilteringBeforeWarping:
            return new JavaImageFilterJob();
        case ImageFilteringAfterWarping:
            if (warpingMode.isWarpsFirstImage()) {
                return new JavaNullImageFilterJob();
            } else {
                return new JavaImageFilterJob();
            }
        default:
            throw new UnknownImageFilteringException("Unknown filtering mode: " + mode);
        }
    }

    public static Job<IImage, IImage> createMainImageBFilterJob(PIVInputParameters inputParameters) {
        ImageFilteringModeFactoryEnum mode = inputParameters.getImageFilteringMode();
        //If doing warping on the first or second image only, only the corresponding filter needs to be performed here,
        //while filtering the other non-warped image as a whole at the start (first iteration only)
        WarpingModeFactoryEnum warpingMode = inputParameters.getWarpingMode();
        
        switch (mode) {
        case NoImageFiltering:
            return new JavaNullImageFilterJob();
        case ImageFilteringBeforeWarping:
            return new JavaImageFilterJob();
        case ImageFilteringAfterWarping:
            if (warpingMode.isWarpsSecondImage()) {
                return new JavaNullImageFilterJob();
            } else {
                return new JavaImageFilterJob();
            }
        default:
            throw new UnknownImageFilteringException("Unknown filtering mode: " + mode);
        }
    }

    public static Job<List<Tile>, List<Tile>> createWarpedTilesAFilterJob(PIVInputParameters inputParameters) {
        ImageFilteringModeFactoryEnum mode = inputParameters.getImageFilteringMode();
        //If doing warping on the first or second image only, only the corresponding filter needs to be performed here,
        //while filtering the other non-warped image as a whole at the start (first iteration only)
        WarpingModeFactoryEnum warpingMode = inputParameters.getWarpingMode();
        
        switch (mode) {
        case NoImageFiltering:
            return new JavaNullTilesFilterJob();
        case ImageFilteringBeforeWarping:
            return new JavaNullTilesFilterJob();
        case ImageFilteringAfterWarping:
            if (warpingMode.isWarpsFirstImage()) {
                return new JavaTilesFilterJob();
            } else {
                return new JavaNullTilesFilterJob();
            }
        default:
            throw new UnknownImageFilteringException("Unknown filtering mode: " + mode);
        }
    }

    public static Job<List<Tile>, List<Tile>> createWarpedTilesBFilterJob(PIVInputParameters inputParameters) {
        ImageFilteringModeFactoryEnum mode = inputParameters.getImageFilteringMode();
        //If doing warping on the first or second image only, only the corresponding filter needs to be performed here,
        //while filtering the other non-warped image as a whole at the start (first iteration only)
        WarpingModeFactoryEnum warpingMode = inputParameters.getWarpingMode();
        
        switch (mode) {
        case NoImageFiltering:
            return new JavaNullTilesFilterJob();
        case ImageFilteringBeforeWarping:
            return new JavaNullTilesFilterJob();
        case ImageFilteringAfterWarping:
            if (warpingMode.isWarpsSecondImage()) {
                return new JavaTilesFilterJob();
            } else {
                return new JavaNullTilesFilterJob();
            }
        default:
            throw new UnknownImageFilteringException("Unknown filtering mode: " + mode);
        }
    }
}
