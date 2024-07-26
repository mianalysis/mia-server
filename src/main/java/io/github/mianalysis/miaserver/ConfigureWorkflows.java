package io.github.mianalysis.miaserver;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.miaserver.modules.AddPathsToMetadata;
import io.github.mianalysis.miaserver.modules.DisplayGraph;
import io.github.mianalysis.miaserver.modules.DisplayImage;
import io.github.mianalysis.miaserver.modules.DisplayMessage;
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

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(AddPathsToMetadata.class);
        AvailableModules.addModuleName(DisplayGraph.class);
        AvailableModules.addModuleName(DisplayImage.class);
        AvailableModules.addModuleName(DisplayMessage.class);

    }
}