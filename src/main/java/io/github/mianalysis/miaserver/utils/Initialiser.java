package io.github.mianalysis.miaserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;
import org.scijava.Context;
import org.scijava.plugin.PluginService;
import org.scijava.plugins.scripting.beanshell.BeanshellScriptLanguage;
import org.scijava.script.ScriptService;
import org.scijava.ui.UIService;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import net.imagej.patcher.LegacyInjector;

public class Initialiser {
    static {
        LegacyInjector.preinit();
    }

    public void initialise() {
        MIA.setHeadless(false);

        try {
            Context context = new Context(PluginService.class, ScriptService.class, UIService.class);

            PluginService pluginService = context.getService(PluginService.class);
            ScriptService scriptService = context.getService(ScriptService.class);
            UIService uiService = context.getService(UIService.class);

            MIA.setPluginService(pluginService);
            MIA.setScriptService(scriptService);
            MIA.setUIService(uiService);

            scriptService.getIndex().add(new BeanshellScriptLanguage());
            System.out.println("Initialised Beanshell");
            // scriptService.getIndex().add(new GroovyScriptLanguage());
            // System.out.println("Initialised Groovy");
            // scriptService.getIndex().add(new IJ1MacroLanguage());
            // System.out.println("Initialised IJ1");
            // scriptService.getIndex().add(new JythonScriptLanguage());
            // System.out.println("Initialised Jython");
            System.out.println("Script service index: " + scriptService.getIndex());

        } catch (Exception e) {
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
    // throw new UnsupportedOperationException("Unimplemented method 'run'");
    // }
}
