package io.github.mianalysis.miaserver.modules;

import java.util.HashMap;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptModule;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.script.RunScript;
import io.github.mianalysis.mia.module.script.RunScript.ParameterTypes;
import io.github.mianalysis.mia.module.script.RunScript.ScriptLanguages;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.parameters.ClickP;

/**
 * Created by Stephen Cross on 23/11/2018.
 */

/**
 * Implement workflow handling outcome based on whether value is true. Outcomes
 * can include termination of the analysis and redirection of the active module
 * to another part of the workflow. Redirection allows parts of the analysis to
 * skipped.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class PerformAction extends Module {

    public static final String EXECUTION_SEPARATOR = "Execution controls";

    public static final String PERFORM_ACTION = "Perform action";

    public static final String SCRIPT_SEPARATOR = "Script controls";

    public static final String SCRIPT_LANGUAGE = "Script language";

    public static final String SCRIPT_TEXT = "Script text";

    public PerformAction(Modules modules) {
        super("Perform action", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.WORKFLOW;
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
        // Get parameters
        String scriptLanguage = parameters.getValue(SCRIPT_LANGUAGE, workspace);
        String scriptText = parameters.getValue(SCRIPT_TEXT, workspace);

        boolean performAction = ((ClickP) parameters.get(PERFORM_ACTION)).getAndSetFalse(workspace);
        if (!performAction)
            return Status.PASS;

        String extension = RunScript.getLanguageExtension(scriptLanguage);

        Map<String, Object> scriptParameters = new HashMap<>();
        
        if (scriptText.contains("@ io.github.mianalysis.mia.object.Workspace workspace"))
            scriptParameters.put("workspace", workspace);

        if (scriptText.contains("@ io.github.mianalysis.mia.module.Module thisModule"))
            scriptParameters.put("thisModule", this);

        // Resolving moved files
        scriptText = RunScript.redirectImports(scriptText, extension);

        // Running script
        try {
            ScriptModule scriptModule = MIA.getScriptService().run("." + extension, scriptText, false, scriptParameters)
                    .get();
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new ClickP(PERFORM_ACTION, this, false));
        parameters.add(new SeparatorP(SCRIPT_SEPARATOR, this));
        parameters.add(new ChoiceP(SCRIPT_LANGUAGE, this, ScriptLanguages.IMAGEJ1, ScriptLanguages.ALL));
        parameters.add(new TextAreaP(SCRIPT_TEXT, this,
                "// The following two parameters will provide references to the workspace and current module.\n#@ io.github.mianalysis.mia.object.Workspace workspace\n#@ io.github.mianalysis.mia.module.Module thisModule\n\nimport io.github.mianalysis.mia.MIA",
                true));

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
}
