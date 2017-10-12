package main.java.core.imagemetadataprocess;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.photoshop.DuckyDirectory;
import main.java.utils.FileUtils;

import java.io.File;
import java.util.List;


/**
 * This class is for analyzing metadata in image.
 * <p/>
 * Created by SaurabhKhanduja on 07/04/16.
 */
public class MetadataAnalyzer {
    /**
     * The Exif Metadata tags to ignore.
     */
    private static String[] exifTagsToIgnore = {"Orientation", "Exif Image Height", "Exif Image Width"};

    /**
     * This function analyzes metadata and returns ImageMetadataAnalysisResult.
     * <p/>
     * It supports detecting if image is copied from FB/Instagram and checks if face detection result(not done yet) is
     * present in metadata. For images with no metadata at all, it results in setting score to 0.
     *
     * @param imagePath The local path of image or its url.
     * @param isUrl     true if path is Url.
     * @return The ImageMetadataAnalysisResult. On error returns default ImageMetadataAnalysisResult object.
     */
    public static ImageMetadataAnalysisResult analyzeImage(String imagePath, boolean isUrl) {
        ImageMetadataAnalysisResult analysisResult = null;

        File imageFile = null;
        try {
            if (isUrl) {
                imageFile = FileUtils.loadImageFromUrl(imagePath);
            } else {
                imageFile = new File(imagePath);
            }

            // Fetch all image metadata
            Metadata fileMetadata = ImageMetadataReader.readMetadata(imageFile);

            // Analyse image metadata
            analysisResult = analyzeMetadata(fileMetadata);

            // Only delete if this function downloaded the image. Else deletion is caller function responsibility.
            if (isUrl) {
                FileUtils.deleteFile(imageFile);
            }

            imageFile = null;
        } catch (Exception e) {
            System.out.println("Metadata Analysis: Exception Caught in processing image " + imagePath);
            e.printStackTrace();
        } finally {
            if (imageFile != null && isUrl) {
                FileUtils.deleteFile(imageFile);
            }

            if (analysisResult == null)
                analysisResult = new ImageMetadataAnalysisResult();
        }

        return analysisResult;
    }

    /**
     * This function analyzes metadata and returns ImageMetadataAnalysisResult.
     * <p/>
     * It supports detecting if image is copied from FB/Instagram and checks if face detection result(not done yet) is
     * present in metadata. For images with no metadata at all, it results in setting score to 0.
     *
     * @param fileMetadata The metadata of the file.
     * @return The ImageMetadataAnalysisResult. On error returns default ImageMetadataAnalysisResult object.
     */
    public static ImageMetadataAnalysisResult analyzeImage(Metadata fileMetadata) {
        ImageMetadataAnalysisResult analysisResult = null;

        try {
            // Analyse image metadata
            analysisResult = analyzeMetadata(fileMetadata);
        } catch (Exception e) {
            System.out.println("Metadata Analysis: Exception Caught in processing image " + e);
        } finally {
            if (analysisResult == null)
                analysisResult = new ImageMetadataAnalysisResult();
        }

        return analysisResult;
    }

    private static ImageMetadataAnalysisResult analyzeMetadata(Metadata metadata) {
        ImageMetadataAnalysisResult metadataAnalysisResult = new ImageMetadataAnalysisResult();

        // Don't change score when unable to read image metadata
        if (metadata == null)
            return metadataAnalysisResult;

        // Check Exif Metadata in the file - Stopped for now. Apps do some processing when user uploads image, which
        // removes relevant metadata.
        // analyzeExifMetadata(eid, metadata, metadataAnalysisResult);

        // If EXIF Metadata sets score to 0, do not early exit. Checking IPTC metadata will help us in figuring out
        // how many images are plagiarized. Check IPTC metadata in the image
        analyzeIPTCMetadata(metadata, metadataAnalysisResult);

        // Analyze Ducky Directory - Optimized for web
        analyzeDuckyMetadata(metadata, metadataAnalysisResult);

        return metadataAnalysisResult;
    }

    private static void analyzeExifMetadata(Metadata metadata, ImageMetadataAnalysisResult metadataAnalysisResult) {
        boolean anyExifMetadataFound = false;
        List<ExifDirectoryBase> exifDirectory = (List<ExifDirectoryBase>) metadata.getDirectoriesOfType
                (ExifDirectoryBase.class);

        for (ExifDirectoryBase exifIFD0Directory : exifDirectory) {
            for (Tag tag : exifIFD0Directory.getTags()) {
                anyExifMetadataFound = true;

                // Logging for analysis
                if (tag.getTagName().equals("Copyright")) {
                    System.out.println("Metadata Analysis: Copyright EXIF metadata found, value: " + tag.getDescription());
                }

                // The metadata Orientation, EXIF Image Height,Width is added just by rotating an image in windows
                // preview or mac preview, etc. Thus ignoring it.
                boolean ignoreMetadata = false;
                for (String anExifTagsToIgnore : exifTagsToIgnore) {
                    if (tag.getTagName().equals(anExifTagsToIgnore)) {
                        ignoreMetadata = true;
                        break;
                    }
                }

                if (!ignoreMetadata)
                    anyExifMetadataFound = true;
            }
        }

        if (!anyExifMetadataFound) {
            // Works on Flipkart, Amazon, Jabong
            System.out.println("Metadata Analysis: No exif metadata found, setting uocScore to 0.");

            // No metadata is probably because of social sites removing this information to reduce the image size.
            // Add count of exif Metadata in ImageMetadataAnalysisResult. Needs further analysis of which tags represent
            // original content.
            // Fails on Twitter - Metadata analyzer wont work on the social sites that preserves metadata
        }
    }

    private static void analyzeIPTCMetadata(Metadata metadata, ImageMetadataAnalysisResult metadataAnalysisResult) {
        List<IptcDirectory> iptcDirectories = (List<IptcDirectory>) metadata.getDirectoriesOfType(IptcDirectory.class);
        for (IptcDirectory iptcDirectory : iptcDirectories) {
            for (Tag tag : iptcDirectory.getTags()) {

                // Logging for analysis
                if (tag.getTagName().equals("Copyright Notice")) {
                    System.out.println("Metadata Analysis: Copyright Notice IPTC metadata found, value: " +
                            tag.getDescription());
                }

                // Check for FB and Instagram Images.
                if (tag.getTagName().equals("Special Instructions") && tag.getDescription().startsWith("FBMD")) {
                    System.out.println("Metadata Analysis: Facebook metadata found, setting uocScore to 0");
                    metadataAnalysisResult.setFacebookPlagiarized(true);
                }
            }
        }
    }

    private static void analyzeDuckyMetadata(Metadata metadata, ImageMetadataAnalysisResult metadataAnalysisResult) {
        List<DuckyDirectory> duckyDirectories =
                (List<DuckyDirectory>) metadata.getDirectoriesOfType(DuckyDirectory.class);
        if (!duckyDirectories.isEmpty()) {
            // Photoshop - Optimize for web images
            System.out.println("Metadata Analysis: Ducky Directory found, setting uocScore to 0.");
            metadataAnalysisResult.setDuckyPlagiarized(true);
        }
    }
}
