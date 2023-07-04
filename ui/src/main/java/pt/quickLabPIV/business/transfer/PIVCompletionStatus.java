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
