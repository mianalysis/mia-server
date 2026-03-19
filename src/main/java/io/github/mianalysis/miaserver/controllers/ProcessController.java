package io.github.mianalysis.miaserver.controllers;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
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

    // // @Resource(name = "getModules")
    // // private Modules modules;

    // @MessageMapping("/getworkflows")
    // @SendToUser("/queue/workflows")
    // public @ResponseBody ResponseEntity<String> getWorkflows() throws Exception {
    // String workflowsPath = "src/main/resources/mia/workflows/";
    // Collection<File> workflowFiles = FileUtils.listFiles(new File(workflowsPath),
    // new String[] { "mia" }, false);

    // return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
    // .body(JSONWriter.getWorkflowsJSON(workflowFiles).toString());

    // }

    public String setWorkflow(String workflowXML, String workflowPath) throws Exception {
        try {
            if (cloudModules == null)
                cloudModules = new CloudModules();

            if (cloudWorkspace == null)
                cloudWorkspace = new CloudWorkspace();

            if (cloudModuleGroups == null)
                cloudModuleGroups = new CloudModuleGroups();

            Modules modules = cloudModules.initialiseModules(workflowXML);
            System.out.println(modules);
            System.out.println(modules.size());
            modules.setAnalysisFilename(workflowPath);
            System.out.println("Set analysis filename");
            GlobalVariables.updateVariables(modules);
            System.out.println("Updated global variables");

            System.out.println("Input control: "+modules.getInputControl());

            String inputPath = modules.getInputControl().getParameterValue(InputControl.INPUT_PATH, null);
            System.out.println("Input path: "+inputPath);
            System.out.println("Cloud workspace: "+cloudWorkspace);
            Workspace workspace = cloudWorkspace.initialiseWorkspace(inputPath);
            System.out.println("Workspace: "+workspace);

            ModuleGroups moduleGroups = cloudModuleGroups.initialiseModuleGroups(modules);
            System.out.println("Module groups: "+moduleGroups);

            ProcessResult.getInstance().clear();
            System.out.println("Cleared process result");

            if (moduleGroups == null) {
                return process();
            } else {
                if (moduleGroups.hasPreprocessingGroup())
                    moduleGroups.getPreprocessingGroup().execute(modules, workspace);

                return processGroup();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String process() throws Exception {
        if (processActive) {
            System.out.println("Process busy");
            return null;
        }

        try {
            processActive = true;
            Modules modules = cloudModules.getModules();
            File workflowFile = new File(modules.getAnalysisFilename());

            ProcessResult.getInstance().clear();
            ProcessResult.getInstance().put("workflowName", JSONWriter.getWorkflowDisplayName(workflowFile));
            ProcessResult.getInstance().put("modules",
                    JSONWriter.getModulesJSON(modules, cloudWorkspace.getWorkspace()));
            File backgroundFile = new File(workflowFile.getParentFile().getParent() + "/background/"
                    + FilenameUtils.getBaseName(workflowFile.getName()) + ".json");
            ProcessResult.getInstance().put("background", JSONWriter.getJSONFromFile(backgroundFile));
            modules.execute(cloudWorkspace.getWorkspace());

            processActive = false;

        } catch (Exception e) {
            e.printStackTrace();
            processActive = false;
        }

        return ProcessResult.getInstance().toString();
    }

    public String processGroup() throws Exception {
        if (processActive) {
            System.out.println("Process busy");
            return null;
        }
        // return
        // ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("busy");

        try {
            processActive = true;

            Modules modules = cloudModules.getModules();
            ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();
            File workflowFile = new File(modules.getAnalysisFilename());

            ProcessResult.getInstance().clear();
            ProcessResult.getInstance().put("workflowName", JSONWriter.getWorkflowDisplayName(workflowFile));
            ProcessResult.getInstance().put("modules", JSONWriter
                    .getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules), cloudWorkspace.getWorkspace()));

            File backgroundFile = new File(workflowFile.getParentFile().getParent() + "/background/"
                    + FilenameUtils.getBaseName(workflowFile.getName()) + ".json");
            ProcessResult.getInstance().put("background", JSONWriter.getJSONFromFile(backgroundFile));

            moduleGroups.getCurrentGroup().execute(modules, cloudWorkspace.getWorkspace());

            processActive = false;

        } catch (Exception e) {
            e.printStackTrace();
            processActive = false;
        }

        return ProcessResult.getInstance().toString();

    }

    // @MessageMapping("/setparameter")
    // @SendToUser("/queue/result")
    // public @ResponseBody ResponseEntity<String> setparameter(SetParameterRequest
    // request) throws Exception {
    // Modules modules = cloudModules.getModules();
    // ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

    // for (Module module : modules.values()) {
    // if (module.getModuleID().equals(request.getModuleID())) {
    // if (request.getParentGroupName() == null ||
    // request.getParentGroupName().equals("")) {
    // Parameter parameter = module.getParameter(request.getParameterName());
    // parameter.setValueFromString(request.getParameterValue());
    // } else {
    // ParameterGroup parentGroup =
    // module.getParameter(request.getParentGroupName());
    // Parameter parameter =
    // parentGroup.getCollections(true).get(request.getGroupCollectionNumber())
    // .getParameter(request.getParameterName());
    // parameter.setValueFromString(request.getParameterValue());
    // }

    // break;
    // }
    // }

    // // Runtime runtime = Runtime.getRuntime();
    // // System.out.println("Memory used =
    // // "+(runtime.totalMemory()-runtime.freeMemory())/(1048576L)+", total users
    // // "+CloudWorkspace.getWorkspaceCount());

    // // if (moduleGroups == null)
    // // return ResponseEntity.ok()
    // // .contentType(MediaType.APPLICATION_JSON)
    // // .body(JSONWriter.getModulesJSON(modules,
    // // cloudWorkspace.getWorkspace()).toString());
    // // else
    // // return ResponseEntity.ok()
    // // .contentType(MediaType.APPLICATION_JSON)
    // //
    // .body(JSONWriter.getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules),
    // // cloudWorkspace.getWorkspace()).toString());

    // if (moduleGroups == null)
    // return process();
    // else
    // return processgroup();

    // }

    // @MessageMapping("/previousgroup")
    // @SendToUser("/queue/result")
    // public @ResponseBody ResponseEntity<String> previousgroup() throws Exception
    // {
    // ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

    // // Move to the previous module group. If not possible, it will return the
    // same
    // // set of modules
    // moduleGroups.previousGroup();

    // // Return the parameters for these modules
    // return processgroup();

    // }

    // @MessageMapping("/haspreviousgroup")
    // @SendToUser("/queue/previousstatus")
    // public @ResponseBody ResponseEntity<String> haspreviousgroup() throws
    // Exception {
    // ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

    // String bodyString = moduleGroups != null ?
    // String.valueOf(moduleGroups.hasPreviousGroup()) : "";

    // // Return the parameters for these modules
    // return ResponseEntity.ok()
    // .contentType(MediaType.TEXT_PLAIN)
    // .body(bodyString);

    // }

    // @MessageMapping("/nextgroup")
    // @SendToUser("/queue/result")
    // public @ResponseBody ResponseEntity<String> nextgroup() throws Exception {
    // ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

    // // Move to the next module group. If not possible, it will return the same
    // set
    // // of modules
    // moduleGroups.nextGroup();

    // // Return the parameters for these modules
    // return processgroup();

    // }

    // @MessageMapping("/hasnextgroup")
    // @SendToUser("/queue/nextstatus")
    // public @ResponseBody ResponseEntity<String> hasnextgroup() throws Exception {
    // ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

    // String bodyString = moduleGroups != null ?
    // String.valueOf(moduleGroups.hasNextGroup()) : "";

    // // Return the parameters for these modules
    // return
    // ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(bodyString);

    // }
    // }
}