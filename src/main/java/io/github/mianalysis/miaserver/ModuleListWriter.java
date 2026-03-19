package io.github.mianalysis.miaserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.mianalysis.mia.module.AvailableModules;

/**
 * Class to write a list of all available modules.  This is read by the server to determine the list of available modules.
 */
public class ModuleListWriter {
    public static void main(String[] args) {
        ModuleListWriter.writeModuleList();
    }

    public static void writeModuleList() {
        // Get a list of core MIA modules
        List<String> moduleNames = AvailableModules.getModuleNames(true);

        // Adding local modules
        File[] moduleFiles = new File("src/main/java/io/github/mianalysis/miaserver/modules/").listFiles();
        for (File moduleFile : moduleFiles) {
                String className = "io.github.mianalysis.miaserver.modules."+ moduleFile.getName().replace(".java", "");
                AvailableModules.addModuleName(className);
        }

        // Writing modules list as a JSON
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (String moduleName : moduleNames)
            jsonArray.put(moduleName);

        json.put("modules", jsonArray);

        try {
            new File("src/main/resources/mia/").mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/mia/modules.json"));
            writer.write(json.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
