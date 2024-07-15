package com.example.demo.modules;

import java.io.File;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.beans.CloudWorkspace;

import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * A template MIA module.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)

public class AddPathsToMetadata extends GlobalVariables {
    @Autowired
	private CloudWorkspace cloudWorkspace;

    public static final String MESSAGE = "Message";

    public AddPathsToMetadata(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Add paths to metadata", modules);
        
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
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new MessageP(MESSAGE, this, "This module has no parameters", ParameterState.MESSAGE));
    }

    @Override
    public Parameters updateAndGetParameters() {
        String workflowPath = modules.getAnalysisFilename();

        File workflowFile = new File(workflowPath);
        if (!workflowFile.exists())
            return parameters;

        String workflowName = workflowFile.getName();
        if (workflowName.endsWith("mia"))
            workflowName = workflowName.substring(0, workflowName.length() - 4);
        // workspace.getMetadata().put("WorkflowName", workflowName);

        String miaFolder = workflowFile.getParentFile().getParent();
        // workspace.getMetadata().put("ImagesPath", miaFolder + "/images/");
        // workspace.getMetadata().put("ThumbnailsPath", miaFolder + "/thumbnails/");
        // workspace.getMetadata().put("WorkflowsPath", miaFolder + "/workflows/");

        globalVariables.put(new StringP("NAME",this,"WorkflowName"), workflowName);
        globalVariables.put(new StringP("NAME",this,"ImagesPath"), miaFolder + "/images/");
        globalVariables.put(new StringP("NAME",this,"ThumbnailsPath"), miaFolder + "/thumbnails/");
        globalVariables.put(new StringP("NAME",this,"WorkflowsPath"), miaFolder + "/workflows/");

        return parameters;

        // Workspace workspace = null;
        // ParameterGroup group = parameters.getParameter(ADD_NEW_VARIABLE);
        // if (group == null)
        //     return parameters;

        // LinkedHashMap<Integer, Parameters> collections = group.getCollections(false);
        // collections.put(0, parameters);

        // for (Parameters collection : collections.values()) {
        //     StringP variableName = (StringP) collection.get(VARIABLE_NAME);
        //     if (isEnabled()) {
        //         switch ((String) collection.getValue(VARIABLE_TYPE, workspace)) {
        //             case VariableTypes.BOOLEAN:
        //                 globalVariables.put(variableName, collection.getValue(VARIABLE_BOOLEAN, workspace).toString());
        //                 break;
        //             case VariableTypes.CHOICE:
        //                 globalVariables.put(variableName, collection.getValue(VARIABLE_CHOICE, workspace));
        //                 break;
        //             case VariableTypes.FILE:
        //                 String path = collection.getValue(VARIABLE_FILE, workspace);
        //                 path = path.replace("\\", "\\\\");
        //                 globalVariables.put(variableName, path);
        //                 break;
        //             case VariableTypes.FOLDER:
        //                 path = collection.getValue(VARIABLE_FOLDER, workspace);
        //                 path = path.replace("\\", "\\\\");
        //                 globalVariables.put(variableName, path);
        //                 break;
        //             case VariableTypes.TEXT:
        //                 globalVariables.put(variableName, collection.getValue(VARIABLE_VALUE, workspace));
        //                 break;
        //         }

        //     } else if (globalVariables.containsKey(variableName)) {
        //         globalVariables.remove(variableName);
        //     }
        // }

        // return parameters;

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
        MetadataRefs returnedRefs = new MetadataRefs();

        returnedRefs.add(metadataRefs.getOrPut("ImagesPath"));
        returnedRefs.add(metadataRefs.getOrPut("ThumbnailsPath"));
        returnedRefs.add(metadataRefs.getOrPut("WorkflowsPath"));

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