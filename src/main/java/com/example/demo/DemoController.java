package com.example.demo;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;

@Controller
public class DemoController {
	private ServerImageRenderer serverImageRenderer = new ServerImageRenderer();

	@Resource(name = "getWorkspace")
	Workspace workspace;

	@Resource(name = "getModules")
	Modules modules;

	@MessageMapping("/process")
  @SendToUser("/queue/result")
	public @ResponseBody ResponseEntity<byte[]> process(ProcessRequest request) throws Exception {
		Image.setDefaultRenderer(serverImageRenderer);
		serverImageRenderer.clearLastOutput();

		modules.getModuleByID("1636961828208").updateParameterValue("Threshold multiplier", request.getThreshold());
		modules.execute(workspace);

		while (serverImageRenderer.getLastOutputImage() == null)
			Thread.sleep(10);

		return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(serverImageRenderer.getLastOutputImage());
	}
}
