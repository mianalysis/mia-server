package io.github.mianalysis.miaserver.modules;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.filter.AbstractObjectFilter;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.utils.ProcessResult;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SelectObjects extends AbstractObjectFilter {

    public static final String SELECTION_SEPARATOR = "Object selection controls";
    public static final String SELECTION_MODE = "Selection mode";

    public interface SelectionModes {
        String SINGLE = "Single";
        String MULTIPLE_TOGGLE = "Multiple (toggle)";

        String[] ALL = new String[] { SINGLE, MULTIPLE_TOGGLE };

    }

    public interface Measurements {
        String SELECTED = "SELECTED";

        String[] ALL = new String[] { SELECTED };

    }

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

    public static JSONObject getObjectsJSON(Objs inputObjects, String selectionMode)
            throws InterruptedException {
        JSONObject regionsJSON = new JSONObject();

        JSONArray regionsJSONArray = new JSONArray();
        for (Obj inputObject : inputObjects.values()) {
            JSONObject regionJSON = new JSONObject();

            Polygon polygon = inputObject.getRoi(0).getPolygon();

            regionJSON.put("x", polygon.xpoints);
            regionJSON.put("y", polygon.ypoints);
            regionJSON.put("n", polygon.npoints);

            regionsJSONArray.put(regionJSON);

        }

        regionsJSON.put("regions", regionsJSONArray);

        return regionsJSON;

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
        String selectionMode = parameters.getValue(SELECTION_MODE, workspace);

        // Getting input objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Initialising all measurements
        for (Obj inputObject : inputObjects.values())
            if (inputObject.getMeasurement(Measurements.SELECTED) == null)
                inputObject.addMeasurement(new Measurement(Measurements.SELECTED, 0));

        try {
            JSONObject selectableJSON = getObjectsJSON(inputObjects, selectionMode);
            ProcessResult.getInstance().put("objects", selectableJSON);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        // boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);
        // Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects)
        // : null;

        // int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(idsString,
        // true, inputObjects.getLargestID());
        // List<Integer> idsList =
        // Arrays.stream(ids).boxed().collect(Collectors.toList());

        // int count = 0;
        // Iterator<Obj> iterator = inputObjects.values().iterator();
        // while (iterator.hasNext()) {
        // Obj inputObject = iterator.next();

        // // Checking the main filter
        // boolean conditionMet = false;
        // for (int testID:idsList) {
        // conditionMet = testFilter(String.valueOf(inputObject.getID()),
        // String.valueOf(testID), filterMethod);
        // if (conditionMet)
        // break;
        // }

        // // Adding measurements
        // if (storeIndividual) {
        // String measurementName = getIndividualFullName(filterMethod, "ID", "selected
        // IDs");
        // inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1
        // : 0));
        // }

        // if (conditionMet) {
        // count++;
        // if (remove)
        // processRemoval(inputObject, outputObjects, iterator);
        // }
        // }

        // // If moving objects, add them to the workspace
        // if (moveObjects)
        // workspace.addObjects(outputObjects);

        // // If storing the result, create a new metadata item for it
        // if (storeSummary) {
        // String measurementName = getSummaryFullName(inputObjectsName, filterMethod,
        // "ID", "selected IDs");
        // workspace.getMetadata().put(measurementName, count);
        // }

        // Showing objects
        if (showOutput) {
            inputObjects.convertToImageIDColours().show(false);
            // if (moveObjects && outputObjects != null)
            // outputObjects.convertToImageIDColours().show(false);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(SELECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(SELECTION_MODE, this, SelectionModes.MULTIPLE_TOGGLE, SelectionModes.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(SELECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SELECTION_MODE));

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

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.SELECTED);
        ref.setObjectsName(parameters.getValue(INPUT_OBJECTS, workspace));

        if (parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.MOVE_FILTERED))
            ref.setObjectsName(parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace));

        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
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