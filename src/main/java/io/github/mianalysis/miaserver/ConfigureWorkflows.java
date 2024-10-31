package io.github.mianalysis.miaserver;

import java.io.File;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

public class ConfigureWorkflows {
    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        File[] moduleFiles = new File("src/main/java/io/github/mianalysis/miaserver/modules/").listFiles();

        // Adding the new modules to MIA's list of available modules.
        for (File moduleFile : moduleFiles) {
            try {
                String className = "io.github.mianalysis.miaserver.modules."
                        + moduleFile.getName().replace(".java", "");
                AvailableModules.addModuleName((Class<Module>) Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}