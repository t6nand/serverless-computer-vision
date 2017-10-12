package main.java.core.featuredetection.dataSorting;

import main.java.core.featuredetection.datapojo.FeatureOfInterest;

import java.io.Serializable;
import java.util.Comparator;

public class SortByScore implements Comparator<FeatureOfInterest>, Serializable {

    @Override
    public int compare(FeatureOfInterest o1, FeatureOfInterest o2) {
        // Return in descending order
        return Double.compare(o2.getFeatureScore(), o1.getFeatureScore());
    }
}
