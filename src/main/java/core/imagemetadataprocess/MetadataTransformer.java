package main.java.core.imagemetadataprocess;

import core.profile.RequestProfiler;
import core.utils.BashUtils;
import exception.PeterParkerException;
import play.Logger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * This class provides method for transforming image metadata of an image.
 * <p/>
 * Created by SaurabhKhanduja on 02/09/16.
 */
public class MetadataTransformer {

    /**
     * This method takes an input image and removes all Exif metadata except its ICC profile and orientation.
     * ICC profile plays crucial role in how an image looks on a device. Removing it causes slight discoloration
     * affects.
     * <p/>
     * ToDo: Compression using ImageMagick is further possible but in Ubuntu(not mac), the -strip flag causes
     * discoloration.
     *
     * @param inputImagePath  The source input image
     * @param outputImagePath The output stripped image. If null, the input image is itself stripped.
     * @return true if metadata is stripped properly.
     */
    public static boolean stripImageMetadata(@NotNull String inputImagePath,
                                             @Nullable String outputImagePath) {
        String command;
        if (outputImagePath == null) {
            command = "mogrify -quality 100% -strip -synchronize " + inputImagePath;
        } else {
            command = "convert -quality 100% -strip -synchronize " + inputImagePath + " " + outputImagePath;
        }

        boolean stripped = true;
        try {
            RequestProfiler.startKey("STRIP_IMAGE_METADATA");
            BashUtils.runBashCommand(command);
        } catch (PeterParkerException e) {
            stripped = false;
            Logger.error("Failed to strip metadata from image: " + inputImagePath, e);
        } finally {
            RequestProfiler.endKey("STRIP_IMAGE_METADATA");
        }
        return stripped;
    }
}
