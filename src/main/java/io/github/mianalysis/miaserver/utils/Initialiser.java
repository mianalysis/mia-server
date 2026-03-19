package io.github.mianalysis.miaserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.scijava.Context;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptService;
import org.scijava.ui.UIService;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import net.imagej.patcher.LegacyInjector;
import weka.core.converters.JSONLoader;

public class Initialiser {
    public static void main(String[] args) {
        new Initialiser().initialise();
    }
    static {
        LegacyInjector.preinit();
    }
        
    public void initialise() {
        System.out.println("Initialising MIA");
        MIA.setHeadless(false);

        try {
            System.out.println("Creating new Context");
            Context context = new Context(PluginService.class, ScriptService.class, UIService.class);
            System.out.println("Created context: "+context);

            PluginService pluginService = context.getService(PluginService.class);
            System.out.println("Created plugin service: "+pluginService);
            ScriptService scriptService = context.getService(ScriptService.class);
            System.out.println("Created script service: "+scriptService);
            UIService uiService = context.getService(UIService.class);
            System.out.println("Created UI service: "+uiService);

            MIA.setPluginService(pluginService);
            MIA.setScriptService(scriptService);
            MIA.setUIService(uiService);

        } catch (Exception e) {
            System.out.println("Failed to create services");
            e.printStackTrace();
        }

        // Loading all modules into AvailableModules list
        InputStream stream = Initialiser.class.getClassLoader().getResourceAsStream("mia/modules.json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        JSONObject json;
        try {
            json = new JSONObject(new BufferedReader(streamReader).readLine());
            json.getJSONArray("modules").forEach(moduleName -> AvailableModules.addModuleName((String) moduleName));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }       

        System.out.println("MIA initialised");

    }

    // @Override
    // public void run() {
    //     throw new UnsupportedOperationException("Unimplemented method 'run'");
    // }
}
