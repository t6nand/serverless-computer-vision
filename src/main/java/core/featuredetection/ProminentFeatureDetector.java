package main.java.core.featuredetection;

import main.java.core.featuredetection.dataSorting.SortByScore;
import main.java.core.featuredetection.datapojo.FeatureOfInterest;
import main.java.utils.FileUtils;
import main.java.utils.MathUtils;
import main.java.utils.StrUtils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

import static main.java.core.constants.Constants.FACE_DETECTION_INPUT_IMAGE_SUFFIX;

public class ProminentFeatureDetector {

    private static final Size MAX_IMAGE_PROCESSING_SIZE = new Size(700, 700);

    /**
     * This function takes an Image and detect all faces in it.
     *
     * @return
     * @throws Exception
     */
    private static List<FeatureOfInterest> extractFaces(String imagePath, int maxNumberOfFaces, boolean isUrl) throws
            Exception {
        FaceDetector faceDetector = null;
        List<FeatureOfInterest> featureOfInterests;

        System.out.println("Started processing:" + imagePath);
        try {
            faceDetector = new FaceDetector(); // Fetch all required resources(classifiers)
            Mat originalMat;
            String destinationFilePath = null;
            if (isUrl) {
                // Note: This will download image to temporary folder and reorient it.
                originalMat = FileUtils.loadMatImageFromUrl(imagePath);
            } else {
                // Create a copy of this image to generate re-oriented image
                destinationFilePath = StrUtils.addSuffixToFileName(imagePath, FACE_DETECTION_INPUT_IMAGE_SUFFIX);
                FileUtils.copyFile(imagePath, destinationFilePath);
                originalMat = Imgcodecs.imread(destinationFilePath);
            }
            double scaleFactor = MathUtils.resizeToBoundingRect(originalMat, MAX_IMAGE_PROCESSING_SIZE);
            Mat rescaledMat = new Mat();
            if (scaleFactor < 1) {
                Imgproc.resize(originalMat, rescaledMat, new Size(0, 0), scaleFactor, scaleFactor, Imgproc.INTER_CUBIC);
                System.out.println("Image re-sized to: " + rescaledMat.size());
            } else {
                rescaledMat = originalMat;
                System.out.println("No resizing required.");
            }

            featureOfInterests = faceDetector.detectFaces(rescaledMat);

            // Sort in descending order using detection score
            Collections.sort(featureOfInterests, new SortByScore());
            
            // Extract top detection score faces
            if (featureOfInterests.size() > maxNumberOfFaces) {
                featureOfInterests = featureOfInterests.subList(0, maxNumberOfFaces);
            }

            // Multiply each rect with 1/scaleFactor if scaleFactor is not 1
            double inverseScaleFactor = 1.0 / scaleFactor;
            for (FeatureOfInterest featureOfInterest : featureOfInterests) {
                if (scaleFactor != 1.0) {
                    featureOfInterest.setFeatureROI(MathUtils.resizeRect(featureOfInterest.getFeatureROI(),
                            inverseScaleFactor));
                }
                // This function call adds computation for other image sizes used in Roposo
                featureOfInterest.setImageSize(originalMat.size());
            }

            // Delete copied file
            if (!isUrl)
                FileUtils.deleteFile(destinationFilePath);


            // Release resources
            faceDetector.deallocateResources();

            faceDetector = null;
        } catch (Exception e) {
            System.out.println("Exception during face detection for image url: " + imagePath + " " + e.getMessage());
            throw e;
        } finally {
            if (faceDetector != null) {
                faceDetector.deallocateResources();
            }
        }

        System.out.println("End processing:" + imagePath);

        return featureOfInterests;
    }

    /**
     * This function detects all faces in image and returns top N faces rect with highest detection score.
     *
     * @param imagePath           The image url or local image path
     * @param maxNumberOfFeatures Maximum Number of Features to return
     * @param isUrl               If the path to image is url or local path.
     * @return The rect which stores the location of face in image. Location is in reference to top-left corner of
     * the images.
     * @throws Exception
     */
    public static List<FeatureOfInterest> detectProminentFeatures(String imagePath, int maxNumberOfFeatures, boolean
            isUrl) throws Exception {
        return extractFaces(imagePath, maxNumberOfFeatures, isUrl);
    }
}
