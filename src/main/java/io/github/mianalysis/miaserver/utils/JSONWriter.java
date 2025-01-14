package io.github.mianalysis.miaserver.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;
import io.github.mianalysis.miaserver.parameters.ClickListenerP;

public class JSONWriter {
    private static GUISeparator loadSeparator;

    public static JSONObject getWorkflowsJSON(Collection<File> workflows) {
        JSONObject json = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        // TreeMap<String,File> sortedList = new TreeMap<>();
        // for (File workflowFile:workflows)
        // sortedList.put(workflowFile.getName(),workflowFile);

        for (File workflowFile : new TreeSet<>(workflows))
            jsonArray.put(getWorkflowJSON(workflowFile));

        json.put("workflows", jsonArray);

        return json;

    }

    public static JSONObject getWorkflowJSON(File workflowFile) {
        JSONObject jsonObject = new JSONObject();

        String fullName = workflowFile.getName();
        if (fullName.endsWith(".mia"))
            fullName = fullName.replace(".mia", "");
        jsonObject.put("fullname", fullName);

        String displayName = workflowFile.getName().replace("$Q", "?").replace("_", " ");
        if (displayName.endsWith(".mia"))
            displayName = displayName.replace(".mia", "");
        jsonObject.put("displayname", displayName);
        jsonObject.put("thumbnail", getThumbnailPNGString(workflowFile));

        return jsonObject;

    }

    public static String getThumbnailPNGString(File workflowFile) {
        String thumbnailName = workflowFile.getParentFile().getParent() + "/thumbnails/"
                + FilenameUtils.getBaseName(workflowFile.getName()) + ".png";
        ImagePlus ipl = IJ.openImage(thumbnailName);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // ipr.setLut(LUT.createLutFromColor(Color.WHITE));
            ImageIO.write(ipl.getBufferedImage(), "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.toByteArray());

    }

    public static JSONObject getModulesJSON(Modules modules, Workspace workspace) {
        loadSeparator = new GUISeparator(modules);
        JSONObject json = new JSONObject();

        AnalysisTester.testModule(modules.getInputControl(), modules);
        AnalysisTester.testModule(modules.getOutputControl(), modules);
        AnalysisTester.testModules(modules, workspace, null);

        // Check if there are no controls to be displayed
        if (!modules.hasVisibleParameters() && !hasClickListener(modules))
            return json;

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(getModuleJSON(loadSeparator));

        // Only modules below an expanded GUISeparator should be displayed
        boolean expanded = ((BooleanP) loadSeparator.getParameter(GUISeparator.EXPANDED_PROCESSING)).isSelected();

        // Adding input control options
        if (expanded)
            jsonArray.put(getModuleJSON(modules.getInputControl()));

        // Adding module buttons
        GUISeparator separator = loadSeparator;
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then
            // return
            if (module instanceof GUISeparator) {
                separator = (GUISeparator) module;

                // If not runnable, don't show this separator (e.g. if WorkflowHandling is
                // skipping this separator)
                if (!separator.isRunnable())
                    continue;

                // Not all GUI separators are shown on the processing view panel
                BooleanP showProcessing = module.getParameter(GUISeparator.SHOW_PROCESSING);
                if (!showProcessing.isSelected())
                    continue;

                // If this separator doesn't control any visible modules, skip it
                if (((GUISeparator) module).getProcessingViewModules().size() == 0 & !module.canBeDisabled())
                    continue;

                expanded = ((BooleanP) module.getParameter(GUISeparator.EXPANDED_PROCESSING)).isSelected();
                jsonArray.put(getModuleJSON(module));

            } else {
                if (separator.isEnabled() && module.isRunnable() || module.invalidParameterIsVisible())
                    if (module.hasVisibleParameters() || module.canBeDisabled() || hasClickListener(module))
                        jsonArray.put(getModuleJSON(module));

            }
        }

        OutputControl outputControl = modules.getOutputControl();
        if (expanded && (outputControl.hasVisibleParameters() || outputControl.canBeDisabled()))
            jsonArray.put(getModuleJSON(outputControl));

        json.put("modules", jsonArray);

        return json;

    }

    public static boolean hasClickListener(Modules modules) {
        for (Module module : modules)
            for (Parameter parameter : module.updateAndGetParameters().values())
                if (parameter instanceof ClickListenerP)
                    return true;

        return false;

    }

    public static boolean hasClickListener(Module module) {
        for (Parameter parameter : module.updateAndGetParameters().values())
            if (parameter instanceof ClickListenerP)
                return true;

        return false;

    }

    public static JSONObject getModuleJSON(Module module) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", module.getModuleID());
        jsonObject.put("name", module.getName());
        jsonObject.put("nickname", module.getNickname());
        jsonObject.put("canBeDisabled", module.canBeDisabled());
        jsonObject.put("enabled", module.isEnabled());
        jsonObject.put("visibleTitle", module.canShowProcessingTitle());

        JSONArray jsonArray = getParameters(module.updateAndGetParameters(), null, null);
        jsonObject.put("parameters", jsonArray);

        if (module instanceof GUISeparator)
            jsonObject.put("expanded", module.getParameterValue(GUISeparator.EXPANDED_PROCESSING, null).toString());

        return jsonObject;

    }

    public static JSONArray getParameters(Parameters parameters, @Nullable Parameter parentGroup,
            @Nullable Integer groupCollectionNumber) {
        JSONArray jsonArray = new JSONArray();
        for (Parameter parameter : parameters.values())
            if (parameter.isVisible() || parameter instanceof ParameterGroup || parameter instanceof ClickListenerP)
                jsonArray.put(getParameterJSON(parameter, parentGroup, groupCollectionNumber));

        return jsonArray;

    }

    public static JSONObject getParameterJSON(Parameter parameter, @Nullable Parameter parentGroup,
            @Nullable Integer groupCollectionNumber) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("moduleid", parameter.getModule().getModuleID());
        jsonObject.put("name", parameter.getName());
        jsonObject.put("nickname", parameter.getNickname());
        jsonObject.put("value", parameter.getRawStringValue());
        jsonObject.put("type", parameter.getClass().getSimpleName());
        jsonObject.put("visible", parameter.isVisible());
        if (parentGroup == null) {
            jsonObject.put("parentGroupName", "");
            jsonObject.put("groupCollectionNumber", 0);
        } else {
            jsonObject.put("parentGroupName", parentGroup.getName());
            jsonObject.put("groupCollectionNumber", groupCollectionNumber);
        }

        if (parameter instanceof ChoiceType) {
            JSONArray choices = new JSONArray();
            for (String choice : ((ChoiceType) parameter).getChoices())
                choices.put(choice);

            jsonObject.put("choices", choices);

        }

        if (parameter instanceof ParameterGroup) {
            JSONArray jsonArrayCollections = new JSONArray();
            LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) parameter).getCollections(true);
            for (int collectionNumber : collections.keySet()) {
                Parameters collection = collections.get(collectionNumber);
                JSONArray jsonArrayParameters = getParameters(collection, parameter, collectionNumber);
                for (Object jsonObjectParameters : jsonArrayParameters)
                    jsonArrayCollections.put(jsonObjectParameters);
            }

            jsonObject.put("collections", jsonArrayCollections);

        }

        return jsonObject;

    }
}
