package pt.quickLabPIV.device;

public class DeviceManagerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6286722044332028411L;

    public DeviceManagerException() {
    }

    public DeviceManagerException(String msg) {
        super(msg);
    }

    public DeviceManagerException(Throwable cause) {
        super(cause);
    }

    public DeviceManagerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DeviceManagerException(String msg, Throwable cause, boolean arg2, boolean arg3) {
        super(msg, cause, arg2, arg3);
    }

}
