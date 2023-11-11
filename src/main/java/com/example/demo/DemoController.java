package com.example.demo;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.utils.ServerImageRenderer;

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

	@GetMapping("/mia")
	public @ResponseBody ResponseEntity<byte[]> mia(@RequestParam(value = "threshold", defaultValue = "1.0") String threshold) throws Exception {
		Image.setDefaultRenderer(serverImageRenderer);
		serverImageRenderer.clearLastOutput();

		modules.getModuleByID("1636961828208").updateParameterValue("Threshold multiplier", Float.parseFloat(threshold));
		modules.execute(workspace);

		while (serverImageRenderer.getLastOutputImage() == null)
			Thread.sleep(10);

		return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(serverImageRenderer.getLastOutputImage());
	}
}
