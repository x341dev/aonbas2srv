package dev.x341.aonbas2srv.util;

public class AOBConstants {
    private AOBConstants() {}
    public static final int DEFAULT_PORT = 8080;
    public static final String NAME = "aonbas2srv";
    public static final String CREATOR = "x341dev";

    //Version
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 2;
    public static final int VERSION_BUILD = 0;

    public static String getFullVersion() {
        return VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_BUILD;
    }
}
