package io.github.mianalysis.miaserver.modules;

import org.jfree.data.json.impl.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
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
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
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
public class DisplayGraph extends Module {
    public static final String GRAPH_SOURCE = "Graph source";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OBJECT_MEASUREMENT = "Object measurement";
    public static final String RANGE_MINIMUM = "Range minimum";
    public static final String RANGE_MAXIMUM = "Range maximum";
    public static final String NUMBER_OF_BINS = "Number of bins";
    public static final String GRAPH_TYPE = "Graph type";
    public static final String SHOW_DATA_LABELS = "Show data labels";
    public static final String X_LABEL = "X-axis label";
    public static final String Y_LABEL = "Y-axis label";

    public interface GraphSources {
        String CHANNEL_COMPONENTS = "Channel components";
        String IMAGE_INTENSITY_HISTOGRAM = "Image intensity histogram";
        String OBJECT_MEASUREMENTS = "Object measurements";
        String OBJECT_MEASUREMENT_HISTOGRAM = "Object measurement histogram";

        String[] ALL = new String[] { CHANNEL_COMPONENTS, IMAGE_INTENSITY_HISTOGRAM, OBJECT_MEASUREMENTS,
                OBJECT_MEASUREMENT_HISTOGRAM };

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

    public JSONObject getImageIntensityHistogramJSON() {
        JSONObject graphJSON = new JSONObject();
        graphJSON.put("source", GraphSources.IMAGE_INTENSITY_HISTOGRAM);

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

    protected String[] getObjectNames(Objs objs) {
        String[] names = new String[objs.size()];

        String rootName = objs.getName();
        int i = 0;
        for (Obj obj : objs.values())
            names[i++] = rootName + " " + obj.getID();

        return names;

    }

    public JSONObject getObjectMeasurementHistogramJSON(Objs objs, String[] measurementsName, double rangeMin,
            double rangeMax, int nBins) {
        JSONObject graphJSON = new JSONObject();

        graphJSON.put("source", GraphSources.OBJECT_MEASUREMENT_HISTOGRAM);
        graphJSON.put("data", getObjectMeasurementHistogramDataJSON(objs, measurementsName, rangeMin, rangeMax, nBins));

        return graphJSON;

    }

    protected JSONObject getObjectMeasurementHistogramDataJSON(Objs objs, String[] measurementsName, double rangeMin,
            double rangeMax, int nBins) {
        JSONObject dataJSON = new JSONObject();
        double[] binCentres = getBinCentres(rangeMin, rangeMax, nBins);

        dataJSON.put("labels", binCentres);

        JSONArray datasetJSON = new JSONArray();
        for (String measurementName : measurementsName)
            datasetJSON.add(getObjectMeasurementHistogramDatasetJSON(objs, measurementName, binCentres));

        dataJSON.put("datasets", datasetJSON);

        return dataJSON;

    }

    protected JSONObject getObjectMeasurementHistogramDatasetJSON(Objs objs, String measurementName,
            double[] binCentres) {
        JSONObject datasetJSON = new JSONObject();

        datasetJSON.put("label", measurementName);
        datasetJSON.put("borderWidth", 1);

        if (objs.size() > 0) {
            double[] data = new double[binCentres.length];
            String[] backgroundColor = new String[objs.size()];

            int i = 0;
            for (Obj obj : objs.values()) {
                Measurement measurement = obj.getMeasurement(measurementName);
                if (measurement == null) {
                    data[i] = Double.NaN;
                } else {
                    double value = measurement.getValue();
                    int closestIdx = getClosestBinIndex(value, binCentres);
                    data[closestIdx]++;
                }

                backgroundColor[i++] = "#f472b6";

            }

            datasetJSON.put("data", data);
            datasetJSON.put("backgroundColor", backgroundColor);

        }

        return datasetJSON;

    }

    protected double[] getBinCentres(double rangeMin, double rangeMax, int nBins) {
        double[] names = new double[nBins];

        double increment = (rangeMax - rangeMin) / nBins;
        for (int i = 0; i < nBins; i++)
            names[i] = rangeMin + (i * increment) + increment / 2;

        return names;

    }

    protected int getClosestBinIndex(double value, double[] binCentres) {
        int closestIdx = -1;
        double closestDist = Double.MAX_VALUE;

        for (int idx = 0; idx < binCentres.length; idx++) {
            double binCentre = binCentres[idx];
            if (Math.abs(binCentre - value) < closestDist) {
                closestDist = Math.abs(binCentre - value);
                closestIdx = idx;
            }
        }

        return closestIdx;

    }

    @Override
    public Status process(Workspace workspace) {
        String graphSource = parameters.getValue(GRAPH_SOURCE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String objectMeasurementName = parameters.getValue(OBJECT_MEASUREMENT, workspace);
        double rangeMin = parameters.getValue(RANGE_MINIMUM, workspace);
        double rangeMax = parameters.getValue(RANGE_MAXIMUM, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        String graphType = parameters.getValue(GRAPH_TYPE, workspace);
        boolean showDataLabels = parameters.getValue(SHOW_DATA_LABELS, workspace);
        String xLabel = parameters.getValue(X_LABEL, workspace);
        String yLabel = parameters.getValue(Y_LABEL, workspace);

        MIA.log.writeDebug("NOTE: Should do axis formatting, etc. in Java rather than client-side");

        JSONObject graphJSON;
        switch (graphSource) {
            case GraphSources.CHANNEL_COMPONENTS:
            default:
                graphJSON = getChannelComponentsJSON();
                graphJSON.put("type", graphType.toLowerCase());
                break;
            case GraphSources.IMAGE_INTENSITY_HISTOGRAM:
                graphJSON = getImageIntensityHistogramJSON();
                graphJSON.put("type", "bar");
                graphJSON.put("xlabel", "Intensity");
                graphJSON.put("ylabel", "Frequency");
                break;
            case GraphSources.OBJECT_MEASUREMENTS:
                Objs inputObjects = workspace.getObjects(inputObjectsName);
                graphJSON = getObjectMeasurementsJSON(inputObjects, new String[] { objectMeasurementName });
                graphJSON.put("type", graphType.toLowerCase());
                switch (graphType) {
                    case GraphTypes.BAR_CHART:
                        graphJSON.put("xlabel", xLabel);
                        graphJSON.put("ylabel", yLabel);
                        break;
                }
                break;
            case GraphSources.OBJECT_MEASUREMENT_HISTOGRAM:
                inputObjects = workspace.getObjects(inputObjectsName);
                graphJSON = getObjectMeasurementHistogramJSON(inputObjects, new String[] { objectMeasurementName },
                        rangeMin, rangeMax, nBins);
                graphJSON.put("type", "bar");
                graphJSON.put("xlabel", xLabel);
                graphJSON.put("ylabel", yLabel);
                break;
        }

        graphJSON.put("showDataLabels", showDataLabels);

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
        parameters.add(new DoubleP(RANGE_MINIMUM, this, 0d));
        parameters.add(new DoubleP(RANGE_MAXIMUM, this, 100d));
        parameters.add(new IntegerP(NUMBER_OF_BINS, this, 10));
        parameters.add(new ChoiceP(GRAPH_TYPE, this, GraphTypes.BAR_CHART, GraphTypes.ALL));
        parameters.add(new BooleanP(SHOW_DATA_LABELS, this, false));
        parameters.add(new StringP(X_LABEL, this));
        parameters.add(new StringP(Y_LABEL, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(GRAPH_SOURCE));
        switch ((String) parameters.getValue(GRAPH_SOURCE, null)) {
            case GraphSources.CHANNEL_COMPONENTS:
                returnedParameters.add(parameters.getParameter(GRAPH_TYPE));
                break;

            case GraphSources.OBJECT_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_MEASUREMENT));

                ObjectMeasurementP ref = parameters.getParameter(OBJECT_MEASUREMENT);
                ref.setObjectName(parameters.getValue(INPUT_OBJECTS, null));

                returnedParameters.add(parameters.getParameter(GRAPH_TYPE));
                switch ((String) parameters.getValue(GRAPH_TYPE, null)) {
                    case GraphTypes.BAR_CHART:
                        returnedParameters.add(parameters.getParameter(X_LABEL));
                        returnedParameters.add(parameters.getParameter(Y_LABEL));
                        break;
                }

                break;

            case GraphSources.OBJECT_MEASUREMENT_HISTOGRAM:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_MEASUREMENT));

                ref = parameters.getParameter(OBJECT_MEASUREMENT);
                ref.setObjectName(parameters.getValue(INPUT_OBJECTS, null));

                returnedParameters.add(parameters.getParameter(RANGE_MINIMUM));
                returnedParameters.add(parameters.getParameter(RANGE_MAXIMUM));
                returnedParameters.add(parameters.getParameter(NUMBER_OF_BINS));
                returnedParameters.add(parameters.getParameter(X_LABEL));
                returnedParameters.add(parameters.getParameter(Y_LABEL));

                break;
        }

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