package io.github.mianalysis.example;

import java.io.File;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

public class MinimalExample {
    public static void main(String[] args) throws Exception {
        String inputFilePath = "F:\\Java Projects\\mia-server\\TestImage.tif";
        String workflowPath = "F:\\Java Projects\\mia-server\\ExampleWorkflow.mia";
                
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(new File(inputFilePath), 1);

        Modules modules = AnalysisReader.loadModules(new File(workflowPath));
        modules.execute(workspace);

    }
}