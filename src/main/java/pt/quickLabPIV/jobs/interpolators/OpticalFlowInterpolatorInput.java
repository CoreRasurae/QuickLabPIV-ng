package pt.quickLabPIV.jobs.interpolators;

import pt.quickLabPIV.images.IImage;

public class OpticalFlowInterpolatorInput {
    public IImage imageA;
    public IImage imageB;
    public float[] us;
    public float[] vs;
    public boolean halfPixelOffset;
    public Object options;
}
