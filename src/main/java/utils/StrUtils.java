package main.java.utils;

import java.util.Random;

/**
 * Created by tapansharma on 12/10/17.
 */
public class StrUtils {
    private static Random rand = new Random();

    public static String getRandomHexadecimalString(int numchars) {
        if (numchars <= 0)
            return "";

        StringBuilder sb = new StringBuilder();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(rand.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

    /**
     * This method is used to append a suffix to a file path. If filename is name.ext, then output will be
     * namesuffix.ext
     * <p/>
     *
     * @param filePath   The file path
     * @param fileSuffix The suffix to add after name and before extension(dot included)
     * @return The suffix added path
     */
    public static String addSuffixToFileName(String filePath, String fileSuffix) {
        int i = filePath.contains(".") ? filePath.lastIndexOf('.') : filePath.length();
        return filePath.substring(0, i) + fileSuffix + filePath.substring(i);
    }
}
