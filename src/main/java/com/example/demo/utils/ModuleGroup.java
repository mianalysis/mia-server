package com.example.demo.utils;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;

public class ModuleGroup {
    int startIdx = -1;
    int endIdx = -1;
    String title = "";
    String description = "";    

    public ModuleGroup(int startIdx, int endIdx, String title, String description) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.title = title;
        this.description = description;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }

    public Modules getModules(Modules modules) {
        Modules groupModules = new Modules();
        groupModules.setInputControl(modules.getInputControl());
        groupModules.setOutputControl(modules.getOutputControl());

        for (int idx = startIdx; idx < endIdx; idx++)
            groupModules.add(modules.get(idx));
        
        return groupModules;

    }

    public boolean execute(Modules modules, Workspace workspace) {
        AnalysisTester.testModules(modules, workspace, null);

        for (int idx = startIdx; idx < endIdx; idx++) {
            Module module = modules.get(idx);

            for (Parameter p : module.updateAndGetParameters().values()) {
                if (p instanceof ParameterGroup) {
                    for (Parameters pp : ((ParameterGroup) p).getCollections(true).values())
                        pp.values();
                }
            }

            if (module.isEnabled() && module.isRunnable()) {
                try {
                    Status success = module.execute(workspace);
                    switch (success) {
                        case REDIRECT:
                            // Getting index of module before one to move to
                            Module redirectModule = module.getRedirectModule(workspace);
                            idx = modules.indexOf(redirectModule) - 1;
                            break;
                        case PASS:
                        case TERMINATE:
                        case TERMINATE_SILENT:
                            break;
                        case FAIL:
                            System.err.println("Module " + module.getName() + " failed to complete.");
                            return false;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return false;
                }
            }
        }

        return true;
        
    }
}
