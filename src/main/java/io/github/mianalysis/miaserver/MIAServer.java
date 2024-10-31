package io.github.mianalysis.miaserver;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import net.imagej.patcher.LegacyInjector;

@SpringBootApplication
public class MIAServer {
	static {
        LegacyInjector.preinit();
    }
	
	public static void main(String[] args) {
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
        
		SpringApplication.run(MIAServer.class, args);

	}
}
