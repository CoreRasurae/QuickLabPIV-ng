package pt.quickLabPIV.jobs.interpolators;

public interface IDenseLucasKanadeKernel {
    public void setKernelArgs(final float _imageA[], final float[] _imageB, final float[] _us, final float[] _vs, boolean halfPixelOffset);
}
