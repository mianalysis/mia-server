package io.github.mianalysis.miaserver.modules;

import org.jfree.data.json.impl.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.utils.ProcessResult;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplayGraph extends Module {
    public static final String GRAPH_SOURCE = "Graph source";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OBJECT_MEASUREMENT = "Object measurement";
    public static final String GRAPH_TYPE = "Graph type";
    public static final String SHOW_DATA_LABELS = "Show data labels";

    public interface GraphSources {
        String CHANNEL_COMPONENTS = "Channel components";
        String OBJECT_MEASUREMENTS = "Object measurements";

        String[] ALL = new String[] { CHANNEL_COMPONENTS, OBJECT_MEASUREMENTS };

    }

    public interface GraphTypes {
        String BAR_CHART = "Bar";
        String DOUGHNUT_CHART = "Doughnut";
        String PIE_CHART = "Pie";

        String[] ALL = new String[] { BAR_CHART, DOUGHNUT_CHART, PIE_CHART };

    }

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(DisplayGraph.class);

    }

    public DisplayGraph(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Display graph", modules);
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

    public JSONObject getChannelComponentsJSON() {
        JSONObject graphJSON = new JSONObject();
        graphJSON.put("source", GraphSources.CHANNEL_COMPONENTS);

        return graphJSON;

    }

    public JSONObject getObjectMeasurementsJSON(Objs objs, String[] measurementsName) {
        JSONObject graphJSON = new JSONObject();

        graphJSON.put("source", GraphSources.OBJECT_MEASUREMENTS);
        graphJSON.put("data", getObjectMeasurementsDataJSON(objs, measurementsName));

        return graphJSON;

    }

    protected JSONObject getObjectMeasurementsDataJSON(Objs objs, String[] measurementsName) {
        JSONObject dataJSON = new JSONObject();

        dataJSON.put("labels", getObjectNames(objs));

        JSONArray datasetJSON = new JSONArray();
        for (String measurementName : measurementsName)
            datasetJSON.add(getObjectMeasurementDatasetJSON(objs, measurementName));

        dataJSON.put("datasets", datasetJSON);

        return dataJSON;

    }

    protected String[] getObjectNames(Objs objs) {
        String[] names = new String[objs.size()];

        String rootName = objs.getName();
        int i = 0;
        for (Obj obj : objs.values())
            names[i++] = rootName + " " + obj.getID();

        return names;

    }

    protected JSONObject getObjectMeasurementDatasetJSON(Objs objs, String measurementName) {
        JSONObject datasetJSON = new JSONObject();

        double[] data = new double[objs.size()];
        String[] backgroundColor = new String[objs.size()];

        int i = 0;
        for (Obj obj : objs.values()) {
            Measurement measurement = obj.getMeasurement(measurementName);
            data[i] = measurement == null ? Double.NaN : measurement.getValue();
            backgroundColor[i++] = "red";
        }

        datasetJSON.put("label", measurementName);
        datasetJSON.put("borderWidth", 1);
        datasetJSON.put("data", data);
        datasetJSON.put("backgroundColor", backgroundColor);

        return datasetJSON;

    }

    @Override
    public Status process(Workspace workspace) {
        String graphSource = parameters.getValue(GRAPH_SOURCE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String objectMeasurementName = parameters.getValue(OBJECT_MEASUREMENT, workspace);
        String graphType = parameters.getValue(GRAPH_TYPE, workspace);
        boolean showDataLabels = parameters.getValue(SHOW_DATA_LABELS, workspace);

        JSONObject graphJSON;
        switch (graphSource) {
            case GraphSources.CHANNEL_COMPONENTS:
            default:
                System.out.println("Channel components");
                graphJSON = getChannelComponentsJSON();
                break;
            case GraphSources.OBJECT_MEASUREMENTS:
                System.out.println("Object measurements");
                Objs inputObjects = workspace.getObjects(inputObjectsName);
                graphJSON = getObjectMeasurementsJSON(inputObjects, new String[] { objectMeasurementName });
                break;
        }

        graphJSON.put("type", graphType.toLowerCase());
        graphJSON.put("showDataLabels",showDataLabels);

        try {
            ProcessResult.getInstance().put("graph", graphJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(GRAPH_SOURCE, this, GraphSources.CHANNEL_COMPONENTS, GraphSources.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ObjectMeasurementP(OBJECT_MEASUREMENT, this));
        parameters.add(new ChoiceP(GRAPH_TYPE, this, GraphTypes.BAR_CHART, GraphTypes.ALL));
        parameters.add(new BooleanP(SHOW_DATA_LABELS, this, false));
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(GRAPH_SOURCE));
        switch ((String) parameters.getValue(GRAPH_SOURCE, null)) {
            case GraphSources.OBJECT_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_MEASUREMENT));

                ObjectMeasurementP ref = parameters.getParameter(OBJECT_MEASUREMENT);
                ref.setObjectName(parameters.getValue(INPUT_OBJECTS, null));

                break;
        }

        returnedParameters.add(parameters.getParameter(GRAPH_TYPE));
        returnedParameters.add(parameters.getParameter(SHOW_DATA_LABELS));

        return returnedParameters;

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