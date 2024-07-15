package com.example.demo;

import com.example.demo.modules.AddPathsToMetadata;
import com.example.demo.modules.DisplayImage;
import com.example.demo.modules.DisplayMessage;

import io.github.mianalysis.mia.module.AvailableModules;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

public class TestRun {
    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(AddPathsToMetadata.class);
        AvailableModules.addModuleName(DisplayImage.class);
        AvailableModules.addModuleName(DisplayMessage.class);

    }
}