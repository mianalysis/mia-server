package io.github.mianalysis.miaserver.modules;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.parameters.ClickListenerP;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GetLocationFromClick extends Module {

    public static final String LOCATION = "Location";

    public static final String OUTPUT_LOCATIONS = "Output locations";

    public static final String REFERENCE_IMAGE = "Reference image";

    public static final String PERSIST_LOCATIONS = "Persist locations";

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(GetLocationFromClick.class);

    }

    public GetLocationFromClick(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Get locations from click", modules);
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
        // Getting parameters
        String location = parameters.getValue(LOCATION, workspace);
        String outputLocationsName = parameters.getValue(OUTPUT_LOCATIONS, workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        boolean persistLocations = parameters.getValue(PERSIST_LOCATIONS, workspace);

        Objs outputLocations;
        if (persistLocations && workspace.getObjects(outputLocationsName) != null) {
            outputLocations = workspace.getObjects(outputLocationsName);
        } else {
            Image referenceImage = workspace.getImage(referenceImageName);
            outputLocations = new Objs(outputLocationsName, referenceImage.getImagePlus());
        }

        if (location.contains(",")) {
            String[] coords = location.split(",");
            Obj outputLocation = outputLocations.createAndAddNewObject(VolumeType.POINTLIST);
            try {
                outputLocation.add(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), 0);
            } catch (NumberFormatException | PointOutOfRangeException e) {
                MIA.log.writeError(e);
            }
        }

        workspace.addObjects(outputLocations);

        // Showing objects
        if (showOutput)
            outputLocations.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ClickListenerP(LOCATION, this));
        parameters.add(new OutputObjectsP(OUTPUT_LOCATIONS, this));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new BooleanP(PERSIST_LOCATIONS, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOCATION));
        returnedParameters.add(parameters.getParameter(OUTPUT_LOCATIONS));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
        returnedParameters.add(parameters.getParameter(PERSIST_LOCATIONS));

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