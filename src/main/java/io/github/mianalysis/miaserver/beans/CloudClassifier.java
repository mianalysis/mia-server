package io.github.mianalysis.miaserver.beans;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.Instances;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CloudClassifier {
    private AbstractClassifier classifier = null;
    private Instances dataset = null;
    private ArrayList<String> classes = null;
    private ArrayList<Attribute> attributes = null;

    public void initialiseClassifier(AbstractClassifier classifier, Instances dataset, ArrayList<String> classes,
            ArrayList<Attribute> attributes) {
        this.classifier = classifier;
        this.dataset = dataset;
        this.classes = classes;
        this.attributes = attributes;
    }

    public AbstractClassifier getClassifier() {
        return classifier;
    }

    public Instances getDataset() {
        return dataset;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    @PostConstruct
    public void init() {
        // Invoked after dependencies injected
    }

    @PreDestroy
    public void destroy() {
        // Invoked when the WebSocket session ends
    }

}
