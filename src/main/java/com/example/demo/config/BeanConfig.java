// package com.example.demo.config;

// import java.io.File;
// import java.io.FileFilter;
// import java.util.Collection;

// import javax.swing.filechooser.FileNameExtensionFilter;

// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Scope;
// import org.springframework.context.annotation.ScopedProxyMode;

// @Configuration
// public class BeanConfig {
//     @Bean
//     @Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
//     public Modules getModules(String workflowName) throws Exception {
//         String workflowPath = "src/main/resources/mia/workflows/"+workflowName;

//         return AnalysisReader.loadModules(new File(workflowPath));

//     }
// }
