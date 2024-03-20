// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
