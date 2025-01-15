package io.github.mianalysis.miaserver.beans;

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
  private static int workspaceCount = 0;

  public Workspace initialiseWorkspace(String inputPath) {
    Workspaces workspaces = new Workspaces();
    workspace = workspaces.getNewWorkspace(new File(inputPath), 1);

    return workspace;

  }

  public Workspace getWorkspace() {
    return workspace;
  }

  @PostConstruct
  public void init() {
    // Invoked after dependencies injected
    workspaceCount++;
    System.out.println("Creating CloudWorkspace");
  }

  @PreDestroy
  public void destroy() {
    // Invoked when the WebSocket session ends
    workspaceCount--;
    System.out.println("Destroying CloudWorkspace");
  }

  public static int getWorkspaceCount() {
    return workspaceCount;
  }
}
