package io.github.mianalysis.miaserver.modules;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AddLabels;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.LabelFactory;
import io.github.mianalysis.miaserver.utils.ProcessResult;

public class DisplayText extends AddLabels {
    private static final String INPUT_IMAGE = "Input image";
    private static final String OUTPUT_SEPARATOR = "Image output";
    private static final String APPLY_TO_INPUT = "Apply to input image";
    private static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    private static final String OUTPUT_IMAGE = "Output image";
    private static final String EXECUTION_SEPARATOR = "Execution controls";
    private static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public DisplayText(Modules modules) {
        super("Display text", modules);
    }

    @Override
    public Category getCategory() {
        return io.github.mianalysis.miaserver.ServerCategories.SCHOOLS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image with each object represented by a text label.  The label can include information such as measurements, associated object counts or ID numbers.";
    }

    public static JSONObject getOverlayJSON(Objs inputObjects, HashMap<Integer, String> labels, int labelSize,
            HashMap<Integer, Color> colours, String labelPosition, int xOffset, int yOffset, boolean centreText,
            @Nullable int[] positions, @Nullable String[] measurementNames) throws InterruptedException {
        JSONObject overlayJSON = new JSONObject();

        JSONArray regionsJSONArray = new JSONArray();
        for (Obj inputObject : inputObjects.values()) {
            JSONObject objectOverlayJSON = new JSONObject();

            Color colour = colours.get(inputObject.getID());

            String hex = String.format("#%02x%02x%02x%02x", colour.getRed(), colour.getGreen(), colour.getBlue(),
                    colour.getAlpha());

            String label = labels == null ? "" : labels.get(inputObject.getID());
            double[] location = getLocation(inputObject, labelPosition, xOffset, yOffset, positions, measurementNames);

            objectOverlayJSON.put("labelsize", labelSize);
            objectOverlayJSON.put("x", location[0]);
            objectOverlayJSON.put("y", location[1]);
            objectOverlayJSON.put("fillcolour", hex);
            objectOverlayJSON.put("strokecolour", hex);
            objectOverlayJSON.put("text", label);

            regionsJSONArray.put(objectOverlayJSON);

        }

        overlayJSON.put("labels", regionsJSONArray);

        return overlayJSON;

    }

    @Override
    public Status process(Workspace workspace) {
        String objectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Objs inputObjects = workspace.getObjects(objectsName);

        // Getting label settings
        String labelMode = parameters.getValue(LABEL_MODE, workspace);
        int labelSize = parameters.getValue(LABEL_SIZE, workspace);
        int xOffset = parameters.getValue(X_OFFSET, workspace);
        int yOffset = parameters.getValue(Y_OFFSET, workspace);
        boolean centreText = parameters.getValue(CENTRE_TEXT, workspace);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES, workspace);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC, workspace);
        String childObjectsForLabelName = parameters.getValue(CHILD_OBJECTS_FOR_LABEL, workspace);
        String parentObjectsForLabelName = parameters.getValue(PARENT_OBJECT_FOR_LABEL, workspace);
        String partnerObjectsForLabelName = parameters.getValue(PARTNER_OBJECTS_FOR_LABEL, workspace);
        String measurementForLabel = parameters.getValue(MEASUREMENT_FOR_LABEL, workspace);
        String objectMetadataForLabel = parameters.getValue(OBJECT_METADATA_FOR_LABEL, workspace);
        String prefix = parameters.getValue(PREFIX, workspace);
        String suffix = parameters.getValue(SUFFIX, workspace);
        String labelPosition = parameters.getValue(LABEL_POSITION, workspace);
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT, workspace);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT, workspace);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT, workspace);
        int xPos = parameters.getValue(X_POSITION, workspace);
        int yPos = parameters.getValue(Y_POSITION, workspace);
        int zPos = parameters.getValue(Z_POSITION, workspace);

        String[] measurementNames = new String[] { xPosMeas, yPosMeas, zPosMeas };
        int[] positions = new int[] { xPos, yPos, zPos };

        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);
        DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces, useScientific);
        HashMap<Integer, String> labels = getLabels(inputObjects, labelMode, df, childObjectsForLabelName,
                parentObjectsForLabelName, partnerObjectsForLabelName, measurementForLabel, objectMetadataForLabel);
        appendPrefixSuffix(labels, prefix, suffix);

        try {
            ProcessResult processResult = ProcessResult.getInstance();
            JSONObject overlayJSON = getOverlayJSON(inputObjects, labels, labelSize, colours, labelPosition, xOffset,
                    yOffset, centreText, positions, measurementNames);
            
            if (!processResult.has("overlays"))
                processResult.put("overlays", new JSONArray());

            ((JSONArray) processResult.get("overlays")).put(overlayJSON);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (showOutput)
            inputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        // Returning irrelevant parameters
        returnedParameters.remove(INPUT_IMAGE);
        returnedParameters.remove(OUTPUT_SEPARATOR);
        returnedParameters.remove(APPLY_TO_INPUT);
        returnedParameters.remove(ADD_OUTPUT_TO_WORKSPACE);
        returnedParameters.remove(OUTPUT_IMAGE);
        returnedParameters.remove(EXECUTION_SEPARATOR);
        returnedParameters.remove(ENABLE_MULTITHREADING);

        return returnedParameters;

    }
}
