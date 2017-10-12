package main.java.core.imageprocess;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import main.java.utils.BashUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tapansharma on 12/10/17.
 */
public class ImageProcessor {

    /**
     * This method validates if the image file exists has valid content.
     *
     * @param filePath The path to the file.
     * @return true if image is valid.
     */
    public static boolean isImageFileValid(@NotNull String filePath) {
        File file = new File(filePath);
        boolean isValid = true;

        // Check if file exists and doesn't have 0 memory size.
        if (file.exists() && file.length() > 0) {

            // Check the resolution of image. The right way to know if a file is valid image file is to load it.
            String size = getSize(filePath);
            if (size == null || size.equals("0x0")) {
                System.out.println("IS_IMAGE_FILE_VALID: Image resolution is " + size + " for " + filePath);
                isValid = false;
            }
        } else {
            System.out.println("IS_IMAGE_FILE_VALID: Image file size is 0 or it doesn't exists for " + filePath);
            isValid = false;
        }
        return isValid;
    }


    /**
     * Returns image size as a String in format '<width>x<height>'.
     *
     * @param imagePath The url or local path of the image.
     * @return Dimension of image, null if failure happens
     */
    @Nullable
    public static String getSize(@NotNull String imagePath) {
        // In case of GIF, output is like 640x640640x640640x640640x640
        // The comma at the end formats output as 640x640,640x640,640x640,640x640
        // Assumption made: All frames have same width and height. ToDo: Need proof.
        // For Jpeg/PNG, output is "720x718,"
        String command = "identify -format '%wx%h,' " + imagePath;
        String size = null;
        try {
            size = BashUtils.runBashCommand(command);
        } catch (Exception e) {
            System.out.println("Image resolution fetch command failed: " + e);
        }

        if (size != null) {
            int firstCommaIndex = size.indexOf(',');
            if (firstCommaIndex == -1) {
                size = null;
            } else {
                // Use wxh before first comma
                size = size.substring(0, firstCommaIndex);
            }
        }

        return size;
    }

    /**
     * Note:- Auto-Orientation is slightly lossy process and heavy when Jpeg dimension is not a multiple of 8x8.
     * For very large image, autoOrientation might be very slow due to non row-major order operation.
     *
     * @param orientationValue Image Orientation Value.
     * @return true if Image does not have default orientation.
     */
    public static boolean shouldAutoOrientImage(int orientationValue) {
        boolean shouldAutoOrient = false;
        if (orientationValue >= 2 && orientationValue <= 8) {
            shouldAutoOrient = true;
        }
        return shouldAutoOrient;
    }

    /**
     * This method is used to obtain orientation data of image by checking it in exif metadata.
     *
     * @param imagePath Path of image or url for which orientation should be found.
     * @return int value of orientation. Returns 1 by default if command fails or multiple values are fetched by
     * command.
     * @throws Exception
     */
    public static int imageOrientationValue(String imagePath) throws Exception {
        int orientation = 1; // Orientation should be set to 1 by default.
        String command = "identify -format '%[exif:orientation]' " + imagePath;
        String output = BashUtils.runBashCommand(command).trim();
        System.out.println("orientation is `{}`" + output);
        if (StringUtils.isBlank(output)) {
            System.out.println("IMAGE_ORIENTATION_VALUE: Received blank or Null value of image orientation for: {}." +
                    imagePath);
        } else {
            // Split into numbers(only positive)
            output = output.replaceAll("[^0-9]+", " ");
            // Yes, multiple values are also fetched. This is not joke.
            List<String> values = Arrays.asList(output.trim().split(" "));
            if (values.size() != 1) {
                System.out.println("unknown orientation of " + imagePath + " as `{}`" + output);
            } else {
                orientation = Integer.parseInt(values.get(0));
                if (orientation < 2 || orientation > 8) {
                    if (orientation > 8) {
                        System.out.println("Invalid orientation of " + imagePath + " as `{}`" + output);
                    }
                    orientation = 1;
                }
            }
        }
        return orientation;
    }

    public static boolean autoOrientImage(String inputImagePath, String outputImagePath) {
        String command = "convert -synchronize -auto-orient " + inputImagePath + " " + outputImagePath;
        return BashUtils.runBashCommand0(command);
    }

    @Nullable
    public static Metadata getMetadata(String imagePath) {
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(new File(imagePath));

            // Log if any directory has errors.
            boolean hasErrors = false;
            StringBuilder sb = null;
            for (Directory directory : metadata.getDirectories()) {
                if (directory.hasErrors()) {
                    if (!hasErrors) {
                        sb = new StringBuilder();
                        hasErrors = true;
                    }
                    for (String errorStr : directory.getErrors()) {
                        sb.append(errorStr);
                        sb.append(System.lineSeparator());
                    }
                }
            }
            if (hasErrors) {
                System.out.println("Errors in image: " + imagePath + " found as " + System.lineSeparator() + sb);
            }
        } catch (ImageProcessingException | IOException e) {
            System.out.println("Failed to read metadata of file: " + imagePath + " " + e);
        }
        return metadata;
    }
}
