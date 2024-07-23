package io.github.mianalysis.miaserver.modules;

import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.results.Result;
import io.github.mianalysis.miaserver.utils.ProcessResult;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplayGraph extends Module implements Result {

    public static final String GRAPH_SOURCE = "Graph source";

    public interface GraphSources {
        String CHANNEL_COMPONENTS = "Channel components";

        String[] ALL = new String[] { CHANNEL_COMPONENTS };
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

    @Override
    public Status process(Workspace workspace) {
        String graphSource = parameters.getValue(GRAPH_SOURCE, workspace);

        JSONObject graphJSON = new JSONObject();
        graphJSON.put("source",graphSource);

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

    @Override
    public JSONObject getJSON() {
        throw new UnsupportedOperationException("Unimplemented method 'getJSON'");
    }
}