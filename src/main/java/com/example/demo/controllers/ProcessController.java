package com.example.demo.controllers;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.beans.CloudWorkspace;
import com.example.demo.requests.ProcessRequest;
import com.example.demo.utils.JSONWriter;
import com.example.demo.utils.ServerImageRenderer;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.image.Image;

@Controller
public class ProcessController {
	private ServerImageRenderer serverImageRenderer = new ServerImageRenderer();

	@Autowired
	private CloudWorkspace cloudWorkspace;

	@Resource(name = "getModules")
	Modules modules;

	@MessageMapping("/process")
  	@SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<byte[]> process(ProcessRequest request) throws Exception {
		Image.setDefaultRenderer(serverImageRenderer);
		serverImageRenderer.clearLastOutput();

		modules.getModuleByID("1636961828208").updateParameterValue("Threshold multiplier", request.getThreshold());
		modules.execute(cloudWorkspace.getWorkspace());

		while (serverImageRenderer.getLastOutputImage() == null)
			Thread.sleep(10);

		return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(serverImageRenderer.getLastOutputImage());
	}

	@MessageMapping("/getparameters")
  	@SendToUser("/queue/parameters")
	public @ResponseBody ResponseEntity<String> getparameters(ProcessRequest request) throws Exception {
		System.out.println("Sending parameters to client");
		return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(JSONWriter.getModulesJSON(modules, cloudWorkspace.getWorkspace()).toString());
	}
}
