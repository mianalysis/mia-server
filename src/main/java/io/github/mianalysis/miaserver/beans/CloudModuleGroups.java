package io.github.mianalysis.miaserver.beans;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import io.github.mianalysis.miaserver.utils.ModuleGroups;

import io.github.mianalysis.mia.module.Modules;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CloudModuleGroups {

    private ModuleGroups moduleGroups = null;

    public ModuleGroups initialiseModuleGroups(Modules modules) {
        moduleGroups = new ModuleGroups(modules);

        return moduleGroups;

    }

    public ModuleGroups getModuleGroups() {
        return moduleGroups;
    }

    @PostConstruct
    public void init() {
        // Invoked after dependencies injected
    }

    @PreDestroy
    public void destroy() {
        // Invoked when the WebSocket session ends
    }
}
