package io.github.mianalysis.miaserver.beans;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CloudModules {

    private Modules modules = null;

    public Modules initialiseModules(String workflowPath) {
        try {
            modules = AnalysisReader.loadModules(new File(workflowPath));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException | IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return modules;
        
    }

    public Modules getModules() {
        return modules;
    }

    @PostConstruct
    public void init() {
        // Invoked after dependencies injected
        System.out.println("Creating CloudModules");
    }

    @PreDestroy
    public void destroy() {
        // Invoked when the WebSocket session ends
        System.out.println("Destroying CloudModules");
    }

}
