package io.github.mianalysis.miaserver.modules;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.filter.AbstractObjectFilter;
import io.github.mianalysis.mia.module.objects.filter.AbstractTextObjectFilter;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.parameters.ObjectSelectorP;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SelectObjects extends AbstractTextObjectFilter {

    public static final String OBJECT_IDS = "IDs (comma-separated)";

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(SelectObjects.class);

    }

    public SelectObjects(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Select objects", modules);
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
    public String getDescription() {
        return "";
    }

    protected static void processRemoval(Obj inputObject, @Nullable Objs outputObjects, Iterator<Obj> iterator) {
        // Getting existing relationships
        LinkedHashMap<String, Objs> children = inputObject.getChildren();
        LinkedHashMap<String, Obj> parents = inputObject.getParents(true);
        LinkedHashMap<String, Objs> partners = inputObject.getPartners();

        // Removing existing relationships
        inputObject.removeRelationships();

        if (outputObjects != null) {
            outputObjects.add(inputObject);
            inputObject.setObjectCollection(outputObjects);

            // Adding new child relationships
            for (Objs childCollection : children.values()) {
                for (Obj childObject : childCollection.values()) {
                    inputObject.addChild(childObject);
                    childObject.addParent(inputObject);
                }
            }

            // Adding new parent relationships
            for (Obj parentObject : parents.values()) {
                parentObject.addChild(inputObject);
                inputObject.addParent(parentObject);
            }

            // Adding new partner relationships
            for (Objs partnerCollection : partners.values()) {
                for (Obj partnerObject : partnerCollection.values()) {
                    inputObject.addPartner(partnerObject);
                    partnerObject.addPartner(inputObject);
                }
            }
        }
        iterator.remove();
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        String idsString = parameters.getValue(OBJECT_IDS, workspace);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS, workspace);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace);

        // Getting input objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);
        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(idsString, true, inputObjects.getLargestID());
        List<Integer> idsList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Checking the main filter
            boolean conditionMet = false;
            for (int testID:idsList) {
                conditionMet = testFilter(String.valueOf(inputObject.getID()), String.valueOf(testID), filterMethod);
                if (conditionMet)
                    break;
            }
            
            // Adding measurements
            if (storeIndividual) {
                String measurementName = getIndividualFullName(filterMethod, "ID", "selected IDs");
                inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1 : 0));
            }

            if (conditionMet) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String measurementName = getSummaryFullName(inputObjectsName, filterMethod, "ID", "selected IDs");
            workspace.getMetadata().put(measurementName, count);
        }

        // Showing objects
        if (showOutput) {
            inputObjects.convertToImageIDColours().show(false);
            if (moveObjects && outputObjects != null)
                outputObjects.convertToImageIDColours().show(false);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ObjectSelectorP(OBJECT_IDS, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(OBJECT_IDS));

        returnedParameters.addAll(super.updateAndGetMeasurementParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = super.updateAndGetObjectMeasurementRefs();

        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace)) {
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            String measurementName = getIndividualFullName(filterMethod, "ID", "selected IDs");
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

            returnedRefs.add(new ObjMeasurementRef(measurementName, inputObjectsName));
            if (parameters.getValue(FILTER_METHOD, workspace).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
                returnedRefs.add(new ObjMeasurementRef(measurementName, outputObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        Workspace workspace = null;

        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            String measurementName = getSummaryFullName(inputObjectsName, filterMethod, "ID", "selected IDs");

            returnedRefs.add(metadataRefs.getOrPut(measurementName));

        }

        return returnedRefs;

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