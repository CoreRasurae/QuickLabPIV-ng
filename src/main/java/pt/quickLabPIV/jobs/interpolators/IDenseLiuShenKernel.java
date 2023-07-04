package pt.quickLabPIV.jobs.interpolators;

public interface IDenseLiuShenKernel {

    public void setKernelArgs(final float _imageLKA[], final float[] _imageLKB, final float _imageLSA[], final float[] _imageLSB,
            final float[] _us, final float[] _vs, final float[] _usNew, final float[] _vsNew, 
            final float[] _totalError, boolean halfPixelOffset);
    
}
