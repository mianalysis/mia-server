package io.github.mianalysis.miaserver.controllers;

import java.io.File;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.miaserver.utils.CloudModuleGroups;
import io.github.mianalysis.miaserver.utils.CloudModules;
import io.github.mianalysis.miaserver.utils.CloudWorkspace;
import io.github.mianalysis.miaserver.utils.Initialiser;
import io.github.mianalysis.miaserver.utils.JSONWriter;
import io.github.mianalysis.miaserver.utils.ModuleGroups;
import io.github.mianalysis.miaserver.utils.ProcessResult;

class ProcessController {
    public static void main(String[] args) {
        new Initialiser().initialise();
    }

    private CloudWorkspace cloudWorkspace = new CloudWorkspace();
    private CloudModules cloudModules = new CloudModules();
    private CloudModuleGroups cloudModuleGroups = new CloudModuleGroups();

    private boolean processActive = false;

    public ProcessResult setWorkflow(String workflowXML, String workflowPath) throws Exception {
        try {
            if (cloudModules == null)
                cloudModules = new CloudModules();

            if (cloudWorkspace == null)
                cloudWorkspace = new CloudWorkspace();

            if (cloudModuleGroups == null)
                cloudModuleGroups = new CloudModuleGroups();

            Modules modules = cloudModules.initialiseModules(workflowXML);
            modules.setAnalysisFilename(workflowPath);
            GlobalVariables.updateVariables(modules);

            String inputPath = modules.getInputControl().getParameterValue(InputControl.INPUT_PATH, null);
            Workspace workspace = cloudWorkspace.initialiseWorkspace(inputPath);

            ModuleGroups moduleGroups = cloudModuleGroups.initialiseModuleGroups(modules);

            ProcessResult.getInstance().clear();

            if (moduleGroups == null) {
                return process();
            } else {
                if (moduleGroups.hasPreprocessingGroup())
                    moduleGroups.getPreprocessingGroup().execute(modules, workspace);

                return processGroup();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ProcessResult process() throws Exception {
        if (processActive) {
            System.out.println("Process busy");
            return null;
        }

        try {
            processActive = true;
            Modules modules = cloudModules.getModules();
            File workflowFile = new File(modules.getAnalysisFilename());

            ProcessResult.getInstance().clear();
            ProcessResult.getInstance().workflowName = JSONWriter.getWorkflowDisplayName(workflowFile);
            ProcessResult.getInstance().modules = JSONWriter.getModulesJSON(modules, cloudWorkspace.getWorkspace());
            modules.execute(cloudWorkspace.getWorkspace());

            processActive = false;

        } catch (Exception e) {
            e.printStackTrace();
            processActive = false;
        }

        ProcessResult result = ProcessResult.getInstance();

        return result;

    }

    public ProcessResult processGroup() throws Exception {
        if (processActive) {
            System.out.println("Process busy");
            return null;
        }

        try {
            processActive = true;

            Modules modules = cloudModules.getModules();
            ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();
            File workflowFile = new File(modules.getAnalysisFilename());

            ProcessResult.getInstance().clear();
            ProcessResult.getInstance().workflowName = JSONWriter.getWorkflowDisplayName(workflowFile);
            ProcessResult.getInstance().modules = JSONWriter
                    .getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules), cloudWorkspace.getWorkspace());

            // SLOW PROCESS
            long t1 = System.currentTimeMillis();
            moduleGroups.getCurrentGroup().execute(modules, cloudWorkspace.getWorkspace());
            long t2 = System.currentTimeMillis();

            MIA.log.writeDebug("Group process time (ms): " + (t2 - t1));

            processActive = false;

        } catch (Exception e) {
            e.printStackTrace();
            processActive = false;
        }

        // VERY SLOW PROCESS
        ProcessResult result = ProcessResult.getInstance();

        return result;

    }

    public ProcessResult setParameter(String moduleID, String parameterName, String parameterValue,
            String parentGroupName,
            int groupCollectionNumber) throws Exception {
        Modules modules = cloudModules.getModules();
        ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

        for (Module module : modules.values()) {
            if (module.getModuleID().equals(moduleID)) {
                if (parentGroupName == null ||
                        parentGroupName.equals("")) {
                    Parameter parameter = module.getParameter(parameterName);
                    parameter.setValueFromString(parameterValue);
                } else {
                    Parameter parentGroup = module.getParameter(parentGroupName);
                    Parameter parameter = ((ParameterGroup) parentGroup).getCollections(true).get(groupCollectionNumber)
                            .getParameter(parameterName);
                    parameter.setValueFromString(parameterValue);
                }

                break;
            }
        }

        // Runtime runtime = Runtime.getRuntime();
        // System.out.println("Memory used =
        // "+(runtime.totalMemory()-runtime.freeMemory())/(1048576L)+", total users
        // "+CloudWorkspace.getWorkspaceCount());

        // if (moduleGroups == null)
        // return ResponseEntity.ok()
        // .contentType(MediaType.APPLICATION_JSON)
        // .body(JSONWriter.getModulesJSON(modules,
        // cloudWorkspace.getWorkspace()).toString());
        // else
        // return ResponseEntity.ok()
        // .contentType(MediaType.APPLICATION_JSON)
        // .body(JSONWriter.getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules),
        // cloudWorkspace.getWorkspace()).toString());

        if (moduleGroups == null)
            return process();
        else
            return processGroup();

    }

    public ProcessResult previousGroup() throws Exception {
        ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

        // Move to the previous module group. If not possible, it will return the same
        // set of modules
        moduleGroups.previousGroup();

        // Return the parameters for these modules
        return processGroup();

    }

    public boolean hasPreviousGroup() throws Exception {
        ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

        return moduleGroups != null ? moduleGroups.hasPreviousGroup() : false;

    }

    public ProcessResult nextGroup() throws Exception {
        ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

        // Move to the next module group. If not possible, it will return the same set
        // of modules
        moduleGroups.nextGroup();

        // Return the parameters for these modules
        return processGroup();

    }

    public boolean hasNextGroup() throws Exception {
        ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

        return moduleGroups != null ? moduleGroups.hasNextGroup() : false;

    }
}