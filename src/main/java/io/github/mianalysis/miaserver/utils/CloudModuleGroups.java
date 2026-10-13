package io.github.mianalysis.miaserver.utils;

import io.github.mianalysis.mia.module.Modules;

public class CloudModuleGroups {

    private ModuleGroups moduleGroups = null;

    public ModuleGroups initialiseModuleGroups(Modules modules) {
        moduleGroups = new ModuleGroups(modules);

        return moduleGroups;

    }

    public ModuleGroups getModuleGroups() {
        return moduleGroups;
    }
}