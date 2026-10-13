package io.github.mianalysis.miaserver.utils;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.system.Status;

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
        // long t1 = System.currentTimeMillis();
        // MIA.log.writeDebug("Running analysis tester from ModuleGroup::execute");
        MIA.log.writeDebug("ModuleGroup::execute may need to have module testing reinstated to some degree for available parameters to become visible.");
        // AnalysisTester_Mod.testModules(modules, workspace, null);
        // long t2 = System.currentTimeMillis();

        // MIA.log.writeDebug("Group test time (ms): " + (t2 - t1));

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
                            Module redirectModule = modules.getModuleByID(module.getRedirectModuleID(workspace));
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
                    MIA.log.writeError(e1);
                    e1.printStackTrace();
                    return false;
                }
            }
        }

        return true;

    }
}
