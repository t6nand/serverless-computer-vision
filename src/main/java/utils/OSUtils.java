package main.java.utils;

/**
 * Created by tapansharma on 13/10/17.
 */
public class OSUtils {
    public enum OS {
        WINDOWS,
        LINUX,
        MAC
    }

    ;// Operating systems.

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            }
        }
        return os;
    }
}
