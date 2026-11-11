package io.github.mianalysis.miaserver.utils;

import java.util.ArrayList;

import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.Instances;

public class WorkflowClassifier {
    private static AbstractClassifier classifier = null;
    private static Instances dataset = null;
    private static ArrayList<String> classes = null;
    private static ArrayList<Attribute> attributes = null;

    public static void initialiseClassifier(AbstractClassifier classifier, Instances dataset, ArrayList<String> classes,
            ArrayList<Attribute> attributes) {
        WorkflowClassifier.classifier = classifier;
        WorkflowClassifier.dataset = dataset;
        WorkflowClassifier.classes = classes;
        WorkflowClassifier.attributes = attributes;
    }

    public static AbstractClassifier getClassifier() {
        return classifier;
    }

    public static Instances getDataset() {
        return dataset;
    }

    public static ArrayList<String> getClasses() {
        return classes;
    }

    public static ArrayList<Attribute> getAttributes() {
        return attributes;
    }
}
