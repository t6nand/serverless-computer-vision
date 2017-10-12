package main.java.core.featuredetection;

import org.opencv.core.Scalar;
import org.opencv.core.Size;

/**
 * @author SaurabhKhanduja
 */
public enum ClassifierTechnique {
    HAAR_FRONTAL_FACE("Frontal Face(Haar)",
            "src/main/java/resources/facedetectionclassifiers/haarcascade_frontalface_alt_tree.xml",
            "_haar-frontal-default", new Size(20, 20), new Scalar(0, 0, 255)),

    HAAR_PROFILE_FACE("Profile Face(Haar)",
            "src/main/java/resources/facedetectionclassifiers/haarcascade_profileface.xml",
            "_haar-profile-default", new Size(20, 20), new Scalar(255, 255, 0)),

    LBP_FACE_VISIONARY("Face(visionary)",
            "src/main/java/resources/facedetectionclassifiers/visionary_FACES_01_LBP_5k_7k_50x50.xml",
            "_haar-face-vis", new Size(50, 50), new Scalar(0, 255, 255));

    String techniqueFriendlyName;

    String resourceUrl;

    String fileFriendlyName;

    Size minimumObjectSize;

    Scalar color;

    ClassifierTechnique(String techniqueFriendlyName, String resourceUrl, String fileFriendlyName,
                        Size minimumObjectSize, Scalar color) {
        this.techniqueFriendlyName = techniqueFriendlyName;
        this.resourceUrl = resourceUrl;
        this.fileFriendlyName = fileFriendlyName;
        this.minimumObjectSize = minimumObjectSize;
        this.color = color;
    }

    public String getTechniqueFriendlyName() {
        return techniqueFriendlyName;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public String getFileFriendlyName() {
        return fileFriendlyName;
    }

    public Size getMinimumObjectSize() {
        return minimumObjectSize;
    }
}
