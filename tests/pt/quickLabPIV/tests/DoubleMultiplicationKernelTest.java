package pt.quickLabPIV.tests;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.internal.kernel.KernelManager;

public class DoubleMultiplicationKernelTest extends Kernel {
    @Local double in1[];
    @Local double in2[];
    double result[];
    double in1Temp[];
    double in2Temp[];

    public DoubleMultiplicationKernelTest(double[] in1Temp, double[] in2Temp, double[] result) {
        this.in1Temp = in1Temp;
        this.in2Temp = in2Temp;
        this.result = result;
        in1 = new double[in1Temp.length];
        in2 = new double[in1Temp.length];
    }

    @Override
    public void run() {
        int i = getGlobalId();
        int numPasses = getPassId();
        in1[i] = in1Temp[i];
        in2[i] = in2Temp[i];

        result[i] = in1[i] * in2[i];
    }
    
    public static void main(String[] args) {
        double[] array1 = new double[10];
        double[] array2 = new double[10];
        double[] result = new double[10];
        
        for (int i = 0; i < 10; i++) {
            array1[i] = i;
            array2[i] = i + 1;
        }
        
        Kernel kernel = new DoubleMultiplicationKernelTest(array1, array2, result);
        Range range = KernelManager.instance().bestDevice().createRange(10, 10);
        
        kernel.execute(range);
        
        for (int i = 0; i < 10; i++) {
            System.out.println("Result i: " + i + " = " + result[i]);
        }
    }
}
