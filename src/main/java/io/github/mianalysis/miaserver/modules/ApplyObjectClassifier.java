package io.github.mianalysis.miaserver.modules;

import java.util.ArrayList;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.beans.ApplicationContextProvider;
import io.github.mianalysis.miaserver.beans.CloudClassifier;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ApplyObjectClassifier extends Module {
    public static String INPUT_OBJECTS = "Input objects";

    private final CloudClassifier cloudClassifier;

    public ApplyObjectClassifier(Modules modules) {
        super("Apply object classifier", modules);
        cloudClassifier = ApplicationContextProvider.getBean(CloudClassifier.class);
    }

    @Override
    public Category getCategory() {
        return ServerCategories.SCHOOLS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }
    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        // Getting objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        try {
            AbstractClassifier tree = cloudClassifier.getClassifier();
            ArrayList<Attribute> attributes = cloudClassifier.getAttributes();
            ArrayList<String> classes = cloudClassifier.getClasses();

            Instances classificationDataset = new Instances("ClassificationDataset", attributes, 0);
            classificationDataset.setClassIndex(attributes.size()-1);

            for (Obj inputObject:inputObjects.values()) {
                Instance instance = CreateObjectClassifier.createInstance(inputObject, attributes, classes, null);
                instance.setDataset(classificationDataset);

                double predictedClassIdx = tree.classifyInstance(instance);
                String predictedClass = instance.classAttribute().value((int) predictedClassIdx);
                inputObject.addMetadataItem(new ObjMetadata("PREDICTED_CLASS", predictedClass));

            }
                        
        } catch (Exception e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);
        ObjMetadataRef ref = objectMetadataRefs.getOrPut("PREDICTED_CLASS");
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
