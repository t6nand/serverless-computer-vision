package main.java.core.featuredetection.datapojo;

import org.opencv.core.Rect;
import org.opencv.core.Size;

/**
 * This class will represent object detected using our algorithms.
 * A object will be represented by what class it belongs to and its ROI Rect.
 */
public class FeatureOfInterest {

    private Rect featureROI;

    private double featureScore;

    private FeatureType featureType;

    private Size imageSize;

    public FeatureOfInterest() {
    }

    public FeatureOfInterest(Rect featureROI, FeatureType featureType, double featureScore) {
        this.featureROI = featureROI;
        this.featureType = featureType;
        this.featureScore = featureScore;
    }

    public Rect getFeatureROI() {
        return featureROI;
    }

    public void setFeatureROI(Rect featureROI) {
        this.featureROI = featureROI;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
    }

    public double getFeatureScore() {
        return featureScore;
    }

    public void setFeatureScore(double featureScore) {
        this.featureScore = featureScore;
    }

    public Size getImageSize() {
        return imageSize;
    }

    public void setImageSize(Size imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String toString() {
        return "FeatureOfInterest [featureROI=" + featureROI + ", featureScore="
                + featureScore + ", featureType=" + featureType + "]";
    }

    public enum FeatureType {
        FACE
    }
}
