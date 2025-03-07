package io.github.mianalysis.miaserver.modules;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ModuleP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.parameters.InputGraphP;
import io.github.mianalysis.miaserver.parameters.ParameterP;
import io.github.mianalysis.miaserver.utils.JSONWriter;
import io.github.mianalysis.miaserver.utils.ProcessResult;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CompositeMessage extends Module {

    public static final String OUTPUT_SEPARATOR = "Message";

    public static final String ADD_OUTPUT = "Add output";

    public static final String OUTPUT_TYPE = "Output type";

    public static final String GRAPH = "Graph to display";

    public static final String MODULE = "Module";

    public static final String PARAMETER = "Parameter to display";

    public static final String TEXT = "Text to display";

    public interface OutputTypes {
        String GRAPH = "Graph";
        String PARAMETER = "Parameter";
        String TEXT = "Text";

        String[] ALL = new String[] { GRAPH, PARAMETER, TEXT };

    }

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(CompositeMessage.class);

    }

    public CompositeMessage(Modules modules) {
        super("Composite mesage", modules);
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
        StringBuilder sb = new StringBuilder();

        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_OUTPUT, workspace);
        for (Parameters collection : collections.values()) {
            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.GRAPH:
                    String inputGraph = collection.getValue(GRAPH, workspace);
                    sb.append(ProcessResult.getInstance().get(inputGraph));
                    break;

                case OutputTypes.PARAMETER:
                    String selectedModuleID = collection.getValue(MODULE, workspace);
                    String selectedParameterName = collection.getValue(PARAMETER, workspace);
                    Parameter selectedParameter = modules.getModuleByID(selectedModuleID).getParameter(selectedParameterName);
                    sb.append(JSONWriter.getParameterJSON(selectedParameter, null, null));
                    
                case OutputTypes.TEXT:
                    String text = collection.getValue(TEXT, workspace);
                    sb.append(text);
                    break;
            }
        }

        System.out.println(sb.toString());

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));

        Parameters collection = new Parameters();
        collection.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.TEXT, OutputTypes.ALL));
        collection.add(new InputGraphP(GRAPH, this));
        collection.add(new ModuleP(MODULE, this, false));
        collection.add(new ParameterP(PARAMETER, this, "", ""));
        collection.add(new StringP(TEXT, this));

        parameters.add(new ParameterGroup(ADD_OUTPUT, this, collection, 1, getUpdaterAndGetter()));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(ADD_OUTPUT));

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

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(OUTPUT_TYPE));
                switch ((String) params.getValue(OUTPUT_TYPE, null)) {
                    case OutputTypes.GRAPH:
                        returnedParameters.add(params.getParameter(GRAPH));
                        break;
                    case OutputTypes.PARAMETER:
                        returnedParameters.add(params.getParameter(MODULE));
                        returnedParameters.add(params.getParameter(PARAMETER));
                        String selectedModuleID = params.getValue(MODULE, null);
                        if (!selectedModuleID.equals(""))
                            ((ParameterP) params.get(PARAMETER)).setSelectedModuleID(selectedModuleID);
                        break;
                    case OutputTypes.TEXT:
                        returnedParameters.add(params.getParameter(TEXT));
                        break;
                }

                return returnedParameters;

            }
        };
    }
}