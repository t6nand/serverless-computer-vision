package main.java.core.featuredetection;

import main.java.core.featuredetection.datapojo.FeatureOfInterest;
import main.java.core.featuredetection.factory.CascadeClassifierFactory;
import main.java.utils.MathUtils;
import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SaurabhKhanduja
 */
class FaceDetector {

    private CascadeClassifier haarFrontalClassifier = null;

    private CascadeClassifier haarProfileClassifier = null;

    private CascadeClassifier lbpFrontalClassifier = null;

    // private SkinDetection skinDetector;

    public FaceDetector() throws main.java.core.featuredetection.FaceDetectorException, InterruptedException {
        System.out.println("Fetching critical resource.");
        CascadeClassifierFactory factory = CascadeClassifierFactory.getInstance();
        haarFrontalClassifier = factory.borrow(ClassifierTechnique.HAAR_FRONTAL_FACE);
        haarProfileClassifier = factory.borrow(ClassifierTechnique.HAAR_PROFILE_FACE);
        lbpFrontalClassifier = factory.borrow(ClassifierTechnique.LBP_FACE_VISIONARY);
        System.out.println("Critical resources fetched.");
    }

    private List<FeatureOfInterest> detectFaceRects(Mat inputImage, ClassifierTechnique technique) throws
            main.java.core.featuredetection.FaceDetectorException {
        // An array of rectangle, this will store all the face rectangles found in the image.
        MatOfRect faceDetected = new MatOfRect();

        // Configuration for multi-scale face detection
        double scaleFactor = 1.05;

        // Don't do any Canny Pruning till speed becomes a bottle-neck
        int flags = 1;

        // Smallest face size.
        Size minFeatureSize = technique.getMinimumObjectSize();

        // No limitation on max face size.
        Size maxFeatureSize = inputImage.size();

        // Detect faces - Use 0 as minNeighbors to get all faces first.
        switch (technique) {
            case HAAR_FRONTAL_FACE:
                haarFrontalClassifier.detectMultiScale(inputImage, faceDetected, scaleFactor, 0, flags, minFeatureSize,
                        maxFeatureSize);
                break;
            case HAAR_PROFILE_FACE:
                haarProfileClassifier.detectMultiScale(inputImage, faceDetected, scaleFactor, 0, flags, minFeatureSize,
                        maxFeatureSize);
                break;
            case LBP_FACE_VISIONARY:
                lbpFrontalClassifier.detectMultiScale(inputImage, faceDetected, scaleFactor, 0, flags, minFeatureSize,
                        maxFeatureSize);
                break;
            default:
                throw new main.java.core.featuredetection.FaceDetectorException("Unsupported Face detection Technique");
        }

        // Use minNeighbors to further group rectangles together and get a score metrics.
        MatOfInt weights = new MatOfInt();
        int groupThreshold = 3;
        double groupEps = 0.2;
        Objdetect.groupRectangles(faceDetected, weights, groupThreshold, groupEps);

        // Convert List of Rectangles to List of FeatureOfInterest.
        List<FeatureOfInterest> featureOfInterests = new ArrayList<>();
        List<Rect> faceDetectedList = faceDetected.toList();
        List<Integer> faceDetectedWeight = weights.toList();
        String logMessage = "[";
        int numFaces = faceDetectedList.size();
        for (int i = 0; i < numFaces; i++) {
            featureOfInterests.add(new FeatureOfInterest(faceDetectedList.get(i), FeatureOfInterest.FeatureType.FACE,
                    faceDetectedWeight.get(i)));
            logMessage += "{" + faceDetectedList.get(i).x + ", " +
                    faceDetectedList.get(i).y + ", " +
                    faceDetectedList.get(i).width + ", " +
                    faceDetectedList.get(i).height + "}";
        }
        System.out.println(technique + ": " + logMessage + "]");

        return featureOfInterests;
    }

    /**
     * Replace this function using groupRectangles provided by Opencv.
     *
     * @param frontalFaceDetected
     * @param profileFaceDetected
     * @return
     */
    public static Map<Integer, Rect> mergeFaceRects(MatOfRect frontalFaceDetected, MatOfRect profileFaceDetected) {
        List<Rect> frontalFaceList = frontalFaceDetected.toList();
        List<Rect> profileFaceList = profileFaceDetected.toList();
        for (Rect frontalFaceRect : frontalFaceList) {
            // Check if frontalFaceRect shares a percentage of area with any rectangle of profileFaceDetected
            List<Rect> profileFacesFilteredList = new ArrayList<>();

            for (Rect profileFaceRect : profileFaceList) {
                Rect intersectionRect = MathUtils.getIntersectionRect(frontalFaceRect, profileFaceRect);
                if (intersectionRect.area() < 0.6 * frontalFaceRect.area()) {
                    profileFacesFilteredList.add(profileFaceRect);
                }
            }

            profileFaceList = profileFacesFilteredList;
        }

        Map<Integer, Rect> mergedFaceRects = new HashMap<>();
        for (Rect rect : frontalFaceList) {
            mergedFaceRects.put(1, rect);
        }
        for (Rect rect : profileFaceList) {
            mergedFaceRects.put(2, rect);
        }
        return mergedFaceRects;
    }

    /**
     * ToDo: Apply profile face detection after flipping the image, once we have a better profile face detector.
     *
     * @return
     * @throws FaceDetectorException
     */
    public List<FeatureOfInterest> detectFaces(Mat inputImage) throws FaceDetectorException {

        // ToDo: Check if image is actually colored image before applying second technique.
        int mergeTechnique = 0;
        List<FeatureOfInterest> finalFaceRects = null;
        if (mergeTechnique == 0) { // Face detected in at least 2 or more techniques should be considered a true face.
            ClassifierTechnique techniques[] = {ClassifierTechnique.HAAR_FRONTAL_FACE, ClassifierTechnique
                    .HAAR_PROFILE_FACE, ClassifierTechnique.LBP_FACE_VISIONARY};
            List<List<FeatureOfInterest>> faceRects = new ArrayList<>(techniques.length);
            int count = 0, index = 0;
            for (ClassifierTechnique technique : techniques) {
                if (index == 2 && count == 0) {
                    break;
                }
                List<FeatureOfInterest> featureOfInterests = detectFaceRects(inputImage, technique);
                faceRects.add(featureOfInterests);
                count += featureOfInterests.size();
                index++;
                finalFaceRects = MathUtils.findIntersectingRects(faceRects);
            }
        } else if (mergeTechnique == 1) {
            // ToDo: complete this logic.
            // Detect face using HAAR frontal
            List<FeatureOfInterest> frontalHaarFaceRects = detectFaceRects(inputImage, ClassifierTechnique
                    .HAAR_FRONTAL_FACE);

            // Verify each ROI using Skin Detection based on color
        }

        return finalFaceRects;
    }

    @Override
    protected void finalize() throws Throwable {
        // This notifies if there is any concurrency issue.
        if (haarFrontalClassifier != null || haarProfileClassifier != null || lbpFrontalClassifier != null) {
            System.out.println("Returning critical resources for feature detection.");
            deallocateResources();
        }
        super.finalize();
    }

    public void deallocateResources() {
        System.out.println("Returning critical resources for feature detection.");
        CascadeClassifierFactory factory = CascadeClassifierFactory.getInstance();
        if (haarFrontalClassifier != null) {
            factory.giveBack(ClassifierTechnique.HAAR_FRONTAL_FACE, haarFrontalClassifier);
            haarFrontalClassifier = null;
        }

        if (haarProfileClassifier != null) {
            factory.giveBack(ClassifierTechnique.HAAR_PROFILE_FACE, haarProfileClassifier);
            haarProfileClassifier = null;
        }

        if (lbpFrontalClassifier != null) {
            factory.giveBack(ClassifierTechnique.LBP_FACE_VISIONARY, lbpFrontalClassifier);
            lbpFrontalClassifier = null;
        }
        System.out.println("Critical resource returned.");
    }
}
