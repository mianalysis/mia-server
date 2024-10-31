package io.github.mianalysis.miaserver.modules;

import java.awt.Color;
import java.awt.Polygon;
import java.util.HashMap;

import org.jfree.data.json.impl.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AbstractOverlay;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.utils.ProcessResult;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplayObjects extends AbstractOverlay {

    public static final String INPUT_SEPARATOR = "Object input";

    public static final String INPUT_OBJECTS = "Objects to display";

    public static final String RENDERING_SEPARATOR = "Rendering controls";

    public interface ColourModes extends AbstractOverlay.ColourModes {
    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(DisplayObjects.class);

    }

    public static JSONObject getObjectsJSON(Objs inputObjects, HashMap<Integer, Color> colours) throws InterruptedException {
        JSONObject objectsJSON = new JSONObject();
        objectsJSON.put("name",inputObjects.getName());

        JSONArray objectJSONArray = new JSONArray();
        for (Obj inputObject:inputObjects.values()) {
            JSONObject objectJSON = new JSONObject();

            Polygon polygon = inputObject.getRoi(0).getPolygon();

            objectJSON.put("id", inputObject.getID());
            objectJSON.put("x", polygon.xpoints);
            objectJSON.put("y", polygon.ypoints);
            objectJSON.put("n", polygon.npoints);

            Color colour = colours.get(inputObject.getID());
            String hex = String.format("#%02x%02x%02x%02x",colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha());
            objectJSON.put("colour", hex);

            objectJSONArray.add(objectJSON);

        }

        objectsJSON.put("objects", objectJSONArray);

        return objectsJSON;

    }

    public DisplayObjects(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Display objects", modules);
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

    @Override
    public Status process(Workspace workspace) {
        String objectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Objs inputObjects = workspace.getObjects(objectsName);

        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);

        try {
            JSONObject objectsJSON = getObjectsJSON(inputObjects, colours);
            ProcessResult.getInstance().put("objects", objectsJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (showOutput)
            inputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);
        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

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