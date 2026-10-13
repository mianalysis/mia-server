package io.github.mianalysis.miaserver.utils;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

public class CloudModules {

    private Modules modules = null;

    public Modules initialiseModules(String workflowXML) {
        try {
            modules = AnalysisReader.loadModules(workflowXML);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return modules;
        
    }

    public Modules getModules() {
        return modules;
    }
}