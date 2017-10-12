package main.java.utils;

import com.tinkerpop.pipes.util.structures.Pair;
import main.java.core.featuredetection.dataSorting.SortByScore;
import main.java.core.featuredetection.datapojo.FeatureOfInterest;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by tapansharma on 12/10/17.
 */
public class MathUtils {
    public static Dimension computeCropAreaToClipToFixedAspectRatio(Dimension imageResolution,
                                                                    Dimension aspectRatioDimension) {
        int w = (int) imageResolution.getWidth(), h = (int) imageResolution.getHeight();
        int aw = (int) aspectRatioDimension.getWidth(), ah = (int) aspectRatioDimension.getHeight();
        int finalHeight, finalWidth;
        if (aw * h > w * ah) { // aw / ah > w / h, final image will be wide as original image
            finalWidth = w;
            finalHeight = (w * ah) / aw;
        } else { // aw / ah <= w / h, final image will be high as original image
            finalWidth = (aw * h) / ah;
            finalHeight = h;
        }
        return new Dimension(finalWidth, finalHeight);
    }

    /**
     * Move cropped area in image towards optimalCenter as much as possible. The croppedAreaResolution should not be
     * greater than Image Resolution.
     *
     * @param imageResolution       The resolution of image.
     * @param croppedAreaResolution The resolution for area to crop.
     * @param requiredCenter        The center where the cropped area should be. Note, the cropped area may no longer lie within the image boundary if placed here.
     * @return The possible center which is as optimally close to required center as possible.
     */
    public static Dimension computeCropCenter(Dimension imageResolution,
                                              Dimension croppedAreaResolution,
                                              Dimension requiredCenter) {
        Dimension initialCenter = new Dimension((int) croppedAreaResolution.getWidth() / 2,
                (int) croppedAreaResolution.getHeight() / 2);
        // Find the degree of movement for cropped area
        double deltaEastPossible = imageResolution.getWidth() - requiredCenter.getWidth();
        double deltaSouthPossible = imageResolution.getHeight() - requiredCenter.getHeight();

        // Find distance between initialCenter and optimalCenter
        double deltaEastRequired = requiredCenter.getWidth() - initialCenter.getWidth(); // Here width and height represent x and y coordinate respectively.
        double deltaSouthRequired = requiredCenter.getHeight() - initialCenter.getHeight();

        // Find optimal movement.
        return new Dimension((int) (initialCenter.getWidth() + Math.max(0, Math.min(deltaEastRequired, deltaEastPossible))),
                (int) (initialCenter.getHeight() + Math.max(0, Math.min(deltaSouthRequired, deltaSouthPossible))));
    }

    /**
     * This class is used to set image coordinates and resolution size which can be used where ever required like making
     * grid collage of image and overlaying.
     */
    public static class ImageAndPosition {
        private String path;
        private Pair<Integer, Integer> topLeftCoOrd;
        private Pair<Integer, Integer> sizeOfContainer;

        public ImageAndPosition(String path,
                                Pair<Integer, Integer> topLeftCoOrd,
                                Pair<Integer, Integer> sizeOfContainer) {
            this.path = path;
            this.topLeftCoOrd = topLeftCoOrd;
            this.sizeOfContainer = sizeOfContainer;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Pair<Integer, Integer> getTopLeftCoOrd() {
            return topLeftCoOrd;
        }

        public void setTopLeftCoOrd(Pair<Integer, Integer> topLeftCoOrd) {
            this.topLeftCoOrd = topLeftCoOrd;
        }

        public Pair<Integer, Integer> getSizeOfContainer() {
            return sizeOfContainer;
        }

        public void setSizeOfContainer(Pair<Integer, Integer> sizeOfContainer) {
            this.sizeOfContainer = sizeOfContainer;
        }

        @Override
        public String toString() {
            return "ImageAndPosition{" +
                    "path='" + path + '\'' +
                    ", topLeftCoOrd= " + topLeftCoOrd.getA() + "x" + topLeftCoOrd.getB() +
                    ", sizeOfContainer= " + sizeOfContainer.getA() + "x" + sizeOfContainer.getB() +
                    '}';
        }
    }

    /**
     * This function check if the value is in the range of [minimum, maximum).
     * Thus allowed range is minimum to maximum - 1. If the value breaches this limit, its
     * clipped to nearest integer in the allowed range.
     *
     * @param value
     * @param minimum
     * @param maximum
     * @return
     */
    public static int clip(int value, int minimum, int maximum) {
        if (value < minimum)
            return minimum;
        if (value >= maximum)
            return maximum;
        return value;
    }

    /**
     * This function gives an intersecting rectangle between two rectangles.
     * If no intersection found, an empty rectangle is returned( area is 0).
     *
     * @param rect1
     * @param rect2
     * @return
     */
    public static Rect getIntersectionRect(Rect rect1, Rect rect2) {
        Rectangle r1 = new Rectangle(rect1.x, rect1.y, rect1.width, rect1.height);
        Rectangle r2 = new Rectangle(rect2.x, rect2.y, rect2.width, rect2.height);
        if (!r1.intersects(r2))
            return new Rect(0, 0, 0, 0);
        Rectangle intersection = r1.intersection(r2);
        return new Rect(intersection.x, intersection.y, intersection.width, intersection.height);
    }

    /**
     * This method finds if there is any rectangle in List A which has intersection with some rectangle in Rect B.
     *
     * @param rectList1
     * @param rectList2
     * @return
     */
    private static java.util.List<FeatureOfInterest> findIntersectingRects(java.util.List<FeatureOfInterest> rectList1,
                                                                           java.util.List<FeatureOfInterest> rectList2) {
        java.util.List<FeatureOfInterest> output = new ArrayList<>();

        for (FeatureOfInterest featureOfInterest1 : rectList1) {
            Rect rect1 = featureOfInterest1.getFeatureROI();
            for (FeatureOfInterest featureOfInterest2 : rectList2) {
                if (featureOfInterest1.getFeatureType() == featureOfInterest2.getFeatureType()) {
                    Rect rect2 = featureOfInterest2.getFeatureROI();
                    Rect intersectRect = getIntersectionRect(rect1, rect2);
                    if (intersectRect.area() > 0) {
                        // && intersectRect.area() > 0.5 * rect1.area() && intersectRect.area() > 0.5 * rect2.area())
                        double featureScore = (featureOfInterest1.getFeatureScore() + featureOfInterest2
                                .getFeatureScore());
                        output.add(new FeatureOfInterest(intersectRect, featureOfInterest1.getFeatureType(),
                                featureScore));
                    }
                }
            }
        }

        return output;
    }

    private static java.util.List<FeatureOfInterest> filterIntersectingRect(java.util.List<FeatureOfInterest> rects) {
        java.util.List<FeatureOfInterest> output = new ArrayList<>();
        boolean[] discardFeature = new boolean[rects.size()];
        for (int i = 0; i < discardFeature.length; i++) {
            discardFeature[i] = false;
        }

        // Sort in descending order of Score
        Collections.sort(rects, new SortByScore());

        // Discard the rectangle with lowest score
        for (int i = 0; i < rects.size() - 1; i++) {
            if (discardFeature[i]) {
                continue;
            }
            FeatureOfInterest featureOfInterest1 = rects.get(i);
            Rect rect1 = featureOfInterest1.getFeatureROI();
            for (int j = i + 1; j < rects.size(); j++) {
                if (discardFeature[j]) {
                    continue;
                }
                FeatureOfInterest featureOfInterest2 = rects.get(j);

                if (featureOfInterest1.getFeatureType() == featureOfInterest2.getFeatureType()) {
                    Rect rect2 = featureOfInterest2.getFeatureROI();
                    Rect intersectRect = getIntersectionRect(rect1, rect2);
                    if (intersectRect.area() > 0) {
                        discardFeature[j] = true;
                    }
                }
            }
        }

        for (int i = 0; i < discardFeature.length; i++) {
            if (!discardFeature[i]) {
                output.add(rects.get(i));
            }
        }

        return output;
    }

    /**
     * This method takes list(A) of list(B) of Rects. It finds if there are
     * more than 2 Rectangles common between two lists of list A.
     *
     * @param rectsLists
     * @return
     */
    public static java.util.List<FeatureOfInterest> findIntersectingRects(
            java.util.List<java.util.List<FeatureOfInterest>> rectsLists) {
        java.util.List<FeatureOfInterest> output = new ArrayList<>();

        if (rectsLists.size() >= 2) {
            // Find common intersecting rectangles between every pair of lists possible
            for (int i = 0; i < rectsLists.size() - 1; i++) {
                for (int j = i + 1; j < rectsLists.size(); j++) {
                    java.util.List<FeatureOfInterest> intersectingRects =
                            findIntersectingRects(rectsLists.get(i), rectsLists.get(j));
                    output.addAll(intersectingRects);
                }
            }
        }

        // Filter if 2 or more rectangles have common intersection and store only intersection.
        output = filterIntersectingRect(output);

        return output;
    }

    /**
     * This function resizes images if image does not fit boundingRectSize while keeping Aspect ratio same. If no
     * resizing is done, original image is returned.
     *
     * @param originalMat
     * @param boundingRectSize
     * @return
     */
    public static double resizeToBoundingRect(Mat originalMat, Size boundingRectSize) {
        double scaleFactor = 1.0;
        // ToDo: Compare number of total pixels to area of boundingRectSize, instead of below condition.
        if (originalMat.height() > boundingRectSize.height || originalMat.width() > boundingRectSize.width) {
            System.out.println("Image Size: " + originalMat.size() + " greater than bounding rect: " + boundingRectSize);
            int originalHeight = originalMat.height();
            int originalWidth = originalMat.width();
            scaleFactor = Math.min(boundingRectSize.height / originalHeight, boundingRectSize.width / originalWidth);
            System.out.println("Image resize size wil be: " + (int) (scaleFactor * originalWidth) + "x" + (int) (scaleFactor *
                    originalHeight));
        }
        return scaleFactor;
    }

    public static Rect resizeRect(Rect featureROI, double resizeFactor) {
        return new Rect((int) (featureROI.x * resizeFactor), (int) (featureROI.y * resizeFactor),
                (int) (featureROI.width * resizeFactor), (int) (featureROI.height * resizeFactor));
    }
}
