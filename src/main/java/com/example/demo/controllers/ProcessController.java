package com.example.demo.controllers;

import javax.annotation.Resource;

import org.jfree.data.json.impl.JSONArray;
import org.jfree.data.json.impl.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.beans.CloudWorkspace;
import com.example.demo.requests.SetParameterRequest;
import com.example.demo.utils.JSONWriter;
import com.example.demo.utils.ModuleGroups;
import com.example.demo.utils.ProcessResult;
import com.example.demo.utils.ServerImageRenderer;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

@Controller
public class ProcessController {
	@Autowired
	private CloudWorkspace cloudWorkspace;

	@Resource(name = "getModules")
	Modules modules;

	// Optional
	private ModuleGroups moduleGroups;

	@MessageMapping("/process")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> process() throws Exception {
		ProcessResult.clear();
		modules.execute(cloudWorkspace.getWorkspace());

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(ProcessResult.getResultJSON().toString());
	}

	// @MessageMapping("/getimage")
	// @SendToUser("/queue/image")
	// public @ResponseBody ResponseEntity<byte[]> getimage() throws Exception {
	// 	return ResponseEntity.ok()
	// 			.contentType(MediaType.IMAGE_PNG)
	// 			.body(ProcessResult.getImage());
	// }

	@MessageMapping("/getparameters")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> getparameters() throws Exception {
		if (moduleGroups == null)
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(JSONWriter.getModulesJSON(modules, cloudWorkspace.getWorkspace()).toString());
		else
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(JSONWriter.getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules),
							cloudWorkspace.getWorkspace()).toString());

	}

	@MessageMapping("/setparameter")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> setparameter(SetParameterRequest request) throws Exception {
		for (Module module : modules.values()) {
			if (module.getModuleID().equals(request.getModuleID())) {
				Parameter parameter = module.getParameter(request.getParameterName());
				parameter.setValueFromString(request.getParameterValue());
				break;
			}
		}

		if (moduleGroups == null)
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(JSONWriter.getModulesJSON(modules, cloudWorkspace.getWorkspace()).toString());
		else
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(JSONWriter.getModulesJSON(moduleGroups.getCurrentGroup().getModules(modules),
							cloudWorkspace.getWorkspace()).toString());

	}

	@MessageMapping("/enablemodulegroups")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> enablemodulegroups() throws Exception {
		moduleGroups = new ModuleGroups(modules);

		// If pre-processing modules are present, run these first (these are modules
		// before the first GUISeparator)
		if (moduleGroups.hasPreprocessingGroup())
			moduleGroups.getPreprocessingGroup().execute(modules, cloudWorkspace.getWorkspace());

		// Get the first set of modules
		Modules groupModules = moduleGroups.getCurrentGroup().getModules(modules);

		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(JSONWriter.getModulesJSON(groupModules, cloudWorkspace.getWorkspace()).toString());

	}

	@MessageMapping("/previousgroup")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> previousgroup() throws Exception {
		// Move to the previous module group. If not possible, it will return the same
		// set of modules
		moduleGroups.previousGroup();

		// Get the first set of modules
		Modules groupModules = moduleGroups.getCurrentGroup().getModules(modules);

		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(JSONWriter.getModulesJSON(groupModules, cloudWorkspace.getWorkspace()).toString());

	}

	@MessageMapping("/haspreviousgroup")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> haspreviousgroup() throws Exception {
		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(String.valueOf(moduleGroups.hasPreviousGroup()));

	}

	@MessageMapping("/nextgroup")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> nextgroup() throws Exception {
		// Move to the next module group. If not possible, it will return the same set
		// of modules
		moduleGroups.nextGroup();

		// Get the first set of modules
		Modules groupModules = moduleGroups.getCurrentGroup().getModules(modules);

		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(JSONWriter.getModulesJSON(groupModules, cloudWorkspace.getWorkspace()).toString());

	}

	@MessageMapping("/hasnextgroup")
	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> hasnextgroup() throws Exception {
		// Return the parameters for these modules
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(String.valueOf(moduleGroups.hasNextGroup()));

	}

	@MessageMapping("/processgroup")
	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<String> processgroup() throws Exception {
		ProcessResult.clear();
		moduleGroups.getCurrentGroup().execute(modules, cloudWorkspace.getWorkspace());

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(ProcessResult.getResultJSON().toString());
	}
}
