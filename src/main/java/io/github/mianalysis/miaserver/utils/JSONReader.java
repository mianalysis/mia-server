package io.github.mianalysis.miaserver.utils;
// package io.github.mianalysis.miaserver.utils;

// import java.io.BufferedReader;
// import java.io.File;
// import java.io.FileReader;
// import java.io.IOException;
// import java.lang.reflect.InvocationTargetException;

// import javax.xml.parsers.ParserConfigurationException;

// import org.json.JSONArray;
// import org.json.JSONObject;
// import org.xml.sax.SAXException;

// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
// import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

// public class JSONReader {
//     public static void main(String[] args)
//             throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
//             InvocationTargetException, IOException, ParserConfigurationException, SAXException, InterruptedException {
//         String workflowPath = "src/main/resources/mia/Ex1_NucleiSegmentation.mia";
//         Modules modules = AnalysisReader.loadModules(new File(workflowPath));

//         String jsonPath = "src/main/resources/mia/Ex1.json";
//         JSONObject jsonObject = new JSONObject(new BufferedReader(new FileReader(jsonPath)).readLine());

//         setParametersFromJSON(jsonObject, modules);

//     }

//     public static void setParametersFromJSON(JSONObject jsonObject, Modules modules) {
//         JSONArray jsonModules = jsonObject.getJSONArray("modules");
//         for (int i = 0; i < jsonModules.length(); i++)
//             for (Module module : modules.values())
//                 if (module.getModuleID().equals(jsonModules.getJSONObject(i).getString("id")))
//                     updateModule(jsonModules.getJSONObject(i), module);

//     }

//     public static void updateModule(JSONObject jsonModule, Module module) {
//         module.setEnabled(jsonModule.getBoolean("enabled"));

//         JSONArray jsonParameters = jsonModule.getJSONArray("parameters");
//         for (int i = 0; i < jsonParameters.length(); i++)
//             for (Parameter parameter : module.getAllParameters().values())
//                 if (parameter.getName().equals(jsonParameters.getJSONObject(i).getString("name")))
//                     parameter.setValueFromString(jsonParameters.getJSONObject(i).getString("value"));

//     }
// }
