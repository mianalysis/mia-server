// package io.github.mianalysis.miaserver.modules;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.LinkedHashMap;

// import com.drew.lang.annotations.Nullable;

// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Obj;
// import io.github.mianalysis.mia.object.ObjMetadata;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.measurements.Measurement;
// import io.github.mianalysis.mia.object.parameters.InputObjectsP;
// import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
// import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
// import io.github.mianalysis.mia.object.parameters.ParameterGroup;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.text.StringP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import io.github.mianalysis.miaserver.ServerCategories;
// import io.github.mianalysis.miaserver.beans.ApplicationContextProvider;
// import io.github.mianalysis.miaserver.beans.CloudClassifier;
// import weka.classifiers.AbstractClassifier;
// import weka.classifiers.trees.J48;
// import weka.core.Attribute;
// import weka.core.DenseInstance;
// import weka.core.Instance;
// import weka.core.Instances;

// public class CreateObjectClassifier extends Module {
//     public static String INPUT_OBJECTS = "Input objects";
//     public static String CLASS_METADATA_ITEM = "Class metadata item";
//     public static String CLASSES = "Classes (comma sep.)";
//     public static String ADD_MEASUREMENT = "Add measurement";
//     public static String MEASUREMENT = "Measurement";

//     private final CloudClassifier cloudClassifier;

//     public CreateObjectClassifier(Modules modules) {
//         super("Create object classifier", modules);
//         cloudClassifier = ApplicationContextProvider.getBean(CloudClassifier.class);        
//     }

//     @Override
//     public Category getCategory() {
//         return ServerCategories.SCHOOLS;
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     public static ArrayList<String> getMeasurementNames(ParameterGroup parameterGroup, Workspace workspace) {
//         ArrayList<String> measurementNames = new ArrayList<>();

//         LinkedHashMap<Integer, Parameters> collections = parameterGroup.getCollections(true);
//         for (Parameters collection:collections.values())
//             measurementNames.add(((ObjectMeasurementP) collection.get(MEASUREMENT)).getValue(workspace));

//         return measurementNames;

//     }

//     public static ArrayList<Attribute> getAttributes(ArrayList<String> measurementNames, ArrayList<String> classes) {
//         ArrayList<Attribute> attributes = new ArrayList<>();

//         // Initialising instances
//         for (String measurementName : measurementNames)
//             attributes.add(new Attribute(measurementName));
        
//         attributes.add(new Attribute("CLASS", classes));

//         return attributes;

//     }

//     public static Instances createInstances(Objs inputObjects, boolean trainingMode, ArrayList<Attribute> attributes, ArrayList<String> classes, @Nullable String classMetadataItem) {
//         Instances data = new Instances("Instances", attributes, 0);

//         // Adding data
//         for (Obj inputObject : inputObjects.values()) {
//             // In training mode, only those with assigned classes are included
//             if (trainingMode && (inputObject.getMetadataItem(classMetadataItem) == null || inputObject.getMetadataItem(classMetadataItem).getValue().equals("")))
//                 continue;

//             data.add(createInstance(inputObject, attributes, classes, classMetadataItem));

//         }

//         data.setClassIndex(data.numAttributes() - 1);

//         return data;

//     }

//     public static Instance createInstance(Obj inputObject, ArrayList<Attribute> attributes, ArrayList<String> classes, @Nullable String classMetadataItem) {
//         DenseInstance instance = new DenseInstance(attributes.size());

//         // Adding measurements
//         for (int i=0;i<attributes.size()-1;i++) {
//             String name = attributes.get(i).name();
//             Measurement measurement = inputObject.getMeasurement(name);
//             if (measurement == null) {
//                 MIA.log.writeWarning("Measurement \"" + name + "\" missing for object "
//                         + inputObject.getID());
//                 instance.setMissing(i);
//             } else {
//                 instance.setValue(i, measurement.getValue());
//             }
//         }

//         // Adding class and skipping if missing (i.e. when preparing instances for classification)
//         if (classMetadataItem == null) {
//             instance.setMissing(attributes.size() - 1);
//         } else {
//             ObjMetadata classMetadata = inputObject.getMetadataItem(classMetadataItem);
//             if (classMetadata == null || classMetadata.getValue().equals(""))
//                 instance.setMissing(attributes.size() - 1);
//             else
//                 instance.setValue(attributes.size() - 1, classes.indexOf(classMetadata.getValue()));
//         }

//         return instance;

//     }

//     @Override
//     protected Status process(Workspace workspace) {
//         // Getting parameters
//         String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
//         String classMetadataItem = parameters.getValue(CLASS_METADATA_ITEM, workspace);
//         String classesString = parameters.getValue(CLASSES, workspace);
//         ParameterGroup parameterGroup = (ParameterGroup) parameters.get(ADD_MEASUREMENT);
        
//         // Getting objects
//         Objs inputObjects = workspace.getObjects(inputObjectsName);

//         // Creating dataset
//         ArrayList<String> classes = new ArrayList<>(Arrays.asList(classesString.split(",")));
//         ArrayList<String> measurementNames = getMeasurementNames(parameterGroup, workspace);
//         ArrayList<Attribute> attributes = getAttributes(measurementNames, classes);
//         Instances dataset = createInstances(inputObjects, true, attributes, classes, classMetadataItem);

//         AbstractClassifier tree;
//         try {
//             tree = new J48();
//             tree.setOptions(new String[] { "-U" });
//             tree.buildClassifier(dataset);
            
//         } catch (Exception e) {
//             MIA.log.writeError(e);
//             return Status.FAIL;
//         }

//         // Storing it for use in other modules
//         cloudClassifier.initialiseClassifier(tree, dataset, classes, attributes);

//         return Status.PASS;
//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
//         parameters.add(new ObjectMetadataP(CLASS_METADATA_ITEM, this));
//         parameters.add(new StringP(CLASSES, this));

//         Parameters templateParameters = new Parameters();
//         templateParameters.add(new ObjectMeasurementP(MEASUREMENT, this));
//         parameters.add(new ParameterGroup(ADD_MEASUREMENT, this, templateParameters));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         ((ObjectMetadataP) parameters.get(CLASS_METADATA_ITEM)).setObjectName(parameters.getValue(INPUT_OBJECTS, null));

//         String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);
//         ParameterGroup parameterGroup = (ParameterGroup) parameters.get(ADD_MEASUREMENT);
//         LinkedHashMap<Integer, Parameters> collections = parameterGroup.getCollections(true);
//         for (Parameters collection:collections.values())
//             ((ObjectMeasurementP) collection.get(MEASUREMENT)).setObjectName(inputObjectsName);

//         return parameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
