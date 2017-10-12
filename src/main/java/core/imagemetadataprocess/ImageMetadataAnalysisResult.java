package main.java.core.imagemetadataprocess;

import main.java.core.featuredetection.datapojo.FeatureOfInterest;

import java.awt.*;

/**
 * This class stores output of Image Metadata Analysis.
 * <p>
 * Created by Saurabh Khanduja on 07/04/16.
 */
public class ImageMetadataAnalysisResult {

    /**
     * This flag tells whether image has a subject area.
     * Subject Area tag tells where face was if the capturing device has the support of face detection, like in Apple
     * Iphone.
     */
    private boolean subjectAreaPresent;

    /**
     * The ROI location.
     */
    private Rectangle subjectAreaRect;

    /**
     * The Image Size
     */
    private Dimension imageDimension;

    /**
     * Feature Type - Face or any other object.
     */
    private FeatureOfInterest.FeatureType featureType;

    /**
     * True if image has facebook metadata.
     */
    private boolean isFacebookPlagiarized;

    /**
     * True if image has Ducky metadata.
     */
    private boolean isDuckyPlagiarized;

    public ImageMetadataAnalysisResult() {
        subjectAreaPresent = false;
        isFacebookPlagiarized = false;
        isDuckyPlagiarized = false;
    }

    public boolean isSubjectAreaPresent() {
        return subjectAreaPresent;
    }

    public void setSubjectAreaPresent(boolean subjectAreaPresent) {
        this.subjectAreaPresent = subjectAreaPresent;
    }

    public Rectangle getSubjectAreaRect() {
        return subjectAreaRect;
    }

    public void setSubjectAreaRect(Rectangle subjectAreaRect) {
        this.subjectAreaRect = subjectAreaRect;
    }

    public Dimension getImageDimension() {
        return imageDimension;
    }

    public void setImageDimension(Dimension imageDimension) {
        this.imageDimension = imageDimension;
    }

    public FeatureOfInterest.FeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureOfInterest.FeatureType featureType) {
        this.featureType = featureType;
    }

    public boolean isFacebookPlagiarized() {
        return isFacebookPlagiarized;
    }

    public void setFacebookPlagiarized(boolean facebookPlagiarized) {
        isFacebookPlagiarized = facebookPlagiarized;
    }

    public boolean isDuckyPlagiarized() {
        return isDuckyPlagiarized;
    }

    public void setDuckyPlagiarized(boolean duckyPlagiarized) {
        isDuckyPlagiarized = duckyPlagiarized;
    }

    public Byte getUocScore() {
        return (byte) ((isFacebookPlagiarized || isDuckyPlagiarized) ? 0 : 100);
    }
}
