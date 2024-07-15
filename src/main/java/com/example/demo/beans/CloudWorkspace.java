package com.example.demo.beans;

import java.io.File;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CloudWorkspace {

  private Workspace workspace = null;

  // public CloudWorkspace() throws Exception {
  //   String inputFilePath = "src/main/resources/mia/images/What is an image_RGB.tif";
  //   // String inputFilePath = "src/main/resources/mia/images/SmallRGB.tif";

  //   Workspaces workspaces = new Workspaces();

  //   workspace = workspaces.getNewWorkspace(new File(inputFilePath), 1);
    
  // }

  public void initialiseWorkspace(String inputPath) {
    Workspaces workspaces = new Workspaces();
    workspace = workspaces.getNewWorkspace(new File(inputPath), 1);
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  @PostConstruct
  public void init() {
    // Invoked after dependencies injected
    System.out.println("Creating CloudWorkspace");
  }

  @PreDestroy
  public void destroy() {
    // Invoked when the WebSocket session ends
    System.out.println("Destroying CloudWorkspace");
  }
  
}
