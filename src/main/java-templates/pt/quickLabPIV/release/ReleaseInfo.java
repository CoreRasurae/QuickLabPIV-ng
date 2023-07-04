package pt.quickLabPIV.release;

public class ReleaseInfo {
    public final static String BUILD_VERSION   = "${project.version}";
    public final static String BUILD_NUMBER = "${buildNumber.value}";
    public final static String BUILD_TIMESTAMP = "${buildNumber.timestamp}";
}
