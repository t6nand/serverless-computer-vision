package main.java;

import com.drew.metadata.Metadata;
import main.java.core.featuredetection.ProminentFeatureDetector;
import main.java.core.featuredetection.datapojo.FeatureOfInterest;
import main.java.core.imagemetadataprocess.ImageMetadataAnalysisResult;
import main.java.core.imagemetadataprocess.MetadataAnalyzer;
import main.java.core.imageprocess.ImageProcessor;
import main.java.utils.BashUtils;
import main.java.utils.OSUtils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tapansharma on 06/10/17.
 */
public class Driver {

    private ImageMetadataAnalysisResult imageMetadataAnalysisResult = null;

    public void warmupBeforeServerlessExecution() {
        System.out.println("OPENCV_DRIVER: Started");
        try {
            // Attempt to load native library.
            System.out.println("Attempting to load OPENCV Native Library: ");
            String baseDirPath = (BashUtils.runBashCommand("pwd")).trim();
            System.out.println("Library Path currently set is: " + System.getProperty("java.library.path"));
            String nativeLibrarySuffix;
            switch (OSUtils.getOS()) {

                case WINDOWS:
                    nativeLibrarySuffix = ".dll";
                    break;
                case LINUX:
                    nativeLibrarySuffix = ".so";
                    break;
                case MAC:
                    nativeLibrarySuffix = ".dylib";
                    break;
                default:
                    nativeLibrarySuffix = null;
            }
            if (nativeLibrarySuffix == null) {
                System.out.println("Failed to determine OS Platform. Can't proceed....");
                System.exit(1);
            }
            System.load(baseDirPath + "/libs/" + "libopencv_java330" + nativeLibrarySuffix);
            System.out.println("OPENCV Native Library loaded.");

            // Test if loaded library is working.
            Mat mat = Imgcodecs.imread(baseDirPath + "/test.jpeg");
            System.out.println("Successfully tested loaded library. Got Matrix object for test image as " + mat);
        } catch (Exception e) {
            System.out.println("OPENCV_DRIVER: Failed to load project properties as stream. Exiting....");
            e.printStackTrace();
            System.exit(1);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("OPENCV_DRIVER: Loading Native OPENCV library Failed. Can't proceed. Exiting...");
            e.printStackTrace();
            System.exit(1);
        }

        // Warm up successful. Let's handle serverless events.
        handleFeatureDetectionEvent("");
    }

    public static void main(String[] args) {
        System.out.println("WEBP_DRIVER: Started");
        try {
            System.out.println("Attempting to load WEBP Native Library: ");
            String baseDirPath = (BashUtils.runBashCommand("pwd")).trim();
            System.out.println("Library Path: " + System.getProperty("java.library.path"));
            String nativeLibrarySuffix;
            switch (OSUtils.getOS()) {

                case WINDOWS:
                    nativeLibrarySuffix = ".dll";
                    break;
                case LINUX:
                    nativeLibrarySuffix = ".so";
                    break;
                case MAC:
                    nativeLibrarySuffix = ".dylib";
                    break;
                default:
                    nativeLibrarySuffix = null;
            }
            if (nativeLibrarySuffix == null) {
                System.out.println("Failed to determine OS Platform. Can't proceed....");
                System.exit(1);
            }
            System.load(baseDirPath + "/libs/" + "libopencv_java330" + nativeLibrarySuffix);
            System.out.println("WEBP Native Library loaded.");
            Mat mat = Imgcodecs.imread(baseDirPath + "/test.jpeg");
            System.out.println(mat);
            int channels = mat.channels();
            System.out.println("This image has " + channels + " channels.");
        } catch (Exception e) {
            System.out.println("WEBP_DRIVER: Failed to load project properties as stream. Exiting....");
            e.printStackTrace();
            System.exit(1);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("WEBP_DRIVER: Loading Native WEBP library Failed. Can't proceed. Exiting...");
            e.printStackTrace();
            System.exit(1);
        }

        Driver driver = new Driver();

        driver.handleFeatureDetectionEvent("");
    }

    private void handleFeatureDetectionEvent(String imagePath) {
        Metadata metadata = ImageProcessor.getMetadata(imagePath);
        imageMetadataAnalysisResult = MetadataAnalyzer.analyzeImage(metadata);
        detectProminentFeaturesInImage(imagePath);
    }

    private void detectProminentFeaturesInImage(String imagePath) {
        List<FeatureOfInterest> featureOfInterests;

        // If face location is not in image metadata
        // ToDo : Possible bug. If image is oriented, face detected may be used incorrectly.
        // OpenCV3 did add exif orientation support, but has not been tested.
        if (imageMetadataAnalysisResult == null || !(imageMetadataAnalysisResult.isSubjectAreaPresent() && imageMetadataAnalysisResult.getFeatureType() == FeatureOfInterest.FeatureType.FACE)) {
            // Find prominent feature(human face for now)
            try {
                featureOfInterests = ProminentFeatureDetector.detectProminentFeatures(imagePath, 1, false);
            } catch (Exception e) {
                System.out.println("FACE DETECTION FAILED....");
                e.printStackTrace();
                return;
            }
            // Log eid and imagePath - this will allow for retrieval of all images where Face detection fails.
            int numOfFaces = (featureOfInterests == null) ? 0 : featureOfInterests.size();
            System.out.println("Number of Faces found: " + numOfFaces + " imagePath: " + imagePath);
        } else {
            // ToDo: Careful of image orientation. Since, different camera will write orientation differently,
            // Auto-orient the rectangle accordingly in ImageMetadataAnalyzer.
            featureOfInterests = new ArrayList<>();
            FeatureOfInterest metadataFeatureOfInterest = new FeatureOfInterest();
            Rectangle rect = imageMetadataAnalysisResult.getSubjectAreaRect();
            metadataFeatureOfInterest.setFeatureROI(new Rect(rect.x, rect.y, rect.width, rect.height));
            metadataFeatureOfInterest.setImageSize(new Size(imageMetadataAnalysisResult.getImageDimension().width,
                    imageMetadataAnalysisResult.getImageDimension().height));
            featureOfInterests.add(metadataFeatureOfInterest);
        }

        // Write face rect to neo4j
        if (featureOfInterests != null && featureOfInterests.size() > 0) {
            Rect featureROI = featureOfInterests.get(0).getFeatureROI();
            System.out.println("Rect is: " + featureROI.x + "," + featureROI.y + "," + featureROI.width + "," + featureROI.height);
        }
    }
}