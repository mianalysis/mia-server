package io.github.mianalysis.miaserver.controllers;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.mianalysis.miaserver.beans.CloudModuleGroups;
import io.github.mianalysis.miaserver.beans.CloudModules;
import io.github.mianalysis.miaserver.beans.CloudWorkspace;
import io.github.mianalysis.miaserver.requests.SetParameterRequest;
import io.github.mianalysis.miaserver.requests.SetWorkflowRequest;
import io.github.mianalysis.miaserver.utils.JSONWriter;
import io.github.mianalysis.miaserver.utils.ModuleGroups;
import io.github.mianalysis.miaserver.utils.ProcessResult;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

@Controller
public class ProcessController {
	@Autowired
	private CloudWorkspace cloudWorkspace;

	@Autowired
	private CloudModules cloudModules;

	@Autowired
	private CloudModuleGroups cloudModuleGroups;

	// @Resource(name = "getModules")
	// private Modules modules;

	@MessageMapping("/getworkflows")
	@SendToUser("/queue/workflows")
	public @ResponseBody ResponseEntity<String> getWorkflows() throws Exception {
		String workflowsPath = "src/main/resources/mia/workflows/";
		Collection<File> workflowFiles = FileUtils.listFiles(new File(workflowsPath), new String[] { "mia" }, false);

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(JSONWriter.getWorkflowsJSON(workflowFiles).toString());

	}

	@MessageMapping("/setworkflow")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> setworkflow(SetWorkflowRequest request) throws Exception {
		String workflowPath = "src/main/resources/mia/workflows/" + request.getWorkflowName() + ".mia";
		Modules modules = cloudModules.initialiseModules(workflowPath);
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

			return processgroup();
		}
	}

	@MessageMapping("/process")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> process() throws Exception {
		Modules modules = cloudModules.getModules();

		ProcessResult.getInstance().clear();
		modules.execute(cloudWorkspace.getWorkspace());

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(ProcessResult.getInstance().toString());
	}

	@MessageMapping("/processgroup")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> processgroup() throws Exception {
		Modules modules = cloudModules.getModules();
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		ProcessResult.getInstance().clear();
		moduleGroups.getCurrentGroup().execute(modules, cloudWorkspace.getWorkspace());

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(ProcessResult.getInstance().toString());

	}

	@MessageMapping("/setparameter")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> setparameter(SetParameterRequest request) throws Exception {
		Modules modules = cloudModules.getModules();
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		for (Module module : modules.values()) {
			if (module.getModuleID().equals(request.getModuleID())) {
				if (request.getParentGroupName() == null || request.getParentGroupName().equals("")) {
					Parameter parameter = module.getParameter(request.getParameterName());
					parameter.setValueFromString(request.getParameterValue());
				} else {
					ParameterGroup parentGroup = module.getParameter(request.getParentGroupName());
					Parameter parameter = parentGroup.getCollections(true).get(request.getGroupCollectionNumber())
							.getParameter(request.getParameterName());
					parameter.setValueFromString(request.getParameterValue());
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
			return processgroup();

	}

	@MessageMapping("/previousgroup")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> previousgroup() throws Exception {
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		// Move to the previous module group. If not possible, it will return the same
		// set of modules
		moduleGroups.previousGroup();

		// Return the parameters for these modules
		return processgroup();

	}

	@MessageMapping("/haspreviousgroup")
	@SendToUser("/queue/previousstatus")
	public @ResponseBody ResponseEntity<String> haspreviousgroup() throws Exception {
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		String bodyString = moduleGroups != null ? String.valueOf(moduleGroups.hasPreviousGroup()) : "";

		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(bodyString);

	}

	@MessageMapping("/nextgroup")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> nextgroup() throws Exception {
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		// Move to the next module group. If not possible, it will return the same set
		// of modules
		moduleGroups.nextGroup();

		// Return the parameters for these modules
		return processgroup();

	}

	@MessageMapping("/hasnextgroup")
	@SendToUser("/queue/nextstatus")
	public @ResponseBody ResponseEntity<String> hasnextgroup() throws Exception {
		ModuleGroups moduleGroups = cloudModuleGroups.getModuleGroups();

		String bodyString = moduleGroups != null ? String.valueOf(moduleGroups.hasNextGroup()) : "";

		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(bodyString);

	}
}
