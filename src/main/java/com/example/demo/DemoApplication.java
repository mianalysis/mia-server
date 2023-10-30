package com.example.demo;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.Resource;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/mia")
	public @ResponseBody ResponseEntity<Resource> mia(@RequestParam(value = "threshold", defaultValue = "1.0") String threshold) throws Exception {
		String inputFilePath = "src/main/resources/mia/TestImage.tif";
		String workflowPath = "src/main/resources/mia/ExampleWorkflow.mia";

		Workspaces workspaces = new Workspaces();
		Workspace workspace = workspaces.getNewWorkspace(new File(inputFilePath), 1);

		Modules modules = AnalysisReader.loadModules(new File(workflowPath));
        modules.getModuleByID("1636961828208").updateParameterValue("Threshold multiplier", Float.parseFloat(threshold));
		modules.execute(workspace);

		Resource imageResource = new ClassPathResource("mia/TestImage_S1_binary.png");

		return ResponseEntity.ok()
				.contentType(new MediaType("image", "png"))
				.body(imageResource);
	}
}
