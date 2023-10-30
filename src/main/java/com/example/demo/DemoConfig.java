package com.example.demo;

import java.io.File;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class DemoConfig {
	
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Workspace getWorkspace() {
        String inputFilePath = "src/main/resources/mia/TestImage.tif";

        Workspaces workspaces = new Workspaces();

        return workspaces.getNewWorkspace(new File(inputFilePath), 1);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Modules getModules() throws Exception {
        String workflowPath = "src/main/resources/mia/ExampleWorkflow.mia";
        
        return AnalysisReader.loadModules(new File(workflowPath));
    }
}
