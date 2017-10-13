package main.java.core.featuredetection.factory;

import main.java.core.featuredetection.ClassifierTechnique;
import main.java.core.featuredetection.FaceDetectorException;
import org.opencv.objdetect.CascadeClassifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author SaurabhKhanduja
 */
public class CascadeClassifierFactory {

    private static final int MAX_NUM_CLASSIFIERS = Runtime.getRuntime().availableProcessors(); // This will allow as
    // many threads as processors.

    private Map<ClassifierTechnique, BlockingDeque<CascadeClassifier>> freeCascadeClassifiers;

    private static CascadeClassifierFactory factory;

    static {
        try {
            factory = new CascadeClassifierFactory();
            System.out.println(MAX_NUM_CLASSIFIERS + " Classifiers loaded.");
        } catch (FaceDetectorException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private CascadeClassifierFactory() throws FaceDetectorException {
        freeCascadeClassifiers = new EnumMap<>(ClassifierTechnique.class);
        ClassifierTechnique[] values = ClassifierTechnique.values();

        for (ClassifierTechnique value : values) {

            freeCascadeClassifiers.put(value, new LinkedBlockingDeque<CascadeClassifier>(MAX_NUM_CLASSIFIERS));
            for (int j = 0; j < MAX_NUM_CLASSIFIERS; j++) {
                CascadeClassifier cascadeClassifier = new CascadeClassifier();
                if (!cascadeClassifier.load("resources/facedetectionclassifiers/" + value.getResourceUrl())) {
                    throw new FaceDetectorException("Unable to load Classifier file: " + value.getResourceUrl());
                }
                freeCascadeClassifiers.get(value).add(cascadeClassifier);
            }
        }
    }

    public static CascadeClassifierFactory getInstance() {
        return factory;
    }

    public CascadeClassifier borrow(ClassifierTechnique technique) throws InterruptedException {
        BlockingDeque<CascadeClassifier> classifiers = freeCascadeClassifiers.get(technique);
        return classifiers.take();
    }

    public void giveBack(ClassifierTechnique technique, CascadeClassifier cascadeClassifier) {
        freeCascadeClassifiers.get(technique).addLast(cascadeClassifier);
    }
}
