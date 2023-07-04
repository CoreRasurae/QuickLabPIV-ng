package pt.quickLabPIV;

import pt.quickLabPIV.PIVContextSingleton;
import pt.quickLabPIV.PIVInputParameters;
import pt.quickLabPIV.PIVReusableObjects;

public class PIVContextTestsSingleton extends PIVContextSingleton {
	public static void setSingleton() {
		PIVContextSingleton.singleton.set(new PIVContextTestsSingleton());
	}
	
	public void reset() {
		super.parameters = new PIVInputParameters();
		super.reusableObjects = new PIVReusableObjects();
	}
	
	public void setParameters(PIVInputParameters params) {
	    PIVContextSingleton.singleton.get().parameters = params;
	}

    public static void setSingletonIfNotSetAlready() {
        if (PIVContextSingleton.getSingleton().getClass() == PIVContextTestsSingleton.class) {
            return;
        }
        setSingleton();
    }
}
