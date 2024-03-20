// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.business.transfer;

import pt.quickLabPIV.exceptions.UIException;

public class PIVCompletionStatus {
    private boolean completed = false;
    private UIException ex = null;
    
    public void setException(UIException _ex) {
        ex = _ex;
    }
    
    public void setCompleted(boolean _completed) {
        completed = _completed;
    }

    public UIException getException() {
        return ex;
    }
    
    public boolean getCompleted() {
        return completed;
    }
}
