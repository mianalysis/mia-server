package io.github.mianalysis.miaserver.utils;

import java.io.File;

import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

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

  public static int getWorkspaceCount() {
    return workspaceCount;
  }
}